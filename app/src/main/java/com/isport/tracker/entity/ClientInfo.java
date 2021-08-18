package com.isport.tracker.entity;

/**
 * Created by Administrator on 2016/6/30.
 *
 * Client info
 */
public class ClientInfo {
    private String clientName;//客户名称
    private boolean isSelected;///是否是该客户的定制app

    public ClientInfo(){
        super();
    }

    public String getClientName(){
        return this.clientName;
    }

    public boolean isSelected(){
        return this.isSelected;
    }

    public void setClientName(String name){
        this.clientName = name;
    }

    public void setSelected(boolean selected){
        this.isSelected = selected;
    }
}
