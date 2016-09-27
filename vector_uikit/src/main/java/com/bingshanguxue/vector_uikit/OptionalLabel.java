package com.bingshanguxue.vector_uikit;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.framework.core.utils.DensityUtil;


/**
 * 复合控件－－（输入框 + 按键）
 * 支持自定义属性，可以直接在xml文件中配置。
 * * <declare-styleable name="OptionalLabel">
 *     <attr name="editQueryView_inputText" format="string" />
 *     <attr name="editQueryView_inputTextColor" format="color" />
 *     <attr name="editQueryView_inputTextSize" format="dimension" />
 *     <attr name="editQueryView_inputHint" format="string" />
 *     <attr name="editQueryView_rightButtonWidth" format="dimension" />
 *     <attr name="editLabelView_rightImageButtonSrc" format="reference" />
 *   </declare-styleable>
 */
public class OptionalLabel extends RelativeLayout {

    private View rootView;
    private TextView tvLabel;
    private ImageButton ibDel;

    private boolean isRightButtonEnabled = true;

    public interface OnViewListener{
        void onClickDel();
    }
    private OnViewListener onViewListener;
    public void setOnViewListener(OnViewListener onViewListener){
        this.onViewListener = onViewListener;
    }

    public OptionalLabel(Context context) {
        this(context, null);
    }

    public OptionalLabel(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.mfh_optional_label_view, this);
        tvLabel = (TextView) rootView.findViewById(R.id.tv_label);
        ibDel = (ImageButton) rootView.findViewById(R.id.ib_del);

        if (attrs != null){
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.OptionalLabel);
            float inputTextSizeInPx = ta.getDimension(R.styleable.OptionalLabel_mLabelView_inputTextSize, 16);
            int inputTextSizeInSp = DensityUtil.px2sp(getContext(), inputTextSizeInPx);

            int inputTextColor = ta.getColor(R.styleable.OptionalLabel_mLabelView_inputTextColor, 0);
            int inputTextColorHint = ta.getColor(R.styleable.OptionalLabel_mLabelView_inputTextColorHint, 0);
            String inputHint = ta.getString(R.styleable.OptionalLabel_mLabelView_inputHint);
            isRightButtonEnabled = ta.getBoolean(R.styleable.OptionalLabel_mLabelView_rightImageButtonEnabled, true);
            ta.recycle();

            tvLabel.setTextSize(inputTextSizeInSp);
            tvLabel.setTextColor(inputTextColor);
            tvLabel.setHintTextColor(inputTextColorHint);
            tvLabel.setHint(inputHint);
        }

        tvLabel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isRightButtonEnabled && s.length() > 0) {
                    ibDel.setVisibility(VISIBLE);
                } else {
                    ibDel.setVisibility(GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvLabel.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (isRightButtonEnabled && hasFocus && tvLabel.getText().length() > 0){
                    ibDel.setVisibility(VISIBLE);
                } else {
                    ibDel.setVisibility(GONE);
                }
            }
        });

        ibDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLabel.setText("");
                if (onViewListener != null) {
                    onViewListener.onClickDel();
                }
            }
        });
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
//        this.rootView.setEnabled(enabled);
        this.tvLabel.setEnabled(enabled);

        if (isRightButtonEnabled){
            this.ibDel.setEnabled(enabled);
            if (!enabled){
                this.ibDel.setVisibility(GONE);
            }
        }
    }

    public String getLabelText() {
        return tvLabel.getText().toString();
    }

    public void setLabelText(String text) {
        this.tvLabel.setText(text);
    }

    public void setHintText(String hintText) {
        this.tvLabel.setHint(hintText);
    }
}
