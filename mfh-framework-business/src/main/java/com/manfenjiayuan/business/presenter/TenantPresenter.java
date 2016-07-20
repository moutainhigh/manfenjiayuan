package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.mode.TenantMode;
import com.manfenjiayuan.business.view.ITenantView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class TenantPresenter {
    private ITenantView iTenantView;
    private TenantMode tenantMode;

    public TenantPresenter(ITenantView iTenantView) {
        this.iTenantView = iTenantView;
        this.tenantMode = new TenantMode();
    }

    /**
     * 获取门店
     */
    public void getTenants(PageInfo pageInfo, String nameLike, Integer abilityItem) {
        tenantMode.getTenants(pageInfo, nameLike, abilityItem, new OnPageModeListener<CompanyInfo>() {
            @Override
            public void onProcess() {
                if (iTenantView != null) {
                    iTenantView.onProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
                if (iTenantView != null) {
                    iTenantView.onSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (iTenantView != null) {
                    iTenantView.onError(errorMsg);
                }
            }
        });

    }
}
