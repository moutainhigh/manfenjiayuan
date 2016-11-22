package com.mfh.litecashier.ui.fragment.cashier;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.invOrder.CashierApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.bean.wrapper.AdvertiseBean;
import com.mfh.litecashier.bean.wrapper.CashierFunctional;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfo;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfoWrapper;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.entity.CommonlyGoodsEntity;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;
import com.mfh.litecashier.database.entity.PosProductEntity;
import com.mfh.litecashier.database.logic.PosOrderItemService;
import com.mfh.litecashier.database.logic.PosOrderService;
import com.mfh.litecashier.event.AddCategoryGoodsEvent;
import com.mfh.litecashier.event.AddCommonlyGoodsEvent;
import com.mfh.litecashier.event.AddLaunchGoodsEvent;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.event.CashierAffairEvent;
import com.mfh.litecashier.presenter.CashierPresenter;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.service.OrderSyncManager;
import com.mfh.litecashier.ui.activity.CashierPayActivity;
import com.mfh.litecashier.ui.activity.ServiceActivity;
import com.mfh.litecashier.ui.activity.SimpleActivity;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.AdvertisementPagerAdapter;
import com.mfh.litecashier.ui.adapter.CashierServiceMenuAdapter;
import com.mfh.litecashier.ui.adapter.CashierSwipAdapter;
import com.mfh.litecashier.ui.dialog.ChangeQuantityDialog;
import com.mfh.litecashier.ui.dialog.DefectiveDialog;
import com.mfh.litecashier.ui.dialog.ExpressDialog;
import com.mfh.litecashier.ui.dialog.HangupOrderDialog;
import com.mfh.litecashier.ui.dialog.InitCardDialog;
import com.mfh.litecashier.ui.dialog.LaundryDialog;
import com.mfh.litecashier.ui.dialog.QueryDialog;
import com.mfh.litecashier.ui.dialog.ReceiveGoodsDialog;
import com.mfh.litecashier.ui.dialog.ReturnGoodsDialog;
import com.mfh.litecashier.ui.dialog.TopupDialog;
import com.mfh.litecashier.ui.fragment.inventory.CreateInventoryTransOrderFragment;
import com.mfh.litecashier.ui.fragment.inventory.StockScSkuGoodsFragment;
import com.mfh.litecashier.ui.fragment.purchase.CreatePurchaseReceiptOrderFragment;
import com.mfh.litecashier.ui.fragment.purchase.CreatePurchaseReturnOrderFragment;
import com.mfh.litecashier.ui.view.ICashierView;
import com.mfh.litecashier.ui.widget.AdvertisementViewPager;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.AnalysisHelper;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.DataCacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 收银页面＋服务台
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CashierFragment extends BaseFragment
        implements ICashierView {

    @Bind(R.id.tv_quantity)
    TextView tvQuantity;
    @Bind(R.id.tv_amount)
    TextView tvAmount;
    @Bind(R.id.tv_last_amount)
    TextView tvLastAmount;
    @Bind(R.id.tv_last_quantity)
    TextView tvLastQuantity;
    @Bind(R.id.tv_last_discount)
    TextView tvLastDiscount;
    @Bind(R.id.tv_last_charge)
    TextView tvLastCharge;
    @Bind(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;
    @Bind(R.id.button_hangup_order)
    Button btnHangupOrder;
    @Bind(R.id.button_settle)
    Button btnSettle;
    @Bind(R.id.product_list)
    RecyclerView productRecyclerView;
    @Bind(R.id.tv_service_title)
    TextView tvServiceTitle;
    @Bind(R.id.float_hangup)
    TextView fabHangup;

    private ItemTouchHelper itemTouchHelper;
    private CashierSwipAdapter productAdapter;

    private static final int SERVICE_SPAN_COUNT = 7;//服务端网格

    @Bind(R.id.category_list)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private CashierServiceMenuAdapter menuAdapter;
//    @Bind(R.id.functional_list)
//    RecyclerView functionalRecyclerView;
//    private CashierFunctionalAdapter functionalAdapter;

    @Bind(R.id.viewpager_adv)
    AdvertisementViewPager advertiseViewPager;
    private AdvertisementPagerAdapter advertisePagerAdapter;

    private QueryDialog expressDeliverDialog = null;
    private InitCardDialog initCardDialog = null;
    private TopupDialog topupDialog = null;
    private ReturnGoodsDialog returnGoodsDialog = null;
    private DefectiveDialog defectiveDialog = null;
    private ExpressDialog expressDialog = null;
    private LaundryDialog laundryDialog = null;
    private ChangeQuantityDialog changeQuantityDialog = null;
    private HangupOrderDialog hangupOrderDialog = null;
    private ReceiveGoodsDialog receiveGoodsDialog = null;

    /**
     * 当前订单条码
     */
    private String curOrderBarCode;
    /**
     * 当前业务类型，默认POS
     */
    private Integer curBizType = BizType.POS;

    private QueryDialog laundryVipDialog;//洗衣会员登录
    /**
     * 衣袋编号
     */
    private String packageCode;

    private CashierPresenter cashierPresenter;

    public static CashierFragment newInstance(Bundle args) {
        CashierFragment fragment = new CashierFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        cashierPresenter = new CashierPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            initBarCodeInput();
            initCashierRecyclerView();
//            initFunctionalRecyclerView();
            initShortcutRecyclerView();

            refreshFrontCategory();
            initCashierOrder();

            //刷新挂单
            refreshFloatHangup();

            //TODO,加载广告数据，然后再填充广告
            List<AdvertiseBean> advList = new ArrayList<>();
            //multi
            advList.add(AdvertiseBean.newInstance(AdvertiseBean.ADV_TYPE_MULTI, "http://resource.manfenjiayuan.cn/product/thumbnail_1294.jpg", "衣服", "秋冬热卖"));
            advList.add(AdvertiseBean.newInstance(AdvertiseBean.ADV_TYPE_MULTI, "http://chunchunimage.b0.upaiyun.com/product/3655.JPG!small", "可口可乐苏州分公司", "年终特卖会，330ml买3箱送一箱，还送50元代金券，还不马上购买!"));
            advList.add(AdvertiseBean.newInstance(AdvertiseBean.ADV_TYPE_MULTI, "http://chunchunimage.b0.upaiyun.com/product/6167.JPG!small", "面包", "新鲜刚出炉的面包，赶快来买!"));
            //simple
            advList.add(AdvertiseBean.newInstance(AdvertiseBean.ADV_TYPE_SIMPLE, "http://chunchunimage.b0.upaiyun.com/product/6167.JPG!small", "面包", "新鲜刚出炉的面包，赶快来买!"));
            advertisePagerAdapter = new AdvertisementPagerAdapter(getActivity(),
                    advList, null);
            advertiseViewPager.setAdapter(advertisePagerAdapter);

            //TODO,定时切换(每隔5秒切换一次)
            advertiseViewPager.startSlide(5 * 1000);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        autorequestFocus();

//        if (functionalRecyclerView != null) {
//            functionalRecyclerView.smoothScrollToPosition(0);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(CashierAffairEvent event) {
        ZLogger.d(String.format("CashierFragment: CashierAffairEvent(%d)", event.getAffairId()));
        //客显
        if (event.getAffairId() == CashierAffairEvent.EVENT_ID_RESET_CASHIER) {
            initCashierOrder();
        } else if (event.getAffairId() == CashierAffairEvent.EVENT_ID_START_CASHIER) {
            if (inlvBarcode != null) {
                inlvBarcode.requestFocus();
            }

            if (curBizType != BizType.POS){
                backToPosCashier();
            }
        }
    }

    public void onEventMainThread(DataSyncManager.DataSyncEvent event) {
        ZLogger.d(String.format("CashierFragment: DataSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncManager.DataSyncEvent.EVENT_ID_REFRESH_FRONT_CATEGORYINFO) {
            refreshFrontCategory();
        }
    }

    public void onEventMainThread(AffairEvent event) {
        ZLogger.d(String.format("CashierFragment: AffairEvent(%d)", event.getAffairId()));
        //有新订单
        if (event.getAffairId() == AffairEvent.EVENT_ID_HIDE_LAUNDRY) {
            backToPosCashier();
        }
    }

    /**
     * 切换到POS收银
     * */
    private void backToPosCashier(){
        curBizType = BizType.POS;
        packageCode = "";
        //TODO,是清空订单还是挂单
        clearOrder();

//            DialogUtil.showHint("正常收银");
    }

    public void onEventMainThread(AddCategoryGoodsEvent event) {
        addProduct(event.getGoods());
    }

    public void onEventMainThread(AddCommonlyGoodsEvent event) {
        addProduct(event.getGoods());
    }

    /**
     * 洗衣
     */
    public void onEventMainThread(AddLaunchGoodsEvent event) {
        addProduct(event.getGoods());
    }

    /**
     * 挂单
     */
    @OnClick(R.id.button_hangup_order)
    public void hangUpOrder() {
        inlvBarcode.clear();
        //清除屏幕上的字符
        SerialManager.clear();
        //TODO,清除客显屏幕

        if (curBizType.equals(BizType.LAUNDRY)) {
            DialogUtil.showHint("洗衣不能挂单");
            return;
        }

        //挂起当前订单
        boolean isSuccess = CashierHelper.hangUpCashierOrder(curOrderBarCode, curBizType, null,
                productAdapter.getEntityList());
        if (isSuccess) {
            //刷新挂单
            refreshFloatHangup();

            //重新生成订单
            validateCurOrderBarcode(null);
            productAdapter.setEntityList(null);
        }
    }

    /**
     * 结算(需要登录)
     */
    @OnClick(R.id.button_settle)
    public void settle() {
        //判断是否登录
        if (!MfhLoginService.get().haveLogined()) {
            DialogUtil.showHint("请先登录");
            return;
        }

        //判断当天是否日结
        if (AnalysisHelper.validateHaveDateEnd(new Date())) {
            DialogUtil.showHint("该网点今天已经日结，请先挂单。");
            return;
        }
        //TODO,洗衣订单恢复后会进入收银订单支付页面。

        //判断是洗衣订单还是收银订单
        // 洗衣订单支付,直接提交订单，不支付，默认货到付款
        if (curBizType.equals(BizType.LAUNDRY)) {
            //登录会员
            loginLaundryVip();
        }
        //收银订单支付
        else {
            DataCacheHelper.getInstance().setMfMemberInfo(null);

            //结算订单,更新购物车商品
            CashierOrderInfo cashierOrderInfo = CashierHelper.settleCashierOrder(curOrderBarCode,
                    curBizType, null, productAdapter.getEntityList());
            if (cashierOrderInfo == null) {
                DialogUtil.showHint("创建订单失败");
                return;
            }

            //显示客显
            updatePadDisplay(CashierOrderInfoWrapper.CMD_PAY_ORDER, cashierOrderInfo);

            Intent intent = new Intent(getActivity(), CashierPayActivity.class);
            Bundle extras = new Bundle();
            extras.putSerializable(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
            intent.putExtras(extras);
            startActivityForResult(intent, Constants.ARC_MFPAY);

        }
    }

    /**
     * 更新客显信息
     * */
    private void updatePadDisplay(int cmdType, CashierOrderInfo cashierOrderInfo){
        if (!SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PREF_KEY_PAD_CUSTOMERDISPLAY_ENABLED, false)){
            ZLogger.d("PAD客显功能未打开");
            return;
        }
        CashierOrderInfoWrapper cashierOrderInfoWrapper = new CashierOrderInfoWrapper();
        cashierOrderInfoWrapper.setCmdType(cmdType);
        cashierOrderInfoWrapper.setCashierOrderInfo(cashierOrderInfo);
        NetProcessor.ComnProcessor processor = new NetProcessor.ComnProcessor<EmbMsg>(){
            @Override
            protected void processOperResult(EmbMsg result){
//                doAfterSendSuccess(result);
                ZLogger.d("发送订单信息到客显成功");
            }
        };
        EmbMsgService msgService = ServiceFactory.getService(EmbMsgService.class, getContext());
        msgService.sendText(MfhLoginService.get().getCurrentGuId(),
                MfhLoginService.get().getCurrentGuId(),
                IMBizType.CUSTOMER_DISPLAY_PAYORDER, JSON.toJSONString(cashierOrderInfoWrapper), processor);
    }

    /**
     * 登录洗衣会员
     */
    public void loginLaundryVip() {
        if (laundryVipDialog == null) {
            laundryVipDialog = new QueryDialog(getActivity());
            laundryVipDialog.setCancelable(false);
            laundryVipDialog.setCanceledOnTouchOutside(false);
        }

        laundryVipDialog.init(QueryDialog.DT_MEMBER_CARD, new QueryDialog.DialogListener() {
            @Override
            public void query(String text) {

            }

            @Override
            public void onNextStep(String fee) {
            }

            @Override
            public void onNextStep(Human human) {
                saveLaundryOrder(human);
            }

            @Override
            public void onNextStep() {
            }
        });
        if (!laundryVipDialog.isShowing()) {
            laundryVipDialog.show();
        }
    }

    /**
     * 保存洗衣订单
     */
    private void saveLaundryOrder(Human human) {
        //跳转到不同的支付页面
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        List<PosOrderItemEntity> entityList = productAdapter.getEntityList();
        if (entityList == null || entityList.size() < 1) {
            return;
        }

        Long companyId = null;
        Double bCount = 0D;
        Double amount = 0D;
        //明细
        JSONArray itemsArray = new JSONArray();
        for (PosOrderItemEntity entity : entityList) {
            bCount += entity.getBcount();
            amount += entity.getAmount();
            if (companyId == null) {
                companyId = entity.getProviderId();
            }

            JSONObject itemJson = new JSONObject();
            itemJson.put("productId", entity.getProductId());
            itemJson.put("goodsId", entity.getGoodsId());
            itemJson.put("skuId", entity.getProSkuId());
            itemJson.put("bcount", entity.getBcount());
            itemJson.put("price", entity.getCostPrice());
            itemJson.put("remark", "");
            itemJson.put("amount", entity.getAmount());
            itemJson.put("adjustedPrice", 0D);
            itemJson.put("promotionPrice", 0D);
            itemJson.put("whereId", MfhLoginService.get().getCurOfficeId());
            itemsArray.add(itemJson);
        }

        //订单
        JSONObject orderJson = new JSONObject();
        orderJson.put("paystatus", 0);
        orderJson.put("remark", "");//备注
        orderJson.put("sellOffice", MfhLoginService.get().getCurOfficeId());//当前所属部门编号
        orderJson.put("btype", BizType.LAUNDRY);
        if (human != null) {
            orderJson.put("humanId", human.getGuid());//必须会员登录
            orderJson.put("mobile", human.getMobile());//必须会员登录
        }
        orderJson.put("companyId", companyId);//取第一个商品的providerId
        orderJson.put("payType", 0);//支付方式，0--货到付款
        orderJson.put("pack_bcode", packageCode);//衣袋编号
        orderJson.put("bcount", bCount);//商品数量
        orderJson.put("amount", amount);//商品金额
        orderJson.put("pack", 0);

        NetCallBack.NetTaskCallBack receiveResponseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                            {"code":"0","msg":"操作成功!","version":"1","data":""}
//                        RspValue<String> retValue = (RspValue<String>) rspData;
//                        String retStr = retValue.getValue();
//
//                        //出库成功:1-556637
//                        ZLogger.d("保存洗衣订单成功:" + retStr);

                        // CLOSE TO ENTER PACKE CODE
                        backToPosCashier();
                        //重新生成订单
                        validateCurOrderBarcode(null);

                        //显示找零
////        SerialManager.show(4, Math.abs(cashierOrderInfo.getHandleAmount()));
//                        SerialManager.vfdShow(String.format("Change:%.2f\r\nThank You!", Math.abs(cashierOrderInfo.getHandleAmount())));
//                        //打印订单
//                        final PosOrderEntity orderEntity = CashierHelper.findPosOrder(cashierOrderInfo.getOrderBarcode());
//                        SerialManager.printPosOrder(orderEntity, true);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("保存洗衣订单失败：" + errMsg);
//                        DialogUtil.showHint("");
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.saveLaundryOrder(orderJson.toJSONString(), itemsArray.toJSONString(), "", "", receiveResponseCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_MFPAY: {
                //清空会员信息
                DataCacheHelper.getInstance().setMfMemberInfo(null);

                if (resultCode == Activity.RESULT_OK && data != null) {
                    CashierOrderInfo cashierOrderInfo = (CashierOrderInfo) data.getSerializableExtra(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO);
                    processSettleResult(cashierOrderInfo);
                }
            }
            break;
            case Constants.ARC_CREATE_PURCHASE_GOODS: {
                if (resultCode == Activity.RESULT_OK) {
                    //TODO,新增商品成功，同步商品
                    DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理订单支付结果
     */
    private void processSettleResult(CashierOrderInfo cashierOrderInfo) {
        //刷新上一单信息
        refreshLastOrder(cashierOrderInfo.getRetailAmount(), cashierOrderInfo.getbCount(),
                cashierOrderInfo.getDiscountAmount(), Math.abs(cashierOrderInfo.getHandleAmount()));

        //重新生成订单
        validateCurOrderBarcode(null);
        productAdapter.setEntityList(null);

        //显示找零
//        SerialManager.show(4, Math.abs(cashierOrderInfo.getHandleAmount()));
        SerialManager.vfdShow(String.format("Change:%.2f\r\nThank You!",
                Math.abs(cashierOrderInfo.getHandleAmount())));

        //更新订单支付信息
//        CashierHelper.updateCashierOrder(cashierOrderInfo);

        PosOrderEntity orderEntity = CashierHelper.findPosOrder(cashierOrderInfo.getOrderBarcode());
        if (orderEntity != null) {
            //同步订单信息
            OrderSyncManager.get().stepUploadPosOrder(orderEntity);
            //打印订单
            SerialManager.printPosOrder(orderEntity, true);
        }

        updatePadDisplay(CashierOrderInfoWrapper.CMD_FINISH_ORDER, cashierOrderInfo);
    }


    @OnClick(R.id.float_hangup)
    public void showOrder() {
        if (hangupOrderDialog == null) {
            hangupOrderDialog = new HangupOrderDialog(getActivity());
            hangupOrderDialog.setCancelable(true);
            hangupOrderDialog.setCanceledOnTouchOutside(true);
        }
        hangupOrderDialog.init(new HangupOrderDialog.OnResponseCallback() {

            @Override
            public void onResumeOrder(String orderBarCode) {
                resumeOrder(orderBarCode);
            }
        });
        if (!hangupOrderDialog.isShowing()) {
            hangupOrderDialog.show();
        }
    }

    /**
     * 获取焦点
     */
    public void autorequestFocus() {
        if (inlvBarcode != null) {
            inlvBarcode.clear();
            inlvBarcode.requestFocus();
        } else {
            if (rootView != null) {
                inlvBarcode = (InputNumberLabelView) rootView.findViewById(R.id.inlv_barcode);
                inlvBarcode.clear();
                inlvBarcode.requestFocus();
            }
        }
    }

    /**
     * 初始化商品列表
     */
    private void initCashierRecyclerView() {
        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        productRecyclerView.setHasFixedSize(true);
        //设置Item增加、移除动画
        productRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //分割线
        productRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));

        productAdapter = new CashierSwipAdapter(getActivity(), null);
        productAdapter.setOnAdapterListener(new CashierSwipAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged(boolean needScroll) {
                tvQuantity.setText(String.format("%.2f", productAdapter.getBcount()));
                tvAmount.setText(String.format("%.2f", productAdapter.getFinalAmount()));//成交价

                if (productAdapter.getItemCount() > 0) {
                    btnSettle.setEnabled(true);
                    btnHangupOrder.setEnabled(true);
                } else {
                    //清除屏幕上的字符
                    SerialManager.clear();
                    //TODO,清除客显屏幕

                    btnSettle.setEnabled(false);
                    btnHangupOrder.setEnabled(false);
                }
                if (needScroll) {
                    //后来者居上
                    productRecyclerView.scrollToPosition(0);
                }
            }
        });


        ItemTouchHelper.Callback callback = new MyItemTouchHelper(productAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        //关联到RecyclerView
        itemTouchHelper.attachToRecyclerView(productRecyclerView);

        // specify an adapter
        productRecyclerView.setAdapter(productAdapter);
    }


    /**
     * 初始化功能区列表
     */
//    private void initFunctionalRecyclerView() {
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        functionalRecyclerView.setLayoutManager(linearLayoutManager);
////        GridLayoutManager mRLayoutManager = new GridLayoutManager(getActivity(), SERVICE_SPAN_COUNT);
//////        mRLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
////        menuRecyclerView.setLayoutManager(mRLayoutManager);
//        //enable optimizations if all item views are of the same height and width for
//        //signficantly smoother scrolling
//        functionalRecyclerView.setHasFixedSize(true);
//        //添加分割线
////        functionalRecyclerView.addItemDecoration(new LineItemDecoration(
////                getActivity(), LineItemDecoration.HORIZONTAL_LIST));
//
//        functionalAdapter = new CashierFunctionalAdapter(CashierApp.getAppContext(), null);
//        functionalAdapter.setOnAdapterLitener(new CashierFunctionalAdapter.AdapterListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                CashierFunctional entity = functionalAdapter.getEntity(position);
//                if (entity == null) {
//                    return;
//                }
//
//                responsePrivateFunction(entity.getId());
//
//            }
//        });
//
//        functionalRecyclerView.setAdapter(functionalAdapter);
//
//        List<CashierFunctional> localList = new ArrayList<>();
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PURCHASE_SEND, "采购订单", R.mipmap.ic_service_purchase_send));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PURCHASE_RECEIPT, "采购收货", R.mipmap.ic_service_purchase_receipt));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PURCHASE_RETURN, "采购退货", R.mipmap.ic_service_purchase_return));
////        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_INVENTORY_TRANS_IN, "调入", R.mipmap.ic_service_inventory_trans_in));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_INVENTORY_TRANS_OUT, "调拨", R.mipmap.ic_service_inventory_trans));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_MALL, "进货优惠", R.mipmap.ic_service_mall));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PURCHASE_INTELLIGENT, "智能订货", R.mipmap.ic_service_intelligent_order));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RETURN_GOODS, "退货", R.mipmap.ic_service_returngoods));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_MEMBER_CARD, "会员卡", R.mipmap.ic_service_membercard));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RECHARGE, "会员充值", R.mipmap.ic_service_recharge));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_STORE_PROMOTION, "门店促销", R.mipmap.ic_service_store_promotion));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_DEFECTIVE, "报损", R.mipmap.ic_service_defective));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_LAUNDRY, "洗衣", R.mipmap.ic_service_laundry));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PACKAGE, " 取包裹", R.mipmap.ic_service_package));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_COURIER, "快递代收", R.mipmap.ic_service_courier));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_EXPRESS, "寄快递", R.mipmap.ic_service_express));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_RECEIVE_GOODS, "商品领取", R.mipmap.ic_service_receivegoods));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_FEEDPAPER, "走纸", R.mipmap.ic_service_feedpaper));
////        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PAYBACK, "返货", R.mipmap.ic_service_payback));
//        localList.add(CashierFunctional.generate(CashierFunctional.OPTION_ID_PRIVATE, "我的", R.mipmap.ic_service_private));
//        if (functionalAdapter != null) {
//            functionalAdapter.setEntityList(localList);
//        }
//    }

    /**
     * 初始化前台类目
     */
    private void initShortcutRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getActivity(), SERVICE_SPAN_COUNT);
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

        menuAdapter = new CashierServiceMenuAdapter(CashierApp.getAppContext(), null);
        menuAdapter.setOnAdapterLitener(new CashierServiceMenuAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                CashierFunctional entity = menuAdapter.getEntity(position);
                if (entity == null) {
                    return;
                }
                if (entity.getType() == 0) {
                    responsePrivateFunction(entity.getId());
                } else {
                    Bundle args = new Bundle();
                    args.putString("title", entity.getNameCn());
                    args.putLong("categoryId", entity.getId());
                    EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SHOW_FRONT_CATEGORY, args));
                }

            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
