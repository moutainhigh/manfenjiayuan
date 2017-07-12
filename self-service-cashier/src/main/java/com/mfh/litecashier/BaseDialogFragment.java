package com.mfh.litecashier;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;

/**
 * Created by bingshanguxue on 09/07/2017.
 */

public abstract class BaseDialogFragment extends DialogFragment {

    protected View rootView;

    protected static final int DIALOG_TYPE_DEFAULT = 0;
    protected static final int DIALOG_TYPE_SMALL = 1;
    protected static final int DIALOG_TYPE_MIDDLE = 2;

    protected int getDialogType() {
        return DIALOG_TYPE_DEFAULT;
    }

    protected abstract int getLayoutResId();//{return 0;}

    protected void initViews(View rootView) {
    }

    protected abstract void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZLogger.d("onCreate");

        //1 通过样式定义
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog);
        //2代码设置 无标题 无边框
//        setStyle(DialogFragment.STYLE_NO_TITLE|DialogFragment.STYLE_NO_FRAME,0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


//3 在此处设置 无标题 对话框背景色
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // //对话框背景色
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.RED));
//        getDialog().getWindow().setDimAmount(0.5f);//背景黑暗度

        //不能在此处设置style
        // setStyle(DialogFragment.STYLE_NORMAL,R.style.Mdialog);//在此处设置主题样式不

        //Inflate the layout for this fragment
        rootView = inflater.inflate(getLayoutResId(), container, false);

        initViews(rootView);

        createViewInner(rootView, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        ZLogger.d("onStart");
        //控制Dialog的宽和高
        Window window = getDialog().getWindow();
        WindowManager m = window.getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = window.getAttributes();
        if (getDialogType() == DIALOG_TYPE_SMALL) {
            p.width = DensityUtil.dip2px(getContext(), 400);
//        p.y = d.getHeight();

//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
            window.setAttributes(p);
        } else if (getDialogType() == DIALOG_TYPE_MIDDLE) {
            p.width = d.getWidth() * 2 / 3;
//        p.y = d.getHeight();

//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
            window.setAttributes(p);
        }
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        ZLogger.d("show");

        return super.show(transaction, tag);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ZLogger.d("onCancel");
    }
}
