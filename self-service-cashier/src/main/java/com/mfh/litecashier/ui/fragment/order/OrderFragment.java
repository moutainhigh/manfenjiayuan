package com.mfh.litecashier.ui.fragment.order;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bingshanguxue.cashier.hardware.printer.PrinterAgent;
import com.bingshanguxue.cashier.hardware.printer.PrinterContract;
import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.mfh.framework.api.pmcstock.PosOrder;
import com.bingshanguxue.vector_uikit.slideTab.TopFragmentPagerAdapter;
import com.bingshanguxue.vector_uikit.slideTab.TopSlidingTabStrip;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.widget.CustomViewPager;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.service.DataUploadManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 订单列表
 * Created by bingshanguxue on 15/8/31.
 */
public class OrderFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tab_order)
    TopSlidingTabStrip paySlidingTabStrip;
    @BindView(R.id.viewpager_order)
    CustomViewPager mViewPager;
    private TopFragmentPagerAdapter viewPagerAdapter;

//    @BindView(R.id.animProgressBar)
//    ProgressBar progressBar;
    @BindView(R.id.order_goods_list)
    RecyclerView goodsRecyclerView;
    private PosOrderItemsAdapter goodsAdapter;

    @BindView(R.id.tv_goods_quantity)
    TextView tvGoodsQunatity;
    @BindView(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @BindView(R.id.button_print)
    Button btnPrint;

    private PosOrder mPosOrder;
    private int printTimes = 1;//打印次数，默认是1

    public static OrderFragment newInstance(Bundle args) {
        OrderFragment fragment = new OrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_order;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("订单列表");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
// Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_reload) {
                    reload();
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_order);

        initTabs();
        initGoodsRecyclerView();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyOrderRefresh(paySlidingTabStrip.getCurrentPosition());
            }
        }, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 确认订单&打印订单明细
     */
    @OnClick(R.id.button_print)
    public void printOrder() {
        btnPrint.setEnabled(false);
        PrinterFactory.getPrinterManager().printPosOrder(mPosOrder, printTimes);
        btnPrint.setEnabled(true);
    }


    private void initTabs() {
        mViewPager.setScrollEnabled(true);
        paySlidingTabStrip.setOnClickTabListener(null);
        paySlidingTabStrip.setOnPagerChange(new TopSlidingTabStrip.OnPagerChangeLis() {
            @Override
            public void onChanged(int page) {
                notifyOrderRefresh(page);
            }
        });
        viewPagerAdapter = new TopFragmentPagerAdapter(getChildFragmentManager(),
                paySlidingTabStrip, mViewPager, R.layout.tabitem_text);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();

        Bundle args0 = new Bundle();
        args0.putInt(PosOrderFragment.EXTRA_KEY_BTYPE, BizType.POS);
        args0.putString(PosOrderFragment.EXTRA_KEY_SUBTYPES, "0");
        args0.putString(PosOrderFragment.EXTRA_KEY_ORDERSTATUS,
                String.valueOf(Constants.ORDER_STATUS_RECEIVED));
        args0.putString(PosOrderFragment.EXTRA_KEY_SELLOFFICES,
                String.valueOf(MfhLoginService.get().getCurOfficeId()));
        mTabs.add(new ViewPageInfo("门店订单", "门店订单", PosOrderFragment.class, args0));

        Bundle args1 = new Bundle();
        args1.putInt(PosOrderFragment.EXTRA_KEY_BTYPE, BizType.SC);
        args1.putString(PosOrderFragment.EXTRA_KEY_ORDERSTATUS,
                String.valueOf(Constants.ORDER_STATUS_DELIVER));
        args1.putString(PosOrderFragment.EXTRA_KEY_SELLOFFICES,
                String.valueOf(MfhLoginService.get().getCurOfficeId()));
        mTabs.add(new ViewPageInfo("平台订单", "平台订单", PosOrderFragment.class, args1));

        Bundle args2 = new Bundle();
        args2.putInt(PosOrderFragment.EXTRA_KEY_BTYPE, BizType.POS);
        args2.putString(PosOrderFragment.EXTRA_KEY_SUBTYPES, "5,6,7,8,9,10");
        args2.putString(PosOrderFragment.EXTRA_KEY_ORDERSTATUS,
                String.valueOf(Constants.ORDER_STATUS_RECEIVED));
        args2.putString(PosOrderFragment.EXTRA_KEY_SELLOFFICES,
                String.valueOf(MfhLoginService.get().getCurOfficeId()));
        mTabs.add(new ViewPageInfo("外部订单", "外部订单", PosOrderFragment.class, args2));

        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(mTabs.size());
    }

    private void notifyOrderRefresh(int index) {
        reloadOrderItems(null);

        Bundle args = new Bundle();
        if (index == 0) {
            printTimes = PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.CASHIER_ORDER);
            args.putInt("bizType", BizType.POS);
            args.putString(PosOrderFragment.EXTRA_KEY_SUBTYPES, "0");
        } else if (index == 1) {
            printTimes = PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.SEND_ORDER);
            args.putInt("bizType", BizType.SC);
//            args.putString(FreshScheduleOrderFragment.EXTRA_KEY_STATUS,
//                    String.valueOf(InvOrderApi.ORDER_STATUS_CONFIRM));
        } else if (index == 2) {
            printTimes = PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.SEND_ORDER_3P);
            args.putInt("bizType", BizType.POS);
            args.putString(PosOrderFragment.EXTRA_KEY_SUBTYPES, "5,6,7,8,9,10");
        }
        EventBus.getDefault().post(new OrderEvent(OrderEvent.EVENT_ID_RELOAD_DATA, args));
    }

    private void initGoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));

        goodsAdapter = new PosOrderItemsAdapter(CashierApp.getAppContext(), null);
        this.goodsRecyclerView.setAdapter(goodsAdapter);
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OrderEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();
        ZLogger.d(String.format("OrderEvent(%d-%s)", eventId, StringUtils.decodeBundle(args)));
        if (eventId == OrderEvent.EVENT_ID_LOAD_POSORDER_ITEMS) {
            PosOrder posOrder = (PosOrder) args.getSerializable("order");
            reloadOrderItems(posOrder);
        }
    }

    /**
     * 重新加载数据
     */
    private void reload() {
        int index = paySlidingTabStrip.getCurrentPosition();
        if (index == 0 || index == 2) {
            DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
        }
        notifyOrderRefresh(index);
    }

    /**
     * 加载订单明细
     * */
    private void reloadOrderItems(PosOrder posOrder){
        mPosOrder = posOrder;
        if (mPosOrder != null) {
            tvGoodsQunatity.setText(MUtils.formatDouble("商品数", "：", mPosOrder.getBcount(), "无", null, null));
            tvTotalAmount.setText(MUtils.formatDouble("商品金额", "：", mPosOrder.getAmount(), "无", null, null));
            btnPrint.setEnabled(true);
            goodsAdapter.setEntityList(mPosOrder.getItems());

        } else {
            tvGoodsQunatity.setText(MUtils.formatDouble("商品数", "：", null, "无", null, null));
            tvTotalAmount.setText(MUtils.formatDouble("商品金额", "：", null, "无", null, null));
            btnPrint.setEnabled(false);
            goodsAdapter.setEntityList(null);

        }
    }
}
