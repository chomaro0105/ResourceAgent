package org.example.vo;

import org.example.vo.network.DefaultNetworkInfo;

import java.util.List;

public interface ServerInfoOs {
    void runningProcess(List<Processes> afterTopData, List<Processes> addTopData, List<Processes> deleteTopData, Long total_memory);
    void runningResource(List<Resource> resourceData);
    void runningNic(List<ServerInfo> serverInfo);
    void runningOS(OsInfo osInfo);
    void findDefaultNetwork(DefaultNetworkInfo defaultNetworkInfo);
    AgentInfo makeSenderData(AgentInfo agentDataDto, List<Processes> topData, List<Resource> resourceData, String type);
}

