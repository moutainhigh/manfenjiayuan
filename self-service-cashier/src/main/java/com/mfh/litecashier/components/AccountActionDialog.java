package com.mfh.litecashier.components;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.BaseDialogFragment;
import com.mfh.litecashier.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * <h1>账号操作：锁定/交接班/登录/退出</h1><br>
 * <p>
 * 1.收银员离开 {@link AccountActionDialog.OnAccountActionListener#onLock()}<br>
 * 2.收银员交接班 {@link AccountActionDialog.OnAccountActionListener#onHandOver()}<br>
 * 3.收银员退出账号 {@link AccountActionDialog.OnAccountActionListener#onLogout()}<br>
 *
 * Created by bingshanguxue on 16/09/2017.
 */

public class AccountActionDialog extends BaseDialogFragment {

    public static final String TAG = "AccountActionDialog";

    public static final int EVENT_CHANGE_ACCOUNT = 0x0001;//切换收银员账号
    public static final int EVENT_ACCOUNT_MGR = 0x0002;//管理员菜单

    private int mEvent;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_header)
    AvatarView ivHeader;
    @BindView(R.id.tv_username)
    TextView tvUsername;
    @BindView(R.id.tv_hint)
    TextView tvHint;
    @BindView(R.id.button_lock)
    Button btnLock;
    @BindView(R.id.button_handover)
    Button btnHandOver;
    @BindView(R.id.button_logout)
    Button btnLogout;
    @BindView(R.id.rl_progressbar)
    RelativeLayout rlProgressBar;

    public interface OnAccountActionListener {
        /**
         * 收银员离开
         */
        void onLock();

        /**
         * 收银员交接班
         */
        void onHandOver();

        /**
         * 收银员退出账号
         */
        void onLogout();
    }

    private OnAccountActionListener mOnAccountActionListener;

    public void setOnAccountActionListener(OnAccountActionListener listener) {
        mOnAccountActionListener = listener;
    }

    public void config(int event, OnAccountActionListener listener) {
        this.mEvent = event;
        this.mOnAccountActionListener = listener;
    }

    @Override
    protected int getDialogType() {
        return DIALOG_TYPE_SMALL;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_account_action;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        ButterKnife.bind(this, rootView);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        toolbar.setTitle(R.string.title_query_customer);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_close) {
                    dismiss();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_normal);

        ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
        tvUsername.setText(MfhLoginService.get().getHumanName());
        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
    }

    @Override
    public void onStart() {
        super.onStart();
        setCancelable(true);

        rlProgressBar.setVisibility(View.GONE);

        if (EVENT_CHANGE_ACCOUNT == mEvent) {
            btnHandOver.setVisibility(View.GONE);
            btnLock.setVisibility(View.GONE);
            toolbar.setTitle(R.string.title_query_cashier);
            tvHint.setText(R.string.hint_change_cashier);
        } else {
            btnHandOver.setVisibility(View.VISIBLE);
            btnLock.setVisibility(View.VISIBLE);
            toolbar.setTitle(R.string.title_query_customer);
            tvHint.setText("");
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ZLogger.d("onDismiss");
        super.onDismiss(dialog);
    }

    @OnClick(R.id.button_lock)
    public void onClickLock() {
        if (mOnAccountActionListener != null) {
            mOnAccountActionListener.onLock();
        }
        dismiss();
    }

    @OnClick(R.id.button_handover)
    public void onClickHandover() {
        if (mOnAccountActionListener != null) {
            mOnAccountActionListener.onHandOver();
        }
        dismiss();
    }

    @OnClick(R.id.button_logout)
    public void onClickLogout() {
        logout();
    }

    public void onLoadStart() {
        rlProgressBar.setVisibility(View.VISIBLE);
        setCancelable(false);
    }

    public void onLoadError(String errorMsg) {
        setCancelable(true);
        rlProgressBar.setVisibility(View.GONE);
        DialogUtil.showHint(errorMsg);
    }

    public void onLoadFinish() {
        setCancelable(true);
        rlProgressBar.setVisibility(View.GONE);
        dismiss();
    }

    /**
     * 退出当前账号
     */
    private void logout() {
        onLoadStart();

        MfhUserManager.getInstance().logout(new Callback() {
            @Override
            public void onSuccess() {
                ZLogger.d("退出成功");
                try {
                    if (mOnAccountActionListener != null) {
                        mOnAccountActionListener.onLogout();
                    }
                    onLoadFinish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                onLoadError(message);
            }
        });
    }

}
