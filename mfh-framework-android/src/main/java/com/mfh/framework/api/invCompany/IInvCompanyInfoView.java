package com.mfh.framework.api.invCompany;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface IInvCompanyInfoView extends MvpView {
    void onProcess();
    void onError(String errorMsg);
    void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList);
}
