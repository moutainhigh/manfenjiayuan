package com.mfh.petitestock.ui.fragment.receipt;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.MfhModule;
import com.mfh.petitestock.Constants;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.logic.DistributionSignService;
import com.mfh.petitestock.ui.SecondaryActivity;
import com.mfh.petitestock.ui.fragment.GpioFragment;
import com.mfh.petitestock.widget.compound.EditQueryView;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品配送－－签收页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvRecvOrderSplashFragment extends GpioFragment {

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;

    public static InvRecvOrderSplashFragment newInstance(Bundle args) {
        InvRecvOrderSplashFragment fragment = new InvRecvOrderSplashFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invrecvorder_splash;
    }

    @Override
    protected void onScanCode(String code) {
        receiveMSendorder(code);
        eqvBarcode.clear();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(false);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                receiveMSendorder(text);
                eqvBarcode.clear();
            }
        });


        if (MfhUserManager.getInstance().containsModule(MfhModule.CHAIN_MANAGER)){
            eqvBarcode.setVisibility(View.GONE);
        }
        else{
            eqvBarcode.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //清空签收数据库
        DistributionSignService.get().clear();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //清空签收数据库
        DistributionSignService.get().clear();
    }

    /**
     * 签收采购订单
     * */
    @OnClick(R.id.button_read_sendorder)
    public void readSendOrder(){
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_SENDORDER);
//        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_DISTRIBUTION_INSPECT);
    }

    /**
     * 新建收货单
     * */
    @OnClick(R.id.button_create_neworder)
    public void createNewOrder(){
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_RECVDORDER_CREATE);
//        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_DISTRIBUTION_INSPECT);
    }

    /**
     * 签收发货单
     * */
    private void receiveMSendorder(String barcode){
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_RECEIVE_M_SENDORDER);
        extras.putString(ReceiveMSendOrderFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_DISTRIBUTION_INSPECT);
    }

}
