package com.manfenjiayuan.im.database.entity;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.IStringId;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.MfhEntity;
import com.mfh.framework.login.logic.MfhLoginService;

import java.text.ParseException;

/**
 * 消息表
 * Created by Administrator on 14-5-6.
 */
@Table(name="emb_msg")
public class EmbMsg extends MfhEntity<String> implements IStringId{
    //发送方
    private Long fromguid; //发送者逻辑编号，不能为空
    private Integer fromchannelid; //发送渠道编号
    private Integer fromChannelType; //发送渠道类型
    private String channelpointid;//发送者物理端点号

    //接收方
    private Long sessionid; //会话编号，关联会话表

    /**
     * 下面的都是EmbMsgBean里面的内容
     */
    private Long guid;//创建人guid
    private String techType; //消息技术类型
    private String param; //消息内容
    //private Integer unread = 0; //是否未读，0-未读  1-已读
    private String tagOne;//消息标签1，备用
    private String tagTwo; //消息标签2，备用
    private String tagThree; //消息标签3，备用
    private Long extparam;//额外业务关联信息
    private Integer bind ;//0:未绑定 1:绑定
    private String localheadimageurl;//头像
    private String pointName; //端点名 可空 对应point_type=1有用
    private String msgInfo = getMsgInfoFromParam();//具体的消息内容，json格式


    private Integer isRead = 0;//是否已读:0-未读  1-已读
    private Integer isdel = 0; //删除标志
    
    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public String getParam() {
        return param;
    }


    public void setParam(String param) {
        this.param = param;
    }


    public Long getFromguid() {
        return fromguid;
    }


    public void setFromguid(Long fromguid) {
        this.fromguid = fromguid;
    }


    public String getTechType() {
        return techType;
    }


    public void setTechType(String techType) {
        this.techType = techType;
    }


    public Long getSessionid() {
        return sessionid;
    }


    public void setSessionid(Long sessionid) {
        this.sessionid = sessionid;
    }


    public Integer getIsdel() {
        return isdel;
    }


    public void setIsdel(Integer isdel) {
        this.isdel = isdel;
    }


    public Integer getFromchannelid() {
        return fromchannelid;
    }


    public void setFromchannelid(Integer fromchannelid) {
        this.fromchannelid = fromchannelid;
    }


    public String getChannelpointid() {
        return channelpointid;
    }


    public void setChannelpointid(String channelpointid) {
        this.channelpointid = channelpointid;
    }


    public String getTagOne() {
        return tagOne;
    }


    public void setTagOne(String tagOne) {
        this.tagOne = tagOne;
    }


    public String getTagTwo() {
        return tagTwo;
    }


    public void setTagTwo(String tagTwo) {
        this.tagTwo = tagTwo;
    }


    public String getTagThree() {
        return tagThree;
    }


    public void setTagThree(String tagThree) {
        this.tagThree = tagThree;
    }

    /** 保存格式化后的时间显示 */
    private String formatCreateTime;

    public Long getExtparam() {
        return extparam;
    }

    public void setExtparam(Long extparam) {
        this.extparam = extparam;
    }

    public Integer getBind() {
        return bind;
    }

    public void setBind(Integer bind) {
        this.bind = bind;
    }

    public Long getGuid() {
        return guid;
    }

    public void setGuid(Long guid) {
        this.guid = guid;
    }

    public String getLocalheadimageurl() {
        return localheadimageurl;
    }

    public void setLocalheadimageurl(String localheadimageurl) {
        this.localheadimageurl = localheadimageurl;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getFormatCreateTime() {
        return formatCreateTime;
    }

    public void setFormatCreateTime(String formatCreateTime) {
        this.formatCreateTime = formatCreateTime;
    }

    public String getMsgInfo() {
        if (param != null) {
            return getMsgInfoFromParam();
        }
        return msgInfo;
    }

    private String getMsgInfoFromParam() {
        if (param != null) {
            JSONObject jsonObject = JSONObject.parseObject(param);
//            JSONObject msgBean = jsonObject.getJSONObject("msgBean");
//            JSONObject msgBody = jsonObject.getJSONObject("body");
            if(jsonObject != null){
                return JSONObject.toJSONString(jsonObject);
            }
        }
        return null;
    }

    public void setMsgInfo(String msgInfo) {
        this.msgInfo = msgInfo;
    }


    /**
     * 处理发送消息的返回值
     * @param json
     * @return
     */
    public static EmbMsg fromSendMessage(JSONObject json, String picUrl) {
//        Log.d("Nat: getEmbMsgFromSendMessage", JSONObject.toJSONString(json));
        EmbMsg entity = new EmbMsg();

        JSONObject msg = JSONObject.parseObject(json.getString("msg"));
        if (msg == null){
            return entity;
        }

        entity.setCreatedBy(msg.getString("spokesman"));
        entity.setLocalheadimageurl(msg.getString("headimageurl"));
        entity.setFormatCreateTime(msg.getString("formatCreateTime"));

        JSONObject from = JSONObject.parseObject(msg.getString("from"));
        if(from != null){
            JSONObject pp = JSONObject.parseObject(from.getString("pp"));
            if(pp != null){
                entity.setChannelpointid(pp.getString("cpt"));
                entity.setFromchannelid(pp.getInteger("cid"));
            }
            entity.setFromguid(from.getLong("guid"));
        }

        JSONObject to = JSONObject.parseObject(msg.getString("to"));
        if (to != null){
            entity.setSessionid(to.getLong("sid"));
        }

        JSONObject msgBean = JSONObject.parseObject(msg.getString("msgBean"));
        if (msgBean != null){
            entity.setParam(JSONObject.toJSONString(msgBean));
            entity.setId(msgBean.getString("id"));
            entity.setTechType(msgBean.getString("type"));
            entity.setPointName(MfhLoginService.get().getLoginName());
            try {
                entity.setCreatedDate(TimeCursor.InnerFormat.parse(msgBean.getString("time")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return entity;
    }

}
