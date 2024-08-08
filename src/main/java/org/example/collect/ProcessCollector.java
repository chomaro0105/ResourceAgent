package org.example.collect;

import com.google.gson.Gson;
import org.example.product.kafka.producer.Producer;
import org.example.util.PropertiesLoader;
import org.example.vo.*;
import org.example.vo.Process;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProcessCollector extends Thread {
    private final int PROCESS_SLEEP = 5*1000;    //5 sec
    private String TOPIC = "agent-process";
    ServerInfoOs OS;

    public void init(ServerInfoOs serverInfoOs) {
        this.OS = serverInfoOs;
    }

    /**
     * Network 데이터를 수집하고 전송한다.
     */
    public void run(){
        while(true){
            Process process = new Process();

            List<ProcessDetail> top = new ArrayList<ProcessDetail>();
            this.OS.runningProcess(top);
            process.setProcessesDetail(top);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            process.setEventDate(dateFormat.format(new Date()));

            /**
             * kafka로 데이터를 전송한다.
             */
            Gson gson = new Gson();
            Producer producer = new Producer();
            producer.running(TOPIC, PropertiesLoader.getInstance().getProperties().getValue("agent","id",""), gson.toJson(process));

            try {
                sleep(PROCESS_SLEEP);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}