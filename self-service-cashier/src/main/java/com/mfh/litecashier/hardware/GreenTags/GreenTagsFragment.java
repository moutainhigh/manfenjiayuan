package com.mfh.litecashier.hardware.GreenTags;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;

import org.century.CenturyFragment;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 绿泰电子价签
 * A placeholder fragment containing a simple view.
 */
public class GreenTagsFragment extends CenturyFragment {

    @BindView(R.id.et_goodsnumber)
    EditText etGoodsNumber;
    @BindView(R.id.et_goodscode)
    EditText etGoodsCode;
    @BindView(R.id.et_tagNo)
    EditText etTagNo;

    static String[] SOAP_VERSIONS = new String[]{"SOAP 1.0", "SOAP 1.1", "SOAP 1.2"};


    public GreenTagsFragment() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_greentags;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        etGoodsCode.setText("1234");
        etTagNo.setText("1800110F");
    }


    @OnClick(R.id.button_ESLBindTag2Goods)
    public void ESLBindTags2Goods() {
        String goodsCode = etGoodsCode.getText().toString();
        if (StringUtils.isEmpty(goodsCode)) {
            DialogUtil.showHint("商品条码不能为空");
            return;
        }

        String tagNo = etTagNo.getText().toString();
        if (StringUtils.isEmpty(tagNo)) {
            DialogUtil.showHint("标签编号不能为空");
            return;
        }

        bindDefaultTag2Goods(goodsCode, tagNo);
    }


    /**
     * 推送商品
     */
    @OnClick(R.id.button_ESLPushGoodsExInfo)
    public void ESLPushGoodsInfoEx() {
        String goodsCode = etGoodsCode.getText().toString();
        if (StringUtils.isEmpty(goodsCode)) {
            DialogUtil.showHint("商品条码不能为空");
            etGoodsCode.requestFocus();
            return;
        }

        pushDefaultGoodsInfoEx(goodsCode);
    }

    /**
     * 批量推送商品
     */
    @OnClick(R.id.button_ESLPushGoodsExInfoPack)
    public void ESLPushGoodsExInfoPack() {
        String goodsNumber = etGoodsNumber.getText().toString();
        if (StringUtils.isEmpty(goodsNumber)) {
            DialogUtil.showHint("请输入商品数量");
            etGoodsNumber.requestFocus();
            return;
        }

        pushDefaultGoodsInfoPackEx(Integer.parseInt(goodsNumber));
    }

    /**
     * 查询商品
     */
    @OnClick(R.id.button_ESLQueryGoods)
    public void ESLQueryGoods() {
        String goodsCode = etGoodsCode.getText().toString();
        if (StringUtils.isEmpty(goodsCode)) {
            DialogUtil.showHint("商品条码不能为空");
            etGoodsCode.requestFocus();
            return;
        }

        queryGoods(goodsCode);
    }

    /**
     * 查询商品
     */
    @OnClick(R.id.button_ESLQueryTagEX)
    public void ESLQueryTagEX() {
        String tagNo = etTagNo.getText().toString();
        if (StringUtils.isEmpty(tagNo)) {
            DialogUtil.showHint("标签编号不能为空");
            etTagNo.requestFocus();
            return;
        }

        queryTag(tagNo);
    }

    @OnClick(R.id.button_getCountryCityByIp)
    public void getCountryCityByIp() {
        //Create instance for AsyncCallWS
        getCountryCityByIpAsyncTask task = new getCountryCityByIpAsyncTask();
        //Call execute
        task.execute();
    }

    @OnClick(R.id.button_getSumOfTwoInts)
    public void getSumOfTowInts() {
        //Create instance for AsyncCallWS
        getSumOfTowIntsAsyncTask task = new getSumOfTowIntsAsyncTask();
        //Call execute
        task.execute();
    }

}
