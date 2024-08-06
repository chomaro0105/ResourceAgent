package org.example.vo;

import lombok.Data;

@Data
public class OsInfo {
    private String name;
    private String version;
    private String arch;

    //node info
    private String full_name;
    private String vendor;
    private String model;
}