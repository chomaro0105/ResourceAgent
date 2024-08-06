package org.example.vo.network;

import lombok.Data;

@Data
public class InterfaceInfo {
    private String interfaceName;
    private Chassis chassis;
    private Port port;
}
