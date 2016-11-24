package com.manfenjiayuan.mixicook_vip.ui.address;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.ToggleSettingItem;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ARCode;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.location.PoiActivity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.api.reciaddr.ReciaddrApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 添加收货地址
 * Created by bingshanguxue on 6/28/16.
 */
public class AddAddressFragment extends BaseFragment {
    public static final String EXTRA_KEY_MODE = "mode";
    public static final String EXTRA_KEY_ADDR = "reciaddr";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.label_subname)
    TextLabelView labelSubName;
    @BindView(R.id.label_address)
    EditLabelView labelAddress;
    @BindView(R.id.label_receiveName)
    EditLabelView labelReceiveName;
    @BindView(R.id.label_receiveMobile)
    EditLabelView labelReceiveMobile;
    @BindView(R.id.toggle_isDefault)
    ToggleSettingItem isDefaultItem;

    private int mode;
    private Reciaddr mReciaddr;
    private AddressBrief mAddressBrief;

    public static AddAddressFragment newInstance(Bundle args) {
        AddAddressFragment fragment = new AddAddressFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_address_add;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mode = args.getInt(EXTRA_KEY_MODE);
            mReciaddr = (Reciaddr) args.getSerializable(EXTRA_KEY_ADDR);

        }
        if (mode == 0){
            toolbar.setTitle("新增地址");
            mAddressBrief = null;
        }
        else{
            toolbar.setTitle("编辑地址");
            mAddressBrief = new AddressBrief();
            if (mReciaddr != null){
                mAddressBrief.setId(mReciaddr.getId());
                mAddressBrief.setLatitude(mReciaddr.getLatitude());
                mAddressBrief.setLongitude(mReciaddr.getLongitude());
                mAddressBrief.setAreaID(String.valueOf(mReciaddr.getAreaID()));
                mAddressBrief.setName(mReciaddr.getSubName());
                mAddressBrief.setAddress(mReciaddr.getAddrName());

                labelReceiveName.setInput(mReciaddr.getReceiveName());
                labelReceiveMobile.setInput(mReciaddr.getReceivePhone());
            }

            labelSubName.setEndText(mAddressBrief.getName());
            labelAddress.setInput(mAddressBrief.getAddress());
        }
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_submit) {
                    submit();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_submit);

        labelAddress.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            labelReceiveName.requestFocusEnd();
                        }
                    }
                });
        labelReceiveName.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            labelReceiveMobile.requestFocusEnd();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_AMAP_POI: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    mAddressBrief = (AddressBrief) data.getSerializableExtra("addressBrief");
                    if (mAddressBrief != null){
                        labelSubName.setEndText(mAddressBrief.getName());
                    }
                    else{
                        labelSubName.setEndText("小区、写字楼、学校");
                    }
                    labelAddress.setEndText("");
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.label_subname)
    public void redirect2Poi(){
//        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_LOCATION);
        Intent intent = new Intent(getActivity(), PoiActivity.class);
//        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_AMAP_POI);
    }
    private void redirect2Location() {
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_LOCATION);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_LOCATION);
    }

    /**
     * 保存收货地址
     */
    private void submit() {
        NetCallBack.NetTaskCallBack submitRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(final IResponseData rspData) {
                        DialogUtil.showHint("操作成功");
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"缺少渠道端点标识！","version":"1","data":null}
                        ZLogger.e(String.format("操作失败:%s", errMsg));
                        DialogUtil.showHint(errMsg);
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        String receiveName = labelReceiveName.getInput();
        String receivePhone = labelReceiveMobile.getInput();
        String addressName = labelSubName.getEndText();
        String address = labelAddress.getInput();

        if (mAddressBrief == null){
            DialogUtil.showHint("请选择收货地址");
//            labelSubName.requestFocusEnd();
            return;
        }

        if (StringUtils.isEmpty(receiveName)){
            DialogUtil.showHint("请输入收件人姓名");
            labelReceiveName.requestFocusEnd();
            return;
        }

        if (StringUtils.isEmpty(receivePhone)){
            DialogUtil.showHint("请输入收件人手机号");
            labelReceiveMobile.requestFocusEnd();
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("receiveName", receiveName);
        jsonObject.put("receivePhone", receivePhone);
        jsonObject.put("subName", mAddressBrief.getName());
        jsonObject.put("addrName", address);
        jsonObject.put("areaID", mAddressBrief.getAreaID());
        jsonObject.put("latitude", mAddressBrief.getLatitude());
        jsonObject.put("longitude", mAddressBrief.getLongitude());

        if (mode == 0){
            ReciaddrApi.createForHuman(MfhLoginService.get().getCurrentGuId(), jsonObject, submitRC);
        }
        else{
            jsonObject.put("id", mAddressBrief.getId());
            ReciaddrApi.updateForHuman(MfhLoginService.get().getCurrentGuId(), jsonObject, submitRC);
        }
    }
}
