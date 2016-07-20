package com.manfenjiayuan.business.bean.wrapper;

import java.io.Serializable;

/**
 * 网点信息
 * 适用场景：
 * 1.批发商选择发货接收方网点
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-9-6
 */
public class NetInfoWrapper implements Serializable {
    private Long netId;        //网点编号
    private String name = "";    //网点名称


    public Long getNetId() {
        return netId;
    }

    public void setNetId(Long netId) {
        this.netId = netId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
