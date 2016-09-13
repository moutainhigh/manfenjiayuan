package com.bingshanguxue.pda.bizz.invrecv;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatCheckBox;
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
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.presenter.ScProductPricePresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.manfenjiayuan.business.view.IScProcuctPriceView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.anon.ProductSku;
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
public class InvRecvInspectFragment extends PDAScanFragment
        implements IScProcuctPriceView, IChainGoodsSkuView {

    public static final String EXTRA_KEY_BARCODE = "barcode";
    public static final String EXTRA_KEY_TENANTID = "tenantId";

    //    @BindView(R2.id.toolbar)
    Toolbar mToolbar;
    //    @BindView(R2.id.scanBar)
    ScanBar mScanBar;
    private AppCompatCheckBox queryCheckbox;

    //    @BindView(R2.id.label_barcode)
    TextLabelView labelBarcode;
    //    @BindView(R2.id.label_productName)
    TextLabelView labelProductName;
    //    @BindView(R2.id.label_send_price)
    TextLabelView labelSendPrice;
    //    @BindView(R2.id.label_send_quantity)
    TextLabelView labelSendQuantity;
    TextLabelView labelSingleCostPrice;
    TextLabelView labelHintPrice;
    //    @BindView(R2.id.label_receive_quantity)
    EditLabelView labelReceiveQuantity;
    //    @BindView(R2.id.label_receive_amount)
    EditLabelView labelReceiveAmount;
    //    @BindView(R2.id.label_receive_price)
    TextLabelView labelReceivePrice;

    //    @BindView(R2.id.fab_submit)
    public FloatingActionButton btnSubmit;


    public Long tenantId;
    private InvRecvGoodsEntity curGoods = null;
    private ScProductPricePresenter mScProductPricePresenter;
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;


    public static InvRecvInspectFragment newInstance(Bundle args) {
        InvRecvInspectFragment fragment = new InvRecvInspectFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

//    @Override
//    protected boolean isResponseBackPressed() {
//        return true;
//    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_invrecv_goods;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScProductPricePresenter = new ScProductPricePresenter(this);
        chainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mScanBar = (ScanBar) rootView.findViewById(R.id.scanBar);
        queryCheckbox = (AppCompatCheckBox) rootView.findViewById(R.id.checkbox);
        labelBarcode = (TextLabelView) rootView.findViewById(R.id.label_barcode);
        labelProductName = (TextLabelView) rootView.findViewById(R.id.label_productName);
        labelSendPrice = (TextLabelView) rootView.findViewById(R.id.label_send_price);
        labelSendQuantity = (TextLabelView) rootView.findViewById(R.id.label_send_quantity);
        labelSingleCostPrice = (TextLabelView) rootView.findViewById(R.id.label_singleCostPrice);
        labelHintPrice = (TextLabelView) rootView.findViewById(R.id.label_hintPrice);
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
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

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

//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelReceiveQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                labelReceiveAmount.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refresh(null);
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
                refresh(null);
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
            tenantId = args.getLong(EXTRA_KEY_TENANTID);

            if (!StringUtils.isEmpty(barcode)) {
                queryByBarcode(barcode);
            }
        }
    }

//    @Override
//    public boolean onBackPressed() {
//        return super.onBackPressed();
//    }

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
        if (!StringUtils.isEmpty(errorMsg)) {
            ZLogger.df(errorMsg);
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }
        isAcceptBarcodeEnabled = true;

        refresh(null);
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

        refresh(null);
    }


    /**
     * 刷新信息
     */
    private void refresh(InvRecvGoodsEntity goods) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = goods;
        if (curGoods == null) {
            labelBarcode.setEndText("");
            labelProductName.setEndText("");
            labelSendQuantity.setEndText("");
            labelSendPrice.setEndText("");
            labelHintPrice.setEndText("");
            labelSingleCostPrice.setEndText("");
            labelReceiveQuantity.setInputAndEnd("", "");
            labelReceiveAmount.setInput("");
            labelReceivePrice.setEndText("");

            btnSubmit.setEnabled(false);
        } else {
            ZLogger.d(JSONObject.toJSONString(curGoods));
            labelBarcode.setEndText(curGoods.getBarcode());
            labelProductName.setEndText(curGoods.getProductName());
            labelSendPrice.setEndText(MUtils.formatDouble(curGoods.getReceiptPrice(), ""));
            labelSendQuantity.setEndText(MUtils.formatDouble(curGoods.getReceiptQuantity(), ""));
            labelHintPrice.setEndText(MUtils.formatDouble(curGoods.getHintPrice(), ""));
            labelSingleCostPrice.setEndText(MUtils.formatDouble(curGoods.getSingleCostPrice(), ""));
//            labelSignQuantity.setEtContent(String.format("%.2f", curGoods.getSignQuantity()));
            //默认签收数量为空，根据实际情况填写
            labelReceiveQuantity.setInputAndEnd("", curGoods.getUnit());
//            labelReceiveAmount.setEndText(curGoods.getUnitSpec());
            labelReceiveAmount.setInput("");
            labelReceivePrice.setEndText(MUtils.formatDouble(calculateReceivePrice(), ""));

            btnSubmit.setEnabled(true);

            labelReceiveQuantity.requestFocus();
        }

