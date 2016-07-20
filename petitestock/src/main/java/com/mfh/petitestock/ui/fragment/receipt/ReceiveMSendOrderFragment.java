package com.mfh.petitestock.ui.fragment.receipt;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.manfenjiayuan.business.bean.InvSendOrder;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.MfhModule;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.petitestock.Constants;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.entity.DistributionSignEntity;
import com.mfh.petitestock.database.logic.DistributionSignService;
import com.mfh.petitestock.presenter.InvSendIoOrderPresenter;
import com.mfh.petitestock.ui.IInvSendIoOrderView;
import com.mfh.petitestock.ui.SecondaryActivity;
import com.mfh.petitestock.ui.adapter.DistributionSignAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 采购收货－－批发商的发货单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ReceiveMSendOrderFragment extends BaseReceiveOrderFragment
        implements IInvSendOrderView, IInvSendIoOrderView {

    public static final String EXTRA_KEY_BARCODE = "barcode";

    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private DistributionSignAdapter officeAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.button_sign)
    View btnSign;

    private InvSendOrderPresenter invSendOrderPresenter;
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
    protected void onScanCode(String code) {

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_receive_m_sendorder;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        invSendOrderPresenter = new InvSendOrderPresenter(this);
        invSendIoOrderPresenter = new InvSendIoOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();

        Bundle args = getArguments();
        if (args != null) {
            barcode = args.getString(EXTRA_KEY_BARCODE);
        }

        if (StringUtils.isEmpty(barcode)) {
            DialogUtil.showHint("订单条码无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            load(barcode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        hideProgressDialog();

        if (MfhUserManager.getInstance().containsModule(MfhModule.SUPM_MANAGER)) {
            doPayWork(orderId, totalAmount);
        } else {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }

    @Override
    public void onReceiveOrderInterrupted() {
        super.onReceiveOrderInterrupted();
        btnSign.setEnabled(true);
        animProgress.setVisibility(View.GONE);
        hideProgressDialog();
    }

    /**
     * 签收
     */
    @OnClick(R.id.button_sign)
    public void sign() {
        btnSign.setEnabled(false);

        if (mInvSendIoOrderItemBrief == null) {
            DialogUtil.showHint("订单无效");
            btnSign.setEnabled(true);
            return;
        }

        animProgress.setVisibility(View.VISIBLE);
        doSignWork(officeAdapter.getEntityList(), null,
                mInvSendIoOrderItemBrief.getSendTenantId(), mInvSendIoOrderItemBrief.getIsPrivate());
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
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_DISTRIBUTION_INSPECT);
        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_DISTRIBUTION_INSPECT);
    }

    private CommonDialog operateDialog = null;

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
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
                operateDialog.setNegativeButton("检查", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        inspect(entity.getBarcode());
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
    }

    private void load(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        //清空签收数据库
        DistributionSignService.get().clear();
        officeAdapter.setEntityList(null);

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
    public void onQueryInvSendOrderProcess() {
        animProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onQueryInvSendOrderError(String errorMsg) {

        animProgress.setVisibility(View.GONE);
    }

    @Override
    public void onQueryInvSendOrderSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {

    }

    @Override
    public void onQueryInvSendOrderItemsSuccess(List<InvSendOrderItem> dataList) {
        if (dataList != null) {
            //现保存到数据库，再从数据库里读取
            DistributionSignService.get().save(dataList);
            officeAdapter.setEntityList(DistributionSignService.get().queryAll());
        }

        animProgress.setVisibility(View.GONE);
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
        refresh(data);
    }

    private void refresh(InvSendIoOrderItemBrief data) {
        mInvSendIoOrderItemBrief = data;
        if (data == null) {
            //清空签收数据库
            DistributionSignService.get().clear();
            officeAdapter.setEntityList(null);
            return;
        }

        //现保存到数据库，再从数据库里读取
        DistributionSignService.get().saveSendIoOrders(data.getItems());
        officeAdapter.setEntityList(DistributionSignService.get().queryAll());

    }
}
