package com.mfh.litecashier.ui.fragment.components;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.manfenjiayuan.business.bean.wrapper.HostServer;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.HandOverBill;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * <h>选择租户，设置域名</h><br>
 * {@link HandOverBill}<br>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class HostServerFragment extends BaseProgressFragment {
    public static String EXTRA_KEY_HOSTSERVER= "hostServer";


    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.recyclerView)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private HostServerAdapter menuAdapter;


    public static HostServerFragment newInstance(Bundle args) {
        HostServerFragment fragment = new HostServerFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_hostname;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
////            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
//            launchMode = args.getInt(EXTRA_KEY_LAUNCHMODE, 0);
//        }

        toolbar.setTitle("选择租户");
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
//        toolbar.setNavigationOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        getActivity().onBackPressed();
//                    }
//                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_close) {
                    getActivity().onBackPressed();
                }
                return true;
            }
        });

//        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_normal);

        initMenuRecyclerView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void initMenuRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 8);
        menuRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.5f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        menuAdapter = new HostServerAdapter(CashierApp.getAppContext(), null);
        menuAdapter.setOnAdapterLitener(new HostServerAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                HostServer entity = menuAdapter.getEntity(position);
                ZLogger.d("选择域名服务：" + JSON.toJSONString(entity));

                Intent data = new Intent();
                data.putExtra("hostServer", entity);
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
        menuAdapter.setEntityList(getAdminMenus());
    }

    /**
     * 获取菜单
     */
    public synchronized List<HostServer> getAdminMenus() {
        List<HostServer> functionalList = new ArrayList<>();
        functionalList.add(new HostServer(1L,
                "米西厨房", "admin.mixicook.com",
                "http://admin.mixicook.com/pmc",
                "http://mobile.mixicook.com/mfhmobile/mobile/api", R.mipmap.ic_launcher));
//        functionalList.add(new HostServer(2L,
//                "满分家园", "admin.manfenjiayuan.com",
//                "http://admin.manfenjiayuan.com/pmc",
//                "http://mobile.manfenjiayuan.com/mfhmobile/mobile/api", R.mipmap.ic_launcher));

        return functionalList;
    }

}
