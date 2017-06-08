package com.mfh.litecashier.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bingshanguxue.cashier.database.service.PosTopupService;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DataCleanManager;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.GlobalInstance;


/**
 * 垃圾回收
 * Created by bingshanguxue on 04/05/2017.
 */

public class TrushService extends IntentService {
    private int count = 0;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TrushService() {
        super("TrushService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            try {
                this.processOnHandleIntent(intent);
            } catch (Exception e) {
                ZLogger.ef(e.toString());
            }
        }
    }

    private void processOnHandleIntent(Intent intent) {
        if (count != 0) {
            ZLogger.d("正在进行垃圾回收...");
            return;
        }

        ZLogger.d("垃圾回收【开始】...");
        Bundle bundle = intent.getExtras();
        if (bundle.getBoolean("isFactory")) {
            ZLogger.d("恢复出厂设置，清空数据");
            CashierHelper.clearOldPosOrder(0);//收银订单
            PosTopupService.get().deleteOldData(0);
            ZLogger.deleteOldFiles(0);
            SMScaleSyncManager2.deleteOldFiles(0);
            EmbMsgService.getInstance().clearReduantData(0);

            //删除缓存
            ACacheHelper.clear();
            //清除数据缓存
            DataCleanManager.clearCache(CashierApp.getAppContext());
            GlobalInstance.getInstance().reset();
        } else {
            ZLogger.d("清除过期数据");
            CashierHelper.clearOldPosOrder(30);//收银订单
            PosTopupService.get().deleteOldData(30);
            ZLogger.deleteOldFiles(7);
            SMScaleSyncManager2.deleteOldFiles(1);
            EmbMsgService.getInstance().clearReduantData(7);

            AppHelper.clearCacheData();
        }
        ZLogger.d("垃圾回收【结束】...");
    }

}
