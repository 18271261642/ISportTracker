package com.isport.tracker.entity;

/**
 * 作者：chs on 2016/9/6 15:14
 * 邮箱：657083984@qq.com
 */
public class ContinousBarChartEntity {
    public int period;//分钟
    public int yValue;
    public int type;

    public ContinousBarChartEntity(int period, int yValue, int type) {

        this.period = period;
        this.yValue = yValue;
        this.type = type;
    }

}
