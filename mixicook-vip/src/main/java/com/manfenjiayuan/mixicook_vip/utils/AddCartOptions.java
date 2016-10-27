package com.manfenjiayuan.mixicook_vip.utils;

import android.view.View;

/**
 * Created by bingshanguxue on 27/10/2016.
 */

public class AddCartOptions {
    private View sharedView;

    public static AddCartOptions makeOptions(View sharedView){
        AddCartOptions addCartOptions = new AddCartOptions();
        addCartOptions.sharedView = sharedView;
        return addCartOptions;
    }

    public View getSharedView() {
        return sharedView;
    }
}
