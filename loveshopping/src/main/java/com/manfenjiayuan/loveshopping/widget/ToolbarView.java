package com.manfenjiayuan.loveshopping.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.manfenjiayuan.loveshopping.R;

import butterknife.ButterKnife;


/**
 * toolbar's view
 */
public class ToolbarView extends RelativeLayout {
//	@Bind(R.id.tv_address) TextView tvAddress;

	public ToolbarView(Context context) {
		super(context);
        init();
	}

	public ToolbarView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.toolbar_subview, this);

		ButterKnife.bind(rootView);
	}

//    public void setText(String text){
//        tvAddress.setText(text);
//    }


}
