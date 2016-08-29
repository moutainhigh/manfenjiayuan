package com.mfh.enjoycity.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.CommonAddrTemp;
import com.mfh.enjoycity.bean.SubdisBean;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.ui.activity.SearchCommunityActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.widget.EditItem;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.List;

import butterknife.Bind;


/**
 * 添加新地址
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 * */
public class AddAddressActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind({ R.id.item_receiver, R.id.item_telephone, R.id.item_address, R.id.item_houseNo })
    List<EditItem> btnItems;

    private SubdisBean tmpSubdisBean;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_add_address;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_address_add);//必须在setSupportActionBar(toolbar);之前设置才有效
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddAddressActivity.this.onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_submit) {
                    createNewAddress();
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_submit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btnItems.get(0).init("收货人", "", EditItem.ThemeType.THEME_TEXT_EDIT, EditItem.SeperateLineType.SEPERATE_LINE_MULTI_TOP);
        btnItems.get(1).init("手机号", "", EditItem.ThemeType.THEME_TEXT_EDIT_PHONE, EditItem.SeperateLineType.SEPERATE_LINE_MULTI_CENTER);
        btnItems.get(2).init("地址", "", EditItem.ThemeType.THEME_TEXT_TEXT_ARROW, EditItem.SeperateLineType.SEPERATE_LINE_MULTI_CENTER);
        btnItems.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddAddressActivity.this, SearchCommunityActivity.class);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SEARCH_COMMUNITY);
            }
        });
        btnItems.get(3).init("门牌号", "", EditItem.ThemeType.THEME_TEXT_EDIT, EditItem.SeperateLineType.SEPERATE_LINE_MULTI_BOTTOM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_submit, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == Constants.ACTIVITY_REQUEST_CODE_SEARCH_COMMUNITY){
            if(resultCode == Activity.RESULT_OK){
                if (intent != null){
                    tmpSubdisBean = (SubdisBean)intent.getSerializableExtra(Constants.INTENT_KEY_ADDRESS_DATA);
                    if(tmpSubdisBean != null){
                        ZLogger.d(tmpSubdisBean.toString());
                        btnItems.get(2).setDetailText(tmpSubdisBean.getSubdisName());
                    }

                }
            }
        }
    }

    private void createNewAddress(){
        for(EditItem editItem : btnItems)
        {
            if (StringUtils.isEmpty(editItem.getDetailText())){
                DialogUtil.showHint(String.format("%s 不能为空", editItem.getTitle()));
                return;
            }
        }

        String receiveName = btnItems.get(0).getDetailText();
        String receivePhone = btnItems.get(1).getDetailText();
        if(tmpSubdisBean == null){
            return;
        }
        Long subdisId = tmpSubdisBean.getId();
        String subName = tmpSubdisBean.getSubdisName();
        String addrName = tmpSubdisBean.getStreet();
        String houseNumber = btnItems.get(3).getDetailText();

        EnjoycityApiProxy.createReceiveAddress(receiveName, receivePhone,
                String.valueOf(subdisId), subName, addrName, houseNumber, createResponseCallback);
    }

    NetCallBack.NetTaskCallBack createResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"新增成功!","version":"1","data":{"val":"100980"}}
                    RspValue<String> retValue = (RspValue<String>) rspData;

                    doQuery(retValue.getValue());
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


    private void doQuery(final String id){
        if (StringUtils.isEmpty(id)){
            AddAddressActivity.this.onBackPressed();
            return;
        }

        NetCallBack.NetTaskCallBack queryResponseCallback = new NetCallBack.NetTaskCallBack<CommonAddrTemp,
                NetProcessor.Processor<CommonAddrTemp>>(
                new NetProcessor.Processor<CommonAddrTemp>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                    java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                        if (rspData != null){
                            RspListBean<CommonAddrTemp> retValue = (RspListBean<CommonAddrTemp>) rspData;
                            ReceiveAddressService.get().init(retValue.getValue());
                        }

                        ShopcartHelper.getInstance().refreshMemberOrderAddr(id);

                        setResult(Activity.RESULT_OK);

                        finish();
                    }

//                            @Override
//                            protected void processFailure(Throwable t, String errMsg) {
//                                super.processFailure(t, errMsg);
//                                Log.d("Nat: updateUserPassword.processFailure", errMsg);
//                                DialogUtil.showHint("修改登录密码失败");
//                            }
                }
                , CommonAddrTemp.class
                , MfhApplication.getAppContext())
        {
        };

        //保存到本地数据库，新增地址频率不大，所以这里查询所有地址信息，全量更新
        EnjoycityApiProxy.queryAllReceiveAddress(queryResponseCallback);

    }
}
