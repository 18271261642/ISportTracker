package com.isport.isportlibrary.entry;

import java.io.Serializable;

/**
 * @author Created by Marcos Cheng on 2016/12/30.
 * Weight scale medical examination data
 */

public class MExamination implements Serializable {

    private String mac;
    private int deviceType;//设备类型
    private int stateFlag;///状态标识
    private float weight;//体重
    private float fat;//脂肪
    private float waterContent;//水分
    private float muscle;///肌肉
    private float boneMass;///骨量
    private float caloric;///卡路里

    public MExamination(String mac, int deviceType, int stateFlag, float weight, float fat, float waterContent, float muscle, float boneMass, float caloric) {
        this.mac = mac;
        this.deviceType = deviceType;
        this.stateFlag = stateFlag;
        this.weight = weight;
        this.fat = fat;
        this.waterContent = waterContent;
        this.muscle = muscle;
        this.boneMass = boneMass;
        this.caloric = caloric;
    }

    public String getMac() {
        return mac;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public int getStateFlag() {
        return stateFlag;
    }

    public float getWeight() {
        return weight;
    }

    public float getFat() {
        return fat;
    }

    public float getWaterContent() {
        return waterContent;
    }

    public float getMuscle() {
        return muscle;
    }

    public float getBoneMass() {
        return boneMass;
    }

    public float getCaloric() {
        return caloric;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public void setStateFlag(int stateFlag) {
        this.stateFlag = stateFlag;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public void setWaterContent(float waterContent) {
        this.waterContent = waterContent;
    }

    public void setMuscle(float muscle) {
        this.muscle = muscle;
    }

    public void setBoneMass(float boneMass) {
        this.boneMass = boneMass;
    }

    public void setCaloric(float caloric) {
        this.caloric = caloric;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
