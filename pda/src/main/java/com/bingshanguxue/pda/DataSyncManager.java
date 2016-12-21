package com.bingshanguxue.pda;

import com.bingshanguxue.pda.utils.SharedPrefesManagerUltimate;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetProcessor;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by bingshanguxue on 5/27/16.
 */
public class DataSyncManager {
    public static final int MAX_SYNC_PAGESIZE = 40;


    private static DataSyncManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return DataSyncManager
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
        msgService.sendText(MfhLoginService.get().getGuidLong(),
                MfhLoginService.get().getGuidLong(), null, null,
                IMBizType.TENANT_SKU_UPDATE, "update sku", processor);
    }

    /**
     * 获取POS商品库同步时间
     */
    public static String getPosLastUpdateCursor() {
        String startCursor = SharedPrefesManagerUltimate.getSyncProductsStartcursor();
        ZLogger.df(String.format("上次商品更新时间(%s)。", startCursor));

//        //得到指定模范的时间
        if (!StringUtils.isEmpty(startCursor)) {
            try {
                Date d1 = TimeCursor.InnerFormat.parse(startCursor);
                Date rightNow = new Date();
                if (d1.compareTo(rightNow) > 0) {
                    startCursor = TimeCursor.InnerFormat.format(rightNow);
                    ZLogger.df(String.format("上次商品更新时间大于当前时间，使用当前时间(%s)。", startCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(String.format("获取POS商品库同步时间失败: %s", e.toString()));
            }
        }

        return startCursor;
    }
}
