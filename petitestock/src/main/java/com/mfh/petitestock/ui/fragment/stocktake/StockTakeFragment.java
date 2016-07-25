package com.mfh.petitestock.ui.fragment.stocktake;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.impl.MerchandiseApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.R;
import com.mfh.petitestock.bean.Shelfnumber;
import com.mfh.petitestock.database.logic.StockTakeService;
import com.mfh.petitestock.ui.SimpleActivity;
import com.mfh.petitestock.ui.dialog.SelectShelvesDialog;
import com.mfh.petitestock.ui.fragment.GpioFragment;
import com.mfh.petitestock.utils.DataSyncService;
import com.mfh.petitestock.utils.SharedPreferencesHelper;
import com.mfh.petitestock.widget.compound.EditLabelView;
import com.mfh.petitestock.widget.compound.EditQueryView;
import com.mfh.petitestock.widget.compound.TextLabelView;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 盘点批次
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class StockTakeFragment extends GpioFragment {

    private static final String TAG = "StockTakeFragment";
    public static final String EXTRA_KEY_ORDER_ID = "orderId";

    @Bind(R.id.shelvesNumberView)
    NaviAddressView shelvesNumberView;
    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind({R.id.label_barcodee, R.id.label_productName, R.id.label_spec, R.id.label_quantity})
    List<TextLabelView> labelViews;
    @Bind(R.id.label_newquantity)
    EditLabelView labelQuantity;

    @Bind(R.id.button_submit)
    Button btnSubmit;


    private SelectShelvesDialog mSelectShelvesDialog = null;

    private Long curOrderId;//当前盘点批次编号
    private Long curShelfNumber = 0L;//当前盘点货架
    private ScGoodsSku curGoods = null;//当前盘点商品
    private boolean isPackage = false;//查询到的商品是否是有规格的，true,显示箱包总数，否则按单品计算

    public static StockTakeFragment newInstance(Bundle args) {
        StockTakeFragment fragment = new StockTakeFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stock_take;
    }

    @Override
    protected void onScanCode(String code) {
        eqvBarcode.setInputString(code);
        query(code);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            curOrderId = args.getLong(EXTRA_KEY_ORDER_ID);
        }

        initProgressDialog("正在同步数据", "同步成功", "同步失败");

        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                query(text);
            }
        });

        labelQuantity.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
        labelQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onInput(String text) {
                submit();
            }

            @Override
            public void onScan() {
                init();
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });

        if (curOrderId == null || curOrderId.compareTo(0L) == 0) {
            DialogUtil.showHint("批次无效");
            getActivity().finish();
            return;
        }

        Long lastOrderId = SharedPreferencesHelper.getLastStocktakeOrderId();
        if (lastOrderId.compareTo(curOrderId) != 0){
            //清空盘点记录
            StockTakeService.get().clear();
        }
        //保存当前盘点编号
        SharedPreferencesHelper.setLastStocktakeOrderId(curOrderId);

        shelvesNumberView.setText(String.format("货架号: %d", curShelfNumber));
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }


    @OnClick(R.id.shelvesNumberView)
    public void toggleShelfnumber() {
        if (mSelectShelvesDialog == null) {
            mSelectShelvesDialog = new SelectShelvesDialog(getActivity());
            mSelectShelvesDialog.setCancelable(true);
            mSelectShelvesDialog.setCanceledOnTouchOutside(false);
        }
        mSelectShelvesDialog.init(26, new SelectShelvesDialog.OnDialogListener() {
            @Override
            public void onItemSelected(Shelfnumber entity) {
                curShelfNumber = entity.getNumber();
                shelvesNumberView.setText(String.format("货架：%d", curShelfNumber));
            }

        });
        if (!mSelectShelvesDialog.isShowing()) {
            mSelectShelvesDialog.show();
        }
    }

    /**
     * 同步盘点数据
     */
    @OnClick(R.id.button_sync)
    public void uploadOrders() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.tip_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);
        DataSyncService.get().sync();
    }

    @OnClick(R.id.button_history)
    public void redirectToStockTakeList() {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FRAGMENT_TYPE_STOCKTAKE_LIST);
//        SimpleActivity.actionStart(getActivity(), extras);
        UIHelper.startActivity(getActivity(), SimpleActivity.class, extras);
    }

    public void onEventMainThread(DataSyncService.StockTakeSyncEvent event) {
        ZLogger.d(String.format("CommodityApplyFragment: StockTakeSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncService.StockTakeSyncEvent.EVENT_ID_SYNC_FINISHED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步成功", true);
        } else if (event.getEventId() == DataSyncService.StockTakeSyncEvent.EVENT_ID_SYNC_FAILED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步失败", true);
        }
    }


    /**
     * 查询包裹信息
     */
    public void query(final String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        eqvBarcode.clear();
        curGoods = null;
        isPackage = false;

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            refresh();
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
                NetProcessor.Processor<ScGoodsSku>>(
                new NetProcessor.Processor<ScGoodsSku>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
                        if (rspData == null) {
                            DialogUtil.showHint("未找到商品");
                        } else {
                            RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                            curGoods = retValue.getValue();
                            if (!curGoods.getBarcode().equals(barcode)) {
                                isPackage = true;
                            }
                        }

                        refresh();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("盘点失败: " + errMsg);
                        DialogUtil.showHint("未找到商品");
                        refresh();
                    }
                }
                , ScGoodsSku.class
                , AppContext.getAppContext()) {
        };

        MerchandiseApiImpl.findStockTakeGoodsByBarcode(barcode, responseCallback);
    }

    @OnClick(R.id.button_submit)
    public void submit() {

        btnSubmit.setEnabled(false);
        if (curGoods == null) {
//            DialogUtil.showHint("请先扫描商品条码，确认商品信息");
            btnSubmit.setEnabled(true);
            return;
        }

        String quantity = labelQuantity.getEtContent();
        if (StringUtils.isEmpty(quantity)) {
            DialogUtil.showHint("请输入盘点数目");
            btnSubmit.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        //保存商品到待盘点列表,在后台进行盘点。
        StockTakeService.get().addNewEntity(curOrderId, curShelfNumber, curGoods, Double.valueOf(quantity));
        DataSyncService.get().sync(DataSyncService.SYNC_STEP_UPLOAD_STOCKTAKE);

        DialogUtil.showHint("提交成功");
        init();
    }

    private void init() {
        curGoods = null;
        refresh();
    }

    /**
     * 刷新信息
     */
    private void refresh() {
        if (curGoods == null) {
            labelViews.get(0).setTvSubTitle("");
            labelViews.get(1).setTvSubTitle("");
            labelViews.get(2).setTvSubTitle("");
            labelViews.get(3).setTvSubTitle("");
            labelQuantity.setEtContent("");

            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();
        } else {
            labelViews.get(0).setTvSubTitle(curGoods.getBarcode());
            labelViews.get(1).setTvSubTitle(curGoods.getSkuName());
            labelViews.get(2).setTvSubTitle(curGoods.getShortName());
            labelViews.get(3).setTvSubTitle(String.format("%.2f", curGoods.getQuantity()));
//            if (isPackage){
//                labelQuantity.setEtContent(String.format("%.2f", curGoods.getPackageNum()));
//            }else{
//                labelQuantity.setEtContent("1");
//            }
            labelQuantity.setEtContent("");

            btnSubmit.setEnabled(true);

            labelQuantity.requestFocus();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelQuantity);
    }

}
