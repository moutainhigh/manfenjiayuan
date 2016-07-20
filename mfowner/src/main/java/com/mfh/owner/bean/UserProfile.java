package com.mfh.owner.bean;

/**
 * 用户信息
 * Created by Administrator on 2015/5/14.
 *
 */
public class UserProfile implements java.io.Serializable{
    private String humanId;
    private String amount;//账户余额
    private String score;//会员积分
    private String favoriteNum;//我的收藏
    private String waitPayNum;//代付款
    private String waitReceiveNum;//待收货
    private String waitPraiseNum;//待评价
    private String serviceOrderNum;//预约服务
    private String shoppingCartNum;//购物车商品
    private String cardCouponsNum;//卡包
    private String defaultStock;//默认收货网点
    private String defaultSubids;//常住小区

    public UserProfile(){
    }


    public String getHumanId() {
        return humanId;
    }

    public void setHumanId(String humanId) {
        this.humanId = humanId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getFavoriteNum() {
        return favoriteNum;
    }

    public void setFavoriteNum(String favoriteNum) {
        this.favoriteNum = favoriteNum;
    }

    public String getWaitPayNum() {
        return waitPayNum;
    }

    public void setWaitPayNum(String waitPayNum) {
        this.waitPayNum = waitPayNum;
    }

    public String getWaitReceiveNum() {
        return waitReceiveNum;
    }

    public void setWaitReceiveNum(String waitReceiveNum) {
        this.waitReceiveNum = waitReceiveNum;
    }

    public String getWaitPraiseNum() {
        return waitPraiseNum;
    }

    public void setWaitPraiseNum(String waitPraiseNum) {
        this.waitPraiseNum = waitPraiseNum;
    }

    public String getServiceOrderNum() {
        return serviceOrderNum;
    }

    public void setServiceOrderNum(String serviceOrderNum) {
        this.serviceOrderNum = serviceOrderNum;
    }

    public String getShoppingCartNum() {
        return shoppingCartNum;
    }

    public void setShoppingCartNum(String shoppingCartNum) {
        this.shoppingCartNum = shoppingCartNum;
    }

    public String getCardCouponsNum() {
        return cardCouponsNum;
    }

    public void setCardCouponsNum(String cardCouponsNum) {
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
