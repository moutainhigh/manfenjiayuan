package com.mfh.enjoycity.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.StringUtils;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.mfh.framework.api.mobile.MobileApi;

import java.util.List;

import butterknife.Bind;


/**
 * 代金券
 * */
public class CarponActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind({ R.id.item_scan, R.id.item_help })
    List<SettingsItem> btnItems;


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_carpon;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_carpon);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CarponActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btnItems.get(0).setOnClickListener(myOnClickListener);
 btnItems.get(1).setOnClickListener(myOnClickListener);
    }

    private View.OnClickListener myOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.item_scan:{
                    //需要处理扫描结果
                    Intent intent = new Intent(CarponActivity.this, ScanActivity.class);
                    startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_ZXING_QRCODE);
                }
                break;
                case R.id.item_help:{

                    StaticWebActivity.actionStart(CarponActivity.this, MobileApi.URL_APP_DESCRIPTION);
                }
                break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.ACTIVITY_REQUEST_CODE_ZXING_QRCODE)
        {
            if(resultCode == Activity.RESULT_OK){
                try{
                    Bundle bundle = data.getExtras();
                    String resultText = bundle.getString("result", "");
//                Bitmap barcode =  (Bitmap)bundle.getParcelable("bitmap");//扫描截图

                    if(StringUtils.isUrl(resultText)){
                        UIHelper.showUrlOption(CarponActivity.this, resultText);
                    }
                    else{
                        UIHelper.showCopyTextOption(CarponActivity.this, resultText);
                    }
                }catch(Exception ex){
                    //TransactionTooLargeException
                    ZLogger.e(ex.toString());
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
