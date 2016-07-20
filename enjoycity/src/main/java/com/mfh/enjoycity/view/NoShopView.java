package com.mfh.enjoycity.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.framework.login.logic.MfhLoginService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/***
 * 店铺为空
 */
public class NoShopView extends LinearLayout {

	@Bind(R.id.btn_select)
	Button btnSelect;

	public interface ViewListener{
		void onSearch();
	}
	private ViewListener listener;
	public void setViewListener(ViewListener listener){
		this.listener = listener;
	}

	@Bind(R.id.tv_hint) TextView tvHint;

	public NoShopView(Context context) {
		super(context);
        init();
	}

	public NoShopView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.view_no_shop, this);
		ButterKnife.bind(rootView);
	}

	public void setTvHint(String text){
		tvHint.setText(text);
	}

	@OnClick(R.id.btn_select)
	public void selectOtherBranches(){
		if (listener != null){
			listener.onSearch();
		}
	}

	public void refresh(){
		if (MfhLoginService.get().haveLogined()){
			btnSelect.setVisibility(GONE);
		}else{
			btnSelect.setVisibility(VISIBLE);
		}
	}

}
