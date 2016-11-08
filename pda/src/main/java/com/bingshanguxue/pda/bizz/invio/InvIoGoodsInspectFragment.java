package com.bingshanguxue.pda.bizz.invio;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.database.entity.InvIoGoodsEntity;
import com.bingshanguxue.pda.database.service.InvIoGoodsService;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
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
 * 出入库商品检查
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvIoGoodsInspectFragment extends PDAScanFragment
        implements IChainGoodsSkuView {

    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    //    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
    //    @Bind(R.id.scanBar)
    public ScanBar mScanBar;
    private AppCompatCheckBox queryCheckbox;
    //    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    //    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    //    @Bind(R.id.label_price)
    EditLabelView labelPrice;
    //    @Bind(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;
    //    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;

    private InvIoGoodsEntity curGoods = null;
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;


    public static InvIoGoodsInspectFragment newInstance(Bundle args) {
        InvIoGoodsInspectFragment fragment = new InvIoGoodsInspectFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_invio_goods;
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
        mScanBar = (ScanBar) rootView.findViewById(R.id.scanBar);queryCheckbox = (AppCompatCheckBox) rootView.findViewById(R.id.checkbox);
        labelBarcode = (TextLabelView) rootView.findViewById(R.id.label_barcode);
        labelProductName = (TextLabelView) rootView.findViewById(R.id.label_productName);
        labelPrice = (EditLabelView) rootView.findViewById(R.id.label_price);
        labelSignQuantity = (EditLabelView) rootView.findViewById(R.id.label_sign_quantity);
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

            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
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
//            mScanBar.setSoftKeyboardEnabled(true);
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
        labelPrice.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                labelSignQuantity.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelSignQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
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

        Bundle args = getArguments();
        if (args != null) {
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

            if (!StringUtils.isEmpty(barcode)) {
                queryByBarcode(barcode);
            }
        }

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
     * 查询商品
     */
    public void queryByBarcode(final String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.reset();
            isAcceptBarcodeEnabled = true;
            return;
        }
        onQueryProcess();
        Observable.create(new Observable.OnSubscribe<InvIoGoodsEntity>() {
            @Override
            public void call(Subscriber<? super InvIoGoodsEntity> subscriber) {
                InvIoGoodsEntity goodsEntity = InvIoGoodsService.get().queryEntityBy(barcode);
                subscriber.onNext(goodsEntity);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<InvIoGoodsEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(InvIoGoodsEntity invIoGoodsEntity) {
                        if (invIoGoodsEntity != null) {
                            onQuerySuccess();
                            refreshPackage(invIoGoodsEntity);
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

        chainGoodsSkuPresenter.getTenantSkuMust(null, barcode, false);
    }


    //    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;

        onSubmitProcess();

        if (curGoods == null) {
            onSubmitError("商品无效");
            return;
        }

        String price = labelPrice.getInput();
        if (StringUtils.isEmpty(price)) {
            onSubmitError("请输入发货价格");
            return;
        }

        String quantityStr = labelSignQuantity.getInput();
        if (StringUtils.isEmpty(quantityStr)) {
            onSubmitError("请输入发货数量");
            return;
        }
        Double quantityCheck = Double.valueOf(quantityStr);

        if (curGoods.getQuantityCheck() > 0) {
            quantityCheckConfirmDialog(curGoods, Double.valueOf(price), quantityCheck);
        } else {
            InvIoGoodsService.get().inspect(curGoods, Double.valueOf(price), quantityCheck);
            onSubmitSuccess();
        }
    }

    /**
     * 查询处理中
     */
    public void onQueryProcess() {
        isAcceptBarcodeEnabled = false;
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    /**
     * 查询失败
     */
    public void onQueryError(String errorMsg) {
        ZLogger.df(errorMsg);
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        isAcceptBarcodeEnabled = true;
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
    private void refreshPackage(InvIoGoodsEntity goods) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = goods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelPrice.setInput("");
            labelSignQuantity.setInput("");

            btnSubmit.setEnabled(false);
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getProductName());
            labelPrice.setInput(MUtils.formatDouble(curGoods.getPrice(), ""));
            //默认签收数量为空，根据实际情况填写
            labelSignQuantity.setInput("");

            btnSubmit.setEnabled(true);

            labelSignQuantity.requestFocus();
        }
    }

    @Override
    public void onChainGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onChainGoodsSkuViewError(String errorMsg) {
        hideProgressDialog();
        refreshPackage(null);
    }

    @Override
    public void onChainGoodsSkuViewSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        hideProgressDialog();
        if (dataList != null && dataList.size() > 0) {
            saveChainGoodsSku(dataList.get(0));
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    @Override
    public void onChainGoodsSkuViewSuccess(ChainGoodsSku data) {
        hideProgressDialog();

        if (data != null) {
            saveChainGoodsSku(data);
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }




    /**
     * 保存搜索商品
     */
    private void saveChainGoodsSku(ChainGoodsSku goods) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getProSkuId() == null && StringUtils.isEmpty(goods.getBarcode())) {
            DialogUtil.showHint(" proSkuId和barcode不能同时为空");
            return;
        }

        InvIoGoodsEntity entity = InvIoGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null) {
            entity = new InvIoGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

//        entity.setOrderId(productEntity.getOrderId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setProductName(goods.getSkuName());
            entity.setPrice(goods.getSingleCostPrice());
            entity.setUnit(goods.getUnit());
            entity.setBarcode(goods.getBarcode());
            entity.setQuantityCheck(0D);
            entity.setUpdatedDate(new Date());
            entity.setPosId(SharedPreferencesManager.getTerminalId());

//            InvIoGoodsService.get().saveOrUpdate(entity);
        }
        refreshPackage(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;

    private void quantityCheckConfirmDialog(final InvIoGoodsEntity entity,
                                            final Double price, final Double quantity) {
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(false);
            quantityCheckConfirmDialog.setCanceledOnTouchOutside(false);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经选择%.2f件，请选择[覆盖]还是[累加]",
                entity.getQuantityCheck()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvIoGoodsService.get().inspect(entity, price, quantity);

                onSubmitSuccess();
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvIoGoodsService.get().inspect(entity, price, entity.getQuantityCheck() + quantity);
                onSubmitSuccess();
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
