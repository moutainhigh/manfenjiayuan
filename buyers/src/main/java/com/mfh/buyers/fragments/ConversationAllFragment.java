package com.mfh.buyers.fragments;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.manfenjiayuan.im.database.dao.IMConversationDao;
import com.manfenjiayuan.im.database.entity.IMConversation;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.im.database.service.IMConversationService;
import com.manfenjiayuan.im.param.WxParam;
import com.mfh.buyers.R;
import com.mfh.buyers.utils.UIHelper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.FaceUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetProcessor;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * 对话 + 历史记录
 * Created by Nat.Zhang on 2015-04-10.
 */
public class ConversationAllFragment extends BaseListFragment<IMConversation> {
    private IMConversationService sessionService;
    private EmbMsgService msgService;
    private IMConversationDao sessionDao;
    private int msgMode = -1;
    private SimpleDateFormat sdf = new SimpleDateFormat(TimeCursor.INER_TIME_FOR_CHAT);
    private BroadcastReceiver receiver;

    private TextView tvTopBarTitle;
    private Button btnSearch;
    private ScrollView scrollView;
    private ConversationHeaderView headerView;

    private boolean needLoadData = true;

    public ConversationAllFragment() {
        super();
        this.cacheDataItem = false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_conversatioin;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        sessionService = ServiceFactory.getService(IMConversationService.class, this.getMyActivity());
        msgService = ServiceFactory.getService(EmbMsgService.class, this.getMyActivity());
        sessionDao = sessionService.getDao();

        super.createViewInner(rootView, container, savedInstanceState);

        initTopBar();
        btnSearch = (Button) rootView.findViewById(R.id.button_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.redirectToActivity(getActivity(), SearchActivity.class);
            }
        });

        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
        headerView = (ConversationHeaderView) rootView.findViewById(R.id.conversation_header);
//        headerView = new ConversationHeaderView(getActivity());
        headerView.setListener(new ConversationHeaderView.ConversationHeaderViewListerner() {
            @Override
            public void onItemClicked(int i) {
                Intent intent = new Intent(getContext(), ConversationActivity.class);
                intent.putExtra(ConversationActivity.EXTRA_KEY_CONVERSATION_TYPE, i);
//                sessionService.getMessageSkipIntent(intent, bean);
                startActivity(intent);
            }
        });
