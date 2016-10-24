package com.bingshanguxue.vector_uikit;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.framework.core.utils.DensityUtil;


/***
 * 自定义View（组合原有安卓控件或者布局）
 * 样式：(Icon + Title/Name) + Detail
 * TODO:定义样式文件加载
 */
public class SettingsItem extends RelativeLayout {
    private ImageView ivLogo;
    private TextView tvTitle;
    private TextView tvSubTitle;
    private ImageView ivArrow;
    private View vSeperateTop;//上分割线
    private View vSeperateMiddle;//中分割线
    private View vSeperateBottom;//下分割线

    public SettingsItem(Context context) {
        this(context, null);
    }

    public SettingsItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(getContext(), R.layout.view_settings_item, this);
        ivLogo = (ImageView) findViewById(R.id.iv_logo);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvSubTitle = (TextView) findViewById(R.id.tv_subtitle);
        ivArrow = (ImageView) findViewById(R.id.iv_arrow_right);
        vSeperateTop = findViewById(R.id.separate_top);
        vSeperateMiddle = findViewById(R.id.separate_middle);
        vSeperateBottom = findViewById(R.id.separate_bottom);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingsItem);
            boolean isLogoVisible = ta.getBoolean(R.styleable.SettingsItem_isLogoVisible, true);
            if (isLogoVisible) {
                this.ivLogo.setVisibility(View.VISIBLE);
            } else {
                this.ivLogo.setVisibility(View.GONE);
            }

            this.ivLogo.setImageResource(ta.getResourceId(R.styleable.SettingsItem_src, 0));

            tvTitle.setText(ta.getString(R.styleable.SettingsItem_text));
            tvTitle.setTextColor(ta.getColor(R.styleable.SettingsItem_textColor, Color.BLACK));
            int textSizeInPx = ta.getDimensionPixelSize(R.styleable.SettingsItem_textSize, 16);
            int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
            tvTitle.setTextSize(textSizeInSp);
            tvSubTitle.setText(ta.getString(R.styleable.SettingsItem_subText));
            tvSubTitle.setTextColor(ta.getColor(R.styleable.SettingsItem_subTextColor, Color.BLACK));
            int sbuTextSizeInPx = ta.getDimensionPixelSize(R.styleable.SettingsItem_subTextSize, 16);
            int sbuTextSizeInSp = DensityUtil.px2sp(getContext(), sbuTextSizeInPx);
            tvSubTitle.setTextSize(sbuTextSizeInSp);
            this.tvSubTitle.setGravity(ta.getInteger(R.styleable.SettingsItem_subTextGravity, Gravity.RIGHT));
            int subTextMaxLines = ta.getInt(R.styleable.SettingsItem_subTextMaxLines, 1);
            this.tvSubTitle.setMaxLines(subTextMaxLines);
            if (subTextMaxLines > 1){
                this.tvSubTitle.setSingleLine(false);
            }
            else{
                this.tvSubTitle.setSingleLine(true);
            }
            this.tvSubTitle.setEllipsize(TextUtils.TruncateAt.END);

            boolean isArrowVisible = ta.getBoolean(R.styleable.SettingsItem_isArrowVisible, true);
            if (isArrowVisible) {
                ivArrow.setVisibility(View.VISIBLE);
            } else {
                ivArrow.setVisibility(View.INVISIBLE);
            }


            int position = ta.getInt(R.styleable.SettingsItem_position, 3);
            if (position == 0) {
                vSeperateTop.setVisibility(View.VISIBLE);
                vSeperateMiddle.setVisibility(View.VISIBLE);
                vSeperateBottom.setVisibility(View.GONE);
            } else if (position == 1) {
                vSeperateTop.setVisibility(View.GONE);
                vSeperateMiddle.setVisibility(View.VISIBLE);
                vSeperateBottom.setVisibility(View.GONE);
            } else if (position == 2) {
                vSeperateTop.setVisibility(View.GONE);
                vSeperateMiddle.setVisibility(View.GONE);
                vSeperateBottom.setVisibility(View.VISIBLE);
            } else {
                vSeperateTop.setVisibility(View.VISIBLE);
                vSeperateMiddle.setVisibility(View.GONE);
                vSeperateBottom.setVisibility(View.VISIBLE);
            }

            ta.recycle();
        }
    }

    public void setTitle(String title){
        tvTitle.setText(title);
    }
    public void setSubTitle(String text){
        tvSubTitle.setText(text);
    }

    public void setSubTitle(CharSequence text){
        tvSubTitle.setText(text);
    }

    public void setSubTitle(String text, int color){
        tvSubTitle.setText(text);
        tvSubTitle.setTextColor(color);
    }

    public String getSubText(){
        return tvSubTitle.getText().toString();
    }
}
