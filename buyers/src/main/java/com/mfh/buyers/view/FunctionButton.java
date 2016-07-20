package com.mfh.buyers.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.buyers.R;
import com.mfh.buyers.bean.FunctionCell;


public class FunctionButton extends RelativeLayout {
	private Context     context;
    private ImageView 	ivTop;
	private TextView    tvDescription;

    private Object data;

	public FunctionButton(Context context) {
		super(context);
		this.context = context;
		initView();
	}

	public FunctionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}

	private void initView() {
		View.inflate(context, R.layout.button_style_1, this);
		ivTop = (ImageView) this.findViewById(R.id.iv_top);
		tvDescription = (TextView) this.findViewById(R.id.tv_description);
	}

    public void setData(Object data) {
        this.data = data;
        FunctionCell cell = (FunctionCell)data;
        ivTop.setImageResource(cell.getPicResId());
        tvDescription.setText(cell.getDescription());
    }

    /**
     * 设置文本样式
     * */
    public void setTextAppearance(int resId){
        tvDescription.setTextAppearance(getContext(), resId);
    }

    public TextView getTvDescription(){
        return tvDescription;
    }


}
