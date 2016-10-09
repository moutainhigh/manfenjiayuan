package com.manfenjiayuan.mixicook_vip.ui.address;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.api.reciaddr.ReciaddrMode;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 收货地址
 * Created by bingshanguxue on 16/3/17.
 */
public class ReciaddrPresenter {
    private IReciaddrView mIReciaddrView;
    private ReciaddrMode mReciaddrMode;

    public ReciaddrPresenter(IReciaddrView iReciaddrView) {
        this.mIReciaddrView = iReciaddrView;
        this.mReciaddrMode = new ReciaddrMode();
    }

    /**
     * 查询收货地址,不支持分页显示
     * */
    public void getAllAddrsByHuman(Long humanId){
        mReciaddrMode.getAllAddrsByHuman(humanId,
                new OnPageModeListener<Reciaddr>() {
                    @Override
                    public void onProcess() {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIReciaddrViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<Reciaddr> dataList) {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIReciaddrViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIReciaddrViewError(errorMsg);
                        }
                    }
                });
    }

    /**
     * 查询默认收货地址,不支持分页显示
     * */
    public void getDefaultAddrsByHuman(Long humanId){
        mReciaddrMode.getDefaultAddrsByHuman(humanId,
                new OnModeListener<Reciaddr>() {
                    @Override
                    public void onProcess() {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIReciaddrViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(Reciaddr data) {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIReciaddrViewSuccess(data);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIReciaddrViewError(errorMsg);
                        }
                    }
                });
    }

}
