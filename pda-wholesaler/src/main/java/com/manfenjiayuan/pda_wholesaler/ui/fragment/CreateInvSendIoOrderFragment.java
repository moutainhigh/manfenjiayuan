package com.manfenjiayuan.pda_wholesaler.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.bizz.company.CompanyListFragment;
import com.bingshanguxue.pda.bizz.invio.InvIoGoodsInspectFragment;
import com.bingshanguxue.pda.bizz.invsendio.InvSendIoGoodsAdapter;
import com.bingshanguxue.pda.database.entity.InvSendIoGoodsEntity;
import com.bingshanguxue.pda.database.service.InvSendIoGoodsService;
import com.bingshanguxue.pda.dialog.ActionDialog;
import com.manfenjiayuan.business.bean.wrapper.NetInfoWrapper;
import com.manfenjiayuan.pda_wholesaler.Constants;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SecondaryActivity;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.api.scChainGoodsSku.ScChainGoodsSkuApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 拣货发货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateInvSendIoOrderFragment extends BaseFragment {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.providerView)
    NaviAddressView mProviderView;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private InvSendIoGoodsAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;
    @Bind(R.id.empty_view)
    View emptyView;


    private ActionDialog mActionDialog = null;

    /**
     * 接收方网点信息
     */
    private NetInfoWrapper mNetInfoWrapper = null;

    public static CreateInvSendIoOrderFragment newInstance(Bundle args) {
        CreateInvSendIoOrderFragment fragment = new CreateInvSendIoOrderFragment();

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
        return R.layout.fragment_create_inv_sendioorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InvSendIoGoodsService.get().clear();
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
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
                if (id == R.id.action_submit) {
                    submit();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_inv_sendio);

        mProviderView.setEnabled(false);
        initRecyclerView();

        selectEntryMode();
    }

    @Override
    public boolean onBackPressed() {
//        DialogUtil.showHint("onBackPressed");
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
                goodsAdapter.setEntityList(InvSendIoGoodsService.get().queryAll());
            }
            break;
            case Constants.ARC_COMPANY_LIST: {
                if (resultCode == Activity.RESULT_OK) {
                    CompanyInfo companyInfo = (CompanyInfo) data.getSerializableExtra("companyInfo");
                    if (companyInfo != null) {
                        changeSendCompany(companyInfo);
                    }
                }

                if (mNetInfoWrapper == null) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
            break;
            case Constants.ARC_INVFINDORDER_INSPECT: {
                if (resultCode == Activity.RESULT_OK) {
                    NetInfoWrapper netInfoWrapper = (NetInfoWrapper) data.getSerializableExtra("netInfoWrapper");
                    importInvFindOrder(netInfoWrapper);
                } else {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 选择入口
     */
    private void selectEntryMode() {
        if (mActionDialog == null) {
            mActionDialog = new ActionDialog(getActivity());
            mActionDialog.setCancelable(false);
            mActionDialog.setCanceledOnTouchOutside(false);
        }
        mActionDialog.init("新建发货单", "可以选择以下方式新建收货单",
                new ActionDialog.DialogClickListener() {
                    @Override
                    public void onAction1Click() {
                        if (mNetInfoWrapper == null) {
                            selectInvCompProvider();
                        }
                    }

                    @Override
                    public void onAction2Click() {
//                        entryMode = SendIoEntryMode.SENDIOORDER;
//                        fetchSendIoOrder();
                    }

                    @Override
                    public void onAction3Click() {
                        fetchInvFindOrder();
                    }
                });
        mActionDialog.registerActions("手动输入商品", null, "导入拣货单");
        if (!mActionDialog.isShowing()) {
            mActionDialog.show();
        }
    }

    /**
     * 切换发货方
     */
    private void changeSendCompany(CompanyInfo companyInfo) {
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

    private CommonDialog operateDialog = null;

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);

        goodsAdapter = new InvSendIoGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new InvSendIoGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                InvSendIoGoodsEntity entity = goodsAdapter.getEntityList().get(position);
                inspect(entity.getBarcode());
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                final InvSendIoGoodsEntity entity = goodsAdapter.getEntity(position);
                if (operateDialog == null) {
                    operateDialog = new CommonDialog(getActivity());
                    operateDialog.setCancelable(true);
                }
                operateDialog.setMessage(String.format("%s\n%s",
                        entity.getBarcode(), entity.getProductName()));
                operateDialog.setPositiveButton("拒收",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                InvSendIoGoodsService.get().reject(entity);

                                goodsAdapter.notifyItemChanged(position);
                            }
                        });
                operateDialog.setNegativeButton("删除", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goodsAdapter.removeEntity(position);
                    }
                });
                if (!operateDialog.isShowing()) {
                    operateDialog.show();
                }
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
            }
        });

        goodsRecyclerView.setAdapter(goodsAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(goodsAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(goodsRecyclerView);
    }


    /**
     * 签收
     */
    public void submit() {
        if (mNetInfoWrapper == null) {
            DialogUtil.showHint("请选择接收方！");
            hideProgressDialog();
            selectInvCompProvider();
            return;
        }

        if (goodsAdapter.getItemCount() < 1) {
            DialogUtil.showHint("商品不能为空");
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
                    }
                });
    }

    /**
     * 发货
     */
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
        for (InvSendIoGoodsEntity goods : goodsAdapter.getEntityList()) {
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
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}

