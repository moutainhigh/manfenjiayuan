package com.manfenjiayuan.pda_supermarket.ui.store.invRecv;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.DataSyncManager;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.pda.bizz.invrecv.InvRecvConvertAdapter;
import com.bingshanguxue.pda.bizz.invrecv.ProductConvertItemWrapper;
import com.bingshanguxue.pda.dialog.InvSendIoOrderPayDialog;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.common.SecondaryActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.anon.sc.productPrice.PubSkus;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.productConvert.ProductConvert;
import com.mfh.framework.api.productConvert.ProductConvertItem;
import com.mfh.framework.api.productConvert.ProductConvertWrapper;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.httpmgr.AnonScHttpManager;
import com.mfh.framework.rxapi.httpmgr.InvSendIoOrderHttpManager;
import com.mfh.framework.rxapi.httpmgr.ScProductConvertHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;


/**
 * 转换收货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvRecvOrderConvertFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.frame_dest)
    RelativeLayout frameDest;
    @BindView(R.id.office_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private InvRecvConvertAdapter goodsAdapter;
    @BindView(R.id.empty_view)
    View emptyView;

    @BindView(R.id.frame_src)
    RelativeLayout frameSrc;
    @BindView(R.id.label_barcode)
    TextLabelView labelBarcode;
    @BindView(R.id.label_productName)
    TextLabelView labelProductName;
    @BindView(R.id.label_receive_quantity)
    EditLabelView labelReceiveQuantity;
    @BindView(R.id.label_receive_amount)
    EditLabelView labelReceiveAmount;

    /**
     * 批发商
     */
    private CompanyInfo mCompanyInfo = null;
    /**
     * 转换规则
     */
    private ProductConvert mProductConvert = null;
    /**
     * 商品转换规则详情
     */
    private ProductConvertWrapper mProductConvertWrapper = null;
    /**
     * 源商品
     */
    private PubSkus mPubSkus = null;
    /**
     * 目标商品
     */
    private List<ProductConvertItemWrapper> mProductConvertItemWrappers;
    protected Double totalAmount = 0D;//收货金额

    private InvSendIoOrderPayDialog payDialog = null;

    public static InvRecvOrderConvertFragment newInstance(Bundle args) {
        InvRecvOrderConvertFragment fragment = new InvRecvOrderConvertFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected boolean isResponseBackPressed() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inv_receiveorder_convert;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
        }
        if (animType == ANIM_TYPE_NEW_FLOW) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }

        mToolbar.setTitle("转换收货");
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_submit) {
//                    submit();
//                }
//                return true;
//            }
//        });
//        // Inflate a menu to be displayed in the toolbar
//        mToolbar.inflateMenu(R.menu.menu_inv_recv);

        initRecyclerView();

        refresh();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_INVCOMPANY_LIST: {
//                DialogUtil.showHint("选择完批发商");
                if (resultCode == Activity.RESULT_OK) {
                    mCompanyInfo = (CompanyInfo) data.getSerializableExtra("companyInfo");
                }

                if (mCompanyInfo == null) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                } else {
                    refresh();
                }
            }
            break;
            case ARCode.ARC_PRODUCT_CONVERT_LIST: {
                if (resultCode == Activity.RESULT_OK) {
                    mProductConvert = (ProductConvert) data.getSerializableExtra("productConvert");
                }

                if (mProductConvert == null) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                } else {
                    refresh();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onBackPressed() {
        if (goodsAdapter.getItemCount() > 0) {
            showConfirmDialog("退出后商品列表将会清空，确定要退出吗？",
                    "退出", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            getActivity().setResult(Activity.RESULT_CANCELED);
                            getActivity().finish();
                        }
                    }, "点错了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        return isResponseBackPressed();
    }

    private void refresh() {
        if (mCompanyInfo == null) {
            selectInvCompProvider();
            return;
        }

        if (mProductConvert == null) {
            selectProductConvert();
            return;
        }

        if (mProductConvertWrapper == null) {
            loadConvertWrapper();
            return;
        }

        reloadGoods(null);

        Map<String, String> options = new HashMap<>();
        options.put("id", String.valueOf(mProductConvertWrapper.getOriginProSku()));
        AnonScHttpManager.getInstance().getById(options, new Subscriber<PubSkus>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
            }

            @Override
            public void onNext(PubSkus pubSkus) {
                hideProgressDialog();
                reloadGoods(pubSkus);
            }
        });

    }

    /**
     * 选择批发商
     */
    public void selectInvCompProvider() {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INV_COMPANYLIST);
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_INVCOMPANY_LIST);
    }

    /**
     * 选择商品转换规则
     */
    public void selectProductConvert() {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_PRODUCT_CONVERT_LIST);
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_PRODUCT_CONVERT_LIST);
    }

    /**
     * 加载商品规则详情
     */
    private void loadConvertWrapper() {
        Map<String, String> options = new HashMap<>();
        options.put("id", String.valueOf(mProductConvert.getId()));
        ScProductConvertHttpManager.getInstance().getById(options,
                new Subscriber<ProductConvertWrapper>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e("加载商品转换详情失败, " + e.toString());
                        mProductConvertWrapper = null;
                    }

                    @Override
                    public void onNext(ProductConvertWrapper productConvertWrapper) {
                        mProductConvertWrapper = productConvertWrapper;

                        refresh();
                    }
                });
    }

    private void reloadGoods(PubSkus goods) {
        mPubSkus = goods;

        if (goods == null) {
            labelBarcode.setEndText("");
            labelProductName.setEndText("");
            labelReceiveQuantity.setInputAndEnd("", "");
            labelReceiveAmount.setInput("");
        } else {
            ZLogger.d(JSONObject.toJSONString(goods));
            labelBarcode.setEndText(goods.getBarcode());
            labelProductName.setEndText(goods.getName());
//            labelSignQuantity.setEtContent(String.format("%.2f", curGoods.getSignQuantity()));
            //默认签收数量为空，根据实际情况填写
//            labelReceiveQuantity.setInputAndEnd("", mChainGoodsSku.getUnit());
//            labelReceiveAmount.setEndText(curGoods.getUnitSpec());
            labelReceiveAmount.setInput("");
            labelReceiveQuantity.requestFocus();

        }
        mToolbar.setTitle("源商品");
        frameSrc.setVisibility(View.VISIBLE);
        frameDest.setVisibility(View.GONE);
    }

    /**
     * 验货
     */
    @OnClick(R.id.fab_convert)
    public void convert() {
        if (mProductConvertWrapper == null || mPubSkus == null) {
            selectProductConvert();
            return;
        }

        String quantityStr = labelReceiveQuantity.getInput();
        if (StringUtils.isEmpty(quantityStr)) {
            DialogUtil.showHint("请输入签收数量");
            return;
        }

        String amountStr = labelReceiveAmount.getInput();
        if (StringUtils.isEmpty(amountStr)) {
            DialogUtil.showHint("请输入收货金额");
            return;
        }

        Double quantityCheck = Double.valueOf(quantityStr);
        Double amount = Double.valueOf(amountStr);
        Double price = MathCompact.div(amount, quantityCheck);
        ;

        mProductConvertItemWrappers = new ArrayList<>();
        List<ProductConvertItem> details = mProductConvertWrapper.getDetails();
        if (details != null && details.size() > 0) {
            for (ProductConvertItem item : details) {
                ProductConvertItemWrapper wrapper = new ProductConvertItemWrapper();
                wrapper.setProSkuId(item.getProSkuId());
                wrapper.setProSkuName(item.getProSkuName());
                wrapper.setPartRate(item.getPartRate());
                wrapper.setQuantityCheck(MathCompact.mult(item.getPartRate(), quantityCheck));
                wrapper.setPrice(price);
                wrapper.setAmount(MathCompact.mult(price, wrapper.getQuantityCheck()));

                mProductConvertItemWrappers.add(wrapper);
            }
        }

        mToolbar.setTitle("目标商品列表");
        totalAmount = amount;
        goodsAdapter.setEntityList(mProductConvertItemWrappers);
        frameSrc.setVisibility(View.GONE);
        frameDest.setVisibility(View.VISIBLE);
    }

    /**
     * 签收
     */
    @OnClick(R.id.fab_submit)
    public void submit() {
        if (mCompanyInfo == null) {
            onReceiveOrderInterrupted("请选择发货方！");
            selectInvCompProvider();
            return;
        }

        List<ProductConvertItemWrapper> goodsList = goodsAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            onReceiveOrderInterrupted("目标商品不能为空");
            return;
        }

        final JSONArray itemsArray = new JSONArray();
        for (ProductConvertItemWrapper goods : goodsList) {
//            if (goods.getReceivePrice() == null) {
//                ZLogger.d("未设置价格不允许收货");
//                continue;
//            }
            JSONObject item = new JSONObject();
//            item.put("barcode", goods.getBarcode());
            String productName = goods.getProSkuName();
            // TODO: 6/10/16  商品名字太长，后台不允许提交,这里增加一层过滤
            if (!StringUtils.isEmpty(productName) && productName.length() > 10) {
                item.put("productName", productName.substring(0, 10));
            } else {
                item.put("productName", productName);
            }

            item.put("amount", goods.getAmount());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("price", goods.getPrice());
//            item.put("chainSkuId", goods.getChainSkuId());//查询供应链
            item.put("proSkuId", goods.getProSkuId());
            item.put("providerId", mCompanyInfo.getTenantId());
            item.put("isPrivate", IsPrivate.PLATFORM);

            itemsArray.add(item);
//            amount += goods.getReceiveAmount();
        }

        final JSONObject jsonStrObject = new JSONObject();
        if (mCompanyInfo.getTenantId() != null) {
            jsonStrObject.put("sendTenantId", mCompanyInfo.getTenantId());
        }
        jsonStrObject.put("sendStoreType", StoreType.WHOLESALER);
        jsonStrObject.put("isPrivate", IsPrivate.PLATFORM);
        jsonStrObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("tenantId", MfhLoginService.get().getSpid());
        jsonStrObject.put("remark", "");
        jsonStrObject.put("items", itemsArray);

        doSignWork(jsonStrObject, null);
    }

    public void doSignWork(JSONObject jsonStrObject, Long otherOrderId) {
        onReceiveOrderProcess();

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onReceiveOrderInterrupted(getString(R.string.toast_network_error));
            return;
        }

        Map<String, String> options = new HashMap<>();
        if (otherOrderId != null) {
            options.put("otherOrderId", String.valueOf(otherOrderId));
        }
        options.put("checkOk", "true");
        options.put("jsonStr", jsonStrObject.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSendIoOrderHttpManager.getInstance().createRecOrder(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef("新建收货单失败:" + e.toString());
                        onReceiveOrderInterrupted(e.getMessage());
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        onReceiveOrderSucceed(data);
                    }

                });

    }

    public void onReceiveOrderProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在处理订单，请稍后...", false);
    }


    /**
     * 支付中断，取消&失败
     */
    public void onReceiveOrderInterrupted(String message) {
        DialogUtil.showHint(message);
        hideProgressDialog();
    }


    /**
     * 支付成功
     */
    public void onReceiveOrderSucceed(String orderId) {
        ZLogger.d("新建收货单成功: " + orderId);

        //更新商品发生变化，通知POS机同步
        DataSyncManager.getInstance().notifyUpdateSku();

        goodsAdapter.setEntityList(null);

        hideProgressDialog();

//        支付收货订单
        doPayWork(orderId, totalAmount);
    }

    /**
     * 支付订单
     */
    public void doPayWork(String orderId, Double amount) {
        if (StringUtils.isEmpty(orderId)) {
            ZLogger.d("订单无效");
            onOrderPayInterrupted();
            return;
        }

        ZLogger.d(String.format("orderId=%s, amount=%.2f", orderId, amount));
        if (amount <= 0) {
            onOrderPaySucceed();
            return;
        }

        //支付
        if (payDialog == null) {
            payDialog = new InvSendIoOrderPayDialog(getActivity());
            payDialog.setCancelable(false);
            payDialog.setCanceledOnTouchOutside(false);
        }
        payDialog.init(orderId, amount, new InvSendIoOrderPayDialog.DialogClickListener() {
            @Override
            public void onPaySucceed() {
                //支付成功
                onOrderPaySucceed();
            }

            @Override
            public void onPayFailed() {

            }

            @Override
            public void onPayCanceled() {
                onOrderPayInterrupted();
            }
        });
        if (!payDialog.isShowing()) {
            payDialog.show();
        }
    }


    /**
     * 支付成功
     */
    public void onOrderPaySucceed() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    /**
     * 支付中断，取消&失败
     */
    public void onOrderPayInterrupted() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
//        addressRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);

        goodsAdapter = new InvRecvConvertAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new InvRecvConvertAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, final int position) {


            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
            }
        });

        goodsRecyclerView.setAdapter(goodsAdapter);
    }

}
