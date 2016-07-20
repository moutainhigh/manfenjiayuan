package com.mfh.litecashier.bean.wrapper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.WayType;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.bean.MarketRules;
import com.mfh.litecashier.bean.RuleBean;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.ArrayList;
import java.util.List;

/**
 * 收银订单
 * Created by Administrator on 2015/5/14.
 */
public class CashierOrderInfo implements java.io.Serializable {
    private final static int MAX_BODY_LENGTH = 20;

    //商品明细, format like: [{skuId:..,bcount:...,price:...,whereId:...}]
    private JSONArray productsInfo;
    //优惠券信息
    private JSONObject couponsInfo;
    //产品sku编号（查询优惠券）
    private JSONArray proSkuIds;
    //产品名称
    private String productNames;
    //数量
    private Double bCount = 0D;
    //总金额(零售价)，
    private Double retailAmount = 0D;
    //总金额(成交价)
    private Double dealAmount = 0D;
    //折扣价(会员优惠)
    private Double discountAmount = 0D;
    //折扣率
    private Double discountRate = 0D;
    //超市商家
    private Long companyId;
    //订单明细
    private List<PosOrderItemEntity> entityList;

    //以下字段支付的时候才会赋值
    //业务类型
    private Integer bizType;
    //订单条码
    private String orderBarcode;
    //订单编号
    private String orderId;
    private String subject;
    private String body;
    //实际支付金额（包含找零金额）
    private Double paidAmount = 0D;
    //规则or卡券
    private MarketRules marketRules;
    //规则
    private String ruleIds;
    //优惠券卡券领用号(已使用)
    private String couponsIds;
    //折扣价(卡券优惠)
    private Double couponDiscountAmount = 0D;
    //支付类型
    private Integer payType = WayType.NA;
    private Human vipMember;//会员

    //当前应该支付金额, >0,表示应收金额; <0,表示找零金额
    private Double handleAmount = 0D;

    private int status = PosOrderEntity.ORDER_STATUS_INIT;

    public JSONArray getProductsInfo() {
        return productsInfo;
    }

    public void setProductsInfo(JSONArray productsInfo) {
        this.productsInfo = productsInfo;
    }

    public String getProductNames() {
        return productNames;
    }

    public void setProductNames(String productNames) {
        this.productNames = productNames;
    }

    public JSONArray getProSkuIds() {
        return proSkuIds;
    }

    public void setProSkuIds(JSONArray proSkuId) {
        this.proSkuIds = proSkuId;
    }

    public Double getbCount() {
        return bCount;
    }

    public void setbCount(Double bCount) {
        this.bCount = bCount;
    }

    public Double getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(Double dealAmount) {
        this.dealAmount = dealAmount;
    }

    public Double getRetailAmount() {
        return retailAmount;
    }

    public void setRetailAmount(Double retailAmount) {
        this.retailAmount = retailAmount;
    }


    public JSONObject getCouponsInfo() {
        return couponsInfo;
    }

