package com.manfenjiayuan.im.database.entity;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;

import java.util.Date;

/**
 * 消息会话表
 * Created by Administrator on 14-5-6.
 */
@Table(name = "emb_session")
public class IMConversation implements ILongId {
    public static int DEFAULT_ORDER = 999;
    private Integer sessionOrder = DEFAULT_ORDER;//对话次序，用于手工设置对话
    //public static Long DEFAULT_TOP_ORDER = 1L;
    public static Long DEFAULT_NOT_TOP_ORDER = 0L;
    private Long topSessionOrder = DEFAULT_NOT_TOP_ORDER;//置顶次序

    /**
     * guid,可保存createguid或者fromguid
     */
    protected String guid;
    protected Long createUnixTime = 0L;
    private String ownerId; //属于谁的,用登陆名代替
    private Integer isGroup; //判断是否是服务组对话
    private String MsgInfo = getMsgInfoFromParam();
    /**
     * 下面的内容都是MsgSession里面的
     */
    private Long id; //会话编号
    /**
     * 消息类别，0=普通消息
     */
    private Integer type;
    /**
     * 消息处理状态 0=未处理,9=关闭
     */
    private Integer status;
    /**
     * 小区名称
     */
    private String subdisname;
    /**
     * 备注信息
     */
    private String remark;
    private Long addrvalid;
    /**
     * 微信昵称
     */
    private String nicknamebin;
    private Integer msgtype;
    private Integer channelId;//创建人渠道编号
    private String channelpointid;//创建人物理端点
    private Integer fromckId = null; //说话人渠道编号
    private String fromcpid = "";
    private String param;
    /**
     * 小区ID
     */
    private Long subdisid;
    /**
     * 微信用户的绑定状态
     */
    private Integer regstatus;
    /**
     * 本地头像
     */
    private String localheadimageurl;

    private String fromguid = "";//会话创建者编号


    /**会话编号*/
    private Long sessionid;//

    /**创建人*/
    private Long humanid;
    /**创建者姓名*/
    private String humanname;
    /**创建者头像*/
    private String headimageurl;
    /**创建者手机*/
    private String mobile = "";
    /**创建人的地址,绑定后的公寓短地址*/
    private String addrvalsbind;


    /**最新一条消息的内容*/
    private String content;
    /**最新的一条消息的创建时间*/
    protected Date createtime;

    /**最后说话人的姓名*/
    private String spokesman;
    /**最后修改时间，用于排序*/
    private Long lastupdate;
    /**保存格式化后的时间显示*/
    protected String formatCreateTime;


    /**是否已读 0=未读，1=已读*/
    private Integer isread;
    /**未读条数*/
    private Long unreadcount = 0L;

    private String getMsgInfoFromParam() {
        if (param != null) {
            JSONObject jsonObject = JSONObject.parseObject(param);
//            JSONObject msgBean = jsonObject.getJSONObject("msgBean");
            if(jsonObject != null){
                return JSONObject.toJSONString(jsonObject);
            }
        }
        return null;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Long getTopSessionOrder() {
        return topSessionOrder;
    }

    public void setTopSessionOrder(Long topSessionOrder) {
        this.topSessionOrder = topSessionOrder;
    }

    public Integer getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(Integer isGroup) {
        this.isGroup = isGroup;
    }

    public String getNicknamebin() {
        return nicknamebin;
    }

    public void setNicknamebin(String nicknamebin) {
        this.nicknamebin = nicknamebin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 下面的内容是MsgSessionBase的
     */

    public String getSubdisname() {
        return subdisname;
    }

    public void setSubdisname(String subdisname) {
        this.subdisname = subdisname;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIsread() {
        return isread;
    }

    public void setIsread(Integer isread) {
        this.isread = isread;
    }

    public Long getUnreadcount() {
        return unreadcount;
    }

    public void setUnreadcount(Long unreadcount) {
        this.unreadcount = unreadcount;
    }

    public String getAddrvalsbind() {
        return addrvalsbind;
    }

    public void setAddrvalsbind(String addrvalsbind) {
        this.addrvalsbind = addrvalsbind;
    }

    public Integer getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(Integer msgtype) {
        this.msgtype = msgtype;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getChannelpointid() {
        return channelpointid;
    }

    public void setChannelpointid(String channelpointid) {
        this.channelpointid = channelpointid;
    }

    public Long getAddrvalid() {
        return addrvalid;
    }

    public void setAddrvalid(Long addrvalid) {
        this.addrvalid = addrvalid;
    }

    public String getFromguid() {
        return fromguid;
    }

    public void setFromguid(String fromguid) {
        this.fromguid = fromguid;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Long getHumanid() {
        return humanid;
    }

    public void setHumanid(Long humanid) {
        this.humanid = humanid;
    }

    public String getHumanname() {
        return humanname;
    }

    public void setHumanname(String humanname) {
        this.humanname = humanname;
    }

    public String getSpokesman() {
        return spokesman;
    }

    public void setSpokesman(String spokesman) {
        this.spokesman = spokesman;
    }


    public String getHeadimageurl() {
        return headimageurl;
    }

    public void setHeadimageurl(String headimageurl) {
        this.headimageurl = headimageurl;
    }

    public String getLocalheadimageurl() {
        return localheadimageurl;
    }

    public void setLocalheadimageurl(String localheadimageurl) {
        this.localheadimageurl = localheadimageurl;
    }

    public Long getSubdisid() {
        return subdisid;
    }

    public void setSubdisid(Long subdisid) {
        this.subdisid = subdisid;
    }

    public Integer getRegstatus() {
        return regstatus;
    }

    public void setRegstatus(Integer regstatus) {
        this.regstatus = regstatus;
    }

    public Long getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(Long lastupdate) {
        this.lastupdate = lastupdate;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Long getSessionid() {
        return sessionid;
    }

    public void setSessionid(Long sessionid) {
        this.sessionid = sessionid;
    }

    public Integer getFromckId() {
        return fromckId;
    }

    public void setFromckId(Integer fromckId) {
        this.fromckId = fromckId;
    }

    public String getFromcpid() {
        return fromcpid;
    }

    public void setFromcpid(String fromcpid) {
        this.fromcpid = fromcpid;
    }

    public Long getCreateUnixTime() {
        return createUnixTime;
    }

    public void setCreateUnixTime(Long createUnixTime) {
        this.createUnixTime = createUnixTime;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        if (createtime != null) {
            setCreateUnixTime(createtime.getTime());
        }
        this.createtime = createtime;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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
        return MsgInfo;
    }

    public void setMsgInfo(String msgInfo) {
        MsgInfo = msgInfo;
    }

    public Integer getSessionOrder() {
        return sessionOrder;
    }

    public void setSessionOrder(Integer sessionOrder) {
        this.sessionOrder = sessionOrder;
    }
}