//        refreshFrontCategory();
    }

    /**
     * 固有功能
     */
    private void responsePrivateFunction(Long id) {
        if (id == null) {
            return;
        }

        if (id.compareTo(CashierFunctional.OPTION_ID_PACKAGE) == 0) {
            packageService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_MEMBER_CARD) == 0) {
            memberCardService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_RECHARGE) == 0) {
            rechargeService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_COURIER) == 0) {
            courierService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_EXPRESS) == 0) {
            expressService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_LAUNDRY) == 0) {
            laundryService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_DEFECTIVE) == 0) {
            defectiveService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_RETURN_GOODS) == 0) {
            returnGoods();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_RECEIVE_GOODS) == 0) {
            receiveGoodsService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_FEEDPAPER) == 0) {
            //走纸
            SerialManager.feedPaper();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_PRIVATE) == 0) {
            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SHOW_COMMONLY));
        } else if (id.compareTo(CashierFunctional.OPTION_ID_MALL) == 0) {
            commodityCenterService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_PURCHASE_SEND) == 0) {
            orderGoodsService();
        } else if (id.compareTo(CashierFunctional.OPTION_ID_FRESH) == 0) {
            purchaseFreshGoods();
        }  else if (id.compareTo(CashierFunctional.OPTION_ID_PURCHASE_INTELLIGENT) == 0){
            Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
            Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_INTELLIGENT_ORDER);
