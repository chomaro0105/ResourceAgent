package org.example.vo;

import lombok.Data;

import java.util.List;

@Data
public class Process {
    List<ProcessDetail> processesDetail;
    String eventDate;
}
