package com.manfenjiayuan.pda_supermarket.database.entity;

import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.utils.StringUtils;

/**
 * POS--商品--库存
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name = "tb_pda_procuct_v00002")
public class PosProductEntity extends MfhEntity<Long> implements ILongId {
    //SPU:Standard Product Unit
//    private Long id;//最小商品库存单元编号
    private Long productId; //产品spu编号
    private String name = "";    // 商品名称(包含规格)
    private String skuName = "";//商品名称
    private String shortName = "";//商品规格
    private String namePinyin = "";//全拼
    private String abbreviation = "";//拼音缩写
    private String nameSortLetter = "";//排序字段

    //SKU:Stock Keeping Unit
    private Long proSkuId;  //产品sku编号
    private String barcode = ""; //最小商品库存单元的条形码
    private Integer priceType = PriceType.PIECE;//价格类型0-计件 1-计重
    private Double costPrice = 0D;  // 商品价格
    private String unit = "";    // 单位，如箱、瓶
    private Double packageNum = 0D;//箱规

    private Long tenantId;  // 租户信息，即微超公司id
    private Double quantity = 0D;   // 商品数量(库存)

    private Long providerId;//商品供应商编号

    /**
     * 当云端下架或删除一个商品时，并未真正删除商品，而是相当于把status修改成0。
     * 如果是物理删除目前没有办法增量同步到pos端。pos端下单时需要自行判断注意只有status=1的商品才能购买
     * <p>
     * 2016-11-04, 门店商品状态，决定线上能否购买,线下收银不作限制：1-有效，默认，0-无效
     */
    private Integer status = 1;

    private Long procateId; //商品类目

    //商品类目的类型，按商品类目同步到电子秤
    private Integer cateType = CateApi.BACKEND_CATE_BTYPE_NORMAL;   //
    //2016-08-01，新增产品线编号清分,产品线的商品默认都归到0，相当于原来的标超
    private Integer prodLineId = 0;


    /**
     * 是否和云端同步:默认1同步，0不同步
     */
    private int isCloudActive = 1;

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkuName() {
        //2016-12-25 兼容旧版本已经下载过商品档案的情况，skuName和shortName没有数据的问题
        if (StringUtils.isEmpty(skuName)){
            return name;
        }
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getNamePinyin() {
        if (namePinyin == null) {
            return "";
        }
        return namePinyin;
    }

    public void setNamePinyin(String namePinyin) {
        this.namePinyin = namePinyin;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getNameSortLetter() {
        if (nameSortLetter == null) {
            return "";
        }
        return nameSortLetter;
    }

    public void setNameSortLetter(String nameSortLetter) {
        this.nameSortLetter = nameSortLetter;
    }

    public String getUnit() {
        if (unit == null) {
            return "";
        }
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }


    /**
     * 价格为空时需要手动补充
     */
    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Integer getStatus() {
        if (status == null) {
            return 1;
        }
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPriceType() {
        if (priceType == null) {
            return PriceType.PIECE;
        }
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public Double getPackageNum() {
        if (packageNum == null) {
            return 0D;
        }
        return packageNum;
    }

    public void setPackageNum(Double packageNum) {
        this.packageNum = packageNum;
    }

    public Long getProcateId() {
        return procateId;
    }

    public void setProcateId(Long procateId) {
        this.procateId = procateId;
    }

    public Integer getCateType() {
        return cateType;
    }

    public void setCateType(Integer cateType) {
        this.cateType = cateType;
    }

    public Integer getProdLineId() {
        if (procateId == null) {
            return 0;
        }
        return prodLineId;
    }

    public void setProdLineId(Integer prodLineId) {
        this.prodLineId = prodLineId;
    }

    public int getIsCloudActive() {
        return isCloudActive;
    }

    public void setIsCloudActive(int isCloudActive) {
        this.isCloudActive = isCloudActive;
    }
}
