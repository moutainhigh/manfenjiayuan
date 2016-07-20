package com.mfh.framework.uikit.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.framework.R;


/**
 * 对话框－－同步数据
 * 
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class ProgressDialog extends CommonDialog {
    public final static int STATUS_NA           = 0;
    public final static int STATUS_PROCESSING   = 1;
    public final static int STATUS_DONE         = 2;
    public final static int STATUS_ERROR        = 3;

    private View rootView;
    private ProgressBar mProgressBar;
    private TextView mMessageView;
    private ImageView mImageView;
    private int status = STATUS_NA;//状态
    private String processText;
    private String doneText;
    private String errorText;
    private int autoDismissDelay = 2;//by second
    private boolean isAutoDismissEnabled = false;


    private ProgressDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ProgressDialog(Context context, int defStyle) {
        super(context, defStyle);
//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        View view = inflater.inflate(a.getResourceId(
//                com.android.internal.R.styleable.AlertDialog_progressLayout,
//                R.layout.progress_dialog), null);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_progress, null);
//        ButterKnife.bind(rootView);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        mMessageView = (TextView) rootView.findViewById(R.id.tv_meaasge);
        mImageView = (ImageView) rootView.findViewById(R.id.iv_status);

        setContent(rootView, 0);
    }

    public ProgressDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);


//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);
    }

    @Override
    public void show() {
        super.show();
    }

    /**
     * 设置对话框各个状态的提示文字，一般适用在只显示一个对话框的情况。
     * */
    public void init(String processText, String doneText, String errorText){
        this.status = STATUS_NA;
        this.processText = processText;
        this.doneText = doneText;
        this.errorText = errorText;
    }

    public void init(String processText, String doneText, String errorText, boolean isAutoDismissEnabled, int autoDismissDelay){
        this.status = STATUS_NA;
        this.processText = processText;
        this.doneText = doneText;
        this.errorText = errorText;
        this.isAutoDismissEnabled = isAutoDismissEnabled;
        this.autoDismissDelay = autoDismissDelay;
    }

    /**
     * 更新进度状态
     * */
    public void setProgress(int status){
        this.status = status;
        switch (status){
            case STATUS_PROCESSING:{
                mProgressBar.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.GONE);
                mMessageView.setText(processText);
            }
            break;
            case STATUS_DONE:{
                mProgressBar.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(R.mipmap.ic_process_done);
                mMessageView.setText(doneText);
                //TODO,秒后关闭窗口
                if (isAutoDismissEnabled){
                    autoDismiss();
                }
            }
            break;
            case STATUS_ERROR:{
                mProgressBar.setVisibility(View.GONE);
                mMessageView.setText(errorText);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(R.mipmap.ic_error_white_64);
                //TODO,秒后关闭窗口
                if (isAutoDismissEnabled){
                    autoDismiss();
                }
            }
            break;
            default:{

            }
            break;
        }
    }
    /**
     * 更新进度状态,可以设置提示文字
     * */
    public void setProgress(int status, String message, boolean isAutoHideEnabled){
        this.status = status;
        switch (status){
            case STATUS_PROCESSING:{
                mProgressBar.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.GONE);
//                mImageView.setImageResource(R.mipmap.ic_process_done);
                mMessageView.setText(message);
            }
            break;
            case STATUS_DONE:{
                mProgressBar.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(R.mipmap.ic_process_done);
                mMessageView.setText(message);
                //TODO,秒后关闭窗口
                if (isAutoHideEnabled){
                    autoDismiss();
                }
            }
            break;
            case STATUS_ERROR:{
                mProgressBar.setVisibility(View.GONE);
                mMessageView.setText(message);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(R.mipmap.ic_error_white_64);
                //TODO,秒后关闭窗口
                if (isAutoHideEnabled){
                    autoDismiss();
                }
            }
            break;
            default:{

            }
            break;
        }
    }

    private void autoDismiss(){
        //检查更新
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, autoDismissDelay * 1000);
    }
}
