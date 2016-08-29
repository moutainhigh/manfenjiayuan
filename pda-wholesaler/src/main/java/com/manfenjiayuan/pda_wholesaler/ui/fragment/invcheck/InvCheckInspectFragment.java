package com.manfenjiayuan.pda_wholesaler.ui.fragment.invcheck;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.database.service.InvCheckGoodsService;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.bean.Shelfnumber;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SecondaryActivity;
import com.manfenjiayuan.pda_wholesaler.ui.dialog.SelectShelvesDialog;
import com.manfenjiayuan.pda_wholesaler.utils.DataSyncService;
import com.manfenjiayuan.pda_wholesaler.utils.SharedPreferencesHelper;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.NetworkUtils;
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
public class InvCheckInspectFragment extends PDAScanFragment implements IInvSkuGoodsView {

    public static final String EXTRA_KEY_ORDER_ID = "orderId";

    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

    @Bind(R.id.shelvesNumberView)
    NaviAddressView shelvesNumberView;
    @Bind({R.id.label_barcodee, R.id.label_productName, R.id.label_spec, R.id.label_quantity})
    List<TextLabelView> labelViews;
    @Bind(R.id.label_newquantity)
    EditLabelView labelQuantity;

    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;


    private SelectShelvesDialog mSelectShelvesDialog = null;

    private Long curOrderId;//当前盘点批次编号
    private Long curShelfNumber = 0L;//当前盘点货架
    private InvSkuGoods curGoods = null;//当前盘点商品
    private InvSkuGoodsPresenter mScGoodsSkuPresenter = null;

    public static InvCheckInspectFragment newInstance(Bundle args) {
        InvCheckInspectFragment fragment = new InvCheckInspectFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_invcheck_goods;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScGoodsSkuPresenter = new InvSkuGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            curOrderId = args.getLong(EXTRA_KEY_ORDER_ID);
        }

        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
            mToolbar.setNavigationOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            DeviceUtils.hideSoftInput(getActivity());
                            getActivity().onBackPressed();
                        }
                    });
            // Set an OnMenuItemClickListener to handle menu item clicks
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // Handle the menu item
                    int id = item.getItemId();
                    if (id == R.id.action_sync) {
                        uploadOrders();
                    } else if (id == R.id.action_history) {
                        redirectToStockTakeHistory();
                    }
                    return true;
                }
            });
            // Inflate a menu to be displayed in the toolbar
            mToolbar.inflateMenu(R.menu.menu_inv_check_inspect);

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
        if (lastOrderId.compareTo(curOrderId) != 0) {
            //清空盘点记录
            InvCheckGoodsService.get().clear();
        }
        //保存当前盘点编号
        SharedPreferencesHelper.setLastStocktakeOrderId(curOrderId);

        shelvesNumberView.setText(String.format("区域号: %d", curShelfNumber));
        mScanBar.reset();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inv_check_inspect, menu);

        super.onCreateOptionsMenu(menu, inflater);
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


    @OnClick(R.id.shelvesNumberView)
    public void toggleShelfnumber() {
        if (mSelectShelvesDialog == null) {
            mSelectShelvesDialog = new SelectShelvesDialog(getActivity());
            mSelectShelvesDialog.setCancelable(true);
            mSelectShelvesDialog.setCanceledOnTouchOutside(true);
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
    public void uploadOrders() {
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.tip_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);
        DataSyncService.get().sync();
    }

    public void redirectToStockTakeHistory() {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE,
                SecondaryActivity.FRAGMENT_TYPE_STOCKTAKE_HISTORY);
//        SimpleActivity.actionStart(getActivity(), extras);
        UIHelper.startActivity(getActivity(), SecondaryActivity.class, extras);
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
     * 搜索条码
     */
    public void queryByBarcode(String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            onQueryError("请先扫描商品条码");
            return;
        }

        if (!NetworkUtils.isConnect(getActivity())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

        mScGoodsSkuPresenter.getByBarcodeMust(barcode);
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

    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;

        onSubmitProcess();

        if (curGoods == null) {
            onSubmitError("请扫描商品条码");
            return;
        }

        String quantity = labelQuantity.getInput();
        if (StringUtils.isEmpty(quantity)) {
            onSubmitError("请输入盘点数目");
            return;
        }


        //保存商品到待盘点列表,在后台进行盘点。
        InvCheckGoodsService.get().addNewEntity(curOrderId, curShelfNumber, curGoods,
                Double.valueOf(quantity));

        if (NetworkUtils.isConnect(AppContext.getAppContext())) {
            DataSyncService.get().sync(DataSyncService.SYNC_STEP_UPLOAD_STOCKTAKE);
        }

        DialogUtil.showHint("提交成功");
        refresh(null);
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
     * 刷新信息
     */
    private void refresh(InvSkuGoods stockTakeGoods) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = stockTakeGoods;
        if (curGoods == null) {
            labelViews.get(0).setTvSubTitle("");
            labelViews.get(1).setTvSubTitle("");
            labelViews.get(2).setTvSubTitle("");
            labelViews.get(3).setTvSubTitle("");
            labelQuantity.setInput("");

            btnSubmit.setEnabled(false);
        } else {
            labelViews.get(0).setTvSubTitle(curGoods.getBarcode());
            labelViews.get(1).setTvSubTitle(curGoods.getName());
            labelViews.get(2).setTvSubTitle("");
            labelViews.get(3).setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), ""));
            labelQuantity.setInput("");

            btnSubmit.setEnabled(true);

            labelQuantity.requestFocusEnd();
        }
    }


    @Override
    public void onIInvSkuGoodsViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onIInvSkuGoodsViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        refresh(null);
    }

    @Override
    public void onIInvSkuGoodsViewSuccess(InvSkuGoods data) {
        hideProgressDialog();

        if (data != null) {
            refresh(data);
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }
}
