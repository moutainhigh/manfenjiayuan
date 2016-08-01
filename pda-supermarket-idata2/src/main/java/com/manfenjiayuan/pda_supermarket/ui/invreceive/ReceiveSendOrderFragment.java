package com.manfenjiayuan.pda_supermarket.ui.invreceive;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.InvSendOrder;
import com.mfh.framework.api.invSendIoOrder.InvSendOrderItem;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.DistributionSignService;
import com.manfenjiayuan.pda_supermarket.ui.activity.SecondaryActivity;
import com.manfenjiayuan.pda_supermarket.ui.adapter.DistributionSignAdapter;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品配送－－签收页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ReceiveSendOrderFragment extends BaseReceiveOrderFragment implements IInvSendOrderView {

    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private DistributionSignAdapter officeAdapter;
    private ItemTouchHelper itemTouchHelper;

    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.button_sign)
    View btnSign;

    private InvSendOrder invSendOrder = null;
    private InvSendOrderPresenter invSendOrderPresenter;

    public static ReceiveSendOrderFragment newInstance(Bundle args) {
        ReceiveSendOrderFragment fragment = new ReceiveSendOrderFragment();

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
    protected void onScanCode(String code) {
//        eqvBarcode.requestFocus();
//
//        eqvBarcode.clear();
        inspect(code);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_distribution_sign;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        invSendOrderPresenter = new InvSendOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            invSendOrder = (InvSendOrder) args.getSerializable("sendOrder");
        }

        initRecyclerView();

//        //清空签收数据库
        DistributionSignService.get().clear();

        if (invSendOrder == null) {
            DialogUtil.showHint("订单无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            if (!NetWorkUtil.isConnect(getActivity())) {
                DialogUtil.showHint(R.string.toast_network_error);
                return;
            }

            invSendOrderPresenter.loadOrderItems(invSendOrder.getId());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        //清空签收数据库
        DistributionSignService.get().clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_DISTRIBUTION_INSPECT: {
                officeAdapter.setEntityList(DistributionSignService.get().queryAll());
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        return isResponseBackPressed();
    }

    @Override
    public void onReceiveOrderSucceed(String orderId) {
        super.onReceiveOrderSucceed(orderId);
        btnSign.setEnabled(true);
        animProgress.setVisibility(View.GONE);
    }

    @Override
    public void onReceiveOrderInterrupted(String message) {
        super.onReceiveOrderInterrupted(message);
        btnSign.setEnabled(true);
        animProgress.setVisibility(View.GONE);
    }

    @Override
    public void onOrderPaySucceed() {
//        super.onOrderPaySucceed();

        Intent data = new Intent();
        data.putExtra("orderId", invSendOrder.getId());
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public void onOrderPayInterrupted() {
//        super.onOrderPayInterrupted();

        Intent data = new Intent();
        data.putExtra("orderId", invSendOrder.getId());
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    /**
     * 签收
     */
    @OnClick(R.id.button_sign)
    public void sign() {
        btnSign.setEnabled(false);

        List<DistributionSignEntity> goodsList = officeAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            onReceiveOrderInterrupted("商品不能为空");
            return;
        }

        final JSONArray itemsArray = new JSONArray();
        Double amount = 0D;
        for (DistributionSignEntity goods : goodsList) {
            if (goods.getReceivePrice() == null){
                ZLogger.d("未设置价格不允许收货");
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());//查询供应链
            item.put("proSkuId", goods.getProSkuId());
            String productName = goods.getProductName();
            // TODO: 6/10/16  商品名字太长，后台不允许提交,这里增加一层过滤
            if (!StringUtils.isEmpty(productName) && productName.length() > 10){
                item.put("productName", productName.substring(0, 10));
            }
            else{
                item.put("productName", productName);
            }
            item.put("quantityCheck", goods.getReceiveQuantity());
            item.put("price", goods.getReceivePrice());
            item.put("amount", goods.getReceiveAmount());
            item.put("barcode", goods.getBarcode());
            item.put("providerId", goods.getProviderId());
            item.put("isPrivate", goods.getIsPrivate());//（0：不是 1：是）

            itemsArray.add(item);
            amount += goods.getReceiveAmount();
        }

        final Double finalAmount = amount;
        showConfirmDialog(String.format("总金额：%.2f,\n请确认已经查验过所有商品。", amount),
                "签收", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        animProgress.setVisibility(View.VISIBLE);

                        doSignWork(itemsArray, finalAmount, invSendOrder.getId(),
                                invSendOrder.getSendTenantId(), invSendOrder.getIsPrivate());

//                        doSignWork(officeAdapter.getEntityList(), invSendOrder.getId(),
//                                invSendOrder.getSendTenantId(), invSendOrder.getIsPrivate());
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
     * 验货
     */
    @OnClick(R.id.button_inspect)
    public void inspect() {
        inspect("");
    }

    private void inspect(String barcode) {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE,
                SecondaryActivity.FRAGMENT_TYPE_DISTRIBUTION_INSPECT);
        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_DISTRIBUTION_INSPECT);
    }

    private CommonDialog operateDialog = null;

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

        officeAdapter = new DistributionSignAdapter(getActivity(), null);
        officeAdapter.setOnAdapterListener(new DistributionSignAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                DistributionSignEntity entity = officeAdapter.getEntity(position);
                if (entity != null) {
                    inspect(entity.getBarcode());
                }
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                final DistributionSignEntity entity = officeAdapter.getEntityList().get(position);
                if (operateDialog == null) {
                    operateDialog = new CommonDialog(getActivity());
                    operateDialog.setCancelable(true);
                }
                operateDialog.setMessage(String.format("%d, %s\n%s", position, entity.getBarcode(), entity.getProductName()));
                operateDialog.setPositiveButton("拒收", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        DistributionSignService.get().reject(entity);

                        officeAdapter.notifyItemChanged(position);
                    }
                });
                operateDialog.setNegativeButton("删除", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        officeAdapter.removeEntity(position);
                    }
                });
                if (!operateDialog.isShowing()) {
                    operateDialog.show();
                }

            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
                animProgress.setVisibility(View.GONE);
            }
        });

        addressRecyclerView.setAdapter(officeAdapter);


        ItemTouchHelper.Callback callback = new MyItemTouchHelper(officeAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(addressRecyclerView);
    }


    @Override
    public void onQueryInvSendOrderProcess() {
        animProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onQueryInvSendOrderError(String errorMsg) {
        animProgress.setVisibility(View.GONE);
        DistributionSignService.get().saveSendOrderItems(null);
    }

    @Override
    public void onQueryInvSendOrderSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {
        animProgress.setVisibility(View.GONE);
    }

    @Override
    public void onQueryInvSendOrderItemsSuccess(List<InvSendOrderItem> dataList) {
        try {
            ZLogger.d("加载订单明细成功");
            DistributionSignService.get().saveSendOrderItems(dataList);
            officeAdapter.setEntityList(DistributionSignService.get().queryAll());
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

        animProgress.setVisibility(View.GONE);
    }
}
