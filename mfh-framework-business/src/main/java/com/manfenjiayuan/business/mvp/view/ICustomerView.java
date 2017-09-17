package com.manfenjiayuan.business.mvp.view;

import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.mvp.MvpView;

/**
 * 搜索会员信息
 * Created by bingshanguxue on 16/3/21.
 */
public interface ICustomerView extends MvpView {
    void onICustomerViewLoading();
    void onICustomerViewError(int type, String content, String errorMsg);
//    void onICustomerViewSuccess(PageInfo pageInfo, List<InvCheckOrder> dataList);
    void onICustomerViewSuccess(int type, String content, Human human);
}
