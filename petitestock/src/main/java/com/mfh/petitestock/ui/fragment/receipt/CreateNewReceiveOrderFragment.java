package com.mfh.petitestock.ui.fragment.receipt;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.mvp.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.mvp.view.IChainGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.compound.OptionalLabel;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.petitestock.Constants;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.entity.DistributionSignEntity;
import com.mfh.petitestock.database.logic.DistributionSignService;
import com.mfh.petitestock.ui.SecondaryActivity;
import com.mfh.petitestock.ui.adapter.DistributionSignAdapter;
import com.mfh.petitestock.ui.dialog.SelectWholesalerDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品配送－－签收页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateNewReceiveOrderFragment extends BaseReceiveOrderFragment
        implements IChainGoodsSkuView {

    @Bind(R.id.label_provider)
    OptionalLabel mLabelProvider;
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


    private SelectWholesalerDialog selectPlatformProviderDialog = null;

    /*供应商*/
    private CompanyInfo companyInfo = null;//当前私有供应商
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;
    private Double totalAmount = 0D;

    public static CreateNewReceiveOrderFragment newInstance(Bundle args) {
        CreateNewReceiveOrderFragment fragment = new CreateNewReceiveOrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_create_invreceiveorder;
    }

    @Override
    protected void onScanCode(String code) {
        load(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();

//        Bundle args = getArguments();
//        if (args != null) {
////            invSendOrder = (InvSendOrder)args.getSerializable("sendOrder");
//        }

        mLabelProvider.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                changeSendCompany(null);
            }
        });
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

    /**
     * 切换发货方
     */
    private void changeSendCompany(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
        this.mLabelProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");

        officeAdapter.setEntityList(null);//清空商品
        DistributionSignService.get().clear();
    }

    @Override
    public void onReceiveOrderSucceed(String orderId) {
        super.onReceiveOrderSucceed(orderId);
        btnSign.setEnabled(true);
        animProgress.setVisibility(View.GONE);
        hideProgressDialog();

        doPayWork(orderId, totalAmount);
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

        if (companyInfo == null) {
            DialogUtil.showHint("请选择发货方！");
            btnSign.setEnabled(true);
            hideProgressDialog();
            return;
        }

        animProgress.setVisibility(View.VISIBLE);
        doSignWork(officeAdapter.getEntityList(), companyInfo.getId(),
                companyInfo.getTenantId(), IsPrivate.PLATFORM);
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
                Double amount = 0D;
                List<DistributionSignEntity> entityList = officeAdapter.getEntityList();
                if (entityList != null && entityList.size() > 0) {
                    for (DistributionSignEntity entity : entityList) {
                        amount += entity.getPrice() * entity.getQuantityCheck();
                    }
                }
            }
        });

        addressRecyclerView.setAdapter(officeAdapter);
    }

    private void load(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        if (companyInfo == null) {
            DialogUtil.showHint("请先选择发货方");
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        chainGoodsSkuPresenter.loadSupplyGoods(new PageInfo(-1, 10),
                companyInfo.getId(), barcode);
    }

    @Override
    public void onProcess() {

        animProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(String errorMsg) {

        animProgress.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        animProgress.setVisibility(View.GONE);
        if (dataList != null && dataList.size() > 0) {
            ChainGoodsSku chainGoodsSku = dataList.get(0);
            if (chainGoodsSku == null) {
                DialogUtil.showHint("商品无效");
                return;
            }
            if (chainGoodsSku.getSingleCostPrice() == null) {
                //“如果singleCostPrice值为null，说明缺少箱规数，信息不完整，这种情况你不允许进行采购或收货
                DialogUtil.showHint("商品未设置单件批发价，无法采购货收货");
            } else {
                //现保存到数据库，再从数据库里读取
                DistributionSignService.get().save(chainGoodsSku);
                officeAdapter.setEntityList(DistributionSignService.get().queryAll());
            }
        } else {
            DialogUtil.showHint("未找到商品");
        }

    }

    /**
     * 选择批发商
     */
    @OnClick(R.id.label_provider)
    public void selectInvCompProvider() {
//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_SELECT_WHOLESALER_TENANT);
//        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_NORMAL);
//        extras.putString(SimpleDialogActivity.EXTRA_KEY_TITLE, "选择发货方");
////        SimpleDialogActivity.actionStart(getActivity(), extras);
//
//        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
//        intent.putExtras(extras);
//        startActivityForResult(intent, Constants.ARC_SELECT_WHOLESALER_TENANT);
        if (selectPlatformProviderDialog == null) {
            selectPlatformProviderDialog = new SelectWholesalerDialog(getActivity());
            selectPlatformProviderDialog.setCancelable(true);
            selectPlatformProviderDialog.setCanceledOnTouchOutside(false);
        }
        selectPlatformProviderDialog.init(new SelectWholesalerDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                changeSendCompany(companyInfo);
            }

        });
        if (!selectPlatformProviderDialog.isShowing()) {
            selectPlatformProviderDialog.show();
        }
    }


}
