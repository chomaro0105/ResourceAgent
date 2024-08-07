package org.example.vo;

import lombok.Data;

@Data
public class ServerInfo {
    private String interfaceName;
    private String ip;
    private String mac;
    private String gateway;
    private String status;
    private Boolean checkDefault = false;
}