    public void setCouponsInfo(JSONObject couponsInfo) {
        this.couponsInfo = couponsInfo;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<PosOrderItemEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<PosOrderItemEntity> entityList) {
        this.entityList = entityList;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public String getOrderBarcode() {
        return orderBarcode;
    }

    public void setOrderBarcode(String orderBarcode) {
        this.orderBarcode = orderBarcode;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Double getHandleAmount() {
        return handleAmount;
    }

    public void setHandleAmount(Double handleAmount) {
        this.handleAmount = handleAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Human getVipMember() {
        return this.vipMember;
    }

    public void setVipMember(Human vipMember) {
        this.vipMember = vipMember;
    }

    public MarketRules getMarketRules() {
        return marketRules;
    }

    public void setMarketRules(MarketRules marketRules) {
        this.marketRules = marketRules;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

    public Double getCouponDiscountAmount() {
        return couponDiscountAmount;
    }

    public void setCouponDiscountAmount(Double couponDiscountAmount) {
        this.couponDiscountAmount = couponDiscountAmount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private void clear() {
        if (this.productsInfo == null) {
            this.productsInfo = new JSONArray();
        } else {
            this.productsInfo.clear();
        }
        if (this.proSkuIds == null) {
            this.proSkuIds = new JSONArray();
        } else {
            this.proSkuIds.clear();
        }
        if (this.couponsInfo == null) {
            this.couponsInfo = new JSONObject();
        } else {
            this.couponsInfo.clear();
        }
        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        } else {
            this.entityList.clear();
        }
        this.productNames = "";
        this.bCount = 0D;
        this.retailAmount = 0D;
        this.dealAmount = 0D;
        this.discountAmount = 0D;
        this.discountRate = 0D;

        this.companyId = 0L;

        this.payType = WayType.NA;
        this.paidAmount = 0D;
//        this.handleAmount = 0D;
        this.marketRules = null;
        this.ruleIds = "";
        this.couponsIds = "";
        this.couponDiscountAmount = 0D;
        this.vipMember = null;
        this.status = PosOrderEntity.ORDER_STATUS_INIT;
    }

    /**
     * 收银
     */
    public synchronized void init(List<PosOrderItemEntity> entityList) {
        clear();

        if (entityList != null){
            this.entityList.addAll(entityList);
        }
        StringBuilder sb = new StringBuilder();

        for (PosOrderItemEntity entity : this.entityList) {

            JSONObject item = new JSONObject();
            item.put("goodsId", entity.getGoodsId());
            item.put("skuId", entity.getProSkuId());
            item.put("bcount", entity.getBcount());
            item.put("price", entity.getCostPrice());
            item.put("whereId", MfhLoginService.get().getCurOfficeId());//网点ID,netid,

            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(entity.getName());

            productsInfo.add(item);
            proSkuIds.add(entity.getProSkuId());
            bCount += entity.getBcount();
            dealAmount += entity.getFinalAmount();
            retailAmount += entity.getAmount();

            companyId = entity.getProviderId();
        }
        productNames = sb.toString();
        if (productNames.length() > MAX_BODY_LENGTH) {
            productNames = productNames.substring(0, MAX_BODY_LENGTH);
        }

        couponsInfo.put("officeId", MfhLoginService.get().getCurOfficeId());
        couponsInfo.put("proSkuIds", proSkuIds);
        couponsInfo.put("orderAmount", retailAmount);

        discountAmount = retailAmount - dealAmount;
        if (retailAmount == 0D){
            discountRate = Double.valueOf(String.valueOf(Integer.MAX_VALUE));
        }
        else{
            discountRate = dealAmount / retailAmount;
        }
    }

    /**
     * 结算收银订单
     * @param payType 已支付金额的支付类型
     * @param paidAmount 已支付金额
     * TOTO,修改支付类型和已结算金额
     */
    public void initCashierSetle(String orderBarcode, Integer bizType, String orderId,
                                 String subject, String body, Human vipMember,
                                 Integer payType, Double paidAmount, List<PosOrderItemEntity> items,
                                 int status) {
        clear();

        this.orderBarcode = orderBarcode;
        this.bizType = bizType;
        this.orderId = orderId;
        this.subject = subject;
        this.vipMember = vipMember;

        this.payType = payType;
        this.paidAmount = paidAmount;
//        this.handleAmount = 0D;
        this.marketRules = null;
        this.ruleIds = "";
        this.couponsIds = "";
        this.couponDiscountAmount = 0D;

        if (items != null){
            this.entityList.addAll(items);
        }
        StringBuilder sb = new StringBuilder();

        for (PosOrderItemEntity entity : this.entityList) {
            JSONObject item = new JSONObject();
            item.put("goodsId", entity.getGoodsId());
            item.put("skuId", entity.getProSkuId());
            item.put("bcount", entity.getBcount());
            item.put("price", entity.getCostPrice());
            item.put("whereId", MfhLoginService.get().getCurOfficeId());//网点ID,netid,

            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(entity.getName());

            productsInfo.add(item);
            proSkuIds.add(entity.getProSkuId());
            bCount += entity.getBcount();
            dealAmount += entity.getFinalAmount();
            retailAmount += entity.getAmount();

            companyId = entity.getProviderId();
        }
        productNames = sb.toString();
        if (productNames.length() > MAX_BODY_LENGTH) {
            productNames = productNames.substring(0, MAX_BODY_LENGTH);
        }

        couponsInfo.put("officeId", MfhLoginService.get().getCurOfficeId());
        couponsInfo.put("proSkuIds", proSkuIds);
        couponsInfo.put("orderAmount", retailAmount);

        if (StringUtils.isEmpty(body)) {
            this.body = this.productNames;
        } else {
            this.body = body;
        }

        discountAmount = retailAmount - dealAmount;
        if (retailAmount == 0D){
            discountRate = Double.valueOf(String.valueOf(Integer.MAX_VALUE));
        }
        else{
            discountRate = dealAmount / retailAmount;
        }
        this.status = status;

        calculateCharge();
    }

    /**
     * 初始化快捷支付
     * @param bizType 业务类型
     * @param orderId 订单编号（日结：机器设备号_日期）
     * @param paidAmount 已支付金额
     * TOTO,修改支付类型和已结算金额
     */
    public void initQuickPayment(Integer bizType, String orderBarcode, String orderId,
                                 String subject, String body, Human vipMember,
                                 Double retailAmount, Double dealAmount) {
        clear();

        this.orderBarcode = orderBarcode;
        this.bizType = bizType;
        this.orderId = orderId;
        this.subject = subject;
        this.vipMember = vipMember;
        this.retailAmount = retailAmount;
        this.dealAmount = dealAmount;

        couponsInfo.put("officeId", MfhLoginService.get().getCurOfficeId());
        couponsInfo.put("proSkuIds", proSkuIds);
        couponsInfo.put("orderAmount", retailAmount);

        if (StringUtils.isEmpty(body)) {
            this.body = this.productNames;
        } else {
            this.body = body;
        }

        discountAmount = retailAmount - dealAmount;
        if (retailAmount == 0D){
            discountRate = Double.valueOf(String.valueOf(Integer.MAX_VALUE));
        }
        else{
            discountRate = dealAmount / retailAmount;
        }

        calculateCharge();
    }

    /**
     * 结算
     */
    public void initSetle(Integer bizType, String orderBarcode, String orderId,
                          String subject, String body, Human vipMember) {
        this.bizType = bizType;
        this.orderBarcode = orderBarcode;
        this.orderId = orderId;
        this.subject = subject;
        if (StringUtils.isEmpty(body)) {
            this.body = this.productNames;
        } else {
            this.body = body;
        }
        this.vipMember = vipMember;

        this.payType = WayType.NA;
        this.paidAmount = 0D;
//        this.handleAmount = 0D;
        this.marketRules = null;
        this.ruleIds = "";
        this.couponsIds = "";
        this.couponDiscountAmount = 0D;

        calculateCharge();
    }

    public void couponPrivilege(MarketRules marketRules){
        this.marketRules = marketRules;

        if (this.marketRules == null){
            this.ruleIds = "";
        }
        else{
            StringBuilder sb = new StringBuilder();

            List<RuleBean> ruleBeans = marketRules.getRuleBeans();
            if (ruleBeans != null && ruleBeans.size() > 0){
                int len = ruleBeans.size();
                for (int i = 0; i < len; i++) {
                    RuleBean bean = ruleBeans.get(i);

                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(bean.getId());
                }
            }

            this.ruleIds = sb.toString();
        }
    }

    /**
     * 抵用优惠券,优惠券(代金券)优惠
     */
    public void coupon(Double amount, String couponsIds) {
        this.couponDiscountAmount = dealAmount - amount;
        this.couponsIds = couponsIds;

        calculateCharge();
    }

    /**
     * VIP特权
     */
    public void vipPrivilege(Human vipMember) {
        this.vipMember = vipMember;

        calculateCharge();
    }

    //支付完成
    public void paid(Integer payType, Double amount) {
        //ZLogger.d(String.format("paid.payType(%d|%d)", this.payType, payType));
        this.payType = this.payType | payType;
        //ZLogger.d("paid.payType" + this.payType);
        this.paidAmount += amount;

        calculateCharge();
    }

    public void calculateCharge() {
        Double amount = retailAmount - discountAmount - couponDiscountAmount - paidAmount;
        //实际场景中应付金额不会小于1分钱，所以这里要保留两位小数
        this.handleAmount = Double.valueOf(String.format("%.2f", amount));
    }

    //支付授权码(条码)
    public String generateOrderInfo(String operatorId, Long storeId, String terminalId,
                                    String sellerId, Double paidAmount, String authCode) {
        JSONObject orderInfo = new JSONObject();
        orderInfo.put("out_trade_no", String.format("%s_%d", orderId, System.currentTimeMillis()));//1_100014_1445935035219
        orderInfo.put("scene", "bar_code");
        orderInfo.put("auth_code", authCode);
        orderInfo.put("total_amount", MUtils.formatDouble(paidAmount, ""));
//        orderInfo.put("discountable_amount", MStringUtil.formatAmount(discountableAmount));
        orderInfo.put("subject", subject);
        orderInfo.put("body", body);
        orderInfo.put("operator_id", operatorId);//商户操作员编号
        orderInfo.put("store_id", storeId);//商户门店编号
        orderInfo.put("terminal_id", terminalId);//设备编号
        orderInfo.put("seller_id", sellerId);

        return orderInfo.toJSONString();
    }


    /**
     * 生成客显数据
     * */
    public String generateDisplayData(){
        JSONObject displayObject = new JSONObject();

        return displayObject.toJSONString();
    }
}
