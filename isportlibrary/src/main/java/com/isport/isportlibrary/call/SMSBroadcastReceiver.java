package com.isport.isportlibrary.call;

/**
 * @author Created by Marcos Cheng on 2016/6/2.
 * If there are sms and {@link com.isport.isportlibrary.entry.NotificationEntry#isAllowSMS} is true, it will be pushed to ble device
 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.isportlibrary.entry.NotificationMsg;
import com.isport.isportlibrary.tools.Utils;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = SMSBroadcastReceiver.class.getSimpleName();
    static String regEx = "[\u4e00-\u9fa5]";
    static Pattern pat = Pattern.compile(regEx);

    public static boolean isContainsChinese(String str) {
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg;
    }

    public static String Bytes2String(byte[] bData) {
        int nLen = 0;
        for (int i = 0; i < bData.length; i++) {
            if (bData[i] != 0) nLen += 1;
            else break;
        }
        byte[] bStringBytes = new byte[nLen];
        for (int j = 0; j < nLen; j++) {
            bStringBytes[j] = bData[j];
        }
        String sbyte = null;
        try {
            //  sbyte = new String(bStringBytes, "GBK");// "mnw
            sbyte = new String(bStringBytes, "UTF-8");// "mnw

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sbyte;// "mnw
    }

    public void onReceive(Context context, Intent intent) {
        try {
            if (IS_DEBUG) {
                Log.e(TAG, "onReceive");
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "RECEIVE_SMS 权限未同意");
                return;
            }
            SmsMessage msg = null;
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (Object p : pdusObj) {
                    msg = SmsMessage.createFromPdu((byte[]) p);
                    String msgTxt = msg.getMessageBody();//get message body
                    Date date = new Date(msg.getTimestampMillis());//message time
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String senderNumber = msg.getOriginatingAddress();


                    String senderName = contactNameByNumber(context, senderNumber);
                    NotificationEntry notificationEntry = NotificationEntry.getInstance(context);

                    boolean isopen_sms = notificationEntry.isAllowSMS();
                    if (IS_DEBUG) {
                        Log.e(TAG, "phone numeber " + senderNumber + "sms content ===" + msgTxt + "  isopen_sms = " + isopen_sms);
                    }
                    if (isopen_sms && msgTxt != null) {
                        byte[] strMsg = msgTxt.getBytes("UTF-8");
                        byte[] strNum = senderNumber.getBytes("UTF-8");

                        handleW311SmsMessage(context, senderName, senderNumber, msgTxt, strNum, strMsg);
                        handleW337BSmsMessage(context, msgTxt);
                    }
                    return;
                }
                return;
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "onReceive 出错了" + e.toString());
            e.printStackTrace();
        }
    }

    private void handleW337BSmsMessage(Context context, String text) {
        byte[] bs = text.getBytes(Charset.forName("UTF-8"));
        byte pkNum = (byte) (bs.length % 17 > 0 ? bs.length / 17 + 1 : bs.length / 17);
        byte[][] contents = new byte[pkNum][20];
        for (int i = 0; i < contents.length; i++) {
            byte[] value = contents[i];
            value[0] = 0x03;
            value[1] = pkNum;
            value[2] = (byte) (i + 1);
            System.arraycopy(bs, 17 * i, value, 3, i < contents.length - 1 ? 17 : bs.length - 17 * (i));
            contents[i] = value;
        }
        Map<Integer, byte[][]> map = new HashMap<>();
        map.put(0x03, contents);
        sendNotiCmd(map);
    }

    private void handleW311SmsMessage(Context context, String senderName, String senderNumber, String sendMsg, byte[] strNum, byte[] strMsg) {
        //todo 内容和title都未去掉空格
        if (CmdController.getInstance(context).getBaseDevice() != null) {
            String deviceName = CmdController.getInstance(context).getBaseDevice().getName();
            String tpName = (deviceName == null ? "" : deviceName.contains("_") ? deviceName.split("_")[0] : deviceName.contains("-") ?
                    deviceName.split("-")[0] : deviceName.split(" ")[0]).toLowerCase();
            float version = CmdController.getInstance(context).getVersion();
            //TYL-5101 版本号大于等于90.32  TLY-5001 版本号大于等于90.32 用冒号"："
//            &&version >= 91.33f
            if ((deviceName.contains("BEAT") || tpName.contains("rush") || deviceName.contains("REFLEX") || deviceName.equalsIgnoreCase("W520") || deviceName.contains("GOODMANS")) || (deviceName.contains("W311N_") && version > 90.69f) || (deviceName.contains("W301B") || deviceName.contains("W307E") || deviceName.contains("TA300")) || (deviceName.contains("AS87") && version >= 90.12f) || (deviceName.contains("SAS87")) || (deviceName.contains("AS97") && version >= 91.06f) || ((deviceName.equalsIgnoreCase("TYL-5001") || deviceName.equalsIgnoreCase("TYL-5101")) && version >= 90.32f)) {
                byte[] content = new byte[60];//所有包括title和content最大60的长度
                String needSendStr;
                if (((deviceName.equalsIgnoreCase("TYL-5001") || deviceName.equalsIgnoreCase("W520") || deviceName.equalsIgnoreCase("TYL-5101")) && version >= 90.32f)) {
                    if (TextUtils.isEmpty(senderName)) {
                        //名字为空那么使用num
                        needSendStr = senderNumber + ":" + sendMsg;
                    } else {
                        needSendStr = senderName + ":" + sendMsg;
                    }
                } else {
                    if (TextUtils.isEmpty(senderName)) {
                        //名字为空那么使用num
                        needSendStr = senderNumber + " " + sendMsg;
                    } else {
                        needSendStr = senderName + " " + sendMsg;
                    }
                }

                Log.e(TAG, "**是REFLEX发送**" + " needSendStr == " + needSendStr);
                StringBuilder needSendStrbuilder = new StringBuilder();
                for (int i = 0; i < needSendStr.length(); i++) {
                    String bb = needSendStr.substring(i, i + 1);
// 生成一个Pattern,同时编译一个正则表达式,其中的u4E00("一"的unicode编码)-\u9FA5("龥"的unicode编码)
                    if (Utils.isGB2312(bb)) {
                        Log.e(TAG, "**是中文用空格代替 == " + bb);
                        needSendStrbuilder.append(" ");
                    } else {
                        Log.e(TAG, "**不是中文直接添加== " + bb);
                        needSendStrbuilder.append(bb);
                    }
                }
                needSendStr = needSendStrbuilder.toString();
                Log.e(TAG, "**要发送的Str== " + needSendStr);
                byte[] byTextTp = new byte[0];
                try {
                    byTextTp = needSendStr.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (byTextTp.length <= 60) {//当长度小于45时补0
                    Log.e(TAG, "**title和content的总长度小于等于60，不足的地方补0**");
                    System.arraycopy(byTextTp, 0, content, 0, byTextTp.length);
                    content = Utils.addFF(content, byTextTp.length, 59);
                } else {//长度大于45切割最前面的45
                    Log.e(TAG, "**title和content的长度大于60，截取60字节**");
                    System.arraycopy(byTextTp, 0, content, 0, 59);
                }
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < content.length; i++) {
                    builder.append(String.format("%02X ", content[i]));
                }
                Log.e(TAG, "**要去发送**" + builder.toString());

                NotificationMsg msg = new NotificationMsg(0x12, content);
                sendNotiCmd(msg);
                sendNotiCmd(content, 1, 0x12);
            } else {
                byte[] btSMS = new byte[45];
                byte[] btSMS_nameOrNum = new byte[15];

                //当名字为空时以电话号码为名字，发送的内容在REFLEX或者GOODMANS时，变为name+" "+content;
                if (!TextUtils.isEmpty(senderName)) {
                    for (int i = 0; i < 15; i++) {
                        char[] strName = senderName.toCharArray();
                        if (strName.length >= (i + 1)) {
                            btSMS_nameOrNum[i] = (byte) strName[i];
                        } else btSMS_nameOrNum[i] = (byte) 0xff;
                    }
                } else {
                    for (int i = 0; i < 15; i++) {
                        if (strNum.length >= (i + 1)) {
                            btSMS_nameOrNum[i] = strNum[i];
                        } else {
                            btSMS_nameOrNum[i] = (byte) 0xff;
                        }
                    }
                }

                if (strMsg.length >= 45) {
                    for (int i = 0; i < 45; i++) {
                        btSMS[i] = strMsg[i];
                    }
                } else {
                    for (int i = 0; i < 45; i++) {
                        if (strMsg.length >= (i + 1)) {
                            btSMS[i] = strMsg[i];
                        } else {
                            btSMS[i] = (byte) 0xff;
                        }
                    }
                }
                byte[] smsContent = new byte[60];
                System.arraycopy(btSMS_nameOrNum, 0, smsContent, 0, 15);
                System.arraycopy(btSMS, 0, smsContent, 15, 45);
                NotificationMsg msg = new NotificationMsg(0x12, smsContent);
                Log.e(TAG, "onReceive 发到实现类 000 ");
                sendNotiCmd(msg);
                sendNotiCmd(smsContent, 1, 0x12);
            }
        } else {
            byte[] btSMS = new byte[45];
            byte[] btSMS_nameOrNum = new byte[15];

            //当名字为空时以电话号码为名字，发送的内容在REFLEX或者GOODMANS时，变为name+" "+content;
            if (!TextUtils.isEmpty(senderName)) {
                for (int i = 0; i < 15; i++) {
                    char[] strName = senderName.toCharArray();
                    if (strName.length >= (i + 1)) {
                        btSMS_nameOrNum[i] = (byte) strName[i];
                    } else btSMS_nameOrNum[i] = (byte) 0xff;
                }
            } else {
                for (int i = 0; i < 15; i++) {
                    if (strNum.length >= (i + 1)) {
                        btSMS_nameOrNum[i] = strNum[i];
                    } else {
                        btSMS_nameOrNum[i] = (byte) 0xff;
                    }
                }
            }

            if (strMsg.length >= 45) {
                for (int i = 0; i < 45; i++) {
                    btSMS[i] = strMsg[i];
                }
            } else {
                for (int i = 0; i < 45; i++) {
                    if (strMsg.length >= (i + 1)) {
                        btSMS[i] = strMsg[i];
                    } else {
                        btSMS[i] = (byte) 0xff;
                    }
                }
            }
            byte[] smsContent = new byte[60];
            System.arraycopy(btSMS_nameOrNum, 0, smsContent, 0, 15);
            System.arraycopy(btSMS, 0, smsContent, 15, 45);
            NotificationMsg msg = new NotificationMsg(0x12, smsContent);
            Log.e(TAG, "onReceive 发到实现类 111 ");

            sendNotiCmd(msg);
            sendNotiCmd(smsContent, 1, 0x12);
        }
    }

    /**
     * W311 serial that type is {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W311}
     *
     * @param msg
     */
    public void sendNotiCmd(NotificationMsg msg) {

    }

    /**
     * use @{@link #sendNotiCmd(NotificationMsg)} instead
     *
     * @param bs
     * @param index
     * @param type
     */
    @Deprecated
    public void sendNotiCmd(byte[] bs, int index, int type) {

    }

    /**
     * W337B serial that type is {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W337B}
     *
     * @param map
     */
    public void sendNotiCmd(Map<Integer, byte[][]> map) {

    }

    /**
     * Query name or email for the number
     *
     * @param context
     * @param number
     * @return
     */
    public String contactNameByNumber(Context context, String number) {
        //String number = "18052369652";
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || number == null) {
            return "";
        }
        String name = "";
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
            ContentResolver resolver = context.getContentResolver();
            cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0);
                    return name;
                }
                cursor.close();
                cursor = null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            cursor = null;
            if (number.contains("/")) {
                number = number.replace("/", "");
                contactNameByNumber(context, number);
            }
        }

        return name;
    }
}