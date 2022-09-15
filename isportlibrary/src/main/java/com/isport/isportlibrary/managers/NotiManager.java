package com.isport.isportlibrary.managers;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.isport.isportlibrary.controller.Cmd337BController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.isportlibrary.entry.NotificationMsg;
import com.isport.isportlibrary.tools.Constants;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.isportlibrary.tools.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

/**
 * Created by Administrator on 2017/9/27.
 */

public class NotiManager {

    private static final String TAG = NotiManager.class.getSimpleName();
    public static String[] pkNames = null;
    public static Vector<NotificationMsg> msgVector = new Vector<>();
    public static Map<String, Integer> notiType = null;
    private static String[][] strPkNames = new String[][]{{Constants.KEY_13_PACKAGE},
            {Constants.KEY_14_PACKAGE},
            {Constants.KEY_15_PACKAGE, Constants.KEY_15_PACKAGE_1, Constants.KEY_15_PACKAGE_2},
            {Constants.KEY_16_PACKAGE},
            {Constants.KEY_17_PACKAGE},
            {Constants.KEY_18_PACKAGE},
            {Constants.KEY_19_PACKAGE},
            {Constants.KEY_1A_PACKAGE},
            {Constants.KEY_1B_PACKAGE},
            {Constants.KEY_1C_PACKAGE}
    };

    private Context mContext;
    private static NotiManager sInstance;
    public static StringBuilder logBuilder;

    private ArrayList<String> musicList=new ArrayList<>();
    private NotiManager(Context context) {
        this.mContext = context;
        musicList.clear();
        musicList.add("com.android.mediacenter");
        musicList.add("com.tencent.qqmusic");
        musicList.add("com.kugou.android");

        initConfig();
    }

