package com.manfenjiayuan.pda_supermarket.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.database.service.InvCheckGoodsService;
import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.database.entity.ShelveEntity;
import com.bingshanguxue.pda.database.entity.InvCheckGoodsEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.ShelveService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.ResponseBody;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;

import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * POS--数据同步
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DataSyncService {
    public static final int SYNC_STEP_NA = -1;
    public static final int SYNC_STEP_UPLOAD_STOCKTAKE = SYNC_STEP_NA + 1;// 同步库存盘点

    private static final int MAX_SYNC_PRODUCTS_PAGESIZE = 50;
    private static final int MAX_SYNC_STOCKTAKE_PAGESIZE = 10;
    private static final int ORDER_STRATEGY_UPLOAD = 1;

    private boolean bSyncInProgress = false;//是否正在同步
    //当前同步进度
    private int nextStep = SYNC_STEP_NA;

    private static DataSyncService instance = null;
    /**
     * 返回 DataSyncService 实例
     * @return
     */
    public static DataSyncService get() {

        if (instance == null) {
            synchronized (DataSyncService.class) {
                if (instance == null) {
                    instance = new DataSyncService();
                }
            }
        }
        return instance;
    }

    /**
     * 下载更新POS数据库
     * */
    public synchronized void sync(){
        if (bSyncInProgress){
            ZLogger.d("DataSync--正在同步盘点机数据...");
            return;
        }

        processStep(SYNC_STEP_UPLOAD_STOCKTAKE, SYNC_STEP_NA);
    }

    public void sync(int step){
        if (bSyncInProgress){
            ZLogger.d("DataSync--正在同步盘点机数据...");
            return;
        }

        processStep(step, SYNC_STEP_NA);
    }

    /**
     * 下一步
     */
    private void nextStep() {
        processStep(nextStep);
    }


    private void processStep(int step, int nextStep) {
        this.nextStep = nextStep;
        switch (step) {
            case SYNC_STEP_UPLOAD_STOCKTAKE: {
                uploadStockTake();
            }
            break;
            default: {
                bSyncInProgress = false;
                ZLogger.d("DataSync--盘点机数据同步结束...");
//                EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SYNC_DATA_FINISHED));
            }
            break;
        }
    }

    private void processStep(int step) {
        this.nextStep = step + 1;
        switch (step) {
            case SYNC_STEP_UPLOAD_STOCKTAKE: {
                uploadStockTake();
            }
            break;
            default: {
                bSyncInProgress = false;
            }
            break;
        }
    }

    private void networkError(){
        ZLogger.d("DataSync--网络未连接，暂停同步盘点机数据。");
        bSyncInProgress = false;
//        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    private void sessionError(){
        ZLogger.d("DataSync--会话已失效，暂停同步盘点机数据。");
        bSyncInProgress = false;
//        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }


    /**
     * 同步盘点记录，逐条提交
     * */
    private void uploadStockTake(){
        if (!MfhLoginService.get().haveLogined()){
            EventBus.getDefault().post(new StockTakeSyncEvent(StockTakeSyncEvent.EVENT_ID_SYNC_FAILED));
            sessionError();
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())){
            EventBus.getDefault().post(new StockTakeSyncEvent(StockTakeSyncEvent.EVENT_ID_SYNC_FAILED));
            networkError();
            return;
        }

        String lastCursor = SharedPreferencesHelper.getStocktakeLastUpdate();

//        List<InvCheckGoodsEntity> entityList = InvCheckGoodsService.get()
//                .queryAllBy(String.format("createdDate > '%s' and syncStatus < '%d'",
//                        lastCursor, InvCheckGoodsEntity.SYNC_STATUS_FINISHED), new PageInfo(1, MAX_SYNC_STOCKTAKE_PAGESIZE));
        List<InvCheckGoodsEntity> entityList = InvCheckGoodsService.get()
                .queryAllBy(String.format("syncStatus < '%d'", L2CSyncStatus.SYNC_STATUS_FINISHED),
                        new PageInfo(1, MAX_SYNC_STOCKTAKE_PAGESIZE));
        if (entityList == null || entityList.size() < 1){
            ZLogger.d(String.format("DataSync--没有盘点记录需要上传(%s)。", lastCursor));
            EventBus.getDefault().post(new StockTakeSyncEvent(StockTakeSyncEvent.EVENT_ID_SYNC_FINISHED));
            nextStep();
            return;
        }
        final InvCheckGoodsEntity entity = entityList.get(0);

        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("proSkuId", entity.getProSkuId());
        item.put("barcode", entity.getBarcode());
        item.put("quantityCheck", entity.getQuantityCheck());
        item.put("updateHint", entity.getUpdateHint());
        item.put("rackId", entity.getShelfNumber());//货架编号
        items.add(item);

        //保存游标
        Date newCursor = entity.getCreatedDate();

        bSyncInProgress = true;

        final Date finalNewCursor = newCursor;

        NetCallBack.RawNetTaskCallBack responseCallback = new NetCallBack.RawNetTaskCallBack<String,
                NetProcessor.RawProcessor<String>>(
                new NetProcessor.RawProcessor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("DataSync--上传盘点记录失败: " + errMsg);
                        EventBus.getDefault().post(new StockTakeSyncEvent(StockTakeSyncEvent.EVENT_ID_SYNC_FAILED));
                        nextStep();
                    }

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        try{
                            switch (rspBody.getRetCode()){
                                //盘点成功，检查是否还有盘点需要上传
                                case "0":{
                                    ZLogger.d("DataSync--盘点成功: " + rspBody.getReturnInfo());
                                    //需要更新订单流水
                                    SharedPreferencesHelper.setStocktakeLastUpdate(finalNewCursor);
                                    entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_FINISHED);
                                    InvCheckGoodsService.get().saveOrUpdate(entity);
                                }
                                break;
                                // 系统异常，需要重试
                                case "1":{
                                    ZLogger.d("DataSync--盘点失败: " + rspBody.getReturnInfo());
                                    entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_SYSTEM_ERROR);
                                    InvCheckGoodsService.get().saveOrUpdate(entity);
                                }
                                break;
                                // 不能提交
                                case "2":{
                                    ZLogger.d("DataSync--盘点失败: " + rspBody.getReturnInfo());
                                    entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_ERROR);
                                    InvCheckGoodsService.get().save(entity);
                                }
                                break;
                                // 参数异常
                                case "5":{
                                    ZLogger.d("DataSync--盘点失败: " + rspBody.getReturnInfo());
                                    entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_PARAMS_ERROR);
                                    InvCheckGoodsService.get().saveOrUpdate(entity);
                                }
                                break;
                                default:{
                                    ZLogger.d("DataSync--盘点失败: " + rspBody.getReturnInfo());
                                    entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_ERROR);
                                    InvCheckGoodsService.get().saveOrUpdate(entity);
                                }
                                break;
                            }
                        }
                        catch (Exception e){
                            ZLogger.e(e.toString());
                        }

                        //继续盘点
                        uploadStockTake();
                    }

                    @Override
                    public void processResult(IResponseData rspData) {

                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };

        InvOrderApiImpl.invCheckOrderBatchCommitItems(entity.getOrderId(), items.toJSONString(), responseCallback);
    }

    /**
     * 同步货架绑定纪录，逐条提交
     * */
    private void uploadShelvesBindRecords(){
        if (!MfhLoginService.get().haveLogined()){
            EventBus.getDefault().post(new StockTakeSyncEvent(StockTakeSyncEvent.EVENT_ID_SYNC_FAILED));
            sessionError();
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())){
            EventBus.getDefault().post(new StockTakeSyncEvent(StockTakeSyncEvent.EVENT_ID_SYNC_FAILED));
            networkError();
            return;
        }

        String lastCursor = SharedPreferencesHelper.getStocktakeLastUpdate();

