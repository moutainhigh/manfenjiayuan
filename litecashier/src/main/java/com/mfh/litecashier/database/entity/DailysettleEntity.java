package com.mfh.litecashier.database.entity;

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
@Table(name = "tb_pos_dailysettle_v0")
public class DailysettleEntity extends MfhEntity<Long> implements ILongId {

    //日结信息
    /**条码号(网点编号_设备编号_业务类型_日期，format like "123079_4_9_20160219")*/
    private String barCode = "";
    private Long officeId;//门店编号
    private String officeName = "";//门店
    private String humanName = "";//结算人
    private Date dailysettleDate; //日结日期

    //经营分析数据
    private Double grossProfit = 0D;//毛利
    private Double turnover = 0D;//营业额合计
    private Double aggPosAmount = 0D;
    private Double aggPosOrderNum = 0D;
    private Double aggScAmount = 0D;
    private Double aggScOrderNum = 0D;
    private Double aggLaundryAmount = 0D;
    private Double aggLaundryOrderNum = 0D;
    private Double aggPijuAmount = 0D;
    private Double aggPijuOrderNum = 0D;
    private Double aggCourierAmount = 0D;
    private Double aggCourierOrderNum = 0D;
    private Double aggExpressAmount = 0D;
    private Double aggExpressOrderNum = 0D;
    private Double aggRechargeAmount = 0D;
    private Double aggRechargeOrderNum = 0D;

    //流水分析数据
    private Double accCashAmount = 0D;//现金收取金额
    private Double accCashOrderNum = 0D;
    private Double accAlipayAmount = 0D;
    private Double accAlipayOrderNum = 0D;
    private Double accWxAmount = 0D;
    private Double accWxOrderNum = 0D;
    private Double accMemberAccount = 0D;
    private Double accMemberOrderNum = 0D;
    private Double accScAmount = 0D;
    private Double accScOrderNum = 0D;
    private Double accBankcardAmount = 0D;
    private Double accBankcardOrderNum = 0D;


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


    public Double getGrossProfit() {
        if (grossProfit == null){
            return 0D;
        }
        return grossProfit;
    }

