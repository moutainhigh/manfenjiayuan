package com.bingshanguxue.vector_uikit;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.framework.core.utils.DensityUtil;
import com.bingshanguxue.vector_uikit.widget.AvatarView;


/**
 * 用户信息
 *
 * @author bingshanguxue
 */
public class ProfileView extends RelativeLayout {

    private AvatarView mAvatarView;
    private TextView tvPrimary;
    private TextView tvSecondary;
    private ImageView ivArrow;

    public ProfileView(Context context) {
        this(context, null);
    }

    public ProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.widget_profile, this);

        mAvatarView = (AvatarView)rootView.findViewById(R.id.iv_header);
        tvPrimary = (TextView)rootView.findViewById(R.id.tv_primary);
        tvSecondary = (TextView)rootView.findViewById(R.id.tv_secondary);
        ivArrow = (ImageView) rootView.findViewById(R.id.iv_arrow_end);


        if (attrs != null){
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProfileView);
//
            tvPrimary.setTextColor(ta.getColor(R.styleable.ProfileView_textColor, Color.BLACK));
            int textSizeInPx = ta.getDimensionPixelSize(R.styleable.ProfileView_textSize, 16);
            int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
            tvPrimary.setTextSize(textSizeInSp);

            tvSecondary.setTextColor(ta.getColor(R.styleable.ProfileView_subTextColor, Color.BLACK));
            int subTextSizeInPx = ta.getDimensionPixelSize(R.styleable.ProfileView_subTextSize, 16);
            int subTextSizeInSp = DensityUtil.px2sp(getContext(), subTextSizeInPx);
            tvSecondary.setTextSize(subTextSizeInSp);


            this.mAvatarView.setImageResource(ta.getResourceId(R.styleable.ProfileView_src, 0));
            mAvatarView.setBorderWidth(ta.getDimensionPixelSize(R.styleable.ProfileView_borderWidth, 3));
            mAvatarView.setBorderColor(ta.getColor(R.styleable.ProfileView_borderColor,
                    Color.parseColor("#e8e8e8")));

            boolean isIvEndVisible = ta.getBoolean(R.styleable.ProfileView_endArrowVisible, true);
            if (isIvEndVisible) {
                ivArrow.setVisibility(View.VISIBLE);
            } else {
                ivArrow.setVisibility(View.INVISIBLE);
            }

            ta.recycle();
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
