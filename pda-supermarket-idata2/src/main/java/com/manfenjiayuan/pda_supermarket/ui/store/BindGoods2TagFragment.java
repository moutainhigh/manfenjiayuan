package com.manfenjiayuan.pda_supermarket.ui.store;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.PDAScanManager;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.manfenjiayuan.business.mvp.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.mvp.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import org.century.GreenTagsApi;
import org.century.GreenTagsApiImpl;
import org.century.GreenTagsSettingsDialog;
import org.century.schemas.GoodsInfoEX;
import org.century.schemas.ReaderInfoEX;
import org.century.schemas.TagInfoEX;
import org.greenrobot.eventbus.EventBus;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 货架商品绑定
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class BindGoods2TagFragment extends PDAScanFragment implements IInvSkuGoodsView {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.scanBar)
    public ScanBar mScanBar;
    @BindView(R.id.label_barcodee)
    TextLabelView labelBarcode;
    @BindView(R.id.label_productName)
    TextLabelView labelProductName;
    @BindView(R.id.label_quantity)
    TextLabelView labelQuantity;
    @BindView(R.id.label_costPrice)
    TextLabelView labelCostPrice;
    @BindView(R.id.label_tagno)
    EditLabelView labelTagNo;
    @BindView(R.id.fab_submit)
    FloatingActionButton btnBind;
    @BindView(R.id.fab_scan)
    FloatingActionButton btnSweep;

    private InvSkuGoods curGoods = null;
    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;

    public static BindGoods2TagFragment newInstance(Bundle args) {
        BindGoods2TagFragment fragment = new BindGoods2TagFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_bindgoods2tag;
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;

        if (labelTagNo.hasFocus()) {
            labelTagNo.setInput(code);
            labelTagNo.requestFocusEnd();
            // Fixed, 扫描价签条码后允许继续扫描修改
            isAcceptBarcodeEnabled = true;
        } else {
//            eqvBarcode.setInputString(code);
//            eqvBarcode.clear();
            queryGoods(code);
        }
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
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
        }
        mToolbar.setTitle("价签绑定");
        if (animType == ANIM_TYPE_NEW_FLOW) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
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
                if (id == R.id.action_settings) {
                    showGreenTagsDialog();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_bindtags);
        mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                queryGoods(text);
            }

            @Override
            public void onAction1Click(String text) {
                queryGoods(text);
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
    }

    @Override
    public void onResume() {
        super.onResume();

        mScanBar.requestFocus();

        if (!GreenTagsApi.validate()) {
            showGreenTagsDialog();
        }
    }

    private GreenTagsSettingsDialog mGreenTagsSettingsDialog = null;

    private void showGreenTagsDialog() {
        if (mGreenTagsSettingsDialog == null) {
            mGreenTagsSettingsDialog = new GreenTagsSettingsDialog(getActivity());
            mGreenTagsSettingsDialog.setCancelable(true);
            mGreenTagsSettingsDialog.setCanceledOnTouchOutside(false);
        }
        mGreenTagsSettingsDialog.refresh();
        if (!mGreenTagsSettingsDialog.isShowing()) {
            mGreenTagsSettingsDialog.show();
        }
    }

    /**
     * 签收采购订单
     */
    @OnClick(R.id.fab_submit)
    public void bindGoods2Tag() {
        btnBind.setEnabled(false);

        if (curGoods == null) {
            DialogUtil.showHint("请重新扫描货条码");
            mScanBar.requestFocus();
            btnBind.setEnabled(true);
            return;
        }

        String tagNo = labelTagNo.getInput();
        if (StringUtils.isEmpty(tagNo)) {
            DialogUtil.showHint("请扫描货架编号");
            labelTagNo.requestFocus();
            btnBind.setEnabled(true);
            return;
        }

        if (!NetworkUtils.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnBind.setEnabled(true);
            return;
        }

        bindDefaultTag2Goods(curGoods.getBarcode(), tagNo);
    }

    /**
     * 查询商品信息
     */
    public void queryGoods(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.requestFocus();
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (!NetworkUtils.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            refresh(null);
            return;
        }

        mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
    }

    /**
     * 刷新信息
     */
    private void refresh(InvSkuGoods invSkuGoods) {
        curGoods = invSkuGoods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelCostPrice.setTvSubTitle("");
            labelQuantity.setTvSubTitle("");
            labelTagNo.setInput("");

            btnBind.setEnabled(false);

            mScanBar.reset();

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            mScanBar.setInputText("");
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getName());
            labelCostPrice.setTvSubTitle(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelTagNo.setInput("");
            labelTagNo.requestFocusEnd();

            btnBind.setEnabled(true);
        }

        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), labelTagNo);
    }

    public void bindDefaultTag2Goods(String goodsCode, String tagNo) {
        GoodsInfoEX googsInfoEX = GoodsInfoEX.createDefault(goodsCode, false);
        TagInfoEX tagInfoEX = TagInfoEX.createDefault(tagNo);
//        tagInfoEX.tagId = 1;
//        propertyList[0] = new Property("name", StringUtils.genNonceChinease(4));
//        propertyList[1] = new Property("origin", StringUtils.genNonceChinease(4));

        ESLBindTag2GoodsAsyncTask task = new ESLBindTag2GoodsAsyncTask(tagInfoEX, googsInfoEX);
        task.execute();
    }

    @Override
    public void onIInvSkuGoodsViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在搜索商品...", false);
    }

    @Override
    public void onIInvSkuGoodsViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);

        refresh(null);
    }

    @Override
    public void onIInvSkuGoodsViewSuccess(InvSkuGoods invSkuGoods) {
        hideProgressDialog();

        refresh(invSkuGoods);
    }

    public class ESLBindTag2GoodsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private TagInfoEX tagInfoEX = new TagInfoEX();
        private GoodsInfoEX googsInfoEX = new GoodsInfoEX();
        private ReaderInfoEX readerInfoEX = null;//new ReaderInfoEX();//deprecated

        public ESLBindTag2GoodsAsyncTask(TagInfoEX tagInfoEX, GoodsInfoEX googsInfoEX) {
            this.tagInfoEX = tagInfoEX;
            this.googsInfoEX = googsInfoEX;
        }

        @Override
        protected Boolean doInBackground(String... params) {
//            getCountryCityByIp("http://www.manfenjiayuan.cn");
//            getCountryCityByIp("221.224.34.30");

            try {
                return GreenTagsApiImpl.ESLBindTag2Goods(tagInfoEX, googsInfoEX, readerInfoEX, null, null);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
                ZLogger.ef(e.toString());
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(e.toString());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ZLogger.d("onPostExecute: 绑定" + (aBoolean ? "成功" : "失败"));
            btnBind.setEnabled(true);

            if (aBoolean) {
                showProgressDialog(ProgressDialog.STATUS_ERROR, "绑定成功", true);
                refresh(null);
            } else {
                showProgressDialog(ProgressDialog.STATUS_ERROR, "绑定失败", true);
            }
//            hideProgressDialog();
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在绑定标签...", false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
