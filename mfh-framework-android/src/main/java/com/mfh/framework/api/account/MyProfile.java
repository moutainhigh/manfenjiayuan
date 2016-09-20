package com.mfh.framework.api.account;

/**
 * 用户信息
 * Created by bingshanguxue on 2015/5/14.
 * {@link com.mfh.framework.api.UserApi#URL_MY_PROFILE}
 */
public class MyProfile implements java.io.Serializable{
    private Long humanId;
    private Double amount;//账户余额
    private Long score;//会员积分
    private Long favoriteNum;//我的收藏
    private Long waitPayNum;//代付款
    private Long waitReceiveNum;//待收货
    private Long waitPraiseNum;//待评价
    private Long serviceOrderNum;//预约服务
    private Long shoppingCartNum;//购物车商品
    private Long cardCouponsNum;//卡包
    private String defaultStock;//默认收货网点
    private String defaultSubids;//常住小区

    public Long getHumanId() {
        return humanId;
    }

    public void setHumanId(Long humanId) {
        this.humanId = humanId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Long getFavoriteNum() {
        return favoriteNum;
    }

    public void setFavoriteNum(Long favoriteNum) {
        this.favoriteNum = favoriteNum;
    }

    public Long getWaitPayNum() {
        return waitPayNum;
    }

    public void setWaitPayNum(Long waitPayNum) {
        this.waitPayNum = waitPayNum;
    }

    public Long getWaitReceiveNum() {
        return waitReceiveNum;
    }

    public void setWaitReceiveNum(Long waitReceiveNum) {
        this.waitReceiveNum = waitReceiveNum;
    }

    public Long getWaitPraiseNum() {
        return waitPraiseNum;
    }

    public void setWaitPraiseNum(Long waitPraiseNum) {
        this.waitPraiseNum = waitPraiseNum;
    }

    public Long getServiceOrderNum() {
        return serviceOrderNum;
    }

    public void setServiceOrderNum(Long serviceOrderNum) {
        this.serviceOrderNum = serviceOrderNum;
    }

    public Long getShoppingCartNum() {
        return shoppingCartNum;
    }

    public void setShoppingCartNum(Long shoppingCartNum) {
        this.shoppingCartNum = shoppingCartNum;
    }

    public Long getCardCouponsNum() {
        return cardCouponsNum;
    }

    public void setCardCouponsNum(Long cardCouponsNum) {
        this.cardCouponsNum = cardCouponsNum;
    }

    public String getDefaultStock() {
        return defaultStock;
    }

    public void setDefaultStock(String defaultStock) {
        this.defaultStock = defaultStock;
    }

    public String getDefaultSubids() {
        return defaultSubids;
    }

    public void setDefaultSubids(String defaultSubids) {
        this.defaultSubids = defaultSubids;
    }
}
