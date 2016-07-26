package com.manfenjiayuan.pda_wholesaler.ui.fragment.invsendio;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.EditQueryView;
import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.bean.InvFindOrderItemBrief;
import com.manfenjiayuan.business.bean.wrapper.NetInfoWrapper;
import com.manfenjiayuan.business.presenter.InvFindOrderPresenter;
import com.manfenjiayuan.business.view.IInvFindOrderView;
import com.manfenjiayuan.pda_wholesaler.Constants;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.database.entity.InvIoPickGoodsEntity;
import com.manfenjiayuan.pda_wholesaler.database.logic.InvIoPickGoodsService;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SecondaryActivity;
import com.manfenjiayuan.pda_wholesaler.ui.adapter.PickingGoodsAdapter;
import com.manfenjiayuan.pda_wholesaler.ui.dialog.SelectTenantDialog;
import com.manfenjiayuan.pda_wholesaler.ui.fragment.receipt.DistributionInspectFragment;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.api.scChainGoodsSku.ScChainGoodsSkuApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 拣货发货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvIoPickingGoodsFragment extends PDAScanFragment
        implements IInvFindOrderView {

    public static final String EXTRA_KEY_ORDER_BARCODE = "orderBarcode";

    @Bind(R.id.providerView)
    NaviAddressView mProviderView;
    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private PickingGoodsAdapter officeAdapter;

    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.button_submit)
    View btnSign;

    private SelectTenantDialog mSelectTenantDialog = null;

    /**
     * 接收方网点信息
     */
    private NetInfoWrapper mNetInfoWrapper = null;
    private InvFindOrderPresenter mInvFindOrderPresenter;

    public static InvIoPickingGoodsFragment newInstance(Bundle args) {
        InvIoPickingGoodsFragment fragment = new InvIoPickingGoodsFragment();

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
        return R.layout.fragment_picking_goods;
    }

    @Override
    protected void onScanCode(String code) {
        eqvBarcode.requestFocus();
        eqvBarcode.clear();
        inspect(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInvFindOrderPresenter = new InvFindOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();

        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setHoldFocusEnable(false);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                onScanCode(text);
            }
        });

        String orderBarcode = null;
        Bundle args = getArguments();
        if (args != null) {
            orderBarcode = args.getString(EXTRA_KEY_ORDER_BARCODE);
        }

        if (!StringUtils.isEmpty(orderBarcode)) {
            loadOrderItemsByBarcode(orderBarcode);
        } else {
            if (mNetInfoWrapper == null) {
                selectInvCompProvider();
            } else {
                eqvBarcode.requestFocus();
                eqvBarcode.clear();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eqvBarcode.requestFocus();
        eqvBarcode.clear();
    }

    @Override
    public boolean onBackPressed() {
//        DialogUtil.showHint("onBackPressed");
        if (officeAdapter.getItemCount() > 0) {
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
//            return true;
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
//            return false;
        }

        return isResponseBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_DISTRIBUTION_INSPECT: {
                officeAdapter.setEntityList(InvIoPickGoodsService.get().queryAll());
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 切换发货方
     */
    private void changeSendCompany(CompanyInfo companyInfo) {
        eqvBarcode.clear();
        eqvBarcode.requestFocus();
        if (companyInfo != null) {
            this.mNetInfoWrapper = new NetInfoWrapper();
            this.mNetInfoWrapper.setNetId(companyInfo.getId());
            this.mNetInfoWrapper.setName(companyInfo.getName());
//        this.mLabelProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");
            this.mProviderView.setText(mNetInfoWrapper.getName());
        } else {
            this.mNetInfoWrapper = null;
            this.mProviderView.setText("");
        }
    }


    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        addressRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        addressRecyclerView.setHasFixedSize(true);
        //添加分割线
        addressRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        addressRecyclerView.setEmptyView(emptyView);

        officeAdapter = new PickingGoodsAdapter(getActivity(), null);
        officeAdapter.setOnAdapterListener(new PickingGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO: 5/18/16
                InvIoPickGoodsEntity entity = officeAdapter.getEntityList().get(position);
                inspect(entity.getBarcode());
            }

            @Override
            public void onItemLongClick(View view, final int position) {
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
                eqvBarcode.requestFocus();
                eqvBarcode.clear();

                if (officeAdapter.getItemCount() > 0) {
                    btnSign.setEnabled(true);
                } else {
                    btnSign.setEnabled(false);
                }
            }
        });

        addressRecyclerView.setAdapter(officeAdapter);
    }


    /**
     * 签收
     */
    @OnClick(R.id.button_submit)
    public void submit() {
        btnSign.setEnabled(false);

        if (mNetInfoWrapper == null) {
            DialogUtil.showHint("请选择接收方！");
            btnSign.setEnabled(true);
            hideProgressDialog();
            return;
        }

        showConfirmDialog("米西小贴士：请确认已经查验过所有商品。",
                "发货", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        doSubmitWork();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        btnSign.setEnabled(true);
                    }
                });
    }

    /**
     * 发货
     * */
    private void doSubmitWork() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在创建发货单...", false);

        JSONObject jsonStrObject = new JSONObject();
        jsonStrObject.put("sendNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("sendTenantId", MfhLoginService.get().getSpid());
        //传了receiveNetId，receiveStoreType可传可不传
        jsonStrObject.put("receiveNetId", mNetInfoWrapper.getNetId());//网点
        jsonStrObject.put("remark", "");
        jsonStrObject.put("bizType", InvOrderApi.BIZTYPE_PURCHASE);
        jsonStrObject.put("orderType", InvOrderApi.ORDERTYPE_RECEIPT);

        JSONArray itemsArray = new JSONArray();
        for (InvIoPickGoodsEntity goods : officeAdapter.getEntityList()) {
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());//查询供应链
            item.put("providerId", goods.getProviderId());
            item.put("isPrivate", goods.getIsPrivate());//（0：不是 1：是）
            item.put("proSkuId", goods.getProSkuId());
            item.put("productName", goods.getProductName());
            item.put("barcode", goods.getBarcode());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("price", goods.getPrice());//批发价
            item.put("amount", goods.getAmount());

            itemsArray.add(item);
        }
        jsonStrObject.put("items", itemsArray);

        InvSendIoOrderApiImpl.createInvSendIoOrder(true, jsonStrObject.toJSONString(), submitRC);
    }

    private NetCallBack.NetTaskCallBack submitRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("新建发货单失败: " + errMsg);
                    //查询失败
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    btnSign.setEnabled(true);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}

