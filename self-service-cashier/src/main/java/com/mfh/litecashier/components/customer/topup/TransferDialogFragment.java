package com.mfh.litecashier.components.customer.topup;

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

import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.BaseDialogFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.CustomerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

/**
 * <h1>会员充值</h1>
 * Created by bingshanguxue on 15/12/15.
 */
public class TransferDialogFragment extends BaseDialogFragment {
    public static final String EXTRA_KEY_HUMAN = "human";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.customer_view)
    CustomerView mCustomerView;
    @BindView(R.id.amountRecyclerView)
    RecyclerView mRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private TopupAdapter mTopupAdapter;
    @BindView(R.id.tabstrip_pay)
    TopSlidingTabStrip paySlidingTabStrip;
    @BindView(R.id.tab_viewpager)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

    private NumberInputDialog priceDialog = null;
    private Human mHuman;

    public interface OnDialogListener {
        void onSuccess();
        void onCancel();
    }

    private OnDialogListener mOnDialogListener;

    public void setOnDialogListener(OnDialogListener listener) {
        mOnDialogListener = listener;
    }


    public static TransferDialogFragment newInstance(Human human) {
        Bundle args = new Bundle();
        if (human != null) {
            args.putSerializable(EXTRA_KEY_HUMAN, human);
        }

        TransferDialogFragment fragment = new TransferDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static TransferDialogFragment newInstance(Bundle args) {
        TransferDialogFragment fragment = new TransferDialogFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }


    @Override
    protected int getDialogType() {
        return DIALOG_TYPE_MIDDLE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_topup;
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        ButterKnife.bind(this, rootView);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            ZLogger.d("打开充值页面 开始");
            Bundle args = getArguments();
            if (args != null) {
                mHuman = (Human) args.getSerializable(EXTRA_KEY_HUMAN);
            }

            toolbar.setTitle(R.string.title_transfer);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // Handle the menu item
                    int id = item.getItemId();
                    if (id == R.id.action_close) {
                        dismiss();
                    }
                    return true;
                }
            });
            // Inflate a menu to be displayed in the toolbar
            toolbar.inflateMenu(R.menu.menu_normal);

            initTabs();
            initRecyclerView();

            mCustomerView.reload(mHuman);
            ZLogger.d("打开充值页面 结束");
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e("打开充值页面 异常" + e.toString());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setCancelable(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 5);
        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));

