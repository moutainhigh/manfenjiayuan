package com.manfenjiayuan.business.mode;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.InvSkuStoreHttpManager;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

/**
 * {@link InvSkuGoods}
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSkuGoodsMode {

    public void getByBarcodeMust(String barcode, final OnModeListener<InvSkuGoods> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("条码不能为空");
            }
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("barcode", barcode);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().getByBarcodeMust(options,
                new Subscriber<InvSkuGoods>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.d("查询商品失败：" + e.toString());

                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(InvSkuGoods invSkuGoods) {
                        if (listener != null) {
                            listener.onSuccess(invSkuGoods);
                        }
                    }
                });
    }


}
