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
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.R;


/**
 * <h1>账号操作：锁定/交接班/登录/退出</h1><br>
 *
 * 1.收银员离开 {@link DialogClickListener#onLock()}<br>
 * 2.收银员交接班 {@link DialogClickListener#onHandOver()}<br>
 * 3.提交营业现金 {@link DialogClickListener#onCommitCash()} ()}<br>
 * 4.收银员退出账号 {@link DialogClickListener#onLogout()}<br>
 *
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class AccountDialog extends CommonDialog {

    private AvatarView ivHeader;
    private TextView tvUsername;
    private Button btnLock, btnHandOver, btnLogout;

    public interface DialogClickListener {
        /**收银员离开*/
        void onLock();
        /**收银员交接班*/
        void onHandOver();
        /**收银员退出账号*/
        void onLogout();
    }

    private View rootView;

    private DialogClickListener mListener;

    private AccountDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private AccountDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_account, null);
//        ButterKnife.bind(rootView);

        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_header);
        tvUsername = (TextView) rootView.findViewById(R.id.tv_username);
        btnLock = (Button) rootView.findViewById(R.id.button_lock);
        btnHandOver = (Button) rootView.findViewById(R.id.button_handover);
        btnLogout = (Button) rootView.findViewById(R.id.button_logout);

        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onLock();
                }
                dismiss();
            }
        });
        btnHandOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onHandOver();
                }
                dismiss();
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onLogout();
                }
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public AccountDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.CENTER);
        }

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
        ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
        tvUsername.setText(MfhLoginService.get().getHumanName());
    }

    public void init(int type, DialogClickListener callback) {
//        this.dialogType = type;
        this.mListener = callback;
        if (type == 0){
            btnLock.setVisibility(View.GONE);
            btnHandOver.setVisibility(View.GONE);
            btnLogout.setText("登录");
        }else{
            btnLock.setVisibility(View.VISIBLE);
            btnHandOver.setVisibility(View.VISIBLE);
            btnLogout.setText("退出");
        }
    }
}
