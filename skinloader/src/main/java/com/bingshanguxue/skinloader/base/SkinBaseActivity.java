package com.bingshanguxue.skinloader.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.View;
import android.widget.TextView;

import com.bingshanguxue.skinloader.attr.base.DynamicAttr;
import com.bingshanguxue.skinloader.config.SkinConfig;
import com.bingshanguxue.skinloader.listener.IDynamicNewView;
import com.bingshanguxue.skinloader.listener.ISkinUpdate;
import com.bingshanguxue.skinloader.loader.SkinInflaterFactory;
import com.bingshanguxue.skinloader.loader.SkinManager;
import com.bingshanguxue.skinloader.statusbar.StatusBarUtil;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseActivity;

import java.util.List;

/**
 * Created by _SOLID
 * Date:2016/4/14
 * Time:10:24
 * 需要实现换肤功能的Activity就需要继承于这个Activity
 */
public class SkinBaseActivity extends BaseActivity implements ISkinUpdate, IDynamicNewView {

    private SkinInflaterFactory mSkinInflaterFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSkinInflaterFactory = new SkinInflaterFactory();
        mSkinInflaterFactory.setAppCompatActivity(this);
        LayoutInflaterCompat.setFactory(getLayoutInflater(), mSkinInflaterFactory);

        super.onCreate(savedInstanceState);
        changeStatusColor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SkinManager.getInstance().attach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SkinManager.getInstance().detach(this);
        mSkinInflaterFactory.clean();
    }

    @Override
    public void onThemeUpdate() {
        try {
            ZLogger.d("皮肤主题切换成功");
            if (mSkinInflaterFactory != null) {
                mSkinInflaterFactory.applySkin();
            }
            changeStatusColor();
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public void changeStatusColor() {
        if (!SkinConfig.isCanChangeStatusColor()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ZLogger.d("changeStatus");
            int color = SkinManager.getInstance().getColorPrimaryDark();
            StatusBarUtil statusBarBackground = new StatusBarUtil(
                    this, color);
            if (color != -1)
                statusBarBackground.setStatusBarbackColor();
        }
    }

    @Override
    public void dynamicAddView(View view, List<DynamicAttr> pDAttrs) {
        mSkinInflaterFactory.dynamicAddSkinEnableView(this, view, pDAttrs);
    }

    @Override
    public void dynamicAddView(View view, String attrName, int attrValueResId) {
        mSkinInflaterFactory.dynamicAddSkinEnableView(this, view, attrName, attrValueResId);
    }

    @Override
    public void dynamicAddFontView(TextView textView) {
        mSkinInflaterFactory.dynamicAddFontEnableView(textView);
    }

}
