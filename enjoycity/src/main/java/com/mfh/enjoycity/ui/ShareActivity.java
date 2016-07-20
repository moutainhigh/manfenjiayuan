package com.mfh.enjoycity.ui;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.wxapi.WXHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;

import java.util.List;

import butterknife.Bind;


/**
 * 分享
 */
public class ShareActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind({R.id.share_to_wx, R.id.share_to_wx_frientcircle})
    List<SettingsItem> btnItems;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_share;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_share);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShareActivity.this.onBackPressed();
                    }
                });
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
                case R.id.share_to_wx: {
                    Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                    WXHelper.getInstance(ShareActivity.this)
                            .sendWebpageToWX("http://a.app.qq.com/o/simple.jsp?pkgname=com.mfh.owner",
                                    "我在用满分家园·城市之间客户端，推荐你一起用！",
                                    "满分家园，品质新生活", thumb,
                                    SendMessageToWX.Req.WXSceneTimeline);
                }
                break;
                case R.id.share_to_wx_frientcircle: {
                    Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                    WXHelper.getInstance(ShareActivity.this)
                            .sendWebpageToWX("http://a.app.qq.com/o/simple.jsp?pkgname=com.mfh.owner",
                                    "我在用满分家园·城市之间客户端，推荐你一起用！",
                                    "满分家园，品质新生活", thumb,
                                    SendMessageToWX.Req.WXSceneSession);
                }
                break;
            }
        }
    };

}
