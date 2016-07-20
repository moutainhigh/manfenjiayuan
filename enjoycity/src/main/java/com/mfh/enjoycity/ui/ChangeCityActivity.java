package com.mfh.enjoycity.ui;


import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.CityAdapter;
import com.mfh.framework.uikit.base.BaseActivity;

import butterknife.Bind;


/**
 * 切换城市
 *
 * */
public class ChangeCityActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.listView)
    ListView mListView;
    private CityAdapter mAdapter;

    protected int activityCloseEnterAnimation;
    protected int activityCloseExitAnimation;

    private static final int MAX_PAGE_SIZE = 100;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_change_city;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_change_city);//必须在setSupportActionBar(toolbar);之前设置才有效
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.btn_toolbar_close);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChangeCityActivity.this.onBackPressed();
                    }
                });

        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_submit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(R.style.AppTheme_NewFlow);
        // Retrieve the animations set in the theme applied to this activity in the
// manifest..
        TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowAnimationStyle});
        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
        activityStyle.recycle();

// Now retrieve the resource ids of the actual animations used in the animation style pointed to by
// the window animation resource id.
        activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId,
                new int[] {android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
        activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
        activityCloseExitAnimation = activityStyle.getResourceId(1, 0);
        activityStyle.recycle();

        super.onCreate(savedInstanceState);

        mAdapter = new CityAdapter(this);
        mAdapter.add("苏州");
        mAdapter.add("测试1");
        mAdapter.add("测试2");
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        DeviceUtils.hideSoftInput();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }

}
