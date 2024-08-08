package org.example;

import org.example.collect.ProcessCollector;
import org.example.collect.ResourceCollector;
import org.example.collect.linux.ServerInfoLinux;
import org.example.collect.SystemCollector;
import org.example.util.PropertiesLoader;
import org.example.vo.Process;

import java.util.UUID;

import static java.lang.Thread.sleep;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Startup {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.

        try {
            Startup main = new Startup();
            main.running();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void running() throws Exception{

//        new ProcessMemoryCheck().start();

        String id = PropertiesLoader.getInstance().getProperties().getValue("agent","id","");
        if(id == null || id.isEmpty()){
            id = UUID.randomUUID().toString();
            PropertiesLoader.getInstance().getProperties().store("agent","id",id);
        }

        /**
         * id 존재 여부 확인
         */
        while(true){
            id = PropertiesLoader.getInstance().getProperties().getValue("agent","id","");
            if(id == null || id.isEmpty()){
                try {
                    System.err.printf("Agent ID is unknow. check gon.properties%n");
                    sleep(1 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }else{
                break;
            }
        }

        SystemCollector systemCollector = new SystemCollector();
        systemCollector.init(new ServerInfoLinux());
        systemCollector.start();

        ResourceCollector resourceCollector = new ResourceCollector();
        resourceCollector.init(new ServerInfoLinux());
        resourceCollector.start();

        ProcessCollector processCollector = new ProcessCollector();
        processCollector.init(new ServerInfoLinux());
        processCollector.start();
    }
}