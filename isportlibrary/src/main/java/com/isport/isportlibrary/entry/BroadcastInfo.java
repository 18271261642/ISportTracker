package com.isport.isportlibrary.entry;

/**
 * Created by Marcos on 2017/12/8.
 * <p>
 * byte description
 * 0-1 FFF0(芯海标识）
 * 2 广播版本号（当前 02）
 * 3 消息属性，参见表一
 * 4~5 体重
 * 6~9 产品 ID
 * 10~11 蓝牙版本
 * 12~13 秤算法版本
 * 14-19 mac 地址
 */

public class BroadcastInfo {

    public static final int UNIT_KG = 0;
    public static final int UNIT_JIN = 0x01;
    public static final int UNIT_LB = 0x02;
    public static final int UNIT_STLB = 0x03;

    private int id;
    /**
     * algorithm version
     */
    private int algorithmVersion;
    /**
     * version of device
     */
    private int version;
    private int weight;
    /**
     * product id
     */
    private int productId;
    /**
     * mac of device
     */
    private String mac;
    /**
     * version of bluetooth
     */
    private int bleVersion;

    private int unitType;

    /**
     * 保留小数位个数
     */
    private int dotNumber;

    public BroadcastInfo(int id, int algorithmVersion, int version, int weight, int productId, String mac, int bleVersion, int unitType, int dotNumber) {
        this.id = id;
        this.algorithmVersion = algorithmVersion;
        this.version = version;
        this.weight = weight;
        this.productId = productId;
        this.mac = mac;
        this.bleVersion = bleVersion;
        this.unitType = unitType;
        this.dotNumber = dotNumber;
    }

    public int getId() {
        return id;
    }

    public int getAlgorithmVersion() {
        return algorithmVersion;
    }

    public int getVersion() {
        return version;
    }

    public int getWeight() {
        return weight;
    }

    public int getProductId() {
        return productId;
    }

    public String getMac() {
        return mac;
    }

    public int getBleVersion() {
        return bleVersion;
    }

    public int getDotNumber() {
        return dotNumber;
    }

    public int getUnitType() {
        return unitType;
    }
}
