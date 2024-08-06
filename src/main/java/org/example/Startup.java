package org.example;

import org.example.collect.ResourceCollector;
import org.example.collect.ServerInfoLinux;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Startup {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        try {
            Startup main = new Startup();
            main.running();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void running() throws Exception{

//        new ProcessMemoryCheck().start();

        ResourceCollector.getInstance().init(new ServerInfoLinux());
    }
}