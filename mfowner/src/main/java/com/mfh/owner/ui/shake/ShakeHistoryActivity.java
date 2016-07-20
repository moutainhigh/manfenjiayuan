package com.mfh.owner.ui.shake;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.owner.R;
import com.mfh.owner.adapter.ShakeHistoryAdapter;
import com.mfh.owner.ui.web.ComnJBH5Activity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


/**
 * 摇一摇·历史
 * */
public class ShakeHistoryActivity extends BaseActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.listView) ListView listView;
    private ShakeHistoryAdapter hisrotyAdapter;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_shake_history;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShakeHistoryService historyService = ServiceFactory.getService(ShakeHistoryService.class.getName());
        hisrotyAdapter = new ShakeHistoryAdapter(this);
        listView.setAdapter(hisrotyAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ShakeHistoryEntity entity = (ShakeHistoryEntity)adapterView.getAdapter().getItem(i);
//                ShakeHistoryEntity entity = (ShakeHistoryEntity)hisrotyAdapter.getItem(i);

                ComnJBH5Activity.actionStart(ShakeHistoryActivity.this, entity.getPageUrl(), true, false, -1);
            }
        });

        List<ShakeHistoryEntity> historyEntityList =  historyService.getDao().queryAll(new PageInfo(1, 100));
        if(historyEntityList != null && historyEntityList.size() > 0){
            hisrotyAdapter.setData(historyEntityList);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shake_history, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 初始化导航栏
     * */
    @Override
    protected void initToolBar() {
        super.initToolBar();
        toolbar.setTitle(R.string.topbar_title_shake_history);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_delete) {
                    showClearAlert(ShakeHistoryActivity.this);
                }
                return true;
            }
        });
    }

    private void showClearAlert(final Context context) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setMessage(R.string.dialog_message_clean_shake_history);
        dialog.setPositiveButton(R.string.dialog_button_clean, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //清空后刷新
                ShakeHistoryService historyService = ServiceFactory.getService(ShakeHistoryService.class.getName());
//                historyService.getDao().clear(SharedPreferencesHelper.getUserGuid());
                historyService.clear();

                List<ShakeHistoryEntity> historyEntityList = historyService.getDao().queryAll(new PageInfo(1, 100));
                if (historyEntityList != null && historyEntityList.size() > 0) {
                    hisrotyAdapter.setData(historyEntityList);
                } else {
                    hisrotyAdapter.setData(new ArrayList<ShakeHistoryEntity>());
                }

                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
