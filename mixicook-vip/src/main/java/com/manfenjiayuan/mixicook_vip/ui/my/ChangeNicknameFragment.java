package com.manfenjiayuan.mixicook_vip.ui.my;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
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
 * 首页
 *
 * @author bingshanguxue created on 2015-04-13
 * @since Framework 1.0
 */
public class ChangeNicknameFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.et_nickname) EditText etNickname;

    public ChangeNicknameFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_change_nickname;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("修改昵称");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
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
            DialogUtil.showHint(getString(R.string.toast_nickname_empty));
            return false;
        }

        if(MfhLoginService.get().getHumanName().equalsIgnoreCase(nickName)){
            DialogUtil.showHint(getString(R.string.toast_nickname_conflict));
            return false;
        }

        if(!NetworkUtils.isConnect(getContext())){
            DialogUtil.showHint(getString(R.string.toast_network_error));
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
        object.put("id", MfhLoginService.get().getCurrentGuId());
        object.put("name", nickName);
        UserApiImpl.updateProfile(object.toJSONString(), responseCallback);
    }
}
