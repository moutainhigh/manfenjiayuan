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
import com.bingshanguxue.skinloader.utils.SkinFileUtils;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.tenant.SassInfo;
import com.mfh.framework.api.tenant.TenantApi;
import com.mfh.framework.api.tenant.TenantInfo;
import com.mfh.framework.api.tenant.TenantMode;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * <h>选择租户，设置域名</h><br>
 * {@link HostServer}<br>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class HostServerFragment extends BaseProgressFragment {
    public static String EXTRA_KEY_MODE = "mode";
    public static String EXTRA_KEY_HOSTSERVER = "hostServer";


    //    @Bind(R.id.recyclerView)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private HostServerAdapter menuAdapter;
    private int mode = 0;
    private TenantMode mTenantMode;


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

        mTenantMode = new TenantMode();

        menuRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        initMenuRecyclerView();

        copySkinResources();

        listWhole();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void initMenuRecyclerView() {
        if (mode == 1) {
            mRLayoutManager = new GridLayoutManager(getActivity(), 4);
        } else {
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
                TenantInfo entity = menuAdapter.getEntity(position);

                updateHostserver(entity);
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
//        menuAdapter.setEntityList(getAdminMenus());
    }

    private void listWhole() {
        mTenantMode.listWhole(TenantApi.BizDomainType.RETAIL, TenantApi.DomainUrlType.NORMAL,
                null, new OnPageModeListener<TenantInfo>() {
                    @Override
                    public void onProcess() {
                        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<TenantInfo> dataList) {
                        if (pageInfo == null || pageInfo.getPageNo() == 1) {
                            menuAdapter.setEntityList(dataList);
                        } else {
                            menuAdapter.appendEntityList(dataList);
                        }
                        hideProgressDialog();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        hideProgressDialog();
                        DialogUtil.showHint(errorMsg);
//                        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "errorMsg", true);
                    }
                });
    }

    private void updateHostserver(final TenantInfo tenantInfo) {
        if (tenantInfo == null) {
            return;
        }
        ZLogger.df("选择租户：" + JSON.toJSONString(tenantInfo));

        String requestUrl = String.format("http://%s/pmc/",
                tenantInfo.getId());
        mTenantMode.getSaasInfo(requestUrl,
                tenantInfo.getSaasId(), new OnModeListener<SassInfo>() {
                    @Override
                    public void onProcess() {
                        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
                    }

                    @Override
                    public void onSuccess(SassInfo sassInfo) {
                        GlobalInstanceBase.getInstance().updateHostServer(tenantInfo, sassInfo);

                        hideProgressDialog();

                        Intent intent = new Intent();
//                        intent.putExtra("hostServer", hostServer);
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        ZLogger.ef(errorMsg);
                        hideProgressDialog();
                        DialogUtil.showHint(errorMsg);
                    }
                });
    }

    /**
     * 拷贝皮肤资源
     */
    private void copySkinResources() {
        try {
            String[] skinFiles = MfhApplication.getAm().list(SkinConfig.SKIN_DIR_NAME);
            for (String fileName : skinFiles) {
                File file = new File(SkinFileUtils.getSkinDir(MfhApplication.getAppContext()), fileName);
                if (!file.exists()) {
                    ZLogger.d("拷贝皮肤文件:" + fileName);
                    SkinFileUtils.copySkinAssetsToDir(MfhApplication.getAppContext(), fileName,
                            SkinFileUtils.getSkinDir(MfhApplication.getAppContext()));
                } else {
                    ZLogger.d("已经安装过皮肤文件:" + fileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }
}
