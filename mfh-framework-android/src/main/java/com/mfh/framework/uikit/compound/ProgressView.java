package com.mfh.framework.uikit.compound;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.framework.R;


/***
 *
 */
public class ProgressView extends LinearLayout {

    public final static int STATUS_NA           = 0;
    public final static int STATUS_PROCESSING   = 1;
    public final static int STATUS_DONE         = 2;
    public final static int STATUS_ERROR        = 3;

    private View rootView;
    private ImageView ivDone;
    private ProgressBar progressBar;
    private TextView tvProcess;


    private int status = STATUS_NA;//状态
    private String processText;
    private String doneText;
    private String errorText;


	public ProgressView(Context context) {
		super(context);
		init();
	}

	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

    public ProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

	private void init() {
		View.inflate(getContext(), R.layout.loading_text_view, this);

        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        tvProcess = (TextView) rootView.findViewById(R.id.tv_process);
        ivDone = (ImageView) rootView.findViewById(R.id.iv_done);
	}

    public void init(String processText, String doneText, String errorText){
        this.status = STATUS_DONE;
        this.processText = processText;
        this.doneText = doneText;
        this.errorText = errorText;
    }

    public void setStatus(int status){
        this.status = status;
        switch (status){
            case STATUS_PROCESSING:{
                progressBar.setVisibility(View.VISIBLE);
                ivDone.setVisibility(View.GONE);
                tvProcess.setText(processText);
            }
            break;
            case STATUS_DONE:{
                progressBar.setVisibility(View.GONE);
                ivDone.setVisibility(View.VISIBLE);
                tvProcess.setText(doneText);
                //TODO,秒后关闭窗口
            }
            break;
            case STATUS_ERROR:{
                progressBar.setVisibility(View.GONE);
                tvProcess.setText(errorText);
            }
            break;
        }
    }

    public void show(String text){
        this.tvProcess.setText(text);
        this.setVisibility(View.VISIBLE);
    }

    public void hide(){
        this.setVisibility(View.GONE);
    }

}
