package com.mfh.framework.api.invCompany;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 批发商
 * Created by bingshanguxue on 16/3/17.
 */
public class InvCompanyPresenter {
    private IInvCompanyInfoView mIInvCompanyInfoView;
    private InvCompanyMode mInvCompanyMode;

    public InvCompanyPresenter(IInvCompanyInfoView iTenantView) {
        this.mIInvCompanyInfoView = iTenantView;
        this.mInvCompanyMode = new InvCompanyMode();
    }


    /**
     * 获取门店批发商
     * */
    public void list(PageInfo pageInfo, String shortCodeLike){
        mInvCompanyMode.list(pageInfo, shortCodeLike,
                new OnPageModeListener<CompanyInfo>() {
            @Override
            public void onProcess() {
                if (mIInvCompanyInfoView != null){
                    mIInvCompanyInfoView.onIInvCompanyInfoViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
                if (mIInvCompanyInfoView != null){
                    mIInvCompanyInfoView.onIInvCompanyInfoViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIInvCompanyInfoView != null){
                    mIInvCompanyInfoView.onIInvCompanyInfoViewError(errorMsg);
                }
            }
        });
    }


}
