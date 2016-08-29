package com.mfh.petitestock.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.serialport.api.SerialPort;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration2;
import com.mfh.petitestock.R;
import com.mfh.petitestock.ui.adapter.HomeAdapter;
import com.mfh.petitestock.bean.wrapper.HomeMenu;
import com.zkc.Service.CaptureService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


/**
 * 库存－－出库
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class StockInFragment extends BaseFragment {

    @Bind(R.id.et_barcode)
    EditText etBarCode;
    @Bind(R.id.menu_option)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private HomeAdapter menuAdapter;

    private BroadcastReceiver scanBroadcastReceiver;


    public static StockInFragment newInstance(Bundle args) {
        StockInFragment fragment = new StockInFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stockin;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Intent newIntent = new Intent(getActivity(), CaptureService.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startService(newIntent);

        registerReceiver();

        try {
            initShortcutMenu();
        } catch (Exception e){
            ZLogger.e(e.toString());
        }

        SerialPort.CleanBuffer();
        CaptureService.scanGpio.openScan();
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (scanBroadcastReceiver != null) {
            getActivity().unregisterReceiver(scanBroadcastReceiver);
        }
    }


    /**
     * 初始化快捷菜单
     */
    private void initShortcutMenu() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 4);
        menuRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        menuRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
                getResources().getColor(R.color.gray), 0.5f,
                getResources().getColor(R.color.gray), 1.0f,
                getResources().getColor(R.color.gray), 0.5f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        menuAdapter = new HomeAdapter(getActivity(), null);
        menuAdapter.setOnAdapterLitener(new HomeAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onCommandSelected(HomeMenu option) {
            }

        });
        menuRecyclerView.setAdapter(menuAdapter);


        List<HomeMenu> menuDatas = new ArrayList<>();
        menuDatas.add(new HomeMenu(HomeMenu.OPTION_ID_PACKAGE, "包裹", R.mipmap.ic_launcher));
        menuAdapter.setOptions(menuDatas);
    }


    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.zkc.scancode");
        scanBroadcastReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                String text = intent.getExtras().getString("code");
                ZLogger.i("MyBroadcastReceiver code:" + text);
                etBarCode.setText(text);
            }
        };
        getActivity().registerReceiver(scanBroadcastReceiver, intentFilter);
    }

}
