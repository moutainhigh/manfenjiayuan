package com.manfenjiayuan.pda_supermarket.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.scanner.PDAScanFragment;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditQueryView;
import com.manfenjiayuan.pda_supermarket.widget.compound.TextLabelView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import org.century.GreenTagsApi;
import org.century.GreenTagsApiImpl;
import org.century.GreenTagsSettingsDialog;
import org.century.schemas.GoodsInfoEX;
import org.century.schemas.ReaderInfoEX;
import org.century.schemas.TagInfoEX;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 货架商品绑定
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class BindGoods2TagFragment extends PDAScanFragment implements IInvSkuGoodsView {
    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind(R.id.eqv_tagno)
    EditQueryView eqvTagNo;
    @Bind(R.id.label_barcodee)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_quantity)
    TextLabelView labelQuantity;
    @Bind(R.id.label_costPrice)
    TextLabelView labelCostPrice;

    @Bind(R.id.button_bind)
    Button btnBind;

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
        if (eqvTagNo.hasFocus()) {
            eqvTagNo.setInputString(code);
            eqvBarcode.requestFocus();
        } else {
//            eqvBarcode.setInputString(code);
            eqvBarcode.clear();
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
        initProgressDialog("正在同步数据", "同步成功", "同步失败");

        eqvTagNo.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvTagNo.setSoftKeyboardEnabled(true);
        eqvTagNo.setInputSubmitEnabled(true);
        eqvTagNo.setOnViewListener(new EditQueryView.OnViewListener() {
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
                queryGoods(text);
            }
        });

        eqvTagNo.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();

        eqvTagNo.requestFocus();

        if (!GreenTagsApi.validate()){
            showGreenTagsDialog();
        }
    }

    private GreenTagsSettingsDialog mGreenTagsSettingsDialog = null;

    @OnClick(R.id.button_settings)
    public void showGreenTagsDialog(){
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
    @OnClick(R.id.button_bind)
    public void bindGoods2Tag() {
        btnBind.setEnabled(false);

        if (curGoods == null){
            DialogUtil.showHint("请重新扫描货条码");
            eqvBarcode.requestFocus();
            btnBind.setEnabled(true);
            return;
        }

        String tagNo = eqvTagNo.getInputString();
        if (StringUtils.isEmpty(tagNo)) {
            DialogUtil.showHint("请扫描货架编号");
            eqvTagNo.requestFocus();
            btnBind.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
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
            eqvBarcode.requestFocus();
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
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

            btnBind.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getName());
            labelCostPrice.setTvSubTitle(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));

            btnBind.setEnabled(true);
        }

        DeviceUtils.hideSoftInput(getActivity(), labelCostPrice);
    }

    public void bindDefaultTag2Goods(String goodsCode, String tagNo){
        GoodsInfoEX googsInfoEX = GoodsInfoEX.createDefault(goodsCode, false);
        TagInfoEX tagInfoEX = TagInfoEX.createDefault(tagNo);
//        tagInfoEX.tagId = 1;
//        propertyList[0] = new Property("name", StringUtils.genNonceChinease(4));
//        propertyList[1] = new Property("origin", StringUtils.genNonceChinease(4));

        ESLBindTag2GoodsAsyncTask task = new ESLBindTag2GoodsAsyncTask(tagInfoEX, googsInfoEX);
        task.execute();
    }

    @Override
    public void onProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在搜索商品...", false);
    }

    @Override
    public void onError(String errorMsg) {

        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);

        refresh(null);
    }

    @Override
    public void onSuccess(InvSkuGoods invSkuGoods) {

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
            ZLogger.d("doInBackground");
//            getCountryCityByIp("http://www.manfenjiayuan.cn");
//            getCountryCityByIp("221.224.34.30");

            try {
                return GreenTagsApiImpl.ESLBindTag2Goods(tagInfoEX, googsInfoEX, readerInfoEX, null, null);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLBindTags2Goods failed, %s", e.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLBindTags2Goods failed, %s", e.toString()));
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ZLogger.d("onPostExecute: 绑定" + (aBoolean ? "成功" : "失败"));
            btnBind.setEnabled(true);


            if (aBoolean){
                showProgressDialog(ProgressDialog.STATUS_ERROR, "绑定成功", true);
                refresh(null);
                eqvTagNo.clear();
                eqvTagNo.requestFocusEnd();
            }
            else{
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
            ZLogger.d("onProgressUpdate");
        }
    }
}
