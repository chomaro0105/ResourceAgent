package org.example.collect;

import org.example.product.kafka.producer.Producer;
import org.example.vo.*;
import org.example.vo.network.Adapter;
import org.example.vo.network.InterfaceInfo;
import org.example.vo.network.Nic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ResourceCollector {

    private static class SingleTon{
        private static ResourceCollector instance = new ResourceCollector();
    }

    public static ResourceCollector getInstance(){
        return SingleTon.instance;
    }

    private AgentInfo agentInfo = new AgentInfo();

    private List<RouteInfo> routeInfo = new ArrayList<RouteInfo>();
    private List<ServerInfo> serverInfo = new ArrayList();
    private List<InterfaceInfo> interfaceInfo = new ArrayList();
    private OsInfo osInfo = new OsInfo();
    private String OS_NAME = "";
    private String OS_VERSION = "";
    private String OS_ARCH = "";
    private String HOST_NAME = "";
    private String DEFAULT_GATEWAY = "";
    private String DEFAULT_IP = "";
    private String DEFAULT_MAC = "";

    private List<String> gateways = new ArrayList<String>();
    private List<String> ifNames = new ArrayList();
    private List<String> ips = new ArrayList<String>();
    private List<String> macs = new ArrayList<String>();
    private long total_memory = 0;

    private int SLEEP_SECOND = 10;

    ServerInfoOs OS;

    public void init(ServerInfoOs serverInfoOs) {
        this.OS = serverInfoOs;
        this.OS_NAME += "-"+getDistribution();

        serverInfoLinux();
        running();

    }

    public void collectData(){
        this.OS.runningNetwork(this.routeInfo);
        this.OS.runningNic(this.serverInfo);
        this.OS.runningOS(this.osInfo);
    }

    public void running(){
        List<Processes> afterTopData = new ArrayList<Processes>();
        List<Processes> addTopData = new ArrayList<Processes>();
        List<Processes> deleteTopData = new ArrayList<Processes>();
        List<Resource> resourceData = new ArrayList<Resource>();

        routeInfo = new ArrayList<RouteInfo>();
        serverInfo = new ArrayList();

        /**
         * 데이터 수집 시작
         */
        collectData();

        //Process 정보
        this.OS.runningProcess(afterTopData, addTopData, deleteTopData, this.total_memory);

        //Resource 정보
        this.OS.runningResource(resourceData);

        //초기화 진행한 데이터들 다시 set
        this.agentInfo = getServerInfo();

        Producer producer = new Producer();
        producer.running(this.agentInfo.toString());
   }

    public void serverInfoLinux(){
        try {

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

            //serverInfo setting
            serverInfo(main_ip);

            //total memory
            builder = new ProcessBuilder("bash", "-c", "free -m | awk '/Mem:/ {print $2}'\n");
            builder.redirectErrorStream(true);
            process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

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

    public void serverInfo(String main_ip){
        /**
         * Default ip,mac,gateway를 지정 하는 로직이 현재 버전 이슈로 인해 while문에서 첫번째로 나오는 ip기준으로 Default 지정중 => 추후에 좀 더 확실한 방식으로 지정 할 필요.
         * 일단 OS별로 main ip 수집하여 처리하는것으로 진행
         *
         */

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

                    if (main_ip.equals(ip)){
                        this.DEFAULT_IP = ip;
                        this.DEFAULT_MAC = mac;
                        this.DEFAULT_GATEWAY = gateway;
                    }
                }
            }

            //host name
            this.HOST_NAME = InetAddress.getLocalHost().getHostName();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public AgentInfo getServerInfo() {
        AgentInfo agentInfo = new AgentInfo();
        String ipList = "";
        boolean first = true;

        //Bryan -> ipList의 ip들은 ips에서 가져오는가 or serverInfo에서 가져오는가
        for(int i=0; i<this.ips.size(); i++){

            String ip = this.ips.get(i);

            if (first == true){
                first = false;
            }
            else {
                ipList += ",";
            }

            ipList += ip;
        }

        agentInfo.setHost_name(this.HOST_NAME);

        //ADAPTER
        Adapter adapter = new Adapter();
        adapter.setDefault_ip(this.DEFAULT_IP);
        adapter.setDefault_mac(this.DEFAULT_MAC);
        adapter.setDefault_gateway(this.DEFAULT_GATEWAY);
        agentInfo.setAdapter(adapter);

        //NIC
        Nic nic = new Nic();
        nic.setServer_info(this.serverInfo);

        nic.setDefaullt_ip(this.DEFAULT_IP);
        nic.setDefault_mac(this.DEFAULT_MAC);
        agentInfo.setNic(nic);

        //IP List
        agentInfo.setIpList(ipList);

        //os detail set
        this.osInfo.setName(this.OS_NAME);
        this.osInfo.setVersion(this.OS_VERSION);
        this.osInfo.setArch(this.OS_ARCH);

        agentInfo.setOsInfo(this.osInfo);

        return agentInfo;
    }

    public String getDistribution(){

        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "grep '^ID=' /etc/os-release | cut -d '=' -f 2 | tr -d '\"'");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    return line.trim();
                }
            }

            reader.close();
            process.destroy();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return "";
    }

}
