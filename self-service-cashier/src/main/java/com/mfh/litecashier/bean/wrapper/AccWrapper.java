package com.mfh.litecashier.bean.wrapper;


import com.mfh.framework.api.constant.WayType;
import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.utils.AnalysisHelper;

import java.util.List;

/**
 * 交接班/日结流水分析数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class AccWrapper implements java.io.Serializable{

    private AccItem cash = new AccItem();
    private AccItem alipay = new AccItem();
    private AccItem wx = new AccItem();
    private AccItem account = new AccItem();
    private AccItem bank = new AccItem();
    private AccItem rule = new AccItem();

    public AccItem getCash() {
        return cash;
    }

    public void setCash(AccItem cash) {
        this.cash = cash;
    }

    public AccItem getAlipay() {
        return alipay;
    }

    public void setAlipay(AccItem alipay) {
        this.alipay = alipay;
    }

    public AccItem getWx() {
        return wx;
    }

    public void setWx(AccItem wx) {
        this.wx = wx;
    }

    public AccItem getAccount() {
        return account;
    }

    public void setAccount(AccItem account) {
        this.account = account;
    }

    public AccItem getBank() {
        return bank;
    }

    public void setBank(AccItem bank) {
        this.bank = bank;
    }

    public AccItem getRule() {
        return rule;
    }

    public void setRule(AccItem rule) {
        this.rule = rule;
    }

    private void reset(){
        cash = AnalysisHelper.generateAccItem(WayType.CASH, "现金");
        alipay = AnalysisHelper.generateAccItem(WayType.CASH, "支付宝扫码付");
        wx = AnalysisHelper.generateAccItem(WayType.CASH, "微信扫码付");
        account = AnalysisHelper.generateAccItem(WayType.CASH, "平台账户");
        bank = AnalysisHelper.generateAccItem(WayType.CASH, "银行卡");
        rule = AnalysisHelper.generateAccItem(WayType.CASH, "卡券");
    }

    /**
     * 日结/交接班流水分析
     * */
    public void initialize(List<AccItem> entityList){
        reset();

        if (entityList == null || entityList.size() < 1){
            return;
        }

        for (AccItem entity : entityList){
            //现金
            if (entity.getPayType().equals(WayType.CASH)){
                cash = entity;
            }
            //支付宝扫码支付
            else if (entity.getPayType().equals(WayType.ALI_F2F)){
                alipay = entity;
            }
            //微信扫码付
            else if (entity.getPayType().equals(WayType.WX_F2F)){
                wx = entity;
            }
            //会员
            else if (entity.getPayType().equals(WayType.VIP)){
                account = entity;
            }
            //银联
            else if (entity.getPayType().equals(WayType.BANKCARD)){
                bank = entity;
            }
            //卡券&规则
            else if (entity.getPayType().equals(WayType.RULES)){
                rule = entity;
            }
        }
    }

}
