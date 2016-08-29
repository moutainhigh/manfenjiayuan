package com.mfh.litecashier.ui.fragment.purchase;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.bean.InvSendOrder;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.bean.OrderStatus;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.manfenjiayuan.business.bean.wrapper.CreateOrderItemWrapper;
import com.manfenjiayuan.business.dialog.AccountQuickPayDialog;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.OptionalLabel;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfo;
import com.mfh.litecashier.event.PurchaseReceiptCreateEvent;
import com.mfh.litecashier.event.PurchaseReceiptEvent;
import com.mfh.litecashier.event.PurchaseSendEvent;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.CreateOrderItemAdapter;
import com.mfh.litecashier.ui.dialog.SelectInvSendOrderDialog;
import com.mfh.litecashier.ui.dialog.SelectWholesalerDialog;
import com.mfh.litecashier.ui.fragment.inventory.GreateScSkuGoodsFragment;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 新建采购收货单<br>
 *
 * {@link #enterMode 入口方式}
 * <li>
 *     0. 新建采购收货单。(default)
 *     1. 采购单收货。
 *     2. 快捷入口。
 * </li>
 * Created by Nat.ZZN(bingshanguxue) on 15/09/24.
 */
public class CreatePurchaseReceiptOrderFragment extends BaseFragment
implements IChainGoodsSkuView, IInvSendOrderView {
    public static final String EK_ENTERMODE = "enterMode";
    public static final String EK_SENDORDER = "sendOrder";

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @Bind(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;
    @Bind(R.id.goods_list)
    RecyclerView productRecyclerView;
    @Bind(R.id.tv_goods_quantity)
    TextView tvGoodsQuantity;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @Bind(R.id.button_submit)
    Button btnSubmit;
    /*备注*/
    @Bind(R.id.et_remark)
    EditText etRemark;

    private ItemTouchHelper itemTouchHelper;
    private CreateOrderItemAdapter productAdapter;

    private Double totalAmount = 0D;

    private SelectInvSendOrderDialog selectSendOrderDialog;

    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;
    private InvSendOrderPresenter invSendOrderPresenter;

    private int enterMode = 0;
    /*采购单*/
    private InvSendOrder curInvSendOrder = null;
    /*供应商*/
    private CompanyInfo companyInfo = null;//当前私有供应商

    public static CreatePurchaseReceiptOrderFragment newInstance(Bundle args) {
        CreatePurchaseReceiptOrderFragment fragment = new CreatePurchaseReceiptOrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_receipt_create;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        chainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
        invSendOrderPresenter = new InvSendOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        tvHeaderTitle.setText("新建收货单");
        initProgressDialog("正在发送请求...", "创建成功", "创建失败");

        initBarCodeInput();
        initGoodsRecyclerView();
        labelInvcompProvider.setHintText("发货方");
        labelInvcompProvider.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                changeSendCompany(null);
                if (curInvSendOrder != null) {
                    curInvSendOrder = null;
                    labelSendOrder.setLabelText("");
                }
                inlvBarcode.setVisibility(View.VISIBLE);
                inlvBarcode.requestFocus();
                productAdapter.setEntityList(null);//清空商品

            }
        });
        labelSendOrder.setHintText("采购订单");
        labelSendOrder.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                changeInvSendOrder(null);
            }
        });


        //设置需要重新加载私有供应商数据，加载成功后设置为false
