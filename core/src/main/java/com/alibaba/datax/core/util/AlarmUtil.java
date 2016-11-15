package com.alibaba.datax.core.util;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 报警用的类
 *
 * @author lujun
 */
public class AlarmUtil {

    /**
     * 向指定的人发送POPO信息，只限于公司的corp账号
     *
     * @param msg       信息内容
     * @param receivers 接受的邮箱地址
     */
    public static void sendPOPOAlarm(String msg, String... receivers) {
        if (null != receivers) {
            for (int i = 0; i < receivers.length; i++) {
                String to = receivers[i];
                POPOAlarm.sendPopoToCorp(to, msg);
            }
        }
    }

    /**
     * 向指定的人发送短信
     *
     * @param msg       信息内容
     * @param receivers 接受者的手机号码
     */
    public static void sendSMSAlarm(String msg, String... receivers) {
        if (null != receivers) {
            for (int i = 0; i < receivers.length; i++) {
                String to = receivers[i];
                SMSAlarm.sendSMS(to, msg);
            }
        }
    }

    public static void sendAlarmToDefaultUser(String msg) {
        List<String> alarmList = Lists.newArrayList();
        try {
            alarmList = Files.readLines(new File("/home/weblog/datax/conf/alarmlist.properties"), Charset.defaultCharset());
            for (String alarm : alarmList) {
                sendPOPOAlarm(msg, alarm.split("#")[0]);
                sendSMSAlarm(msg, alarm.split("#")[1]);
            }
        } catch (IOException e) {
            System.out.println("exception");
            e.printStackTrace();
        }
    }
}
