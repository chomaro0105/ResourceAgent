package org.example.vo.network;

import lombok.Data;

@Data
public class DefaultNetworkInfo {
    private String interf;
    private String ip;
    private String netmask;
    private String gateway;
    private String flag;
}
