package com.manfenjiayuan.pda_supermarket;

import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetProcessor;

/**
 * Created by bingshanguxue on 5/27/16.
 */
public class DataSyncManager {
    private static DataSyncManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static DataSyncManager getInstance() {
        if (instance == null) {
            synchronized (DataSyncManager.class) {
                if (instance == null) {
                    instance = new DataSyncManager();
                }
            }
        }
        return instance;
    }

    /**
     * 通知更新SKU
     * */
    public void notifyUpdateSku(){
        NetProcessor.ComnProcessor processor = new NetProcessor.ComnProcessor<EmbMsg>() {
            @Override
            protected void processOperResult(EmbMsg result) {
//                doAfterSendSuccess(result);
                ZLogger.d("通知更新SKU商品");
            }
        };
        EmbMsgService msgService = ServiceFactory.getService(EmbMsgService.class,
                MfhApplication.getAppContext());
        msgService.sendText(MfhLoginService.get().getCurrentGuId(),
                MfhLoginService.get().getCurrentGuId(),
                IMBizType.TENANT_SKU_UPDATE, "update sku", processor);
    }
}
