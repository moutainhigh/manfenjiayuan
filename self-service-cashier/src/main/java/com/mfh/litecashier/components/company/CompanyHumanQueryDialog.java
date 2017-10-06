package com.mfh.litecashier.components.company;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.im.IMClient;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.bean.CompanyHuman;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.http.ErrorCode;
import com.mfh.framework.rxapi.http.ExceptionHandle;
import com.mfh.framework.rxapi.httpmgr.CompanyHumanHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MSubscriber;
import com.mfh.litecashier.BaseDialogFragment;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.AffairEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import rx.Subscriber;

/**
 * 查询会员
 * 生命周期：show()->onCreate->onCreateView->{@link #onStart()}->onDismiss()
 * <p>
 * Created by bingshanguxue on 30/06/2017.
 */

public class CompanyHumanQueryDialog extends BaseDialogFragment {

    public static final String TAG = "CompanyHumanQueryDialog";

    public static final int TARGET_CASHIER_SIGNIN = 0x0001;//收银员登录
    private int mTarget;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.tv_endText)
    TextView tvEndText;
    @BindView(R.id.button_submit)
    Button btnSubmit;
    @BindView(R.id.rl_progressbar)
    RelativeLayout rlProgressBar;

    private CompanyHuman mHuman;

    public interface OnCompanyHumanQueryListener {
        void onQuerySuccess(int target, CompanyHuman companyHuman);

//        void onCancelOrDismiss(int target);
    }

    private OnCompanyHumanQueryListener mOnCustomerQueryListener;

    public void setOnCompanyHumanQueryListener(OnCompanyHumanQueryListener listener) {
        mOnCustomerQueryListener = listener;
    }

    public void setTargetAndListener(int target, OnCompanyHumanQueryListener listener) {
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

        rlProgressBar.setVisibility(View.GONE);

        if (TARGET_CASHIER_SIGNIN == mTarget) {
            toolbar.setTitle(R.string.title_query_cashier);
            setCancelable(false);
        } else {
            toolbar.setTitle(R.string.title_query_customer);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        ZLogger.d("mTarget:" + mTarget);
        if (TARGET_CASHIER_SIGNIN == mTarget) {
            // Inflate a menu to be displayed in the toolbar
//            toolbar.inflateMenu(R.menu.menu_empty);
            toolbar.setTitle(R.string.title_cashier_login);
            toolbar.getMenu().findItem(R.id.action_close).setVisible(false);
            setCancelable(false);
        } else {
            // Inflate a menu to be displayed in the toolbar
            toolbar.setTitle(R.string.title_query_cashier);
            toolbar.getMenu().findItem(R.id.action_close).setVisible(true);
//            toolbar.inflateMenu(R.menu.menu_normal);
            setCancelable(true);
        }

        etInput.getText().clear();
        mHuman = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ZLogger.d("onDismiss");
        super.onDismiss(dialog);

        if (mOnCustomerQueryListener != null) {
            mOnCustomerQueryListener.onQuerySuccess(mTarget, mHuman);
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

        //长度为8(466CAF31) ，会员卡芯片号
        if (input.length() == 8) {
            final String cardNo = MUtils.parseCardId(input);
            if (StringUtils.isEmpty(cardNo)) {
                onLoadError("卡芯片号无效");
                return;
            }

            findCompanyHumansByPrivateInfo(0, String.valueOf(MfhLoginService.get().getSpid()), cardNo);
        }
        //长度为11(15250065084)，手机号
        else if (input.length() == 11) {
            findCompanyHumansByPrivateInfo(2, String.valueOf(MfhLoginService.get().getSpid()), input);
        }
        //长度为15(000000000712878)，微信付款码
        else if (input.length() == 15) {
            String humanId = MUtils.parseMfPaycode(input);
            if (StringUtils.isEmpty(humanId)) {
                onLoadError("付款码无效");
                return;
            }

            findCompanyHumansByPrivateInfo(1, String.valueOf(MfhLoginService.get().getSpid()), humanId);
        } else {
            onLoadError("参数无效");
        }
    }

    private void findCompanyHumansByPrivateInfo(final int type, final String tenantId, final String content) {
        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
        if (!StringUtils.isEmpty(content)) {
            if (type == 0) {
                options.put("cardNo", content);
            } else if (type == 1) {
                options.put("humanId", content);
            } else if (type == 2) {
                options.put("mobile", content);
            }
        }

//        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put("tenantId", tenantId);
//        if (pageInfo != null){
//            options.put("page", Integer.toString(pageInfo.getPageNo()));
//            options.put("rows", Integer.toString(pageInfo.getPageSize()));
//        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        onLoadStart();
        CompanyHumanHttpManager.getInstance().findCompanyHumansByPrivateInfo(options,
                new MQuerySubscriber<CompanyHuman>(new PageInfo(0, 10)) {

                    @Override
                    public void onError(Throwable e) {
                        onLoadError(e.getMessage());
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<CompanyHuman> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (dataList != null && dataList.size() > 0) {
                            mHuman = dataList.get(0);
                        } else {
                            mHuman = null;
                        }

                        if (mHuman != null) {
                            loginByPrivateCard(type, tenantId, content);
                        } else {
                            onLoadError("没有查询到账号");
                        }
                    }
                });
    }

    private void loginByPrivateCard(int type, String tenantId, String content) {
        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
        if (!StringUtils.isEmpty(content)) {
            if (type == 0) {
                options.put("cardNo", content);
            } else if (type == 1) {
                options.put("humanId", content);
            } else if (type == 2) {
                options.put("mobile", content);
            }
        }
        options.put("tenantId", tenantId);
        options.put("loginKind", "humanId");
        //不需要KEY_JSESSIONID
//        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        RxHttpManager.getInstance().loginByPrivateCard(options,
                new MSubscriber<MResponse<UserMixInfo>>() {

//                    @Override
//                    public void onError(Throwable e) {
//                        onLoadError(e.getMessage());
//                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        onLoadError(e.getMessage());

                    }

                    @Override
                    public void onNext(MResponse<UserMixInfo> userMixInfoMResponse) {
                        if (userMixInfoMResponse != null &&
                                (ErrorCode.SUCCESS.equals(userMixInfoMResponse.getCode()) || ErrorCode.SUCCESS_NEW.equals(userMixInfoMResponse.getCode()))) {
                            DialogUtil.showHint(userMixInfoMResponse.getMsg());
                            MfhLoginService.get().saveUserMixInfo(null, null, userMixInfoMResponse.getData());

                            //注册到消息桥
                            IMClient.getInstance().registerBridge();

                            //初始化收银,createdBy(humanId)已经改变
                            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_RESET_CASHIER));
                            onLoadFinish();
                        } else {
                            onLoadError(userMixInfoMResponse.getMsg());
                        }
                    }

                });
    }

    public void onLoadStart() {
        rlProgressBar.setVisibility(View.VISIBLE);
        setCancelable(false);
    }

    public void onLoadError(String errorMsg) {
        if (TARGET_CASHIER_SIGNIN == mTarget) {
            setCancelable(false);
        } else {
            setCancelable(true);
        }
        etInput.getText().clear();
        rlProgressBar.setVisibility(View.GONE);
        DialogUtil.showHint(errorMsg);
    }

    public void onLoadFinish() {
        setCancelable(true);
        rlProgressBar.setVisibility(View.GONE);
        dismiss();
    }
}
