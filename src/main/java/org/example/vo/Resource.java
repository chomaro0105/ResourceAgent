package org.example.vo;

import lombok.Data;

@Data
public class Resource {
    private String cpuUsed;
    private String cpuCore;
    private String cpuClock;
    private String memoryUsage;
    private String memoryTotal;
    private String diskUsage;
    private String diskTotal;

    public Resource(){
    }
    /**
     *
     * @param resourceData
     */
    public Resource(String resourceData){
        System.out.println("resourceData:"+resourceData);
        this.cpuUsed = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();
        System.out.println("cpuUsed1 :"+this.cpuUsed);
        this.cpuCore = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();
        System.out.println("cpuUsed2 :"+this.cpuCore);
        this.cpuClock = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();
        System.out.println("cpuUsed3 :"+this.cpuClock);
        this.memoryTotal = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();
        System.out.println("cpuUsed4 :"+this.memoryTotal);
        this.memoryUsage = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();
        System.out.println("cpuUsed5 :"+this.memoryUsage);
        this.diskTotal = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();
        System.out.println("cpuUsed6 :"+this.diskTotal);
        this.diskUsage = resourceData;
        System.out.println("cpuUsed7 :"+this.diskUsage);
    }
}