package org.example.vo;

import java.util.List;

public interface ServerInfoOs {
    void runningProcess(List<Processes> afterTopData, List<Processes> addTopData, List<Processes> deleteTopData, Long total_memory);
    void runningResource(List<Resource> resourceData);
    void runningNetwork(List<RouteInfo> routeInfo);
    //Bryan => NIC 정보
    void runningNic(List<ServerInfo> serverInfo);
    void runningOS(OsInfo osInfo);
    AgentInfo makeSenderData(AgentInfo agentDataDto, List<Processes> topData, List<Resource> resourceData, String type);
}