//                        animProgress.setVisibility(View.GONE);
                    ZLogger.d("新建发货单成功: ");
//                    changeSendCompany(null);
//                    InvSendIoGoodsService.get().clear();
//                    goodsAdapter.setEntityList(InvSendIoGoodsService.get().queryAll());
//                    showProgressDialog(ProgressDialog.STATUS_DONE, "发货成功", true);
                    hideProgressDialog();
                    DialogUtil.showHint("发货成功");
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


    /**
     * 导入拣货单
     */
    private void fetchInvFindOrder() {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INVFINDORDER_INSPECT);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_INVFINDORDER_INSPECT);
    }

    /**
     * 导入拣货单
     */
    private void importInvFindOrder(final NetInfoWrapper netInfoWrapper) {
        if (netInfoWrapper == null) {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "加载数据...", false);

        mNetInfoWrapper = netInfoWrapper;
        List<InvSendIoGoodsEntity> entities = InvSendIoGoodsService.get().queryAll();

        // TODO: 6/8/16 获取商品批发价（拣货单商品没有批发价）
        retrieveGoodsPrice(entities);
    }

    /**
     * 获取商品价格*
     */
    private void retrieveGoodsPrice(List<InvSendIoGoodsEntity> entities) {
        StringBuilder sb = new StringBuilder();
        for (InvSendIoGoodsEntity wrapper : entities) {
            if (wrapper.getProSkuId() == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(wrapper.getProSkuId());
        }

        PageInfo pageInfo = new PageInfo(1, entities.size());

        NetCallBack.QueryRsCallBack retrievePriceRC = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                List<ScGoodsSku> scGoodsSkus = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        scGoodsSkus.add(wrapper.getBean());
                    }
                }

                //刷新商品价格
                InvSendIoGoodsService.get().infusePriceList(scGoodsSkus);
                goodsAdapter.setEntityList(InvSendIoGoodsService.get().queryAll());
                hideProgressDialog();
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);

                ZLogger.d("获取拣货单商品价格失败：" + errMsg);
                goodsAdapter.setEntityList(InvSendIoGoodsService.get().queryAll());
                hideProgressDialog();
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScChainGoodsSkuApiImpl.scChainGoodsSkuList(sb.toString(), retrievePriceRC);
    }

    /**
     * 验货
     */
    @OnClick(R.id.fab_add)
    public void inspect() {
        inspect(null);
    }

    /**
     * 拣货
     */
    private void inspect(String barcode) {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INVIO_PICK_GOODS);
        extras.putString(InvIoGoodsInspectFragment.EXTRA_KEY_BARCODE, barcode);
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

        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_COMPANYLIST);
        extras.putInt(CompanyListFragment.EXTRA_KEY_ABILITY_ITEM, AbilityItem.TENANT);
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_COMPANY_LIST);
    }

}
