package org.example.collect;

import org.example.util.StringUtil;
import org.example.vo.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerInfoLinux implements ServerInfoOs {

    @Override
    public void runningProcess(List<Processes> afterTopData, List<Processes> addTopData, List<Processes> deleteTopData, Long total_memory) {
        try{
            //List<TopData> compareBeforeTopData = new ArrayList<TopData>();
            //Map<String, Integer> idx = new HashMap<>();

            String line;
            //Integer listIdx;
            int exceptionDataCount = 0; //list에 다른 값들이 존재해서 해당 값만큼 제외
            //Process process = new ProcessBuilder("ps", "auxw").start();
            //200180531 Wade - Command 변경
            Process process = new ProcessBuilder("ps", "-eao", "user,pid,ppid,pcpu,pmem,stat,stime,time,command").start();
            Scanner sc = new Scanner(process.getInputStream());
            while (sc.hasNextLine()) {
                line = sc.nextLine().trim();
                if(line.length() > 0 && exceptionDataCount == 1){
                    afterTopData.add(new Processes(line, "linux"));
                } else {
                    exceptionDataCount++;
                }
            }

            sc.close();
            process.destroy();
/*
            PROCESS STATE CODES 프로세스 상태값
            Here are the different values that the s, stat and state output specifiers (header "STAT" or "S") will display to describe the state
            of a process:

            D    uninterruptible sleep (usually IO)
            R    running or runnable (on run queue)
            S    interruptible sleep (waiting for an event to complete)
            T    stopped by job control signal
            t    stopped by debugger during the tracing
            W    paging (not valid since the 2.6.xx kernel)
            X    dead (should never be seen)
            Z    defunct ("zombie") process, terminated but not reaped by its parent

            For BSD formats and when the stat keyword is used, additional characters may be displayed:

               <    high-priority (not nice to other users)
            N    low-priority (nice to other users)
            L    has pages locked into memory (for real-time and custom IO)
            s    is a session leader
            l    is multi-threaded (using CLONE_THREAD, like NPTL pthreads do)
            +    is in the foreground process group
*/

        }catch(Exception e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void runningResource(List<Resource> resourceData){
        try{
            String cpuUsed = "";
            String cpuCore = "";
            String cpuClock = "";
            String memory = "";
            String disk = "";
            String line = "";

            cpuUsed = getCpu();
            cpuCore = getCpuCore();
            cpuClock = getCpuClock();
            memory = getMemory();
            disk = getDisk();

            line = cpuUsed +","+ cpuCore +","+ cpuClock +","+ memory +","+ disk;

            System.out.println("aaaaaaaaaaaaaa : "+cpuUsed+"//"+cpuCore+"//"+cpuClock+"//"+memory+"//"+disk);

            resourceData.add(new Resource(line));

        }catch(Exception e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void runningNetwork(List<RouteInfo> routeInfo) {
        try {
            Process process = new ProcessBuilder("netstat", "-rn").start();
            Scanner sc = new Scanner(process.getInputStream());
            String line = "";

            int exception = 0;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                line = line.trim();

                if(exception == 2){
                    String netIP = line.substring(0, line.indexOf(" "));
                    line = line.substring(line.indexOf(" "), line.length()).trim();

                    String gateway = line.substring(0, line.indexOf(" "));
                    line = line.substring(line.indexOf(" "), line.length()).trim();

                    String genmask = line.substring(0, line.indexOf(" "));
                    line = line.substring(line.indexOf(" "), line.length()).trim();

                    String flags = line.substring(0, line.indexOf(" "));

                    RouteInfo route = new RouteInfo();
                    route.setIp(netIP);
                    route.setNetmask(genmask);
                    route.setGateway(gateway);

                    if(flags.equals("UG")){
                        routeInfo.add(route);
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

        Collections.sort(routeInfo, new Comparator<RouteInfo>() {
            @Override
            public int compare(RouteInfo a1, RouteInfo a2) {
                return -(StringUtil.ipToInt(a1.getNetmask()) - StringUtil.ipToInt(a2.getNetmask()));
            }
        });
    }

    @Override
    public void runningOS(OsInfo osInfo) {
        try {
            String name = "";
            String vendor = "";
            String model = "";

            Process process = new ProcessBuilder("/bin/sh","-c","cat /etc/os-release | grep PRETTY_NAME").start();
            Scanner sc = new Scanner(process.getInputStream());
            String line = "";

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                name = line.split("=")[1].trim().replace("\"","");
            }

            process = new ProcessBuilder("dmidecode","-s","system-manufacturer").start();
            sc = new Scanner(process.getInputStream());

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                vendor = line.trim();
            }

            process = new ProcessBuilder("dmidecode","-s","system-product-name").start();
            sc = new Scanner(process.getInputStream());

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                model = line.trim();
            }

            osInfo.setFull_name(name);
            osInfo.setVendor(vendor);
            osInfo.setModel(model);

            process.destroy();
            sc.close();

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getGatewayByIP(String ip){
        try{
            String gateway = null;

            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "netstat -rn | grep UG | awk '{print $1,$2}'");
            builder.redirectErrorStream(true);
            Process process = builder.start();
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

    @Override
    public void runningNic(List<ServerInfo> serverInfo) {
        /*
        NIC에 삽입될 데이터 수집 하는 메소드
        정보 : {[interface 이름, ip 주소, mac 주소, gateway],...} ServerInfo VO에 적용될 리스트만 추출하면됨
        추출한 데이터는 전역변수로 다른 메소드의 방식과 똑같이 적용한다
        */

        try {
            //ifconfig | awk '/^[^[:space:]]/{ interface=$1; print } $1=="inet" { print $2 } $1=="ether" { print $2 }'
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "ifconfig -a | awk '/^[^[:space:]]/{ interface=$1; print } $1==\"inet\" { print $1, $2 } $1==\"ether\" { print $1, $2 }'");
            //에러도 표준 출력으로 처리되서 서비스 지속가능하게하지만 그러기에 디버깅이 어려워진다..
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                ServerInfo server_info = new ServerInfo();

                String ifName;
                String ip = null;
                String mac = null;
                String gateway = null;
                String status = null;

                String[] interfaces = line.split(":");
                if (interfaces[0].trim().equals("lo") || interfaces[0].trim().equals("tunl0")){
                    line = reader.readLine();
                    if(line.contains("inet")){
//                        reader.readLine();
                        continue;
                    }
                }
                else {
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

                    server_info.setInterfaceName(ifName);
                    server_info.setIp(ip);
                    server_info.setMac(mac);
                    server_info.setGateway(gateway);
                    server_info.setStatus(status);

                    serverInfo.add(server_info);
                }
            }

            reader.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AgentInfo makeSenderData(AgentInfo agentInfo, List<Processes> topData, List<Resource> resourceData, String type){
        agentInfo.setProcesses(topData);
        return agentInfo;
    }

    public String getCpu()  {
        try {
            String line;
            int exceptionDataCount = 0;
            final String regexp = "(\\d+.\\d+)\\W+us,\\s (\\d+.\\d+)\\W+sy";
            float cpu = 0;
            String cpuUsed = "";

            Process process = new ProcessBuilder("top", "-d1", "-b", "-n2", "-p", "1").start();
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

    public String getCpuCore() {
        String cpuCore = null;

        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", "cat /proc/cpuinfo | grep processor | wc -l");
            //에러도 표준 출력으로 처리되서 서비스 지속가능하게하지만 그러기에 디버깅이 어려워진다..
            builder.redirectErrorStream(true);
            Process process = builder.start();
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

    public String getCpuClock(){
        String cpuClock = null;

        try {
//            ProcessBuilder builder = new ProcessBuilder("bash", "-c", " cat /proc/cpuinfo | grep model\\ name | awk '{print $10}' | uniq");
//            ProcessBuilder builder = new ProcessBuilder("bash", "-c", " grep -m 1 'model name' /proc/cpuinfo");
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", " grep -m 1 'cpu MHz' /proc/cpuinfo | awk '{print $4}'");
            //에러도 표준 출력으로 처리되서 서비스 지속가능하게하지만 그러기에 디버깅이 어려워진다..
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()){
                    cpuClock = line.replace("GHz","").trim();
                    cpuClock = line.replace("MHz","").trim();
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

    public String getMemory(){
        try {
            String line = "";
            String memmory = "";

            /**
             * 한글과 영어를 어떻게 구분할것인가...
             */
//            Process process = new ProcessBuilder("bash", "-c","free -b | awk '/^Mem:/ {print $2/1024/1024, $3/1024/1024}'").start();
            Process process = new ProcessBuilder("bash", "-c","free -b | awk '/^메모리:/ {print $2/1024/1024, $3/1024/1024}'").start();
//            Process process = new ProcessBuilder("bash", "-c","free --mega | awk '/^Mem:/ {print $2, $3}'").start();
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

            Process process = new ProcessBuilder("bash", "-c","df -Bk | awk '{total += $2; used += $3} END {print total/1024, used/1024}'").start();
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