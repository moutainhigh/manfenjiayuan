package com.mfh.litecashier.ui.fragment.goods;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.bingshanguxue.cashier.database.service.PosLocalCategoryService;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.category.ScCategoryInfoApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.service.DataSyncManagerImpl;
import com.mfh.litecashier.ui.dialog.ModifyLocalCategoryDialog;
import com.mfh.litecashier.ui.dialog.TextInputDialog;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * POS-本地前台类目
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class LocalFrontCategoryFragment extends BaseFragment {
    @Bind(R.id.tab_category_goods)
    TopSlidingTabStrip mCategoryGoodsTabStrip;
    @Bind(R.id.viewpager_category_goods)
    ViewPager mCategoryGoodsViewPager;
    private TopFragmentPagerAdapter categoryGoodsPagerAdapter;

    private List<PosLocalCategoryEntity> curCategoryList;//当前子类目

    private ModifyLocalCategoryDialog mModifyLocalCategoryDialog = null;
    private TextInputDialog mTextInputDialog = null;

    public static LocalFrontCategoryFragment newInstance(Bundle args) {
        LocalFrontCategoryFragment fragment = new LocalFrontCategoryFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_local_frontcategory;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initCategoryGoodsView();

        reload();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataChanged(mCategoryGoodsTabStrip.getCurrentPosition());
            }
        }, 1000);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 通知刷新数据
     * */
    private void notifyDataChanged(int page){
        ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(page);
        if (viewPageInfo != null) {
            Bundle args = viewPageInfo.args;
            EventBus.getDefault().post(
                    new LocalFrontCategoryGoodsEvent(LocalFrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, args));
        }
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(DataSyncManagerImpl.DataSyncEvent event) {
        ZLogger.d(String.format("DataSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncManagerImpl.DataSyncEvent.EVENT_FRONTEND_CATEGORY_UPDATED) {
            reload();
//            int oldIndex = mCategoryGoodsTabStrip.getCurrentPosition();
//            ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(oldIndex);
//            if (viewPageInfo != null) {
//                Long categoryId = viewPageInfo.args.getLong(LocalFrontCategoryGoodsFragment.KEY_CATEGORY_ID);
//                PosLocalCategoryEntity categoryEntity = PosLocalCategoryService.get().getEntityById(String.valueOf(categoryId));
//                if (categoryEntity == null){
//                    reload();
//                }
//                mCategoryGoodsViewPager.setCurrentItem(oldIndex);
//            }
        } else if (event.getEventId() == DataSyncManagerImpl.DataSyncEvent.EVENT_PRODUCT_CATALOG_UPDATED) {
            notifyDataChanged(mCategoryGoodsTabStrip.getCurrentPosition());
        }
    }

    private void initCategoryGoodsView() {
        mCategoryGoodsTabStrip.setOnClickTabListener(new TopSlidingTabStrip.OnClickTabListener() {
            @Override
            public void onClickTab(View tab, int index) {
            }

            @Override
            public void onLongClickTab(View tab, int index) {
                ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(index);
                if (viewPageInfo != null) {
                    ZLogger.d(StringUtils.decodeBundle(viewPageInfo.args));
                    changeName(viewPageInfo.args.getLong(LocalFrontCategoryGoodsFragment.KEY_CATEGORY_ID));
                } else {
                    ZLogger.d("no tabs");
                }
            }
        });
        mCategoryGoodsTabStrip.setClickEnabled(true);
        mCategoryGoodsTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyDataChanged(page);
            }
        });

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text_80);
    }

    private void reload() {
        curCategoryList = PosLocalCategoryService.get().queryAll(null, null);

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        for (PosLocalCategoryEntity category : curCategoryList) {
            Bundle args = new Bundle();
            args.putLong(LocalFrontCategoryGoodsFragment.KEY_CATEGORY_ID, category.getId());

            mTabs.add(new ViewPageInfo(category.getName(), category.getName(),
                    LocalFrontCategoryGoodsFragment.class, args));
//            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), FrontCategoryGoodsFragment.class, args));
        }
        categoryGoodsPagerAdapter.removeAll();
        categoryGoodsPagerAdapter.addAllTab(mTabs);

        mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());
        if (mCategoryGoodsViewPager.getCurrentItem() == 0) {
            notifyDataChanged(0);
        } else {
            mCategoryGoodsViewPager.setCurrentItem(0, false);
        }
    }


    /**
     * 新增类目
     */
    @OnClick(R.id.ib_add)
    public void addCategory() {
        ZLogger.d("新增类目");
        final Long parentId = SharedPreferencesHelper.getLong(SharedPreferencesHelper.PK_L_CATETYPE_POS_ID, 0L);
        if (parentId.equals(0L)){
            DialogUtil.showHint("请先创建根目录");
            return;
        }

        if (mTextInputDialog == null) {
            mTextInputDialog = new TextInputDialog(getActivity());
            mTextInputDialog.setCancelable(false);
            mTextInputDialog.setCanceledOnTouchOutside(false);
        }
        mTextInputDialog.initialize("添加栏目", "请输入栏目名称", false,
                new TextInputDialog.OnTextInputListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm(String text) {
                        createCategoryInfo(parentId, text);
                    }
                });
        if (!mTextInputDialog.isShowing()) {
            mTextInputDialog.show();
        }
    }

    /**
     * 创建前台类目
     */
    private void createCategoryInfo(Long parentId, final String nameCn) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        ScCategoryInfoApi.create(parentId, CateApi.DOMAIN_TYPE_PROD,
                CateApi.CATE_POSITION_FRONT, MfhLoginService.get().getSpid(),
                nameCn, null, createRC);
    }

    private NetCallBack.NetTaskCallBack createRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("创建前台类目失败, " + errMsg);
                    hideProgressDialog();
                }

                @Override
                public void processResult(IResponseData rspData) {
                    //新建类目成功，保存类目信息，并触发同步。
                    try {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String result = retValue.getValue();
                            Long code = Long.valueOf(result);
                            ZLogger.df("新建前台类目成功:" + code);

                            DataSyncManagerImpl.get().sync(DataSyncManagerImpl.SYNC_STEP_FRONTEND_CATEGORY);
                        }

                    } catch (Exception e) {
                        ZLogger.ef(e.toString());
                    }
                    hideProgressDialog();
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    /**
     * 修改类目名称
     */
    private void changeName(Long id) {
        if (id == null) {
            return;
        }
        PosLocalCategoryEntity categoryEntity = PosLocalCategoryService.get()
                .getEntityById(String.valueOf(id));
        if (categoryEntity == null) {
            ZLogger.d("类目无效");
            return;
        }

        if (mModifyLocalCategoryDialog == null) {
            mModifyLocalCategoryDialog = new ModifyLocalCategoryDialog(getActivity());
            mModifyLocalCategoryDialog.setCanceledOnTouchOutside(true);
            mModifyLocalCategoryDialog.setCancelable(false);
        }

        mModifyLocalCategoryDialog.init(categoryEntity, new ModifyLocalCategoryDialog.DialogListener() {
            @Override
            public void onComplete() {
                reload();
            }
        });

        if (!mModifyLocalCategoryDialog.isShowing()) {
            mModifyLocalCategoryDialog.show();
        }
    }

}