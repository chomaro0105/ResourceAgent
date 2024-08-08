package org.example.collect.linux;

import org.example.vo.*;
import org.example.vo.Process;
import org.example.vo.network.DefaultNetworkInfo;
import org.example.vo.network.Nic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerInfoLinux implements ServerInfoOs {

    /**
     * Process 정보들을 수집하는 장소.
     * 차후 Process 관리를 할수 있도록 한다.
     * 새로이 추가 되는 부분만 선택해서 데이터를 전송 할 수 있거나, 특정 장비의 정보를 수집할 수 있다.
     * @param top
     */
    @Override
    public void runningProcess(List<ProcessDetail> top) {
        try{
            String line;

            int exceptionDataCount = 0; //list에 다른 값들이 존재해서 해당 값만큼 제외
            java.lang.Process process = new ProcessBuilder("ps", "-eao", "user,pid,ppid,pcpu,pmem,stat,stime,time,command").start();
            Scanner sc = new Scanner(process.getInputStream());
            while (sc.hasNextLine()) {
                line = sc.nextLine().trim();
                if(line.length() > 0 && exceptionDataCount == 1){
                    top.add(new ProcessDetail(line, "linux"));
                } else {
                    exceptionDataCount++;
                }
            }

            sc.close();
            process.destroy();

        }catch(Exception e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void runningResource(Resource resourceData){
        try{
            resourceData.setData(getCpu(), getCpuCore(), getCpuClock(), getMemory(), getDisk());
        }catch(Exception e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    /**
     * network 정보를 추출한다.
     * @param defaultNetworkInfo
     */
    public void findDefaultNetwork(DefaultNetworkInfo defaultNetworkInfo) {
        try {
            java.lang.Process process = new ProcessBuilder("netstat", "-rn").start();
            Scanner sc = new Scanner(process.getInputStream());
            String line = "";

            int exception = 0;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                line = line.trim();

                if(exception == 2){
                    line = line.substring(line.indexOf(" "), line.length()).trim();

                    String gateway = line.substring(0, line.indexOf(" "));
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    line = line.substring(line.indexOf(" "), line.length()).trim();

                    String flags = line.substring(0, line.indexOf(" "));
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    String iface = line.substring(line.indexOf(" "), line.length()).trim();

                    if(flags.equals("UG")){
                        defaultNetworkInfo.setFlag("UG");
                        defaultNetworkInfo.setInterf(iface);
                        defaultNetworkInfo.setGateway(gateway);
                        break;
                    }
                }else{
                    exception++;
                }
            }

            process.destroy();
            sc.close();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }

//        Collections.sort(routeInfo, new Comparator<RouteInfo>() {
//            @Override
//            public int compare(RouteInfo a1, RouteInfo a2) {
//                return -(StringUtil.ipToInt(a1.getNetmask()) - StringUtil.ipToInt(a2.getNetmask()));
//            }
//        });
    }

    /**
     * os정보를 추출한다.
     * @param os
     */
    @Override
    public void runningOS(Os os) {
        try {
            String[] keyValue = new String[2];
            java.lang.Process process = new ProcessBuilder("/bin/sh","-c","cat /etc/os-release").start();
            Scanner sc = new Scanner(process.getInputStream());
            String line = "";

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                keyValue = line.split("=");
                if(keyValue[0].equalsIgnoreCase("PRETTY_NAME")){
                    os.setFullName(keyValue[1].trim().replace("\"",""));
                }else if(keyValue[0].equalsIgnoreCase("ID")){
                    os.setName(keyValue[1].trim().replace("\"",""));
                }else if(keyValue[0].equalsIgnoreCase("VERSION_ID")){
                    os.setVersion(keyValue[1].trim().replace("\"",""));
                }
            }

            process = new ProcessBuilder("/bin/sh","-c","id").start();
            sc = new Scanner(process.getInputStream());
            String[] values = new String[1];        //첫번째 데이터만 필요하다.
            boolean isRoot = false;

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                values = line.split(" ");
                keyValue = values[0].split("=");
                if(keyValue[0].equalsIgnoreCase("uid")){
                    if(keyValue[1].startsWith("0")){
                        isRoot = true;
                    }

                    break;
                }
            }

            if(isRoot){
                /**
                 * 하드웨어 제조사 벤더
                 * root 계정으로 실행해야만 가능하다.
                 */
                process = new ProcessBuilder("dmidecode","-s","system-manufacturer").start();
                sc = new Scanner(process.getInputStream());

                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    os.setVendor(line.trim());
                }

                /**
                 * 하드웨어 장비 모델명
                 * root 계정으로 실행해야만 가능하다.
                 */
                process = new ProcessBuilder("dmidecode","-s","system-product-name").start();
                sc = new Scanner(process.getInputStream());

                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    os.setModel(line.trim());
                }
            }else{
                os.setVendor("unKnow(is not root)");
                os.setModel("unKnow(is not root)");
            }

            process.destroy();
            sc.close();

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * ip별 gateway를 추출한다.
     * @param ip
     * @return
     */
    public String getGatewayByIP(String ip){
        try{
            String gateway = null;

            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "netstat -rn | grep UG | awk '{print $1,$2}'");
            builder.redirectErrorStream(true);
            java.lang.Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            int best_score = 0;
            int score = 0;
            while ((line = reader.readLine()) != null){
                //1차적으로 각 옥텟 별로 split 해서 순서대로 같은지 equal 으로 체크후 같은값중에 가장가까운 대역을찾아 해당하는 대역 gateway set
                String[] tokens = line.split(" ");
                String destination = tokens[0].trim();
                String gateways = tokens[1].trim();

                String[] destination_tokens = destination.split("\\.");
                String[] ip_tokens = ip.trim().split("\\.");

                for (int i = 0; i < 4; i++){
                    String destination_token = destination_tokens[i];
                    String ip_token = ip_tokens[i];

                    if (destination_token.equals(ip_token)){
                        score ++;
                    }
                    else {
                        if (best_score < score){
                            gateway = gateways;
                            best_score = score;
                            score = 0;
                        }
                        break;
                    }
                }

                if (gateway == null && destination.equals("0.0.0.0")){
                    gateway = gateways;
                }
            }

            process.destroy();
            reader.close();
            return gateway;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Nic 정보를 수집한다.
     * @param nics
     */
    @Override
    public void runningNic(List<Nic> nics) {
        /*
        NIC에 삽입될 데이터 수집 하는 메소드
        정보 : {[interface 이름, ip 주소, mac 주소, gateway],...} ServerInfo VO에 적용될 리스트만 추출하면됨
        추출한 데이터는 전역변수로 다른 메소드의 방식과 똑같이 적용한다
        */

        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "ifconfig -a | awk '/^[^[:space:]]/{ interface=$1; print } $1==\"inet\" { print $1, $2 } $1==\"ether\" { print $1, $2 }'");
            //에러도 표준 출력으로 처리되서 서비스 지속가능하게하지만 그러기에 디버깅이 어려워진다..
            builder.redirectErrorStream(true);
            java.lang.Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                Nic nic = new Nic();

                String ifName = "";
                String ip = "";
                String mac = "";
                String gateway = "";
                String status = "";

                String[] interfaces = line.split(":");
                if (interfaces[0].trim().equals("lo") || interfaces[0].trim().equals("tunl0")){
                    reader.readLine();
                } else {
                    ifName = interfaces[0].trim();
                    String flags = interfaces[1].trim();
                    if (flags.contains("<UP,") || flags.contains(",UP,")) {
                        if ( flags.contains(",RUNNING,")  || flags.contains(",RUNNING>")){
                            status = "10010010";
                        }else{
                            status = "10010006";
                        }
                    }
                    else{
                        status = "10010007";
                    }

                    line = reader.readLine();
                    if (line.contains("inet")){
                        ip = line.replace("inet","").trim();
                        mac = reader.readLine().replace("ether","").trim();
                    }
                    else if (line.contains("ether")){
                        mac = line.replace("ether","").trim();
                    }

                    if (ip != null){
                        gateway = getGatewayByIP(ip);
                    }

                    nic.setInterfaceName(ifName);
                    nic.setIp(ip);
                    nic.setMac(mac);
                    nic.setGateway(gateway);
                    nic.setStatus(status);
                    nics.add(nic);
                }
            }

            reader.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            java.lang.Process process = new ProcessBuilder("netstat", "-rn").start();
            Scanner sc = new Scanner(process.getInputStream());
            String line = "";

            int exception = 0;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                line = line.trim();

                if(exception == 2){
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    line = line.substring(line.indexOf(" "), line.length()).trim();

                    String flags = line.substring(0, line.indexOf(" "));
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    line = line.substring(line.indexOf(" "), line.length()).trim();
                    String iface = line.substring(line.indexOf(" "), line.length()).trim();

                    if(flags.equals("UG")){
                        for(Nic nic : nics){
                            if(nic.getInterfaceName().equalsIgnoreCase(iface)){
                                nic.setCheckDefault(true);
                            }
                        }
                        break;
                    }
                }else{
                    exception++;
                }
            }

            process.destroy();
            sc.close();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

//    @Override
//    public AgentInfo makeSenderData(AgentInfo agentInfo, List<Processes> topData, List<Resource> resourceData, String type){
//        agentInfo.setProcesses(topData);
//        return agentInfo;
//    }

    /**
     * cpu 사용량 수집
     * @return
     */
    public String getCpu()  {
        try {
            String line;
            int exceptionDataCount = 0;
            final String regexp = "(\\d+.\\d+)\\W+us,\\s (\\d+.\\d+)\\W+sy";
            float cpu = 0;
            String cpuUsed = "";

            java.lang.Process process = new ProcessBuilder("top", "-d1", "-b", "-n2", "-p", "1").start();
            Scanner sc = new Scanner(process.getInputStream());

            while (sc.hasNextLine()) {
                line = sc.nextLine().trim();
                if (line.length() > 0 && exceptionDataCount == 1 && line.contains("Cpu")) {
                    Pattern pattern = Pattern.compile(regexp);
                    Matcher match = pattern.matcher(line);
                    if (match.find()) {
                        cpu = Float.parseFloat(match.group(1)) + Float.parseFloat(match.group(2));
                    }
                } else if (line.contains("Cpu")) {
                    exceptionDataCount++;
                }
            }
            cpuUsed = String.format("%.1f" , cpu);

            sc.close();
            process.destroy();

            return cpuUsed;
        }catch (Exception e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * cpu-core 수집
     * @return
     */
    public String getCpuCore() {
        String cpuCore = null;

        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "cat /proc/cpuinfo | grep processor | wc -l");
            //에러도 표준 출력으로 처리되서 서비스 지속가능하게하지만 그러기에 디버깅이 어려워진다..
            builder.redirectErrorStream(true);
            java.lang.Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()){
                    cpuCore = line.trim();
                }
                if (cpuCore != null){
                    break;
                }
            }

            reader.close();
            process.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cpuCore;
    }

    /**
     * cpu-clock 수집
     * @return
     */
    public String getCpuClock(){
        String cpuClock = null;

        try {
//            ProcessBuilder builder = new ProcessBuilder("bash", "-c", " cat /proc/cpuinfo | grep model\\ name | awk '{print $10}' | uniq");
//            ProcessBuilder builder = new ProcessBuilder("bash", "-c", " grep -m 1 'model name' /proc/cpuinfo");
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", " grep -m 1 'cpu MHz' /proc/cpuinfo | awk '{print $4}'");
            //에러도 표준 출력으로 처리되서 서비스 지속가능하게하지만 그러기에 디버깅이 어려워진다..
            builder.redirectErrorStream(true);
            java.lang.Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()){

                    cpuClock = line.replace("GHz","").trim();
                    if(cpuClock.length() < 1){
                        cpuClock = line.replace("MHz","").trim();
                    }
                }
                if (cpuClock != null){
                    break;
                }
            }

            reader.close();
            process.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cpuClock;
    }

    /**
     * Memory 사용량 수집
     * @return
     */
    public String getMemory(){
        try {
            String line = "";
            String memmory = "";

            /**
             * 영어로만 할 수 있는 방법은 없는가?
             */
            java.lang.Process process = new ProcessBuilder("bash", "-c","free -b | awk '/^메모리:/ {print $2/1024/1024, $3/1024/1024}' || '/^Mem:/ {print $2/1024/1024, $3/1024/1024}'").start();
            Scanner sc = new Scanner(process.getInputStream());

            while (sc.hasNextLine()) {
                line = sc.nextLine().trim();
                String[] tokens = line.split(" ");
                memmory = tokens[0] + "," + tokens[1];
            }

            sc.close();
            process.destroy();

            return memmory;
        }catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return null;
    }

    private String getDisk() {
        try {
            String line;
            String disk;
            String diskTotal = "";
            String diskUsage = "";

            java.lang.Process process = new ProcessBuilder("bash", "-c","df -Bk | awk '{total += $2; used += $3} END {print total/1024, used/1024}'").start();
            Scanner sc = new Scanner(process.getInputStream());

            while (sc.hasNextLine()) {
                line = sc.nextLine().trim();
                String[] tokens = line.split(" ");
                diskTotal = tokens[0];
                diskUsage = tokens[1];
            }

            disk = String.format("%.1f", Float.parseFloat(diskTotal)) + "," + String.format("%.1f", Float.parseFloat(diskUsage));

            sc.close();
            process.destroy();

            return disk;

        }catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return null;
    }
}