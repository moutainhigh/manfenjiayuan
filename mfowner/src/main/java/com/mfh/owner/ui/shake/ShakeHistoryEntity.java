package com.mfh.owner.ui.shake;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.IStringId;
import com.mfh.framework.api.abs.MfhEntity;

/**
 * 消息表
 * Created by Administrator on 14-5-6.
 */
@Table(name="shake_history")
public class ShakeHistoryEntity extends MfhEntity<String> implements IStringId{
    /**
     * 下面的都是EmbMsgBean里面的内容
     */
    private String guid;//创建人guid
    private Long deviceId; //设备ID，关联biz_shop_device表
    private Long pageId; //页面ID，微信返回的page_id
    private String title; //主标题
    private String description; //副标题
    private String pageUrl; //页面url
    private String remark; //备注信息
    private String iconUrl; //图标url

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
