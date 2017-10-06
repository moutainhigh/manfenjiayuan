package com.mfh.litecashier.components.customer;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.httpmgr.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.subscriber.MSubscriber;
import com.mfh.litecashier.BaseDialogFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.dialog.EnterPasswordDialog;
import com.mfh.litecashier.ui.widget.CustomerView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;


/**
 * 会员支付密码
 * Created by bingshanguxue on 17/07/06.
 */
public class ChangePayPwdDialogFragment extends BaseDialogFragment {
    public static final String EXTRA_KEY_HUMAN = "human";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.customer_view)
    CustomerView mCustomerView;
    @BindView(R.id.inlv_old_pwd)
    InputNumberLabelView inlvOldPwd;
    @BindView(R.id.inlv_new_pwd)
    InputNumberLabelView inlvNewPwd;
    @BindView(R.id.inlv_confirm_pwd)
    InputNumberLabelView inlvConfirmPwd;
    @BindView(R.id.button_submit)
    Button btnSubmit;

    private Human mMemberInfo = null;

    public static ChangePayPwdDialogFragment newInstance(Human human) {
        Bundle args = new Bundle();
        if (human != null) {
            args.putSerializable(EXTRA_KEY_HUMAN, human);
        }

        ChangePayPwdDialogFragment fragment = new ChangePayPwdDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getDialogType() {
        return DIALOG_TYPE_MIDDLE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_customer_paypwd;
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        ButterKnife.bind(this, rootView);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mMemberInfo = (Human) args.getSerializable(EXTRA_KEY_HUMAN);
            ZLogger.d("会员：" + JSONObject.toJSONString(mMemberInfo));
        } else {
            ZLogger.d("未找到会员");
        }

        toolbar.setTitle(R.string.title_change_pay_password);
//        setSupportActionBar(toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
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

        initOldPwd();
        initNewPwd();
        initConfirmPwd();

        inlvOldPwd.requestFocusEnd();

//自动加载会员信息
        refreshVipMemberInfo(mMemberInfo);
    }

    @Override
    public void onStart() {
        super.onStart();

        setCancelable(true);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        int ret = super.show(transaction, tag);
        try {
            //hide soft input
            DeviceUtils.hideSoftInput(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }


    private void initOldPwd() {
        inlvOldPwd.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD},
                new InputNumberLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            inlvNewPwd.requestFocusEnd();
                        }
                    }
                });
        inlvOldPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                inlvOldPwd.requestFocusEnd();

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
                            || inlvOldPwd.isSoftKeyboardEnabled()) {
                        enterPayPassword(getString(R.string.old_password), EnterPasswordDialog.TARGET_OLD_PASSWORD);
                    }
                }

                //返回true,不再继续传递事件
                return true;
            }
        });
    }

    private void initNewPwd() {
        inlvNewPwd.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD},
                new InputNumberLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            inlvConfirmPwd.requestFocusEnd();
                        }
                    }
                });
        inlvNewPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                inlvNewPwd.requestFocusEnd();

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
                            || inlvNewPwd.isSoftKeyboardEnabled()) {
                        enterPayPassword(getString(R.string.new_password), EnterPasswordDialog.TARGET_NEW_PASSWORD);
                    }
                }

                //返回true,不再继续传递事件
                return true;
            }
        });
    }

    private void initConfirmPwd() {
        inlvConfirmPwd.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD},
                new InputNumberLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            submit();
                        }
                    }
                });
//        inlvOldPwd.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                calculateCharge();
//            }
//        });
        inlvConfirmPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                inlvConfirmPwd.requestFocusEnd();

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
                            || inlvConfirmPwd.isSoftKeyboardEnabled()) {
                        enterPayPassword(getString(R.string.confirm_password), EnterPasswordDialog.TARGET_CONFIRM_PASSWORD);
                    }
                }

                //返回true,不再继续传递事件
                return true;
            }
        });
    }

    /**
     * 加载会员信息
     */
    private void refreshVipMemberInfo(Human memberInfo) {
        mMemberInfo = memberInfo;
        mCustomerView.reload(memberInfo);
    }

    private EnterPasswordDialog mEnterPasswordDialog = null;

    /**
     * 输入密码
     */
    private void enterPayPassword(String title, int target) {
        if (mEnterPasswordDialog == null) {
            mEnterPasswordDialog = new EnterPasswordDialog(getActivity());
            mEnterPasswordDialog.setCancelable(true);
            mEnterPasswordDialog.setCanceledOnTouchOutside(true);
        }
        mEnterPasswordDialog.init(title, target, new EnterPasswordDialog.OnEnterPasswordListener() {
            @Override
            public void onSubmit(int target, String password) {
                if (target == EnterPasswordDialog.TARGET_OLD_PASSWORD) {
                    inlvOldPwd.setInputString(password);
                    inlvOldPwd.requestFocusEnd();
                } else if (target == EnterPasswordDialog.TARGET_NEW_PASSWORD) {
                    inlvNewPwd.setInputString(password);
                    inlvNewPwd.requestFocusEnd();
                } else if (target == EnterPasswordDialog.TARGET_CONFIRM_PASSWORD) {
                    inlvConfirmPwd.setInputString(password);
                    inlvConfirmPwd.requestFocusEnd();
                }
            }

            @Override
            public void onCancel(int target) {

            }
        });
        mEnterPasswordDialog.show();
    }


    @OnClick(R.id.button_submit)
    public void submit() {
        if (mMemberInfo == null) {
            DialogUtil.showHint("会员信息无效");
            return;
        }
        String oldPwd = inlvOldPwd.getInputString();
        if (StringUtils.isEmpty(oldPwd)) {
            DialogUtil.showHint(R.string.hint_old_password);
            inlvOldPwd.requestFocusEnd();
            return;
        }

        String newPwd = inlvNewPwd.getInputString();
        if (StringUtils.isEmpty(newPwd)) {
            DialogUtil.showHint(R.string.hint_new_password);
            inlvNewPwd.requestFocusEnd();
            return;
        }

        String confirmPwd = inlvConfirmPwd.getInputString();
        if (StringUtils.isEmpty(confirmPwd)) {
            DialogUtil.showHint(R.string.hint_confirm_password);
            inlvConfirmPwd.requestFocusEnd();
            return;
        }

        if (!confirmPwd.equals(newPwd)) {
            DialogUtil.showHint(R.string.hint_confirm_password_error);
            inlvConfirmPwd.requestFocusEnd();
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        setCancelable(false);
        Map<String, String> options = new HashMap<>();
        options.put("humanId", String.valueOf(mMemberInfo.getId()));
        options.put("oldPwd", oldPwd);
        options.put("newPwd", newPwd);
//        options.put("confirmPwd", confirmPwd);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        CommonUserAccountHttpManager.getInstance().changePwd(options,
                new MSubscriber<String>() {

//                    @Override
//                    public void onError(Throwable e) {
////                        etPayCode.getText().clear();
//                        DialogUtil.showHint(e.getMessage());
//                        setCancelable(true);
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        DialogUtil.showHint(e.getMessage());
                        setCancelable(true);
                    }

                    @Override
                    public void onNext(String s) {
                        DialogUtil.showHint("修改支付密码成功");
                        setCancelable(true);
                        dismiss();
                    }

                });

    }

}
