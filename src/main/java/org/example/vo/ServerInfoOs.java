package org.example.vo;

import org.example.vo.network.DefaultNetworkInfo;
import org.example.vo.network.Nic;

import java.util.List;

public interface ServerInfoOs {
    void runningProcess(List<ProcessDetail> top);
    void runningResource(Resource resource);
    void runningNic(List<Nic> nics);
    void runningOS(Os os);
    void findDefaultNetwork(DefaultNetworkInfo defaultNetwork);
//    AgentInfo makeSenderData(AgentInfo agentDataDto, List<Processes> topData, List<Resource> resourceData, String type);
}

