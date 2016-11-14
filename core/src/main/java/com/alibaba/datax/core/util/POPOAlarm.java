package com.alibaba.datax.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class POPOAlarm {

    private static final String CORP_POPO_URL = "http://220.181.29.178:5820/popo";
    private static Log logger = LogFactory.getLog(POPOAlarm.class);

    /**
     * 给泡泡发消息 post方式提交.
     *
     * @param to  String 接收人
     * @param msg String 信息内容
     */
    public static void sendPopoToCorp(String to, String msg) {
        if (StringUtils.isBlank(to)) {
            logger.error("Receivor is missing, operation aborted");
            return;
        }

        if (StringUtils.isBlank(msg)) {
            logger.error("Message content is missing, operation aborted");
            return;
        }
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("account", to);
            params.put("msg", msg);
            HttpClientUtils.post(CORP_POPO_URL, params);
        } catch (Exception e) {
            logger.error("Error occured when send message to POPO gateway, operation aborted", e);
            return;
        }
    }

//    public static void sendPopoToDefaultUser(String msg) {
//        URL url = POPOAlarm.class.getClassLoader().getResource("alarmlist.properties");
//        List<String> corpEmails = Lists.newArrayList();
//        try {
//            corpEmails = Files.readLines(new File(url.getPath()), Charset.defaultCharset());
//            for (String email : corpEmails) {
//                sendPopoToCorp(email, msg);
//            }
//        } catch (IOException e) {
//            System.out.println("exception");
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) {
//        sendPopoToDefaultUser("sss");
//    }
}
