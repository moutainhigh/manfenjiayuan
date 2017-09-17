package com.mfh.litecashier.components.customer;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manfenjiayuan.business.mvp.presenter.CustomerPresenter;
import com.manfenjiayuan.business.mvp.view.ICustomerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.BaseDialogFragment;
import com.mfh.litecashier.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * 查询会员
 * 生命周期：show()->onCreate->onCreateView->{@link #onStart()}->onDismiss()
 *
 * Created by bingshanguxue on 30/06/2017.
 */

public class CustomerQueryDialogFragment extends BaseDialogFragment implements ICustomerView {
    public static final String TAG = "CustomerQueryDialogFragment";

    public static final int TARGET_CASHIER_SETTLE = 0x0002;//收银订单支付
    public static final int TARGET_CUSTOMER_TRANSACTION = 0x0011;//交易查询
    public static final int TARGET_CHANGE_PAY_PASSWORD = 0x0012;//修改支付密码
    public static final int TARGET_PRINT_GROUP_ORDER = 0x0013;//商品自提-团购
    public static final int TARGET_EXCHANGE_SCORE = 0x0014;//积分兑换
    public static final int TARGET_TRANSFER = 0x0015;//会员充值
    private int mTarget;
    private int mType;


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.tv_endText)
    TextView tvEndText;
    @BindView(R.id.button_submit)
    Button btnSubmit;
    private RelativeLayout rlProgressBar;

    private CustomerPresenter mCustomerPresenter;
    private Human mHuman;

    public interface OnCustomerQueryListener {
        void onQuerySuccess(int target, int type, Human human);

//        void onCancelOrDismiss(int target);
    }

    private OnCustomerQueryListener mOnCustomerQueryListener;

    public void setOnCustomerQueryListener(OnCustomerQueryListener listener) {
        mOnCustomerQueryListener = listener;
    }

    public void setTargetAndListener(int target, OnCustomerQueryListener listener) {
        this.mTarget = target;
        this.mOnCustomerQueryListener = listener;
    }

    @Override
    protected int getDialogType() {
        return DIALOG_TYPE_SMALL;
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_query_customer;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCustomerPresenter = new CustomerPresenter(this);
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

        etInput.setCursorVisible(false);//隐藏光标
        etInput.setFocusable(true);
        etInput.setFocusableInTouchMode(true);
//        etInput.setFilters(new InputFilter[]{new DecimalInputFilter(DECIMAL_DIGITS)});
        etInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submit();
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(getContext(), etInput);
                }
                requestFocusEnd();
                //返回true,不再继续传递事件
                return true;
            }
        });

//
//        rootViewtView.findViewById(R.id.key_del).setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
////                etInput.setText("");
//
//                return true;
//            }
//        });
        rlProgressBar = (RelativeLayout) rootView.findViewById(R.id.rl_progressbar);
        rlProgressBar.setVisibility(View.GONE);
    }


    @Override
    public void onStart() {
        super.onStart();

        setCancelable(true);
        etInput.getText().clear();
        mHuman = null;
        mType = 0;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ZLogger.d("onDismiss");
        super.onDismiss(dialog);

        if (mOnCustomerQueryListener != null) {
            mOnCustomerQueryListener.onQuerySuccess(mTarget, mType, mHuman);
        }
    }

    public void requestFocusEnd() {
        this.etInput.setSelection(this.etInput.length());
        this.etInput.requestFocus();
    }

    @OnClick(R.id.key_0)
    public void onClick0() {
        simulateKeyDown(KeyEvent.KEYCODE_0);
    }
    @OnClick(R.id.key_1)
    public void onClick1() {
        simulateKeyDown(KeyEvent.KEYCODE_1);
    }
    @OnClick(R.id.key_2)
    public void onClick2() {
        simulateKeyDown(KeyEvent.KEYCODE_2);
    }
    @OnClick(R.id.key_3)
    public void onClick3() {
        simulateKeyDown(KeyEvent.KEYCODE_3);
    }
    @OnClick(R.id.key_4)
    public void onClick4() {
        simulateKeyDown(KeyEvent.KEYCODE_4);
    }
    @OnClick(R.id.key_5)
    public void onClick5() {
        simulateKeyDown(KeyEvent.KEYCODE_5);
    }
    @OnClick(R.id.key_6)
    public void onClick6() {
        simulateKeyDown(KeyEvent.KEYCODE_6);
    }
    @OnClick(R.id.key_7)
    public void onClick7() {
        simulateKeyDown(KeyEvent.KEYCODE_7);
    }
    @OnClick(R.id.key_8)
    public void onClick8() {
        simulateKeyDown(KeyEvent.KEYCODE_8);
    }
    @OnClick(R.id.key_9)
    public void onClick9() {
        simulateKeyDown(KeyEvent.KEYCODE_9);
    }
    @OnClick(R.id.key_dot)
    public void onClickDot() {
        simulateKeyDown(KeyEvent.KEYCODE_NUMPAD_DOT);
    }

    @OnClick(R.id.key_del)
    public void onClickDel() {
        simulateKeyDown(KeyEvent.KEYCODE_DEL);
    }

    @OnLongClick(R.id.key_del)
    public boolean onLongClickDel() {
        etInput.getText().clear();
        simulateKeyDown(KeyEvent.KEYCODE_DEL);
        return true;
    }

    /**
     * 搜索会员信息
     */
    @OnClick(R.id.button_submit)
    public void submit() {
        String input = etInput.getText().toString();
        if (StringUtils.isEmpty(input)) {
            DialogUtil.showHint(R.string.hint_customer_empty);
            return;
        }
        mCustomerPresenter.getCustomerByOther(input);
    }

    @Override
    public void onICustomerViewLoading() {
        rlProgressBar.setVisibility(View.VISIBLE);
        setCancelable(false);
    }

    @Override
    public void onICustomerViewError(int type, String content, String errorMsg) {
        setCancelable(true);
        rlProgressBar.setVisibility(View.GONE);
        DialogUtil.showHint(errorMsg);
    }

    @Override
    public void onICustomerViewSuccess(int type, String content, Human human) {
        setCancelable(true);
        mHuman = human;
        mType = type;
        rlProgressBar.setVisibility(View.GONE);
        if (human == null) {
            DialogUtil.showHint(R.string.tip_cannot_find_customer);
        }
        dismiss();
    }
}
