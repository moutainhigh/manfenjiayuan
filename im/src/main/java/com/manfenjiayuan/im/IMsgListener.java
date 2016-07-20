/*
 * 文件名称: IMsgHttp.java
 * 版权信息: Copyright 2013-2014 chunchen technology Co., LTD. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: jguo
 * 修改日期: 2014-11-3
 * 修改内容: 
 */
package com.manfenjiayuan.im;

/**
 * 
 * @author jguo created on 2014-11-3
 */
public interface IMsgListener {
	 /**
     * 适配器接收到客户端消息的内部处理
     * @param IMsgRequest
     * @throws Exception
     * @author jguo created on 2014-11-03
     */
     IResponse onMessage(IMsgRequest request) throws Exception;
}
