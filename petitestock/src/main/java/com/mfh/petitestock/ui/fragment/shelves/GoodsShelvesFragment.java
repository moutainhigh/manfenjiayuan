package com.mfh.petitestock.ui.fragment.shelves;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.invOrder.MerchandiseApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.logic.ShelveService;
import com.mfh.petitestock.ui.SimpleActivity;
import com.mfh.petitestock.ui.fragment.GpioFragment;
import com.mfh.petitestock.utils.ShelveSyncManager;
import com.mfh.petitestock.widget.compound.EditQueryView;
import com.mfh.petitestock.widget.compound.TextLabelView;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 货架商品绑定
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsShelvesFragment extends GpioFragment {
    @Bind(R.id.eqv_shelve)
    EditQueryView eqvShelve;
    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind({R.id.label_barcodee, R.id.label_productName, R.id.label_spec, R.id.label_quantity})
    List<TextLabelView> labelViews;

    @Bind(R.id.button_bind)
    Button btnBind;

    private ScGoodsSku curGoods = null;//当前盘点商品
    private boolean isPackage = false;//查询到的商品是否是有规格的，true,显示箱包总数，否则按单品计算

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

        EventBus.getDefault().register(this);
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

    NetCallBack.NetTaskCallBack updateResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    String retStr = retValue.getValue();

                    //出库成功:1-556637
                    ZLogger.d("绑定成功:" + retStr);
//                        DialogUtil.showHint("修改成功");
//                    hideProgressDialog();
                    showProgressDialog(ProgressDialog.STATUS_ERROR, "绑定成功", true);
                    loadInit();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("绑定失败：" + errMsg);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    btnBind.setEnabled(true);
//                    ShelveService.get().addNewEntity();
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };

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

        curGoods = null;
        isPackage = false;

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
                NetProcessor.Processor<ScGoodsSku>>(
                new NetProcessor.Processor<ScGoodsSku>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
                        if (rspData == null) {
                            DialogUtil.showHint("未找到商品");
                            refreshGoodsInfo(null);
                        } else {
                            RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                            ScGoodsSku goods = retValue.getValue();
                            if (!goods.getBarcode().equals(barcode)) {
                                isPackage = true;
                            }
                            refreshGoodsInfo(goods);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("查询失败: " + errMsg);
                        DialogUtil.showHint("未找到商品");
                        refreshGoodsInfo(null);
                    }
                }
                , ScGoodsSku.class
                , AppContext.getAppContext()) {
        };

        MerchandiseApiImpl.findStockTakeGoodsByBarcode(barcode, responseCallback);
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

    private void refreshGoodsInfo(ScGoodsSku goods) {
        curGoods = goods;
        if (goods != null) {
//            eqvShelve.clear();
            eqvBarcode.clear();
            eqvBarcode.requestFocus();

            labelViews.get(0).setTvSubTitle(curGoods.getBarcode());
            labelViews.get(1).setTvSubTitle(curGoods.getSkuName());
            labelViews.get(2).setTvSubTitle(curGoods.getShortName());
            labelViews.get(3).setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), ""));

            btnBind.setEnabled(true);
            DeviceUtils.hideSoftInput(getActivity(), eqvBarcode);
        } else {
            eqvShelve.clear();
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

}
