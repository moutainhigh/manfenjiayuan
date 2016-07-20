package com.mfh.petitestock.bean;

import java.io.Serializable;

/**
 * Shelfnumber
 * Created by Nat.ZZN(bingshanguxue) on 15/9/2.
 */
public class Shelfnumber implements  Serializable {

    private Long number;//编号

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public static Shelfnumber newInstance(Long number){
        Shelfnumber shelfnumber = new Shelfnumber();
        shelfnumber.setNumber(number);
        return shelfnumber;
    }
}
