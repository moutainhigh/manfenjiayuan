package com.manfenjiayuan.pda_supermarket.bean;

import java.io.Serializable;

/**
 * 优惠券
 * Created by Nat.ZZN(bingshanguxue) on 15/9/30.
 */
public class CoupBean implements Serializable {

    private Long myCouponsId;//我领用的卡券编号,不是卡券编号本身
//    private String tenantName;//租户名字
//    private String logoPicPath;//网点/店铺图片

    private Long id;//优惠券ID
    private String title; //卡券标题
    private String subTitle; //副标题
    //    private Integer shareBits = 0; //分享标志集 01：可分享 10：可转赠
    private String color; //卡券颜色，rgb值 如#FFFFFF
    private Double discount; //卡券类型为折扣券时，折扣 是大于0.1且小于10的相对数字；类型为代金券时，为减免金额，大于0.01的绝对数字；
//    private Double conditionLimit; //抵扣条件，消费满多少元可用。如不填写则默认：消费满任意金额可用
//
//    //目前最新设计（2015-09-29），只有类型1，并且起始日期设置是在专门的投放场景表中定义。
//    private Integer validDateType = 0; //有效期类型：0-固定日期(参见投放场景表中的日期维度) 1-领取N天后，有效期M天
//    //validDateType=1时需要
//    private Integer startNum; //领取后生效天数，<=0 则立刻生效
//    private Integer validNum; //有效期天数，<=0则一直有效
//    private Integer status = 0;//CardCouponsConstants.CARD_STATUS_PASS; //卡券状态：0-初始状态审核中  1-审核通过待投放   2-已投放
//    private Long quantum;//库存数量
//    private Integer limitCoupons = 1; //领券限制，每个用户领券上限，如不填，则默认为1
//    private Long qrCodeId;//二维码Id
//    private String detail; //优惠详情
//    private String useNotice; //使用须知
//    private String customerServicePhone; //客服电话

    private Integer cardType = 0;//CardCouponsConstants.CARD_TYPE_VOUCHER; //卡券类型：0-折扣券 1-代金券 2-礼品券 3-团购券 4-优惠券
    private Long tenantId; //所属租户


    //是否被选中
    private boolean isSelected;

    public Long getMyCouponsId() {
        return myCouponsId;
    }

    public void setMyCouponsId(Long myCouponsId) {
        this.myCouponsId = myCouponsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Integer getCardType() {
        return cardType;
    }

    public void setCardType(Integer cardType) {
        this.cardType = cardType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
