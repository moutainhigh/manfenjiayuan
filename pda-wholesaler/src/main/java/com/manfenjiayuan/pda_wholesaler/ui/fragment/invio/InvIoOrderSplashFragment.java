package com.manfenjiayuan.pda_wholesaler.ui.fragment.invio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.widget.EditQueryView;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.database.logic.InvIoPickGoodsService;
import com.bingshanguxue.pda.PDAScanFragment;
import com.manfenjiayuan.pda_wholesaler.ui.activity.PrimaryActivity;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.UIHelper;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 发货页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvIoOrderSplashFragment extends PDAScanFragment {

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;

    public static InvIoOrderSplashFragment newInstance(Bundle args) {
        InvIoOrderSplashFragment fragment = new InvIoOrderSplashFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invioorder_splash;
    }

    @Override
    protected void onScanCode(String code) {
        createNewOrderFromPickingOrder(code);
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

        /**
         * 拣货单不保存，每次进来都需要清空
         * */
        InvIoPickGoodsService.get().clear();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 新建发货单
     */
    @OnClick(R.id.button_create_neworder)
    public void createNewOrder() {
        eqvBarcode.clear();

        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_CREATE_INVIOORDER_BYFINDORDER);

        UIHelper.startActivity(getActivity(), PrimaryActivity.class, extras);
        getActivity().finish();
    }

    /**
     * 根据拣货单新建发货单
     */
    private void createNewOrderFromPickingOrder(String barcode) {
        isAcceptBarcodeEnabled = false;
        eqvBarcode.clear();
        eqvBarcode.requestFocusEnd();
        if (StringUtils.isEmpty(barcode)) {
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (!barcode.startsWith("4")) {
            DialogUtil.showHint("发货单条码不正确，请输入以4开头的条码");
            isAcceptBarcodeEnabled = true;
            return;
        }
        ZLogger.d("加载拣货单" + barcode);

        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FT_CREATE_INVIOORDER_BYFINDORDER);
        extras.putString(InvIoPickingGoodsFragment.EXTRA_KEY_ORDER_BARCODE, barcode);
        UIHelper.startActivity(getActivity(), PrimaryActivity.class, extras);
        getActivity().finish();

//        isAcceptBarcodeEnabled = true;
    }
}