//        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

        List<TopAmount> entities = new ArrayList<>();
        entities.add(new TopAmount(100D, 100D, false, true));
        entities.add(new TopAmount(200D, 200D, false, false));
        entities.add(new TopAmount(300D, 300D, false, false));
        entities.add(new TopAmount(400D, 400D, false, false));
        entities.add(new TopAmount(500D, 500D, false, false));
        entities.add(new TopAmount(600D, 600D, false, false));
        entities.add(new TopAmount(700D, 700D, false, false));
        entities.add(new TopAmount(800D, 800D, false, false));
        entities.add(new TopAmount(1000D, 1000D, false, false));
        entities.add(new TopAmount(0.01D, null, true, false));
        mTopupAdapter = new TopupAdapter(getActivity(), null);
        mTopupAdapter.setEntityList(entities);
        mTopupAdapter.setOnAdapterLitener(new TopupAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                topupStep1(mTopupAdapter.getEntity(position));
                chengePrice(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onDataSetChanged() {
                notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());
            }
        });
        mRecyclerView.setAdapter(mTopupAdapter);

    }

    private void initTabs() {
        //setupViewPager
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyPayInfoChanged(page);
//                if (page == 1 || page == 2 || page == 4 || page == 5) {
//                    if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
//                        DialogUtil.showHint("网络异常,请选择其他支付方式");
////                        paySlidingTabStrip.setSelected();
//                    }
//                }
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        Bundle parArgs = new Bundle();
        parArgs.putString(BaseTopupFragment.EXTRA_KEY_BIZ_TYPE,
                String.valueOf(BizType.RECHARGE));
//        parArgs.putString(BaseTopupFragment.EXTRA_KEY_BODY,
//                cashierOrderInfo.getBody());
//        parArgs.putString(BaseTopupFragment.EXTRA_KEY_SUBJECT,
//                );
//        parArgs.putDouble(BasePayFragment.EXTRA_KEY_HANDLE_AMOUNT,
//                CashierProvider.getHandleAmount(cashierOrderInfo));

        mTabs.add(new ViewPageInfo("现金", "现金", CustomerTopupFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("支付宝", "支付宝", AlipayTopupFragment.class,
                parArgs));
        mTabs.add(new ViewPageInfo("微信", "微信", WepayTopupFragment.class,
                parArgs));

        viewPagerAdapter.removeAll();
        viewPagerAdapter.addAllTab(mTabs);

        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(TopupActionEvent event) {
        ZLogger.d(String.format("TopupActionEvent:%d\n%s",
                event.getAction(), StringUtils.decodeBundle(event.getArgs())));
        switch (event.getAction()) {
            case TopupActionEvent.TOPUP_CUSTOMER: {
                Bundle bundle = event.getArgs();
                int topupType = bundle.getInt(TopupActionEvent.KEY_TOPUP_TYPE, -1);
                if (topupType == 1 && bundle.containsKey(TopupActionEvent.KEY_PAY_HUMANID)) {
                    customerTransfer(bundle.getLong(TopupActionEvent.KEY_PAY_HUMANID, -1L));
                }
            }
            break;
            case TopupActionEvent.TOPUP_PROCESS: {
                activeMode(false);
            }
            break;
            case TopupActionEvent.TOPUP_ERROR: {
                activeMode(true);
            }
            break;
            case TopupActionEvent.TOPUP_SUCCEED: {
                onTopupSuccess();
            }
            break;

        }
    }

    /**
     * */
    public void activeMode(boolean isActive) {
        try {
            setCancelable(!isActive);

            mViewPager.setScrollEnabled(isActive);
            paySlidingTabStrip.setClickEnabled(isActive);
            mRecyclerView.setEnabled(isActive);
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }

    /**
     * 通知：应付信息变化
     */
    private void notifyPayInfoChanged(int page) {
        final TopAmount topAmount = mTopupAdapter.getCurEntity();

        Intent intent = new Intent();
        Bundle extras = new Bundle();
        if (topAmount != null) {
            extras.putDouble(BaseTopupFragment.EXTRA_KEY_TOTAL_AMOUNT,
                    topAmount.getCurrent());
        }
        if (mHuman != null) {
            extras.putLong(BaseTopupFragment.EXTRA_KEY_CUSTOMER_ID,
                    mHuman.getId());
        }

        if (page == 0) {
            intent.setAction(Constants.BA_HANDLE_TOPUPAMOUNT_CHANGED_CUSTOMER);
//            curPayType = WayType.ALI_F2F;
        } else if (page == 1) {
            intent.setAction(Constants.BA_HANDLE_TOPUPAMOUNT_CHANGED_ALIPAY);
        } else if (page == 2) {
            intent.setAction(Constants.BA_HANDLE_TOPUPAMOUNT_CHANGED_WEPAY);
        }
        intent.putExtras(extras);
        getContext().sendBroadcast(intent);
    }


    /**
     * 自定义充值金额
     */
    public void chengePrice(final int position) {
        final TopAmount topAmount = mTopupAdapter.getEntity(position);
        if (topAmount == null || !topAmount.isEditabled()) {
//            mTopupAdapter.notifyDataSetChanged(false);
            notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());

            return;
        }

        if (priceDialog == null) {
            priceDialog = new NumberInputDialog(getActivity());
            priceDialog.setCancelable(false);
            priceDialog.setCanceledOnTouchOutside(false);
        }

        priceDialog.initializeBarcode(EditInputType.PRICE, "自定义", "充值金额", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {

                    }

                    @Override
                    public void onNext(Double value) {
                        topAmount.setCurrent(value);
                        mTopupAdapter.notifyItemChanged(position);
                        notifyPayInfoChanged(paySlidingTabStrip.getCurrentPosition());
//                        mTopupAdapter.notifyDataSetChanged(true);
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        if (!priceDialog.isShowing()) {
            priceDialog.show();
        }
    }

    /**
     * 转账
     */
    public void customerTransfer(Long payHumanId) {
        onLoadProcess("请稍候...");

        if (mHuman == null) {
            onLoadError(getString(R.string.tip_cannot_find_customer));
            return;
        }

        TopAmount amount = mTopupAdapter.getCurEntity();
        if (amount == null || amount.getCurrent() < 0D) {
            onLoadError("请选择充值金额。");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，请重新尝试。");
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("isCash", "1");
        options.put("amount", String.valueOf(amount.getCurrent()));
        options.put("receiveHumanId", String.valueOf(mHuman.getId()));
        options.put("payHumanId", String.valueOf(payHumanId));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        //{"code":"1","msg":"个人代充值必须是当前登录用户!","data":null,"version":1}
        CommonUserAccountHttpManager.getInstance().transferFromMyAccount(options,
                new Subscriber<UserAccount>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
//                        etPayCode.getText().clear();
                        onLoadError(e.getMessage());
                    }

                    @Override
                    public void onNext(UserAccount userAccount) {
                        if (userAccount == null) {
                            onLoadError("充值失败：");
                        } else {
//                            ZLogger.d(String.format("充值成功:%d-%d", userAccount.getId(), userAccount.getOwnerId()));
                            onTopupSuccess();
                        }
                    }


                });
    }


    public void onLoadProcess(String description) {
        activeMode(false);
    }

    public void onLoadFinished() {
        activeMode(true);
    }

    public void onLoadError(String errMessage) {
        DialogUtil.showHint(errMessage);
        activeMode(true);
    }

    /**
     * 充值成功
     */
    private void onTopupSuccess() {
        DialogUtil.showHint("充值成功");

        activeMode(true);

        if (mOnDialogListener != null) {
            mOnDialogListener.onSuccess();
        }
        dismiss();
    }

}
