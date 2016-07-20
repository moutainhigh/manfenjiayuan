package com.mfh.enjoycity.ui;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.AddressAdapter;
import com.mfh.enjoycity.database.ReceiveAddressEntity;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.uikit.UIHelper;

import java.util.List;

import butterknife.Bind;


/**
 * 地址管理
 * */
public class AddressManagerActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;
    @Bind(R.id.listView)
    ListView listView;
    private AddressAdapter addressAdapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_address_manager;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_address_manager);//必须在setSupportActionBar(toolbar);之前设置才有效
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddressManagerActivity.this.onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.acton_add) {
                    //TODO:添加新地址
                    UIHelper.startActivity(AddressManagerActivity.this, AddAddressActivity.class);
//                    AddressManagerActivity.this.onBackPressed();
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

        ReceiveAddressService historyService = ServiceFactory.getService(ReceiveAddressService.class.getName());
        addressAdapter = new AddressAdapter(this);
        listView.setAdapter(addressAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                ShakeHistoryEntity entity = (ShakeHistoryEntity) adapterView.getAdapter().getItem(i);
//                ShakeHistoryEntity entity = (ShakeHistoryEntity)hisrotyAdapter.getItem(i);

                //TODO,编辑地址
            }
        });

        List<ReceiveAddressEntity> historyEntityList =  historyService.getDao().queryAll(new PageInfo(1, 100));
        if(historyEntityList != null && historyEntityList.size() > 0){
            addressAdapter.setData(historyEntityList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add, menu);

        return super.onCreateOptionsMenu(menu);
    }

}
