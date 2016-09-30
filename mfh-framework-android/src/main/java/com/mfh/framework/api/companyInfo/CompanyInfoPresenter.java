package com.mfh.framework.api.companyInfo;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class CompanyInfoPresenter {
    private ICompanyInfoView mICompanyInfoView;
    private CompanyInfoMode mCompanyInfoMode;

    public CompanyInfoPresenter(ICompanyInfoView iCompanyInfoView) {
        this.mICompanyInfoView = iCompanyInfoView;
        this.mCompanyInfoMode = new CompanyInfoMode();
    }

    /**
     * 获取门店
     */
    public void findPublicCompanyInfo(PageInfo pageInfo, String nameLike, Integer abilityItem) {
        mCompanyInfoMode.findPublicCompanyInfo(pageInfo, nameLike, abilityItem,
                new OnPageModeListener<CompanyInfo>() {
            @Override
            public void onProcess() {
                if (mICompanyInfoView != null) {
                    mICompanyInfoView.onICompanyInfoViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
                if (mICompanyInfoView != null) {
                    mICompanyInfoView.onICompanyInfoViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mICompanyInfoView != null) {
                    mICompanyInfoView.onICompanyInfoViewError(errorMsg);
                }
            }
        });

    }

    /**
     * 获取门店
     */
    public void findServicedNetsForUserPos(Long cityId, String userLng, String userLat,
                                           PageInfo pageInfo) {
        mCompanyInfoMode.findServicedNetsForUserPos(cityId, userLng, userLat,pageInfo,
                new OnPageModeListener<CompanyInfo>() {
                    @Override
                    public void onProcess() {
                        if (mICompanyInfoView != null) {
                            mICompanyInfoView.onICompanyInfoViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
                        if (mICompanyInfoView != null) {
                            mICompanyInfoView.onICompanyInfoViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mICompanyInfoView != null) {
                            mICompanyInfoView.onICompanyInfoViewError(errorMsg);
                        }
                    }
                });

    }
}
