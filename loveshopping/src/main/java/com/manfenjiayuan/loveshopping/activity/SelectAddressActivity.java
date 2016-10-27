package com.manfenjiayuan.loveshopping.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.manfenjiayuan.loveshopping.R;
import com.mfh.framework.uikit.base.BaseActivity;

import butterknife.Bind;

public class SelectAddressActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_select_address;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle("搜索地址");//必须在setSupportActionBar(toolbar);之前设置才有效
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SelectAddressActivity.this.onBackPressed();
                    }
                });

        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_add);
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