//                        animProgress.setVisibility(View.GONE);
                    /**
                     * 新建调拨单，更新采购单列表
                     * */
                    ZLogger.d("新建发货单成功: ");
                    changeSendCompany(null);
                    btnSign.setEnabled(true);
                    officeAdapter.setEntityList(null);
                    showProgressDialog(ProgressDialog.STATUS_DONE, "发货成功", true);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


    /**
     * 加载拣货单明细
     */
    private void loadOrderItemsByBarcode(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        mInvFindOrderPresenter.loadOrderItemsByBarcode(barcode);
    }

    @Override
    public void onQueryInvFindOrderProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在加载拣货单明细", false);
    }

    @Override
    public void onQueryInvFindOrderError(String errorMsg) {
        hideProgressDialog();
    }

    @Override
    public void onQueryInvFindOrderSuccess(InvFindOrderItemBrief data) {
        if (data != null) {
            this.mNetInfoWrapper = new NetInfoWrapper();
            mNetInfoWrapper.setNetId(data.getTargetNetId());
            mNetInfoWrapper.setName(data.getTargetNetCaption());

            InvIoPickGoodsService.get().saveInvFindOrderItems(data.getItems());
            officeAdapter.setEntityList(InvIoPickGoodsService.get().queryAll());

            // TODO: 6/8/16 获取商品批发价（拣货单商品没有批发价）
            retrieveGoodsPrice();
        }

        hideProgressDialog();
    }

    /**
     * 获取商品价格*
     */
    private void retrieveGoodsPrice() {
        List<InvIoPickGoodsEntity> entityList = officeAdapter.getEntityList();
        if (entityList == null || entityList.size() < 1) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (InvIoPickGoodsEntity wrapper : entityList) {
            if (wrapper.getProSkuId() == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(wrapper.getProSkuId());
        }

        PageInfo pageInfo = new PageInfo(1, entityList.size());

        NetCallBack.QueryRsCallBack retrievePriceRC = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                if (rs == null) {
                    return;
                }

                List<ScGoodsSku> scGoodsSkus = new ArrayList<>();
                for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                    scGoodsSkus.add(wrapper.getBean());
                }

                //刷新商品价格
                InvIoPickGoodsService.get().infusePriceList(scGoodsSkus);
                officeAdapter.setEntityList(InvIoPickGoodsService.get().queryAll());
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);

                ZLogger.d("获取拣货单商品价格失败：" + errMsg);
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScChainGoodsSkuApiImpl.scChainGoodsSkuList(sb.toString(), retrievePriceRC);
    }

    /**
     * 拣货
     * */
    private void inspect(String barcode) {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INVIO_PICK_GOODS);
        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);
//        extras.putInt(DistributionInspectFragment.EXTRA_KEY_INSPECTMODE, 1);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_DISTRIBUTION_INSPECT);
    }

    /**
     * 选择批发商
     */
    @OnClick(R.id.providerView)
    public void selectInvCompProvider() {
        if (mSelectTenantDialog == null) {
            mSelectTenantDialog = new SelectTenantDialog(getActivity());
            mSelectTenantDialog.setCancelable(true);
            mSelectTenantDialog.setCanceledOnTouchOutside(false);
        }
        mSelectTenantDialog.init(AbilityItem.TENANT, new SelectTenantDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                changeSendCompany(companyInfo);
            }

        });
        if (!mSelectTenantDialog.isShowing()) {
            mSelectTenantDialog.show();
        }
    }

}
