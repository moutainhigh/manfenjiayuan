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

    private AccItem cashItem;
    private AccItem alipayItem;
    private AccItem wxItem;
    private AccItem accountItem;
    private AccItem bankItem;
    private AccItem ruleItem;

    public AccItem getCashItem() {
        return cashItem;
    }

    public void setCashItem(AccItem cashItem) {
        this.cashItem = cashItem;
    }

    public AccItem getAlipayItem() {
        return alipayItem;
    }

    public void setAlipayItem(AccItem alipayItem) {
        this.alipayItem = alipayItem;
    }

    public AccItem getWxItem() {
        return wxItem;
    }

    public void setWxItem(AccItem wxItem) {
        this.wxItem = wxItem;
    }

    public AccItem getAccountItem() {
        return accountItem;
    }

    public void setAccountItem(AccItem accountItem) {
        this.accountItem = accountItem;
    }

    public AccItem getBankItem() {
        return bankItem;
    }

    public void setBankItem(AccItem bankItem) {
        this.bankItem = bankItem;
    }

    public AccItem getRuleItem() {
        return ruleItem;
    }

    public void setRuleItem(AccItem ruleItem) {
        this.ruleItem = ruleItem;
    }

    private void reset(){
        cashItem = AnalysisHelper.constructorAccItem(WayType.CASH, "现金");
        alipayItem = AnalysisHelper.constructorAccItem(WayType.CASH, "支付宝");
        wxItem = AnalysisHelper.constructorAccItem(WayType.CASH, "微信");
        accountItem = AnalysisHelper.constructorAccItem(WayType.CASH, "平台账户");
        bankItem = AnalysisHelper.constructorAccItem(WayType.CASH, "银行卡");
        ruleItem = AnalysisHelper.constructorAccItem(WayType.CASH, "卡券");
    }

    /**
     * 日结/交接班流水分析
     * */
    public void initWithDailysettleAccItems(List<AccItem> entityList){
        reset();

        if (entityList == null || entityList.size() < 1){
            return;
        }

        for (AccItem entity : entityList){
            //现金
            if (entity.getPayType().equals(WayType.CASH)){
                cashItem = entity;
            }
            //支付宝扫码支付
            else if (entity.getPayType().equals(WayType.ALI_F2F)){
                alipayItem = entity;
            }
            //微信扫码付
            else if (entity.getPayType().equals(WayType.WX_F2F)){
                wxItem = entity;
            }
            //会员
            else if (entity.getPayType().equals(WayType.VIP)){
                accountItem = entity;
            }
            //银联
            else if (entity.getPayType().equals(WayType.BANKCARD)){
                bankItem = entity;
            }
            //卡券&规则
            else if (entity.getPayType().equals(WayType.RULES)){
                ruleItem = entity;
            }
        }
    }

}
