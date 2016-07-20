package com.mfh.enjoycity.bean;

/**
 * ADDRESS
 * Created by NAT.ZZN on 2015/4/21.
 */
public class AddressData {
    private String name;
    private String address;

    public AddressData(String name, String address){
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
