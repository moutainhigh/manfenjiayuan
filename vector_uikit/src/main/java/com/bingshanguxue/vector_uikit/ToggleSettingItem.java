package com.bingshanguxue.vector_uikit;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.framework.core.utils.DensityUtil;


/***
 * 自定义View（组合原有安卓控件或者布局）
 * 样式：(Icon + Title/Name) + Detail
 */
public class ToggleSettingItem extends RelativeLayout {
    private ImageView ivLogo;
    private TextView tvTitle;
    private TextView tvSubTitle;
    private SwitchCompat buttonToggle;
    private View vSeperateTop;//上分割线
    private View vSeperateMiddle;//中分割线
    private View vSeperateBottom;//下分割线

    public interface OnViewListener {
        void onToggleChanged(boolean isChecked);
    }

    private OnViewListener listener;


    public ToggleSettingItem(Context context) {
        this(context, null);
    }

    public ToggleSettingItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(getContext(), R.layout.settings_item_toggle, this);
        this.ivLogo = (ImageView) findViewById(R.id.iv_logo);
        this.tvTitle = (TextView) findViewById(R.id.tv_title);
        this.tvSubTitle = (TextView) findViewById(R.id.tv_subtitle);
        this.buttonToggle = (SwitchCompat) findViewById(R.id.button_toggle);
        buttonToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.onToggleChanged(isChecked);
                }
            }
        });
        vSeperateTop = findViewById(R.id.separate_top);
        vSeperateMiddle = findViewById(R.id.separate_middle);
        vSeperateBottom = findViewById(R.id.separate_bottom);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToggleSettingItem);
            boolean isLogoVisible = a.getBoolean(R.styleable.ToggleSettingItem_isLogoVisible, true);
            if (isLogoVisible) {
                this.ivLogo.setVisibility(View.VISIBLE);
            } else {
                this.ivLogo.setVisibility(View.GONE);
            }
            this.ivLogo.setImageResource(a.getResourceId(R.styleable.ToggleSettingItem_src, 0));

            this.tvTitle.setText(a.getString(R.styleable.ToggleSettingItem_text));
            this.tvTitle.setTextColor(a.getColor(R.styleable.ToggleSettingItem_textColor, Color.BLACK));
            int textSizeInPx = a.getDimensionPixelSize(R.styleable.ToggleSettingItem_textSize, 16);
            int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
            this.tvTitle.setTextSize(textSizeInSp);

            this.tvSubTitle.setText(a.getString(R.styleable.ToggleSettingItem_subText));
            this.tvSubTitle.setTextColor(a.getColor(R.styleable.ToggleSettingItem_subTextColor, Color.BLACK));
            int sbuTextSizeInPx = a.getDimensionPixelSize(R.styleable.ToggleSettingItem_subTextSize, 12);
            int sbuTextSizeInSp = DensityUtil.px2sp(getContext(), sbuTextSizeInPx);
            this.tvSubTitle.setTextSize(sbuTextSizeInSp);
            this.tvSubTitle.setGravity(a.getInteger(R.styleable.ToggleSettingItem_subTextGravity,
                    Gravity.CENTER_VERTICAL | Gravity.END));
            int subTextMaxLines = a.getInt(R.styleable.ToggleSettingItem_subTextMaxLines, 1);
            this.tvSubTitle.setMaxLines(subTextMaxLines);
            if (subTextMaxLines > 1) {
                this.tvSubTitle.setSingleLine(false);
            } else {
                this.tvSubTitle.setSingleLine(true);
            }
            this.tvSubTitle.setEllipsize(TextUtils.TruncateAt.END);

            int position = a.getInt(R.styleable.ToggleSettingItem_position, 3);
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

            a.recycle();
        }
    }

    /**
     * 初始化
     */
    public void init(OnViewListener listener) {
        this.listener = listener;
    }

    public void setChecked(boolean checked) {
        this.buttonToggle.setChecked(checked);
    }

    public boolean getChecked(){
        return buttonToggle.isChecked();
    }

    public void setSubTitle(String text) {
        tvSubTitle.setText(text);
    }

    public String getSubTitle(){
        return tvSubTitle.getText().toString();
    }
}
