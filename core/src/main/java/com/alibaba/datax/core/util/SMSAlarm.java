package com.alibaba.datax.core.util;


public class SMSAlarm {

    // private static final String SMS_SEND_URL_FORMAT = "http://220.181.29.61:8082/sendsms.do?mobile=%s&message=%s";
    private static final String SMS_SEND_URL_FORMAT =
            "http://smsknl.163.com:8089/servlet/CorpIdentifyNotCheck?phone=%s&message=%s&frmphone=%s&msgprop=61033&corpinfo=1";

    /**
     * 通过中转机发送短信
     *
     * @param mobile 电话号码
     * @param sms    短信内容
     */
    public static void sendSMS(String mobile, String sms) {
        String url = String.format(SMS_SEND_URL_FORMAT, mobile, bin2hex(sms), mobile);
        HttpClientUtils.get(url);
    }

    public static String bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }
}
