package com.manfenjiayuan.pda_supermarket.ui.fragment.receipt;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.widget.EditQueryView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.logic.DistributionSignService;
import com.manfenjiayuan.pda_supermarket.scanner.PDAScanFragment;
import com.manfenjiayuan.pda_supermarket.ui.activity.SecondaryActivity;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.UIHelper;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品配送－－签收页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvRecvOrderSplashFragment extends PDAScanFragment {

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;

    public static InvRecvOrderSplashFragment newInstance(Bundle args) {
        InvRecvOrderSplashFragment fragment = new InvRecvOrderSplashFragment();

        if (args != null) {
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
        eqvBarcode.requestFocusEnd();
        eqvBarcode.clear();
        receiveMSendorder(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                onScanCode(text);
            }
        });

        //清空签收数据库
//        InvRecvGoodsService.get().clear();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //清空签收数据库
//        InvRecvGoodsService.get().clear();
    }

    /**
     * 签收采购订单
     */
    @OnClick(R.id.button_read_sendorder)
    public void readSendOrder() {
        DistributionSignService.get().clear();
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_SENDORDER);
//        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

        UIHelper.startActivity(getActivity(), SecondaryActivity.class, extras);
    }

    /**
     * 新建收货单
     */
    @OnClick(R.id.button_create_neworder)
    public void createNewOrder() {
        DistributionSignService.get().clear();
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_RECVDORDER_CREATE);
//        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

        UIHelper.startActivity(getActivity(), SecondaryActivity.class, extras);
    }

    /**
     * 签收发货单
     */
    private void receiveMSendorder(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        if (!barcode.startsWith(InvOrderApi.PI)) {
            DialogUtil.showHint("发货单条码格式不正确");
            eqvBarcode.clear();
            eqvBarcode.requestFocusEnd();
            return;
        }
        DistributionSignService.get().clear();
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_RECEIVEORDER_INVIOORDER);
        extras.putString(ReceiveMSendOrderFragment.EXTRA_KEY_BARCODE, barcode);

        UIHelper.startActivity(getActivity(), SecondaryActivity.class, extras);
    }
}
