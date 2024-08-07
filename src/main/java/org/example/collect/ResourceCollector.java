package org.example.collect;

import com.google.gson.Gson;
import org.example.product.kafka.producer.Producer;
import org.example.vo.*;
import org.example.vo.network.Adapter;
import org.example.vo.network.InterfaceInfo;
import org.example.vo.network.Nic;
import org.example.vo.network.DefaultNetworkInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


/**
 * 실제 데이터를 수집하는 부분
 * network, system 정보들을 모두 취합한다
 */
public class ResourceCollector {

    private static class SingleTon{
        private static ResourceCollector instance = new ResourceCollector();
    }

    public static ResourceCollector getInstance(){
        return SingleTon.instance;
    }

    private AgentInfo agentInfo = new AgentInfo();

    private List<ServerInfo> serverInfo = new ArrayList();

    private OsInfo osInfo = new OsInfo();

//    private String osName = "";
//    private String osVersion = "";
//    private String osArch = "";
    private String hostName = "";
    private List<String> gateways = new ArrayList<String>();
    private List<String> ifNames = new ArrayList();
    private List<String> ips = new ArrayList<String>();
    private List<String> macs = new ArrayList<String>();
    private DefaultNetworkInfo defaultNetworkInfo = new DefaultNetworkInfo();

    private long total_memory = 0;

    private final int SLEEP_SECOND = 10;

    ServerInfoOs OS;

    public void init(ServerInfoOs serverInfoOs) {
        this.OS = serverInfoOs;
        running();
    }

    /**
     * 데이터를 수집하고 전송한다.
     */
    public void running(){
        List<Processes> afterTopData = new ArrayList<Processes>();
        List<Processes> addTopData = new ArrayList<Processes>();
        List<Processes> deleteTopData = new ArrayList<Processes>();
        List<Resource> resourceData = new ArrayList<Resource>();
        serverInfo = new ArrayList();

        /**
         * 데이터 수집 시작
         */
//        collectData();

        //Process 정보
        this.OS.runningProcess(afterTopData, addTopData, deleteTopData, this.total_memory);

        //Resource 정보
        this.OS.runningResource(resourceData);

        //Network Nic 정보
        this.OS.runningNic(this.serverInfo);

        this.OS.findDefaultNetwork(this.defaultNetworkInfo);

        //OS 정보
        this.OS.runningOS(this.osInfo);

        serverInfoLinux();

        //초기화 진행한 데이터들 다시 set
        this.agentInfo = getServerInfo();

        /**
         * kafka로 데이터를 전송한다.
         */
        Gson gson = new Gson();
        Producer producer = new Producer();
        producer.running(gson.toJson(this.agentInfo));
   }

   public void findDefaultNetwork(){

   }

    /**
     * OS가 Linux 일 경우 데이터를 수집하는 방법
     */
    public void serverInfoLinux(){
        try {

/*
            String main_ip ="";

            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "ip addr show | grep 'inet ' | awk '{print $2}' | cut -d '/' -f1 | grep -v '127'");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    main_ip = line.trim();
                    break;
                }
            }

            reader.close();
            process.destroy();
*/

            //serverInfo setting
            serverInfo();

            //total memory
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "free -m | awk '/메모리:/ {print $2}' || '/Mem:/ {print $2}'\n");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line2;
            while ((line2 = reader.readLine()) != null) {
                if (!line2.isEmpty()) {
                    this.total_memory = Long.parseLong(line2.trim());
                }
            }

            reader.close();
            process.destroy();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void serverInfo(){

        try {
            for (ServerInfo info : this.serverInfo){
                String ifName = info.getInterfaceName();
                String ip = info.getIp();
                String mac = info.getMac();
                String gateway = info.getGateway();

                if (ip != null && mac != null && ifName != null){
                    this.ifNames.add(ifName);
                    this.ips.add(ip);
                    this.macs.add(mac);
                    this.gateways.add(gateway);

                    if(this.defaultNetworkInfo.getInterf().equalsIgnoreCase(info.getInterfaceName())){
                        info.setCheckDefault(true);
                    }
                }
            }

            //host name
            this.hostName = InetAddress.getLocalHost().getHostName();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * 취합한 정보들을 AgentInfo에 담는 부분
     * @return
     */
    public AgentInfo getServerInfo() {
        AgentInfo agentInfo = new AgentInfo();
        List<String> ipList = new ArrayList<>();

        //Bryan -> ipList의 ip들은 ips에서 가져오는가 or serverInfo에서 가져오는가
        for(int i=0; i<this.ips.size(); i++){

            String ip = this.ips.get(i);
            ipList.add(ip);
        }

        for (ServerInfo info : this.serverInfo) {
            if (info.getCheckDefault()) {
                //ADAPTER
                Adapter adapter = new Adapter();
                adapter.setDefault_ip(info.getIp());
                adapter.setDefault_mac(info.getMac());
                adapter.setDefault_gateway(info.getGateway());
                agentInfo.setAdapter(adapter);
            }
        }

        //NIC
        Nic nic = new Nic();
        nic.setServer_info(this.serverInfo);
        agentInfo.setNic(nic);

        //IP List
        agentInfo.setIpList(ipList);
        agentInfo.setOsInfo(this.osInfo);

        return agentInfo;
    }
}
