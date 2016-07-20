package com.mfh.enjoycity.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.enjoycity.R;

import butterknife.Bind;
import butterknife.ButterKnife;


/***
 * 店铺订单商品
 */
public class CategoryItem extends LinearLayout {
	@Bind(R.id.tv_name)
	TextView tvName;
	@Bind(R.id.iv_arrow)
	ImageView ivArrow;

	View rootView;


	public CategoryItem(Context context) {
		super(context);
        init();
	}

	public CategoryItem(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		rootView = View.inflate(getContext(), R.layout.view_item_subcategory, this);
		ButterKnife.bind(rootView);
	}

	public void setTvName(String text, int color){
		tvName.setText(text);
		tvName.setTextColor(color);
	}

	public void setViewBackground(int color){
//		rootView.setBackgroundColor(color);
		rootView.setBackgroundResource(R.color.black_40);
//		rootView.setBackground(getResources().getColor(R.color.black_40));
	}

}
