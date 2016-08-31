package com.mfh.owner.ui.settings;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.owner.R;
import com.mfh.owner.fragments.BaseFragment;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 账户安全·重置登录密码
 *
 * @author zhangzn created on 2015-04-13
 * @since Framework 1.0
 */
public class ResetLoginPwdFragment extends BaseFragment {

    @Bind(R.id.et_new_pwd) EditText etNewPwd;
    @Bind(R.id.et_new_pwd_confirm) EditText etNewConfirmPwd;

    public ResetLoginPwdFragment() {
        super();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.fragment_reset_login_pwd;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
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
    private boolean validate(String newPwd, String confirmNewPwd){
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

        if(!NetWorkUtil.isConnect(getContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            return false;
        }

        return true;
    }

    @OnClick(R.id.button_submit)
    public void updateUserPassword(){
        final String newPwd = etNewPwd.getText().toString();
        String confirmNewPwd = etNewConfirmPwd.getText().toString();

        if(!validate(newPwd, confirmNewPwd)){
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
//                        DialogUtil.showHint(getContext().getString(R.string.toast_change_loginpwd_success));
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

//        NetProxy.updateUserPassword(MfhLoginService.get().getCurrentGuId(), newPwd, responseCallback);
    }
}
