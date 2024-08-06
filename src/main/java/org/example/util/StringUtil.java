package org.example.util;

import java.security.MessageDigest;

public class StringUtil {
    public static String intToIp(long num) {
        return ((num >> 24 ) & 0xFF) + "." +
                ((num >> 16 ) & 0xFF) + "." +
                ((num >>  8 ) & 0xFF) + "." +
                ( num        & 0xFF);
    }

    public static Long ipToLong(String ipAddr) {
        String[] ipAddrArray = ipAddr.split("\\.");

        long num = 0;
        for (int i=0;i<ipAddrArray.length;i++) {
            int power = 3-i;
            /*
             * i의 값으 범위는 0~255 사이이며, 그 값을 256으로 나눈 나머지 값에
             * 256을 power의 값인
             * 1~9 사이는 2,
             * 10~99 사이는 1,
             * 100 이상은 0 의 값에 누승하여 num 값에 더함
             */
            num += ((Integer.parseInt(ipAddrArray[i])%256 * Math.pow(256,power)));
        }
        return num;
    }

    public static Integer ipToInt(String ipAddr) {
        String[] ipAddrArray = ipAddr.split("\\.");

        int num = 0;
        for (int i=0;i<ipAddrArray.length;i++) {
            int power = 3-i;
            /*
             * i의 값으 범위는 0~255 사이이며, 그 값을 256으로 나눈 나머지 값에
             * 256을 power의 값인
             * 1~9 사이는 2,
             * 10~99 사이는 1,
             * 100 이상은 0 의 값에 누승하여 num 값에 더함
             */
            num += ((Integer.parseInt(ipAddrArray[i])%256 * Math.pow(256,power)));
        }
        return num;
    }

    public static String emptyToDash(String str, int num){

        if(str.length() < num){
            int cnt = str.length();
            if(str.length() != num){
                for(int i=0; i<num-cnt; i++){
                    str += "-";
                }
            }
        }

        return str;
    }

    public static String getEncMD5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }

            return hexString.toString();
        } catch (Exception e){
            return e.getMessage();
        }
    }

    public static String remakeGateway(String s) {
        StringBuilder sb = new StringBuilder();
        s = s.replace(".",":");
        String[] split = s.split(":");

        for(int i=0; i<split.length; i++){
            if (i != 0) {
                sb.append(":");
            }

            if(split[i].length() <2){
                split[i] = "0"+split[i];
            }

            sb.append(split[i]);
        }

        return sb.toString();
    }

        /*
        StringBuffer sbuf = new StringBuffer();


        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        mDigest.update(txt.getBytes());

        byte[] msgStr = mDigest.digest() ;

        for(int i=0; i < msgStr.length; i++){
            String tmpEncTxt = Integer.toHexString((int)msgStr[i] & 0x00ff) ;
            sbuf.append(tmpEncTxt) ;
        }

        return sbuf.toString() ;
    }
    */
}