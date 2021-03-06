package com.alibaba.datax.core.util;

import com.alibaba.datax.common.util.StrUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

/**
 * 报警用的类
 *
 * @author lujun
 */
public class AlarmUtil {
    public static final String proid = "datacube";
    public static final String signature = "e60cf0e8-0d25-4137-958d-da35ee66e28f";
    public static final String methods = "sendPOPO,sendYiXin,sendSMS";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String msg = String.format("\n" + "%s: %s\n" +
                        "%s: %s\n" + "%s: %s\n" + "%s: %s\n"
                        + "%s: %s\n" + "%s: %s\n" + "%s: %s\n"
                        + "%s: %s\n",
                "当前任务信息",
                123,

                "任务启动时刻",
                123,

                "任务结束时刻",
                123,

                "任务总计耗时",
                String.valueOf(123) + "s",
                "任务平均流量",
                StrUtil.stringify(123)
                        + "/s",
                "记录写入速度",
                String.valueOf(123)
                        + "rec/s", "读出记录总数",
                String.valueOf(123),
                "读写失败总数",
                String.valueOf(123)
        );

        try {
//            String receivers = getAlarmUsers("/home/weblog/datax/conf/alarmlist.properties");
//            String msg = "测试";
            String receivers = getAlarmUsers("/Users/qmgeng/workspace/DataX/core/src/main/conf/alarmlist.properties");
            long ac_timestamp = System.currentTimeMillis();
            String ac_signature = generateSignature("e60cf0e8-0d25-4137-958d-da35ee66e28f", ac_timestamp);
            String ss = HttpClientUtils.get("http://alarm.netease.com/api/sendMessage?to=" + receivers + "&isSync=true&content=" + URLEncoder.encode(msg, "utf-8") + "&ac_signature=" + ac_signature + "&ac_timestamp=" + ac_timestamp + "&ac_appName=" + proid + "&methods=" + methods);
            System.out.println(ss);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    /**
     * 向指定的人发送POPO信息，只限于公司的corp账号
     *
     * @param msg       信息内容
     * @param receivers 接受的邮箱地址
     */
    public static void sendPOPOAlarm(String msg, String receivers) {
        try {
            if (null != receivers) {
                long ac_timestamp = System.currentTimeMillis();
                String ac_signature = generateSignature("e60cf0e8-0d25-4137-958d-da35ee66e28f", ac_timestamp);
                HttpClientUtils.get("http://alarm.netease.com/api/sendMessage?to=" + receivers + "&isSync=true&content=" + URLEncoder.encode(msg, "utf-8") + "&ac_signature=" + ac_signature + "&ac_timestamp=" + ac_timestamp + "&ac_appName=" + proid + "&methods=sendPOPO");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向指定的人发送短信
     *
     * @param msg       信息内容
     * @param receivers 接受者的手机号码
     */
    public static void sendSMSAlarm(String msg, String receivers) {
        try {
            if (null != receivers) {
                long ac_timestamp = System.currentTimeMillis();
                String ac_signature = generateSignature("e60cf0e8-0d25-4137-958d-da35ee66e28f", ac_timestamp);
                HttpClientUtils.get("http://alarm.netease.com/api/sendMessage?to=" + receivers + "&isSync=true&content=" + URLEncoder.encode(msg, "utf-8") + "&ac_signature=" + ac_signature + "&ac_timestamp=" + ac_timestamp + "&ac_appName=" + proid + "&methods=sendSMS");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向指定的人发送易信
     *
     * @param msg       信息内容
     * @param receivers 接受者的易信
     */
    public static void sendYiXinAlarm(String msg, String receivers) {
        try {
            if (null != receivers) {
                long ac_timestamp = System.currentTimeMillis();
                String ac_signature = generateSignature("e60cf0e8-0d25-4137-958d-da35ee66e28f", ac_timestamp);
                HttpClientUtils.get("http://alarm.netease.com/api/sendMessage?to=" + receivers + "&isSync=true&content=" + URLEncoder.encode(msg, "utf-8") + "&ac_signature=" + ac_signature + "&ac_timestamp=" + ac_timestamp + "&ac_appName=" + proid + "&methods=sendYiXin");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void sendAlarm(String msg) {
        try {
            String receivers = getAlarmUsers("/home/weblog/datax/conf/alarmlist.properties");
            long ac_timestamp = System.currentTimeMillis();
            String ac_signature = generateSignature("e60cf0e8-0d25-4137-958d-da35ee66e28f", ac_timestamp);
            HttpClientUtils.get("http://alarm.netease.com/api/sendMessage?to=" + receivers + "&isSync=true&content=" + URLEncoder.encode(msg, "utf-8") + "&ac_signature=" + ac_signature + "&ac_timestamp=" + ac_timestamp + "&ac_appName=" + proid + "&methods=" + methods);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String getJobInfoById(String jobid) {
        Properties prop = new Properties();
        String jobinfo = "";
        try {
            Reader inStream = new InputStreamReader(new FileInputStream("/home/weblog/datax/conf/jobinfo.properties"), "UTF-8");
            prop.load(inStream);
            jobinfo = prop.getProperty(jobid);
            if (StringUtils.isBlank(jobinfo)) {
                return jobid;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jobinfo;
    }

    public static String getAlarmUsers(String path) {
        List<String> alarmList = Lists.newArrayList();
        String alarmUsers = "";
        try {
            alarmList = Files.readLines(new File(path), Charset.defaultCharset());
            alarmUsers = Joiner.on(",").skipNulls().join(alarmList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return alarmUsers;
    }

    public static String generateSignature(String secret, long timestamp) throws NoSuchAlgorithmException {
        String value = secret + timestamp;

        MessageDigest messageDigest = MessageDigest.getInstance("md5");
        messageDigest.update(value.getBytes());
        byte[] bb = messageDigest.digest();

        String s = getFormattedText(bb);
        return s;
    }

    public static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

}
