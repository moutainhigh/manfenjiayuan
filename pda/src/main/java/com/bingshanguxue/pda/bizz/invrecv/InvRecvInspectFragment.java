package com.bingshanguxue.pda.bizz.invrecv;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.database.entity.InvRecvGoodsEntity;
import com.bingshanguxue.pda.database.service.InvRecvGoodsService;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 商品配送－－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvRecvInspectFragment extends PDAScanFragment implements IChainGoodsSkuView {

    public static final String EXTRA_KEY_BARCODE = "barcode";

//    @BindView(R2.id.toolbar)
     Toolbar mToolbar;
//    @BindView(R2.id.scanBar)
     ScanBar mScanBar;

//    @BindView(R2.id.label_barcode)
    TextLabelView labelBarcode;
//    @BindView(R2.id.label_productName)
    TextLabelView labelProductName;
//    @BindView(R2.id.label_send_price)
    TextLabelView labelSendPrice;
//    @BindView(R2.id.label_send_quantity)
    TextLabelView labelSendQuantity;
//    @BindView(R2.id.label_receive_quantity)
    EditLabelView labelReceiveQuantity;
//    @BindView(R2.id.label_receive_amount)
    EditLabelView labelReceiveAmount;
//    @BindView(R2.id.label_receive_price)
    TextLabelView labelReceivePrice;


//    @BindView(R2.id.fab_submit)
    public FloatingActionButton btnSubmit;

    private InvRecvGoodsEntity curGoods = null;
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;


    public static InvRecvInspectFragment newInstance(Bundle args) {
        InvRecvInspectFragment fragment = new InvRecvInspectFragment();

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
        return R.layout.fragment_inspect_invrecv_goods;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mScanBar = (ScanBar) rootView.findViewById(R.id.scanBar);
        labelBarcode = (TextLabelView) rootView.findViewById(R.id.label_barcode);
        labelProductName = (TextLabelView) rootView.findViewById(R.id.label_productName);
        labelSendPrice = (TextLabelView) rootView.findViewById(R.id.label_send_price);
        labelSendQuantity = (TextLabelView) rootView.findViewById(R.id.label_send_quantity);
        labelReceiveQuantity = (EditLabelView) rootView.findViewById(R.id.label_receive_quantity);
        labelReceiveAmount = (EditLabelView) rootView.findViewById(R.id.label_receive_amount);
        labelReceivePrice = (TextLabelView) rootView.findViewById(R.id.label_receive_price);
         btnSubmit = (FloatingActionButton) rootView.findViewById(R.id.fab_submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
            mToolbar.setNavigationOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().onBackPressed();
                        }
                    });
        } else {
            ZLogger.d("mToolbar is null");
        }

        if (mScanBar != null) {
            mScanBar.setSoftKeyboardEnabled(true);
            mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }

                @Override
                public void onAction1Click(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }
            });
        } else {
            ZLogger.d("mScanBar is null");
        }

//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelReceiveQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                labelReceiveAmount.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
        labelReceiveQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                labelReceivePrice.setTvSubTitle(MUtils.formatDouble(calculateReceivePrice(), ""));

            }
        });