//        SharedPreferencesHelper.setSyncEnabled(SharedPreferencesHelper.PREF_KEY_RELOAD_OPTIONAL_PROVIDER_ENABLED, true);

        initData();
    }

    @Override
    public void onResume() {
        super.onResume();

        inlvBarcode.requestFocus();
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
    public void onEventMainThread(PurchaseReceiptCreateEvent event) {
        ZLogger.d(String.format("CreatePurchaseReceiptOrderFragment: PurchaseReceiptCreateEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == PurchaseReceiptCreateEvent.EVENT_ID_RELOAD_INV_SENDORDER) {
            Bundle args = event.getArgs();
            if (args != null) {
                changeInvSendOrder((InvSendOrder) args.getSerializable("data"));
            }
        }
    }

    /**
     * 返回首页，保存数据
     */
    @OnClick(R.id.button_header_close)
    public void finishAndSaveData() {
        saveCacheData(curInvSendOrder, companyInfo, productAdapter.getEntityList());
        //TODO 通知刷新
        getActivity().finish();
    }

    /**
     * 保存缓存数据
     * */
    private void saveCacheData(final InvSendOrder invSendOrder, final CompanyInfo companyInfo,
                               final List<CreateOrderItemWrapper> goodsList){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                //保存批发商
                if (invSendOrder != null) {
                    JSONArray cacheArrays = new JSONArray();
                    cacheArrays.add(invSendOrder);
                    ACacheHelper.put(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_ORDER_DATA, cacheArrays.toJSONString());
                }
                if (companyInfo != null) {
                    JSONArray cacheArrays = new JSONArray();
                    cacheArrays.add(companyInfo);
                    ACacheHelper.put(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_SUPPLY_DATA, cacheArrays.toJSONString());
                }

                if (goodsList != null && goodsList.size() > 0) {
                    //保存缓存数据
                    JSONArray cacheArrays = new JSONArray();
                    cacheArrays.addAll(goodsList);
                    ACacheHelper.put(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_GOODS_DATA, cacheArrays.toJSONString());
                }
//            }
//        });
    }

    /**
     * 选择采购订单
     */
    @Bind(R.id.label_sendorder)
    OptionalLabel labelSendOrder;

    @OnClick(R.id.label_sendorder)
    public void selectSendOrder() {
        String status = String.format("%d,%d", OrderStatus.STATUS_CONFIRM, OrderStatus.STATUS_SENDED);
        String cacheKey = String.format("%s_%s", ACacheHelper.CK_PURCHASE_ORDER, status);
//        if (selectSendOrderDialog == null) {
//            selectSendOrderDialog = new SelectInvSendOrderDialog(getActivity());
//            selectSendOrderDialog.setCancelable(false);
//            selectSendOrderDialog.setCanceledOnTouchOutside(false);
//        }
//        selectSendOrderDialog.init(status, cacheKey, new SelectInvSendOrderDialog.OnDialogListener() {
//            @Override
//            public void onItemSelected(InvSendOrder invSendOrder) {
//                //TODO,加载订单明细
//                changeInvSendOrder(invSendOrder);
//            }
//        });
//        if (!selectSendOrderDialog.isShowing()) {
//            selectSendOrderDialog.show();
//        }

        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_SELECT_INV_SENDORDER);
        extras.putString(SelectInvSendOrderFragment.EXTRA_KEY_STATUS, status);
        extras.putString(SelectInvSendOrderFragment.EXTRA_KEY_CACHEKEY, cacheKey);
        if (companyInfo != null){
            String sendTenantId = companyInfo.getTenantId() != null ? String.valueOf(companyInfo.getTenantId()) : null;
            extras.putString(SelectInvSendOrderFragment.EK_SENDTENANTID, sendTenantId);
        }

        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 选择批发商
     */
    @Bind(R.id.label_invcomp_provider)
    OptionalLabel labelInvcompProvider;

    private SelectWholesalerDialog selectPlatformProviderDialog = null;

    /**
     * 选择批发商
     */
    @OnClick(R.id.label_invcomp_provider)
    public void selectInvCompProvider() {
//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_SELECT_WHOLESALER_TENANT);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_NORMAL);
//        extras.putString(SimpleDialogActivity.EXTRA_KEY_TITLE, "选择发货方");
////        SimpleDialogActivity.actionStart(getActivity(), extras);
//
//        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
//        intent.putExtras(extras);
//        startActivityForResult(intent, Constants.ARC_SELECT_WHOLESALER_TENANT);
        if (selectPlatformProviderDialog == null) {
            selectPlatformProviderDialog = new SelectWholesalerDialog(getActivity());
            selectPlatformProviderDialog.setCancelable(false);
            selectPlatformProviderDialog.setCanceledOnTouchOutside(false);
        }
        selectPlatformProviderDialog.init(new SelectWholesalerDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                curInvSendOrder = null;
                inlvBarcode.setVisibility(View.VISIBLE);
                inlvBarcode.requestFocus();

                labelSendOrder.setLabelText("");
                productAdapter.setEntityList(null);//清空商品
                changeSendCompany(companyInfo);
            }

        });
        if (!selectPlatformProviderDialog.isShowing()) {
            selectPlatformProviderDialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_SELECT_WHOLESALER_TENANT: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        changeSendCompany((CompanyInfo) data.getSerializableExtra("data"));
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
//                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        queryByBarcode();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    private void initGoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        productRecyclerView.setHasFixedSize(true);
        //添加分割线
        productRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        productAdapter = new CreateOrderItemAdapter(getActivity(), null, true, true);
        productAdapter.setOnAdapterListener(new CreateOrderItemAdapter.OnAdapterListener() {

            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onDataSetChanged() {
                refreshBottomBar();
            }
        });
        productRecyclerView.setAdapter(productAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(productAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(productRecyclerView);
    }

    /**
     * 根据条码查询商品<br>
     * <b>规则：</b>首先查城市之间的商品库，如果有则自动添加商品；
     * 如果没有，则继续查询平台的商品档案，如果查到商品则自动添加商品，否则弹窗自建商品档案。
     */
    private void queryByBarcode() {
        final String barcode = inlvBarcode.getInputString();
        inlvBarcode.clear();

        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        if (companyInfo == null){
            Snackbar.make(inlvBarcode, "请先选择发货方!", Snackbar.LENGTH_SHORT)
                    .setAction("选择批发商", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectInvCompProvider();
                        }
                    })
                    .setActionTextColor(Color.WHITE)
                    .show();
//            DialogUtil.showHint("请先选择发货方");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        chainGoodsSkuPresenter.findTenantSku(new PageInfo(-1, 10),
                companyInfo.getId(), barcode);
    }

    private void generateGoods(ScGoodsSku goodsSku) {
        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_GENERATE_PURCHASE_GOODS);
        extras.putSerializable(GreateScSkuGoodsFragment.EXTRA_KEY_PURCHASE_GOODS, goodsSku);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 创建收货单
     */
    @OnClick(R.id.button_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        showConfirmDialog("确定要提交收货单吗？",
                "收货", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        doSubmitStuff();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        btnSubmit.setEnabled(true);
                    }
                });
    }

    private void doSubmitStuff(){
        showProgressDialog(ProgressDialog.STATUS_PROCESSING);

        if (companyInfo == null){
            DialogUtil.showHint("请选择发货方！");
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        List<CreateOrderItemWrapper> goodsList = productAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            btnSubmit.setEnabled(true);
            DialogUtil.showHint("商品不能为空");
            hideProgressDialog();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        JSONObject jsonStrObject = new JSONObject();
        jsonStrObject.put("sendNetId", companyInfo.getId());
        jsonStrObject.put("sendTenantId", companyInfo.getTenantId());
        jsonStrObject.put("isPrivate", IsPrivate.PLATFORM);
        jsonStrObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("tenantId", MfhLoginService.get().getSpid());//收货方
        jsonStrObject.put("remark", etRemark.getText().toString());

        JSONArray itemsArray = new JSONArray();
        for (CreateOrderItemWrapper goods : goodsList) {
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());//查询批发商
            item.put("providerId", goods.getProviderId());
            item.put("isPrivate", goods.getIsPrivate());//（0：不是 1：是）
            item.put("proSkuId", goods.getProSkuId());
            item.put("productName", goods.getProductName());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("price", goods.getPrice());
            item.put("amount", goods.getAmount());
            item.put("barcode", goods.getBarcode());

            itemsArray.add(item);
        }
        jsonStrObject.put("items", itemsArray);

        InvOrderApiImpl.createInvSendIoRecOrder(curInvSendOrder != null ? curInvSendOrder.getId() : null, true,
                jsonStrObject.toJSONString(), responseCallback);
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
//                    DialogUtil.showHint("新建收货单失败" + errMsg);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    btnSubmit.setEnabled(true);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            hideProgressDialog();
//                        }
//                    }, 1000);
                }

                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"新增成功!","version":"1","data":{"val":"463"}}
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
//                        animProgress.setVisibility(View.GONE);

                    RspValue<String> retValue = (RspValue<String>) rspData;
                    String orderId = retValue.getValue();
                    /**
                     * 新增采购单成功，更新采购单列表
                     * */
                    ZLogger.d(String.format("新建收货单成功: %s", orderId));

