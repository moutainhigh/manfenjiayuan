package com.mfh.enjoycity.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

import com.mfh.comn.bean.PageInfo;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.SearchProductAdapter;
import com.mfh.enjoycity.adapter.SearchProductBean;
import com.mfh.enjoycity.database.HistorySearchEntity;
import com.mfh.enjoycity.database.HistorySearchService;
import com.mfh.enjoycity.view.NaviSearchView;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


/**
 * 搜索·
 *
 * */
public class SearchProductActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.my_recycler_view)
    RecyclerView mRecyclerView;

    private static final int MAX_PAGE_SIZE = 100;
    public static final String EXTRA_KEY_SHOP_ID = "EXTRA_KEY_SHOP_ID";
    public static final String EXTRA_KEY_SHOP_NAME = "EXTRA_KEY_SHOP_NAME";
    private String shopId, shopName;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_search_product;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_search_community);//必须在setSupportActionBar(toolbar);之前设置才有效
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SearchProductActivity.this.onBackPressed();
                    }
                });
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.searchButton) {
//                    doDeepSearch(mSearchView.getQuery().toString());
//                }
//                return true;
//            }
//        });
        NaviSearchView searchView = new NaviSearchView(this);
        searchView.setNaviSearchListener(new NaviSearchView.NaviSearchListener() {
            @Override
            public void onDeepSearch(String queryText) {
                doDeepSearch(queryText);
            }

            @Override
            public void onSearch(String queryText) {
                doSearchWork(queryText);
            }
        });
        toolbar.addView(searchView);
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_submit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();
        super.onCreate(savedInstanceState);

//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(layoutManager);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        SearchProductBean adapterData = new SearchProductBean();

        List<String> hotData = new ArrayList<>();
        for(int i=0; i<10; i++){
            hotData.add("数据" + StringUtils.genNonceStringByLength(i));
        }
        adapterData.setHotData(hotData);

        HistorySearchService dbService = HistorySearchService.get();
        List<HistorySearchEntity> historyEntityList = dbService.queryAll(new PageInfo(1, 10));
        adapterData.setHistoryData(historyEntityList);

        SearchProductAdapter hotSearchAdapter = new SearchProductAdapter(this, adapterData);
        hotSearchAdapter.setOnItemClickLitener(new SearchProductAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onSearch(String queryText) {
                DialogUtil.showHint("click " + queryText);
            }
        });
        mRecyclerView.setAdapter(hotSearchAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_product, menu);
//        MenuItem search = menu.findItem(R.id.search_content);
//        mSearchView = (SearchView) search.getActionView();
//        mSearchView.setIconifiedByDefault(false);
//        setSearch();

//        MenuItem searchButton = menu.findItem(R.id.searchButton);
//        TextView button = (TextView)searchButton.getActionView();
//        button.setText("搜索");

        return super.onCreateOptionsMenu(menu);
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
     * */
    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            shopId = intent.getStringExtra(EXTRA_KEY_SHOP_ID);
            shopName = intent.getStringExtra(EXTRA_KEY_SHOP_NAME);
        }
    }

    private void doSearchWork(String queryText){
        if(TextUtils.isEmpty(queryText)){
//            mAdapter.clearData();
//            mAdapter.notifyDataSetChanged();
            //隐藏搜索结果页面
            return;
        }

        DialogUtil.showHint("模糊搜索 " + queryText);
        //TODO
        PageInfo pageInfo = new PageInfo(0, MAX_PAGE_SIZE);
//        List<IMConversation> result = sessionDao.queryMySessions(SharedPreferencesHelper.getLoginUsername(),
//                queryText, pageInfo);
//
//        mAdapter.clearData();
//        mAdapter.addDataItems(KvBean.exportToKvsDirect(result));
//        mAdapter.notifyDataSetChanged();
    }

    private void doDeepSearch(String queryText){

        if(TextUtils.isEmpty(queryText)){
//            mAdapter.clearData();
//            mAdapter.notifyDataSetChanged();
            return;
        }
        DialogUtil.showHint("搜索商品 " + queryText);

        //TODO,搜索成功显示搜索结果
        HistorySearchService dbService = HistorySearchService.get();
//        HistorySearchEntity entity = new HistorySearchEntity();
//        entity.setId(shopId + String.valueOf(TimeUtil.genTimeStamp()));
//        entity.setShopId(shopId);
//        entity.setShopName(shopName);
//        entity.setQueryContent(queryText);
//        dbService.saveOrUpdate(entity);
        dbService.addNewEntity(shopId, shopName, queryText);

        //TODO
    }



}