//            extras.putString(DailySettleFragment.EXTRA_KEY_DATETIME, datetime);
//            extras.putBoolean(DailySettleFragment.EXTRA_KEY_CANCELABLE, cancelable);
            intent.putExtras(extras);
            startActivity(intent);
        } else if (id.compareTo(CashierFunctional.OPTION_ID_PURCHASE_RECEIPT) == 0) {
            Bundle extras = new Bundle();
            extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_PURCHASE_RECEIPT_ORDER);
            extras.putInt(CreatePurchaseReceiptOrderFragment.EK_ENTERMODE, 2);

//        ServiceActivity.actionStart(getActivity(), extras);

            Intent intent = new Intent(getActivity(), ServiceActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
        } else if (id.compareTo(CashierFunctional.OPTION_ID_PURCHASE_RETURN) == 0) {
            Bundle extras = new Bundle();
            extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_PURCHASE_RETURN_ORDER);
            extras.putInt(CreatePurchaseReturnOrderFragment.EK_ENTERMODE, 2);
//        ServiceActivity.actionStart(getActivity(), extras);

            Intent intent = new Intent(getActivity(), ServiceActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
        } else if (id.compareTo(CashierFunctional.OPTION_ID_INVENTORY_TRANS_IN) == 0) {
            Bundle extras = new Bundle();
            extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_INVENTORY_ALLOCATION_ORDER);
            extras.putInt(CreateInventoryTransOrderFragment.EK_ENTERMODE, 2);

            ServiceActivity.actionStart(getActivity(), extras);

