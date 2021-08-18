package com.isport.isportlibrary.tools;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.controller.IBleCmdCallback;
import com.isport.isportlibrary.database.DbHeartRateHistory;
import com.isport.isportlibrary.database.DbHistorySport;
import com.isport.isportlibrary.database.DbHistorySportN;
import com.isport.isportlibrary.database.DbRealTimePedo;
import com.isport.isportlibrary.database.DbSprotDayData;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.HeartData;
import com.isport.isportlibrary.entry.HeartRateData;
import com.isport.isportlibrary.entry.HeartRateHistory;
import com.isport.isportlibrary.entry.HeartRecord;
import com.isport.isportlibrary.entry.HistorySport;
import com.isport.isportlibrary.entry.HistorySportN;
import com.isport.isportlibrary.entry.PedoRealData;
import com.isport.isportlibrary.entry.SportData337B;
import com.isport.isportlibrary.entry.SportDayData;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

/**
 * @author Created by Marcos Cheng on 2016/9/21.
 * 数据解析类
 */
public class ParserData {
    private final static String TAG = ParserData.class.getSimpleName();


    //private static DbUtils dbUtils;
    public static int byteArrayToInt(byte[] data) {
        int value = 0;
        if (data != null && data.length > 0) {
            int len = data.length - 1;
            for (int i = len; i >= 0; i--) {
                value += ((data[len - i] & 0x00ff) << (8 * i));
            }
        }
        return value;
    }

    public static int byteToInt(byte data) {
        return (data & 0x00ff);
    }

