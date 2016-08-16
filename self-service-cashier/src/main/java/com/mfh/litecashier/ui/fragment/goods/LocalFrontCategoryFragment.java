package com.mfh.litecashier.ui.fragment.goods;


import android.os.Bundle;
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
import com.mfh.framework.api.category.CateApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.ui.activity.FragmentActivity;
import com.mfh.litecashier.ui.dialog.ModifyLocalCategoryDialog;
import com.mfh.litecashier.ui.dialog.TextInputDialog;

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
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initCategoryGoodsView();

        reload();
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
                    changeName(viewPageInfo.args.getLong("id"));
                } else {
                    ZLogger.d("no tabs");
                }
            }
        });
        mCategoryGoodsTabStrip.setClickEnabled(true);
        mCategoryGoodsTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                Long categoryId = curCategoryList.get(page).getId();
                EventBus.getDefault().post(new FrontCategoryGoodsEvent(FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
            }
        });

        categoryGoodsPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                mCategoryGoodsTabStrip, mCategoryGoodsViewPager, R.layout.tabitem_text);
    }


    private void reload() {
        curCategoryList = PosLocalCategoryService.get().queryAll(null, null);

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        for (PosLocalCategoryEntity category : curCategoryList) {
            Bundle args = new Bundle();
            args.putLong("id", category.getId());

            mTabs.add(new ViewPageInfo(category.getName(), category.getName(),
                    LocalFrontCategoryGoodsFragment.class, args));
//            mTabs.add(new ViewPageInfo(category.getNameCn(), category.getNameCn(), FrontCategoryGoodsFragment.class, args));
        }
        categoryGoodsPagerAdapter.removeAll();
        categoryGoodsPagerAdapter.addAllTab(mTabs);

        mCategoryGoodsViewPager.setOffscreenPageLimit(mTabs.size());
        if (mCategoryGoodsViewPager.getCurrentItem() == 0) {
            //如果直接加载，可能会出现加载两次的问题
            if (curCategoryList != null && curCategoryList.size() > 0) {
                Long categoryId = curCategoryList.get(0).getId();

                EventBus.getDefault().post(new FrontCategoryGoodsEvent(FrontCategoryGoodsEvent.EVENT_ID_RELOAD_DATA, categoryId));
            }
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
        if (mTextInputDialog == null) {
            mTextInputDialog = new TextInputDialog(getActivity());
            mTextInputDialog.setCancelable(false);
            mTextInputDialog.setCanceledOnTouchOutside(false);
        }
        mTextInputDialog.initialize("添加栏目", "请输入栏目名称", true,
                new TextInputDialog.OnTextInputListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm(String text) {
                        createCategoryInfo(text);
                    }
                });
        if (!mTextInputDialog.isShowing()) {
            mTextInputDialog.show();
        }
    }

    /**
     * 创建前台类目
     */
    private void createCategoryInfo(final String nameCn) {
        NetCallBack.NetTaskCallBack createRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("创建前台类目失败, " + errMsg);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        //新建类目成功，保存类目信息，并触发同步。
                        try {
                            if (rspData == null) {
                                return;
                            }

                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String result = retValue.getValue();
                            Long code = Long.valueOf(result);

                            DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_FRONTEND_CATEGORY);
//                            if (code != null) {
//                                // TODO: 8/15/16
//                                PosLocalCategoryEntity entity = new PosLocalCategoryEntity();
//                                entity.setName(nameCn);
//                                entity.setId(code);
//                                PosLocalCategoryService.get().saveOrUpdate(entity);
//
//                                reload();
//                            }
                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CateApiImpl.createScCategoryInfo(CateApi.DOMAIN_TYPE_PROD,
                CateApi.CATE_POSITION_FRONT, MfhLoginService.get().getSpid(),
                nameCn, createRC);
    }

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

    /**
     * 添加更多商品
     */
    @OnClick(R.id.fab_add_more)
    public void addMoreGoods() {
        PosCategoryGodosTempService.getInstance().clear();

        Bundle extras = new Bundle();
        extras.putInt(FragmentActivity.EXTRA_KEY_SERVICE_TYPE,
                FragmentActivity.FT_ADDMORE_LOCALFRONTGOODS);
        ViewPageInfo viewPageInfo = categoryGoodsPagerAdapter.getTab(mCategoryGoodsTabStrip.getCurrentPosition());
        if (viewPageInfo != null) {
            extras.putLong(FrontCategoryFragment.EXTRA_CATEGORY_ID_POS, viewPageInfo.args.getLong("id"));
        }
        extras.putLong(FrontCategoryFragment.EXTRA_CATEGORY_ID, CateApi.FRONT_CATEGORY_ID_POS);

        UIHelper.startActivity(getActivity(), FragmentActivity.class, extras);
    }
}