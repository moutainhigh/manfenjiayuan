/*
 * 文件名称: PhysicalPoint.java
 * 版权信息: Copyright 2013-2014 chunchen technology Co., LTD. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: zhangyz
 * 修改日期: 2014-10-16
 * 修改内容: 
 */
package com.manfenjiayuan.im.bean;

import java.io.Serializable;

/**
 * 物理端点对象
 * @author zhangyz created on 2014-10-16
 */
public class PhysicalPoint implements Serializable {
    /**  */
//    private static final long serialVersionUID = 5413326527502328200L;
    private Long cid;    //渠道 编号
    private Integer ctype;  // 通信渠道类型 参见MsgChanneltypeConst
    /**
     * channelPointId,通讯渠道内通讯地址或者说端口，例如:微信openid、个推clientId、
     * 满分家园用户号、满分家园业务模块号，如洗衣、快递、手机号、个人QQ号、个人邮箱地址,
     * 若point_type=1,则为渠道内部的组号*/
	private String cpt;
            
//    public PhysicalPoint() {
//        super();
//    }

//    public PhysicalPoint(Integer channelId, Integer channelType, String pointId) {
//        super();
//        this.cid = channelId;
//        this.ctype = channelType;
//        this.cpt = pointId;
//    }
    
//    /**
//     * 目标点的构造
//     * @param channelType
//     * @param pointId
//     */
//    public PhysicalPoint(Integer channelType, String pointId) {
//        super();
//        this.ctype = channelType;
//        this.cpt = pointId;
//    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Integer getCtype() {
        return ctype;
    }

    public void setCtype(Integer ctype) {
        this.ctype = ctype;
    }

    public String getCpt() {
        return cpt;
    }

    public void setCpt(String cpt) {
        this.cpt = cpt;
    }


//    @Override
//    public String toString(){
//        return JSON.toJSONString(this);
//    }
    
    /**
     * 反序列化
     * @param json
     * @return
     * @author zhangyz created on 2014-11-15
     */
//    public static PhysicalPoint parse(String json) {
//        return JSON.parseObject(json, PhysicalPoint.class);
//    }

}
