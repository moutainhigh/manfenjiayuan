package com.mfh.enjoycity.ui.activity;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.comn.bean.PageInfo;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.SelectAddressAdapter;
import com.mfh.enjoycity.database.ReceiveAddressEntity;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;

import java.util.List;

import butterknife.Bind;


/**
 * 搜索·小区
 *
 * */
public class SelectAddressActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    private SelectAddressAdapter mAdapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_select_address;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_select_address);//必须在setSupportActionBar(toolbar);之前设置才有效
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SelectAddressActivity.this.onBackPressed();
                    }
                });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_add);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                this, LineItemDecoration.VERTICAL_LIST));

        ReceiveAddressService dbService = ReceiveAddressService.get();
        List<ReceiveAddressEntity> entityList =  dbService.queryAll(new PageInfo(1, 100));

        mAdapter = new SelectAddressAdapter(this, entityList);
        mAdapter.setOnItemClickLitener(new SelectAddressAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                String addrId = mAdapter.getData().get(position).getId();

                ShopcartHelper.getInstance().refreshMemberOrderAddr(addrId);

                setResult(RESULT_OK);

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

}
