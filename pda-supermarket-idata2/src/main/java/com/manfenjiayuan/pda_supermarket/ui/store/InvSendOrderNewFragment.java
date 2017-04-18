package com.manfenjiayuan.pda_supermarket.ui.store;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.pda.bizz.invsend.InvSendGoodsAdapter;
import com.manfenjiayuan.business.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.manfenjiayuan.business.intelligent.IIntelligentPurchaseView;
import com.manfenjiayuan.business.intelligent.IntelligentPurchasePresenter;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.common.SecondaryActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.invOrder.InvOrderApi;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.InvSendOrderHttpManager;
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


/**
 * 采购（智能订货）
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvSendOrderNewFragment extends BaseFragment implements IIntelligentPurchaseView {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.office_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private InvSendGoodsAdapter goodsAdapter;
    @BindView(R.id.empty_view)
    View emptyView;

    /**
     * 批发商
     */
    private CompanyInfo mCompanyInfo = null;
    private IntelligentPurchasePresenter mIntelligentPurchasePresenter;


    public static InvSendOrderNewFragment newInstance(Bundle args) {
        InvSendOrderNewFragment fragment = new InvSendOrderNewFragment();

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
        return R.layout.fragment_inv_sendorder_new;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntelligentPurchasePresenter = new IntelligentPurchasePresenter(this);

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

        mToolbar.setTitle("智能订货");
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
//                DialogUtil.showHint("选择批发商");
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

        mIntelligentPurchasePresenter.autoAskSendOrder(mCompanyInfo.getId());
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

    @Override
    public void onIntelligentPurchaseProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在为您智能订货...", false);
    }

    @Override
    public void onIntelligentPurchaseError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
    }

    @Override
    public void onIntelligentPurchaseSuccess(List<InvSendOrderItem> dataList) {
        List<PurchaseShopcartGoodsWrapper> goodsWrappers = new ArrayList<>();
        if (dataList != null && dataList.size() > 0) {
            for (InvSendOrderItem data : dataList) {
                PurchaseShopcartGoodsWrapper wrapper = PurchaseShopcartGoodsWrapper
                        .fromIntelligentOrderItem(data, mCompanyInfo,
                                IsPrivate.PLATFORM);
                goodsWrappers.add(wrapper);
            }
        }
        goodsAdapter.setEntityList(goodsWrappers);
//        showProgressDialog(ProgressDialog.STATUS_DONE, "智能订货完成", true);
        hideProgressDialog();

    }


    /**
     * 提交采购订单
     */
    @OnClick(R.id.fab_submit)
    public void sbumit() {
        if (mCompanyInfo == null) {
            onReceiveOrderInterrupted("请选择批发商！");
            selectInvCompProvider();
            return;
        }

        List<PurchaseShopcartGoodsWrapper> goodsWrappers = goodsAdapter.getEntityList();
        if (goodsWrappers == null || goodsWrappers.size() < 1) {
            onReceiveOrderInterrupted("商品不能为空");
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);

        JSONObject jsonObject = new JSONObject();
        JSONArray items = new JSONArray();

        //status的值不要传了，后台自动判断（因为生鲜类要求是0，其他类是1）
//        jsonObject.put("status", OrderStatus.STATUS_CONFIRM);
        jsonObject.put("bizType", InvOrderApi.BIZTYPE_PURCHASE);
        jsonObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonObject.put("contact", MfhLoginService.get().getHumanName());
        jsonObject.put("receiveMobile", MfhLoginService.get().getTelephone());
        //TODO,这里需要填写地址，暂时使用网点名称
        jsonObject.put("receiveAddr", MfhLoginService.get().getCurOfficeName());

        for (PurchaseShopcartGoodsWrapper goods : goodsWrappers) {
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());
            item.put("proSkuId", goods.getProSkuId());
            item.put("providerId", goods.getSupplyId());
            item.put("isPrivate", goods.getIsPrivate());
            item.put("productName", goods.getProductName());
            item.put("askTotalCount", goods.getQuantityCheck());
//                        item.put("totalCount", goods.getQuantityCheck());
            item.put("price", goods.getBuyPrice());
            item.put("amount", MathCompact.mult(goods.getQuantityCheck(), goods.getBuyPrice()));
            item.put("barcode", goods.getBarcode());
            if (goods.getIsPrivate().equals(IsPrivate.PRIVATE)) {
                item.put("controlType", "1");
            } else if (goods.getIsPrivate().equals(IsPrivate.UNIFORM)) {
                item.put("controlType", "0");
            }

            items.add(item);
        }
        jsonObject.put("items", items);

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonObject.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSendOrderHttpManager.getInstance().askSendOrder(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.df("采购订单创建失败: " + e.toString());
                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        ZLogger.df("新建采购订单成功,清空购物车..." + data);
                        showProgressDialog(ProgressDialog.STATUS_DONE);

                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }

                });
    }

    /**
     * 支付中断，取消&失败
     */
    public void onReceiveOrderInterrupted(String message) {
        DialogUtil.showHint(message);
        hideProgressDialog();
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

        goodsAdapter = new InvSendGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new InvSendGoodsAdapter.OnAdapterListener() {
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
