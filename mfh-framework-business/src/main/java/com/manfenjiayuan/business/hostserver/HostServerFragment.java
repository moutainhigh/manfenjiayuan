package com.manfenjiayuan.business.hostserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.skinloader.config.SkinConfig;
import com.bingshanguxue.skinloader.listener.ILoaderListener;
import com.bingshanguxue.skinloader.loader.SkinManager;
import com.bingshanguxue.skinloader.utils.SkinFileUtils;
import com.manfenjiayuan.business.AppIconManager;
import com.manfenjiayuan.business.R;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseProgressFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * <h>选择租户，设置域名</h><br>
 * {@link HostServer}<br>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class HostServerFragment extends BaseProgressFragment {
    public static String EXTRA_KEY_MODE= "mode";
    public static String EXTRA_KEY_HOSTSERVER= "hostServer";


//    @Bind(R.id.toolbar)
//    Toolbar toolbar;
//    @Bind(R.id.recyclerView)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private HostServerAdapter menuAdapter;
    private int mode = 0;


    public static HostServerFragment newInstance(Bundle args) {
        HostServerFragment fragment = new HostServerFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_hostserver;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
//            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            mode = args.getInt(EXTRA_KEY_MODE, 0);
        }

//        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
//        toolbar.setTitle("选择租户");
////        setSupportActionBar(toolbar);
////        toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
////        toolbar.setNavigationOnClickListener(
////                new View.OnClickListener() {
////                    @Override
////                    public void onClick(View v) {
////                        getActivity().onBackPressed();
////                    }
////                });
//        // Set an OnMenuItemClickListener to handle menu item clicks
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_close) {
//                    getActivity().onBackPressed();
//                }
//                return true;
//            }
//        });
//
////        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_normal);

        menuRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        initMenuRecyclerView();

        copySkinResources();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void initMenuRecyclerView() {
        if (mode == 1){
            mRLayoutManager = new GridLayoutManager(getActivity(), 4);
        }
        else{
            mRLayoutManager = new GridLayoutManager(getActivity(), 6);
        }
        menuRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

        menuAdapter = new HostServerAdapter(MfhApplication.getAppContext(), null);
        menuAdapter.setOnAdapterLitener(new HostServerAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                HostServer entity = menuAdapter.getEntity(position);

                updateHostserver(entity);
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
        menuAdapter.setEntityList(getAdminMenus());
    }

    /**
     * 获取菜单
     */
    public synchronized List<HostServer> getAdminMenus() {
        List<HostServer> functionalList = new ArrayList<>();
        String packageName = MfhApplication.getAppContext().getPackageName();
        if ("com.manfenjiayuan.pda_supermarket".equals(packageName)){
            functionalList.add(new HostServer(1L,
                    "米西厨房", "admin.mixicook.com",
                    "http://admin.mixicook.com/pmc",
                    R.mipmap.ic_textlogo_mixicook, R.mipmap.ic_launcher_mixicook,
                    AppIconManager.ACTIVITY_ALIAS_MIXICOOK, "mixicook.skin"));
            functionalList.add(new HostServer(2L,
                    "满分邻居", "lanlj.mixicook.com",
                    "http://lanlj.mixicook.com/pmc",
                    R.mipmap.ic_textlogo_lanlj, R.mipmap.ic_launcher_lanlj,
                    AppIconManager.ACTIVITY_ALIAS_LANLJ, "lanlj.skin"));
            functionalList.add(new HostServer(3L,
                    "千万加", "qianwj.mixicook.com",
                    "http://qianwj.mixicook.com/pmc",
                    R.mipmap.ic_textlogo_qianwj, R.mipmap.ic_launcher_qianwj,
                    AppIconManager.ACTIVITY_ALIAS_QIANWJ, "qianwj.skin"));
        }
        else if ("com.mfh.litecashier".equals(packageName)){
            functionalList.add(new HostServer(1L,
                    "米西厨房", "admin.mixicook.com",
                    "http://admin.mixicook.com/pmc",
                    R.mipmap.ic_textlogo_mixicook, R.mipmap.ic_launcher_mixicook,
                    AppIconManager.ACTIVITY_ALIAS_CASHIER_MIXICOOK, "mixicook.skin"));
            functionalList.add(new HostServer(2L,
                    "满分邻居", "lanlj.mixicook.com",
                    "http://lanlj.mixicook.com/pmc",
                    R.mipmap.ic_textlogo_lanlj, R.mipmap.ic_launcher_lanlj,
                    AppIconManager.ACTIVITY_ALIAS_CASHIER_LANLJ, "lanlj.skin"));
            functionalList.add(new HostServer(3L,
                    "千万加", "qianwj.mixicook.com",
                    "http://qianwj.mixicook.com/pmc",
                    R.mipmap.ic_textlogo_qianwj, R.mipmap.ic_launcher_qianwj,
                    AppIconManager.ACTIVITY_ALIAS_CASHIER_QIANWJ, "qianwj.skin"));
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()){
                functionalList.add(new HostServer(4L,
                        "米西厨房-测试", "dev.mixicook.com",
                        "http://dev.mixicook.com/pmc",
                        R.mipmap.ic_textlogo_qianwj, R.mipmap.ic_launcher_mixicook,
                        AppIconManager.ACTIVITY_ALIAS_CASHIER_MIXICOOK, "mixicook.skin"));
            }
        }

        return functionalList;
    }

    private void updateHostserver(final HostServer hostServer){
        if (hostServer == null){
            return;
        }
        ZLogger.d("选择域名服务：" + JSON.toJSONString(hostServer));

        //切换皮肤
        SkinManager.getInstance().loadSkin(hostServer.getSkinName(),
                new ILoaderListener() {
                    @Override
                    public void onStart() {
                        ZLogger.d("正在切换主题");
//                        dialog.show();
                        Intent data = new Intent();
                        data.putExtra("hostServer", hostServer);
                        getActivity().setResult(Activity.RESULT_OK, data);
                        getActivity().finish();
                    }

                    @Override
                    public void onSuccess() {
                        ZLogger.d("切换主题成功");
                        DialogUtil.showHint("切换租户成功");
//                        dialog.dismiss();
                        Intent data = new Intent();
                        data.putExtra("hostServer", hostServer);
                        getActivity().setResult(Activity.RESULT_OK, data);
                        getActivity().finish();
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        ZLogger.d("切换主题失败:" + errMsg);
                        DialogUtil.showHint(errMsg);
//                        dialog.dismiss();
                        Intent data = new Intent();
                        data.putExtra("hostServer", hostServer);
                        getActivity().setResult(Activity.RESULT_OK, data);
                        getActivity().finish();
                    }

                    @Override
                    public void onProgress(int progress) {
                        ZLogger.d("主题皮肤文件下载中:" + progress);
                    }
                }

        );
    }

    private void copySkinResources(){
        try {
            String[] skinFiles = MfhApplication.getAm().list(SkinConfig.SKIN_DIR_NAME);
            for (String fileName : skinFiles) {
                File file = new File(SkinFileUtils.getSkinDir(MfhApplication.getAppContext()), fileName);
                if (!file.exists()){
                    ZLogger.d("拷贝皮肤文件:" + fileName);
                    SkinFileUtils.copySkinAssetsToDir(MfhApplication.getAppContext(), fileName,
                            SkinFileUtils.getSkinDir(MfhApplication.getAppContext()));
                }
                else{
                    ZLogger.d("已经安装过皮肤文件:" + fileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

}
