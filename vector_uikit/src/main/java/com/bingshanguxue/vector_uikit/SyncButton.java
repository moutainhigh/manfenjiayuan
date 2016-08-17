package com.bingshanguxue.vector_uikit;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


/**
 * 同步按钮
 *
 * @author bingshanguxue
 */
public class SyncButton extends RelativeLayout {

    private ImageView ivSync;
    private ImageView ivBadge;
    private ProgressBar mProgressBar;

    public SyncButton(Context context) {
        this(context, null);
    }

    public SyncButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        View rootView = View.inflate(getContext(), R.layout.widget_sync_button, this);

        ivSync = (ImageView) rootView.findViewById(R.id.iv_sync);
        ivBadge = (ImageView) rootView.findViewById(R.id.iv_badge);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        if (attrs != null) {
//            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSearchView);
//
//            etQueryText.setTextColor(a.getColor(R.styleable.CustomSearchView_textColor, Color.BLACK));
//            etQueryText.setHint(a.getString(R.styleable.CustomSearchView_hint));
//            etQueryText.setHintTextColor(a.getColor(R.styleable.CustomSearchView_textColorHint, Color.BLACK));
//
//            a.recycle();
        }
    }

    /**
     * 开始同步
     */
    public void startSync() {
        this.setEnabled(false);
        ivSync.setVisibility(INVISIBLE);
        mProgressBar.setVisibility(VISIBLE);
    }

    /**
     * 停止同步
     */
    public void stopSync() {
        this.setEnabled(true);
        ivSync.setVisibility(VISIBLE);
        mProgressBar.setVisibility(INVISIBLE);
        ivBadge.setVisibility(GONE);
    }

    /**
     * 设置红点是否可见
     */
    public void setBadgeEnabled(boolean enabled) {
        if (enabled) {
            ivBadge.setVisibility(VISIBLE);
        } else {
            ivBadge.setVisibility(GONE);
        }
    }
}
