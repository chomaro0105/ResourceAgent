package org.example.vo;

public class ProcessDetail {
    public String PID;
    public String PPID;         //200180531 Wade - PPID 추가
    public String USER_NAME;
    private String PR;
    private String NI;
    private String VIRT;
    private String RES;
    private String SHR;
    private String STATE;         //STATE
    private String CPU;
    private String MEM;
    private String TIME;
    private String NAME;          //COMMAND
    public  int CHILD_COUNT;
    public String state_code;

    /**
     * LINUX
     * @param topData
     */
    public ProcessDetail(String topData, String osType){

        this.USER_NAME = topData.substring(0, topData.indexOf(" "));
        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        this.PID = topData.substring(0, topData.indexOf(" "));
        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        ////200180531 Wade - 공백 삭제 및 PPID 추가
        this.PPID = topData.substring(0, topData.indexOf(" "));
        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        this.CPU = topData.substring(0, topData.indexOf(" "));
        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        this.MEM = topData.substring(0, topData.indexOf(" "));
        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        this.STATE = topData.substring(0, topData.indexOf(" "));
        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        String STIME = "";
        STIME = topData.substring(0, topData.indexOf(" "));

        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        this.TIME = topData.substring(0, topData.indexOf(" "));
        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        this.NAME = topData.substring(0, topData.length());
//        }
    }

    /**
     * WINDOWS
     * [0] process name
     * [1] pid
     * [2] session name
     * [3] session state(비활성(0), 활성(1))
     * [4] memory
     * [5] process state(실행중)
     * [6] user name
     * [7] cpu time
     * [8] windows tab name
     * @param topData
     * @param total_memory
     */
    public ProcessDetail(String[] topData, long total_memory){
        this.NAME       = topData[0];
        this.PID        = topData[1];

        if(topData[4].indexOf("K") > -1){
            this.MEM    = String.format("%.2f", Long.parseLong(topData[4].replace(" K","").replace(",","")) * 1000 * 100 / (double)total_memory);
        }else{
            this.MEM    = topData[4];
        }
        this.CPU        = "0";

        this.STATE      = topData[5];
        this.USER_NAME  = topData[6];
    }

    public ProcessDetail(String topData, long total_memory){

        String pid = "";
        String name = "";
        String cpu = "";
        String mem = "";
        String memUsed = "";
        long memory = 0;
        float memoryUsed = 0;

        pid = topData.substring(0, topData.indexOf("  "));
        topData = topData.substring(topData.indexOf("  "), topData.length()).trim();

        name= topData.substring(0, topData.indexOf("  "));
        topData = topData.substring(topData.indexOf("  "), topData.length()).trim();

        cpu = topData.substring(0, topData.indexOf(" "));
        topData = topData.substring(topData.indexOf(" "), topData.length()).trim();

        mem = topData.substring(0, topData.length());

        memory = Long.parseLong(mem);
        memoryUsed = (memory/1024) * 1000 * 100 / ((float)total_memory);

        memUsed = String.format("%.1f" , memoryUsed);

        if(!pid.equals("0")){
            this.NAME       = name;
            this.PID        = pid;
            this.MEM        = memUsed;
            this.CPU        = cpu;
            this.STATE      = "Running";
        }
    }
}