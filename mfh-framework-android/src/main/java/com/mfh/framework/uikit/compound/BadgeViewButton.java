package com.mfh.framework.uikit.compound;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.framework.R;


/**
 *
 */
public class BadgeViewButton extends LinearLayout {
    private static final int MIN_NUM = 0;
    private static final int MAX_NUM = 99;
    private static final String TEXT_MAX_NUM = "99+";

    private ImageView 	ivButtonImage;
	private TextView    tvButtonText;
    private TextView    tvBadgeNumber;

	public BadgeViewButton(Context context) {
        this(context, null);
	}

	public BadgeViewButton(Context context, AttributeSet attrs) {
		super(context, attrs);
        View.inflate(getContext(), R.layout.view_badge_button, this);
        ivButtonImage = (ImageView) this.findViewById(R.id.iv_buttonImage);
        tvButtonText = (TextView) this.findViewById(R.id.tv_buttonText);
        tvBadgeNumber = (TextView) this.findViewById(R.id.tv_badgeNumber);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BadgeViewButton);

            tvButtonText.setText(a.getString(R.styleable.BadgeViewButton_text));
            tvButtonText.setTextColor(a.getColor(R.styleable.BadgeViewButton_textColor, Color.BLACK));
            tvButtonText.setTextSize(a.getDimension(R.styleable.BadgeViewButton_textSize, 16));
            ivButtonImage.setImageResource(a.getResourceId(R.styleable.BadgeViewButton_src, R.mipmap.ic_image_error));

            boolean isTextVisible = a.getBoolean(R.styleable.BadgeViewButton_textVisible, true);
            if (isTextVisible){
                tvButtonText.setVisibility(VISIBLE);
            }
            else{
                tvButtonText.setVisibility(GONE);
            }

            a.recycle();
        }
	}

    public void setBadgeNumber(int badgeNumber){
        if(badgeNumber > MIN_NUM){
            if(badgeNumber > MAX_NUM){
                this.tvBadgeNumber.setText(TEXT_MAX_NUM);
            }else{
                this.tvBadgeNumber.setText(String.valueOf(badgeNumber));
            }

            this.tvBadgeNumber.setVisibility(View.VISIBLE);
        }
        else{
            this.tvBadgeNumber.setVisibility(View.GONE);
        }
    }


}