//        DeviceUtils.hideSoftInput(getActivity(), labelReceiveQuantity);
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
                            hideProgressDialog();
                            refresh(invRecvGoodsEntity);
                            getTenantSkuMust(barcode);
                        } else {
                            findProductSku(barcode);
                        }
                    }

                });
    }

    /**
     * 查询平台商品档案
     */
    private void findProductSku(String barcode) {
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

        mScProductPricePresenter.findProductSku(barcode, null);
    }

    @Override
    public void onScProcuctPriceViewProcess() {
        onQueryProcess();
    }

    @Override
    public void onScProcuctPriceViewError(String errorMsg) {
        hideProgressDialog();
        refresh(null);
    }

    @Override
    public void onScProcuctPriceViewSuccess(PageInfo pageInfo, List<ProductSku> dataList) {
        if (dataList != null && dataList.size() > 0) {
            saveProductSku(dataList.get(0));
        } else {
            hideProgressDialog();
            isAcceptBarcodeEnabled = true;
            saveProductSku(null);
        }
    }

    /**
     * 保存平台商品档案
     */
    private void saveProductSku(ProductSku goods) {
        if (goods == null) {
            DialogUtil.showHint("未找到商品");
            refresh(null);
            return;
        }

        InvRecvGoodsEntity entity = InvRecvGoodsService.get().queryEntityByBarcode(goods.getBarcode());
        if (entity == null) {
            entity = new InvRecvGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
            entity.setProductName(goods.getName());
            entity.setUnit(goods.getUnit());
            entity.setBarcode(goods.getBarcode());

//            entity.setProSkuId(goods.getProSkuId());
//            entity.setChainSkuId(goods.getId());
//            entity.setProviderId(goods.getTenantId());
//            entity.setIsPrivate(IsPrivate.PLATFORM);
            entity.setUpdatedDate(new Date());
        }
        refresh(entity);

        //查询供应商的商品信息
        getTenantSkuMust(goods.getBarcode());
    }


    /**
     * 查询供应商下的商品信息
     */
    private void getTenantSkuMust(String barcode) {
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

        chainGoodsSkuPresenter.getTenantSkuMust(tenantId, barcode, queryCheckbox.isChecked());
    }

    @Override
    public void onChainGoodsSkuViewProcess() {
        onQueryProcess();
    }

    @Override
    public void onChainGoodsSkuViewError(String errorMsg) {
        hideProgressDialog();
        refresh(null);
    }

    @Override
    public void onChainGoodsSkuViewSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        hideProgressDialog();
        isAcceptBarcodeEnabled = true;

        if (dataList != null && dataList.size() > 0) {
            saveChainGoodsSku(dataList.get(0));
        } else {
            saveChainGoodsSku(null);
        }
    }

    @Override
    public void onChainGoodsSkuViewSuccess(ChainGoodsSku data) {
        hideProgressDialog();
        isAcceptBarcodeEnabled = true;

        saveChainGoodsSku(data);
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

    /**
     * 保存供应商商品
     */
    private void saveChainGoodsSku(ChainGoodsSku goods) {
        if (goods == null) {
            return;
        }

        if (curGoods != null) {
            curGoods.setProSkuId(goods.getProSkuId());
            curGoods.setChainSkuId(goods.getId());
            //使用当前选择的供应商代替商品的供应商属性，有的商品无供应商信息
            curGoods.setProviderId(tenantId);
//            curGoods.setProviderId(goods.getTenantId());
            curGoods.setIsPrivate(IsPrivate.PLATFORM);
            curGoods.setSingleCostPrice(goods.getSingleCostPrice());
            curGoods.setHintPrice(goods.getHintPrice());
            curGoods.setUpdatedDate(new Date());
        }
        refresh(curGoods);

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
