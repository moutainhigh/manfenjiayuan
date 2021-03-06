package com.manfenjiayuan.pda_wholesaler.ui.fragment.shelves;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bingshanguxue.pda.widget.EditQueryView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.database.logic.ShelveService;
import com.bingshanguxue.pda.PDAScanFragment;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SimpleActivity;
import com.manfenjiayuan.pda_wholesaler.utils.ShelveSyncManager;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 货架商品绑定
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsShelvesFragment extends PDAScanFragment implements IInvSkuGoodsView {
    @Bind(R.id.eqv_shelve)
    EditQueryView eqvShelve;
    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind({R.id.label_barcodee, R.id.label_productName, R.id.label_costPrice, R.id.label_quantity})
    List<TextLabelView> labelViews;

    @Bind(R.id.button_bind)
    Button btnBind;


    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;
    private InvSkuGoods curGoods = null;//
//    private boolean isPackage = false;//查询到的商品是否是有规格的，true,显示箱包总数，否则按单品计算

    public static GoodsShelvesFragment newInstance(Bundle args) {
        GoodsShelvesFragment fragment = new GoodsShelvesFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_wholesaler_goodsshelves;
    }

    @Override
    protected void onScanCode(String code) {
        if (eqvShelve.hasFocus()) {
            eqvShelve.setInputString(code);
            eqvBarcode.requestFocus();
        } else {
            eqvBarcode.setInputString(code);
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

        initProgressDialog("正在同步数据", "同步成功", "同步失败");

        eqvShelve.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvShelve.setSoftKeyboardEnabled(true);
        eqvShelve.setInputSubmitEnabled(true);
        eqvShelve.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                eqvBarcode.requestFocus();
            }
        });

        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                query(text);
            }
        });

        eqvShelve.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (curGoods == null){
            loadInit();
        }
        else{
            refreshGoodsInfo(curGoods);
        }

//        onScanCode("*222*22*");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onEventMainThread(ShelveSyncManager.ShelveSyncManagerEvent event) {
        ZLogger.d(String.format("GoodsShelvesFragment: ShelveSyncManagerEvent(%d)", event.getEventId()));
        if (event.getEventId() == ShelveSyncManager.ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步成功", true);
        } else if (event.getEventId() == ShelveSyncManager.ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步失败", true);
        }
    }

    /**
     * 同步数据
     */
    @OnClick(R.id.button_sync)
    public void syncData() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.tip_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);
        ShelveSyncManager.get().sync();
    }

    /**
     * 同步盘点数据
     */
    @OnClick(R.id.button_history)
    public void uploadOrders() {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_SERVICE_TYPE, SimpleActivity.FRAGMENT_TYPE_SHELVESBIND_HISTORY);
//        SimpleActivity.actionStart(getActivity(), extras);
        UIHelper.startActivity(getActivity(), SimpleActivity.class, extras);
    }

    /**
     * 签收采购订单
     */
    @OnClick(R.id.button_bind)
    public void bindGoods2RackNo() {
        btnBind.setEnabled(false);
        if (curGoods == null) {
            btnBind.setEnabled(true);
            return;
        }

        String rackNo = eqvShelve.getInputString();
        if (StringUtils.isEmpty(rackNo)) {
            DialogUtil.showHint("请扫描货架编号");
            eqvShelve.requestFocus();
            btnBind.setEnabled(true);
            return;
        }

        if (rackNo.length() > 10) {
            DialogUtil.showHint("货架编号格式不正确");
            eqvBarcode.requestFocus();
            btnBind.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnBind.setEnabled(true);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在绑定货架...", false);
        //保存商品到待盘点列表,在后台进行盘点。
        ShelveService.get().addNewEntity(rackNo, curGoods.getBarcode());
        ShelveSyncManager.get().sync();
        loadInit();
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
            return;
        }


        mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
    }

    private void loadInit() {
        curGoods = null;

        eqvShelve.clear();
        eqvShelve.requestFocus();
        eqvBarcode.clear();

        labelViews.get(0).setTvSubTitle("");
        labelViews.get(1).setTvSubTitle("");
        labelViews.get(2).setTvSubTitle("");
        labelViews.get(3).setTvSubTitle("");

        btnBind.setEnabled(false);

        DeviceUtils.hideSoftInput(getActivity(), eqvShelve);
    }

    private void refreshGoodsInfo(InvSkuGoods goods) {
        curGoods = goods;
        if (goods != null) {
//            eqvShelve.clear();
            eqvBarcode.clear();
            eqvBarcode.requestFocus();

            labelViews.get(0).setTvSubTitle(curGoods.getBarcode());
            labelViews.get(1).setTvSubTitle(curGoods.getName());
            labelViews.get(2).setTvSubTitle(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelViews.get(3).setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), ""));

            btnBind.setEnabled(true);
            DeviceUtils.hideSoftInput(getActivity(), eqvBarcode);
        } else {
//            eqvShelve.clear();
            eqvBarcode.clear();
            eqvBarcode.requestFocus();

            labelViews.get(0).setTvSubTitle("");
            labelViews.get(1).setTvSubTitle("");
            labelViews.get(2).setTvSubTitle("");
            labelViews.get(3).setTvSubTitle("");

            btnBind.setEnabled(false);
            DeviceUtils.hideSoftInput(getActivity(), eqvShelve);
        }
    }

    @Override
    public void onProcess() {

    }

    @Override
    public void onError(String errorMsg) {
        DialogUtil.showHint(errorMsg);
        refreshGoodsInfo(null);
    }

    @Override
    public void onSuccess(InvSkuGoods invSkuGoods) {
        if (invSkuGoods == null) {
            DialogUtil.showHint("未找到商品");
        }
        refreshGoodsInfo(invSkuGoods);
    }
}
