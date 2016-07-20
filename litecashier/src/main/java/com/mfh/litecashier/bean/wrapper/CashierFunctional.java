package com.mfh.litecashier.bean.wrapper;

import com.mfh.comn.bean.ILongId;
import com.mfh.litecashier.bean.PosCategory;

import java.io.Serializable;

/**
 * 服务台－－功能菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/9/2.
 */
public class CashierFunctional implements ILongId, Serializable {
    public static final Long OPTION_ID_FRESH        = 1L;//生鲜
    public static final Long OPTION_ID_BAKERY = 2L;//烘培
    public static final Long OPTION_ID_FRUIT = 3L;//水果
    public static final Long OPTION_ID_BREAKFAST = 4L;//早餐
    public static final Long OPTION_ID_MILK = 5L;//鲜奶
    public static final Long OPTION_ID_LAUNDRY = 6L;//洗衣
    public static final Long OPTION_ID_PACKAGE = 7L;//包裹(取快递)
    public static final Long OPTION_ID_COURIER = 8L;//快递代收
    public static final Long OPTION_ID_EXPRESS = 9L;//寄快递
    public static final Long OPTION_ID_MEMBER_CARD = 10L;//会员卡（开卡）
    public static final Long OPTION_ID_RECHARGE = 11L;//充值
    public static final Long OPTION_ID_FEEDPAPER= 12L;//走纸
    public static final Long OPTION_ID_RETURN_GOODS= 13L;//退货
    public static final Long OPTION_ID_MONEY_BOX= 14L;//钱箱
    public static final Long OPTION_ID_DEFECTIVE= 15L;//报损
    public static final Long OPTION_ID_PRIVATE= 16L;//我的
    public static final Long OPTION_ID_PAYBACK= 17L;//返货
    public static final Long OPTION_ID_RESET_ORDER= 18L;//重置订单
    public static final Long OPTION_ID_PURCHASE_SEND= 19L;//采购订单
    public static final Long OPTION_ID_PURCHASE_RECEIPT= 20L;//采购收货
    public static final Long OPTION_ID_PURCHASE_RETURN= 21L;//采购退货
    public static final Long OPTION_ID_MALL= 22L;//商城
    public static final Long OPTION_ID_RECEIVE_GOODS= 23L;//商品领取
    public static final Long OPTION_ID_STORE_PROMOTION= 24L;//门店促销
    public static final Long OPTION_ID_PURCHASE_INTELLIGENT= 25L;//智能订货
    public static final Long OPTION_ID_INVENTORY_TRANS_IN= 26L;//调拨－调入
    public static final Long OPTION_ID_INVENTORY_TRANS_OUT= 27L;//调拨－调出

    private int type = 0;//0:local;1-category
    private Long id;//编号
    private String nameCn;//名称

    private int resId;//本地图片资源
    private String imageUrl;//

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    //local
    public static CashierFunctional generate(Long id, String nameCn, int resId){
        CashierFunctional entity = new CashierFunctional();
        entity.type = 0;
        entity.id = id;
        entity.resId = resId;
        entity.nameCn = nameCn;
        return entity;
    }

    public static CashierFunctional generate(PosCategory category){
        CashierFunctional entity = new CashierFunctional();
        entity.type = 1;
        entity.id = category.getId();
        entity.nameCn = category.getNameCn();
        entity.imageUrl = category.getImageUrl();
        return entity;
    }


}