//                    Intent intent = new Intent(getActivity(), ServiceActivity.class);
//                    intent.putExtras(extras);
//                    startActivity(intent);
        } else if (id.compareTo(CashierFunctional.OPTION_ID_INVENTORY_TRANS_OUT) == 0) {
            Bundle extras = new Bundle();
            extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_CREATE_INVENTORY_ALLOCATION_ORDER);
            extras.putInt(CreateInventoryTransOrderFragment.EK_ENTERMODE, 2);

            ServiceActivity.actionStart(getActivity(), extras);

//                    Intent intent = new Intent(getActivity(), ServiceActivity.class);
//                    intent.putExtras(extras);
//                    startActivity(intent);
        } else {
            DialogUtil.showHint("@开发君 失踪了...");
        }
    }

    /**
     * 开钱箱
     */
    @OnClick(R.id.button_money_box)
    public void openMoneyBox() {
        //打开钱箱
        SerialManager.openMoneyBox();
    }


    /**
     * 初始化条码输入
     */
    private void initBarCodeInput() {
        inlvBarcode.setEnterKeySubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
        inlvBarcode.requestFocus();
        inlvBarcode.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String barcode = inlvBarcode.getInputString();
                        if (!StringUtils.isEmpty(barcode)) {
                            addProduct(barcode);
                        }
                    }

                    return true;
                }
                //Press “*”
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //判断是否已经有数字，如果已经有则直接加数字，否则弹窗
                        String inputText = inlvBarcode.getInputString();
                        if (StringUtils.isEmpty(inputText)) {
                            if (productAdapter != null) {
                                productAdapter.changeQuantity(getActivity());
                            }
                        } else {
                            inlvBarcode.clear();
                            try {
                                if (productAdapter != null) {
                                    productAdapter.changeQuantity(Double.valueOf(inputText));
                                }
                            } catch (Exception e) {
                                ZLogger.e(e.toString());
                            }
                        }
                    }
                    return true;
                }
                //Press “＋”
                if (keyCode == KeyEvent.KEYCODE_NUMPAD_ADD) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (btnSettle.isEnabled()) {
                            settle();
                        }
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }


    /**
     * 清空当前订单
     */
    @OnClick(R.id.button_clear_order)
    public void clearOrder() {
        //称重
        inlvBarcode.clear();

        //检查订单是否被锁定，如果被锁定则不能清空商品
        if (checkIsOrderLocked()) {
            return;
        }

        updatePadDisplay(CashierOrderInfoWrapper.CMD_CLEAR_ORDER, null);
        productAdapter.setEntityList(null);
        //清除订单明细
        CashierHelper.clearOrderItems(curOrderBarCode);
    }

    /**
     * 同步数据
     */
    @OnClick(R.id.ib_sync_data)
    public void syncData() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        //设置需要更新前台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PUBLIC_FRONTCATEGORY_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_CUSTOM_FRONTCATEGORY_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PUBLIC_LAUNDRY_FRONTCATEGORY_ENABLED, true);
        //设置需要更新前台类目
        SharedPreferencesHelper.setSyncFrontCategorySubEnabled(true);
        //设置需要更新商品中心,商品后台类目
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_FRESH_ENABLED, true);

        //同步数据
        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SYNC_DATA_START));
    }

    /**
     * 刷新上一单信息
     */
    private void refreshLastOrder(Double amount, Double count, Double discount, Double charge) {
        tvLastAmount.setText(String.format("合计: ¥%.2f", amount));
        tvLastQuantity.setText(String.format("数量: %.2f", count));
        tvLastDiscount.setText(String.format("优惠: ¥%.2f", discount));
        tvLastCharge.setText(String.format("找零: ¥%.2f", charge));
    }

    /**
     * 刷新挂起浮动按钮
     */
    private void refreshFloatHangup() {
        List<PosOrderEntity> orderEntityList = PosOrderService.get()
                .queryAllBy(String.format("status = '%d' and sellerId = '%d'",
                        PosOrderEntity.ORDER_STATUS_HANGUP, MfhLoginService.get().getSpid()));
        if (orderEntityList != null && orderEntityList.size() > 0) {
            fabHangup.setText(String.valueOf(orderEntityList.size()));
            fabHangup.setVisibility(View.VISIBLE);
        } else {
            fabHangup.setText("0");
            fabHangup.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化订单流水
     */
    private void initCashierOrder() {
        try {
            tvServiceTitle.setText(MfhLoginService.get().getCurOfficeName());
            inlvBarcode.requestFocus();

            //刷新上一单数据
            refreshLastOrder(0D, 0D, 0D, 0D);

            //加载订单
            validateCurOrderBarcode(null);
            productAdapter.setEntityList(null);

            //刷新挂单
            refreshFloatHangup();
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 调单
     */
    private void resumeOrder(String orderBarcode) {
        ZLogger.d(String.format("调单：%s", orderBarcode));
        PosOrderEntity orderEntity = CashierHelper.findPosOrder(orderBarcode);
        if (orderEntity == null){
            DialogUtil.showHint("调单失败");
            return;
        }

        //修改订单状态：挂单改为待支付
        orderEntity.setStatus(PosOrderEntity.ORDER_STATUS_STAY_PAY);
        PosOrderService.get().saveOrUpdate(orderEntity);

        List<PosOrderItemEntity> itemEntityList = PosOrderItemService.get()
                .queryAllBy(String.format("orderBarCode = '%s'", orderBarcode));

        if (curBizType.equals(BizType.LAUNDRY)) {
//            clearOrder();
            backToPosCashier();
            //加载新订单
            validateCurOrderBarcode(orderBarcode);
            //加载明细
            productAdapter.setEntityList(itemEntityList);
        } else {
            //挂起当前订单
            CashierHelper.hangUpCashierOrder(curOrderBarCode, curBizType, null,
                    productAdapter.getEntityList());

            curBizType = BizType.POS;//
            packageCode = "";
            //加载新订单
            validateCurOrderBarcode(orderBarcode);
            //加载明细
            productAdapter.setEntityList(itemEntityList);
        }

        //刷新挂单
        refreshFloatHangup();
    }

    /**
     * 检查订单是否被锁，已付款的订单既不能添加商品也不能清空
     */
    private boolean checkIsOrderLocked() {
        // 检查参数
        if (StringUtils.isEmpty(curOrderBarCode)) {
            curOrderBarCode = MUtils.getOrderBarCode();
        }

        // 检查是否存在订单，如果订单不存在，则可以添加商品；
        // 如果订单已经存在，则判断订单是否已经支付，如果已经支付过全部或部分则不能添加商品，反之可以添加。
        PosOrderEntity orderEntity = CashierHelper.findPosOrder(curOrderBarCode);
        if (orderEntity != null && orderEntity.getPaystatus() == PosOrderEntity.PAY_STATUS_YES) {
            DialogUtil.showHint("请先支付订单");
            return true;
        }
        return false;
    }

    /**
     * 根据条码查询商品
     */
    private void addProduct(final String barCode) {
        // 清空二维码输入，避免下次扫描条码错误
        inlvBarcode.clear();

        if (!checkIsOrderLocked()) {
            cashierPresenter.findGoods(barCode);
        }
    }

    /**
     * 添加类目商品
     */
    private void addProduct(final ScGoodsSku goods) {
        if (goods == null) {
            return;
        }

        // 清空二维码输入，避免下次扫描条码错误
        inlvBarcode.clear();

        if (checkIsOrderLocked()) {
            return;
        }

        //添加商品
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            Double weightVal = DataCacheHelper.getInstance().getNetWeight();
            if (weightVal > 0) {
                productAdapter.append(PosOrderItemService.get().generate(curOrderBarCode,
                        goods, weightVal));
            } else {
                //手动输入重量
                if (changeQuantityDialog == null) {
                    changeQuantityDialog = new ChangeQuantityDialog(getActivity());
                    changeQuantityDialog.setCancelable(true);
                    changeQuantityDialog.setCanceledOnTouchOutside(true);
                }
                changeQuantityDialog.init("重量", 3, weightVal, new ChangeQuantityDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        productAdapter.append(PosOrderItemService.get()
                                .generate(curOrderBarCode, goods, quantity));
                    }
                });
                changeQuantityDialog.show();
            }
        } else {
            productAdapter.append(PosOrderItemService.get().generate(curOrderBarCode, goods, 1D));
        }

    }

    /**
     * 添加洗衣商品,已经有前置条件了，这里不需要称重判断
     */
    private void addProduct(final ChainGoodsSku goods) {
        if (goods == null) {
            return;
        }

        // 清空二维码输入，避免下次扫描条码错误
        inlvBarcode.clear();

        if (!checkIsOrderLocked()) {
            productAdapter.append(PosOrderItemService.get().fromLaundryGoods(curOrderBarCode, goods, 1D));
        }
    }


    /**
     * 添加常用商品
     */
    private void addProduct(final CommonlyGoodsEntity entity) {
        if (entity == null) {
            return;
        }

        // 清空二维码输入，避免下次扫描条码错误
        inlvBarcode.clear();

        if (checkIsOrderLocked()) {
            return;
        }

        //添加商品
        if (entity.getPriceType().equals(PriceType.WEIGHT)) {
            Double weightVal = DataCacheHelper.getInstance().getNetWeight();
            if (weightVal > 0) {
                productAdapter.append(PosOrderItemService.get().generate(curOrderBarCode, entity, weightVal));
            } else {
                if (changeQuantityDialog == null) {
                    changeQuantityDialog = new ChangeQuantityDialog(getActivity());
                    changeQuantityDialog.setCancelable(true);
                    changeQuantityDialog.setCanceledOnTouchOutside(true);
                }
                changeQuantityDialog.init("重量", 3, weightVal, new ChangeQuantityDialog.OnResponseCallback() {
                    @Override
                    public void onQuantityChanged(Double quantity) {
                        productAdapter.append(PosOrderItemService.get().generate(curOrderBarCode, entity, quantity));
                    }
                });
                changeQuantityDialog.show();
            }
        } else {
            productAdapter.append(PosOrderItemService.get().generate(curOrderBarCode, entity, 1D));
        }

//        addProductThreadExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                //显示 单价－－金额
////                SerialDisplayHelper.show(1, goods.getCostPrice());
//
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        Looper.prepare();
//                        //刷新订单列表
//
////                        Looper.loop();
//                    }
//                });
//            }
//        });
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//
//
//            }
//        }.start();
    }

    /**
     * */
    public void validateCurOrderBarcode(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            curOrderBarCode = MUtils.getOrderBarCode();
        }
        else{
            curOrderBarCode = barcode;
        }
    }


    /**
     * 刷新前台类目
     */
    private synchronized void refreshFrontCategory() {
//        List<PosCategory> localList = new ArrayList<>();
//
//        //公共前台类目
//        String publicCateCache = ACache.get(CashierApp.getAppContext(), Constants.CACHE_NAME)
//                .getAsString(Constants.CK_PUBLIC_FRONT_CATEGORY);
//        String customCateCache = ACache.get(CashierApp.getAppContext(), Constants.CACHE_NAME)
//                .getAsString(Constants.CK_CUSTOM_FRONT_CATEGORY);
//        //私有前台类目
//        List<PosCategory> publicData = JSONArray.parseArray(publicCateCache, PosCategory.class);
//        List<PosCategory> customData = JSONArray.parseArray(customCateCache, PosCategory.class);
//        if (menuAdapter != null) {
//            menuAdapter.setEntityList(localList, publicData, customData);
//        }

        if (menuAdapter != null) {
            menuAdapter.setEntityList(cashierPresenter.getCashierFunctions());
        }
    }

    /**
     * 取包裹
     */
    private void packageService() {
//        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint(R.string.toast_network_error);
//            return;
//        }

        //直接根据取货码查询
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_STOCK_DETAIL);
        ServiceActivity.actionStart(getActivity(), extras);
    }

    /**
     * 商城
     */
    private void commodityCenterService() {
        //直接根据取货码查询
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_COMMODITY_CENTER);
        ServiceActivity.actionStart(getActivity(), extras);
    }

    /**
     * 订货
     */
    private void orderGoodsService() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FRAGMENT_TYPE_COMMODITY_APPLY);
        UIHelper.startActivity(getActivity(), SimpleActivity.class, extras);
    }

    private void purchaseFreshGoods() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FT_PURCHASE_FRESH_GOODS);
        UIHelper.startActivity(getActivity(), SimpleActivity.class, extras);
    }

    /**
     * 会员卡开卡
     */
    private void memberCardService() {
//        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint(R.string.toast_network_error);
//            return;
//        }

        //开卡
        if (initCardDialog == null) {
            initCardDialog = new InitCardDialog(getActivity());
            initCardDialog.setCancelable(false);
            initCardDialog.setCanceledOnTouchOutside(false);
        }
        initCardDialog.initialize();
        if (!initCardDialog.isShowing()) {
            initCardDialog.show();
        }
    }

    /**
     * 充值
     */
    private void rechargeService() {
        //充值
        if (topupDialog == null) {
            topupDialog = new TopupDialog(getActivity());
            topupDialog.setCancelable(false);
            topupDialog.setCanceledOnTouchOutside(false);
        }
        topupDialog.init();
        if (!topupDialog.isShowing()) {
            topupDialog.show();
        }
    }

    /**
     * 快递代收
     */
    private void courierService() {
//        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint(R.string.toast_network_error);
//            return;
//        }

        if (expressDeliverDialog == null) {
            expressDeliverDialog = new QueryDialog(getActivity());
            expressDeliverDialog.setCancelable(false);
            expressDeliverDialog.setCanceledOnTouchOutside(false);
        }
        expressDeliverDialog.init(QueryDialog.DT_EXPRESS_COLLECTION, new QueryDialog.DialogListener() {
            @Override
            public void query(String text) {

            }

            @Override
            public void onNextStep(String fee) {
            }

            @Override
            public void onNextStep(Human human) {
                Bundle extras = new Bundle();
                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_STOCK_IN);
                extras.putSerializable(ServiceActivity.EXTRA_KEY_COURIER, human);
                ServiceActivity.actionStart(getActivity(), extras);
            }

            @Override
            public void onNextStep() {
            }
        });
        if (!expressDeliverDialog.isShowing()) {
            expressDeliverDialog.show();
        }
    }

    /**
     * 寄快递
     */
    private void expressService() {
//        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint(getString(R.string.toast_network_error));
//            return;
//        }

        if (expressDialog == null) {
            expressDialog = new ExpressDialog(getActivity());
            expressDialog.setCancelable(false);
            expressDialog.setCanceledOnTouchOutside(false);
        }
        expressDialog.init(new ExpressDialog.DialogListener() {
            @Override
            public void query(String text) {

            }

            @Override
            public void onNextStep() {
                //TODO,显示快递页面
                EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SHOW_EXPRESS));
            }
        });
        if (!expressDialog.isShowing()) {
            expressDialog.show();
        }
    }

    /**
     * 洗衣
     */
    private void laundryService() {
        if (laundryDialog == null) {
            laundryDialog = new LaundryDialog(getActivity());
            laundryDialog.setCancelable(false);
            laundryDialog.setCanceledOnTouchOutside(false);
        }
        laundryDialog.init(new LaundryDialog.DialogListener() {

            @Override
            public void onNextStep(String text) {
                //挂起POS收银
                hangUpOrder();

                //保存衣袋编号，同时标记开始进行洗衣服务，挂起当前订单
                packageCode = text;
                curBizType = BizType.LAUNDRY;

                //显示洗衣商品
                EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SHOW_LAUNDRY));
            }
        });
        if (!laundryDialog.isShowing()) {
            laundryDialog.show();
        }
    }

    /**
     * 报损服务
     * TOTO,基本
     */
    private void defectiveService() {
//        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint(getString(R.string.toast_network_error));
//            return;
//        }

        if (defectiveDialog == null) {
            defectiveDialog = new DefectiveDialog(getActivity());
            defectiveDialog.setCancelable(false);
            defectiveDialog.setCanceledOnTouchOutside(false);
        }
        defectiveDialog.init();
        if (!defectiveDialog.isShowing()) {
            defectiveDialog.show();
        }
    }

    /**
     * 退货
     */
    private void returnGoods() {
        if (returnGoodsDialog == null) {
            returnGoodsDialog = new ReturnGoodsDialog(getActivity());
            returnGoodsDialog.setCancelable(false);
            returnGoodsDialog.setCanceledOnTouchOutside(false);
        }
        if (!returnGoodsDialog.isShowing()) {
            returnGoodsDialog.show();
        }
    }

    /**
     * 领取商品
     */
    private void receiveGoodsService() {
//        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
//            DialogUtil.showHint(R.string.toast_network_error);
//            return;
//        }

        if (receiveGoodsDialog == null) {
            receiveGoodsDialog = new ReceiveGoodsDialog(getActivity());
            receiveGoodsDialog.setCancelable(false);
            receiveGoodsDialog.setCanceledOnTouchOutside(false);
        }
        receiveGoodsDialog.init(new ReceiveGoodsDialog.DialogListener() {
            @Override
            public void onOrderConfirmed(String orderBarcode) {
                resumeOrder(orderBarcode);
            }
        });
        if (!receiveGoodsDialog.isShowing()) {
            receiveGoodsDialog.show();
        }
    }


    @Override
    public void onFindGoods(PosProductEntity goods, int packFlag) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getStatus() != 1) {
            DialogUtil.showHint(String.format("商品已经下架:%s", goods.getBarcode()));
            return;
        }

        //添加商品
        final PosOrderItemEntity entity;
        if (goods.getPriceType().equals(PriceType.WEIGHT)) {
            entity = PosOrderItemService.get().generate(curOrderBarCode, goods,
                    DataCacheHelper.getInstance().getNetWeight());
//                        Double weightVal = DataCacheHelper.getInstance().getNetWeight();
//                        if (weightVal > 0){
//
//                        }
//                        else{
//                            if (changeQuantityDialog == null) {
//                                changeQuantityDialog = new ChangeQuantityDialog(getActivity());
//                                changeQuantityDialog.setCancelable(true);
//                                changeQuantityDialog.setCanceledOnTouchOutside(true);
//                            }
//                            changeQuantityDialog.init("重量", 3, weightVal, new ChangeQuantityDialog.OnResponseCallback() {
//                                @Override
//                                public void onQuantityChanged(Double quantity) {
//                                    productAdapter.append(ShopcartService.get().generate(curOrderBarCode, goods, quantity));
//
//                                    entity = ShopcartService.get().generate(curOrderBarCode, productEntity, weightVal);
//                                }
//                            });
//                            changeQuantityDialog.show();
//                        }

        } else {
            if (packFlag == 1) {
                entity = PosOrderItemService.get().generate(curOrderBarCode, goods, goods.getPackageNum());
            } else {
                entity = PosOrderItemService.get().generate(curOrderBarCode, goods, 1D);
            }
        }

        //刷新订单列表
        productAdapter.append(entity);
    }

    @Override
    public void onFindGoodsEmpty(String barcode) {
        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_CREATE_PURCHASE_GOODS);
        extras.putString(StockScSkuGoodsFragment.EXTRY_KEY_BARCODE, barcode);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_CREATE_PURCHASE_GOODS);
    }
}