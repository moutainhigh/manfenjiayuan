package com.manfenjiayuan.business.storeIn;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.ScGoodsSkuHttpManager;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

/**
 * 商超库存商品
 * Created by bingshanguxue on 16/3/17.
 */
public class StoreInPresenter {
    private IStoreInView mStoreInView;

    public StoreInPresenter(IStoreInView iStoreInView) {
        this.mStoreInView = iStoreInView;
    }

    public void storeIn(String jsonStr, Integer storeType) {

        if (mStoreInView != null) {
            mStoreInView.onIStoreInViewProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonStr);
        options.put("storeType", String.valueOf(StoreType.SUPERMARKET));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ScGoodsSkuHttpManager.getInstance().storeIn(options, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.e("商品建档失败: " + e.toString());
                if (mStoreInView != null) {
                    mStoreInView.onIStoreInViewError(e.getMessage());
                }
            }

            @Override
            public void onNext(String s) {
                if (mStoreInView != null) {
                    mStoreInView.onIStoreInViewSuccess();
                }
            }
        });
    }

}
