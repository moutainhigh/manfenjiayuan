package com.mfh.litecashier.ui.widget;

import android.os.Bundle;

public final class ViewPageInfo {

	public final String tag;
    public final Class<?> clss;
    public final Bundle args;
    public final String title;
    public final int resId;

    public ViewPageInfo(String _title, int resId, String _tag, Class<?> _class, Bundle _args) {
    	title = _title;
        this.resId = resId;
        tag = _tag;
        clss = _class;
        args = _args;
    }
}