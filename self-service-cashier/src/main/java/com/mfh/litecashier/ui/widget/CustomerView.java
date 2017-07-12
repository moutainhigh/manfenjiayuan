package com.mfh.litecashier.ui.widget;


import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.litecashier.R;


/**
 * 会员信息通用组件
 */
public class CustomerView extends RelativeLayout implements View.OnClickListener {
    private TextView tvCurCash, tvTopup, tvCurScore, tvDeduct, tvPhone;
    private AvatarView ivMemberHeader;

    private Human mHuman;

    public interface OnCustomerVierListener {
        void onClickTopup();

        void onClickDeduct();
    }

    private OnCustomerVierListener mOnCustomerVierListener;

    public CustomerView(Context context) {
        this(context, null);
    }

    public CustomerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(getContext(), R.layout.layout_customer, this);
        tvCurCash = (TextView) findViewById(R.id.tv_curcash);
        tvTopup = (TextView) findViewById(R.id.tv_topup);
        tvCurScore = (TextView) findViewById(R.id.tv_curscore);
        tvDeduct = (TextView) findViewById(R.id.tv_deduct);
        ivMemberHeader = (AvatarView) findViewById(R.id.iv_header);
        tvPhone = (TextView) findViewById(R.id.tv_phone);

        tvTopup.setOnClickListener(this);
        tvDeduct.setOnClickListener(this);
        ivMemberHeader.setBorderWidth(3);
        ivMemberHeader.setBorderColor(Color.parseColor("#e8e8e8"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_topup: {
                if (mOnCustomerVierListener != null) {
                    mOnCustomerVierListener.onClickTopup();
                }
            }
            break;
            case R.id.tv_deduct: {
                if (mOnCustomerVierListener != null) {
                    mOnCustomerVierListener.onClickDeduct();
                }
            }
            break;
        }
    }

    public void registerCustomerViewListener(OnCustomerVierListener listener) {
        mOnCustomerVierListener = listener;
        if (mOnCustomerVierListener != null) {
            tvTopup.setVisibility(VISIBLE);
            tvDeduct.setVisibility(VISIBLE);
        } else {
            tvTopup.setVisibility(GONE);
            tvDeduct.setVisibility(GONE);
        }
    }

    /**
     * 加载会员信息
     */
    public void reload(Human human) {
        try {
            mHuman = human;
            ZLogger.d(String.format("刷新会员信息：%s", JSONObject.toJSONString(human)));
            if (human != null) {
                tvCurCash.setText(Html.fromHtml(String.format("<font color=#000000>余额：</font><font color=#FE5000>%.2f</font>", human.getCurCash())));
                tvCurScore.setText(Html.fromHtml(String.format("<font color=#000000>积分：</font><font color=#FE5000>%d</font>", human.getCurScore())));
                ivMemberHeader.setAvatarUrl(human.getHeadimageUrl());
                tvPhone.setText(human.getMobile());

                if (mOnCustomerVierListener != null) {
                    tvTopup.setVisibility(VISIBLE);
                    tvDeduct.setVisibility(VISIBLE);
                } else {
                    tvTopup.setVisibility(GONE);
                    tvDeduct.setVisibility(GONE);
                }
            } else {
//                tvCurCash.setText(Html.fromHtml("<font color=#000000>余额：</font><font color=#FE5000>NA</font>"));
//                tvCurScore.setText(Html.fromHtml("<font color=#000000>积分：</font><font color=#FE5000>NA</font>"));
                ivMemberHeader.setImageResource(R.drawable.chat_tmp_user_head);
                tvPhone.setText("...");
                tvCurCash.setVisibility(GONE);
                tvCurScore.setVisibility(GONE);
                tvTopup.setVisibility(GONE);
                tvDeduct.setVisibility(GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
        }
    }

    public void setTvPhone(String phone) {
        tvPhone.setText(phone);
    }
}
