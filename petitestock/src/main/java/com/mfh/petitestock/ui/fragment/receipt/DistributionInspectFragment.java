package com.mfh.petitestock.ui.fragment.receipt;


import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.entity.DistributionSignEntity;
import com.mfh.petitestock.database.logic.DistributionSignService;
import com.mfh.petitestock.ui.fragment.GpioFragment;
import com.mfh.petitestock.widget.compound.EditLabelView;
import com.mfh.petitestock.widget.compound.EditQueryView;
import com.mfh.petitestock.widget.compound.TextLabelView;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品配送－－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class DistributionInspectFragment extends GpioFragment {


    private static final String TAG = "DistributionInspectFragment";
    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind({ R.id.label_productName, R.id.label_price, R.id.label_totalcount })
    List<TextLabelView> labelViews;
    @Bind(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;

    @Bind(R.id.button_reject) Button btnReject;
    @Bind(R.id.button_submit)
    Button btnSubmit;

    private DistributionSignEntity curGoods = null;

    public static DistributionInspectFragment newInstance(Bundle args) {
        DistributionInspectFragment fragment = new DistributionInspectFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_distribution_inspect_2;
    }

    @Override
    protected void onScanCode(String code) {
        eqvBarcode.setInputString(code);
        query(code);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        labelSignQuantity.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelSignQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
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

        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                query(text);
            }
        });


        Bundle args = getArguments();
        if (args != null) {
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);
            if (!StringUtils.isEmpty(barcode)){
                eqvBarcode.setInputString(barcode);
            }
            else{
                eqvBarcode.setInputString("");
            }
        }

        query(eqvBarcode.getInputString());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 查询包裹信息
     * */
    public void query(String barcode){
        curGoods = DistributionSignService.get().queryEntityBy(barcode);
        refreshPackage();
    }

    @OnClick(R.id.button_reject)
    public void reject(){
        DistributionSignService.get().reject(curGoods);

        curGoods = null;
        refreshPackage();
    }

    @OnClick(R.id.button_submit)
    public void submit() {
        String quantity = labelSignQuantity.getEtContent();
        if (StringUtils.isEmpty(quantity)){
            DialogUtil.showHint("请输入签收数量");
            return;
        }

        DistributionSignService.get().inspect(curGoods, Double.valueOf(quantity));

        init();
    }

    private void init(){
        curGoods = null;
        refreshPackage();
    }

    /**
     * 刷新信息
     * */
    private void refreshPackage(){
        if (curGoods == null){
            labelViews.get(0).setTvSubTitle("");
            labelViews.get(1).setTvSubTitle("");
            labelViews.get(2).setTvSubTitle("");
            labelSignQuantity.setEtContent("");

            btnReject.setEnabled(false);
            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();
        }
        else {
            labelViews.get(0).setTvSubTitle(curGoods.getProductName());
            labelViews.get(1).setTvSubTitle(String.format("¥%.2f", curGoods.getPrice()));
            labelViews.get(2).setTvSubTitle(String.format("%.2f", curGoods.getTotalCount()));
//            labelSignQuantity.setEtContent(String.format("%.2f", curGoods.getSignQuantity()));
            //默认签收数量为空，根据实际情况填写
            labelSignQuantity.setEtContent("");

            btnReject.setEnabled(true);
            btnSubmit.setEnabled(true);

            labelSignQuantity.requestFocus();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelSignQuantity);
    }
}
