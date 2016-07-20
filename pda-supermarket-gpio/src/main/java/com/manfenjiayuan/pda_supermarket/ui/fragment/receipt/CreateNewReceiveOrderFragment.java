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

import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.DistributionSignService;
import com.manfenjiayuan.pda_supermarket.ui.activity.SecondaryActivity;
import com.manfenjiayuan.pda_supermarket.ui.adapter.DistributionSignAdapter;
import com.manfenjiayuan.pda_supermarket.ui.dialog.SelectWholesalerDialog;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditQueryView;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品配送－－签收页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateNewReceiveOrderFragment extends BaseReceiveOrderFragment {

    @Bind(R.id.providerView)
    NaviAddressView mProviderView;
    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private DistributionSignAdapter officeAdapter;
    private ItemTouchHelper itemTouchHelper;

    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.button_sign)
    View btnSign;

    private SelectWholesalerDialog selectPlatformProviderDialog = null;

    /*供应商*/
    private CompanyInfo companyInfo = null;//当前私有供应商

    public static CreateNewReceiveOrderFragment newInstance(Bundle args) {
        CreateNewReceiveOrderFragment fragment = new CreateNewReceiveOrderFragment();

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
        return R.layout.fragment_create_inv_receiveorder;
    }

    @Override
    protected void onScanCode(String code) {
//        eqvBarcode.setInputString(code);
        eqvBarcode.requestFocus();
        eqvBarcode.clear();
        inspect(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //清空签收数据库
        DistributionSignService.get().clear();
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();

//        Bundle args = getArguments();
//        if (args != null) {
////            invSendOrder = (InvSendOrder)args.getSerializable("sendOrder");
//        }
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
    }

    @Override
    public void onResume() {
        super.onResume();

        if (companyInfo == null) {
            selectInvCompProvider();
        } else {
            eqvBarcode.requestFocus();
            eqvBarcode.clear();
        }
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


    /**
     * 切换发货方
     */
    private void changeSendCompany(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
//        this.mLabelProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");
        this.mProviderView.setText(companyInfo != null ? companyInfo.getName() : "");

//        officeAdapter.setEntityList(null);//清空商品
//        DistributionSignService.get().clear();
    }

    @Override
    public void onReceiveOrderSucceed(String orderId) {
        super.onReceiveOrderSucceed(orderId);
        btnSign.setEnabled(true);
    }

    @Override
    public void onReceiveOrderInterrupted() {
        super.onReceiveOrderInterrupted();
        btnSign.setEnabled(true);
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
            selectInvCompProvider();
        } else {
            showConfirmDialog("米西小贴士：请确认已经查验过所有商品。",
                    "签收", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            doSignWork(officeAdapter.getEntityList(), null,
                                    companyInfo.getTenantId(), IsPrivate.PLATFORM);
                        }
                    }, "点错了", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            btnSign.setEnabled(true);
                        }
                    });
        }
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
                operateDialog.setMessage(String.format("%s\n%s", entity.getBarcode(), entity.getProductName()));
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
                eqvBarcode.requestFocus();
                eqvBarcode.clear();
            }
        });

        addressRecyclerView.setAdapter(officeAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(officeAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(addressRecyclerView);
    }

    /**
     * 选择批发商
     */
    @OnClick(R.id.providerView)
    public void selectInvCompProvider() {
        if (selectPlatformProviderDialog == null) {
            selectPlatformProviderDialog = new SelectWholesalerDialog(getActivity());
            selectPlatformProviderDialog.setCancelable(true);
            selectPlatformProviderDialog.setCanceledOnTouchOutside(false);
        }
        selectPlatformProviderDialog.init(null, new SelectWholesalerDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                changeSendCompany(companyInfo);
            }

        });
        selectPlatformProviderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                eqvBarcode.requestFocusEnd();
            }
        });
        if (!selectPlatformProviderDialog.isShowing()) {
            selectPlatformProviderDialog.show();
        }
    }


}
