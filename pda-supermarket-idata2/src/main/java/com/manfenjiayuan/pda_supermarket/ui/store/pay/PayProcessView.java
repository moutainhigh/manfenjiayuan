package com.manfenjiayuan.pda_supermarket.ui.store.pay;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by bingshanguxue on 4/8/16.
 */
public class PayProcessView extends RelativeLayout{
    @Bind(R.id.tv_countdown)
    TextView tvCountdown;
    @Bind(R.id.animProgress)
    ProgressBar progressBar;
    @Bind(R.id.tv_process)
    TextView tvProcess;
    @Bind(R.id.fab_cancel)
    FloatingActionButton fabCancel;
    @Bind(R.id.fab_refresh)
    FloatingActionButton fabRefresh;

    private View rootView;
    private PayCountDownTimer payCountDownTimer;
    private boolean payTimerRunning;

    private int countdownTime = 30;//by seconds

    public interface OnIViewListener{
        void onCancel();
        void onRefresh();
    }
    private OnIViewListener onIViewListener;

    public PayProcessView(Context context) {
        this(context, null);
    }

    public PayProcessView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rootView = View.inflate(getContext(), R.layout.fragment_pay_process, this);

        ButterKnife.bind(rootView);

        payCountDownTimer = new PayCountDownTimer(countdownTime * 1000, 1000);
    }

    @OnClick(R.id.fab_cancel)
    public void cancel(){
        this.setVisibility(GONE);
        if (onIViewListener != null){
            onIViewListener.onCancel();
        }
    }

    @OnClick(R.id.fab_refresh)
    public void refresh(){
        if (onIViewListener != null){
            onIViewListener.onRefresh();
        }
    }

    public void init(int countdownTime, OnIViewListener onIViewListener){
        this.countdownTime = countdownTime;
        this.onIViewListener = onIViewListener;

        payCountDownTimer = new PayCountDownTimer(countdownTime * 1000, 1000);

    }
    public static final int STATE_INIT = 0;
    public static final int STATE_PROCESS = 1;
    public static final int STATE_FAILED = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_SUCCESS = 4;
    public void setState(int state, String message){
        tvProcess.setText(message);

        switch (state){
            case STATE_PROCESS:{
                tvProcess.setTextColor(Color.parseColor("#FF000000"));

                progressBar.setVisibility(View.VISIBLE);
                fabCancel.setVisibility(View.GONE);
                fabRefresh.setVisibility(View.GONE);

                if (!payTimerRunning) {
                    tvCountdown.setText("");
                    tvCountdown.setVisibility(VISIBLE);
                    payCountDownTimer.start();
                    payTimerRunning = true;
                }

                this.setVisibility(View.VISIBLE);
            }
            break;
            case STATE_SUCCESS:{
                tvProcess.setTextColor(AppHelper.getOkTextColor());

                progressBar.setVisibility(View.GONE);
                fabCancel.setVisibility(View.GONE);
                fabRefresh.setVisibility(View.GONE);

                tvCountdown.setText("");
                tvCountdown.setVisibility(GONE);
                payCountDownTimer.cancel();
                payTimerRunning = false;
            }
            break;
            case STATE_FAILED:
            case STATE_ERROR:{
                tvProcess.setTextColor(AppHelper.getErrorTextColor());

                progressBar.setVisibility(View.GONE);
                fabCancel.setVisibility(View.VISIBLE);
                fabRefresh.setVisibility(View.VISIBLE);

                tvCountdown.setText("");
                tvCountdown.setVisibility(GONE);
                payCountDownTimer.cancel();
                payTimerRunning = false;
            }
            break;
        }
    }

    /**
     * 销毁
     * */
    public void onDestory(){
        if (payCountDownTimer != null){
            payCountDownTimer.cancel();
            payCountDownTimer = null;
        }
        payTimerRunning = false;
    }

    /**
     * 倒计时
     */
    public class PayCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public PayCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvCountdown.setText(String.format("%d秒", millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            tvCountdown.setText("");
//            btnCancelAliBarPay.setVisibility(View.VISIBLE);
        }
    }
}
