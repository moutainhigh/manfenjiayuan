package com.manfenjiayuan.pda_supermarket.ui.fragment.receipt;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.DistributionSignService;
import com.manfenjiayuan.pda_supermarket.presenter.InvSendIoOrderPresenter;
import com.manfenjiayuan.pda_supermarket.ui.IInvSendIoOrderView;
import com.manfenjiayuan.pda_supermarket.ui.activity.SecondaryActivity;
import com.manfenjiayuan.pda_supermarket.ui.adapter.DistributionSignAdapter;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 采购收货－－批发商的发货单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ReceiveMSendOrderFragment extends BaseReceiveOrderFragment
        implements IInvSendIoOrderView {

    public static final String EXTRA_KEY_BARCODE = "barcode";

    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private DistributionSignAdapter officeAdapter;
    private ItemTouchHelper itemTouchHelper;

    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.button_sign)
    Button btnSign;
    @Bind(R.id.button_inspect)
    Button btnInspect;

    private InvSendIoOrderPresenter invSendIoOrderPresenter;

    private String barcode = null;
    private InvSendIoOrderItemBrief mInvSendIoOrderItemBrief = null;

    public static ReceiveMSendOrderFragment newInstance(Bundle args) {
        ReceiveMSendOrderFragment fragment = new ReceiveMSendOrderFragment();

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
        if (mInvSendIoOrderItemBrief != null){
            inspect(barcode);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_receive_m_sendorder;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        invSendIoOrderPresenter = new InvSendIoOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();

        Bundle args = getArguments();
        if (args != null) {
            barcode = args.getString(EXTRA_KEY_BARCODE);
        }

        //清空签收数据库
        DistributionSignService.get().clear();

        if (StringUtils.isEmpty(barcode)) {
            DialogUtil.showHint("订单条码无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            load(barcode);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //清空签收数据库
        DistributionSignService.get().clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_DISTRIBUTION_INSPECT: {
                btnInspect.setEnabled(true);
                officeAdapter.setEntityList(DistributionSignService.get().queryAll());
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onReceiveOrderSucceed(String orderId) {
        super.onReceiveOrderSucceed(orderId);
        btnSign.setEnabled(true);
        animProgress.setVisibility(View.GONE);
    }

    @Override
    public void onReceiveOrderInterrupted() {
        super.onReceiveOrderInterrupted();
        btnSign.setEnabled(true);
        animProgress.setVisibility(View.GONE);
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
        }
        else{
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        return isResponseBackPressed();
    }



    /**
     * 签收
     */
    @OnClick(R.id.button_sign)
    public void sign() {
        btnSign.setEnabled(false);

        if (mInvSendIoOrderItemBrief == null){
            DialogUtil.showHint("发货单无效，请退出重新扫描发货单！");
            btnSign.setEnabled(true);
            return;
        }


        showConfirmDialog("米西小贴士：请确认已经查验过所有商品。",
                "签收", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        animProgress.setVisibility(View.VISIBLE);
                        doSignWork(officeAdapter.getEntityList(), null,
                                mInvSendIoOrderItemBrief.getSendTenantId(), mInvSendIoOrderItemBrief.getIsPrivate());
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
        btnInspect.setEnabled(false);
        if (mInvSendIoOrderItemBrief == null){
            DialogUtil.showHint("发货单无效，请退出重新扫描发货单！");
            btnInspect.setEnabled(true);
            return;
        }
        inspect("");
    }

    private void inspect(String barcode) {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_DISTRIBUTION_INSPECT);
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
                DistributionSignEntity entity = officeAdapter.getEntityList().get(position);
                inspect(entity.getBarcode());
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

    private void load(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

//        if (barcode.startsWith("1")){
//            invSendOrderPresenter.loadOrderItemsByBarcode(barcode);
//        }
//        else if (barcode.startsWith("2")){
//            invSendIoOrderPresenter.loadOrderItemsByBarcode(barcode);
//        }
//        else {
//            DialogUtil.showHint("订单编号无效");
//        }
        invSendIoOrderPresenter.loadOrderItemsByBarcode(barcode);
    }

    @Override
    public void onQueryInvSendIoOrderProcess() {
        animProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onQueryInvSendIoOrderError(String errorMsg) {
        animProgress.setVisibility(View.GONE);
    }

    @Override
    public void onQueryInvSendIoOrderSuccess(InvSendIoOrderItemBrief data) {
        animProgress.setVisibility(View.GONE);

        mInvSendIoOrderItemBrief = data;
        if (data == null) {
            DistributionSignService.get().saveSendIoOrdersItems(null);
            officeAdapter.setEntityList(null);
//            btnInspect.setEnabled(false);
//            btnSign.setEnabled(false);
        }
        else{
            //现保存到数据库，再从数据库里读取
            DistributionSignService.get().saveSendIoOrdersItems(data.getItems());
            officeAdapter.setEntityList(DistributionSignService.get().queryAll());
//            btnInspect.setEnabled(true);
//            btnSign.setEnabled(true);
        }
    }
}