//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelReceiveAmount.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                submit();
            }

            @Override
            public void onScan() {
                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
        labelReceiveAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                labelReceivePrice.setTvSubTitle(MUtils.formatDouble(calculateReceivePrice(), ""));

            }
        });

        Bundle args = getArguments();
        if (args != null) {
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

            if (!StringUtils.isEmpty(barcode)){
                queryByBarcode(barcode);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
//        if (curGoods != null){
//            // TODO: 6/10/16 删除当前商品信息
//        }
//
//        showConfirmDialog("退出后商品列表将会清空，确定要退出吗？",
//                "退出", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                        getActivity().setResult(Activity.RESULT_CANCELED);
//                        getActivity().finish();
//                    }
//                }, "点错了", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//
////        DialogUtil.showHint("onBackPressed");
//        if (officeAdapter.getItemCount() > 0) {
//
//        } else {
//            getActivity().setResult(Activity.RESULT_CANCELED);
//            getActivity().finish();
//        }

        return false;
    }


    private Double calculateReceivePrice() {
        String quantityStr = labelReceiveQuantity.getInput();
        if (StringUtils.isEmpty(quantityStr)) {
            return 0D;
        }

        String amount = labelReceiveAmount.getInput();
        if (StringUtils.isEmpty(amount)) {
            return 0D;
        }

        Double quantityVal = Double.valueOf(quantityStr);
        Double amountVal = Double.valueOf(amount);
        if (quantityVal == 0) {
            return 0D;
        } else {
            return amountVal / quantityVal;
        }
    }

    public void onQueryError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)){
            ZLogger.df(errorMsg);
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        }
        else{
            hideProgressDialog();
        }
        isAcceptBarcodeEnabled = true;

        refreshPackage(null);
    }

//    @OnClick(R2.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;

        onSubmitProcess();

        String quantityStr = labelReceiveQuantity.getInput();
        if (StringUtils.isEmpty(quantityStr)) {
            onSubmitError("请输入签收数量");
            return;
        }

        String amount = labelReceiveAmount.getInput();
        if (StringUtils.isEmpty(amount)) {
            onSubmitError("请输入收货金额");
            return;
        }

        Double quantityCheck = Double.valueOf(quantityStr);
        if (curGoods != null && curGoods.getReceiveQuantity() > 0) {
            quantityCheckConfirmDialog(curGoods, Double.valueOf(amount), quantityCheck);
        } else {
            InvRecvGoodsService.get().inspect(curGoods, Double.valueOf(amount), quantityCheck);

            onSubmitSuccess();
        }
    }

    /**
     * 提交处理中
     */
    public void onSubmitProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    /**
     * 提交失败
     */
    public void onSubmitError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)) {
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
            ZLogger.df(errorMsg);
        } else {
            hideProgressDialog();
        }
        isAcceptBarcodeEnabled = true;
        btnSubmit.setEnabled(true);
    }

    /**
     * 提交成功
     */
    public void onSubmitSuccess() {
//        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
        hideProgressDialog();

        refreshPackage(null);
    }


    /**
     * 刷新信息
     */
    private void refreshPackage(InvRecvGoodsEntity goods) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = goods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelSendQuantity.setTvSubTitle("");
            labelSendPrice.setTvSubTitle("");
            labelReceiveQuantity.setInputAndEnd("", "");
            labelReceiveAmount.setInput("");
            labelReceivePrice.setTvSubTitle("");

            btnSubmit.setEnabled(false);

        } else {
            ZLogger.d(JSONObject.toJSONString(curGoods));
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getProductName());
            labelSendQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getSendQuantity(), ""));
//            labelSignQuantity.setEtContent(String.format("%.2f", curGoods.getSignQuantity()));
            labelSendPrice.setTvSubTitle(MUtils.formatDouble(curGoods.getSendPrice(), ""));
            //默认签收数量为空，根据实际情况填写
            labelReceiveQuantity.setInputAndEnd("", curGoods.getUnitSpec());
//            labelReceiveAmount.setEndText(curGoods.getUnitSpec());
            labelReceiveAmount.setInput("");
            labelReceivePrice.setTvSubTitle(MUtils.formatDouble(calculateReceivePrice(), ""));

            btnSubmit.setEnabled(true);

            labelReceiveQuantity.requestFocus();
        }

