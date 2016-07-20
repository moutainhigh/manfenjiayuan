package com.manfenjiayuan.mixicook_vip;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 首页
 *
 * @author bingshanguxue created on 2015-04-13
 * @since Framework 1.0
 */
public class InputTextFragment extends BaseFragment {
    public static final String EXTRA_HINT_TEXT = "extraHintText";
    public static final String EXTRA_RAW_TEXT = "extraRawText";
    public static final String EXTRA_RESULT = "extraResult";

    @Bind(R.id.et_nickname) EditText etNickname;

    public InputTextFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_input_text;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        etNickname.setText(MfhLoginService.get().getHumanName());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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


    @OnClick(R.id.button_submit)
    public void updateProfile(){
        final String nickName = etNickname.getText().toString();

        Intent data = new Intent();
        data.putExtra(EXTRA_RESULT, nickName);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();

    }
}
