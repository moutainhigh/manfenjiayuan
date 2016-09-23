package com.bingshanguxue.almigod.home;


import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.almigod.R;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.uikit.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class HomeFragment extends BaseFragment {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.menu_option)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private HomeAdapter menuAdapter;


    public static HomeFragment newInstance(Bundle args) {
        HomeFragment fragment = new HomeFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        initMenus();

    }

    /**
     * 初始化快捷菜单
     */
    private void initMenus() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 3);
        menuRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));

        menuRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

        menuAdapter = new HomeAdapter(getActivity(), null);
        menuAdapter.setOnAdapterLitener(new HomeAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onCommandSelected(HomeMenu option) {
                processMenuOption(option.getId());
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);

        configMenuOptions();
    }

    private void configMenuOptions() {
        MfhUserManager.getInstance().updateModules();

        List<HomeMenu> menus = new ArrayList<>();

        menus.add(new HomeMenu(HomeMenu.OPTION_ID_REMOTECONTROL,
                "远程控制", R.mipmap.ic_launcher));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_STORE_IN,
//                "商品建档", R.mipmap.ic_goods_storein));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_TAKE,
//                "盘点", R.mipmap.ic_stocktake));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_BIND_GOODS_2_TAGS,
//                "电子价签", R.mipmap.ic_bind_tags));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_DISTRIBUTION,
//                "收货", R.mipmap.ic_receive_goods));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_RETURNORDER,
//                "退货", R.mipmap.ic_return_goods));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_PICK_GOODS,
//                "拣货", R.mipmap.ic_pick_goods));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_OUT,
//                "出库", R.mipmap.ic_stock_out));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_IN,
//                "入库", R.mipmap.ic_stock_in));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_LOSSORDER,
//                "报损", R.mipmap.ic_report_loss));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_PRINT_TAGS,
//                "价签打印", R.mipmap.ic_print_tags));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_PACKAGE,
//                "取包裹", R.mipmap.ic_package));
//        menus.add(new HomeMenu(HomeMenu.OPTION_ID_INV_CONVERT,
//                "库存转换", R.mipmap.ic_inv_convert));

        menuAdapter.setEntityList(menus);
    }

    private void processMenuOption(Long id) {
        if (id == null) {
            return;
        }

//        if (id.compareTo(HomeMenu.OPTION_ID_REMOTECONTROL) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_GOODS);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        }
//        else if (id.compareTo(HomeMenu.OPTION_ID_STORE_IN) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_STORE_IN);
//            extras.putInt(ScSkuGoodsStoreInFragment.EXTRA_STORE_TYPE, StoreType.SUPERMARKET);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        } else if (id.compareTo(HomeMenu.OPTION_ID_PACKAGE) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_PACKAGE);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        } else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_TAKE) == 0) {
//            Office office = DataCacheHelper.getInstance().getCurrentOffice();
//            if (office == null) {
//                DialogUtil.showHint("请先选择网点");
//                return;
//            }
//
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_INVENTORY_CHECK);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        } else if (id.compareTo(HomeMenu.OPTION_ID_DISTRIBUTION) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_DISTRIBUTION);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        } else if (id.compareTo(HomeMenu.OPTION_ID_BIND_GOODS_2_TAGS) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_BIND_GOODS_2_TAGS);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        } else if (id.compareTo(HomeMenu.OPTION_ID_CREATE_INV_LOSSORDER) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_CREATE_INV_LOSSORDER);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        } else if (id.compareTo(HomeMenu.OPTION_ID_CREATE_INV_RETURNORDER) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_CREATE_INV_RETURNORDER);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        } else if (id.compareTo(HomeMenu.OPTION_ID_INV_CONVERT) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_INV_CONVERT);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        } else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_IN) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_INVIO_IN);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        }  else if (id.compareTo(HomeMenu.OPTION_ID_STOCK_OUT) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_INVIO_OUT);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        }  else if (id.compareTo(HomeMenu.OPTION_ID_PRINT_TAGS) == 0) {
//            Bundle extras = new Bundle();
////                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//            extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_PRINT_PRICETAGS);
//            PrimaryActivity.actionStart(MainActivity.this, extras);
//        }
        else {
            DialogUtil.showHint("开发君失踪了...");
        }
    }


}
