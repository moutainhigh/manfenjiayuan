package com.manfenjiayuan.pda_supermarket.ui.fragment.stocktake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 库存盘点-－设置
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class StockTakeSettingsFragment extends BaseFragment {

    private static final String TAG = "StockTakeSettingsFragment";

    @Bind(R.id.et_shelf_number)
    EditText etShelfNumber;

    public static StockTakeSettingsFragment newInstance(Bundle args) {
        StockTakeSettingsFragment fragment = new StockTakeSettingsFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stocktake_settings;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

    }

    @OnClick(R.id.button_submit)
    public void submit() {
        String shelfNumberText = etShelfNumber.getText().toString();
        if (StringUtils.isEmpty(shelfNumberText)){
            DialogUtil.showHint("货架编号不能为空");
        }

        int shelfNumber = Integer.valueOf(shelfNumberText);

        Intent intent = new Intent();
        intent.putExtra("shelfNumber", shelfNumber);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

}
