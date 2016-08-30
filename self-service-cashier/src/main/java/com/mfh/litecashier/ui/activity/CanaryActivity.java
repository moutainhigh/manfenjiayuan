package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import com.bingshanguxue.cashier.model.wrapper.LocalMenu;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.AdministratorMenuAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


/**
 * Canary
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CanaryActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.menulist)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private AdministratorMenuAdapter menuAdapter;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, CanaryActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_canary;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle("Canary");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CanaryActivity.this.onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_close) {
//                    CanaryActivity.this.onBackPressed();
//                }
//                return true;
//            }
//        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_empty);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(R.style.NewFlow);

        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initMenuRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_empty, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void initMenuRecyclerView() {
        mRLayoutManager = new GridLayoutManager(this, 8);
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

        menuAdapter = new AdministratorMenuAdapter(CashierApp.getAppContext(), null);
        menuAdapter.setOnAdapterLitener(new AdministratorMenuAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                LocalMenu entity = menuAdapter.getEntity(position);
                if (entity != null) {
                    responseMenu(entity.getId());
                }
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
        menuAdapter.setEntityList(getAdminMenus());
    }

    public synchronized List<LocalMenu> getAdminMenus() {
        List<LocalMenu> functionalList = new ArrayList<>();
        functionalList.add(new LocalMenu(LocalMenu.CANARY_MENU_GOODS,
                "库存", R.mipmap.ic_admin_menu_inventory));
        functionalList.add(new LocalMenu(LocalMenu.CANARY_MENU_ORDERFLOW,
                "流水", R.mipmap.ic_admin_menu_orderflow));
        functionalList.add(new LocalMenu(LocalMenu.CANARY_MENU_CANARY,
                "设置", R.mipmap.ic_admin_menu_settings));

        return functionalList;
    }

    /**
     * 固有功能
     */
    private void responseMenu(Long id) {
        if (id == null) {
            return;
        }
        if (id.compareTo(LocalMenu.CANARY_MENU_GOODS) == 0) {
            redirect2Goods();
        }
        else if (id.compareTo(LocalMenu.CANARY_MENU_ORDERFLOW) == 0) {
            redirect2Orderflow();
        }
        else if (id.compareTo(LocalMenu.CANARY_MENU_CANARY) == 0) {
            redirect2Canary();
        }  else {
            DialogUtil.showHint("@开发君 失踪了...");
        }
    }

    /**
     * 商品
     */
    public void redirect2Goods() {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_CANARY_GOODS);
        UIHelper.startActivity(this, SimpleActivity.class, extras);
    }

    /**
     * 流水
     */
    public void redirect2Orderflow() {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_CANARY_ORDERFLOW);
        UIHelper.startActivity(this, SimpleActivity.class, extras);
    }

    /**
     * 异常订单
     */
    public void redirect2Canary() {
        Bundle extras = new Bundle();
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE,
                SimpleActivity.FT_CANARY_CANARY);
        UIHelper.startActivity(this, SimpleActivity.class, extras);
    }


}