//        List<InvCheckGoodsEntity> entityList = InvCheckGoodsService.get()
//                .queryAllBy(String.format("createdDate > '%s' and syncStatus < '%d'",
//                        lastCursor, InvCheckGoodsEntity.SYNC_STATUS_FINISHED), new PageInfo(1, MAX_SYNC_STOCKTAKE_PAGESIZE));
        List<ShelveEntity> entityList = ShelveService.get()
                .queryAllBy(String.format("syncStatus < '%d'", L2CSyncStatus.SYNC_STATUS_FINISHED),
                        new PageInfo(1, MAX_SYNC_STOCKTAKE_PAGESIZE));
        if (entityList == null || entityList.size() < 1){
            ZLogger.d(String.format("DataSync--没有绑定记录需要上传(%s)。", lastCursor));
            EventBus.getDefault().post(new StockTakeSyncEvent(StockTakeSyncEvent.EVENT_ID_SYNC_FINISHED));
            nextStep();
            return;
        }
        final ShelveEntity entity = entityList.get(0);

        //保存游标
        Date newCursor = entity.getCreatedDate();

        bSyncInProgress = true;

        final Date finalNewCursor = newCursor;

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();

                        //出库成功:1-556637
                        ZLogger.d("上传绑定记录成功:" + retStr);

                        //需要更新订单流水
                        SharedPreferencesHelper.setStocktakeLastUpdate(finalNewCursor);
                        entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_FINISHED);
                        ShelveService.get().saveOrUpdate(entity);

                        //继续提交
                        uploadShelvesBindRecords();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("DataSync--上传绑定记录失败: " + errMsg);
                        EventBus.getDefault().post(new StockTakeSyncEvent(StockTakeSyncEvent.EVENT_ID_SYNC_FAILED));
                        nextStep();
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };

        InvSkuStoreApiImpl.bindRackNo(entity.getBarcode(), String.valueOf(entity.getRackNo()), responseCallback);

    }


    public class StockTakeSyncEvent {
        public static final int EVENT_ID_SYNC_FINISHED = 0X01;//同步完成
        public static final int EVENT_ID_SYNC_FAILED = 0X02;//同步失败

        private int eventId;

        public StockTakeSyncEvent(int eventId) {
            this.eventId = eventId;
        }

        public int getEventId() {
            return eventId;
        }
    }

}
