package com.manfenjiayuan.pda_supermarket.ui.store;

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
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.pda.bizz.invreturn.InvReturnGoodsInspectFragment;
import com.bingshanguxue.pda.bizz.invreturn.InvReturnOrderGoodsAdapter;
import com.bingshanguxue.pda.database.entity.InvReturnGoodsEntity;
import com.bingshanguxue.pda.database.service.InvReturnGoodsService;
import com.bingshanguxue.vector_uikit.widget.NaviAddressView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.common.SecondaryActivity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 新建退货单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateInvReturnOrderFragment extends PDAScanFragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.providerView)
    NaviAddressView mProviderView;
    @BindView(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private InvReturnOrderGoodsAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;

    @BindView(R.id.empty_view)
    View emptyView;

    /*供应商*/
    private CompanyInfo companyInfo = null;//当前私有供应商

    public static CreateInvReturnOrderFragment newInstance(Bundle args) {
        CreateInvReturnOrderFragment fragment = new CreateInvReturnOrderFragment();

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
        return R.layout.fragment_create_inv_returnorder;
    }

    @Override
    protected void onScanCode(String code) {
        inspect(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        //清空签收数据库
        InvReturnGoodsService.get().clear();
//        chainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
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
        mToolbar.inflateMenu(R.menu.menu_inv_io);

        mProviderView.setEnabled(false);
        initRecyclerView();

        if (companyInfo == null) {
            selectInvCompProvider();
        }
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
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        return isResponseBackPressed();
    }

    /**
     * 切换发货方
     */
    private void changeSendCompany(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
//        this.mLabelProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");
        this.mProviderView.setText(companyInfo != null ? companyInfo.getName() : "");

        goodsAdapter.setEntityList(null);//清空商品
    }

    /**
     * 签收
     */
    public void submit() {
        final List<InvReturnGoodsEntity> goodsList = goodsAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            DialogUtil.showHint("您还没有添加商品");
            hideProgressDialog();
            return;
        }

        if (companyInfo == null) {
            hideProgressDialog();
            selectInvCompProvider();
            return;
        }

        showConfirmDialog("确定要提交退货单吗？",
                "退货", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        doSubmitStuff(goodsList);
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

    }

    private void doSubmitStuff(List<InvReturnGoodsEntity> goodsList) {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            hideProgressDialog();
            return;
        }

        JSONObject jsonStrObject = new JSONObject();
//        jsonStrObject.put("sendNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("sendTenantId", MfhLoginService.get().getSpid());
        jsonStrObject.put("receiveStoreType", 1);
        jsonStrObject.put("tenantId", companyInfo.getTenantId());
        jsonStrObject.put("remark", "");

        JSONArray itemsArray = new JSONArray();
        for (InvReturnGoodsEntity goods : goodsList) {
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

        InvSendIoOrderApiImpl.createBackOrder(null, true,
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

//                    DataSyncManagerImpl.get().sync(DataSyncManagerImpl.SYNC_STEP_PRODUCTS);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        addressRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        addressRecyclerView.setHasFixedSize(true);
        //添加分割线
//        addressRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        addressRecyclerView.setEmptyView(emptyView);

        goodsAdapter = new InvReturnOrderGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new InvReturnOrderGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                CreateOrderItemWrapper entity = goodsAdapter.getEntity(position);
//                inspect(entity.getBarcode());
//                changeQuantityCheck();
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
            }
        });

        addressRecyclerView.setAdapter(goodsAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(goodsAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(addressRecyclerView);
    }

    @OnClick(R.id.fab_add)
    public void inspect() {
        inspect(null);
    }

    private void inspect(String barcode) {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INVRETURN_INSPECTGOODS);
        extras.putString(InvReturnGoodsInspectFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_DISTRIBUTION_INSPECT);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_DISTRIBUTION_INSPECT: {
                goodsAdapter.setEntityList(InvReturnGoodsService.get().queryAll());
            }
            break;
            case ARCode.ARC_INVCOMPANY_LIST: {
                if (resultCode == Activity.RESULT_OK) {
                    CompanyInfo companyInfo = (CompanyInfo) data.getSerializableExtra("companyInfo");
                    if (companyInfo != null) {
                        changeSendCompany(companyInfo);
                    }
                }

                if (companyInfo == null) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 选择批发商
     */
    @OnClick(R.id.providerView)
    public void selectInvCompProvider() {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INV_COMPANYLIST);
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_INVCOMPANY_LIST);
    }


}
