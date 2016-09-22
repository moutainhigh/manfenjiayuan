/*
 * 文件名称: DestInfo.java
 * 版权信息: Copyright 2013-2014 chunchen technology Co., LTD. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: zhangyz
 * 修改日期: 2014-10-23
 * 修改内容: 
 */
package com.manfenjiayuan.im.bean;


import com.manfenjiayuan.im.constants.IMChannelType;
import com.mfh.framework.core.utils.StringUtils;

/**
 * 发送到目标信息。
 *
 * @author zhangyz created on 2014-10-23
 */
@SuppressWarnings("serial")
public class DestInfo implements java.io.Serializable {
    /**
     * 下面三个不能同时为空
     */
    private Long sid;//会话Id，空代表无须会话，-1，代表需要新建会话，其他>0代表实际的会话ID
    private Long guid = null;//逻辑端点信息
    private PhysicalPoint pp = null;//目标物理端点信息

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public Long getSid() {
        return sid;
    }

    public Long getGuid() {
        return guid;
    }

    public void setGuid(Long guid) {
        this.guid = guid;
    }

    public PhysicalPoint getPp() {
        return pp;
    }

    public void setPp(PhysicalPoint pp) {
        this.pp = pp;
    }

    public static DestInfo create(Long channelId, String channePointId, Long guid) {
        DestInfo destInfo = new DestInfo();
        destInfo.setGuid(guid);
        if (!StringUtils.isEmpty(channePointId)) {
            PhysicalPoint physicalPoint = new PhysicalPoint();
            physicalPoint.setCtype(IMChannelType.APP);
            physicalPoint.setCpt(channePointId);
            physicalPoint.setCid(channelId);

            destInfo.setPp(physicalPoint);
        }

        return destInfo;
    }

    public static DestInfo create(Long conversationId) {
        DestInfo destInfo = new DestInfo();
        destInfo.setSid(conversationId);

        return destInfo;
    }
}
