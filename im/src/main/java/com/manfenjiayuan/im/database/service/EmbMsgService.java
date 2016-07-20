package com.manfenjiayuan.im.database.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.im.IMApi;
import com.manfenjiayuan.im.IMConfig;
import com.manfenjiayuan.im.IMConstants;
import com.manfenjiayuan.im.bean.MsgParameter;
import com.manfenjiayuan.im.utils.IMFactory;
import com.manfenjiayuan.im.IMHelper;
import com.manfenjiayuan.im.bean.BizMsgParamWithSession;
import com.manfenjiayuan.im.bean.MsgBean;
import com.manfenjiayuan.im.bean.MsgParameterWrapper;
import com.manfenjiayuan.im.database.dao.EmbMsgDao;
import com.manfenjiayuan.im.database.dao.EmbMsgNetDao;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.param.WxParam;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.net.NetProcessor;

import net.tsz.afinal.http.AjaxParams;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 消息服务类
 * Created by Administrator on 14-5-6.
 */
public class EmbMsgService extends BaseService<EmbMsg, String, EmbMsgDao> {
    private static final Boolean QUERY_RESULT_SYNC = false;
    private EmbMsgNetDao netDao = new EmbMsgNetDao();
    private String theLastCreateTime = "";//本轮最大游标
    private long cursorValue = -1L;//针对一个session此轮下载涉及到的最大游标
//    private BaseListFragment msgFragment = null;//消息界面列表
    private int msgMode;
    private SimpleDateFormat format = new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT);
   // private String lastUpdateDate;

    @Override
    protected Class<EmbMsgDao> getDaoClass() {
        return EmbMsgDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    /**
     * 设置消息模式
     * @param msgMode
     */
    public void setMsgMode(int msgMode) {
        this.msgMode = msgMode;
        netDao.setMsgMode(msgMode);
    }

    public void save(EmbMsg msg) {
        dao.save(msg);
    }

//    public void setMsgFragment(BaseListFragment msgFragment) {
//        this.msgFragment = msgFragment;
//    }

    /**
//     * 保存个推传过来的Msg，并且返回id
//     * @param jsonString
//     */
//    public String saveNewMsg(String jsonString) {
//        EmbMsg msg = parseOjbect(jsonString);
//
//        String msgId = null;
//
//        if(msg != null){
//            getDao().saveOrUpdate(msg);
//
//            Date createdDate = msg.getCreatedDate();
//            if (createdDate != null){
//                msgSet.saveMaxCreateTime(msg.getSessionid(), format.format(msg.getCreatedDate()));
//            }
//
//            msgId =  msg.getId();
////            Log.d("Nat: saveNewMsgFromGeTui", String.format("msgId=%s", msgId));
//
//            //发送广播，让页面刷新
//            Intent intent = new Intent(MsgConstants.GE_TUI_MSG_SHOW);
//            intent.putExtra("ge_id", msgId);
//            getContext().sendBroadcast(intent);
//        }
//
//        return msgId;
//    }

    private EmbMsg parseOjbect(String jsonString) {
        EmbMsg msg = new EmbMsg();

        JSONObject object = JSONObject.parseObject(jsonString);
        JSONObject msgObj = object.getJSONObject("msg");
        if(msgObj != null){
            msg.setCreatedBy(msgObj.getString("spokesman"));//发送者姓名
            msg.setLocalheadimageurl(msgObj.getString("headimageurl"));//发送者头像
            msg.setFormatCreateTime(msgObj.getString("formatCreateTime"));//格式化后的消息发送时间，客户端直接拿来显示

            JSONObject msgBean = msgObj.getJSONObject("msgBean");
            JSONObject msgFrom = msgObj.getJSONObject("from");
            JSONObject msgTo = msgObj.getJSONObject("to");

            if(msgBean != null){
                msg.setTechType(msgBean.getString("type"));
                msg.setId(msgBean.getString("id"));
                msg.setParam(JSONObject.toJSONString(msgBean));

                try {
                    msg.setCreatedDate(format.parse(msgBean.getString("time")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

//                JSONObject msgBody = msgBean.getJSONObject("body");
            }

            msg.setFromguid(msgFrom.getLong("guid"));
            ZLogger.d(String.format("sessionId= %s", String.valueOf(msgTo.getLong("sid"))));
            msg.setSessionid(msgTo.getLong("sid"));
        }


        return msg;
    }

    /**
     * 发送图片
     * @param params
     * @param processor
     */
    public void sendImageMsg(AjaxParams params, NetProcessor.ComnProcessor processor) {
        netDao.sendMessage(params, processor);
    }


    /**
     * 支持多个会话Id的消息下载
     */
    public class MyPageInfo extends PageInfo {
        private Iterator<Long> sessionIdIter;
        public MyPageInfo(int pageNo, int pageSize) {
            super(pageNo, pageSize);
        }

        public void setSessionIdIter(Iterator<Long> sessionIdIter) {
            this.sessionIdIter = sessionIdIter;
        }

        /**
         * 是否还有下一个sessionId
         * @return
         */
        public  boolean hasNextSession() {
            return sessionIdIter.hasNext();
        }

        /**
         * 启动下一个sessionId的下载工作
         */
        public Long nextSessionId() {
            this.setPageNo(1);//从第一页开始。
            this.setTotalCount(-1);//初始化，重新开始
            return sessionIdIter.next();
        }
    }

    /**
     * 保存个推传过来的Msg，并且返回id
     * @param jsonString
     */
    public String saveNewMsg(String jsonString) {
        EmbMsg msg = parseOjbect(jsonString);

        String msgId = null;

        if(msg != null){
            getDao().saveOrUpdate(msg);

            Date createdDate = msg.getCreatedDate();
            if (createdDate != null){
                IMConfig.saveMaxCreateTime(msg.getSessionid(), format.format(msg.getCreatedDate()));
            }

            msgId =  msg.getId();
//            Log.d("Nat: saveNewMsgFromGeTui", String.format("msgId=%s", msgId));

            //发送广播，让页面刷新
            Intent intent = new Intent(IMConstants.GE_TUI_MSG_SHOW);
            intent.putExtra("ge_id", msgId);
            getContext().sendBroadcast(intent);
        }

        return msgId;
    }

    /**
     * 保存一条新发的消息
     * @param msg
     */
    public void saveNewMsg(EmbMsg msg) {
        if (msg.getIsRead() != 1 || !getDao().entityExistById(msg.getId())){
            msg.setIsRead(1);
            getDao().saveOrUpdate(msg);//存储到本地数据库

            Long sid = msg.getSessionid();
            if(sid != null){
                //msgSet.saveMaxId(msg.getSessionId(), msg.getMsgId());//避免下次重新同步浪费
                IMConfig.saveMaxCreateTime(msg.getSessionid(), format.format(msg.getCreatedDate()));
            }
            //msgSet.saveLastUpdate((long) (msg.getCreatedDate().getTime() * 0.001));
        }

    }

    /**
     * 向后台发送消息
     * @param sessionId
     * @param wxParam
     */
    public void sendMessage(Long guid, Long sessionId, WxParam wxParam,
                            NetProcessor.ComnProcessor processor)  {
        AjaxParams params = new AjaxParams();

//        ZLogger.d("wxParam: " + JSON.toJSONString(wxParam));
//        String jsonStr = String.valueOf();
        MsgParameter msgParameter = IMFactory.createMsgParam(IMConfig.getPushClientId(),
                guid, wxParam.getContent(), sessionId);
        params.put(IMApi.PARAM_KEY_JSON_STR, msgParameter.toString());

        netDao.sendMessage(params, processor);
    }

    /**
     * 向后台发送消息
     * @param sessionId
     * @param wxParam
     */
    public void sendMessageToPeople(Long fromGuid, Long toGuid, WxParam wxParam,
                            NetProcessor.ComnProcessor processor)  {
        AjaxParams params = new AjaxParams();

//        ZLogger.d("wxParam: " + JSON.toJSONString(wxParam));
//        String jsonStr = String.valueOf();
        MsgParameter msgParameter = IMFactory.chatMessage(IMConfig.getPushClientId(),
                fromGuid, wxParam.getContent(), toGuid);
        params.put(IMApi.PARAM_KEY_JSON_STR, msgParameter.toString());

        netDao.sendMessage(params, processor);
    }

    /**
     * 向后台发送消息
     * @param sessionId
     * @param wxParam
     */
    public void sendText(Long fromGuid, Long toGuid, Integer bizType, String content,
                                    NetProcessor.ComnProcessor processor)  {
        AjaxParams params = new AjaxParams();

//        ZLogger.d("wxParam: " + JSON.toJSONString(wxParam));
//        String jsonStr = String.valueOf();
        MsgParameter msgParameter = IMFactory.textMessageParameter(fromGuid,
                bizType, content, toGuid);
        params.put(IMApi.PARAM_KEY_JSON_STR, msgParameter.toString());

        netDao.sendMessage(params, processor);
    }

    /**
     * 向后台发送消息，基于素材库
     * @param sessionId
     * @param sourceid
     * @param context
     * @param processor
     */
    public void sendMessage(Long guid, Long cpid, Long sessionId, Long sourceid, final Context context,
                            NetProcessor.ComnProcessor processor ) {
        AjaxParams netParam = new AjaxParams();
        netParam.put(IMApi.PARAM_KEY_SOURCE_ID, sourceid.toString());
        netParam.put(IMApi.PARAM_KEY_SESSION_ID, Long.toString(sessionId));
        netParam.put(IMApi.PARAM_KEY_CHANNEL_POINT_ID, String.valueOf(cpid));
        netParam.put(IMApi.PARAM_KEY_GUID, String.valueOf(guid));
        netDao.sendMessage(netParam, processor);
    }

    /**
     * @param  sessionId 针对哪个会话
     * 向后台执行一次查询请求
     */
    public void queryFromNet(long sessionId) {
        List<Long> sessionIds = new ArrayList<>();
        sessionIds.add(sessionId);
        queryFromNets(sessionIds);
    }

    public void queryFromNet(long sessionId, String lastUpdate) {
        List<Long> sessionIds = new ArrayList<>();
        sessionIds.add(sessionId);
        queryFromNets(sessionIds, lastUpdate);
    }

    /**
     * 批量下载一批会话的消息
     * @param sessionIds
     */
    public void queryFromNets(List<Long> sessionIds) {
        queryFromNets(sessionIds, null);
    }

    public void queryFromNets(List<Long> sessionIds, String lastUpdate) {
        if (sessionIds.size() == 0 /*|| netDao.isDownLoading()*/){
//            System.out.print("Nat: queryFromNets, no sessionId exist");
            return;
        }

        Iterator<Long> iter = sessionIds.iterator();
        MyPageInfo pageInfoParam = new MyPageInfo(1, 100);//要求第一页从1开始
        pageInfoParam.setSessionIdIter(iter);

        //必要的初始化工作
        Long sessionId = pageInfoParam.nextSessionId();//523LA
        cursorValue = -1L;

//        msgSet = new IMHelper(SharedPreferencesHelper.getLoginUsername());
        String createTime = IMConfig.getMaxCreateTime(sessionId);//"-1L"
        ZLogger.d(String.format("createTime=%s, sessionId=%s", createTime, String.valueOf(sessionId)));
        ZLogger.d(String.format("MaxMsgUpdateDate=%s", IMConfig.getMaxMsgUpdateDate()));
        if (createTime.equals("") || createTime.equals("0000-00-00 00:00:00")){
            if(lastUpdate == null || lastUpdate.equals("") || lastUpdate.equals("0000-00-00 00:00:00")){
                createTime = IMConfig.getMaxMsgUpdateDate();
            }
            else{
                createTime = lastUpdate;
            }
        }
        ZLogger.d(String.format("createTime=%s, sessionId=%s", createTime, String.valueOf(sessionId)));

        netDao.setDownLoading(true);
        queryFromNet(sessionId, createTime, pageInfoParam);
    }

    /**
     * 执行网络请求
     * @param pageInfoParam
     */
    public void queryFromNet(final Long sessionId, final String createTime, PageInfo pageInfoParam) {
        try {
            if (sessionId > 0){
                final EmbMsgService that = this;
                AjaxParams params = new AjaxParams();
                params.put("sessionid", sessionId.toString());
                params.put("createtime", createTime);

                netDao.query(params, new NetProcessor.QueryRsProcessor<BizMsgParamWithSession>(pageInfoParam) {
                    @Override
                    public void processQueryResult(RspQueryResult<BizMsgParamWithSession> rs) {//此处在主线程中执行。
                        getContext().sendBroadcast(new Intent(IMConstants.ACTION_DIALOG_MISS));
                        //考虑到修改本地数据库也比较耗时，故再采用异步。
                        if (QUERY_RESULT_SYNC) {
                            that.saveQueryResult(rs, sessionId);
                            continueOrBreak(pageInfo, sessionId, createTime);
                        }
                        else {
                            new SaveQueryResultAsync(pageInfo, sessionId, createTime).execute(rs);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d(String.format("errMsg:%s", errMsg));
                        super.processFailure(t, errMsg);
                        getContext().sendBroadcast(new Intent(IMConstants.ACTION_DIALOG_MISS));
                    }

                    @Override
                    public void processResult(IResponseData result) {
                        ZLogger.d(String.format("result:%s", result.toString()));
                        super.processResult(result);
                        getContext().sendBroadcast(new Intent(IMConstants.ACTION_DIALOG_MISS));
                    }
                }, "/biz/msg/getMessageItems");
            }
            else{
                System.out.print("bingshanguxue: sessionId is invalid.");
            }
        }
        catch (Throwable ex) {
            netDao.setDownLoading(false);
            ZLogger.d("queryFromNet " +  ex.toString());
//            throw new RuntimeException(ex);
        }
    }



    /**
     * 将后台返回的结果集保存到本地,同步执行
     * @param rs 结果集
     * @param sessionId 会话Id
     */
    private void saveQueryResult(RspQueryResult<BizMsgParamWithSession> rs, Long sessionId) {
        try {
            //保存下来
            int retSize = rs.getReturnNum();
            ZLogger.d(String.format("%d messages, content:%s", retSize, rs.toString()));

            EmbMsg bean;
            BizMsgParamWithSession rsBizMsgParamWithSession;

            boolean needShow = false;
            boolean needRefresh = false;
            StringBuilder ids = null;
            //MsgTimer msgTimer = ServiceFactory.getService(MsgTimer.class.getName());
//            if (msgFragment != null) {
//                needShow = true;
//                ids = new StringBuilder("");
//            }

            int unreadCount = 0;
            for (int ii = retSize-1; ii >= 0; ii--) {
                rsBizMsgParamWithSession = rs.getRowEntity(ii);
                if (rsBizMsgParamWithSession == null){
                    continue;
                }
//                Log.d("Nat", String.format("rsBizMsgParamWithSession message %d = %s", ii, rsBizMsgParamWithSession.toString()));

                bean = new EmbMsg();

                MsgParameterWrapper messageBean = rsBizMsgParamWithSession.getMsg();
                if (messageBean != null){
                    bean.setCreatedBy(messageBean.getSpokesman());
                    bean.setLocalheadimageurl(messageBean.getHeadimageurl());
                    bean.setFormatCreateTime(messageBean.getFormatCreateTime());
                    bean.setSessionid(messageBean.getSid());
                    MsgBean msgBean = messageBean.getMsgBean();
                    if (msgBean != null){
                        bean.setTechType(msgBean.getType());
                        bean.setId(msgBean.getId());
                        bean.setParam(JSONObject.toJSONString(msgBean));
                        bean.setFromguid(messageBean.getFromGuid());
                        Date createDae = msgBean.getTime();
                        bean.setCreatedDate(createDae);
                        try {
                            if (createDae.getTime() > format.parse(IMConfig.getMaxCreateTime(sessionId)).getTime()) {
                                theLastCreateTime = format.format(createDae);
                                //msgSet.saveMaxCreateTime(sessionId, bean.getFormatCreateTime());
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }


                }
                /*if (bean.getMsgId() > cursorValue) {//原来的MsgId变成了String类型，所以这段代码已经无效
                    cursorValue = bean.getMsgId();
                    theLastCreateTime = bean.getFormatCreateTime();
                }*/
                /*if (format.parse(bean.getFormatCreateTime()).getTime() > format.parse(theLastCreateTime).getTime()) {
                    theLastCreateTime = bean.getFormatCreateTime();
                }*/

                if (bean.getSessionid() == null){
                    bean.setSessionid(sessionId);
                }

                if (needShow) {
                    bean.setIsRead(1);
                    if (ids.length() > 0){
                        ids.append(",");
                    }
                    MsgBean msgBean = messageBean.getMsgBean();
                    if(msgBean != null){
                        ids.append(msgBean.getId());
                    }
                }

                if (!dao.entityExistById(bean.getId())) {
                    dao.save(bean);
                    needRefresh = true;
                    unreadCount++;
                }
            }
            IMConfig.saveMaxCreateTime(sessionId, theLastCreateTime);
            this.getContext().sendBroadcast(new Intent(IMConstants.ACTION_DIALOG_MISS));
            if (ids != null && ids.length() > 0 && needRefresh) {
                Intent intent = new Intent(IMConstants.ACTION_RECEIVE_MSG);
                intent.putExtra("ids", ids.toString());
                this.getContext().sendBroadcast(intent);
                //刷新未读数量
                IMHelper.changeSessionUnReadCount(this.getContext(), sessionId, unreadCount);
            }
        }
        catch(Throwable ex) {
            netDao.setDownLoading(false);
            throw new RuntimeException(ex);
        }
    }

    /**
     * 判断当前session是否继续下载，否则切换到下一个session。全部结束则退出。
     * @param pageInfo
     * @param sessionId
     * //@param maxId
     */
    private void continueOrBreak(PageInfo pageInfo, long sessionId, String createTime) {
        Long newSessionId = continueOrBreakInner(pageInfo, sessionId);
        if (newSessionId != null) {
            if (newSessionId != sessionId) {
                createTime = IMConfig.getMaxCreateTime(newSessionId);//"-1L"
                if (createTime.equals("")) {
                    createTime = IMConfig.getMaxMsgUpdateDate();
                }
            }
            else
                pageInfo.moveToNext();
            queryFromNet(newSessionId, createTime, pageInfo);
        }
    }



    /**
     * 是否继续下载,若不需要则持久化保存此轮查询涉及到的最大游标。
     * @param sessionId 当前针对哪个session
     * @param pageInfo 分页信息
     * @return 新的session，可能还是当前session
     */
    private Long continueOrBreakInner(PageInfo pageInfo, Long sessionId) {
        MyPageInfo myPageInfo = (MyPageInfo)pageInfo;
        if (myPageInfo.hasNextPage()
                && (myPageInfo.getHavedCount() < IMConfig.getMaxMsgNumOneSession()))//若还有继续发起请求,并且最多下载500个会话。
            return sessionId;
        else {
            //一个session的消息已经下载完毕
            try {//保存最新游标
                if (pageInfo.getTotalCount() > 0) {//追加未读消息数
                    if (cursorValue > -1) {
                        //msgSet.saveMaxId(sessionId, cursorValue);
                        IMConfig.saveMaxCreateTime(sessionId, theLastCreateTime);
                    }
                    //changeSessionUnReadCount(sessionId, pageInfo.getTotalCount());,这个逻辑不合理（加载的未必就是未读的消息）
                }
                cursorValue = -1;//再初始化
            }
            catch(Throwable ex) {
                logger.error(ex.getMessage(), ex);
            }

            if (myPageInfo.hasNextSession()) {
                return myPageInfo.nextSessionId();
            }
            else {//所有session的消息已经下载完毕
                netDao.setDownLoading(false);
                return null;
            }
        }
    }

    /**
     * 内部类，执行异步保存到本地数据库
     */
    private class SaveQueryResultAsync extends AsyncTask<RspQueryResult<BizMsgParamWithSession>,
            Integer, Long> {
        private PageInfo pageInfo;
        private Long sessionId;
        private String maxId;

        /**
         * 构造函数
         * @param pageInfo
         */
        public SaveQueryResultAsync(PageInfo pageInfo, Long sessionId, String maxId) {
            this.pageInfo = pageInfo;
            this.sessionId = sessionId;
            this.maxId = maxId;
        }

        @Override
        protected void onPostExecute(Long s) {
            super.onPostExecute(s);
            if (s == 0) {
                //若成功继续发起请求
                continueOrBreak(pageInfo, sessionId, maxId);
            }
        }

        @Override
        protected Long doInBackground(RspQueryResult<BizMsgParamWithSession>... params) {
            saveQueryResult(params[0], sessionId);
            return 0L;
        }
    }

    /**
     * 保存来自个推的消息（组要的消息接收方式）
     */
    public void saveMsgFromGeTui(String json) {

    }
}
