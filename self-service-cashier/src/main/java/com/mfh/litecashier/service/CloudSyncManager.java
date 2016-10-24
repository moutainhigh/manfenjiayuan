package com.mfh.litecashier.service;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

/**
 * 云端数据同步
 * Created by bingshanguxue on 7/6/16.
 */
public class CloudSyncManager {

    private static CloudSyncManager instance = null;

    /**
     * 返回 DataSyncManagerImpl 实例
     *
     * @return CloudSyncManager
     */
    public static CloudSyncManager get() {
        if (instance == null) {
            synchronized (CloudSyncManager.class) {
                if (instance == null) {
                    instance = new CloudSyncManager();
                }
            }
        }
        return instance;
    }

    /**
     * 2016-07-06 固定同步米西生鲜商品，其他批发商的商品暂时放在米西生鲜里做前台类目
     * */
    public void importFromChainSku(){
        importFromChainSku(135799L, String.valueOf(CateApi.BACKEND_CATE_BTYPE_FRESH));
    }

    /**
     * 从批发商导入某个类目的商品到当前门店
     * 注意此操作只会同步批发商的商品库到门店的商品库，POS机的商品库同步是另外一个逻辑
     *
     * @param sendTenantId 平台上的某个批发商
     * @param cateType     类目
     */
    public void importFromChainSku(Long sendTenantId, String cateType) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            return;
        }

        final String startCursorKey = String.format("%s_%d_%s",
                SharedPreferencesHelper.PK_S_IMPORT_FROMCHAINSKU_STARTCURSOR, sendTenantId, cateType);
        String startCursorValue = SharedPreferencesHelper.getText(startCursorKey, "");


        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String newCursor = retValue.getValue();
                            SharedPreferencesHelper.set(startCursorKey, newCursor);
                            ZLogger.df(String.format("导入批发商商品数据成功%s:%s", startCursorKey, newCursor));
                        }
//                        nextStep();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("导入批发商商品数据失败," + errMsg);
//                        nextStep();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        ScGoodsSkuApiImpl.importFromChainSku(sendTenantId, cateType,
                startCursorValue, responseCallback);
    }

    public void remoteControl(){

    }

}
