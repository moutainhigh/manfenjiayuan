package com.manfenjiayuan.loveshopping.mvp;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.entity.Subdis;
import com.mfh.framework.mvp.MvpBasePresenter;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class SubdisListPresenter extends MvpBasePresenter<ISubdisListView> {
    private SubdisMode mSubdisMode = new SubdisMode();
    /**
     * 加载采购订单
     * @param frontCategoryId 类目编号
     * */
    public void listSubdis(PageInfo pageInfo, String cityId, String subdisName){
        mSubdisMode.listSubdis(pageInfo, cityId, subdisName, new OnPageModeListener<Subdis>() {
            @Override
            public void onProcess() {
                if (getView() != null) {
                    getView().onQuerySubdisProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<Subdis> dataList) {
                if (getView() != null) {
                    getView().onQuerySubdisSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (getView() != null) {
                    getView().onQuerySubdisError(errorMsg);
                }
            }
        });
    }


}
