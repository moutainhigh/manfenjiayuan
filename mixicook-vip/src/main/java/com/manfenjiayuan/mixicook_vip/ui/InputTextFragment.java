package com.manfenjiayuan.mixicook_vip.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.BindView;


/**
 * 首页
 *
 * @author bingshanguxue created on 2015-04-13
 * @since Framework 1.0
 */
public class InputTextFragment extends BaseFragment {
    public static final String EXTRA_KEY_TITLE = "title";
    public static final String EXTRA_KEY_HINT_TEXT = "hintText";
    public static final String EXTRA_KEY_RAW_TEXT = "rawText";
    public static final String EXTRA_KEY_RESULT = "result";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_nickname) EditText etNickname;

    private String title, hintText, rawText;

    public static InputTextFragment newInstance(Bundle args){
        InputTextFragment fragment = new InputTextFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_input_text;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            title = args.getString(EXTRA_KEY_TITLE);
            hintText = args.getString(EXTRA_KEY_HINT_TEXT);
            rawText = args.getString(EXTRA_KEY_RAW_TEXT);

        }
        toolbar.setTitle(title);
        if (animType == ANIM_TYPE_NEW_FLOW) {
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        }
        else{
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_submit) {
                    submit();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_submit);

        etNickname.setHint(hintText);
        etNickname.setText(rawText);
        etNickname.setSelection(etNickname.length());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }



    public void submit(){
        final String nickName = etNickname.getText().toString();

        Intent data = new Intent();
        data.putExtra(EXTRA_KEY_RESULT, nickName);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();

    }
}