//        DeviceUtils.hideSoftInput(getActivity(), labelReceiveQuantity);
    }

    @Override
    public void onChainGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }
    @Override
    public void onChainGoodsSkuViewError(String errorMsg) {
        hideProgressDialog();
        refreshPackage(null);
        isAcceptBarcodeEnabled = true;
    }

    @Override
    public void onChainGoodsSkuViewSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        hideProgressDialog();
        isAcceptBarcodeEnabled = true;

        if (dataList != null && dataList.size() > 0) {
            saveChainGoodsSku(dataList.get(0));
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    @Override
    public void onChainGoodsSkuViewSuccess(ChainGoodsSku data) {
        hideProgressDialog();
        isAcceptBarcodeEnabled = true;

        saveChainGoodsSku(data);
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;
        mScanBar.reset();
        queryByBarcode(code);
    }

    /**
     * 搜索条码
     */
    public void queryByBarcode(final String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            onQueryError("请先扫描商品条码");
            return;
        }

        onQueryProcess();

        Observable.create(new Observable.OnSubscribe<InvRecvGoodsEntity>() {
            @Override
            public void call(Subscriber<? super InvRecvGoodsEntity> subscriber) {
                InvRecvGoodsEntity goodsEntity = InvRecvGoodsService.get().queryEntityByBarcode(barcode);
                subscriber.onNext(goodsEntity);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<InvRecvGoodsEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(InvRecvGoodsEntity invRecvGoodsEntity) {
                        if (invRecvGoodsEntity != null) {
                            saveDistributionSignEntity(invRecvGoodsEntity);
                        } else {
                            queryNetGoods(barcode);
                        }
                    }

                });
    }

    private void queryNetGoods(String barcode) {
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

        chainGoodsSkuPresenter.getTenantSkuMust(null, barcode);
    }

    /**
     * 查询处理中
     */
    public void onQueryProcess() {
        isAcceptBarcodeEnabled = false;
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    /**
     * 查询成功
     */
    public void onQuerySuccess() {
//        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
        hideProgressDialog();
        isAcceptBarcodeEnabled = true;
    }

    public void saveDistributionSignEntity(InvRecvGoodsEntity goods) {
        hideProgressDialog();
        refreshPackage(goods);
    }


    /**
     * 保存搜索商品
     */
    private void saveChainGoodsSku(ChainGoodsSku goods) {
        if (goods == null) {
            DialogUtil.showHint("未找到商品");
            return;
        }

//        if (goods.getSingleCostPrice() == null) {
//            //“如果singleCostPrice值为null，说明缺少箱规数，信息不完整，这种情况你不允许进行采购或收货
//            DialogUtil.showHint("商品未设置单件批发价，无法采购货收货");
//            return;
//        }

        InvRecvGoodsEntity entity = InvRecvGoodsService.get().queryEntityByBarcode(goods.getBarcode());
        if (entity == null) {
            entity = new InvRecvGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

            entity.setProSkuId(goods.getProSkuId());
            entity.setChainSkuId(goods.getId());
            entity.setProductName(goods.getSkuName());
            entity.setUnitSpec(goods.getUnit());
            entity.setBarcode(goods.getBarcode());
            entity.setProviderId(goods.getTenantId());
            entity.setIsPrivate(IsPrivate.PLATFORM);

            entity.setSendPrice(goods.getSingleCostPrice());
            entity.setSendAmount(0D);
            entity.setReceiveQuantity(0D);
            entity.setReceiveAmount(0D);
            entity.setInspectStatus(InvRecvGoodsEntity.INSPECT_STATUS_NONE);
            entity.setUpdatedDate(new Date());


//            InvRecvGoodsService.get().saveOrUpdate(entity);
        }
        refreshPackage(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;

    private void quantityCheckConfirmDialog(final InvRecvGoodsEntity entity,
                                            final Double amount, final Double quantity) {
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(false);
            quantityCheckConfirmDialog.setCanceledOnTouchOutside(false);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经签收%.2f件，请选择[覆盖]还是[累加]",
                entity.getReceiveQuantity()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvRecvGoodsService.get().inspect(entity, amount, quantity);

                onSubmitSuccess();
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvRecvGoodsService.get().inspect(entity,
                        amount + entity.getReceiveAmount(),
                        entity.getReceiveQuantity() + quantity);

                onSubmitSuccess();
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
