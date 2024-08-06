package org.example.vo.network;

import lombok.Data;

import java.util.List;

@Data
public class Chassis {
    private String chassisID;
    private String sysName;
    private List<String> sysDescr;
    private String mgmtIP;
    private String vendor;
}
