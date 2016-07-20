package com.mfh.litecashier.bean.wrapper;


/**
 * 交接班/日结
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class AnalysisItemWrapper implements java.io.Serializable{

    private String caption;//业务类型/支付类型
    private Double orderNum;//数量
    private Double turnover;//金额

    private boolean isShowIndex;//是否显示序号

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Double getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Double orderNum) {
        this.orderNum = orderNum;
    }

    public Double getTurnover() {
        return turnover;
    }

    public void setTurnover(Double turnover) {
        this.turnover = turnover;
    }

    public boolean isShowIndex() {
        return isShowIndex;
    }

    public void setIsShowIndex(boolean isShowIndex) {
        this.isShowIndex = isShowIndex;
    }
}
