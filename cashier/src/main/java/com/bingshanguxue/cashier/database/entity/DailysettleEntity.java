package com.bingshanguxue.cashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

import java.util.Date;

/**
 * <h1>POS--日结</h1><br>
 * <p>
 *
 * </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name = "tb_pos_dailysettle_v1")
public class DailysettleEntity extends MfhEntity<Long> implements ILongId {

    //日结信息
    /**条码号(网点编号_设备编号_业务类型_日期，format like "123079_4_9_20160219")*/
    private String barCode = "";
    private Long officeId;//门店编号
    private String officeName = "";//门店
    private String humanName = "";//结算人
    private Date dailysettleDate; //日结日期

    //经营分析数据
    private Double turnover = 0D;//营业额合计
    private String aggData;

    //流水分析数据
    private Double cash = 0D;//现金收取金额
    private String accData;

    public static final int CONFIRM_STATUS_NO = 0;//未确认
    public static final int CONFIRM_STATUS_YES = 1;//已确认
    /**
     * 日结确认状态
     * 0-未确认,每次打开日结页面都需要重新统计，可以查询统计结果。
     * 1-已确认，不可以再统计，可以查询统计结果。
     */
    private int confirmStatus = CONFIRM_STATUS_NO;

    public static final int PAY_STATUS_INIT         = 0;   //未付款
    public static final int PAY_STATUS_PROCESS      = 1;   //支付处理中
    public static final int PAY_STATUS_FAILED       = 2;   //支付失败
    public static final int PAY_STATUS_EXCEPTION    = 3;   //支付异常
    public static final int PAY_STATUS_SUCCEED      = 4;   //支付成功
    /**
     * 支付状态
     * 0-未付款
     * 1-已付款(全部或部分)
     */
    private int paystatus = PAY_STATUS_INIT;//支付金额可以去查看支付记录


    public static final int SYNC_STATUS_NONE = 0;//初始状态
    public static final int SYNC_STATUS_SYNCED = 1;//结束状态
    /**
     * 同步状态
     * 0-初始
     * 1-已同步
     */
    private int syncStatus = SYNC_STATUS_NONE;//同步参数：0，未同步；1已同步

    //流水分析

    private String remark = ""; //备注

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getHumanName() {
        return humanName;
    }

    public void setHumanName(String humanName) {
        this.humanName = humanName;
    }

    public Date getDailysettleDate() {
        return dailysettleDate;
    }

    public void setDailysettleDate(Date dailysettleDate) {
        this.dailysettleDate = dailysettleDate;
    }

    public int getConfirmStatus() {
        return confirmStatus;
    }

    public void setConfirmStatus(int confirmStatus) {
        this.confirmStatus = confirmStatus;
    }

    public int getPaystatus() {
        return paystatus;
    }

    public void setPaystatus(int paystatus) {
        this.paystatus = paystatus;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getTurnover() {
        if (turnover == null){
            return 0D;
        }
        return turnover;
    }

    public void setTurnover(Double turnover) {
        this.turnover = turnover;
    }

    public String getAggData() {
        return aggData;
    }

    public void setAggData(String aggData) {
        this.aggData = aggData;
    }

    public String getAccData() {
        return accData;
    }

    public void setAccData(String accData) {
        this.accData = accData;
    }

    public Double getCash() {
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
    }

    /**
     *  {@link #PAY_STATUS_INIT
     *  0(未付款)}<br>
     *  {@link #PAY_STATUS_PROCESS
     *  1(支付处理中)}<br>
     *  {@link #PAY_STATUS_FAILED
     *  2(支付失败)}<br>
     *  {@link #PAY_STATUS_EXCEPTION
     *  3(支付异常)}<br>
     *  {@link #PAY_STATUS_SUCCEED
     *  4(支付成功)}<br>
     */
    public static String getPayStatusDesc(int payStatus){
        switch (payStatus){
            case DailysettleEntity.PAY_STATUS_PROCESS:{
                return "支付处理中";
            }
            case DailysettleEntity.PAY_STATUS_FAILED:{
                return "支付失败";
            }
            case DailysettleEntity.PAY_STATUS_EXCEPTION:{
                return "支付异常";
            }
            case DailysettleEntity.PAY_STATUS_SUCCEED:{
                return "支付成功";
            }
            default:{
                return "未支付";
            }
        }
    }


}
