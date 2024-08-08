package org.example.vo.network;

import lombok.Data;

@Data
public class Nic {
    private String interfaceName;
    private String ip;
    private String mac;
    private String gateway;
    private String status;
    private Boolean checkDefault = false;
}
