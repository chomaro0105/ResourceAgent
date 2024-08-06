package org.example.vo.network;

import lombok.Data;

import java.util.List;

@Data
public class Port {
    private String portID;
    private List<String> portDescr;
    private String TTL;
}
