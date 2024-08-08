package org.example.collect;

import com.google.gson.Gson;
import org.example.product.kafka.producer.Producer;
import org.example.util.PropertiesLoader;
import org.example.vo.*;
import org.example.vo.network.Adapter;
import org.example.vo.network.DefaultNetworkInfo;
import org.example.vo.network.Nic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SystemCollector extends Thread{
    ServerInfoOs OS;
    private Os os = new Os();
    private DefaultNetworkInfo defaultNetworkInfo = new DefaultNetworkInfo();

    private String TOPIC = "agent-system";

    private AgentInfo agentInfo = new AgentInfo();

    private List<Nic> nics = new ArrayList();

    private final int SYSTEM_SLEEP = 60*1000*5;    //5 min

    public void init(ServerInfoOs serverInfoOs) {
        this.OS = serverInfoOs;
    }

    public void run(){
        while(true){
            this.nics = new ArrayList();

            /**
             * 시스템 정보데이터와 리소스 정보데이터를 보내는 주기를 나누자.
             * 시스템 데이터는 한번만 해줘도 되....흠... 중간에 메모리가 바뀌거나 disk용량이 바뀐다면.....흠...
             * 가끔씩 정보를 보내는걸로 해야겠다...
             */
            //Network Nic 정보
            this.OS.runningNic(this.nics);
            this.OS.findDefaultNetwork(this.defaultNetworkInfo);

            //OS 정보
            this.OS.runningOS(this.os);

            //초기화 진행한 데이터들 다시 set
            this.agentInfo = getServerInfo();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            agentInfo.setEventDate(dateFormat.format(new Date()));

            /**
             * kafka로 데이터를 전송한다.
             */
            Gson gson = new Gson();
            Producer producer = new Producer();
            producer.running(TOPIC, PropertiesLoader.getInstance().getProperties().getValue("agent","id",""), gson.toJson(this.agentInfo));

            try {
                sleep(SYSTEM_SLEEP);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 취합한 정보들을 AgentInfo에 담는 부분
     * @return
     */
    public AgentInfo getServerInfo() {
        AgentInfo agentInfo = new AgentInfo();
        for (Nic nic : this.nics) {
            if (nic.getCheckDefault()) {
                //ADAPTER
                Adapter adapter = new Adapter();
                adapter.setDefaultIp(nic.getIp());
                adapter.setDefaultMac(nic.getMac());
                adapter.setDefaultGateway(nic.getGateway());
                agentInfo.setAdapter(adapter);
            }
        }

        //Nic
        agentInfo.setNic(this.nics);

        //System Os
        agentInfo.setOs(this.os);

        try {
            agentInfo.setHostName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        return agentInfo;
    }
}
