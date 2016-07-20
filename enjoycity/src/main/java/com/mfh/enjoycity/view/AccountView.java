package com.mfh.enjoycity.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.enjoycity.R;

import butterknife.Bind;
import butterknife.ButterKnife;


/***
 * 账户信息
 */
public class AccountView extends RelativeLayout {
	@Bind(R.id.tv_address)
	TextView tvAddress;

	public AccountView(Context context) {
		super(context);
        init();
	}

	public AccountView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.view_navi_address, this);

		ButterKnife.bind(rootView);
	}

    public void setText(String text){
        tvAddress.setText(text);
    }


}