    public void setGrossProfit(Double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public Double getAggPosAmount() {
        if (aggPosAmount == null){
            return 0D;
        }
        return aggPosAmount;
    }

    public void setAggPosAmount(Double aggPosAmount) {
        this.aggPosAmount = aggPosAmount;
    }

    public Double getAggPosOrderNum() {
        if (aggPosOrderNum == null){
            return 0D;
        }
        return aggPosOrderNum;
    }

    public void setAggPosOrderNum(Double aggPosOrderNum) {
        this.aggPosOrderNum = aggPosOrderNum;
    }

    public Double getAggScAmount() {
        if (aggScAmount == null){
            return 0D;
        }
        return aggScAmount;
    }

    public void setAggScAmount(Double aggScAmount) {
        this.aggScAmount = aggScAmount;
    }

    public Double getAggScOrderNum() {
        if (aggScOrderNum == null){
            return 0D;
        }
        return aggScOrderNum;
    }

    public void setAggScOrderNum(Double aggScOrderNum) {
        this.aggScOrderNum = aggScOrderNum;
    }

    public Double getAggLaundryAmount() {
        if (aggLaundryAmount == null){
            return 0D;
        }
        return aggLaundryAmount;
    }

    public void setAggLaundryAmount(Double aggLaundryAmount) {
        this.aggLaundryAmount = aggLaundryAmount;
    }

    public Double getAggLaundryOrderNum() {
        if (aggLaundryOrderNum == null){
            return 0D;
        }
        return aggLaundryOrderNum;
    }

    public void setAggLaundryOrderNum(Double aggLaundryOrderNum) {
        this.aggLaundryOrderNum = aggLaundryOrderNum;
    }

    public Double getAggPijuAmount() {
        if (aggPijuAmount == null){
            return 0D;
        }
        return aggPijuAmount;
    }

    public void setAggPijuAmount(Double aggPijuAmount) {
        this.aggPijuAmount = aggPijuAmount;
    }

    public Double getAggPijuOrderNum() {
        if (aggPijuOrderNum == null){
            return 0D;
        }
        return aggPijuOrderNum;
    }

    public void setAggPijuOrderNum(Double aggPijuOrderNum) {
        this.aggPijuOrderNum = aggPijuOrderNum;
    }

    public Double getAggCourierAmount() {
        if (aggCourierAmount == null){
            return 0D;
        }
        return aggCourierAmount;
    }

    public void setAggCourierAmount(Double aggCourierAmount) {
        this.aggCourierAmount = aggCourierAmount;
    }

    public Double getAggCourierOrderNum() {
        if (aggCourierOrderNum == null){
            return 0D;
        }
        return aggCourierOrderNum;
    }

    public void setAggCourierOrderNum(Double aggCourierOrderNum) {
        this.aggCourierOrderNum = aggCourierOrderNum;
    }

    public Double getAggExpressAmount() {
        if (aggExpressAmount == null){
            return 0D;
        }
        return aggExpressAmount;
    }

    public void setAggExpressAmount(Double aggExpressAmount) {
        this.aggExpressAmount = aggExpressAmount;
    }

    public Double getAggExpressOrderNum() {
        if (aggExpressOrderNum == null){
            return 0D;
        }
        return aggExpressOrderNum;
    }

    public void setAggExpressOrderNum(Double aggExpressOrderNum) {
        this.aggExpressOrderNum = aggExpressOrderNum;
    }

    public Double getAggRechargeAmount() {
        if (aggRechargeAmount == null){
            return 0D;
        }
        return aggRechargeAmount;
    }

    public void setAggRechargeAmount(Double aggRechargeAmount) {
        this.aggRechargeAmount = aggRechargeAmount;
    }

    public Double getAggRechargeOrderNum() {
        if (aggRechargeOrderNum == null){
            return 0D;
        }
        return aggRechargeOrderNum;
    }

    public void setAggRechargeOrderNum(Double aggRechargeOrderNum) {
        this.aggRechargeOrderNum = aggRechargeOrderNum;
    }

    public Double getAccCashAmount() {
        if (accCashAmount == null){
            return 0D;
        }
        return accCashAmount;
    }

    public void setAccCashAmount(Double accCashAmount) {
        this.accCashAmount = accCashAmount;
    }

    public Double getAccCashOrderNum() {
        if (accCashOrderNum == null){
            return 0D;
        }
        return accCashOrderNum;
    }

    public void setAccCashOrderNum(Double accCashOrderNum) {
        this.accCashOrderNum = accCashOrderNum;
    }

    public Double getAccAlipayAmount() {
        if (accAlipayAmount == null){
            return 0D;
        }
        return accAlipayAmount;
    }

    public void setAccAlipayAmount(Double accAlipayAmount) {
        this.accAlipayAmount = accAlipayAmount;
    }

    public Double getAccAlipayOrderNum() {
        if (accAlipayOrderNum == null){
            return 0D;
        }
        return accAlipayOrderNum;
    }

    public void setAccAlipayOrderNum(Double accAlipayOrderNum) {
        this.accAlipayOrderNum = accAlipayOrderNum;
    }

    public Double getAccWxAmount() {
        if (accWxAmount == null){
            return 0D;
        }
        return accWxAmount;
    }

    public void setAccWxAmount(Double accWxAmount) {
        this.accWxAmount = accWxAmount;
    }

    public Double getAccWxOrderNum() {
        if (accWxOrderNum == null){
            return 0D;
        }
        return accWxOrderNum;
    }

    public void setAccWxOrderNum(Double accWxOrderNum) {
        this.accWxOrderNum = accWxOrderNum;
    }

    public Double getAccMemberAccount() {
        if (accMemberAccount == null){
            return 0D;
        }
        return accMemberAccount;
    }

    public void setAccMemberAccount(Double accMemberAccount) {
        this.accMemberAccount = accMemberAccount;
    }

    public Double getAccMemberOrderNum() {
        if (accMemberOrderNum == null){
            return 0D;
        }
        return accMemberOrderNum;
    }

    public void setAccMemberOrderNum(Double accMemberOrderNum) {
        this.accMemberOrderNum = accMemberOrderNum;
    }

    public Double getAccScAmount() {
        if (accScAmount == null){
            return 0D;
        }
        return accScAmount;
    }

    public void setAccScAmount(Double accScAmount) {
        this.accScAmount = accScAmount;
    }

    public Double getAccScOrderNum() {
        if (accScOrderNum == null){
            return 0D;
        }
        return accScOrderNum;
    }

    public void setAccScOrderNum(Double accScOrderNum) {
        this.accScOrderNum = accScOrderNum;
    }

    public Double getAccBankcardAmount() {
        if (accBankcardAmount == null){
            return 0D;
        }
        return accBankcardAmount;
    }

    public void setAccBankcardAmount(Double accBankcardAmount) {
        this.accBankcardAmount = accBankcardAmount;
    }

    public Double getAccBankcardOrderNum() {
        if (accBankcardOrderNum == null){
            return 0D;
        }
        return accBankcardOrderNum;
    }

    public void setAccBankcardOrderNum(Double accBankcardOrderNum) {
        this.accBankcardOrderNum = accBankcardOrderNum;
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
