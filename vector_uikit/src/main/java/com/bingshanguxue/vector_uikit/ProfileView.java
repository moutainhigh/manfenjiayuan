package com.bingshanguxue.vector_uikit;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.framework.uikit.widget.AvatarView;


/**
 * 用户信息
 *
 * @author bingshanguxue
 */
public class ProfileView extends RelativeLayout {

    private AvatarView mAvatarView;
    private TextView tvPrimary;
    private TextView tvSecondary;

    public ProfileView(Context context) {
        this(context, null);
    }

    public ProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.widget_profile, this);

        mAvatarView = (AvatarView)rootView.findViewById(R.id.iv_header);
        tvPrimary = (TextView)rootView.findViewById(R.id.tv_primary);
        tvSecondary = (TextView)rootView.findViewById(R.id.tv_secondary);

        mAvatarView.setBorderWidth(3);
        mAvatarView.setBorderColor(Color.parseColor("#e8e8e8"));

        if (attrs != null){
//            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSearchView);
//
//            etQueryText.setTextColor(a.getColor(R.styleable.CustomSearchView_textColor, Color.BLACK));
//            etQueryText.setHint(a.getString(R.styleable.CustomSearchView_hint));
//            etQueryText.setHintTextColor(a.getColor(R.styleable.CustomSearchView_textColorHint, Color.BLACK));
//
//            a.recycle();
        }
    }

    public void setAvatarUrl(String url) {
        mAvatarView.setAvatarUrl(url);
    }

    public void setPrimaryText(String name) {
        tvPrimary.setText(name);
    }


    public void setSecondaryText(String phone) {
        tvSecondary.setText(phone);
    }
}
