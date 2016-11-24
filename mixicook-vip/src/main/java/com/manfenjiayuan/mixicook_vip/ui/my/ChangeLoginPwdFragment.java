package com.manfenjiayuan.mixicook_vip.ui.my;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mfh.framework.api.account.UserApiImpl;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 账户安全·修改登录密码
 *
 * @author zhangzn created on 2015-04-13
 * @since Framework 1.0
 */
public class ChangeLoginPwdFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.et_old_pwd) EditText etOldPwd;
    @BindView(R.id.et_new_pwd) EditText etNewPwd;
    @BindView(R.id.et_new_pwd_confirm) EditText etNewConfirmPwd;

    public ChangeLoginPwdFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_change_pwd;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("修改登录密码");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        etOldPwd.setHint(R.string.hint_edit_old_pwd);
        etNewPwd.setHint(R.string.hint_edit_new_pwd);
        etNewConfirmPwd.setHint(R.string.hint_edit_new_pwd_confirm);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 输入校验
     * */
    private boolean validate(String oldPwd, String newPwd, String confirmNewPwd){
        if(StringUtils.isEmpty(oldPwd)){
            DialogUtil.showHint(getString(R.string.toast_oldpwd_empty));
            return false;
        }
        if(StringUtils.isEmpty(newPwd)){
            DialogUtil.showHint(getString(R.string.toast_newpwd_empty));
            return false;
        }
        if(newPwd.length() < 6){
            DialogUtil.showHint(getString(R.string.toast_newpwd_short));
            return false;
        }
        if(StringUtils.isEmpty(confirmNewPwd)){
            DialogUtil.showHint(getString(R.string.toast_confirmpwd_empty));
            return false;
        }
        if(!newPwd.equals(confirmNewPwd)){
            DialogUtil.showHint(getString(R.string.toast_newpwd_conflict));
            return false;
        }

        if(!NetworkUtils.isConnect(getContext())){
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return false;
        }

        return true;
    }

    @OnClick(R.id.button_submit)
    public void updateUserPassword(){
        final String oldPwd = etOldPwd.getText().toString();
        final String newPwd = etNewPwd.getText().toString();
        String confirmNewPwd = etNewConfirmPwd.getText().toString();

        if(!validate(oldPwd, newPwd, confirmNewPwd)){
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                                    RspValue<String> retValue = (RspValue<String>) rspData;
//                                    if(retValue != null){
//                                        Log.d("Nat: updateUserPassword.response", retValue.getValue());
//                                    }
                        DialogUtil.showHint(getString(R.string.toast_change_loginpwd_success));
                        MfhLoginService.get().changePassword(newPwd);

                        //返回上一层页面
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }

//                            @Override
//                            protected void processFailure(Throwable t, String errMsg) {
//                                super.processFailure(t, errMsg);
//                                Log.d("Nat: updateUserPassword.processFailure", errMsg);
//                                DialogUtil.showHint("修改登录密码失败");
//                            }
                }
                , String.class
                , MfhApplication.getAppContext())
        {

        };

        UserApiImpl.updateUserPassword(MfhLoginService.get().getCurrentGuId(), oldPwd, newPwd, responseCallback);
    }
}
