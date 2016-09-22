package com.manfenjiayuan.im.utils;

import com.alibaba.fastjson.JSON;
import com.manfenjiayuan.im.IMConfig;
import com.manfenjiayuan.im.bean.DestInfo;
import com.manfenjiayuan.im.bean.FromInfo;
import com.manfenjiayuan.im.bean.MsgBean;
import com.manfenjiayuan.im.constants.IMChannelType;
import com.manfenjiayuan.im.bean.MsgParameter;
import com.manfenjiayuan.im.bean.PhysicalPoint;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.constants.IMTechType;
import com.manfenjiayuan.im.param.RegisterParam;
import com.manfenjiayuan.im.param.TextParam;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.Date;

/**
 * Created by bingshanguxue on 16/3/4.
 */
public class IMFactory {

	/**
	 * 获得注册或者注销所用的参数
	 * @param cpid
	 * @param guid
	 * @return MsgParameter
	 */
	public static MsgParameter register(String cpid, Long guid){
		MsgParameter msgParameter = new MsgParameter();

		PhysicalPoint fromPhysicalPoint = new PhysicalPoint();
		fromPhysicalPoint.setCtype(IMChannelType.APP);
		fromPhysicalPoint.setCpt(cpid);//

		FromInfo from = new FromInfo();
		from.setGuid(guid);
		from.setPp(fromPhysicalPoint);
		msgParameter.setFrom(from);

		String exparam = "-1";
		RegisterParam param = new RegisterParam();
		param.setBind(1);
		param.setParam(exparam);

		MsgBean msgBean = new MsgBean();
		msgBean.setTime(new Date());
		msgBean.setType(IMTechType.JSON);
		msgBean.setBizType(IMBizType.REGISTER);
		msgBean.setBody(param);

		msgParameter.setMsgBean(msgBean);

		return msgParameter;
	}

	/**
	 * 获得消息所用的参数
	 * @param cpid
	 * @param guid
	 * @return MsgParameter
	 */
    public static MsgParameter createMsgParam(String cpid, Long guid,
                                                       String content, Long sessionId){
//		ZLogger.d(String.format("createMsgParam, cpid=%s, guid=%d,content=%s, sessionId=%d",
//				cpid, guid, content, sessionId));
        MsgParameter msgParameter = new MsgParameter();
//		ZLogger.d("msgParameter: " + JSON.toJSONString(msgParameter));

        msgParameter.setFrom(FromInfo.create(cpid, guid));
//		msgParameter.setTo(DestInfo.create(cpid, guid));
		msgParameter.setTo(DestInfo.create(sessionId));

		TextParam textParam = new TextParam(content);
        MsgBean msgBean = new MsgBean();
		msgBean.setBizType(IMBizType.CS);
		msgBean.setType(IMTechType.TEXT);
		msgBean.setTime(new Date());
		msgBean.setBody(textParam);

		msgParameter.setMsgBean(msgBean);
//		ZLogger.d("textParam: " + JSON.toJSONString(textParam));


		/*
		if(IMBizType.REGISTER == request.getBiztype()){
			String exparam = "-1";
			//获取关注时二维码参数
			if(!StringUtils.isEmpty(((TextRequest) request).getContent())){
				exparam = ((TextRequest) request).getContent();
			}
			RegisterParam param = new RegisterParam();
			param.setBind(1);
			param.setParam(exparam);
			msgBean.setTechType(IMTechType.JSON);
			msgBean.setMsgBody(param);
		}else if(IMBizType.UNREGISTER == request.getBiztype()){
			msgBean.setTechType(IMTechType.TEXT);

			TextParam textParam = new TextParam();
			textParam.setContent("取消关注");
			msgBean.setMsgBody(textParam);
		}else{
			msgBean.setTechType(IMTechType.TEXT);
			TextParam textParam = new TextParam();
			textParam.setContent(((TextRequest) request).getContent());
			msgBean.setMsgBody(textParam);
		}*/

		ZLogger.d("msgParameter: " + JSON.toJSONString(msgParameter));
        return msgParameter;
    }


	/**
	 * 获得消息所用的参数
	 * @param cpid
	 * @param guid
	 * @return MsgParameter
	 */
	public static MsgParameter chatMessage(String cpid, Long guid,
											  String content, Long toGuid){
//		ZLogger.d(String.format("createMsgParam, cpid=%s, guid=%d,content=%s, sessionId=%d",
//				cpid, guid, content, sessionId));
		MsgParameter msgParameter = new MsgParameter();
//		ZLogger.d("msgParameter: " + JSON.toJSONString(msgParameter));

		msgParameter.setFrom(FromInfo.create(cpid, guid));
		msgParameter.setTo(DestInfo.create(cpid, toGuid));

		TextParam textParam = new TextParam(content);
		MsgBean msgBean = new MsgBean();
		msgBean.setBizType(IMBizType.CS);
		msgBean.setType(IMTechType.TEXT);
		msgBean.setTime(new Date());
		msgBean.setBody(textParam);

		msgParameter.setMsgBean(msgBean);
//		ZLogger.d("textParam: " + JSON.toJSONString(textParam));


		/*
		if(IMBizType.REGISTER == request.getBiztype()){
			String exparam = "-1";
			//获取关注时二维码参数
			if(!StringUtils.isEmpty(((TextRequest) request).getContent())){
				exparam = ((TextRequest) request).getContent();
			}
			RegisterParam param = new RegisterParam();
			param.setBind(1);
			param.setParam(exparam);
			msgBean.setTechType(IMTechType.JSON);
			msgBean.setMsgBody(param);
		}else if(IMBizType.UNREGISTER == request.getBiztype()){
			msgBean.setTechType(IMTechType.TEXT);

			TextParam textParam = new TextParam();
			textParam.setContent("取消关注");
			msgBean.setMsgBody(textParam);
		}else{
			msgBean.setTechType(IMTechType.TEXT);
			TextParam textParam = new TextParam();
			textParam.setContent(((TextRequest) request).getContent());
			msgBean.setMsgBody(textParam);
		}*/

		ZLogger.d("msgParameter: " + JSON.toJSONString(msgParameter));
		return msgParameter;
	}

	/**
	 * 获得消息所用的参数
	 * @param cpid
	 * @param guid
	 * @return MsgParameter
	 */
	public static MsgParameter textMessageParameter(Integer bizType, String content,
													Long guid, String fromChannelPointId,
													Long toGuid, String toChannelPointId){
		MsgParameter msgParameter = new MsgParameter();
//		ZLogger.d("msgParameter: " + JSON.toJSONString(msgParameter));

		msgParameter.setFrom(FromInfo.create(fromChannelPointId, guid));
		msgParameter.setTo(DestInfo.create(toChannelPointId, toGuid));

		MsgBean msgBean = new MsgBean();
		msgBean.setBizType(bizType);
		msgBean.setType(IMTechType.TEXT);
		msgBean.setTime(new Date());
		msgBean.setBody(new TextParam(content));

		msgParameter.setMsgBean(msgBean);
		ZLogger.d(JSON.toJSONString(msgParameter));
		return msgParameter;
	}


}
