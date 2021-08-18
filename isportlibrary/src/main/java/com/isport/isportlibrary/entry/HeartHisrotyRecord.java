package com.isport.isportlibrary.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Created by Marcos Cheng on 2017/8/14.
 * heart rate test record that saved in ble device
 */

public class HeartHisrotyRecord {

    private int totalCount;
    private List<byte[]> heartHistList;
    private long checkSum;////

    public HeartHisrotyRecord(){
        totalCount = 0;
        heartHistList = Collections.synchronizedList(new ArrayList<byte[]>());
    }

    public HeartHisrotyRecord(int totalCount) {
        this.totalCount = totalCount;
        this.heartHistList = Collections.synchronizedList(new ArrayList<byte[]>());
    }

    public HeartHisrotyRecord(List<byte[]> list) {
        this.heartHistList = list;
        this.totalCount = 0;
    }

    public HeartHisrotyRecord(int totalCount, List<byte[]> list) {
        this.totalCount = totalCount;
        this.heartHistList = list;
    }

    /**
     * how many heart rate had been saved
     * @return
     */
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * heart rate list
     * @return
     */
    public List<byte[]> getHeartHistList() {
        return heartHistList;
    }

    public void setHeartHistList(List<byte[]> heartHistList) {
        this.heartHistList = heartHistList;
    }

    /**
     * check sum to check whether there is error when sync the data
     * @return
     */
    public long getCheckSum() {
        return (checkSum );
    }

    public void setCheckSum(long checkSum) {
        this.checkSum = checkSum;
    }
}
