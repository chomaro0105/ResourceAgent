package org.example.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ProcessMemoryCheck extends Thread {

    public void run() {
        int cntCheckMem = 30;

        SimpleDateFormat DATEFORMAT_MMDD = new SimpleDateFormat("MMdd");
        Calendar compareCal = Calendar.getInstance();

        String strCompareDate = DATEFORMAT_MMDD.format(compareCal.getTime());
        String strCurrentDate = strCompareDate;

        while (true) {
            Calendar currentCal = Calendar.getInstance();
            strCurrentDate = DATEFORMAT_MMDD.format(compareCal.getTime());

            /**
             * 날짜 바뀜
             */
            if(!strCurrentDate.equalsIgnoreCase(strCompareDate)){
                strCompareDate = strCurrentDate;

                /**
                 * 날짜 변경시 추가 행동.
                 */
                changeDayRunnging();
            }

            /**
             * 매분 실행되니깐, 10번이면 10분에 한번은 메모리 상태 체크
             */
            if (cntCheckMem > 10) {
                checkMemory();
                checkMemoryAfterRunning();
                cntCheckMem = 0;
            } else {
                cntCheckMem++;
            }

            /**
             * 매번 실행 행동.
             */
            running();
            try {
                sleep(1000 * 60);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * 메모리 상태를 체크한다.
     */
    private void checkMemory(){
        System.out.println("================================================");
        System.out.println("JVM MAX MEMORY   : " + Runtime.getRuntime().maxMemory() / 1024 + " MB");
        System.out.println("JVM TOTAL MEMORY : " + Runtime.getRuntime().totalMemory() / 1024 + " MB");
        System.out.println("JVM FREE MEMORY  : " + Runtime.getRuntime().freeMemory() / 1024 + " MB");
        System.out.println("================================================");
    }

    /**
     * 매분 실행 할 내용
     */
    public void running(){ }

    /**
     * 10분마다 실행할 내용
     */
    public void checkMemoryAfterRunning(){ }

    /**
     * 날짜 변경시 마다 실행 할 내용.
     */
    public void changeDayRunnging(){ }
}
