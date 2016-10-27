package com.mfh.enjoycity.ui.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.SearchCommunityAdapter;
import com.mfh.enjoycity.bean.SubdisBean;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.subdist.SubdistApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.CustomSearchView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


/**
 * 搜索·小区
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 * */
public class SearchCommunityActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.searchBar)
    CustomSearchView searchView;
    @Bind(R.id.button_cancel)
    Button btnCancel;
    @Bind(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    private SearchCommunityAdapter mAdapter;

    private static final int MAX_PAGE_SIZE = 100;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_search_community;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_search_community);//必须在setSupportActionBar(toolbar);之前设置才有效
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SearchCommunityActivity.this.onBackPressed();
                    }
                });

//        Button spinner = new Button(this);
//        spinner.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UIHelper.redirectToActivity(SearchCommunityActivity.this, ChangeCityActivity.class);
//            }
//        });
//        //TODO://选择地址
//        spinner.setText("选择地址");
//        spinner.setBackground(null);
//        toolbar.addView(spinner);

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_submit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSearchViewEX();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new SearchCommunityAdapter(this, null);
        mAdapter.setOnItemClickLitener(new SearchCommunityAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent data = new Intent();
                data.putExtra(Constants.INTENT_KEY_ADDRESS_DATA, mAdapter.getData().get(position));
                setResult(Activity.RESULT_OK, data);

                finish();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        DeviceUtils.hideSoftInput();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 初始化搜索框
     * */
    private void initSearchViewEX(){
        searchView.setHint(R.string.search_bar_hint_conversation);
        searchView.setTextColor(ContextCompat.getColor(this, R.color.material_black));
        searchView.setListener(new CustomSearchView.CustomSearchViewListener() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                doSearchWork(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void doSearch(String queryText) {
                //TODO
                doSearchWork(queryText);
            }

        });

        btnCancel.setText("搜索");
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSearchWork(searchView.getQueryText());
            }
        });
    }

    private void doSearchWork(String queryText){
        if(TextUtils.isEmpty(queryText)){
//            mAdapter = new SearchCommunityAdapter(this, null);
//            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setData(null);
            return;
        }

        if(!NetworkUtils.isConnect(this)){
            animProgress.setVisibility(View.GONE);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        animProgress.setVisibility(View.VISIBLE);

        //回调
        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<SubdisBean>(new PageInfo(1, 100)) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<SubdisBean> rs) {//此处在主线程中执行。
                        saveQueryResult(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);
                        super.processFailure(t, errMsg);
                        Message message = new Message();
                        message.what =MSG_ERROR;
                        uiHandler.sendMessage(message);
                    }
                }
                , SubdisBean.class
                , MfhApplication.getAppContext());

        PageInfo pageInfo = new PageInfo(0, MAX_PAGE_SIZE);
        SubdistApi.list("", queryText, pageInfo, responseCallback);
    }

    /**
     * 将后台返回的结果集保存到本地,同步执行
     * @param rs 结果集
     */
    private void saveQueryResult(RspQueryResult<SubdisBean> rs) {
        try {
            int retSize = rs.getReturnNum();
            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));
//
            if(retSize < 1){
                Message message = new Message();
                message.what = MSG_NONE;
                uiHandler.sendMessage(message);
                return;
            }

            List<SubdisBean> result = new ArrayList<>();
            for (int i = 0; i < retSize; i++) {
                result.add(rs.getRowEntity(i));
            }
            Message message = new Message();
            message.what = MSG_SUCCESS;
            message.obj = result;
            uiHandler.sendMessage(message);
        }
        catch(Throwable ex){
            ZLogger.e(ex.toString());
            Message message = new Message();
            message.what = MSG_ERROR;
            uiHandler.sendMessage(message);
////            throw new RuntimeException(ex);
        }
    }

    private final static int MSG_NONE = 0;
    private final static int MSG_ERROR = 1;
    private final static int MSG_SUCCESS = 2;
    private Handler uiHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NONE:
                    DialogUtil.showHint("无结果，请重新再试一次");
//                    loadingTextView.hide();
                    animProgress.setVisibility(View.GONE);
                    break;
                case MSG_ERROR:
//                    loadingTextView.hide();
                    animProgress.setVisibility(View.GONE);
                    break;
                case MSG_SUCCESS:
//                    loadingTextView.hide();
                    List<SubdisBean> result = (List<SubdisBean>)msg.obj;
//                    mAdapter = new SearchCommunityAdapter(SearchCommunityActivity.this, result);
//                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.setData(result);
                    animProgress.setVisibility(View.GONE);
                    break;
            }
            super.handleMessage(msg);
        }
    };



}
