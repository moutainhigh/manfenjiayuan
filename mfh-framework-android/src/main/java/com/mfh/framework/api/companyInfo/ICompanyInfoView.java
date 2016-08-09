package com.mfh.framework.api.companyInfo;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface ICompanyInfoView extends MvpView {
    void onICompanyInfoViewProcess();
    void onICompanyInfoViewError(String errorMsg);
    void onICompanyInfoViewSuccess(PageInfo pageInfo, List<CompanyInfo> dataList);
}
