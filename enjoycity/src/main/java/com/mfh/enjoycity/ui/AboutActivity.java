package com.mfh.enjoycity.ui;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.enjoycity.R;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.uikit.base.BaseActivity;
import com.bingshanguxue.vector_uikit.SettingsItem;

import java.util.List;

import butterknife.Bind;


/**
 * 关于
 * Created by Nat.ZZN(bingshanguxue)
 */
public class AboutActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind({R.id.item_app_description, R.id.item_licience})
    List<SettingsItem> btnItems;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_about);//必须在setSupportActionBar(toolbar);之前设置才有效
        //import that this is set first
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AboutActivity.this.onBackPressed();
                    }
                });

//        final Drawable backArrow = getResources().getDrawable(R.drawable.ic_toolbar_back);
//        if (backArrow != null) {
//            backArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        }
//
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(backArrow);
//        }


        //call setNavigationIcon() to display our back arrow image which we’ll use to navigate back
//        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
//        toolbar.setNavigationOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        AboutActivity.this.onBackPressed();
//                    }
//                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btnItems.get(0).setOnClickListener(myOnClickListener);
        btnItems.get(1).setOnClickListener(myOnClickListener);
    }

    private View.OnClickListener myOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_app_description: {
                    StaticWebActivity.actionStart(AboutActivity.this, H5Api.URL_APP_DESCRIPTION);
                }
                break;
                case R.id.item_licience: {
                    StaticWebActivity.actionStart(AboutActivity.this, H5Api.URL_LICENSE);
                }
                break;
            }
        }
    };


}