//        getListView().addView(headerView, 0);//

        //TODO
        Bundle args = getArguments();
        if (args != null){
            msgMode = args.getInt("msgMode");
            if (msgMode != -1) {
                sessionService.setMsgMode(msgMode);
                msgService.setMsgMode(msgMode);
            }
        }

       registerReceiver();//onPause,onResume统一做

        //检查是否已经登陆
        if (!MfhLoginService.get().haveLogined()){
            Intent intent = new Intent(UIHelper.ACTION_REDIRECT_TO_LOGIN_H5);
            getActivity().sendBroadcast(intent);

            needLoadData = false;
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        //msgTimer.addSessionPeroid();
       // msgTimer.changeNothing(MsgTimer.TYPE_SESSION);//待机时还是要工作为好
    }

    @Override
    public void onStop() {
        super.onStop();
        //msgTimer.addSessionPeroid();
        //msgTimer.changeNothing(MsgTimer.TYPE_SESSION);//待机时还是要工作为好
    }


    @Override
    public void onDestroy() {
       // msgTimer.changeNothing(MsgTimer.TYPE_SESSION);

        super.onDestroy();
    }

    @Override
    public ProgressDialog onPreExecute(int taskKind) {
        /*if (taskKind == 1) {
            Integer myCount = sessionDao.getMyCount(ls.getLoginName());
            sessionService.queryFromNet();
            if (myCount != null && myCount <= 0) {
                return super.onPreExecute(taskKind);//首次加载，时间比较长，故显示等待框。
            }
            else
                return null;
        }
        else*/
            return null;// super.onPreExecute(taskKind);//屏蔽等待框
    }

    /**
     * */
    @Override
    public int getItemResLayoutId(int position) {
        return R.layout.chat_list_item;
    }

    @Override
    public void fillListItemView(KvBean<IMConversation> kvBean, View listItemView, int position, ViewGroup parent) {
        if (kvBean == null)
            return;
        IMConversation bean = kvBean.getBean();
        if(bean == null){
            return;
        }

        if(bean.getTopSessionOrder() != IMConversation.DEFAULT_NOT_TOP_ORDER){
            listItemView.setBackgroundColor(getResources().getColor(R.color.topListItemView));
        }

        showHeaderImage(listItemView, bean, R.id.ms_headImg);
        showBadgeNumber(listItemView, bean, R.id.ms_unReadCountText);
        showNickName(listItemView, bean, R.id.ms_humanName);
        showReceiveTime(listItemView, bean, R.id.ms_formatCreateTime);
        showMsgContent(listItemView, bean, R.id.ms_lastMsgContent);

        ImageView ivTypeMarker = (ImageView)listItemView.findViewById(R.id.iv_type_marker);
        //TODO,显示会话类型，这里默认显示个人。
        ivTypeMarker.setImageResource(R.drawable.marker_individual);
    }

    /**
     * 显示昵称
     * */
    private void showNickName(View listItemView, IMConversation bean, int resId){
        TextView tv = (TextView)listItemView.findViewById(resId);
        if(tv != null && bean != null){
            String strUserName;
            if (StringUtils.isBlank(bean.getHumanname())) {
                if (StringUtils.isBlank(bean.getNicknamebin()))
                    strUserName = "新用户";
                else
                    strUserName = bean.getNicknamebin();
            }
            else
                strUserName = bean.getHumanname();

            if (strUserName.length() > 8)
                strUserName = strUserName.substring(0,8) + "...";
            tv.setText(strUserName);
        }
    }

    /**
     * 显示时间
     * */
    private void showReceiveTime(View listItemView, IMConversation bean, int resId){
        TextView tv = (TextView)listItemView.findViewById(resId);
        if(tv != null && bean != null){
            tv.setText(bean.getFormatCreateTime());
        }
    }

    /**
     * 显示消息内容
     * */
    private void showMsgContent(View listItemView, IMConversation bean, int resId){
        TextView tv = (TextView)listItemView.findViewById(resId);
        if(tv != null && bean != null){
            String rawInfo = bean.getMsgInfo();
            if(StringUtils.isNotBlank(rawInfo)) {
                String msgInfo = WxParam.fromJson(rawInfo).getSummary();
                SpannableString spannableString ;
                if (bean.getSpokesman() == null) {
                    spannableString = FaceUtil.getSpannable(getContext(), "某" + msgInfo, 15, 15);
                }
                else{
                    spannableString = FaceUtil.getSpannable(getContext(), msgInfo, 15, 15);
                }

                tv.setText(spannableString);
            }
            else
                tv.setText("");
        }
    }

    /**
     * 显示未读消息数
     * */
    private void showBadgeNumber(View listItemView, IMConversation bean, int resId){
        TextView tv = (TextView)listItemView.findViewById(resId);
        if(tv != null && bean != null){
            Long unreadMsgCount = bean.getUnreadcount();
            if (unreadMsgCount <= 0 || StringUtils.isBlank(bean.getMsgInfo())) {//或者最后会话无的（相当于没有会话）
                tv.setVisibility(View.INVISIBLE);
            }
            else {
                tv.setVisibility(View.VISIBLE);

                if(unreadMsgCount > 99){
                    tv.setText("99+");//未读消息数大于99条时，显示"99+"。
                }else{
                    tv.setText(Long.toString(bean.getUnreadcount()));//是整形的话要出错的
                }
            }
        }
    }

    /**
     * 显示头像
     * */
    private void showHeaderImage(View listItemView, IMConversation bean, int resId){
        FineImgView img = (FineImgView)listItemView.findViewById(resId);
        if(img != null){
            if ("系统通知".equals(bean.getHumanname())) {
                img.getImageView().setImageResource(R.drawable.notes);
            }else {
                if (StringUtils.isNotBlank(bean.getLocalheadimageurl())) {
//                    Log.d("Nat: SessionHeadImageUrl", String.format("%s,%s",bean.getHumanname(), bean.getLocalheadimageurl()));
                    img.setNeedSample(true);
                    img.setMaxWidth(96);
                    img.setSrc(bean.getLocalheadimageurl());
                }
            }
        }
    }

    @Override
    protected boolean isAsyncDao() {
        return false;
    }

    public static List<KvBean<IMConversation>> makeList(int size) {
        List<KvBean<IMConversation>> list = new ArrayList<KvBean<IMConversation>>();
        for (int i = 0; i < size; i++) {
            list.add(new KvBean<IMConversation>());
        }
        return list;
    }


    /**
     * 查询本地数据库，填充数据
     * */
    @Override
    protected List<KvBean<IMConversation>> readListPageData(String searchToken,
                        PageInfo pageInfo, NetProcessor.QueryRsProcessor<IMConversation> callBack) {
        if (sessionDao == null)
            return null;
        List<IMConversation> ess = sessionDao.queryMySessions(MfhLoginService.get().getLoginName(), searchToken, pageInfo);
        /*for(int i = 0;i<ess.size();i++){
            //ess.get(i).setUnReadCount(0);
            if (ess.get(i).getMsgType() != null)
                if(ess.get(i).getMsgType() == 1) {
                    if(sessionDao.getListByCpointId(ess.get(i).getChannelpointid())){
                        ess.remove(ess.get(i));
                    }
                }
        }*/
        return KvBean.exportToKvsDirect(ess);
    }

    @Override
    public BaseService getService() {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        KvBean<IMConversation> kvBean = (KvBean<IMConversation>)view.getTag();
        if (kvBean == null){
            return;
        }

//        // 进入对话列表页面，取消通知栏通知。
//        //TODO,取消指定会话通知
//        NotificationManager notificationManager = (NotificationManager)this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancel(MsgConstants.MSG_NOTIFICATION);

        if(sessionService != null){
            ChatActivity.actionStart(getContext(), 1, kvBean.getBean(), sessionService.getMsgMode());
        }
        else{
            ChatActivity.actionStart(getContext(), 1, kvBean.getBean(), 0);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);

        AppHelper.hideSoftInput(getMyActivity());
    }

    private void initTopBar(){
        tvTopBarTitle = (TextView) rootView.findViewById(R.id.topbar_title);
        tvTopBarTitle.setText(R.string.topbar_title_converdaction);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MsgConstants.ACTION_RECEIVE_SESSION);
        filter.addAction(MsgConstants.ACTION_REFRESH_SESSIONUNREAD);
        filter.addAction(MsgConstants.ACTION_MSG_SERVERERROR);
        filter.addAction(MsgConstants.ACTION_SORT_SCOLL_UNREAD_MSG);
        filter.addAction(MsgConstants.ACTION_DOWNLOAD_FINISH);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                MLog.d(String.format("ConversationAllFragment.onReceive.action= %s", action));
                if (action.equals(MsgConstants.ACTION_RECEIVE_SESSION)) {
                    doLoadAndRefreshStart();
                }
                else if (action.equals(MsgConstants.ACTION_REFRESH_SESSIONUNREAD)) {
                    try{
                        Long sessionId = intent.getLongExtra("sessionId", -1L);
                        //
                        for(KvBean<IMConversation> entity : mAdapter.getDataItems()){
                            IMConversation bean = entity.getBean();
                            if (bean.getId().equals(sessionId)) {
                                if(sessionDao != null){
                                    int nowCount = sessionDao.getSessionUnReadCount(sessionId);
                                    bean.setUnreadcount(Long.valueOf(nowCount));
                                }
                                break;
                            }
                        }
                    }
                    catch(Exception e){
                        Log.e("Nat", e.toString());
                    }
                    finally{
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else if (action.equals(MsgConstants.ACTION_MSG_SERVERERROR)) {
                    //直接停止，要重新开始只要重新连接网络
                    //msgTimer.stop();
                    DialogUtil.showMessage(getContext(), "消息服务器端繁忙,连接暂停!请稍候手动重启网络或重启程序后重试。");
                }
                else if(action.equals(MsgConstants.ACTION_SORT_SCOLL_UNREAD_MSG)) {
                    scollUnReadMsg();
                }else if (action.equals(MsgConstants.ACTION_DOWNLOAD_FINISH)) {
                    doLoadAndRefreshNext();
                }
            }
        };
        getActivity().registerReceiver(receiver, filter);
    }

    private void loadData(){
        if(needLoadData){
            reloadData();
        }
    }

    /**
     * 加载数据
     * */
    public void reloadData(){
        needLoadData = true;

        if(scrollView != null){
            scrollView.smoothScrollTo(0, 0);
        }
//        if(sessionService != null){
//            //查询请求
//            sessionService.queryFromNet();
//            sessionService.resetHaveAlert();
//        }
        refreshToLoadMore();
    }

    @Override
    public void refreshToLoadMore() {
        super.refreshToLoadMore();
//
        if(sessionService != null){
            sessionService.queryFromNet();
            sessionService.resetHaveAlert();
        }
    }

    /**
     * 跳转到第一条未读
     * */
    private void scollUnReadMsg() {
        List<KvBean<IMConversation>> kvBeans = getListAdapter().getDataItems();
        int position = getListView().getFirstVisiblePosition();
        position += 2;
        for(int i = position; i < kvBeans.size(); i++) {
            IMConversation bean = kvBeans.get(i).getBean();
            Long count = bean.getUnreadcount();
            if(count > 0) {
                getListView().smoothScrollToPositionFromTop(i,4);
                break;
            }
            if(i == kvBeans.size()-1) {
                getListView().smoothScrollToPositionFromTop(0,4);
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (receiver != null)
            getActivity().unregisterReceiver(receiver);
    }


}
