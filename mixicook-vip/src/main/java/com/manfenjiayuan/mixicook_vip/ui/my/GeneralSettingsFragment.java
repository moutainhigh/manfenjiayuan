package com.manfenjiayuan.mixicook_vip.ui.my;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.InputTextFragment;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 设置·通用
 *
 * @author bingshanguxue
 */
public class GeneralSettingsFragment extends BaseFragment {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.item_2_0)
    SettingsItem btnItem;
    @Bind(R.id.item_location)
    ToggleSettingItem itemLocation;
    @Bind(R.id.item_notification)
    ToggleSettingItem itemNotification;

    public static GeneralSettingsFragment newInstance(Bundle args) {
        GeneralSettingsFragment fragment = new GeneralSettingsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_settings_general;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);


        }

        toolbar.setTitle(R.string.topbar_title_settings_general);
//        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
//        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
//        setSupportActionBar(toolbar);
        if (animType == ANIM_TYPE_NEW_FLOW) {
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        itemLocation.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean on) {
                SharedPreferencesManager.setLocationAcceptEnabled(on);
            }
        });
        itemNotification.init(new ToggleSettingItem.OnViewListener() {
            @Override
            public void onToggleChanged(boolean on) {
                SharedPreferencesManager.setNotificationAcceptEnabled(on);
            }
        });

        refresh();
    }

    private void refresh() {
        itemLocation.setChecked(SharedPreferencesManager.getLocationAcceptEnabled());
        itemNotification.setChecked(SharedPreferencesManager.getNotificationAcceptEnabled());
    }

    /**
     * 显示Dialog
     */
    @OnClick(R.id.item_2_0)
    public void showCleanCacheDialog() {
        showConfirmDialog("系统变慢6，您确定要清除缓存吗？",
                "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
    }
}

