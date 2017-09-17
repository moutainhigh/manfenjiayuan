package com.mfh.litecashier.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.AppHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by bingshanguxue on 4/8/16.
 */
public class PayProcessView extends RelativeLayout {
    @BindView(R.id.tv_countdown)
    TextView tvCountdown;
    @BindView(R.id.animProgress)
    ProgressBar progressBar;
    @BindView(R.id.tv_process)
    TextView tvProcess;
    @BindView(R.id.button_action1)
    Button btnAction1;
    @BindView(R.id.button_action2)
    Button btnAction2;
    @BindView(R.id.button_action3)
    Button btnAction3;

    private View rootView;
    private PayCountDownTimer payCountDownTimer;
    private boolean payTimerRunning;

    private int countdownTime = 30;//by seconds
    private boolean isAction1Enabled, isAction2Enabled, isAction3Enabled;

    public interface onCustomerViewListener {
        void onAction1();

        void onAction2();
        void onAction3();
    }

    private onCustomerViewListener onIViewListener;

    public PayProcessView(Context context) {
        this(context, null);
    }

    public PayProcessView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rootView = View.inflate(getContext(), R.layout.fragment_pay_process, this);

        ButterKnife.bind(rootView);

        payCountDownTimer = new PayCountDownTimer(countdownTime * 1000, 1000);
    }

    @OnClick(R.id.button_action1)
    public void action1() {
        this.setVisibility(GONE);
        if (onIViewListener != null) {
            onIViewListener.onAction1();
        }
    }

    @OnClick(R.id.button_action2)
    public void action2() {
        this.setVisibility(GONE);
        if (onIViewListener != null) {
            onIViewListener.onAction2();
        }
    }

    @OnClick(R.id.button_action3)
    public void action3() {
        this.setVisibility(GONE);
        if (onIViewListener != null) {
            onIViewListener.onAction3();
        }
    }

    public void init(int countdownTime, onCustomerViewListener onIViewListener) {
        this.countdownTime = countdownTime;
        this.onIViewListener = onIViewListener;

        this.payCountDownTimer = new PayCountDownTimer(countdownTime * 1000, 1000);
    }

    public void init(int coundownTime, String text1, String text2, String text3, onCustomerViewListener listener) {
        this.isAction1Enabled = !StringUtils.isEmpty(text1);
        this.btnAction1.setText(text1);
        this.isAction2Enabled = !StringUtils.isEmpty(text2);
        this.btnAction2.setText(text2);
        this.isAction3Enabled = !StringUtils.isEmpty(text3);
        this.btnAction3.setText(text3);

        this.countdownTime = coundownTime;
        this.onIViewListener = listener;

        this.payCountDownTimer = new PayCountDownTimer(coundownTime * 1000, 1000);
    }

    public static final int STATE_INIT = 0;
    public static final int STATE_PROCESS = 1;
    public static final int STATE_FAILED = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_SUCCESS = 4;

    public void setState(int state, String message) {
        try {
            switch (state) {
                case STATE_INIT: {
                    tvProcess.setText(message);
                    tvProcess.setTextColor(Color.parseColor("#FF000000"));

                    progressBar.setVisibility(View.GONE);
                    btnAction1.setVisibility(View.GONE);
                    btnAction2.setVisibility(View.GONE);
                    btnAction3.setVisibility(View.GONE);

                    tvCountdown.setText("");
                    tvCountdown.setVisibility(GONE);
                    cancelCountdownTimer();
                    this.setVisibility(View.GONE);
                }
                break;
                case STATE_PROCESS: {
                    tvProcess.setText(message);
                    tvProcess.setTextColor(Color.parseColor("#FF000000"));

                    progressBar.setVisibility(View.VISIBLE);
                    btnAction1.setVisibility(View.GONE);
                    btnAction2.setVisibility(View.GONE);
                    btnAction3.setVisibility(View.GONE);

                    tvCountdown.setText("");
                    tvCountdown.setVisibility(VISIBLE);

                    startCountdownTimer();
                    this.setVisibility(View.VISIBLE);
                }
                break;
                case STATE_SUCCESS: {
                    tvProcess.setText(message);
                    tvProcess.setTextColor(AppHelper.getOkTextColor());

                    progressBar.setVisibility(View.GONE);
                    btnAction1.setVisibility(View.GONE);
                    btnAction2.setVisibility(View.GONE);
                    btnAction3.setVisibility(View.GONE);

                    tvCountdown.setText("");
                    tvCountdown.setVisibility(GONE);
                    cancelCountdownTimer();
                }
                break;
                case STATE_ERROR: {
                    if (isAction2Enabled) {
                        btnAction2.setVisibility(View.VISIBLE);
                    } else {
                        btnAction2.setVisibility(View.GONE);
                    }

                    if (isAction3Enabled) {
                        btnAction3.setVisibility(View.VISIBLE);
                    } else {
                        btnAction3.setVisibility(View.GONE);
                    }
                }
                case STATE_FAILED: {
                    if (isAction1Enabled) {
                        btnAction1.setVisibility(View.VISIBLE);
                    } else {
                        btnAction1.setVisibility(View.GONE);
                    }

                    tvProcess.setText(message);
                    tvProcess.setTextColor(AppHelper.getErrorTextColor());

                    progressBar.setVisibility(View.GONE);

                    tvCountdown.setText("");
                    tvCountdown.setVisibility(GONE);
                    cancelCountdownTimer();
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }

    }

    private void startCountdownTimer() {
        if (payCountDownTimer == null) {
            payCountDownTimer = new PayCountDownTimer(countdownTime * 1000, 1000);
        }
        if (!payTimerRunning) {
            payCountDownTimer.start();
        }
        payTimerRunning = true;
    }

    private void cancelCountdownTimer() {
        if (payCountDownTimer != null) {
            payCountDownTimer.cancel();
            payCountDownTimer = null;
        }
        payTimerRunning = false;
    }

    /**
     * 销毁
     */
    public void onDestory() {
        cancelCountdownTimer();
    }

    /**
     * 倒计时
     */
    private class PayCountDownTimer extends CountDownTimer {

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
