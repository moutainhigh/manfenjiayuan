package com.mfh.owner.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.owner.R;

/***
 *
 */
public class BadgeViewButton extends RelativeLayout {
    private static final int MIN_NUM = 0;
    private static final int MAX_NUM = 99;
    private static final String TEXT_MAX_NUM = "99+";

    private ImageView 	ivButtonImage;
	private TextView    tvButtonText;
    private TextView    tvBadgeNumber;

	public BadgeViewButton(Context context) {
		super(context);
        init();
	}

	public BadgeViewButton(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View.inflate(getContext(), R.layout.view_badge_button, this);
        ivButtonImage = (ImageView) this.findViewById(R.id.iv_buttonImage);
		tvButtonText = (TextView) this.findViewById(R.id.tv_buttonText);
        tvBadgeNumber = (TextView) this.findViewById(R.id.tv_badgeNumber);
	}

    public void init(int resId, String text){
        this.ivButtonImage.setImageResource(resId);
        this.tvButtonText.setText(text);
        this.tvBadgeNumber.setVisibility(View.GONE);
    }
    public void init(int imageResId, int titleResId){
        this.ivButtonImage.setImageResource(imageResId);
        this.tvButtonText.setText(titleResId);
        this.tvBadgeNumber.setVisibility(View.GONE);
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
