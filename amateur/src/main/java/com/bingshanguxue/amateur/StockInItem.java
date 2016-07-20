package com.bingshanguxue.amateur;

/**
 * 入库－－快递明细
 * Created by Administrator on 2015/5/14.
 *
 */
public class StockInItem implements java.io.Serializable{
    private String fdorderNumber;//快递面单号(必填)
    private String mobile;//收件号码(必填)
    private String humanId;//收件人编号（可空）
    private String addrvalId;//收件地址编号(可空)
    private Integer needmsg = 0;//出库时是否需要提醒（0-不需要，1-需要）
    private Integer mustsms = 1;//必要时是否需要短信通知（0-需要 1-不需要）

    private String name;

    private Integer bindwx;

    public String getFdorderNumber() {
        return fdorderNumber;
    }

    public void setFdorderNumber(String fdorderNumber) {
        this.fdorderNumber = fdorderNumber;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getHumanId() {
        return humanId;
    }

    public void setHumanId(String humanId) {
        this.humanId = humanId;
    }


    public String getAddrvalId() {
        return addrvalId;
    }

    public void setAddrvalId(String addrvalId) {
        this.addrvalId = addrvalId;
    }

    public Integer getNeedmsg() {
        return needmsg;
    }

    public void setNeedmsg(Integer needmsg) {
        this.needmsg = needmsg;
    }

    public Integer getMustsms() {
        return mustsms;
    }

    public void setMustsms(Integer mustsms) {
        this.mustsms = mustsms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBindwx() {
        return bindwx;
    }

    public void setBindwx(Integer bindwx) {
        this.bindwx = bindwx;
    }
}
