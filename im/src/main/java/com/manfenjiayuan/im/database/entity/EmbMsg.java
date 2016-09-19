package com.manfenjiayuan.im.database.entity;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.IStringId;
import com.mfh.framework.core.MfhEntity;
import com.mfh.framework.core.utils.TimeUtil;

/**
 * 消息表
 * Created by Administrator on 14-5-6.
 */
@Table(name="emb_msg_v0001")
public class EmbMsg extends MfhEntity<String> implements IStringId{
    private static final String KEY_PHYSICS_POINT = "pp";//物理端点
    private static final String KEY_CHANNEL_ID = "cid";//
    private static final String KEY_CHANNEL_POINTER_ID = "cpt";
    private static final String KEY_ID = "id";

    public static final Integer READ = 1;//已读
    public static final Integer UNREAD = 0;//未读


    //发送方
    private Long fromGuid; //发送者逻辑编号，不能为空
    private Integer fromChannelId; //发送渠道编号
    private Integer fromChannelType; //发送渠道类型
    private String fromChannelPointId;//发送者物理端点号

    //接收方
    private Long sessionid; //会话编号，关联会话表
    private Integer toChannelId; //发送渠道编号
    private String toChannelPointId;//发送者物理端点号

    //消息内容
    private String techType; //消息技术类型
    private Integer bizType; //消息业务类型
    private String msgBean; //消息内容

//    private Integer bind ;//0:未绑定 1:绑定
//    private String localheadimageurl;//头像
//    private String pointName; //端点名 可空 对应point_type=1有用

    //标志
    private Integer isRead = UNREAD;//是否已读:0-未读  1-已读
    /** 保存格式化后的时间显示 */
    private String formatCreateTime;


    public Long getFromGuid() {
        return fromGuid;
    }

    public void setFromGuid(Long fromGuid) {
        this.fromGuid = fromGuid;
    }

    public Integer getFromChannelId() {
        return fromChannelId;
    }

    public void setFromChannelId(Integer fromChannelId) {
        this.fromChannelId = fromChannelId;
    }

    public Integer getFromChannelType() {
        return fromChannelType;
    }

    public void setFromChannelType(Integer fromChannelType) {
        this.fromChannelType = fromChannelType;
    }

    public String getFromChannelPointId() {
        return fromChannelPointId;
    }

    public void setFromChannelPointId(String fromChannelPointId) {
        this.fromChannelPointId = fromChannelPointId;
    }

    public Integer getToChannelId() {
        return toChannelId;
    }

    public void setToChannelId(Integer toChannelId) {
        this.toChannelId = toChannelId;
    }

    public String getToChannelPointId() {
        return toChannelPointId;
    }

    public void setToChannelPointId(String toChannelPointId) {
        this.toChannelPointId = toChannelPointId;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public String getTechType() {
        return techType;
    }

    public String getMsgBean() {
        return msgBean;
    }

    public void setMsgBean(String msgBean) {
        this.msgBean = msgBean;
    }

    public void setTechType(String techType) {
        this.techType = techType;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public Long getSessionid() {
        return sessionid;
    }


    public void setSessionid(Long sessionid) {
        this.sessionid = sessionid;
    }

    public String getFormatCreateTime() {
        return formatCreateTime;
    }

    public void setFormatCreateTime(String formatCreateTime) {
        this.formatCreateTime = formatCreateTime;
    }

    public String getMsgInfo() {
        if (msgBean != null) {
            JSONObject jsonObject = JSONObject.parseObject(msgBean);
//            JSONObject msgBean = jsonObject.getJSONObject("msgBean");
//            JSONObject msgBody = jsonObject.getJSONObject("body");
            if(jsonObject != null){
                return JSONObject.toJSONString(jsonObject);
            }
        }
        return null;
    }


    /**
     * 处理发送消息的返回值
     * @param json
     * @return
     */
    public static EmbMsg parseOjbect(JSONObject json) {
        EmbMsg entity = new EmbMsg();

        JSONObject msg = json.getJSONObject("msg");
        if (msg == null){
            return null;
        }

        entity.setCreatedBy(msg.getString("spokesman"));
//        entity.setLocalheadimageurl(msg.getString("headimageurl"));
        entity.setFormatCreateTime(msg.getString("formatCreateTime"));

        //发送方
        JSONObject from = msg.getJSONObject("from");
        if(from != null){
            entity.setFromGuid(from.getLong("guid"));
            JSONObject pp = from.getJSONObject(KEY_PHYSICS_POINT);
            if(pp != null){
                entity.setFromChannelPointId(pp.getString(KEY_CHANNEL_POINTER_ID));
                entity.setFromChannelId(pp.getInteger(KEY_CHANNEL_ID));
                entity.setFromChannelType(pp.getInteger("ctype"));
            }
        }

        JSONObject to = msg.getJSONObject("to");
        if (to != null){
            entity.setSessionid(to.getLong("sid"));
            JSONObject pp = to.getJSONObject(KEY_PHYSICS_POINT);
            if(pp != null){
                entity.setToChannelPointId(pp.getString(KEY_CHANNEL_POINTER_ID));
                entity.setToChannelId(pp.getInteger(KEY_CHANNEL_ID));
            }
        }

        JSONObject msgBean = msg.getJSONObject("msgBean");
        if (msgBean != null){
            entity.setMsgBean(JSONObject.toJSONString(msgBean));
            entity.setId(msgBean.getString(KEY_ID));
            entity.setTechType(msgBean.getString("type"));
            entity.setBizType(msgBean.getInteger("bizType"));
            entity.setCreatedDate(TimeUtil.parse(msgBean.getString("time"), TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        }

        return entity;
    }

    public static EmbMsg parseOjbect(String jsonString) {
        return parseOjbect(JSONObject.parseObject(jsonString));
    }
}
