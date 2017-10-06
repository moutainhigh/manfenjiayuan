package com.manfenjiayuan.business.mvp.view;

import com.mfh.framework.mvp.MvpView;
import com.mfh.framework.rxapi.bean.CompanyHuman;
import com.mfh.framework.rxapi.bean.Human;

/**
 * 搜索会员信息
 * Created by bingshanguxue on 16/3/21.
 */
public interface ICompanyHumanView extends MvpView {
    void onICompanyHumanViewLoading();
    void onICompanyHumanViewError(int type, String content, String errorMsg);
//    void onICompanyHumanViewSuccess(PageInfo pageInfo, List<InvCheckOrder> dataList);
    void onICompanyHumanViewSuccess(int type, String content, CompanyHuman human);
}
