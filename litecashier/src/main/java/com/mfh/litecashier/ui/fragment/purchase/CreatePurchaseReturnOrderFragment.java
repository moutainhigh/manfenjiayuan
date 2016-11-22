package com.mfh.litecashier.ui.fragment.purchase;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.bean.InvSendIoOrder;
import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.manfenjiayuan.business.bean.OrderStatus;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.invOrder.InvOrderApiImpl;
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
import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.bean.wrapper.CreateOrderItemWrapper;
import com.mfh.litecashier.event.PurchaseReceiptEvent;
import com.mfh.litecashier.event.PurchaseReturnCreateEvent;
import com.mfh.litecashier.event.PurchaseReturnEvent;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.CreateOrderItemAdapter;
import com.mfh.litecashier.ui.dialog.SelectInvRecvOrderDialog;
import com.mfh.litecashier.ui.dialog.SelectWholesalerDialog;
import com.mfh.litecashier.ui.fragment.inventory.GreateScSkuGoodsFragment;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 采购退货--新建采购退货单
 * Created by Nat.ZZN(bingshanguxue) on 15/09/24.
 */
public class CreatePurchaseReturnOrderFragment extends BaseFragment
implements IChainGoodsSkuView{

    public static final String EK_ENTERMODE = "enterMode";
    public static final String EK_RECVORDER = "recvOrder";

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


    private SelectInvRecvOrderDialog selectInvRecvOrderDialog = null;

    private ItemTouchHelper itemTouchHelper;
    private CreateOrderItemAdapter productAdapter;

    private int enterMode = 0;
    /*收货方:供应商*/
    private CompanyInfo companyInfo = null;//当前私有供应商
    /*收货单*/
    private InvSendIoOrder mCurInvSendIoOrder;

    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;


    public static CreatePurchaseReturnOrderFragment newInstance(Bundle args) {
        CreatePurchaseReturnOrderFragment fragment = new CreatePurchaseReturnOrderFragment();

        fragment.setArguments(args);
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
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        tvHeaderTitle.setText("新建退货单");
        initProgressDialog("正在发送请求", "创建成功", "创建失败");

        initBarCodeInput();
        initGoodsRecyclerView();

        labelRecvOrder.setHintText("收货订单");
        labelRecvOrder.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                changeInvRecvOrder(null);
            }
        });
        labelInvcompProvider.setHintText("收货方");
        labelInvcompProvider.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                changeRecvCompany(null);

                if (mCurInvSendIoOrder != null) {
                    mCurInvSendIoOrder = null;
                    labelRecvOrder.setLabelText("");
                }
                inlvBarcode.setVisibility(View.VISIBLE);
                inlvBarcode.requestFocus();

                productAdapter.setEntityList(null);//清空商品
            }
        });

        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void onEventMainThread(PurchaseReturnCreateEvent event) {
        ZLogger.d(String.format("CreatePurchaseReturnOrderFragment: PurchaseReturnCreateEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == PurchaseReturnCreateEvent.EVENT_ID_RELOAD_INV_RECVORDER) {
            Bundle args = event.getArgs();
            if (args != null) {
                changeInvRecvOrder((InvSendIoOrder) args.getSerializable("data"));
            }
        }
    }

    @OnClick(R.id.button_header_close)
    public void finishAndSaveData() {
        saveCacheData(mCurInvSendIoOrder, companyInfo, productAdapter.getEntityList());
        //TODO 通知刷新
        getActivity().finish();
    }

    /**
     * 保存缓存数据
     * */
    private void saveCacheData(final InvSendIoOrder invSendIoOrder, final CompanyInfo companyInfo,
                               final List<CreateOrderItemWrapper> goodsList){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                if (invSendIoOrder != null) {
                    //保存缓存数据
                    JSONArray cacheArrays = new JSONArray();
                    cacheArrays.add(invSendIoOrder);
                    ACacheHelper.put(ACacheHelper.TCK_PURCHASE_CREATERETURN_ORDER_DATA, cacheArrays.toJSONString());
                }
                if (companyInfo != null) {
                    JSONArray cacheArrays = new JSONArray();
                    cacheArrays.add(companyInfo);
                    ACacheHelper.put(ACacheHelper.TCK_PURCHASE_CREATERETURN_SUPPLY_DATA, cacheArrays.toJSONString());
                }
                if (goodsList != null && goodsList.size() > 0) {
                    //保存缓存数据
                    JSONArray cacheArrays = new JSONArray();
                    cacheArrays.addAll(goodsList);
                    ACacheHelper.put(ACacheHelper.TCK_PURCHASE_CREATERETURN_GOODS_DATA, cacheArrays.toJSONString());
                }
//            }
//        });
    }


    /**
     * 选择采购订单
     */
    @Bind(R.id.label_sendorder)
    OptionalLabel labelRecvOrder;

    @OnClick(R.id.label_sendorder)
    public void selectSendIoOrder() {
        String status = String.valueOf(OrderStatus.STATUS_RECEIVE);
        String cacheKey = String.format("%s_%d", ACacheHelper.CK_PURCHASE_RETURN, OrderStatus.STATUS_RECEIVE);

//        if (selectInvRecvOrderDialog == null) {
//            selectInvRecvOrderDialog = new SelectInvRecvOrderDialog(getActivity());
//            selectInvRecvOrderDialog.setCancelable(false);
//            selectInvRecvOrderDialog.setCanceledOnTouchOutside(false);
//        }
//        selectInvRecvOrderDialog.init(status, cacheKey, new SelectInvRecvOrderDialog.OnDialogListener() {
//            @Override
//            public void onItemSelected(InvSendIoOrder invSendOrder) {
//                changeInvRecvOrder(invSendOrder);
//            }
//        });
//        if (!selectInvRecvOrderDialog.isShowing()) {
//            selectInvRecvOrderDialog.show();
//        }

        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_SELECT_INV_RECVORDER);
        extras.putString(SelectInvRecvOrderFragment.EXTRA_KEY_STATUS, status);
        extras.putString(SelectInvRecvOrderFragment.EXTRA_KEY_CACHEKEY, cacheKey);
        if (companyInfo != null){
            String sendTenantId = companyInfo.getTenantId() != null ? String.valueOf(companyInfo.getTenantId()) : null;
            extras.putString(SelectInvRecvOrderFragment.EK_SENDTENANTID, sendTenantId);
        }
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * 选择供应商
     */
    @Bind(R.id.label_invcomp_provider)
    OptionalLabel labelInvcompProvider;

    private SelectWholesalerDialog selectPlatformProviderDialog;


    @OnClick(R.id.label_invcomp_provider)
    public void selectInvCompProvider() {
//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_SELECT_WHOLESALER_TENANT);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_NORMAL);
//        extras.putString(SimpleDialogActivity.EXTRA_KEY_TITLE, "选择收货方");
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
                changeRecvCompany(companyInfo);
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
                        companyInfo = (CompanyInfo) data.getSerializableExtra("data");
                        labelInvcompProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");
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
            DialogUtil.showHint("请先选择发货方");
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
     * 清空缓存数据
     */
    private void clearCacheData() {
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERETURN_ORDER_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERETURN_SUPPLY_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERETURN_GOODS_DATA);
    }

    /**
     * 初始化数据
     * */
    private void initData() {
//        enterMode = 1;
//        curInvSendOrder = null;
//        companyInfo = null;

        Bundle args = getArguments();
        if (args != null) {
            enterMode = args.getInt(EK_ENTERMODE);
            if (enterMode == 1){
                mCurInvSendIoOrder = (InvSendIoOrder) args.getSerializable(EK_RECVORDER);

                if (mCurInvSendIoOrder != null){
                    companyInfo = new CompanyInfo();
                    companyInfo.setId(mCurInvSendIoOrder.getSendNetId());
                    companyInfo.setName(mCurInvSendIoOrder.getSendCompanyName());
                    companyInfo.setTenantId(mCurInvSendIoOrder.getSendTenantId());

                    InvOrderApiImpl.getInvSendIoOrderById(mCurInvSendIoOrder.getId(), orderdetailRespCallback);
                }
            }
            else if (enterMode == 2){
                String orderCacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_PURCHASE_CREATERETURN_ORDER_DATA);
                List<InvSendIoOrder> orderCacheData = JSONArray.parseArray(orderCacheStr, InvSendIoOrder.class);
                if (orderCacheData != null && orderCacheData.size() > 0) {
                    mCurInvSendIoOrder = orderCacheData.get(0);
                } else {
                    mCurInvSendIoOrder = null;
                }

                String supplyCacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_PURCHASE_CREATERETURN_SUPPLY_DATA);
                List<CompanyInfo> supplyCacheData = JSONArray.parseArray(supplyCacheStr,
                        CompanyInfo.class);
                if (supplyCacheData != null && supplyCacheData.size() > 0) {
                    companyInfo = supplyCacheData.get(0);
                } else {
                    companyInfo = null;
                }

                String goodsCacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_PURCHASE_CREATERETURN_GOODS_DATA);
                List<CreateOrderItemWrapper> goodsCacheData = JSONArray.parseArray(goodsCacheStr,
                        CreateOrderItemWrapper.class);
                productAdapter.setEntityList(goodsCacheData);
            }
        }

        clearCacheData();

        if (mCurInvSendIoOrder == null){
            inlvBarcode.setVisibility(View.VISIBLE);
            inlvBarcode.requestFocus();
            labelRecvOrder.setLabelText("");
//            productAdapter.setEntityList(null);
        }
        else{
            inlvBarcode.setVisibility(View.GONE);
            labelRecvOrder.setLabelText(mCurInvSendIoOrder.getOrderName());
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
     * 切换收货方
     * */
    private void changeRecvCompany(CompanyInfo companyInfo){
        this.companyInfo = companyInfo;
        labelInvcompProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");
    }

    /**
     * 切换采购收货单,同时更新收货方和采购商品
     */
    private void changeInvRecvOrder(InvSendIoOrder order) {
        mCurInvSendIoOrder = order;
        if (order == null) {
            inlvBarcode.setVisibility(View.VISIBLE);
            inlvBarcode.requestFocus();
            labelRecvOrder.setLabelText("");

            productAdapter.setEntityList(null);//清空商品
            changeRecvCompany(null);
        }
        else {
            CompanyInfo companyInfo = new CompanyInfo();
            companyInfo.setId(mCurInvSendIoOrder.getSendNetId());
            companyInfo.setName(mCurInvSendIoOrder.getSendCompanyName());
            companyInfo.setTenantId(mCurInvSendIoOrder.getSendTenantId());

            inlvBarcode.setVisibility(View.GONE);
            labelRecvOrder.setLabelText(mCurInvSendIoOrder.getOrderName());
//        inlvBarcode.setEnabled(false);
//        btnSelectGoods.setEnabled(false);

//        tvTotalAmount.setText(String.format("商品金额：%.2f", curOrder.getGoodsFee()));

            changeRecvCompany(companyInfo);

            productAdapter.setEntityList(null);//清空商品
            //加载订单明细
            InvOrderApiImpl.getInvSendIoOrderById(mCurInvSendIoOrder.getId(), orderdetailRespCallback);
        }
    }

    NetCallBack.NetTaskCallBack orderdetailRespCallback = new NetCallBack.NetTaskCallBack<InvSendIoOrderItemBrief,
            NetProcessor.Processor<InvSendIoOrderItemBrief>>(
            new NetProcessor.Processor<InvSendIoOrderItemBrief>() {
                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null) {
                        productAdapter.setEntityList(null);
                        return;
                    }
                    //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                    RspBean<InvSendIoOrderItemBrief> retValue = (RspBean<InvSendIoOrderItemBrief>) rspData;
                    InvSendIoOrderItemBrief orderDetail = retValue.getValue();

                    if (orderDetail != null) {
                        productAdapter.setSendIoOrderItems(orderDetail.getItems());
                    } else {
                        productAdapter.setEntityList(null);
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("加载商品失败：" + errMsg);
                    productAdapter.setEntityList(null);
                }
            }
            , InvSendIoOrderItemBrief.class
            , CashierApp.getAppContext()) {
    };

    @OnClick(R.id.button_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        showConfirmDialog("确定要提交退货单吗？",
                "退货", new DialogInterface.OnClickListener() {

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

        List<CreateOrderItemWrapper> goodsList = productAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            btnSubmit.setEnabled(true);
            DialogUtil.showHint("商品不能为空");
            hideProgressDialog();
            return;
        }

        if (companyInfo == null){
            DialogUtil.showHint("请选择收货方！");
            hideProgressDialog();
            btnSubmit.setEnabled(true);
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
        jsonStrObject.put("sendNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("sendTenantId", MfhLoginService.get().getSpid());
        jsonStrObject.put("receiveNetId", companyInfo.getId());
        jsonStrObject.put("tenantId", companyInfo.getTenantId());
        jsonStrObject.put("remark", etRemark.getText().toString());

        JSONArray itemsArray = new JSONArray();
        for (CreateOrderItemWrapper goods : goodsList) {
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());//查询供应链
            item.put("providerId", goods.getProviderId());
//            item.put("providerId", MfhLoginService.get().getSpid());
            item.put("isPrivate", goods.getIsPrivate());//（0：不是 1：是）
            item.put("proSkuId", goods.getProSkuId());
            item.put("productName", goods.getProductName());
            item.put("barcode", goods.getBarcode());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("price", goods.getPrice());
            item.put("amount", goods.getAmount());

            itemsArray.add(item);
        }
        jsonStrObject.put("items", itemsArray);

        InvOrderApiImpl.createInvSendIoBackOrder(mCurInvSendIoOrder != null ? mCurInvSendIoOrder.getId() : null, true,
                jsonStrObject.toJSONString(), responseCallback);
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("新建退货单失败: " + errMsg);
//                    {"code":"1","msg":"132079网点有仓储单正在处理中...","version":"1","data":null}
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
//                    DialogUtil.showHint("新建退货单失败" + errMsg);
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
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
//                        animProgress.setVisibility(View.GONE);
                    /**
                     * 新建退货单成功，更新采购单列表
                     * */
                    ZLogger.d("新建退货单成功: ");
                    hideProgressDialog();
                    getActivity().finish();

                    if (enterMode == 1) {
                        EventBus.getDefault().post(new PurchaseReceiptEvent(PurchaseReceiptEvent.EVENT_ID_RELOAD_DATA));
                    } else {
                        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PURCHASERETURN_ORDER_ENABLED, true);
                        EventBus.getDefault().post(new PurchaseReturnEvent(PurchaseReturnEvent.EVENT_ID_RELOAD_DATA));
                    }

//                    DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    private void refreshBottomBar() {
        if (productAdapter != null && productAdapter.getItemCount() > 0) {
            Double quantityCheck = 0D;
            Double amount = 0D;
            List<CreateOrderItemWrapper> entityList = productAdapter.getEntityList();
            for (CreateOrderItemWrapper entity : entityList) {
                quantityCheck += entity.getQuantityCheck();
                amount += entity.getAmount();
            }
            tvGoodsQuantity.setText(String.format("商品数：%.2f", quantityCheck));
            tvTotalAmount.setText(String.format("商品金额：%.2f", amount));
            btnSubmit.setVisibility(View.VISIBLE);
        } else {
            tvGoodsQuantity.setText(String.format("商品数：%d", 0));
            tvTotalAmount.setText(String.format("商品金额：%.2f", 0D));
            btnSubmit.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onProcess() {

    }

    @Override
    public void onError(String errorMsg) {

    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        if (dataList != null && dataList.size() > 0){
            ChainGoodsSku chainGoodsSku = dataList.get(0);
            if (chainGoodsSku != null && chainGoodsSku.getSingleCostPrice() != null){
                productAdapter.appendSupplyGoods(chainGoodsSku);
            }
            else{
                DialogUtil.showHint("商品信息不完整");
            }
        }
        else{
            DialogUtil.showHint("未找到商品");
        }
    }

}
