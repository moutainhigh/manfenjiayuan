package com.manfenjiayuan.pda_supermarket.ui.store.invloss;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.PDAScanManager;
import com.bingshanguxue.pda.database.entity.InvLossStockGoodsEntity;
import com.bingshanguxue.pda.database.service.InvLossStockGoodsService;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 报损－－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvLossStockInspectFragment extends PDAScanFragment implements IScGoodsSkuView {

    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";
    @BindView(R.id.toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.scanBar)
    public ScanBar mScanBar;

    @BindView(R.id.label_barcode)
    TextLabelView labelBarcode;
    @BindView(R.id.label_productName)
    TextLabelView labelName;
    @BindView(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;

    @BindView(R.id.fab_submit)
    public FloatingActionButton btnSubmit;
    @BindView(R.id.fab_scan)
    FloatingActionButton btnSweep;

    private InvLossStockGoodsEntity curGoods = null;
    private ScGoodsSkuPresenter chainGoodsSkuPresenter;


    public static InvLossStockInspectFragment newInstance(Bundle args) {
        InvLossStockInspectFragment fragment = new InvLossStockInspectFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_invloss_goods;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
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
        labelSignQuantity.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            submit();
                        }
                    }
                });

        btnSweep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt(PDAScanManager.ScanBarcodeEvent.KEY_EVENTID,
                        PDAScanManager.ScanBarcodeEvent.EVENT_ID_START_ZXING);
                EventBus.getDefault().post(new PDAScanManager.ScanBarcodeEvent(args));
            }
        });

        if (SharedPrefesManagerFactory.isCameraSweepEnabled()) {
            btnSweep.setVisibility(View.VISIBLE);
        } else {
            btnSweep.setVisibility(View.GONE);
        }

        String barcode = null;
        Bundle args = getArguments();
        if (args != null) {
            barcode = args.getString(EXTRA_KEY_BARCODE, null);
        }


        if (!StringUtils.isEmpty(barcode)) {
            queryByBarcode(barcode);
        } else {
            refresh(null);
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
    public void queryByBarcode(String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.reset();
            isAcceptBarcodeEnabled = true;
            return;
        }

        QueryGoodsAsyncTask queryGoodsAsyncTask = new QueryGoodsAsyncTask(barcode);
        queryGoodsAsyncTask.execute();
    }

    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;

        onSubmitProcess();

        if (curGoods == null) {
            onSubmitError("商品无效");
            return;
        }
        String quantityStr = labelSignQuantity.getInput();
        if (StringUtils.isEmpty(quantityStr)) {
            onSubmitError("请输入报损数量");
            return;
        }
        Double quantityCheck = Double.valueOf(quantityStr);

        if (curGoods.getCheckInv() > 0) {
            quantityCheckConfirmDialog(curGoods, quantityCheck);
        } else {
            InvLossStockGoodsService.get().inspect(curGoods, quantityCheck);
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
    private void refresh(InvLossStockGoodsEntity goods) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = goods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelName.setTvSubTitle("");
            labelSignQuantity.setInput("");

            btnSubmit.setEnabled(false);
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelName.setTvSubTitle(curGoods.getProductName());

            //默认签收数量为空，根据实际情况填写
            labelSignQuantity.setInput("");

            btnSubmit.setEnabled(true);

            labelSignQuantity.requestFocus();
        }
    }


    @Override
    public void onIScGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        refresh(null);
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
        hideProgressDialog();

        if (dataList != null && dataList.size() > 0) {
            saveScGoodsSku(dataList.get(0));
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku data) {
        hideProgressDialog();

        if (data != null) {
            saveScGoodsSku(data);
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }


    class QueryGoodsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String barcode;
        private InvLossStockGoodsEntity goodsEntity;

        public QueryGoodsAsyncTask(String barcode) {
            this.barcode = barcode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                goodsEntity = InvLossStockGoodsService.get().queryEntityBy(barcode);
                if (goodsEntity != null) {
                    return true;
                }
            } catch (Exception e) {
                ZLogger.d("查询本地收货商品失败, " + e.toString());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                hideProgressDialog();

                refresh(goodsEntity);
            } else {
                queryNetGoods(barcode);
            }
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            onIScGoodsSkuViewProcess();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    private void queryNetGoods(String barcode) {
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onIScGoodsSkuViewError(getString(R.string.toast_network_error));
            return;
        }

        chainGoodsSkuPresenter.getGoodsByBarCode(barcode);
    }

    /**
     * 保存搜索商品
     */
    private void saveScGoodsSku(ScGoodsSku goods) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        InvLossStockGoodsEntity entity = InvLossStockGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null) {
            entity = new InvLossStockGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

            entity.setProSkuId(goods.getProSkuId());
            entity.setProductName(goods.getSkuName());
            entity.setBarcode(goods.getBarcode());

            //默认数量是0
            entity.setCheckInv(0D);

            entity.setUpdatedDate(new Date());

            InvLossStockGoodsService.get().saveOrUpdate(entity);
        }
        refresh(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;

    private void quantityCheckConfirmDialog(final InvLossStockGoodsEntity entity, final Double quantity) {
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(true);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经盘点 %.2f 件，请选择 [覆盖] or [累加]",
                entity.getCheckInv()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvLossStockGoodsService.get().inspect(entity, quantity);

                onSubmitSuccess();
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvLossStockGoodsService.get().inspect(entity, entity.getCheckInv() + quantity);
                onSubmitSuccess();
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
