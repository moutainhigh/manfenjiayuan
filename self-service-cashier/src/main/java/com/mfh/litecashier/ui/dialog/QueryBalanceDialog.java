package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.CustomerPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.ICustomerView;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

/**
 * <p>
 * 对话框 -- 门店用户注册
 * <li>Step 1: 输入手机号，查询用户是否注册满分。</li>
 * <li>Step 2: 如果用户已经注册满分，则发送验证码给用户。（再次发送验证码需要等待60秒）</li>
 * <li>Step 3: 输入用户收到的验证码，然后点击‘验证’按钮验证是否正确。</li>
 * </p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class QueryBalanceDialog extends CommonDialog implements ICustomerView {

    private View rootView;
    private ImageButton btnClose;
    private TextView tvTitle;
    private EditText etCardNo;
    private AvatarView ivHeader;
    private TextLabelView tvName, tvCash, tvScore;
//    private AvatarView ivHeader;
    private Button btnSubmit;

    private ProgressBar progressBar;
    private CustomerPresenter mCustomerPresenter;



//    private QueryBalanceDialog(Context context, boolean flag, OnCancelListener listener) {
//        super(context, flag, listener);
//    }

    @SuppressLint("InflateParams")
    private QueryBalanceDialog(Context context, int defStyle) {
        super(context, defStyle);

        mCustomerPresenter = new CustomerPresenter(this);

        rootView = getLayoutInflater().inflate(R.layout.dialogview_query_balance, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        ivHeader = (AvatarView) rootView.findViewById(R.id.iv_header);
        etCardNo = (EditText) rootView.findViewById(R.id.et_card_id);
        tvName = (TextLabelView) rootView.findViewById(R.id.tv_humanName);
        tvCash = (TextLabelView) rootView.findViewById(R.id.tv_cash);
        tvScore = (TextLabelView) rootView.findViewById(R.id.tv_score);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);

        tvTitle.setText("余额查询");
        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
        etCardNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        loadUserAccount(etCardNo.getText().toString());
                    }
                    return true;
                }
//                return true;
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etCardNo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etCardNo);
                }
                etCardNo.requestFocus();
                etCardNo.setSelection(etCardNo.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnSubmit.setText("确定");
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setContent(rootView, 0);
    }

    public QueryBalanceDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.CENTER);
        }
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = d.getHeight();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();
        DeviceUtils.hideSoftInput(getOwnerActivity());
        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        etCardNo.getText().clear();
    }

    public void initialize() {
        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }


    private void refresh(UserAccount userAccount){
        if (userAccount != null){
            tvCash.setEndText(MUtils.formatDouble(userAccount.getCur_cash(), "暂无数据"));
            tvScore.setEndText(userAccount.getCur_score() != null
                    ? String.valueOf(userAccount.getCur_score())
            : "暂无数据");
        }
        else{
            tvCash.setEndText("暂无数据");
            tvScore.setEndText("暂无数据");
        }

        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }

    private void refresh2(Human human){
        if (human != null){
            ivHeader.setAvatarUrl(human.getHeadimageUrl());
            tvName.setTvSubTitle(human.getCustomName());
            tvCash.setEndText(MUtils.formatDouble(human.getCurCash(), "暂无数据"));
            tvScore.setEndText(human.getCurScore() != null
                    ? String.valueOf(human.getCurScore())
                    : "暂无数据");
        }
        else{
            ivHeader.setAvatarUrl(null);
            tvName.setTvSubTitle("暂无数据");
            tvCash.setEndText("暂无数据");
            tvScore.setEndText("暂无数据");
        }

        etCardNo.getText().clear();
        etCardNo.requestFocus();
    }

    private void loadUserAccount(String cardId){
        refresh2(null);



        if (!NetworkUtils.isConnect(getContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mCustomerPresenter.getCustomerByOther(cardId);

        final String cardId2 = MUtils.parseCardId(cardId);
        if (StringUtils.isEmpty(cardId2)) {
            DialogUtil.showHint("芯片号无效");
            return;
        }

//        Map<String, String> options = new HashMap<>();
//        if (!StringUtils.isEmpty(cardId2)) {
////            options.put("cardId", cardId2);
//            options.put("cardNo", cardId2);
//        }
//        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//        RxHttpManager.getInstance().getCustomerByOther(options,
//                new Subscriber<Human>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        progressBar.setVisibility(View.GONE);
//                        DialogUtil.showHint(e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(Human human) {
//                        refresh2(human);
//                        progressBar.setVisibility(View.GONE);
//                    }
//                });
    }

    @Override
    public void onICustomerViewLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onICustomerViewError(int type, String content, String errorMsg) {
        refresh2(null);

        progressBar.setVisibility(View.GONE);
        DialogUtil.showHint(errorMsg);
    }

    @Override
    public void onICustomerViewSuccess(int type, String content, Human human) {
        refresh2(human);
        progressBar.setVisibility(View.GONE);

    }

}
