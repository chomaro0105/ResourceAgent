package org.example.collect;

import com.google.gson.Gson;
import org.example.product.kafka.producer.Producer;
import org.example.util.PropertiesLoader;
import org.example.vo.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 실제 데이터를 수집하는 부분
 * network, system 정보들을 모두 취합한다
 */
public class ResourceCollector extends Thread {
    private String TOPIC = "agent-resource";
    private final int RESOURCE_SLEEP = 60 * 1000;    //1 min

    ServerInfoOs OS;

    public void init(ServerInfoOs serverInfoOs) {
        this.OS = serverInfoOs;
    }

    /**
     * Network 데이터를 수집하고 전송한다.
     */
    public void run() {
        Resource resourceData = new Resource();

        while (true) {

            //Resource 정보
            this.OS.runningResource(resourceData);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resourceData.setEventDate(dateFormat.format(new Date()));

            /**
             * kafka로 데이터를 전송한다.
             */
            Gson gson = new Gson();
            Producer producer = new Producer();
            producer.running(TOPIC, PropertiesLoader.getInstance().getProperties().getValue("agent", "id", ""), gson.toJson(resourceData));

            try {
                sleep(RESOURCE_SLEEP);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
