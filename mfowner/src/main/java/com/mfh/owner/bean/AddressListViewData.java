package com.mfh.owner.bean;

/**
 * Created by Administrator on 2015/4/21.
 */
public class AddressListViewData {
    private String name;
    private String address;

    public AddressListViewData(String name, String address){
        this.name = name;
        this.address = address;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
