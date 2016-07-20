package com.mfh.enjoycity.ui;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.mfh.enjoycity.R;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;

import butterknife.Bind;


/**
 * 推荐人
 * */
public class InviteActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.et_input)
    EditText etInput;

    private static final String DEFAULT_TELPHONE_NUMBER = "4008866671";


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_invite;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_invite);//必须在setSupportActionBar(toolbar);之前设置才有效
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InviteActivity.this.onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_submit) {
                    //TODO:提交，并返回
                    String text = etInput.getText().toString();
                    if (StringUtils.isEmpty(text)) {
                        DialogUtil.showHint("请填写推荐人编号");
                    } else {
                        DialogUtil.showHint("您输入的推荐人编号是： " + text);
                        InviteActivity.this.onBackPressed();
                    }
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_submit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etInput.setHint(R.string.hint_edit_invite);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_submit, menu);

        return super.onCreateOptionsMenu(menu);
    }

}
