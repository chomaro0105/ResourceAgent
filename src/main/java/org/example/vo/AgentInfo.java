package org.example.vo;

import lombok.Data;
import org.example.vo.network.Adapter;
import org.example.vo.network.InterfaceInfo;
import org.example.vo.network.Nic;

import java.util.List;

@Data
public class AgentInfo {
    private Nic nic;
    private Resource resource;
    //lldp로 수집된 내용
    private List<InterfaceInfo> lldp;
    //cdp로 수집된 내용
    private List<InterfaceInfo> cdp;
    private String ipList;
    private List<Processes> processes;
    private OsInfo osInfo;
    private Adapter adapter;

    //auth & processes
    private String host_name;
}
