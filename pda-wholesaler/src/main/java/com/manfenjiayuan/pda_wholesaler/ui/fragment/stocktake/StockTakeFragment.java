package com.manfenjiayuan.pda_wholesaler.ui.fragment.stocktake;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.EditQueryView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.bean.Shelfnumber;
import com.bingshanguxue.pda.PDAScanFragment;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SimpleActivity;
import com.manfenjiayuan.pda_wholesaler.ui.dialog.SelectShelvesDialog;
import com.manfenjiayuan.pda_wholesaler.utils.DataSyncService;
import com.manfenjiayuan.pda_wholesaler.utils.SharedPreferencesHelper;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 盘点批次
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class StockTakeFragment extends PDAScanFragment implements IInvSkuGoodsView{

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


    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;
    private SelectShelvesDialog mSelectShelvesDialog = null;

    private Long curOrderId;//当前盘点批次编号
    private Long curShelfNumber = 0L;//当前盘点货架
    private InvSkuGoods curGoods = null;//当前盘点商品

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

        mInvSkuGoodsPresenter = new InvSkuGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            curOrderId = args.getLong(EXTRA_KEY_ORDER_ID);
        }

//        initProgressDialog("正在同步数据", "同步成功", "同步失败");

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
            public void onKeycodeEnterClick(String text) {
                submit();
            }

            @Override
            public void onScan() {
                refresh(null);
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

        shelvesNumberView.setText(String.format("区域号: %d", curShelfNumber));
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh(null);
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
                shelvesNumberView.setText(String.format("区域号：%d", curShelfNumber));
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
        ZLogger.d(String.format("StocTakeFragment: StockTakeSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncService.StockTakeSyncEvent.EVENT_ID_SYNC_FINISHED) {
//            showProgressDialog(ProgressDialog.STATUS_DONE, "同步成功", true);
            hideProgressDialog();
        } else if (event.getEventId() == DataSyncService.StockTakeSyncEvent.EVENT_ID_SYNC_FAILED) {
//            showProgressDialog(ProgressDialog.STATUS_DONE, "同步失败", true);
            hideProgressDialog();
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

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            refresh(null);
            return;
        }

        mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
//        ScGoodsSkuApiImpl.findStockTakeGoodsByBarcode(barcode, queryResponseCallback);
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

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        //保存商品到待盘点列表,在后台进行盘点。
        StockTakeService.get().addNewEntity(curOrderId, curShelfNumber, curGoods,
                Double.valueOf(quantity));
        DataSyncService.get().sync(DataSyncService.SYNC_STEP_UPLOAD_STOCKTAKE);

//        DialogUtil.showHint("提交成功");
        refresh(null);
    }

    /**
     * 刷新信息
     */
    private void refresh(InvSkuGoods stockTakeGoods) {
        curGoods = stockTakeGoods;
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
            labelViews.get(1).setTvSubTitle(curGoods.getName());
            labelViews.get(2).setTvSubTitle("");
            labelViews.get(3).setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), ""));
            labelQuantity.setEtContent("");

            btnSubmit.setEnabled(true);

            labelQuantity.requestFocus();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelQuantity);
    }

    @Override
    public void onProcess() {

    }

    @Override
    public void onError(String errorMsg) {

        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        refresh(null);
    }

    @Override
    public void onSuccess(InvSkuGoods invSkuGoods) {
        if (invSkuGoods == null) {
            DialogUtil.showHint("未找到商品");
        }
        refresh(invSkuGoods);
        hideProgressDialog();
    }
}
