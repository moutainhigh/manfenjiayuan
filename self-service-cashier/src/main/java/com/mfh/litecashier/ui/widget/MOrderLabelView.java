package com.mfh.litecashier.ui.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.litecashier.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 复合控件－－（文本 + 排序）{@link com.mfh.litecashier.R.mipmap#ic_order_asc_normal}
 * 支持自定义属性，可以直接在xml文件中配置。
 * * <declare-styleable name="EditQueryView">
 *     <attr name="editQueryView_inputText" format="string" />
 *     <attr name="editQueryView_inputTextColor" format="color" />
 *     <attr name="editQueryView_inputTextSize" format="dimension" />
 *     <attr name="editQueryView_inputHint" format="string" />
 *     <attr name="editQueryView_rightButtonWidth" format="dimension" />
 *     <attr name="editLabelView_rightImageButtonSrc" format="reference" />
 *   </declare-styleable>
 */
public class MOrderLabelView extends LinearLayout {
    private static final String TAG = "MOrderLabelView";

    public static final int ORDER_STATUS_DESC_NORMAL = 0;
    public static final int ORDER_STATUS_DESC_SELECTED = 1;
    public static final int ORDER_STATUS_ASC_NORMAL = 2;
    public static final int ORDER_STATUS_ASC_SELECTED = 3;


    @BindView(R.id.tv_label)
    TextView tvLabel;
    @BindView(R.id.iv_orderstatus)
    ImageView ivOrderStatus;

    private int currentOrderStatus = ORDER_STATUS_DESC_NORMAL;

    public MOrderLabelView(Context context) {
        this(context, null);
    }

    public MOrderLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.mfh_order_labelview, this);

        ButterKnife.bind(rootView);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MOrderLabelView);

        float textSize = ta.getDimension(R.styleable.MOrderLabelView_mOrderLabelView_textSize, 16);
        int textColor = ta.getColor(R.styleable.MOrderLabelView_mOrderLabelView_textColor, 0);
        String text = ta.getString(R.styleable.MOrderLabelView_mOrderLabelView_text);

        ta.recycle();

        tvLabel.setText(text);
        tvLabel.setTextSize(textSize);
        tvLabel.setTextColor(textColor);
    }

    public void setOrderEnabled(boolean enabled){
        super.setSelected(enabled);
        if (enabled){
            switch (currentOrderStatus){
                case ORDER_STATUS_DESC_NORMAL:
                case ORDER_STATUS_ASC_SELECTED:
                    currentOrderStatus = ORDER_STATUS_DESC_SELECTED;
                    ivOrderStatus.setImageResource(R.mipmap.ic_order_desc_selected);
                    break;
                case ORDER_STATUS_ASC_NORMAL:
                case ORDER_STATUS_DESC_SELECTED:
                    currentOrderStatus = ORDER_STATUS_ASC_SELECTED;
                    ivOrderStatus.setImageResource(R.mipmap.ic_order_asc_selected);
                    break;
            }
        }
        else{
            switch (currentOrderStatus){
                case ORDER_STATUS_DESC_NORMAL:
                case ORDER_STATUS_DESC_SELECTED:
                    currentOrderStatus = ORDER_STATUS_DESC_NORMAL;
                    ivOrderStatus.setImageResource(R.mipmap.ic_order_desc_normal);
                    break;
                case ORDER_STATUS_ASC_NORMAL:
                case ORDER_STATUS_ASC_SELECTED:
                    currentOrderStatus = ORDER_STATUS_ASC_NORMAL;
                    ivOrderStatus.setImageResource(R.mipmap.ic_order_asc_normal);
                    break;
            }
        }
    }

    public int getCurrentOrderStatus() {
        return currentOrderStatus;
    }

}
