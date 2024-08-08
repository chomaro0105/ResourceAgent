package org.example.vo;

import lombok.Data;
import org.example.vo.network.Adapter;
import org.example.vo.network.Nic;

import java.util.List;

@Data
public class AgentInfo {
    private List<Nic> nic;
    private Resource resource;
    private List<Process> processes;
    private Os os;
    private Adapter adapter;

    //auth & processes
    private String hostName;

    //collected date
    private String eventDate;

//    //lldp로 수집된 내용
//    private List<InterfaceInfo> lldp;
//    //cdp로 수집된 내용
//    private List<InterfaceInfo> cdp;
}
