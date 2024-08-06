package org.example.vo.network;

import lombok.Data;
import org.example.vo.ServerInfo;

import java.util.List;

@Data
public class Nic {
    private List<ServerInfo> server_info;
    private String defaullt_ip;
    private String default_mac;
}
