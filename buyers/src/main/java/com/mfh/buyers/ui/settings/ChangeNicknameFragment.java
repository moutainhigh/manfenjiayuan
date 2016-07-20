package com.mfh.buyers.ui.settings;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.mfh.buyers.fragments.BaseFragment;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.buyers.R;
import com.mfh.buyers.utils.NetProxy;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 首页
 *
 * @author zhangzn created on 2015-04-13
 * @since Framework 1.0
 */
public class ChangeNicknameFragment extends BaseFragment {

    @Bind(R.id.et_nickname) EditText etNickname;

    public ChangeNicknameFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_change_nickname;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        etNickname.setText(MfhLoginService.get().getHumanName());
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

    private boolean validate(String nickName){
        if(StringUtils.isEmpty(nickName)){
            DialogUtil.showHint(R.string.toast_nickname_empty);
            return false;
        }

        if(MfhLoginService.get().getHumanName().equalsIgnoreCase(nickName)){
            DialogUtil.showHint(R.string.toast_nickname_conflict);
            return false;
        }

        if(!NetWorkUtil.isConnect(getContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            return false;
        }

        return true;
    }

    @OnClick(R.id.button_submit)
    public void updateProfile(){
        final String nickName = etNickname.getText().toString();

        if(!validate(nickName)){
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
                        DialogUtil.showHint(getString(R.string.toast_change_nickname_success));
                        MfhLoginService.get().changeHumanName(nickName);

                        //返回上一层页面
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }

//                            @Override
//                            protected void processFailure(Throwable t, String errMsg) {
//                                super.processFailure(t, errMsg);
//                                Log.d("Nat: updateProfile.processFailure", errMsg);
//                                DialogUtil.showHint("修改昵称失败");
//                            }
                }
                , String.class
                , MfhApplication.getAppContext()) {

        };

        JSONObject object = new JSONObject();
        object.put(NetProxy.PARAM_KEY_ID, MfhLoginService.get().getCurrentGuId());
        object.put(NetProxy.PARAM_KEY_NAME, nickName);
        NetProxy.updateProfile(object.toJSONString(), responseCallback);
    }
}
