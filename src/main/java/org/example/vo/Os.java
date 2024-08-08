package org.example.vo;

import lombok.Data;

@Data
public class Os {
    private String name;
    private String version;
    private String arch;

    //node info
    private String fullName;
    private String vendor;
    private String model;
}