package org.example.vo;

import lombok.Data;

@Data
public class ResourceData {

    private String resourceFlag;
    private String cpuCore;
    private String cpuUser;
    private String cpuNice;
    private String cpuSystem;
    private String cpuIdle;
    private String cpuIowait;
    private String cpuIrq;
    private String cpuSoftirq;
    private String cpuUsde;
    private String memTotal;
    private String memFree;
    private String memUsage;
    private String memBuffers;
    private String memCached;
    private String memSwaptotal;
    private String memSwapfree;
    private String diskVolumn;
    private String diskName;
    private String diskTotal;
    private String diskUsage;


    public ResourceData() {

    }
    /**
     *
     * @param resourceData
     */
    public ResourceData(String resourceData){
        this.cpuUsde = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();

        this.memTotal = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();

        this.memUsage = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();

        this.diskTotal = resourceData.substring(0,resourceData.indexOf(","));
        resourceData = resourceData.substring(resourceData.indexOf(",")+1, resourceData.length()).trim();

        this.diskUsage = resourceData;
    }
}
