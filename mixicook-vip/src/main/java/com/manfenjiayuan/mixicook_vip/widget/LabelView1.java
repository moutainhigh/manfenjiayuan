package com.manfenjiayuan.mixicook_vip.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;


/**
 * 支付方式
 *
 * @author bingshanguxue
 */
public class LabelView1 extends RelativeLayout {

    private ImageView ivLogo, ivLabel;
    private TextView tvTitle, tvSubTitle;
    private CheckBox mCheckBox;

    public interface  OnViewListener{
        void onClick(View v, boolean isChecked);
        void onClickCheck(boolean isChecked);
        void onCheckedChanged(boolean isChecked);
    }
    private OnViewListener mViewListener;
    public void setOnViewListener(OnViewListener viewListener){
        mViewListener = viewListener;
    }

    public LabelView1(Context context) {
        this(context, null);
    }

    public LabelView1(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.widget_labelview_1, this);

        ivLogo = (ImageView)rootView.findViewById(R.id.iv_logo);
        ivLabel = (ImageView) rootView.findViewById(R.id.iv_label);
        tvTitle = (TextView)rootView.findViewById(R.id.tv_title);
        tvSubTitle = (TextView)rootView.findViewById(R.id.tv_subtitle);
        mCheckBox = (CheckBox) rootView.findViewById(R.id.checkbox);

        if (attrs != null){
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LabelView1);
//
            tvTitle.setTextColor(ta.getColor(R.styleable.LabelView1_textColor, Color.BLACK));
            int textSizeInPx = ta.getDimensionPixelSize(R.styleable.LabelView1_textSize, 16);
            int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
            tvTitle.setTextSize(textSizeInSp);
            tvTitle.setText(ta.getString(R.styleable.LabelView1_text));


            tvSubTitle.setTextColor(ta.getColor(R.styleable.LabelView1_subTextColor, Color.BLACK));
            int subTextSizeInPx = ta.getDimensionPixelSize(R.styleable.LabelView1_subTextSize, 16);
            int subTextSizeInSp = DensityUtil.px2sp(getContext(), subTextSizeInPx);
            tvSubTitle.setTextSize(subTextSizeInSp);
            tvSubTitle.setText(ta.getString(R.styleable.LabelView1_subText));

            this.ivLogo.setImageResource(ta.getResourceId(R.styleable.LabelView1_src, 0));
            this.ivLabel.setImageResource(ta.getResourceId(R.styleable.LabelView1_labelSrc, 0));
            mCheckBox.setChecked(ta.getBoolean(R.styleable.LabelView1_isChecked, false));
            ta.recycle();
        }

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZLogger.d("click checkbox's parentview：" + isChecked());
//                mCheckBox.setChecked(!mCheckBox.isChecked());

                if (mViewListener != null){
                    mViewListener.onClick(v, isChecked());
                }
            }
        });
        mCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ZLogger.d("click checkbox：" + isChecked());
                if (mViewListener != null){
                    mViewListener.onClickCheck(isChecked());
                }
            }
        });
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mViewListener != null){
                    mViewListener.onCheckedChanged(isChecked);
                }
            }
        });
    }

    public void setChecked(boolean checked){
        mCheckBox.setChecked(checked);
    }

    public boolean isChecked(){
        return mCheckBox.isChecked();
    }
}