    public static NotiManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (NotiManager.class) {
                if (sInstance == null) {
                    sInstance = new NotiManager(context.getApplicationContext());

                }
            }
        }
        return sInstance;
    }


    String startNoti="";

    public void sendmusic(String text) {
        text = text.toString().replace(" ", " ");

        Log.e("sendmusic","startNoti="+startNoti+"text="+text);

        if(!startNoti.equals("Track not obtained")&&startNoti.equals(text)){
            return;
        }
        startNoti=text;
        if (!TextUtils.isEmpty(text)) {
            byte[] src = text.getBytes(Charset.forName("UTF-8"));
            int len = 44;
            if (src.length >= 44) {
                len = 44;
            } else {
                len = src.length;
            }
            len=len+1;
            int pkNum = (len % 15 > 0 ? len / 15 + 1 : len / 15);
            byte[] bs = new byte[pkNum*15];
            for (int i = 0; i < bs.length; i++) {
                bs[i] = 0;
            }
            int srcLen=src.length<bs.length?src.length:bs.length;
            if(srcLen>40){
                srcLen=40;
            }
            for (int i = 0; i <srcLen; i++) {
                bs[i] = src[i];
            }

            pkNum = (pkNum < 0 ? 0 : pkNum);
            byte[][] contents = new byte[pkNum][20];
            ArrayList<byte[]> lists = new ArrayList<>();
            for (int i = 0; i < contents.length; i++) {
                byte[] value = contents[i];
                value[0] = (byte) 0xBE;
                value[1] = (byte) 0x08;
                value[2] = (byte) 0x03;
                value[3] = (byte) 0xFE;
                value[4] = (byte) (i);
                System.arraycopy(bs, 15 * i, value, 5, i < contents.length - 1 ? 15 : bs.length - 15 * (i));
                // contents[i] = value;

                lists.add(value);
            }



            CmdController.getInstance(mContext).sendNotiCmdMusic(lists);
        }
    }

    /**
     * handle notification, need to set NotificationEntry, to see {@link NotificationEntry} for details
     *
     * @param packagename  whose notifications you want to push to ble device
     * @param notification the notification will be pushed to ble device
     */

    public void handleNotification(String packagename, Notification notification) {

        NotificationEntry notiEntry = NotificationEntry.getInstance(mContext);
        Log.e(TAG, "notiEntry.isAllowApp()" + notiEntry.isAllowApp() + ",notiEntry.isOpenNoti()" + notiEntry.isOpenNoti() + ",packagename:" + packagename + "----notiType"+notification.tickerText);


        String tickerText = null;
        String title = null;
        String text = null;
        boolean isKitKat = false;
        Log.e("XUQIAN", "收到通知" + packagename);

        if (notification != null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                isKitKat = true;
                Bundle bundle = notification.extras;
                text = bundle.getString(Notification.EXTRA_TEXT);////notifitication content
                title = bundle.getString(Notification.EXTRA_TITLE);///notification title
                CharSequence[] sequences = bundle.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                if (sequences != null && sequences.length > 0) {
                    if (TextUtils.isEmpty(title)) {
                        title = "";
                    }
                    if (title.equals("Instagram")) {
                        text = sequences[0].toString();
                    } else {
                        text = sequences[sequences.length - 1].toString();
                    }
                }
                if (text == null || title == null) {
                    isKitKat = false;
                    tickerText = (notification.tickerText == null ? "" : notification.tickerText.toString());
                }
            } else {
                tickerText = (notification.tickerText == null ? "" : notification.tickerText.toString());
            }
            Log.e("XUQIAN", "音乐名称" + tickerText);

            if(musicList.contains(packagename)){
                if(TextUtils.isEmpty(tickerText)){
                    sendmusic("Track not obtained");
                    Log.e("XUQIAN", "未发送音乐名称");

                }else{
                    sendmusic(tickerText);
                    Log.e("XUQIAN", "发送音乐名称" + tickerText);

                }

            }
        }
        if (!(notiEntry.isAllowApp() && notiEntry.isOpenNoti())) {
            return;
        }

        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(packagename.toString(), 0);
            boolean pack = notiEntry.isAllowPackage(packagename, true);
            boolean noti = notiEntry.isOpenNoti();
            boolean appnoti = notiEntry.isAllowApp();


            if (notiType == null) {
                initConfig();
            }
            Log.e(TAG, "notiType.size() >" + notiType.size() + ",notiType.get(packagename)" + notiType.get(packagename) + ",pack:" + pack + "noti:" + noti + "appnoti:" + appnoti);
            if (notiType != null && notiType.size() > 0 && notiType.get(packagename) != null && pack &&
                    noti && appnoti) {
                int type = notiType.get(packagename).byteValue();
                boolean isopen_detail = notiEntry.isShowDetail();


                Log.e(TAG, "notiEntry.isAllowApp()" + notiEntry.isAllowApp() + ",notiEntry.isOpenNoti()" + notiEntry.isOpenNoti() + ",packagename:" + packagename + "notiType" + "isopen_detail:" + isopen_detail + "notification:" + notification);


                if (notification != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        isKitKat = true;
                        Bundle bundle = notification.extras;
                        text = bundle.getString(Notification.EXTRA_TEXT);////notifitication content
                        title = bundle.getString(Notification.EXTRA_TITLE);///notification title
                        CharSequence[] sequences = bundle.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                        if (sequences != null && sequences.length > 0) {
                            if (TextUtils.isEmpty(title)) {
                                title = "";
                            }
                            if (title.equals("Instagram")) {
                                text = sequences[0].toString();
                            } else {
                                text = sequences[sequences.length - 1].toString();
                            }
                        }
                        if (text == null || title == null) {
                            isKitKat = false;
                            tickerText = (notification.tickerText == null ? "" : notification.tickerText.toString());
                        }
                    } else {
                        tickerText = (notification.tickerText == null ? "" : notification.tickerText.toString());
                    }
                    //对WhatsApp的特殊处理
//                    ***isKitKat***true***text***‪+86 177 2047 9861‬:
// Hhh***title***WhatsApp***tickerText***null***type***27
                    //  if (IS_DEBUG)
                    Log.e(TAG, "00000000***isKitKat***" + isKitKat + "***text***" + text + "***title***" + title +
                            "***tickerText***" + tickerText + "***type***" + type);
//                    if ("WhatsApp".equals(title)) {
//                        if (text.contains(":")){
//                            String[] split = text.split(":");
//                            title = split[0];
//                            text = split[1];
//                        }else {
//                            //消息很多的情况
//                            title = "";
//                        }
//                    }
                } else {
                    tickerText = "";
                }
                if (IS_DEBUG)
                    Log.e("NotiManager", DateUtil.dataToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
                if (IS_DEBUG)
                    Log.e(TAG, "111111111***isKitKat***" + isKitKat + "***text***" + text + "***title***" + title +
                            "***tickerText***" + tickerText + "***type***" + type);
//                05 - 16 10:38:46.422 10960 - 10960 / com.isport.tracker E / NotiManager: ***isKitKat ***true
// ***text ***
//                Vghh ***title ***‪+86 177 2047 9861‬: ​***tickerText ***null ***type ***27
//                05 - 16 10:38:46.473 10960 - 10960 / com.isport.tracker E / NotiManager: ***isKitKat ***true
// ***text ***
//                gh ***title ***Test(‎3条信息):madbug ***tickerText ***null ***type ***27
//                05 - 16 10:38:46.514 10960 - 10960 / com.isport.tracker E / NotiManager: ***isKitKat ***true
// ***text ***
//                madbug @Test :gh ***title ***WhatsApp ***tickerText ***null ***type ***27

                if (isopen_detail) {
                    handlerW311Msg(isKitKat, text, title, tickerText, type, isopen_detail);
                } else {
                    handlerW311Msg(isKitKat, "", title, tickerText, type, isopen_detail);
                }


                handleW337Msg(packagename.toString(), (TextUtils.isEmpty(title) ? "" : title) + " : " + (text == null ?
                        tickerText : text));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e("NotiManager", e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("NotiManager", e.toString());
        } catch (Exception e) {
            Log.e("NotiManager", e.toString());
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

//    public boolean isGB2312(String str){
//        char[] chars=str.toCharArray();
//        boolean isGB2312=false;
//        for(int i=0;i<chars.length;i++){
//            byte[] bytes=(""+chars[i]).getBytes();
//            if(bytes.length==2){
//                int[] ints=new int[2];
//                ints[0]=bytes[0]& 0xff;
//                ints[1]=bytes[1]& 0xff;
//                if(ints[0]>=0x81 && ints[0]<=0xFE && ints[1]>=0x40 && ints[1]<=0xFE){
//                    isGB2312=true;
//                    break;
//                }
//            }
//        }
//        return isGB2312;
//    }


    private void handlerW311Msg(boolean isKitKat, String text, String title, String tickerText, int type, boolean isDetail) throws
            UnsupportedEncodingException {
//                ***text ***Vghh ***title ***‪+86 177 2047 9861‬: ​***tickerText ***null ***type ***27
//                ***text ***gh ***title ***Test(‎3条信息):madbug ***tickerText ***null ***type ***27
//                ***text ***madbug @Test :gh ***title ***WhatsApp ***tickerText ***null ***type ***27

        try {
            if (TextUtils.isEmpty(title)) {
                title = "";
            }
            if (isDetail) {
                if (title.equals("Instagram")) {
                    title = "";
                }
            }

            byte[] btTicket = null;
            if (tickerText != null) {
                btTicket = tickerText.getBytes("UTF-8");
            }
            byte[] content = new byte[60];//所有包括title和content最大60的长度

            //todo 内容和title都未去掉空格
            if (isKitKat) {
                if (CmdController.getInstance(mContext).getBaseDevice() != null) {
                    int deviceType = CmdController.getInstance(mContext).getBaseDevice().getDeviceType();
                    String deviceName = CmdController.getInstance(mContext).getBaseDevice().getName();
                    String tpName = (deviceName == null ? "" : deviceName.contains("_") ? deviceName.split("_")[0] : deviceName.contains("-") ?
                            deviceName.split("-")[0] : deviceName.split(" ")[0]).toLowerCase();
                    float version = CmdController.getInstance(mContext).getVersion();
//            if (BaseDevice.TYPE_W307S==deviceType){
//                //todo  费费客户307s要求title和content衔接在一起
//            }
                    if (IS_DEBUG)
                        Log.e(TAG, "**是deviceName发送**" + deviceName);
                    if ((deviceName.contains("Reflex2C")||deviceName.contains("BEAT") || tpName.contains("rush") || deviceName.contains("REFLEX") || deviceName.contains("GOODMANS")) || (deviceName.contains("W311N_") && version > 90.69f) || (deviceName.contains("W301B") || deviceName.contains("W307E") || deviceName.contains("TA300")) || (deviceName.contains("AS87") && version >= 90.12f) || (deviceName.contains("SAS87") && version >= 91.33f) || (deviceName.contains("AS97") && version >= 91.06f) || ((deviceName.equalsIgnoreCase("TYL-5001") || deviceName.equalsIgnoreCase("TYL-5101")) && version >= 90.32f)) {
                        Log.e(TAG, "**是REFLEX发送**" + " title == " + title + " text == " + text);
                        String needSendStr;
                        if (((deviceName.equalsIgnoreCase("TYL-5001") || deviceName.equalsIgnoreCase("TYL-5101")) && version >= 90.32f) || (deviceName.contains("SAS87"))) {
                            needSendStr = title + ":" + text;
                        } else {
                            if (text.contains(":")) {
                                text = text.replace(":", "");
                            }
                            needSendStr = title + " " + text;

                        }

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
//                char[] chars = needSendStr.toCharArray();
//                for (int i = 0; i < chars.length; i++) {
//                    if (isGB2312(String.valueOf(chars[i]))){
//                        //是中文，直接用" "空格代替
//                        Log.e(TAG, "**是中文用空格代替 == "+String.valueOf(chars[i]));
//                        needSendStrbuilder.append(" ");
//                    }else{
//                        //不是中文，直接拼接
//                        Log.e(TAG, "**不是中文直接添加== "+String.valueOf(chars[i]));
//                        needSendStrbuilder.append(String.valueOf(chars[i]));
//                    }
//                }
                        needSendStr = needSendStrbuilder.toString();
                        Log.e(TAG, "**要发送的Str== " + needSendStr);
                        byte[] byTextTp = needSendStr.getBytes("UTF-8");
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
                    } else {
                        byte[] byText = new byte[45];//这儿是内容的长度，最大是45
                        byte[] byTextTp = text.getBytes("UTF-8");
                        if (byTextTp.length <= 45) {//当长度小于45时补0
                            if (IS_DEBUG)
                                Log.e(TAG, "**content的长度小于15**");
                            System.arraycopy(byTextTp, 0, byText, 0, byTextTp.length);
                            byText = Utils.addFF(byText, byTextTp.length, 44);
                        } else {//长度大于45切割最前面的45
                            if (IS_DEBUG)
                                Log.e(TAG, "**content的长度大于15**");
                            System.arraycopy(byTextTp, 0, byText, 0, 45);
                        }

                        byte[] byTitle = new byte[15];//title的长度，最大是15
                        byte[] byTitleTp = title.getBytes("UTF-8");
                        if (byTitleTp.length <= 15) {//当长度小于15时，补0
                            if (IS_DEBUG)
                                Log.e(TAG, "**title的长度小于15**");
                            System.arraycopy(byTitleTp, 0, byTitle, 0, byTitleTp.length);
                            byTitle = Utils.addFF(byTitle, byTitleTp.length, 14);
                        } else {//长度大于15时，切割
                            if (IS_DEBUG)
                                Log.e(TAG, "**title的长度大于15**");
                            System.arraycopy(byTitleTp, 0, byTitle, 0, 15);
                        }
                        //最后将title和content拼接到所有内容中，用于发送
                        System.arraycopy(byTitle, 0, content, 0, 15);
                        System.arraycopy(byText, 0, content, 15, 45);
                    }
                } else {
                    byte[] byText = new byte[45];//这儿是内容的长度，最大是45
                    byte[] byTextTp = text.getBytes("UTF-8");
                    if (byTextTp.length <= 45) {//当长度小于45时补0
                        if (IS_DEBUG)
                            Log.e(TAG, "**content的长度小于15**");
                        System.arraycopy(byTextTp, 0, byText, 0, byTextTp.length);
                        byText = Utils.addFF(byText, byTextTp.length, 44);
                    } else {//长度大于45切割最前面的45
                        if (IS_DEBUG)
                            Log.e(TAG, "**content的长度大于15**");
                        System.arraycopy(byTextTp, 0, byText, 0, 45);
                    }

                    byte[] byTitle = new byte[15];//title的长度，最大是15
                    byte[] byTitleTp = title.getBytes("UTF-8");
                    if (byTitleTp.length <= 15) {//当长度小于15时，补0
                        if (IS_DEBUG)
                            Log.e(TAG, "**title的长度小于15**");
                        System.arraycopy(byTitleTp, 0, byTitle, 0, byTitleTp.length);
                        byTitle = Utils.addFF(byTitle, byTitleTp.length, 14);
                    } else {//长度大于15时，切割
                        if (IS_DEBUG)
                            Log.e(TAG, "**title的长度大于15**");
                        System.arraycopy(byTitleTp, 0, byTitle, 0, 15);
                    }
                    //最后将title和content拼接到所有内容中，用于发送
                    System.arraycopy(byTitle, 0, content, 0, 15);
                    System.arraycopy(byText, 0, content, 15, 45);
                }
            } else {
                if (btTicket == null) {
                    content = Utils.addFF(content, 0, content.length - 1);
                } else {
                    if (btTicket.length >= 60) {
                        System.arraycopy(btTicket, 0, content, 0, 60);
                    } else {
                        System.arraycopy(btTicket, 0, content, 0, btTicket.length);
                        content = Utils.addFF(content, btTicket.length, 59);
                    }
                }
            }
//        byte[] ddd = new byte[15];
//        System.arraycopy(content, 0, ddd, 0, 15);
            CmdController.getInstance(mContext).sendNotiCmd(new NotificationMsg(type, content));

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //BEBE--0808	--0303--FEFE--序号序号	--名称名称


    private void handleW337Msg(String packName, String text) {
        byte type = 0;
        /*if (packName.contains(Constants.KEY_14_PACKAGE)) {
            type = 0x02;
        } else if (packName.contains(Constants.KEY_13_PACKAGE)) {
            type = 0x01;
        } else */
        if (packName.contains(Constants.KEY_15_PACKAGE) || packName.contains(Constants.KEY_15_PACKAGE_1) || packName
                .contains(Constants.KEY_15_PACKAGE_2)) {
            type = 0x05;
        } else if (packName.contains(Constants.KEY_1B_PACKAGE)) {
            type = 0x04;
        }
        if (type != 0 && text != null) {
            byte[] bs = text.getBytes(Charset.forName("UTF-8"));
            int pkNum = (bs.length % 17 > 0 ? bs.length / 17 + 1 : bs.length / 17);
            pkNum = (pkNum < 0 ? 0 : pkNum);
            byte[][] contents = new byte[pkNum][20];
            for (int i = 0; i < contents.length; i++) {
                byte[] value = contents[i];
                value[0] = type;
                value[1] = (byte) (pkNum & 0xff);
                value[2] = (byte) (i + 1);
                System.arraycopy(bs, 17 * i, value, 3, i < contents.length - 1 ? 17 : bs.length - 17 * (i));
                contents[i] = value;
            }
            Map<Integer, byte[][]> map = new HashMap<>();
            map.put((int) type, contents);
            Cmd337BController.getInstance(mContext).sendMessage(map);
        }
    }

    private void initConfig() {
        try {
           /* ApplicationInfo info = IsportManager.getInstance().getIsportManagerContext().getPackageManager()
                    .getApplicationInfo(IsportManager.getInstance().getIsportManagerContext().getPackageName(),
                            PackageManager.GET_META_DATA);
            if (info != null) {
                int packlistPath = info.metaData.getInt("isport.pklist");
                final XmlResourceParser parser = IsportManager.getInstance().getIsportManagerContext().getResources()
                        .getXml(packlistPath);

                Log.e("initConfig,", "packlistPath:" + packlistPath + "parser:" + parser);*/
            //if (parser != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Looper.prepare();
                               /* parser.next();
                                List<String> list = new ArrayList<>();
                                boolean inTitle = false;
                                int eventType = parser.getEventType();
                                while (eventType != XmlPullParser.END_DOCUMENT) {
                                    if (eventType == XmlPullParser.START_TAG) {
                                        if (parser.getName().equals("item")) {
                                            inTitle = true;
                                        }
                                    }
                                    if (eventType == XmlPullParser.TEXT && inTitle) {
                                        list.add(parser.getText());
                                        inTitle = false;
                                    }
                                    parser.next();
                                    eventType = parser.getEventType();
                                }*/
                        /**
                         *   <item>com.tencent.mobileqq</item>
                         <item>com.tencent.mm</item>
                         <item>com.skype.raider</item>
                         <item>com.skype.polaris</item>
                         <item>com.skype.rover</item>
                         <item>com.facebook.katana</item>
                         <item>com.twitter.android</item>
                         <item>com.linkedin.android</item>
                         <item>com.instagram.android</item>
                         <item>life.inovatyon</item>
                         <item>com.whatsapp</item>
                         <item>com.facebook.orca</item>
                         */
                        List<String> list = new ArrayList<>();
                        list.add("com.tencent.mobileqq");
                        list.add("com.tencent.mm");
                        list.add("com.skype.raider");
                        list.add("com.skype.polaris");
                        list.add("om.skype.rover");
                        list.add("com.facebook.katana");
                        list.add("com.twitter.android");
                        list.add("com.linkedin.android");
                        list.add("com.instagram.android");
                        list.add("life.inovatyon");
                        list.add("com.whatsapp");
                        list.add("com.facebook.orca");
                        pkNames = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            pkNames[i] = list.get(i);
                            //  Log.e("initConfig", "pkNames--------------" + pkNames[i]);
                        }

                        if (list.size() > 0) {
                            List<String> listPkName = new ArrayList<>();
                            listPkName.addAll(list);
                            // Log.e("initConfig", "listPkName--------------" + listPkName.size() + "strPkNames.length---------" + strPkNames.length);
                            notiType = new HashMap<>();
                            if (listPkName.size() > 0) {
                                for (int i = 0; i < strPkNames.length; i++) {


                                    if ((i + 0x13) > 0x2B) {
                                        break;
                                    }

                                    String[] tpStrs = strPkNames[i];
                                    // Log.e("initConfig", "tpStrs--------------" + tpStrs.length);

                                    for (int j = 0; j < tpStrs.length; j++) {
                                        // Log.e("initConfig", "pkNames" + tpStrs[j]);
                                        if (listPkName.contains(tpStrs[j])) {
                                            //  Log.e("initConfig", "pkNames" + tpStrs[j] + "values:" + (i + 0x13));
                                            notiType.put(tpStrs[j], (i + 0x13));
                                        }
                                    }

                                }
                            }
                        }
                        Looper.loop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
