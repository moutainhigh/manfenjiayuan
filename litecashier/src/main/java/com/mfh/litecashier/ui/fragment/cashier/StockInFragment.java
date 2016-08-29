package com.mfh.litecashier.ui.fragment.cashier;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.widget.AvatarView;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.Human;
import com.mfh.litecashier.bean.HumanCompany;
import com.mfh.litecashier.bean.HumanCompanyOption;
import com.mfh.litecashier.bean.ReceiveOrderHumanInfo;
import com.mfh.litecashier.bean.StockInItem;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfo;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.ui.activity.CashierPayActivity;
import com.mfh.litecashier.ui.activity.ServiceActivity;
import com.mfh.litecashier.ui.adapter.StockInAdapter;
import com.mfh.litecashier.ui.dialog.ExpressCompanyDialog;
import com.mfh.litecashier.ui.dialog.QueryDialog;
import com.mfh.litecashier.ui.dialog.ReceiveOrderAddressDialog;
import com.mfh.litecashier.utils.DataCacheHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 服务－－快递代收
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class StockInFragment extends BaseFragment {

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @Bind(R.id.iv_member_header)
    AvatarView ivMemberHeader;
    @Bind(R.id.tv_member_name)
    TextView tvMemberName;
    @Bind(R.id.tv_member_company)
    TextView tvMemberCompany;
    @Bind(R.id.tv_express_amount)
    TextView tvExpressAmount;
    @Bind(R.id.et_barCode)
    EditText etBarCode;
    @Bind(R.id.et_phoneNumber)
    EditText etPhonenumber;
    @Bind(R.id.tv_quantity)
    TextView tvQuantity;
    @Bind(R.id.tv_soft_fee)
    TextView tvSoftFee;
    @Bind(R.id.tv_sms_fee)
    TextView tvSmsFee;
    @Bind(R.id.tv_storage_fee)
    TextView tvStorageFee;
    @Bind(R.id.tv_batch_income)
    TextView tvBatchIncome;
    @Bind(R.id.button_stockIn)
    Button btnSubmit;
    @Bind(R.id.product_list)
    RecyclerView productRecyclerView;

    private ItemTouchHelper itemTouchHelper;
    private StockInAdapter productAdapter;


    private String curBatchId;
    private Double serviceFee = 0d;

    private QueryDialog payDialog = null;
    private ExpressCompanyDialog companyDialog = null;
    private ReceiveOrderAddressDialog addressDialog = null;

    private Human courier = null;//快递员信息

    public static StockInFragment newInstance(Bundle args) {
        StockInFragment fragment = new StockInFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stock_in;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        tvHeaderTitle.setText("快递代收");
        ivMemberHeader.setBorderWidth(3);
        ivMemberHeader.setBorderColor(Color.parseColor("#e8e8e8"));

        initRecyclerView();

        initBarCodeInput();
        initPhoneInput();

        refreshBottomBar();

        Bundle args = getArguments();
        if (args != null) {
            courier = (Human)args.getSerializable(ServiceActivity.EXTRA_KEY_COURIER);
        }

        loadCourierInfo();
    }

    @Override
    public void onResume() {
        super.onResume();

        initInputStatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_MFPAY: {
                //清空会员信息
                DataCacheHelper.getInstance().setMfMemberInfo(null);

                if (resultCode == Activity.RESULT_OK) {

                    if (data != null) {
                        CashierOrderInfo cashierOrderInfo = (CashierOrderInfo)data
                                .getSerializableExtra(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO);
                        serviceFee += cashierOrderInfo.getRetailAmount();
                        //显示找零
//                        SerialManager.show(4, cashierOrderInfo.getHandleAmount());
                        SerialManager.vfdShow(String.format("Change:%.2f\r\nThank You!",
                                Math.abs(cashierOrderInfo.getHandleAmount())));
                    }

                    refreshBottomBar();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().finish();
    }

    @OnClick(R.id.button_express_pay)
    public void payExpress() {
        if (payDialog == null) {
            payDialog = new QueryDialog(getActivity());
            payDialog.setCancelable(false);
            payDialog.setCanceledOnTouchOutside(false);
        }
        payDialog.init(QueryDialog.DT_EXPRESS_PAY, new QueryDialog.DialogListener() {
            @Override
            public void query(String text) {

            }

            @Override
            public void onNextStep(String fee) {
                Double amount;
                if (StringUtils.isEmpty(fee)) {
                    amount = 0D;
                } else {
                    amount = Double.valueOf(fee);
                }

                //当前收银信息
                CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
                cashierOrderInfo.init(null);
                cashierOrderInfo.setRetailAmount(amount);
                cashierOrderInfo.setDealAmount(amount);
                cashierOrderInfo.setDiscountAmount(0D);
                cashierOrderInfo.setDiscountRate(1D);
                String subject = String.format("快递入库，批次: %s", curBatchId);
                cashierOrderInfo.initSetle(BizType.STOCK, "", curBatchId, subject, "", null);

                Intent intent = new Intent(getActivity(), CashierPayActivity.class);
                Bundle extras = new Bundle();
                extras.putSerializable(CashierPayActivity.EXTRA_KEY_CASHIER_ORDERINFO, cashierOrderInfo);
                intent.putExtras(extras);
                startActivityForResult(intent, Constants.ARC_MFPAY);
            }

            @Override
            public void onNextStep(Human human) {

            }

            @Override
            public void onNextStep() {

            }
        });
        if (!payDialog.isShowing()) {
            payDialog.show();
        }
    }

    @OnClick(R.id.button_query)
    public void query() {
        final String barCode = etBarCode.getText().toString();
        if (StringUtils.isEmpty(barCode)) {
            DialogUtil.showHint("快递单号不能为空");
            return;
        }

        final String phoneNumber = etPhonenumber.getText().toString();
        if (StringUtils.isEmpty(phoneNumber)) {
            DialogUtil.showHint("手机号不能为空");
            return;
        }

        NetCallBack.NetTaskCallBack findHumanInfoByMobileRspCallback = new NetCallBack.NetTaskCallBack<ReceiveOrderHumanInfo,
                NetProcessor.Processor<ReceiveOrderHumanInfo>>(
                new NetProcessor.Processor<ReceiveOrderHumanInfo>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        try {

                            List<ReceiveOrderHumanInfo> humanInfos = new ArrayList<>();
                            if (rspData != null) {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspListBean
                                //java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspBean
                                RspListBean<ReceiveOrderHumanInfo> retValue = (RspListBean<ReceiveOrderHumanInfo>) rspData;
                                humanInfos = retValue.getValue();
                            }

                            if (humanInfos == null || humanInfos.size() < 1) {
                                saveUnbindStockItem(barCode, phoneNumber);
                            } else {
                                if (humanInfos.size() > 2){
                                    //消息缓存：通过 Handler 的 obtainMessage 回收 Message 对象，减少 Message 对象的创建开销
//                                    fragmentHandler.sendMessage(fragmentHandler.obtainMessage(MSG_SELECT_ADDRESS, humanInfos));
//                                    List<ReceiveOrderHumanInfo> humanInfos = (List<ReceiveOrderHumanInfo>) msg.obj;
                                    if (humanInfos.size() < 2) {
//                        DialogUtil.showHint("用户收货地址不能为空");
                                        return;
                                    }

                                    //选择收货地址
                                    if (addressDialog == null) {
                                        addressDialog = new ReceiveOrderAddressDialog(getActivity());
                                        addressDialog.setCancelable(true);
                                        addressDialog.setCanceledOnTouchOutside(true);
                                    }
                                    addressDialog.init(humanInfos, new ReceiveOrderAddressDialog.OnResponseCallback() {
                                        @Override
                                        public void onItemClick(ReceiveOrderHumanInfo entity) {

                                            saveStockItem(barCode, entity);
                                        }
                                    });
                                    if (!addressDialog.isShowing()) {
                                        addressDialog.show();
                                    }
                                }
                                else if (humanInfos.size() > 1) {
                                    initInputStatus();
                                } else {
                                    saveStockItem(barCode, humanInfos.get(0));
                                }
                            }
                        } catch (Exception ex) {
                            ZLogger.e("快递代收，查询用户信息 异常 " + ex.toString());
//                            DialogUtil.showHint("查询用户信息失败");
                            saveUnbindStockItem(barCode, phoneNumber);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        1:12345卡芯片号不存在，请重新输入!
                        ZLogger.e("快递代收，查询用户信息 失败, " + errMsg);
//                        DialogUtil.showHint("查询用户信息失败");

                        saveUnbindStockItem(barCode, phoneNumber);
                    }
                }
                , ReceiveOrderHumanInfo.class
                , CashierApp.getAppContext()) {
        };

        if (NetWorkUtil.isConnect(CashierApp.getAppContext())){
            CashierApiImpl.findHumanInfoByMobile(phoneNumber, findHumanInfoByMobileRspCallback);
        }else{
            saveUnbindStockItem(barCode, phoneNumber);
        }
    }

    /**
     * 入库并通知
     */
    @OnClick(R.id.button_stockIn)
    public void stockInItems() {
        btnSubmit.setEnabled(false);
        if (productAdapter == null || productAdapter.getItemCount() < 1) {
            btnSubmit.setEnabled(true);
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        List<StockInItem> items = productAdapter.getEntityList();
        JSONArray jsonArray = new JSONArray();
        for (StockInItem item : items) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fdorderNumber", item.getFdorderNumber());
            jsonObject.put("mobile", item.getMobile());
            if (!StringUtils.isEmpty(item.getHumanId())) {
                jsonObject.put("humanId", item.getHumanId());
            }//1-订单类 2-快递类 99-库存类
            jsonObject.put("needmsg", item.getNeedmsg());
            jsonObject.put("mustsms", item.getMustsms());
            jsonObject.put("addrvalId", item.getAddrvalId());

            jsonArray.add(jsonObject);
        }

        //回调
        NetCallBack.NetTaskCallBack batchResponseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        try {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String batchId = retValue.getValue();
                            ZLogger.d("入库成功:" + batchId);
                            btnSubmit.setEnabled(true);
//                            DialogUtil.showHint("入库成功");
                            if (batchId.equals(curBatchId)) {
                                finishActivity();
                            }
                        } catch (Exception ex) {
                            ZLogger.e("batchResponseCallback, " + ex.toString());
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        {"code":"2","msg":"系统已存在未出库的4快递单,不允许重复录入", "version":"1","data":""}
                        ZLogger.d("入库失败：" + errMsg);
                        DialogUtil.showHint(errMsg);
                        btnSubmit.setEnabled(true);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.receiveOrderStockInItems(curBatchId, jsonArray.toJSONString(), batchResponseCallback);
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        productRecyclerView.setHasFixedSize(true);
//        添加分割线
        productRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        productAdapter = new StockInAdapter(CashierApp.getAppContext(), null);
        productAdapter.setOnAdapterListener(new StockInAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onDataSetChanged() {
                refreshBottomBar();

                //显示 总计－－金额
//                SerialDisplayHelper.show(2, productAdapter.getProductAmount());
            }
        });

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(productAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(productRecyclerView);

        productRecyclerView.setAdapter(productAdapter);

    }

    private void initBarCodeInput() {
        etBarCode.setFocusable(true);
        etBarCode.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
        etBarCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etBarCode);
                }
                etBarCode.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etBarCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        etPhonenumber.requestFocus();
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    private void initPhoneInput() {
        etPhonenumber.setFocusable(true);
        etPhonenumber.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etPhonenumber.requestFocus();
//        etPhonenumber.setCursorVisible(false);//隐藏光标
        etPhonenumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etPhonenumber);
                }
                etPhonenumber.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        etPhonenumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etPhonenumber): keyCode=%d, action=%d", keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER  || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        query();
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
     * 加载快递员信息
     */
    private void loadCourierInfo() {
        if (courier != null) {
            ivMemberHeader.setAvatarUrl(courier.getHeadimageUrl());
            tvMemberName.setText(courier.getName());
            tvMemberCompany.setText("[]");

            //创建批次
            CashierApiImpl.receiveBatchCreateAndFee(MfhLoginService.get().getCurStockId(), courier.getGuid(), createBatchResponseCallback);

            //查询快递公司
            loadCompany(true);

        } else {
            ivMemberHeader.setImageResource(R.drawable.chat_tmp_user_head);
            tvMemberName.setText("");
            tvMemberCompany.setText("[]");

            DialogUtil.showHint("快递人员信息加载失败，请退出重试");
        }
    }

    /**
     * 刷新底部信息
     * */
    private void refreshBottomBar() {
        if (productAdapter != null && productAdapter.getItemCount() > 0) {
            btnSubmit.setVisibility(View.VISIBLE);

            List<StockInItem> smsList = productAdapter.getSmsEntityList();
            Double smsFee = smsList.size() * 0.1;
            int count = productAdapter.getItemCount();
            Double softFee = count * 0.1;
            Double batchIncome = serviceFee - smsFee - softFee;

            tvExpressAmount.setText(String.format("%.2f", serviceFee));
            tvQuantity.setText(String.format("数量：%d", count));
            tvSoftFee.setText(Html.fromHtml(String.format("<font color=#000000>软件费：</font><font color=#FF009B4E>-%.2f</font>", softFee)));
            tvSmsFee.setText(Html.fromHtml(String.format("<font color=#000000>短信费：</font><font color=#FF009B4E>-%.2f</font>", smsFee)));
            if (serviceFee < 0) {
                tvStorageFee.setText(Html.fromHtml(String.format("<font color=#000000>保管费：</font><font color=#FF009B4E>%.2f</font>", serviceFee)));
            } else {
                tvStorageFee.setText(Html.fromHtml(String.format("<font color=#000000>保管费：</font><font color=#FF009B4E>＋%.2f</font>", serviceFee)));
            }

            if (batchIncome < 0) {
                tvBatchIncome.setText(Html.fromHtml(String.format("<font color=#000000>批次收益：</font><font color=#FF009B4E>%.2f</font>", batchIncome)));
            } else {
                tvBatchIncome.setText(Html.fromHtml(String.format("<font color=#000000>批次收益：</font><font color=#FF009B4E>＋%.2f</font>", batchIncome)));
            }
        } else {
            btnSubmit.setVisibility(View.INVISIBLE);

            Double smsFee = 0D;
            Double softFee = 0D;
            Double batchIncome = serviceFee - smsFee - softFee;

            tvExpressAmount.setText(String.format("%.2f", serviceFee));
            tvQuantity.setText(String.format("数量：%d", 0));
            tvSoftFee.setText(Html.fromHtml(String.format("<font color=#000000>软件费：</font><font color=#FF009B4E>-%.2f</font>", softFee)));
            tvSmsFee.setText(Html.fromHtml(String.format("<font color=#000000>短信费：</font><font color=#FF009B4E>-%.2f</font>", smsFee)));
            if (serviceFee < 0) {
                tvStorageFee.setText(Html.fromHtml(String.format("<font color=#000000>保管费：</font><font color=#FF009B4E>%.2f</font>", serviceFee)));
            } else {
                tvStorageFee.setText(Html.fromHtml(String.format("<font color=#000000>保管费：</font><font color=#FF009B4E>＋%.2f</font>", serviceFee)));
            }

            if (batchIncome < 0) {
                tvBatchIncome.setText(Html.fromHtml(String.format("<font color=#000000>批次收益：</font><font color=#FF009B4E>%.2f</font>", batchIncome)));
            } else {
                tvBatchIncome.setText(Html.fromHtml(String.format("<font color=#000000>批次收益：</font><font color=#FF009B4E>＋%.2f</font>", batchIncome)));
            }
        }
    }

    /***
     * 初始化输入状态
     */
    private void initInputStatus() {
        etBarCode.getText().clear();
        etBarCode.requestFocus();
        etPhonenumber.getText().clear();
    }

    /**
     * 保存快递单－－未绑定用户
     */
    private void saveUnbindStockItem(String fdorderNumber, String phoneNumber) {
        ReceiveOrderHumanInfo humanInfo = new ReceiveOrderHumanInfo();
        humanInfo.setName("");
        humanInfo.setMobile(phoneNumber);
        humanInfo.setBindwx(0);
        humanInfo.setAddress("");
        humanInfo.setHumanid(null);

        saveStockItem(fdorderNumber, humanInfo);
    }

    /**
     * 保存快递单
     */
    private void saveStockItem(String fdorderNumber, ReceiveOrderHumanInfo entity) {
        StockInItem item = new StockInItem();
        item.setName(entity.getName());
        item.setMobile(entity.getMobile());
        item.setFdorderNumber(fdorderNumber);
        item.setBindwx(entity.getBindwx());
        if (entity.getBindwx().equals(1)) {
            item.setMustsms(0);
        } else {
            item.setMustsms(1);
        }
        item.setNeedmsg(0);
        item.setHumanId(entity.getHumanid() == null ? "" : String.valueOf(entity.getHumanid()));

        productAdapter.appendEntity(item);

        initInputStatus();
    }

    //创建批次
    private NetCallBack.NetTaskCallBack createBatchResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    try {
//                        {"code":"0","msg":"新增成功!","version":"1","data":{"val":"40513"}}
//                        java.lang.ClassCastException: java.lang.Integer cannot be cast to com.alibaba.fastjson.JSONObject
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        curBatchId = retValue.getValue();
                        ZLogger.d("创建批次成功:" + curBatchId);
                    } catch (Exception ex) {
                        ZLogger.e("创建批次失败:" + ex.toString());
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.e("创建批次失败:" + errMsg);
                    finishActivity();
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    private void loadCompany(final boolean bNeedCreate) {
        if (courier == null) {
            return;
        }

        NetCallBack.NetTaskCallBack queryRespCallback = new NetCallBack.NetTaskCallBack<HumanCompany,
                NetProcessor.Processor<HumanCompany>>(
                new NetProcessor.Processor<HumanCompany>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        try {
                            HumanCompany humanCompany = null;
                            if (rspData != null) {
//                        java.lang.ClassCastException: java.lang.Integer cannot be cast to com.alibaba.fastjson.JSONObject
                                RspBean<HumanCompany> retValue = (RspBean<HumanCompany>) rspData;
                                humanCompany = retValue.getValue();
                            }

                            List<HumanCompanyOption> options = new ArrayList<>();
                            if (humanCompany != null) {
                                options = humanCompany.getOptions();
                            }

                            if (options != null && options.size() > 0) {
                                HumanCompanyOption option = options.get(0);
                                tvMemberCompany.setText(String.format("[%s]", option.getValue()));

                                if (options.size() > 1) {
                                    selectCompany(options);
                                }
                            } else {
                                if (bNeedCreate) {
                                    createNewCompany();
                                }
                            }

                        } catch (Exception ex) {
                            ZLogger.e("queryRespCallback, " + ex.toString());
                        }
                    }
                }
                , HumanCompany.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.findCompanyByHumanId(courier.getGuid(), queryRespCallback);
    }

    /**选择快递公司*/
    private void selectCompany(List<HumanCompanyOption> options){
        if (companyDialog == null) {
            companyDialog = new ExpressCompanyDialog(getActivity());
            companyDialog.setCancelable(true);
            companyDialog.setCanceledOnTouchOutside(true);
        }
        companyDialog.init(ExpressCompanyDialog.DT_SELECT, options, new ExpressCompanyDialog.OnResponseCallback() {
            @Override
            public void saveHumanFdCompany(HumanCompanyOption option) {
//                                            Human courier = DataCacheHelper.getInstance().getCourier();
//                                            if (courier != null) {
//                                                CashierApiImpl.receiveBatchSaveHumanFDCompany(courier.getId(), option.getCode(), saveFDCompayRspCallback);
//                                            }
            }

            @Override
            public void onSelectCompany(String value) {
                //刷新
                tvMemberCompany.setText(String.format("[%s]", value));
            }
        });
        if (!companyDialog.isShowing()) {
            companyDialog.show();
        }
    }

    /**添加快递公司*/
    private void createNewCompany(){
        if (companyDialog == null) {
            companyDialog = new ExpressCompanyDialog(getActivity());
            companyDialog.setCancelable(true);
            companyDialog.setCanceledOnTouchOutside(true);
        }
        companyDialog.init(ExpressCompanyDialog.DT_CREATE, null, new ExpressCompanyDialog.OnResponseCallback() {
            @Override
            public void saveHumanFdCompany(HumanCompanyOption option) {
                CashierApiImpl.receiveBatchSaveHumanFDCompany(courier.getGuid(), option.getCode(), saveFDCompayRspCallback);
            }

            @Override
            public void onSelectCompany(String value) {
            }
        });
        if (!companyDialog.isShowing()) {
            companyDialog.show();
        }
    }


    //保存快递公司
    private NetCallBack.NetTaskCallBack saveFDCompayRspCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    try {
//                        {"code":"0","msg":"新增成功!","version":"1","data":{"val":"40513"}}
//                        java.lang.ClassCastException: java.lang.Integer cannot be cast to com.alibaba.fastjson.JSONObject
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();

                        ZLogger.d(retStr);

                        loadCompany(false);
                    } catch (Exception ex) {
                        ZLogger.e("saveFDCompayRspCallback, " + ex.toString());
                    }
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };
}
