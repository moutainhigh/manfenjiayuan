package com.mfh.enjoycity.bean;

/**
 * 预支付·微信
 * Created by NAT.ZZN on 2015/5/14.
 *
 */
public class CommonAddrTemp implements java.io.Serializable{
    private Long id; //地址编号
    private Long subdisId; //小区编号
    private String subName; //小区名
    private String receiveName; //收件人名称
    private String receivePhone; //收件人手机
    private Long addrvalid;//公寓编号
    private String addrName; //楼幢地址名
    private String provinceID; //省编号
    private String cityID; //城市编号
    private String areaID; //区县编号
    private Integer status; //地址状态 0:未验证，但有效，可以送货; -1:已验证,无效地址，无法送

    public CommonAddrTemp(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubdisId() {
        return subdisId;
    }

    public void setSubdisId(Long subdisId) {
        this.subdisId = subdisId;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public Long getAddrvalid() {
        return addrvalid;
    }

    public void setAddrvalid(Long addrvalid) {
        this.addrvalid = addrvalid;
    }

    public String getAddrName() {
        return addrName;
    }

    public void setAddrName(String addrName) {
        this.addrName = addrName;
    }

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public String getReceivePhone() {
        return receivePhone;
    }

    public void setReceivePhone(String receivePhone) {
        this.receivePhone = receivePhone;
    }

    public String getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(String provinceID) {
        this.provinceID = provinceID;
    }

    public String getCityID() {
        return cityID;
    }

    public void setCityID(String cityID) {
        this.cityID = cityID;
    }

    public String getAreaID() {
        return areaID;
    }

    public void setAreaID(String areaID) {
        this.areaID = areaID;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\nsubdisId=%s", subdisId));
        sb.append(String.format("\nsubName=%s", subName));
        sb.append(String.format("\naddrName=%s", addrName));
        sb.append(String.format("\nreceiveName=%s", receiveName));
        sb.append(String.format("\nreceivePhone=%s", receivePhone));
        sb.append(String.format("\nprovinceID=%s", provinceID));
        sb.append(String.format("\ncityID=%s", cityID));
        return sb.toString();
    }
}
