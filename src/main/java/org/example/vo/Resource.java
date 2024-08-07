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

    public Resource(String cpuUsed, String cpuCore, String cpuClock, String memory, String disk){
        this.cpuUsed = cpuUsed;
        this.cpuCore = cpuCore;
        this.cpuClock = cpuClock;

        this.memoryTotal = memory.substring(0,memory.indexOf(","));
        this.memoryUsage = memory.substring(memory.indexOf(",")+1, memory.length()).trim();
        this.diskTotal = disk.substring(0,disk.indexOf(","));
        this.diskUsage = disk.substring(disk.indexOf(",")+1, disk.length()).trim();
    }
}