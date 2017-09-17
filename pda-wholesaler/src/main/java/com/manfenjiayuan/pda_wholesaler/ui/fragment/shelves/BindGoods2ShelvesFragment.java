package com.manfenjiayuan.pda_wholesaler.ui.fragment.shelves;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.manfenjiayuan.business.mvp.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.mvp.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.database.logic.ShelveService;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SecondaryActivity;
import com.manfenjiayuan.pda_wholesaler.utils.ShelveSyncManager;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 货架商品绑定
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class BindGoods2ShelvesFragment extends PDAScanFragment implements IInvSkuGoodsView {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

    @Bind(R.id.label_barcodee)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_quantity)
    TextLabelView labelQuantity;
    @Bind(R.id.label_costPrice)
    TextLabelView labelCostPrice;
    @Bind(R.id.label_tagno)
    EditLabelView labelTagNo;

    @Bind(R.id.fab_submit)
    FloatingActionButton btnBind;

    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;
    private InvSkuGoods curGoods = null;

    public static BindGoods2ShelvesFragment newInstance(Bundle args) {
        BindGoods2ShelvesFragment fragment = new BindGoods2ShelvesFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_bindgoods2shelves;
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;

        if (labelTagNo.hasFocus()) {
            labelTagNo.setInput(code);
            labelTagNo.requestFocus();
        } else {
            query(code);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInvSkuGoodsPresenter = new InvSkuGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("价签绑定");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                    syncData();
                } else if (id == R.id.action_history) {
                    redirectToHistory();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_bindshelves);
        mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                query(text);
            }

            @Override
            public void onAction1Click(String text) {
                query(text);
            }
        });

        initProgressDialog("正在同步数据", "同步成功", "同步失败");

        labelTagNo.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            labelTagNo.requestFocusEnd();
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshGoodsInfo(curGoods);
    }

    public void onEventMainThread(ShelveSyncManager.ShelveSyncManagerEvent event) {
        ZLogger.d(String.format("ShelveSyncManagerEvent(%d)", event.getEventId()));
        if (event.getEventId() == ShelveSyncManager.ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步成功", true);
        } else if (event.getEventId() == ShelveSyncManager.ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步失败", true);
        }
    }

    /**
     * 同步数据
     */
    public void syncData() {
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.tip_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);
        ShelveSyncManager.get().sync();
    }

    /**
     * 同步盘点数据
     */
    public void redirectToHistory() {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE,
                SecondaryActivity.FRAGMENT_TYPE_SHELVESBIND_HISTORY);
//        SimpleActivity.actionStart(getActivity(), extras);
        UIHelper.startActivity(getActivity(), SecondaryActivity.class, extras);
    }

    /**
     * 签收采购订单
     */
    @OnClick(R.id.fab_submit)
    public void submit() {
        btnBind.setEnabled(false);
        if (curGoods == null) {
            btnBind.setEnabled(true);
            return;
        }

        String rackNo = labelTagNo.getInput();
        if (StringUtils.isEmpty(rackNo)) {
            DialogUtil.showHint("请扫描货架编号");
            labelTagNo.requestFocusEnd();
            btnBind.setEnabled(true);
            return;
        }

        if (rackNo.length() > 10) {
            DialogUtil.showHint("货架编号格式不正确");
            labelTagNo.requestFocusEnd();
            btnBind.setEnabled(true);
            return;
        }

        if (!NetworkUtils.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnBind.setEnabled(true);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在绑定货架...", false);
        //保存商品到待盘点列表,在后台进行盘点。
        ShelveService.get().addNewEntity(rackNo, curGoods.getBarcode());
        ShelveSyncManager.get().sync();
        refreshGoodsInfo(null);
    }

    /**
     * 查询包裹信息
     */
    public void query(final String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.reset();
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (!NetworkUtils.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            return;
        }

        mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
    }

    private void refreshGoodsInfo(InvSkuGoods goods) {
        curGoods = goods;
        if (goods != null) {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getName());
            labelCostPrice.setTvSubTitle(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), ""));
            labelTagNo.setInput("");
            labelTagNo.requestFocusEnd();
            btnBind.setEnabled(true);
        } else {
            mScanBar.reset();

            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelCostPrice.setTvSubTitle("");
            labelQuantity.setTvSubTitle("");
            labelTagNo.setInput("");

            btnBind.setEnabled(false);
        }

        DeviceUtils.hideSoftInput(getActivity(), labelTagNo);
        isAcceptBarcodeEnabled = true;
    }

    @Override
    public void onIInvSkuGoodsViewProcess() {

    }

    @Override
    public void onIInvSkuGoodsViewError(String errorMsg) {
        DialogUtil.showHint(errorMsg);
        refreshGoodsInfo(null);
    }

    @Override
    public void onIInvSkuGoodsViewSuccess(InvSkuGoods invSkuGoods) {
        if (invSkuGoods == null) {
            DialogUtil.showHint("未找到商品");
        }
        refreshGoodsInfo(invSkuGoods);
    }
}
