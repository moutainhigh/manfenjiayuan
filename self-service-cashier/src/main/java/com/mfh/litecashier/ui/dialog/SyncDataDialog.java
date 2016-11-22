package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mfh.framework.uikit.dialog.CommonDialog;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.mfh.litecashier.R;


/**
 * <h1>同步数据</h1><br>
 *
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class SyncDataDialog extends CommonDialog {

    private AvatarView ivHeader;
    private TextView tvTitle;
    private Button btnIncremetal, btnFullscale;

    public interface DialogClickListener {
        /**收银员离开*/
        void onFullscale();
        /**收银员交接班*/
        void onIncremental();
    }

    private View rootView;

    private String title;
    private DialogClickListener mListener;

    private SyncDataDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SyncDataDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_syncdata, null);
//        ButterKnife.bind(rootView);

        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_header);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        btnIncremetal = (Button) rootView.findViewById(R.id.button_incremental);
        btnFullscale = (Button) rootView.findViewById(R.id.button_fullscale);

        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
        btnIncremetal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onIncremental();
                }
                dismiss();
            }
        });
        btnFullscale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onFullscale();
                }
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public SyncDataDialog(Context context) {
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
//        ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
        tvTitle.setText(title);
    }

    public void init(String title, DialogClickListener callback) {
//        this.dialogType = type;
        this.mListener = callback;
        this.title = title;
    }
}
