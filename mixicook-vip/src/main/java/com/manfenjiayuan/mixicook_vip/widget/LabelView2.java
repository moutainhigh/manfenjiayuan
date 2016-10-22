package com.manfenjiayuan.mixicook_vip.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.core.utils.DensityUtil;


/**
 * 支付方式
 *
 * @author bingshanguxue
 */
public class LabelView2 extends RelativeLayout {

    private ImageView ivStart, ivEnd;
    private TextView tvTitle, tvSubTitle, tvEnd;

    public interface  OnViewListener{
        void onClick(View v);
        void onCheckedChanged(boolean isChecked);
    }
    private OnViewListener mViewListener;
    public void setOnViewListener(OnViewListener viewListener){
        mViewListener = viewListener;
    }

    public LabelView2(Context context) {
        this(context, null);
    }

    public LabelView2(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.widget_labelview_2, this);

        ivStart = (ImageView)rootView.findViewById(R.id.iv_start);
        ivEnd = (ImageView) rootView.findViewById(R.id.iv_end);
        tvTitle = (TextView)rootView.findViewById(R.id.tv_title);
        tvSubTitle = (TextView)rootView.findViewById(R.id.tv_subtitle);
        tvEnd = (TextView)rootView.findViewById(R.id.tv_endText);


        if (attrs != null){
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LabelView2);
//
            tvTitle.setTextColor(ta.getColor(R.styleable.LabelView2_textColor, Color.BLACK));
            int textSizeInPx = ta.getDimensionPixelSize(R.styleable.LabelView2_textSize, 16);
            int textSizeInSp = DensityUtil.px2sp(getContext(), textSizeInPx);
            tvTitle.setTextSize(textSizeInSp);
            tvTitle.setText(ta.getString(R.styleable.LabelView2_text));

            tvSubTitle.setTextColor(ta.getColor(R.styleable.LabelView2_subTextColor, Color.BLACK));
            int subTextSizeInPx = ta.getDimensionPixelSize(R.styleable.LabelView2_subTextSize, 16);
            int subTextSizeInSp = DensityUtil.px2sp(getContext(), subTextSizeInPx);
            tvSubTitle.setTextSize(subTextSizeInSp);
            tvSubTitle.setText(ta.getString(R.styleable.LabelView2_subText));

            tvEnd.setTextColor(ta.getColor(R.styleable.LabelView2_endTextColor, Color.BLACK));
            int endTextSizeInPx = ta.getDimensionPixelSize(R.styleable.LabelView2_endTextSize, 16);
            int endTextSizeInSp = DensityUtil.px2sp(getContext(), endTextSizeInPx);
            tvEnd.setTextSize(endTextSizeInSp);
            tvEnd.setText(ta.getString(R.styleable.LabelView2_endText));

            this.ivStart.setImageResource(ta.getResourceId(R.styleable.LabelView2_src, 0));
            this.ivEnd.setImageResource(ta.getResourceId(R.styleable.LabelView2_endSrc,
                    R.drawable.icon_arrow_right));
            boolean isIvEndVisible = ta.getBoolean(R.styleable.LabelView2_endSrcVisible, true);
            if (isIvEndVisible) {
                ivEnd.setVisibility(View.VISIBLE);
            } else {
                ivEnd.setVisibility(View.INVISIBLE);
            }
            ta.recycle();
        }

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewListener != null){
                    mViewListener.onClick(v);
                }
            }
        });
    }

    public void setTitle(String title){
        tvTitle.setText(title);
    }

    public void setSubTitle(String subTitle){
        tvSubTitle.setText(subTitle);
    }

    public void setEndText(String endText){
        tvEnd.setText(endText);
    }

}
