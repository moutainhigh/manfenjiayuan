package com.mfh.framework.api;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品经营分析统计数据
 * Created by bingshanguxue on 8/10/16.
 */
public class ProductAggDate implements Serializable{
    private Date aggDate;//日结日期
    private Double productNum = 0D;//商品数量
    private Double turnover = 0D;//营业额

    private String tenantSkuIdWrapper;//名称

    public Date getAggDate() {
        return aggDate;
    }

    public void setAggDate(Date aggDate) {
        this.aggDate = aggDate;
    }

    public Double getProductNum() {
        if (productNum == null){
            return 0D;
        }
        return productNum;
    }

    public void setProductNum(Double productNum) {
        this.productNum = productNum;
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

    public String getTenantSkuIdWrapper() {
        return tenantSkuIdWrapper;
    }

    public void setTenantSkuIdWrapper(String tenantSkuIdWrapper) {
        this.tenantSkuIdWrapper = tenantSkuIdWrapper;
    }
}
