package com.bingshanguxue.almigod.remoteControl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.almigod.FragmentActivity;
import com.bingshanguxue.almigod.R;
import com.bingshanguxue.vector_uikit.OptionalLabel;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.posRegister.PosRegister;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 远程控制
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class RemoteControlFragment extends BaseFragment {

    @Bind(R.id.tv_pos)
    TextView tvPos;
    @Bind(R.id.label_remoteControl)
    OptionalLabel labelRemotecontrol;

    @Bind(R.id.fab_submit)
    FloatingActionButton fabSubmit;

    private PosRegister mPosRegister;
    private RemoteControl mRemoteControl;

    public static RemoteControlFragment newInstance(Bundle args) {
        RemoteControlFragment fragment = new RemoteControlFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_remotecontrol;
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            abilityItem = args.getInt(EXTRA_KEY_ABILITY_ITEM, AbilityItem.TENANT);
//        }
        refresh();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2: {
                if (resultCode == Activity.RESULT_OK){
                    mPosRegister = (PosRegister) data.getSerializableExtra("posRegister");
                    refresh();
                }
            }
            break;
            case 3: {
                if (resultCode == Activity.RESULT_OK){
                    mRemoteControl = (RemoteControl) data.getSerializableExtra("remoteControl");
                    refresh();
                }
            }
            break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @OnClick(R.id.button_clientlog)
    public void redirect2ClientLog() {
        Bundle extras = new Bundle();
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_CLIENTLOG_LIST);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.tv_pos)
    public void redirect2PosList() {
        Bundle extras = new Bundle();
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_INSPECT_POS_LIST);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, 2);
    }

    @OnClick(R.id.label_remoteControl)
    public void redirect2RemoteControl() {
        Bundle extras = new Bundle();
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_REMOTECONTROL_LIST);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, 3);
    }

    @OnClick(R.id.fab_submit)
    public void submmit(){
        if (mRemoteControl == null){
            DialogUtil.showHint("请选择指令");
            return;
        }

        NetProcessor.ComnProcessor processor = new NetProcessor.ComnProcessor<EmbMsg>() {
            @Override
            protected void processOperResult(EmbMsg result) {
//                doAfterSendSuccess(result);
                ZLogger.d("发送远程控制指令成功");
            }
        };
        EmbMsgService msgService = ServiceFactory.getService(EmbMsgService.class,
                MfhApplication.getAppContext());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("remoteId", mRemoteControl.getId());
        jsonObject.put("remoteInfo", mRemoteControl.getName());
        msgService.sendText(MfhLoginService.get().getCurrentGuId(),
                null, mPosRegister.getChannelId(), mPosRegister.getChannelPointId(),
                IMBizType.REMOTE_CONTROL_CMD, jsonObject.toJSONString(), processor);
    }

    private void refresh(){
        if (mPosRegister != null){
            tvPos.setText(String.format("设备编号: %d\nchannelPointId=%s",
                    mPosRegister.getId(), mPosRegister.getChannelPointId()));
            fabSubmit.setVisibility(View.VISIBLE);
        }
        else{
            tvPos.setText("选择设备");
            fabSubmit.setVisibility(View.GONE);
        }

        if (mRemoteControl != null){
            labelRemotecontrol.setLabelText(String.format("%d-%s",
                    mRemoteControl.getId(), mRemoteControl.getName()));
        }
        else{
            labelRemotecontrol.setLabelText("选择指令");
        }
    }

}