    public static void processRealTimeData(Context context, String mac, IBleCmdCallback callback, byte[] data) {
        Calendar calendar = Calendar.getInstance();
        int totalStep = byteArrayToInt(new byte[]{data[8], data[9], data[10], data[11]});
        int totalCaloric = byteArrayToInt(new byte[]{data[12], data[13], data[14], data[15]});
        float totalDist = byteArrayToInt(new byte[]{data[16], data[17]}) / 100.0f;
        int totalSportTime = byteArrayToInt(new byte[]{data[18], data[19]});
        String dataString = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
        if (IS_DEBUG)
            Log.e(TAG, dataString + "---实时数据返回----" + totalStep);
        SportDayData dayData = new SportDayData(mac, dataString, totalStep, totalDist, totalCaloric, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, totalSportTime, 0);
        PedoRealData pedoRealData = new PedoRealData(dataString, mac, totalStep, totalCaloric, totalDist);
        DbRealTimePedo.getInstance(context).saveOrUpdate(pedoRealData);
        if (callback != null) {
            callback.realTimeDayData(dayData);
        }
        Intent intent = new Intent(BaseController.ACTION_REAL_DATA);
        intent.putExtra(BaseController.EXTRA_REAL_CALORIC, totalCaloric);
        intent.putExtra(BaseController.EXTRA_REAL_DIST, totalDist);
        intent.putExtra(BaseController.EXTRA_REAL_STEPS, totalStep);
        intent.putExtra(BaseController.EXTRA_REAL_SPORTTIME, totalSportTime);
        intent.putExtra(BaseController.EXTRA_REAL_DATE, dataString);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void processHeartRateHistoryData(boolean is288, Context context, String mac, List<byte[]> list, Handler commandHandler) {
        // List<byte[]> data = list;
        List<byte[]> data = new ArrayList<>();
        data.addAll(list);
        if (list != null && list.size() >= 1) {
            byte[] startBytes = data.get(0);
//        DE-02-10-FE-YY-MM-DD-总数(1bytes)-心率数据1-心率数据2-...心率数据48（历史数据为48个，当天按实际数据）
            int year = startBytes[4] * 256 + (startBytes[5] & 0x00ff);
            int mon = startBytes[6];
            int day = startBytes[7];
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, mon - 1, day, 0, 0, 0);
            String dateStr = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
            int dataCount;
            if (is288) {
                dataCount = byteArrayToInt(new byte[]{startBytes[8], startBytes[9]});//有效数据的长度
            } else {
                dataCount = byteArrayToInt(new byte[]{startBytes[8]});//有效数据的长度
            }
            if (data != null) {
                Log.e("CmdController", dataCount + "---" + "is288" + is288 + "data.size" + data.size());
            }
            ArrayList<HeartRateData> heartDataList = new ArrayList<>();//所有数据数组
            ArrayList<Integer> dataList = new ArrayList<>();//有效数据数组
            int totalHeart = 0;
            int minHeart = 0;
            int maxHeart = 0;
            HeartRateHistory listHeartRecord = new HeartRateHistory();
            listHeartRecord.setStartDate(dateStr);
            listHeartRecord.setMac(mac);
            listHeartRecord.setCount(dataCount);

            //*****************************更简便的方式获取数据**************************************//
            byte[] resultBytes = new byte[dataCount];
            try {
                for (int i = 0; i < data.size(); i++) {
                    byte[] tmp = (byte[]) data.get(i);
                    if (tmp != null) {
                        if (i == 0) {
                            if (data.size() == 1) {
                                //只有一包的情况
                                if (is288) {
                                    System.arraycopy(tmp, 10, resultBytes, 0, dataCount);
                                } else {
                                    System.arraycopy(tmp, 9, resultBytes, 0, dataCount);
                                }
                            } else {
                                //有多包的情况
                                if (is288) {
                                    System.arraycopy(tmp, 10, resultBytes, 0, 10);
                                } else {
                                    System.arraycopy(tmp, 9, resultBytes, 0, 11);
                                }
                            }
                        } else {
                            //第N包 N>=2
                            int formIndex;
                            if (is288) {
                                formIndex = 10 + (i - 1) * 20;
                            } else {
                                formIndex = 11 + (i - 1) * 20;
                            }
                            if (i == data.size() - 1) {
                                //最后一包的情况
                                System.arraycopy(tmp, 0, resultBytes, formIndex, dataCount - (formIndex));
                            } else {
                                //中间包
                                System.arraycopy(tmp, 0, resultBytes, formIndex, 20);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                for (int i = 0; i < resultBytes.length; i++) {
                    //确定最大值，最小值，平均值，所有数据
                    int rate = resultBytes[i] & 0xff;
                    if (rate != 0) {///过滤掉心率为0
                        totalHeart += rate;
                        if (minHeart == 0) {
                            minHeart = rate;
                        } else {
                            minHeart = minHeart < rate ? minHeart : rate;
                        }
                        maxHeart = maxHeart < rate ? rate : maxHeart;
                        dataList.add(rate);
                    }
                    //为0的数据也加入
                    heartDataList.add(new HeartRateData(rate, i));
                }
                listHeartRecord.setAvg(dataList.size() <= 0 ? 0 : (int) (totalHeart / (dataList.size() * 1.0)));
                listHeartRecord.setMax(maxHeart);
                listHeartRecord.setMin(minHeart);
                listHeartRecord.setHeartDataList(heartDataList);
                DbHeartRateHistory.getIntance(context).saveOrUpdate(listHeartRecord);
                if (IS_DEBUG)
                    Log.e(TAG, "存储心率数据,并查询");

                List<HeartRateHistory> listHistory = DbHeartRateHistory.getIntance(context).getListHistory(DbHeartRateHistory
                                .COLUMN_MAC + "=?", new String[]{mac},
                        null, null,
                        "datetime(" +
                                DbHeartRateHistory.COLUMN_DATE +
                                ") DESC");
                if (listHistory != null && listHistory.size() > 0) {
                    for (int i = 0; i < listHistory.size(); i++) {
                        Log.e(TAG, listHistory.get(i).toString());
                    }
                }

                Message msgTp = Message.obtain();
                msgTp.what = 0x21;
                //需要把时间也传递出去

                int[] tpi = new int[]{year, mon,
                        day};
                msgTp.obj = tpi;
                commandHandler.sendMessageDelayed(msgTp, 300);
            }


            //**********************************笨方式*******************************//
//            byte[] resultBytes = new byte[dataCount];
//            //当天数据无数据不会补FF,历史数据是全的
//            //当天数据要根据数据长度来判断有多少数据，有多少包
//            if (dataCount <= 11) {
//                //一包的情况
//                System.arraycopy(startBytes, 9, resultBytes, 0, dataCount);
//                if (IS_DEBUG)
//                    Log.e(TAG, "---一包的情况----");
//            } else if (dataCount > 11 && dataCount <= 31) {
//                //两包的情况
//                byte[] middleBytes = data.get(1);
//                //获取前48个数据
//                System.arraycopy(startBytes, 9, resultBytes, 0, 11);
//                System.arraycopy(middleBytes, 0, resultBytes, 11, dataCount - 11);
//                Log.e(TAG, "---两包的情况----");
//            } else if (dataCount > 31 && dataCount <= 51) {
//                //三包的情况
//                byte[] middleBytes = data.get(1);
//                byte[] endBytes = data.get(2);
//                //获取前48个数据
//                System.arraycopy(startBytes, 9, resultBytes, 0, 11);
//                System.arraycopy(middleBytes, 0, resultBytes, 11, 20);
//                System.arraycopy(endBytes, 0, resultBytes, 31, dataCount - 31);
//                Log.e(TAG, "---三包的情况----");
//            } else if (dataCount > 51 && dataCount <= 71) {
//                //四包的情况
//                byte[] middleBytes = data.get(1);
//                byte[] middleBytes1 = data.get(2);
//                byte[] endBytes = data.get(3);
//                //获取前48个数据
//                System.arraycopy(startBytes, 9, resultBytes, 0, 11);
//                System.arraycopy(middleBytes, 0, resultBytes, 11, 20);
//                System.arraycopy(middleBytes1, 0, resultBytes, 31, 20);
//                System.arraycopy(endBytes, 0, resultBytes, 51, dataCount - 51);
//                Log.e(TAG, "---四包的情况----");
//            } else if (dataCount > 71 && dataCount <= 91) {
//                //五包的情况
//                byte[] middleBytes = data.get(1);
//                byte[] middleBytes1 = data.get(2);
//                byte[] middleBytes2 = data.get(3);
//                byte[] endBytes = data.get(4);
//                //获取前48个数据
//                System.arraycopy(startBytes, 9, resultBytes, 0, 11);
//                System.arraycopy(middleBytes, 0, resultBytes, 11, 20);
//                System.arraycopy(middleBytes1, 0, resultBytes, 31, 20);
//                System.arraycopy(middleBytes2, 0, resultBytes, 51, 20);
//                System.arraycopy(endBytes, 0, resultBytes, 71, dataCount - 71);
//                Log.e(TAG, "---五包的情况----");
//            } else {
//                //六包的情况
//                byte[] middleBytes = data.get(1);
//                byte[] middleBytes1 = data.get(2);
//                byte[] middleBytes2 = data.get(3);
//                byte[] middleBytes3 = data.get(4);
//                byte[] endBytes = data.get(5);
//                //获取前48个数据
//                System.arraycopy(startBytes, 9, resultBytes, 0, 11);
//                System.arraycopy(middleBytes, 0, resultBytes, 11, 20);
//                System.arraycopy(middleBytes1, 0, resultBytes, 31, 20);
//                System.arraycopy(middleBytes2, 0, resultBytes, 51, 20);
//                System.arraycopy(middleBytes3, 0, resultBytes, 71, 20);
//                System.arraycopy(endBytes, 0, resultBytes, 91, dataCount - 91);
//            }


        }

    }

    /**
     * 删除beat 91.61以及以上版本数据解析
     * 91.70
     *
     * @param mac
     * @param callback
     * @param tp
     * @param cmdController
     */
    public static void processBeatData(Context context, String mac, IBleCmdCallback callback, List<byte[]> tp, CmdController cmdController) {
        List<byte[]> list = new ArrayList<>();
        list.addAll(tp);
        if (list != null && list.size() >= 1) {
            Log.e("DbHistorySportN 数据size", tp.size() + "");
//            DE+02+01+FE+日历年(2Byte)+日历月(1Byte)+日历日(1Byte)+
//                    该日 累计总步数（4Byte)+该日累计总卡路里(4Byte)+该日累计总运动时间 (2Byte)
            byte[] data = (byte[]) list.get(0);
            int year = byteArrayToInt(new byte[]{data[4], data[5]});
            int mon = byteArrayToInt(new byte[]{data[6]});
            int day = byteArrayToInt(new byte[]{data[7]});
            int totalStep = byteArrayToInt(new byte[]{data[8], data[9], data[10], data[11]});
            int totalCaloric = byteArrayToInt(new byte[]{data[12], data[13], data[14], data[15]});
            float totalDist = byteArrayToInt(new byte[]{data[16], data[17]}) / 100.0f;
            int totalSportTime = byteArrayToInt(new byte[]{data[18], data[19]});
            String dataString = String.format("%04d", year) + "-" + String.format("%02d", mon) + "-" + String.format
                    ("%02d", day);
            SportDayData dayData = new SportDayData(mac, dataString, totalStep, totalDist, totalCaloric, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0,
                    totalSportTime, 0);
            // TODO: 2019/4/19 距离客户自己算
            PedoRealData pedoRealData = new PedoRealData(dataString, mac, totalStep, totalCaloric, totalDist);
            DbRealTimePedo.getInstance(context).saveOrUpdate(pedoRealData);
            DbSprotDayData.getInstance(context).saveOrUpdate(dayData);


            for (int i = 0; i < list.size(); i++) {
                StringBuilder stringBuilder = new StringBuilder();
                byte[] bytes = list.get(i);
                for (byte byteChar : bytes) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.e("DbHistorySportN", dataString + " == " + stringBuilder);
            }
            //数据解析
            data = new byte[(list.size() - 1) * 19];//-2是减去2byte的总睡眠时间
            for (int i = 1; i < list.size(); i++) {
                byte[] tmp = (byte[]) list.get(i);
                if (tmp != null) {
                    System.arraycopy(tmp, 1, data, (i - 1) * 19, 19);
                }
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                builder.append(String.format("%02X ", data[i]));
            }
            Log.e("DbHistorySportN 数据 == ", builder.toString());


            List histList = new ArrayList();
//            DbHistorySportN.getInstance(context).beginTransaction();

            boolean haslPak = false;
            //判断是否是当天数据
            Calendar instance = Calendar.getInstance();
            if (year == instance.get(Calendar.YEAR) && mon == instance.get
                    (Calendar.MONTH) + 1 && day == instance.get(Calendar.DAY_OF_MONTH)) {
                //当天数据
                //通过获取当前时间来判断包数，12:32  (12*60+32)/19   39.57  40包数据
                int hourOfDay = instance.get(Calendar.HOUR_OF_DAY);//当前的小时
                //instance.add(Calendar.MINUTE, -1);
                // int minuteOfDay = instance.get(Calendar.MINUTE);//当前分钟
              /*  float floatF = (hourOfDay * 60 + minuteOfDay) / (float) 19;
                int intI = (hourOfDay * 60 + minuteOfDay) / 19;*/

                int minuteOfDay = instance.get(Calendar.MINUTE) - 1;//当前分钟
                //时间长度   当天的数据
                int pakNum = hourOfDay * 60 + minuteOfDay;
               /* int pakNum;
                if ((floatF - intI) > 0) {
                    pakNum = intI + 1;
                } else {
                    pakNum = intI;
                }*/
                //-2是减去2byte的总睡眠时间
                if (data.length - 2 >= pakNum || data.length - 2 >= pakNum) {
//                        Logger.myLog("pakNum == " + pakNum * 19 + "当天数据未丢包 data.length == " + data.length);
                } else {
                    haslPak = true;
                    if (callback != null) {
                        callback.syncState(BaseController.STATE_SYNC_ERROR);
                    }
//                        Logger.myLog("pakNum == " + pakNum * 19 + "当天数据丢包 data.length == " + data.length);
                }
            } else {
                //历史数据
                //判断是否丢包,应该有152包数据  1440*2/19  151.57 除去了index位
                //-2是减去2byte的总睡眠时间
                if (data.length - 2 >= 1440) {
                    //没有丢包
//                        Logger.myLog("历史数据未丢包 == " + data.length);

                } else {
                    //丢包
                    haslPak = true;
                    Log.e("DbHistorySportN", "丢包了");
                    if (callback != null) {
                        callback.syncState(BaseController.STATE_SYNC_ERROR);
                    }
//                        Logger.myLog("历史数据丢包 == " + data.length);
                }
            }

            if (!haslPak) {
                for (int i = 0; i < data.length; i++) {
                    //最大不超过1440个数据
                    if (i == 1442) {
                        break;
                    }
                    //最前面2byte是睡眠总时间
                    if (i == 0 || i == 1) {
                        continue;
                    }
                    HistorySportN historySport = new HistorySportN();
                    historySport.setDateString(dataString + " " + DateUtil.getFormatTimemmss(i - 1));
                    historySport.setMac(mac);
                    historySport.setIndex(i - 1);
                    byte byte0 = data[i];
                    //当天可能出现1197的情况
                    int int0 = Utils.byte2Int(byte0);
                    //不再补FF，硬件因为补FF出现index0为FF情况
//                    if (int0 == 255) {
//                        //结尾包了
////                            Logger.myLog("结尾包了 == " + i);
//                        break;
//                    }
                    /**
                     * 计步或者睡眠
                     */
                    if (int0 >= 250) {
                        //为睡眠数据
                        if (int0 == 250) {
                            //清醒
//                                Logger.myLog("深睡");
                            historySport.setSleepState(128);
                        } else if (int0 == 251) {
                            //及浅睡 level 2
//                                Logger.myLog("浅睡 level 2");
                            historySport.setSleepState(129);
                        } else if (int0 == 252) {
                            //浅睡 level 1
//                                Logger.myLog("浅睡 level 1");
                            historySport.setSleepState(130);
                        } else if (int0 == 253) {
                            //深睡
//                                Logger.myLog("清醒");
                            historySport.setSleepState(131);
                        } else {
                            historySport.setSleepState(0);
                        }
                        historySport.setStepNum(0);
                    } else {
                        //步数数据
//                            Logger.myLog("步数数据 == " + int0);
                        historySport.setSleepState(0);
                        historySport.setStepNum(int0);
                    }
                    historySport.setHeartRate(0);
                    histList.add(historySport);
                }
            } else {
                Log.e("DbHistorySportN", "丢包了");
                //丢包了
                if (callback != null) {
                    callback.syncState(BaseController.STATE_SYNC_ERROR);
                }
            }
//            sleepState == 128   深睡
//            sleepState == 129   浅睡
//            sleepState == 130   极浅睡
//            sleepState == 131   醒

//            DbHistorySportN.getInstance(context).setTransactionSuccessful();

            if (histList != null && histList.size() > 0) {
                for (int i = 0; i < histList.size(); i++) {
                    // cmdController.logBuilder.append(histList.get(i).toString() + "\r\n");
//                    Log.e("DbHistorySportN", histList.get(i).toString());
                }
                //在这儿处理是最好的，对查询的所有数据遍历
//                    BaseController.saveLog(new StringBuilder(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss ")
// +" 历史统计总数: "+ totalStepLog+"\r\n"));
                DbHistorySportN.getInstance(context).saveOrUpdate(histList);
            }
        } else {
            Log.e("DbHistorySportN", "list为空或者size<=0");
        }
    }

    public static void processData(Context context, String mac, IBleCmdCallback callback, List<byte[]> tp, int dtype) {
        // TODO: 2018/3/17 针对301H 307H由于数据存储是replace的，所以可以在处理历史数据时，在产生第一个睡眠数据时操作前8个数据未固定值
        //而对于实时产生的数据，将如何处理呢？
        /*List list = new ArrayList();
        if(tp != null){
            list.addAll(tp);
        }*/

        try {
            if (tp == null || tp.size() == 0) {
            }
            List<byte[]> list = tp;

            if (list != null && list.size() >= 3) {
                byte[] data = (byte[]) list.get(0);
                int year = byteArrayToInt(new byte[]{data[4], data[5]});
                int mon = byteArrayToInt(new byte[]{data[6]});
                int day = byteArrayToInt(new byte[]{data[7]});
                int totalStep = byteArrayToInt(new byte[]{data[8], data[9], data[10], data[11]});
                if (IS_DEBUG)
                    Log.e(TAG, year + "-" + mon + "-" + day + "***总数***" + totalStep);
//            BaseController.saveLog(new StringBuilder(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + "
// 第一包返回总数: "+totalStep +"\r\n"));
                int totalCaloric = byteArrayToInt(new byte[]{data[12], data[13], data[14], data[15]});
                float totalDist = byteArrayToInt(new byte[]{data[16], data[17]}) / 100.0f;
                int totalSportTime = byteArrayToInt(new byte[]{data[18], data[19]});
                data = (byte[]) list.get(1);
                int totalSleepTime = byteArrayToInt(new byte[]{data[0], data[1]});
                int totalStillTime = byteArrayToInt(new byte[]{data[2], data[3]});
                int walkTime = byteArrayToInt(new byte[]{data[4], data[5]});
                int lowSpeedWalkTime = byteArrayToInt(new byte[]{data[6], data[7]});
                int midSpeedWalkTime = byteArrayToInt(new byte[]{data[8], data[9]});
                int larSpeedWalkTime = byteArrayToInt(new byte[]{data[10], data[11]});
                int lowSpeedRunTime = byteArrayToInt(new byte[]{data[12], data[13]});
                int midSpeedRunTime = byteArrayToInt(new byte[]{data[14], data[15]});
                int larSpeedRunTime = byteArrayToInt(new byte[]{data[16], data[17]});
                data = (byte[]) list.get(2);
                float strideLen = byteArrayToInt(new byte[]{data[0], data[1]}) / 100.0f;
                float weight = byteArrayToInt(new byte[]{data[2], data[3]}) / 100.0f;
                int targetStep = byteArrayToInt(new byte[]{data[4], data[5], data[6]});
                int targetSleepTime = byteArrayToInt(new byte[]{data[7], data[8]});
                String dataString = String.format("%04d", year) + "-" + String.format("%02d", mon) + "-" + String.format
                        ("%02d", day);

                SportDayData dayData = new SportDayData(mac, dataString, totalStep, totalDist, totalCaloric, targetStep,
                        strideLen, weight, totalSleepTime, totalStillTime,
                        walkTime, lowSpeedWalkTime, midSpeedWalkTime, larSpeedWalkTime,
                        lowSpeedRunTime, midSpeedRunTime, larSpeedRunTime,
                        totalSportTime, targetSleepTime);

                PedoRealData pedoRealData = new PedoRealData(dataString, mac, totalStep, totalCaloric, totalDist);
                if (IS_DEBUG)
                    Log.e(TAG, dataString + "---同步时更新实时----" + totalStep);
                DbRealTimePedo.getInstance(context).saveOrUpdate(pedoRealData);
                DbSprotDayData.getInstance(context).saveOrUpdate(dayData);

                int totalByteCount = byteArrayToInt(new byte[]{data[9], data[10]});
                if (IS_DEBUG)
                    Log.e(TAG, "---totalByteCount----" + totalByteCount);
                data = new byte[(list.size() - 3) * 20];
                for (int i = 3; i < list.size(); i++) {
                    byte[] tmp = (byte[]) list.get(i);
                    if (tmp != null ) {
                        System.arraycopy(tmp, 0, data, (i - 3) * 20, 20);
                    }
                }
                List histList = new ArrayList();
                /**
                 * jiao yan
                 */
                if (IS_DEBUG)
                    Log.e(TAG, "---totalByteCount----" + totalByteCount + "----data.length---" + data.length);
                if (totalByteCount > data.length) {
                    if (callback != null) {
                        callback.syncState(BaseController.STATE_SYNC_ERROR);
                    }
                } else {
                    int totalStepLog = 0;
                    int length = data.length;
                    Calendar calendar = Calendar.getInstance();
                    String todayStr = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
                    calendar.set(year, mon - 1, day, 0, 0, 0);
                    String dateHist = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
                    int i = 0;
                    int index = 0;
                    int lastFlag = -1;
                    DbHistorySport.getInstance(context).beginTransaction();
                    boolean hasData = false;
                    boolean has12Data = false;
                    try {
                        boolean isToday = todayStr.equals(dateHist);///
                        int mmmm = 0;
                        int nnnn = 0;
                        while (i < totalByteCount) {
                            nnnn++;
                            if (isToday) {
                                if (i + 2 >= totalByteCount)
                                    break;
                            } else {
                                /*修復最後一個五分鐘數據是睡眠數據不解析的情況*/
                                if (i + 1 >= totalByteCount)
                                    break;
                                int tttttpflag = byteToInt(data[i + 1]) & 0x0080;
                                if (tttttpflag == 0x80) {
                                    if (i + 1 >= totalByteCount)
                                        break;
                                } else {
                                    if (i + 2 >= totalByteCount)
                                        break;
                                }
                            }
                            mmmm++;

                            int tIndex = byteToInt(data[i]);//当前的序号
                            int dv = 1;
                            if (tIndex - index > 0) {
                                dv = tIndex - index;
                            } else if (tIndex - index < 0) {
                                dv = byteToInt((byte) 0xff) - index + tIndex;
                            }
                            int flag = byteToInt(data[i + 1]) & 0x0080;
                            HistorySport sport = null;
                            if (IS_DEBUG)
                                Log.e(TAG, "---flag----" + flag);
                            if (flag == 0x80) {//sleep data
                                byte[] tdp = {data[i + 1]};
                                int sleepState = byteToInt(tdp[0]);
                                Log.e(TAG, sleepState + "***进了888888888***sleepState" + sleepState + "dv:" + dv);
                                for (int j = 1; j <= dv; j++) {
                                    String dateString = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd HH:mm");
                                    if (IS_DEBUG)
                                        Log.e(TAG, sleepState + "***进了888888888***" + dateString);
                                    String detail = DateUtil.dataToString(calendar.getTime(), "HH:mm");
                                    if (IS_DEBUG)
                                        Log.e(TAG, dateString + "***当前时间***" + detail);
                                    // TODO: 2018/3/17 第一确认是否超过12点，超过12点才来判断是否产生了第一个睡眠数据，如果有产生第一个睡眠数据，就向前补全40分钟的数据
                                    //怎么替换前40分钟的数据，而且当天的睡眠总时间是否要加上
                 /*               if (dtype == BaseDevice.TYPE_W307H || dtype == BaseDevice.TYPE_W301H) {
                                    if (!hasData) {
                                        if (Integer.parseInt(detail.split(":")[0]) >= 0 && Integer.parseInt(detail
                                        .split(":")[0]) <= 11) {
                                            //超过24点，有睡眠数据产生,补全前40分钟的空白数据，包括昨天晚上的数据也要覆盖了，
                                            if (sleepState == 128 || sleepState == 129 || sleepState == 130 ||
                                                    sleepState == 131) {
                                                hasData = true;
                                                // TODO: 2018/3/19 要计算前40分钟的情况,如果是昨天数据，要查询昨天数据并填补上
                                                if ((Integer.parseInt(detail.split(":")[0]) == 0 && Integer.parseInt
                                                        (detail.split(":")[1]) >= 40) || Integer.parseInt(detail
                                                        .split(":")[0]) >= 1) {
                                                    //第一种情况，当>=00:40时补当天的数据
                                                    //暂时排除午睡的情况
                                                    //当返回的第一个睡眠数据是12:40数据
                                                    //向前推40分钟数据分别为12:00 12:05 12:10 12:15 12:20 12:25 12:30 12:35
                                                    List<String> last40MintueTime = DateUtil.getLast40MintueTime
                                                            (detail);
                                                    //需要替换当天的前40分钟数据  histList
                                                    //获取当前list的前8个数据，remove再做添加
                                                    int size = histList.size();
                                                    Log.e(TAG, (size - 8) + "***SIZE***" + size);
                                                    for (int m = size - 8; m < size; m++) {
                                                        HistorySport historySport = (HistorySport) histList.get(m);
                                                        int mSleepSate = 0;
                                                        if (m == size - 8) {
                                                            mSleepSate = 131;
                                                        } else if (m == size - 7) {
                                                            mSleepSate = 129;
                                                        } else if (m == size - 6) {
                                                            mSleepSate = 129;
                                                        } else if (m == size - 5) {
                                                            mSleepSate = 129;
                                                        } else if (m == size - 4) {
                                                            mSleepSate = 129;
                                                        } else if (m == size - 3) {
                                                            mSleepSate = 129;
                                                        } else if (m == size - 2) {
                                                            mSleepSate = 129;
                                                        } else if (m == size - 1) {
                                                            mSleepSate = 128;
                                                        }
                                                        HistorySport mHistorySport = new HistorySport(mac, historySport
                                                                .getDateString(), historySport.getStepNum(),
                                                                                                      mSleepSate);
                                                        Log.e(TAG, "***产生第一个睡眠数据，补全40分钟***Date***" + historySport
                                                                .getDateString() + "***StepNum***" + historySport
                                                                .getStepNum
                                                                        () + "***SleepStae***" + mSleepSate);
                                                        histList.set(m, mHistorySport);
                                                        Log.e(TAG, "***再次查询获取结果***" + ((HistorySport) histList.get(m))
                                                                .getDateString());
                                                    }
                                                } else {
                                                    //当产生的数据在00:00-00:35之间时，会产生要补昨天情况，要先查询昨天睡眠数据，如果昨天12点后有睡眠数据，那么就不补，因为昨天已经补过了
                                                    String sql = DbHistorySport.COLUMN_DATE + " >= ? and " +
                                                            DbHistorySport.COLUMN_MAC + "=?";
                                                    Calendar curCalendar = Calendar.getInstance();
                                                    curCalendar.add(Calendar.DAY_OF_YEAR, -1);
                                                    String lastDateString = DateUtil.dataToString(curCalendar.getTime
                                                            (), "yyyy-MM-dd");
                                                    List<HistorySport> tpH = DbHistorySport.getInstance(context)
                                                            .findAll(sql, new String[]{lastDateString + " 12:00", mac},
                                                                     "datetime(" + DbHistorySport.COLUMN_DATE + ") " +
                                                                             "ASC");
                                                    if (tpH != null && tpH.size() > 0) {
                                                        //昨天有数据，说明补过了，要其中有睡眠数据才算补过了
                                                        boolean hasSleepData = false;
                                                        for (int m = 0; m < tpH.size(); m++) {
                                                            if (tpH.get(m).getSleepState() > 0) {
                                                                hasSleepData = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!hasSleepData) {
                                                            //没有睡眠数据，说明没有补过，要补
                                                            //查看需要补多少
                                                            getLastDayNeedData(context, tpH, histList, mac);
                                                            getCurrentDayNeedData(histList, mac);
                                                        }
                                                    } else {
                                                        //昨天无数据（昨天没有同步数据呢？昨天是第一天），需要补数据

                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!has12Data) {
                                        if (Integer.parseInt(detail.split(":")[0]) >= 12) {
                                            has12Data = true;
                                            if (sleepState == 128 || sleepState == 129 || sleepState == 130 ||
                                                    sleepState == 131) {
                                                hasData = true;
                                                //暂时排除午睡的情况
                                                //当返回的第一个睡眠数据是12:40数据
                                                //向前推40分钟数据分别为12:00 12:05 12:10 12:15 12:20 12:25 12:30 12:35
                                                List<String> last40MintueTime = DateUtil.getLast40MintueTime(detail);
                                                //需要替换当天的前40分钟数据  histList
                                                //获取当前list的前8个数据，remove再做添加
                                                int size = histList.size();
                                                Log.e(TAG, (size - 8) + "***SIZE***" + size);
                                                for (int m = size - 8; m < size; m++) {
                                                    HistorySport historySport = (HistorySport) histList.get(m);
                                                    int mSleepSate = 0;
                                                    if (m == size - 8) {
                                                        mSleepSate = 131;
                                                    } else if (m == size - 7) {
                                                        mSleepSate = 129;
                                                    } else if (m == size - 6) {
                                                        mSleepSate = 129;
                                                    } else if (m == size - 5) {
                                                        mSleepSate = 129;
                                                    } else if (m == size - 4) {
                                                        mSleepSate = 129;
                                                    } else if (m == size - 3) {
                                                        mSleepSate = 129;
                                                    } else if (m == size - 2) {
                                                        mSleepSate = 129;
                                                    } else if (m == size - 1) {
                                                        mSleepSate = 128;
                                                    }
                                                    HistorySport mHistorySport = new HistorySport(mac, historySport
                                                            .getDateString(), historySport.getStepNum(), mSleepSate);
                                                    Log.e(TAG, "***产生第一个睡眠数据，补全40分钟***Date***" + historySport
                                                            .getDateString() + "***StepNum***" + historySport.getStepNum
                                                            () + "***SleepStae***" + mSleepSate);
                                                    histList.set(m, mHistorySport);
                                                    Log.e(TAG, "***再次查询获取结果***" + ((HistorySport) histList.get(m))
                                                            .getDateString());
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "***进了777777777777***");
                                }*/
                                    HistorySport historySport = DbHistorySport.getInstance(context).
                                            findFirst(DbHistorySport.COLUMN_DATE + "=? and " + DbHistorySport.COLUMN_MAC
                                                    + "=?", new String[]{dateString, mac});
                                    if (historySport != null) {
                                        historySport.setSleepState(sleepState);
                                        historySport.setStepNum(0);
                                        historySport.setMac(mac);
                                    } else {
                                        historySport = new HistorySport(mac, dateString, 0, sleepState);
                                    }
                                    histList.add(historySport);
                                    Log.e("sleepdata", "datestring = " + dateString + " , state = " + sleepState + "historySport:" + historySport);
                                    calendar.add(Calendar.MINUTE, 5);
                                }

                                if (isToday) {
                                    i = i + 3;
                                } else {
                                    i = i + 2;
                                }
                            } else if (flag == 0) {//step data
                                byte[] tdp = {data[i + 1], data[i + 2]};//
                                int step = byteArrayToInt(tdp);
                                for (int j = 1; j <= dv; j++) {
                                    String dateString = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd HH:mm");
                                    HistorySport historySport = DbHistorySport.getInstance(context).
                                            findFirst(DbHistorySport.COLUMN_DATE + "=? and " + DbHistorySport.COLUMN_MAC
                                                    + "=?", new String[]{dateString, mac});
                                    if (IS_DEBUG)
                                        // Log.e(TAG, dateString + "***step***" + step);
                                        if (historySport != null) {
                                            historySport.setStepNum(step);
                                            historySport.setSleepState(0);
                                            historySport.setMac(mac);
                                            histList.add(historySport);
                                        } else {
                                            histList.add(new HistorySport(mac, dateString, step, 0));
                                        }
                                    totalStepLog += step;
//                                BaseController.saveLog(new StringBuilder(DateUtil.dataToString(new Date(), "MM/dd
// HH:mm:ss ") + dateString+" 步数: "+step +"\r\n"));
                                    //Log.e("sleepdata","datestring = "+dateString+" , state = "+step);
                                    calendar.add(Calendar.MINUTE, 5);
                                }
                                i = i + 3;
                            }
                            index = tIndex;
                            lastFlag = flag;
                        }
                        DbHistorySport.getInstance(context).setTransactionSuccessful();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        DbHistorySport.getInstance(context).endTransaction();
                    }
                    if (histList != null && histList.size() > 0) {
                        //在这儿处理是最好的，对查询的所有数据遍历
//                    BaseController.saveLog(new StringBuilder(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss ")
// +" 历史统计总数: "+ totalStepLog+"\r\n"));
                        DbHistorySport.getInstance(context).saveOrUpdate(histList);
                    }
                }
            } else {
            }
        }catch (Exception e){

        }



    }

    private static void getCurrentDayNeedData(List histList, String mac) {
        int size = histList.size();
        for (int m = size - 1; m >= 0; m--) {
            HistorySport historySport = (HistorySport) histList.get(m);
            int mSleepSate = 0;
            if (m == size - 1) {
                mSleepSate = 128;
            } else {
                mSleepSate = 129;
            }
            HistorySport mHistorySport = new HistorySport(mac, historySport
                    .getDateString(), historySport.getStepNum(),
                    mSleepSate);
            Log.e(TAG, "***产生第一个睡眠数据，补全40分钟***Date***" + historySport
                    .getDateString() + "***StepNum***" + historySport
                    .getStepNum
                            () + "***SleepStae***" + mSleepSate);
            histList.set(m, mHistorySport);
            Log.e(TAG, "***再次查询获取结果***" + ((HistorySport) histList.get(m))
                    .getDateString());
        }
    }

    private static void getLastDayNeedData(Context context, List tpH, List histList, String mac) {
        int size = histList.size();//今天现有的个数
        //要补全8-size数据
        int index;
        for (int m = tpH.size() - (8 - size); m < tpH.size(); m++) {
            HistorySport historySport = (HistorySport) tpH.get(m);
            int mSleepSate = 0;
            if (m == tpH.size() - (8 - size)) {
                mSleepSate = 131;
            } else {
                if (size == 8) {
                    if (m == tpH.size() - 1) {
                        mSleepSate = 128;
                    } else {
                        mSleepSate = 129;
                    }
                } else {
                    mSleepSate = 129;
                }
            }
            HistorySport mHistorySport = new HistorySport(mac, historySport
                    .getDateString(), historySport.getStepNum(),
                    mSleepSate);
            Log.e(TAG, "***产生第一个睡眠数据，补全40分钟***Date***" + historySport
                    .getDateString() + "***StepNum***" + historySport
                    .getStepNum
                            () + "***SleepStae***" + mSleepSate);
            tpH.set(m, mHistorySport);
            Log.e(TAG, "***再次查询获取结果***" + ((HistorySport) tpH.get(m))
                    .getDateString());
            DbHistorySport.getInstance(context).saveOrUpdate(tpH);
        }
    }

    public static void proccessData194Data(Context context, String mac, IBleCmdCallback callback, Vector<byte[]> tp,
                                           int checkSum, int deviceType) {
        if (tp == null || tp.size() == 0)
            return;
        Vector<byte[]> list = new Vector<>();
        list.addAll(tp);
        int clength = list.size() * 19;
        if (clength < checkSum) {
            if (callback != null) {
                callback.syncState(BaseController.STATE_SYNC_ERROR);
            }
            return;
        }

        checkSum -= 2;///减去两个字节的校验和低位、高位
        byte[] realData = new byte[checkSum];

        int realLength = 0;
        boolean isOk = false;
        if (checkSum >= 0) {
            for (int i = 0; i < list.size(); i++) {
                byte[] tppp = list.get(i);
                for (int j = 1; j < tppp.length; j++) {
                    realData[realLength++] = tppp[j];
                    if (realLength >= checkSum) {
                        isOk = true;
                        break;
                    }
                }
                if (isOk) {
                    break;
                }
            }
        }
        if (realLength < 12) {
            return;
        }
        byte[] totalSteps = new byte[4];
        System.arraycopy(realData, 0, totalSteps, 0, 4);
        int tSteps = Utils.lBytesToInt(totalSteps);

        byte[] totalCalories = new byte[4];
        System.arraycopy(realData, 4, totalCalories, 0, 4);
        int tCalories = Utils.lBytesToInt(totalCalories);

        byte[] totalDistance = new byte[4];
        System.arraycopy(realData, 8, totalDistance, 0, 4);
        int tDistance = Utils.lBytesToInt(totalDistance);

        Calendar cale = Calendar.getInstance();
        String nowDate = DateUtil.dataToString(cale.getTime(), "yyyy-MM-dd");
        UserInfo userInfo = UserInfo.getInstance(context);
        float totalDist = tDistance / 100.0f;
        SportDayData dayData = new SportDayData(mac, nowDate, tSteps, totalDist, tCalories, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0);

        PedoRealData pedoRealData = new PedoRealData(nowDate, mac, tSteps, tCalories, totalDist);
        if (IS_DEBUG)
            Log.e(TAG, nowDate + "---194同步时更新实时----" + tSteps);
        DbRealTimePedo.getInstance(context).saveOrUpdate(pedoRealData);
        DbSprotDayData.getInstance(context).saveOrUpdate(dayData);


        int minute = cale.get(Calendar.MINUTE);
        if (deviceType == BaseDevice.TYPE_P118 || deviceType == BaseDevice.TYPE_MILLIONPEDOMETER) {//这是W118设备
            if (minute < 30) {
                cale.add(Calendar.MINUTE, -(Calendar.MINUTE + 30));
            } else {
                cale.add(Calendar.MINUTE, -minute);
            }

            cale.set(Calendar.SECOND, 0);
        } else {
            //每五分钟数据，当前时间分钟不满5分钟，则向下取整为5分钟倍数，
            //如，9:36获取的数据，则最近6个字节数据对应的时间为9:30-9:35
            //所以获取的数据对应的时间应该从9：35往前推，依次是9:25-9:30，9:20-9:25
            int oStep = cale.get(Calendar.MINUTE);
            cale.add(Calendar.MINUTE, -(oStep % 5 + 5));
            cale.set(Calendar.SECOND, 0);
        }

        //每5分钟数据;
        int oSteps = 0;//计步数据;
        int oCalories = 0;
        int oDistance = 0;
        int x = 0;
        int y = 0;
        int z = 0;
        boolean flag = true;
        if (realLength >= 18) {
            int i;
            List<HistorySport> listSports = new ArrayList<>();
            byte[] oneSteps;
            byte[] oneCalories;
            byte[] oneDistance;
            int num;
            DbHistorySport.getInstance(context).beginTransaction();
            try {
                if (deviceType == BaseDevice.TYPE_W194 || deviceType == BaseDevice.TYPE_ACTIVITYTRACKER ||
                        deviceType == BaseDevice.TYPE_W240 || deviceType == BaseDevice.TYPE_P118S) {//解析w194设备.
                    for (i = 12; i < realLength - 6; i += 6) {//每5分钟数据 为6个字节;
                        flag = true;
                        x = 0;
                        y = 0;
                        z = 0;

                        oneSteps = new byte[2];
                        System.arraycopy(realData, i, oneSteps, 0, 2);
                        if (oneSteps[1] >= 0) {
                            flag = false;
                            oSteps = Utils.lBytesToInt(oneSteps);
                        } else {
                            flag = true;
                            oneSteps[1] = (byte) (127 & oneSteps[1]);
                            x = Utils.lBytesToInt(oneSteps);
                        }

                        oneCalories = new byte[2];
                        System.arraycopy(realData, i + 2, oneCalories, 0, 2);
                        if (oneCalories[1] >= 0) {
                            oCalories = Utils.lBytesToInt(oneCalories);
                        } else {
                            oneCalories[1] = (byte) (127 & oneCalories[1]);
                            y = Utils.lBytesToInt(oneCalories);
                        }

                        oneDistance = new byte[2];
                        System.arraycopy(realData, i + 4, oneDistance, 0, 2);
                        if (oneDistance[1] >= 0) {
                            oDistance = Utils.lBytesToInt(oneDistance) * 10;
                        } else {
                            oneDistance[1] = (byte) (127 & oneDistance[1]);
                            z = Utils.lBytesToInt(oneDistance);
                        }
                        int sleepState = 0;
                        String dateString = DateUtil.dataToString(cale.getTime(), "yyyy-MM-dd HH:mm");
                        HistorySport historySport = DbHistorySport.getInstance(context).
                                findFirst(DbHistorySport.COLUMN_DATE + "=? and " + DbHistorySport.COLUMN_MAC + "=?",
                                        new String[]{dateString, mac});
                        if (flag) {//flag 判断是每5分钟数据是计步数据还是卡路里数据;
                            num = x + y + z;
                            if (num <= 800) {
                                sleepState = 0x80;
                            } else if (num <= 1600) {
                                sleepState = 0x81;
                            } else if (num <= 3500) {
                                sleepState = 0x82;
                            } else {
                                sleepState = 0x83;
                            }

                            if (historySport != null) {
                                historySport.setSleepState(sleepState);
                                historySport.setStepNum(0);
                            } else {
                                historySport = new HistorySport(mac, dateString, 0, sleepState);
                            }
                            historySport.setMac(mac);
                            listSports.add(historySport);
                        } else {
                            if (historySport != null) {
                                historySport.setStepNum(oSteps);
                                historySport.setSleepState(0);
                                listSports.add(historySport);
                                historySport.setMac(mac);
                            } else {
                                listSports.add(new HistorySport(mac, dateString, oSteps, 0));
                            }
                        }

                        cale.add(Calendar.MINUTE, -5);
                    }
                } else {
                    for (i = realLength - 6; i >= 12; i -= 6) {
                        flag = true;
                        x = 0;
                        y = 0;
                        z = 0;
                        oneSteps = new byte[2];
                        System.arraycopy(realData, i, oneSteps, 0, 2);
                        if (oneSteps[1] >= 0) {
                            flag = false;
                            oSteps = Utils.lBytesToInt(oneSteps);
                        } else {
                            flag = true;
                            oneSteps[1] = (byte) (127 & oneSteps[1]);
                            x = Utils.lBytesToInt(oneSteps);
                        }

                        oneCalories = new byte[2];
                        System.arraycopy(realData, i + 2, oneCalories, 0, 2);
                        if (oneCalories[1] >= 0) {
                            oCalories = Utils.lBytesToInt(oneCalories);
                        } else {
                            oneCalories[1] = (byte) (127 & oneCalories[1]);
                            y = Utils.lBytesToInt(oneCalories);
                        }

                        oneDistance = new byte[2];
                        System.arraycopy(realData, i + 4, oneDistance, 0, 2);
                        if (oneDistance[1] >= 0) {
                            oDistance = Utils.lBytesToInt(oneDistance) * 10;
                        } else {
                            oneDistance[1] = (byte) (127 & oneDistance[1]);
                            z = Utils.lBytesToInt(oneDistance);
                        }

                        int sleepState = 0;
                        String dateString = DateUtil.dataToString(cale.getTime(), "yyyy-MM-dd HH:mm");
                        HistorySport historySport = DbHistorySport.getInstance(context).
                                findFirst(DbHistorySport.COLUMN_DATE + "=? and " + DbHistorySport.COLUMN_MAC + "=?",
                                        new String[]{dateString, mac});
                        if (flag) {
                            num = x + y + z;
                            if (num <= 800) {
                                sleepState = 0x80;
                            } else if (num <= 1600) {
                                sleepState = 0x81;
                            } else if (num <= 3500) {
                                sleepState = 0x82;
                            } else {
                                sleepState = 0x83;
                            }
                            if (historySport != null) {
                                historySport.setSleepState(sleepState);
                                historySport.setStepNum(0);
                            } else {
                                historySport = new HistorySport(mac, dateString, 0, sleepState);
                            }
                            historySport.setMac(mac);
                            listSports.add(historySport);
                        } else {
                            if (historySport != null) {
                                historySport.setStepNum(oSteps);
                                historySport.setSleepState(0);
                                historySport.setMac(mac);
                                listSports.add(historySport);
                            } else {
                                listSports.add(new HistorySport(mac, dateString, oSteps, 0));
                            }
                        }

                        if (deviceType == BaseDevice.TYPE_MILLIONPEDOMETER || deviceType == BaseDevice.TYPE_P118) {
                            cale.add(Calendar.MINUTE, -30);
                        } else {
                            cale.add(Calendar.MINUTE, -5);
                        }

                    }
                }
                DbHistorySport.getInstance(context).setTransactionSuccessful();
                Calendar tpCalendar = Calendar.getInstance();
                tpCalendar.add(Calendar.DAY_OF_MONTH, -1);
                for (int j = 0; j <= 15; j++) {
                    String dts = DateUtil.dataToString(tpCalendar.getTime(), "yyyy-MM-dd");
                    float[] val = DbHistorySport.getInstance(context).sum(DbHistorySport.TABLE_NAME, new
                                    String[]{DbHistorySport
                                    .COLUMN_STEP_NUM},
                            DbHistorySport.COLUMN_MAC + "=? and " +
                                    DbHistorySport.COLUMN_DATE + " like" +
                                    " ?", new String[]{mac, dts + "%"});
                    if (val != null && val.length > 0) {
                        int steppp = (int) val[0];
                        SportDayData sportDayDataTP = DbSprotDayData.getInstance(context)
                                .findFirst(DbSprotDayData.COLUMN_MAC + "=? and " + DbSprotDayData.COLUMN_DATE + "=?",
                                        new String[]{mac, dts}, null);
                        if (sportDayDataTP != null) {

                            if (sportDayDataTP.getTotalStep() < steppp) {
                                sportDayDataTP.setTotalStep(steppp);
                                sportDayDataTP.setTotalCaloric(sportDayDataTP.getTotalCaloric() + cal194Caloric
                                        (context, steppp - sportDayDataTP.getTotalStep()));
                                sportDayDataTP.setTotalDist(sportDayDataTP.getTotalDist() + cal194Distance(context,
                                        steppp -
                                                sportDayDataTP.getTotalStep()));
                                DbSprotDayData.getInstance(context).saveOrUpdate(sportDayDataTP);
                                Log.e(TAG, nowDate + "---194同步时更新实时----" + tSteps);
                                DbRealTimePedo.getInstance(context).saveOrUpdate(
                                        new PedoRealData(dts, mac, steppp, sportDayDataTP.getTotalCaloric(),
                                                sportDayDataTP.getTotalDist()));
                            }
                        } else {
                            DbSprotDayData.getInstance(context).saveOrUpdate(
                                    new SportDayData(mac, dts, steppp, cal194Distance(context, steppp), cal194Caloric
                                            (context, steppp),
                                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
                            Log.e(TAG, nowDate + "---194同步时更新实时----" + tSteps);
                            DbRealTimePedo.getInstance(context).saveOrUpdate(
                                    new PedoRealData(dts, mac, steppp, cal194Caloric(context, steppp), cal194Distance
                                            (context, steppp)));
                        }
                    }
                    tpCalendar.add(Calendar.DAY_OF_MONTH, -1);
                }
                if (callback != null) {
                    callback.syncState(BaseController.STATE_SYNC_COMPLETED);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.syncState(BaseController.STATE_SYNC_ERROR);
                }
                e.printStackTrace();
            } finally {
                DbHistorySport.getInstance(context).endTransaction();
            }
            if (listSports != null && listSports.size() > 0) {
                DbHistorySport.getInstance(context).saveOrUpdate(listSports);
            }
        } else {
            if (callback != null) {
                callback.syncState(BaseController.STATE_SYNC_ERROR);
            }
        }
    }

    private static int cal194Caloric(Context context, double step) {
        UserInfo userInfo = UserInfo.getInstance(context);
        return (int) (step * ((userInfo.getWeight() - 13.63636) * 0.000693 + 0.000495));
    }

    private static float cal194Distance(Context context, double step) {
        UserInfo userInfo = UserInfo.getInstance(context);
        return (float) (step * userInfo.getStrideLength() / 1000f);
    }

    public static void processRealTime194Data(Context context, String mac, IBleCmdCallback callback, byte[] data) {

    }

    public static SportData337B proccessData337BData(Context context, String mac, IBleCmdCallback callback, byte[]
            data, int checkSum, int deviceType, int index) {
        if (data == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            builder.append(String.format("%02X", data[i]) + " ");
        }
        Log.e("parser", builder.toString());
        int sportType = data[0];
        int speed = Utils.byte2Int(data[1]);
        int totalStep = Utils.byte2Int(data[2]) + (Utils.byte2Int(data[3]) << 8);
        int totalDist = Utils.byte2Int(data[4]) + (Utils.byte2Int(data[5]) << 8);
        int totalCal = Utils.byte2Int(data[6]) + (Utils.byte2Int(data[7]) << 8);
        int sportTime = Utils.byte2Int(data[8]) + (Utils.byte2Int(data[9]) << 8);
        int deepTime = Utils.byte2Int(data[10]) + (Utils.byte2Int(data[11]) << 8);
        int lightTime = Utils.byte2Int(data[12]) + (Utils.byte2Int(data[13]) << 8);
        int dayRestTime = Utils.byte2Int(data[14]) + (Utils.byte2Int(data[15]) << 8);
        int heartRate = Utils.byte2Int(data[16]);
        int bloodOxygen = Utils.byte2Int(data[17]);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, index);
        String date = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
        return new SportData337B(date, mac, sportType, speed, totalStep, totalDist, totalCal, sportTime, deepTime,
                lightTime, dayRestTime, heartRate, bloodOxygen);
    }

    /**
     * 存储心率数据
     *
     * @param context
     * @param mac
     * @param totalcount
     * @param list
     * @param settingCallBack
     */
    public static void processHeartHistory(Context context, String mac, int totalcount, List<byte[]> list,
                                           OnDeviceSetting settingCallBack) {
        byte[] bs = new byte[totalcount + 2];
        int tpcount = 0;
        List<HeartRecord> listHeartRecord = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            byte[] tpb = list.get(i);
            for (int j = 0; j < tpb.length; j++) {
                bs[tpcount] = tpb[j];
                tpcount++;
            }
        }
        int len = 0;
        int totalHeart = 0;
        int minHeart = 200;
        int maxHeart = 0;

        HeartRecord heartRecord = null;
        ArrayList<HeartData> dataList = null;
        Calendar calendar = null;
        while (len < totalcount) {
            /*FA-FA-FA-FA-second-minute-hour-day-month-year(2bytes)-HRdata-HRdata-HRdata
            HRdata-......HRdata-FF-FF-FF-FF*/
            if (len + 11 <= tpcount && ((bs[len] & 0xff) == 0xFA && (bs[len + 1] & 0xff) == 0xFA && (bs[len + 2] &
                    0xff) == 0xFA && (bs[len + 3] & 0xff) == 0xFA)) {
                int second = bs[len + 4] & 0xff;
                int min = bs[len + 5] & 0xff;
                int hour = bs[len + 6] & 0xff;
                int day = bs[len + 7] & 0xff;
                int month = bs[len + 8] & 0xff;
                int year = ((bs[len + 9] & 0xff) << 8) + (bs[len + 10] & 0xff);

                calendar = Calendar.getInstance();
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, second);
                calendar.set(Calendar.MINUTE, min);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.YEAR, year);

                heartRecord = new HeartRecord();
                dataList = new ArrayList<>();
                heartRecord.setStartTime(DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"));
                len = len + 11;
                minHeart = 0;
                maxHeart = 0;
                totalHeart = 0;
            } else if (len + 4 <= tpcount && ((bs[len] & 0xff) == 0xff && (bs[len + 1] & 0xff) == 0xff &&
                    (bs[len + 2] & 0xff) == 0xff) && (bs[len + 3] & 0xff) == 0xff) {
                heartRecord.setDataList(dataList);
                heartRecord.setMac(mac);
                heartRecord.setMax(maxHeart);
                heartRecord.setMin(minHeart);
                heartRecord.setTotal(totalHeart);
                heartRecord.setAvg(dataList.size() <= 0 ? 0 : (int) (totalHeart / (dataList.size() * 1.0)));

                listHeartRecord.add(heartRecord);
                len = len + 4;
            } else {
                int rate = bs[len] & 0xff;
                if (rate != 0) {///过滤掉心率为0
                    totalHeart += rate;
                    if (minHeart == 0) {
                        minHeart = rate;
                    } else {
                        minHeart = minHeart < rate ? minHeart : rate;
                    }
                    maxHeart = maxHeart < rate ? rate : maxHeart;
                    calendar.add(Calendar.SECOND, 5);
                    dataList.add(new HeartData(rate, calendar.getTimeInMillis()));
                }
                len++;
            }
        }
        if (listHeartRecord.size() > 0) {
            if (settingCallBack != null) {
                settingCallBack.onHeartHistorySynced(listHeartRecord);
            }
        } else {
            if (settingCallBack != null) {
                settingCallBack.onHeartHistorySynced(OnDeviceSetting.SYNC_HEART_STATE_SUCCESS);
            }
        }
    }

    public static void processW311TData(Context context, String mac, IBleCmdCallback callback, List<byte[]> tp) {

        /*List list = new ArrayList();
        if(tp != null){
            list.addAll(tp);
        }*/
        if (tp == null || tp.size() == 0) {

        }
        List<byte[]> list = tp;

        if (list != null && list.size() >= 3) {
            byte[] data = (byte[]) list.get(0);
            int year = byteArrayToInt(new byte[]{data[4], data[5]});
            int mon = byteArrayToInt(new byte[]{data[6]});
            int day = byteArrayToInt(new byte[]{data[7]});
            int totalStep = byteArrayToInt(new byte[]{data[8], data[9], data[10], data[11]});
            int totalCaloric = byteArrayToInt(new byte[]{data[12], data[13], data[14], data[15]});
            float totalDist = byteArrayToInt(new byte[]{data[16], data[17]}) / 100.0f;
            int totalSportTime = byteArrayToInt(new byte[]{data[18], data[19]});
            data = (byte[]) list.get(1);
            int totalSleepTime = byteArrayToInt(new byte[]{data[0], data[1]});
            int totalStillTime = byteArrayToInt(new byte[]{data[2], data[3]});
            int walkTime = byteArrayToInt(new byte[]{data[4], data[5]});
            int lowSpeedWalkTime = byteArrayToInt(new byte[]{data[6], data[7]});
            int midSpeedWalkTime = byteArrayToInt(new byte[]{data[8], data[9]});
            int larSpeedWalkTime = byteArrayToInt(new byte[]{data[10], data[11]});
            int lowSpeedRunTime = byteArrayToInt(new byte[]{data[12], data[13]});
            int midSpeedRunTime = byteArrayToInt(new byte[]{data[14], data[15]});
            int larSpeedRunTime = byteArrayToInt(new byte[]{data[16], data[17]});
            data = (byte[]) list.get(2);
            float strideLen = byteArrayToInt(new byte[]{data[0], data[1]}) / 100.0f;
            float weight = byteArrayToInt(new byte[]{data[2], data[3]}) / 100.0f;
            int targetStep = byteArrayToInt(new byte[]{data[4], data[5], data[6]});
            int targetSleepTime = byteArrayToInt(new byte[]{data[7], data[8]});
            String dataString = String.format("%04d", year) + "-" + String.format("%02d", mon) + "-" + String.format
                    ("%02d", day);

            SportDayData dayData = new SportDayData(mac, dataString, totalStep, totalDist, totalCaloric, targetStep,
                    strideLen, weight, totalSleepTime, totalStillTime,
                    walkTime, lowSpeedWalkTime, midSpeedWalkTime, larSpeedWalkTime,
                    lowSpeedRunTime, midSpeedRunTime, larSpeedRunTime,
                    totalSportTime, targetSleepTime);

            PedoRealData pedoRealData = new PedoRealData(dataString, mac, totalStep, totalCaloric, totalDist);
            if (IS_DEBUG)
                Log.e(TAG, dataString + "---311T同步时更新实时----" + totalStep);
            DbRealTimePedo.getInstance(context).saveOrUpdate(pedoRealData);
            DbSprotDayData.getInstance(context).saveOrUpdate(dayData);

            int totalByteCount = byteArrayToInt(new byte[]{data[9], data[10]});
            data = new byte[(list.size() - 3) * 20];
            for (int i = 3; i < list.size(); i++) {
                byte[] tmp = (byte[]) list.get(i);
                if (tmp != null) {
                    System.arraycopy(tmp, 0, data, (i - 3) * 20, 20);
                }
            }
            List histList = new ArrayList();
            /**
             * jiao yan
             */
            if (totalByteCount > data.length) {
                if (callback != null) {
                    callback.syncState(BaseController.STATE_SYNC_ERROR);
                }
            } else {
                int length = data.length;
                Calendar calendar = Calendar.getInstance();
                String todayStr = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
                calendar.set(year, mon - 1, day, 0, 0, 0);
                String dateHist = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
                int i = 0;
                int index = 0;
                int lastFlag = -1;
                DbHistorySport.getInstance(context).beginTransaction();
                try {
                    int mmmm = 0;
                    int nnnn = 0;
                    while (i < totalByteCount) {
                        nnnn++;
                        /*修復最後一個五分鐘數據是睡眠數據不解析的情況*/
                        if (i + 1 >= totalByteCount)
                            break;
                        int tttttpflag = byteToInt(data[i + 1]) & 0x0080;
                        if (tttttpflag == 0x80) {
                            if (i + 1 >= totalByteCount)
                                break;
                        } else {
                            if (i + 2 >= totalByteCount)
                                break;
                        }

                        mmmm++;

                        int tIndex = byteToInt(data[i]);//
                        int dv = 1;
                        if (tIndex - index > 0) {
                            dv = tIndex - index;
                        } else if (tIndex - index < 0) {
                            dv = byteToInt((byte) 0xff) - index + tIndex;
                        }
                        int flag = byteToInt(data[i + 1]) & 0x0080;
                        HistorySport sport = null;

                        if (flag == 0x80) {//sleep data
                            byte[] tdp = {data[i + 1]};//
                            int sleepState = byteToInt(tdp[0]);
                            for (int j = 1; j <= dv; j++) {

                                String dateString = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd HH:mm");
                                HistorySport historySport = DbHistorySport.getInstance(context).
                                        findFirst(DbHistorySport.COLUMN_DATE + "=? and " + DbHistorySport.COLUMN_MAC
                                                + "=?", new String[]{dateString, mac});
                                if (historySport != null) {
                                    historySport.setSleepState(sleepState);
                                    historySport.setStepNum(0);
                                    historySport.setMac(mac);
                                } else {
                                    historySport = new HistorySport(mac, dateString, 0, sleepState);
                                }
                                histList.add(historySport);
                                //Log.e("sleepdata","datestring = "+dateString+" , state = "+sleepState);
                                calendar.add(Calendar.MINUTE, 5);
                            }
                            i = i + 2;

                        } else if (flag == 0) {//step data
                            byte[] tdp = {data[i + 1], data[i + 2]};//
                            int step = byteArrayToInt(tdp);
                            for (int j = 1; j <= dv; j++) {

                                String dateString = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd HH:mm");
                                HistorySport historySport = DbHistorySport.getInstance(context).
                                        findFirst(DbHistorySport.COLUMN_DATE + "=? and " + DbHistorySport.COLUMN_MAC
                                                + "=?", new String[]{dateString, mac});
                                if (historySport != null) {
                                    historySport.setStepNum(step);
                                    historySport.setSleepState(0);
                                    historySport.setMac(mac);
                                    histList.add(historySport);
                                } else {
                                    histList.add(new HistorySport(mac, dateString, step, 0));
                                }
                                //Log.e("sleepdata","datestring = "+dateString+" , state = "+step);
                                calendar.add(Calendar.MINUTE, 5);
                            }
                            i = i + 3;
                        }
                        index = tIndex;
                        lastFlag = flag;
                    }
                    DbHistorySport.getInstance(context).setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    DbHistorySport.getInstance(context).endTransaction();
                }
                if (histList != null && histList.size() > 0) {
                    DbHistorySport.getInstance(context).saveOrUpdate(histList);
                }
            }
        }
    }
}