//                    btnSubmit.setEnabled(true);
                    hideProgressDialog();

                    //新建完收货单要立刻支付
                    doPayWork(orderId, totalAmount);
//                    productAdapter.setEntityList(null);

                    DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    private AccountQuickPayDialog payDialog;
    private void doPayWork(String orderId, Double amount) {
        if (StringUtils.isEmpty(orderId)) {
            ZLogger.d("订单无效");
            //支付成功
            getActivity().finish();

            //刷新数据
            if (enterMode == 1) {
                EventBus.getDefault().post(new PurchaseSendEvent(PurchaseSendEvent.EVENT_ID_RELOAD_DATA));
            } else {
                EventBus.getDefault().post(new PurchaseReceiptEvent(PurchaseReceiptEvent.EVENT_ID_RELOAD_DATA));
            }
            return;
        }

        Human human = new Human();
        human.setGuid(String.valueOf(MfhLoginService.get().getCurrentGuId()));
        human.setHeadimageUrl(MfhLoginService.get().getHeadimage());

        //当前收银信息
        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.init(null);
        cashierOrderInfo.setRetailAmount(amount);
        cashierOrderInfo.setDealAmount(amount);
        cashierOrderInfo.setDiscountAmount(0D);
        cashierOrderInfo.setDiscountRate(1D);
        cashierOrderInfo.initSetle(BizType.STOCK, "", orderId, "支付采购收货单", "", human);

        //支付
        if (payDialog == null) {
            payDialog = new AccountQuickPayDialog(getActivity());
            payDialog.setCancelable(false);
            payDialog.setCanceledOnTouchOutside(false);
        }
        payDialog.init(cashierOrderInfo.getOrderId(),
                cashierOrderInfo.getHandleAmount(), new AccountQuickPayDialog.DialogClickListener() {
            @Override
            public void onPaySucceed() {
                //支付成功
                getActivity().finish();

                //刷新数据
                if (enterMode == 1) {
                    EventBus.getDefault().post(new PurchaseSendEvent(PurchaseSendEvent.EVENT_ID_RELOAD_DATA));
                } else {
                    EventBus.getDefault().post(new PurchaseReceiptEvent(PurchaseReceiptEvent.EVENT_ID_RELOAD_DATA));
                }
            }

            @Override
            public void onPayFailed() {

            }

            @Override
            public void onPayCanceled() {
                //支付成功
                getActivity().finish();

                //刷新数据
                if (enterMode == 1) {
                    EventBus.getDefault().post(new PurchaseSendEvent(PurchaseSendEvent.EVENT_ID_RELOAD_DATA));
                } else {
                    EventBus.getDefault().post(new PurchaseReceiptEvent(PurchaseReceiptEvent.EVENT_ID_RELOAD_DATA));
                }
            }
        });
        payDialog.show();
    }

    private void refreshBottomBar() {
        try{
            if (productAdapter != null && productAdapter.getItemCount() > 0) {
                btnSubmit.setVisibility(View.VISIBLE);
                Double quantityCheck = 0D;
                Double amount = 0D;
                List<CreateOrderItemWrapper> entityList = productAdapter.getEntityList();
                for (CreateOrderItemWrapper entity : entityList) {
                    quantityCheck += entity.getQuantityCheck();
                    amount += entity.getAmount();
                }
                totalAmount = amount;
                tvGoodsQuantity.setText(String.format("商品数：%.2f", quantityCheck));
                tvTotalAmount.setText(String.format("商品金额：%.2f", totalAmount));
            } else {
                btnSubmit.setVisibility(View.INVISIBLE);
                totalAmount = 0D;
                tvGoodsQuantity.setText(String.format("商品数：%d", 0));
                tvTotalAmount.setText(String.format("商品金额：%.2f", totalAmount));
            }
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }

    }

    /**
     * 清空缓存数据
     */
    private void clearCacheData() {
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_ORDER_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_SUPPLY_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_GOODS_DATA);
    }

    /**
     * 初始化数据
     * */
    private void initData() {
//        enterMode = 1;
//        curInvSendOrder = null;
//        curInvCompProvider = null;

        Bundle args = getArguments();
        if (args != null) {
            enterMode = args.getInt(EK_ENTERMODE);
            if (enterMode == 1){
                curInvSendOrder = (InvSendOrder) args.getSerializable(EK_SENDORDER);

                if (curInvSendOrder != null){
                    companyInfo = new CompanyInfo();
                    companyInfo.setId(curInvSendOrder.getSendNetId());
                    companyInfo.setName(curInvSendOrder.getSendCompanyName());
                    companyInfo.setTenantId(curInvSendOrder.getSendTenantId());

                    invSendOrderPresenter.loadOrderItems(curInvSendOrder.getId());
                }
            }
            else if (enterMode == 2){
                //读取缓存
                String orderCacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_ORDER_DATA);
                List<InvSendOrder> orderCacheData = JSONArray.parseArray(orderCacheStr, InvSendOrder.class);
                if (orderCacheData != null && orderCacheData.size() > 0) {
                    curInvSendOrder = orderCacheData.get(0);
                } else {
                    curInvSendOrder = null;
                }

                String supplyCacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_SUPPLY_DATA);
                List<CompanyInfo> supplyCacheData = JSONArray.parseArray(supplyCacheStr,
                        CompanyInfo.class);
                if (supplyCacheData != null && supplyCacheData.size() > 0) {
                    companyInfo = supplyCacheData.get(0);
                } else {
                    companyInfo = null;
                }

                String goodsCacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_GOODS_DATA);
                List<CreateOrderItemWrapper> goodsCacheData = JSONArray.parseArray(goodsCacheStr,
                        CreateOrderItemWrapper.class);
                productAdapter.setEntityList(goodsCacheData);
            }
        }

        clearCacheData();

        if (curInvSendOrder == null){
            inlvBarcode.setVisibility(View.VISIBLE);
            inlvBarcode.requestFocus();
            labelSendOrder.setLabelText("");
//            productAdapter.setEntityList(null);
        }
        else{
            inlvBarcode.setVisibility(View.GONE);
            labelSendOrder.setLabelText(curInvSendOrder.getName());
        }

        if (companyInfo == null){
            labelInvcompProvider.setLabelText("");
            selectInvCompProvider();
        }
        else{
            labelInvcompProvider.setLabelText(companyInfo.getName());
        }
    }


    /**
     * 切换发货方
     * */
    private void changeSendCompany(CompanyInfo companyInfo){
        this.companyInfo = companyInfo;
        this.labelInvcompProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");
    }

    /**
     * 切换采购订单明细
     */
    private void changeInvSendOrder(InvSendOrder order) {
        curInvSendOrder = order;

        if (order == null) {
            inlvBarcode.setVisibility(View.VISIBLE);
            inlvBarcode.requestFocus();

            labelSendOrder.setLabelText("");
            productAdapter.setEntityList(null);//清空商品

            changeSendCompany(null);
        }
        else{
            inlvBarcode.setVisibility(View.GONE);
            labelSendOrder.setLabelText(curInvSendOrder.getName());

            CompanyInfo companyInfo = new CompanyInfo();
            companyInfo.setId(curInvSendOrder.getSendNetId());
            companyInfo.setName(curInvSendOrder.getSendCompanyName());
            companyInfo.setTenantId(curInvSendOrder.getSendTenantId());
            changeSendCompany(companyInfo);

            productAdapter.setEntityList(null);//清空商品
            invSendOrderPresenter.loadOrderItems(curInvSendOrder.getId());
        }
    }


    @Override
    public void onProcess() {

    }

    @Override
    public void onError(String errorMsg) {
        ZLogger.d(errorMsg);
//        productAdapter.setEntityList(null);
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        if (dataList != null && dataList.size() > 0){
            ChainGoodsSku chainGoodsSku = dataList.get(0);
            if (chainGoodsSku != null && chainGoodsSku.getSingleCostPrice() != null){
                productAdapter.appendSupplyGoods(chainGoodsSku);
            }
            else{
                //“如果singleCostPrice值为null，说明缺少箱规数，信息不完整，这种情况你不允许进行采购或收货
                DialogUtil.showHint("商品信息不完整");
            }
        }
        else{
            DialogUtil.showHint("未找到商品");
        }
    }

    @Override
    public void onQueryInvSendOrderProcess() {

    }

    @Override
    public void onQueryInvSendOrderError(String errorMsg) {

    }

    @Override
    public void onQueryInvSendOrderSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {

    }

    @Override
    public void onQueryInvSendOrderItemsSuccess(List<InvSendOrderItem> dataList) {
        if (productAdapter != null) {
            productAdapter.setSendOrderItems(dataList);
        }
    }
}
