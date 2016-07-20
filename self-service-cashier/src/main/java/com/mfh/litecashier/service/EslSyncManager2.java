package com.mfh.litecashier.service;


import android.os.AsyncTask;
import android.os.Bundle;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.hardware.GreenTags.GreenTagsApiImpl2;

import org.century.GreenTagsApi;
import org.century.schemas.ArrayOfGoodsInfoEX;
import org.century.schemas.ArrayOfProperty;
import org.century.schemas.GoodsInfoEX;
import org.century.schemas.Property;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 绿泰价签数据同步管理
 * <ol>
 * 适用场景
 * <li>同步商品数据到绿泰价签管理后台</li>
 * </ol>
 * <p/>
 * <ol>
 * 同步策略
 * <li>全量更新，每次都会同步所有数据。</li>
 * <li>增量更新，每天首次开机全量同步一次（保证数据一致性）,之后仅同步有更新的数据。</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class EslSyncManager2 {
    //每次同步最大数量
    private static final int MAX_SYNC_PAGESIZE = 20;

    private boolean bSyncInProgress = false;//是否正在同步
    //是否连接基站失败，if true,失败后需要重试一次
    private static int retrySyncFlag = 0;

    private static EslSyncManager2 instance = null;


    /**
     * 返回 EslSyncManager 实例
     *
     * @return
     */
    public static EslSyncManager2 getInstance() {
        if (instance == null) {
            synchronized (EslSyncManager2.class) {
                if (instance == null) {
                    instance = new EslSyncManager2();
                }
            }
        }
        return instance;
    }

    public EslSyncManager2() {
    }


    /**
     * 上传POS订单
     */
    public synchronized void sync() {
        retrySyncFlag = 0;
        if (bSyncInProgress) {
            uploadProcess("正在同步价签商品...");
            return;
        }

        if (GreenTagsApi.FULLSCALE_ENABLED) {
            ZLogger.d("全量同步");
            SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                    GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, "");
        }
        batchUploadGoodsInfo();
    }


    /**
     * 上传结束
     */
    private void uploadProcess(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = true;
        EventBus.getDefault().post(new EslSyncManagerEvent(EslSyncManagerEvent.EVENT_ID_SYNC_DATA_PROCESS));
    }

    /**
     * 上传结束
     */
    private void uploadFinished(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new EslSyncManagerEvent(EslSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    /**
     * 上传结束失败
     */
    private void uploadFailed(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new EslSyncManagerEvent(EslSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED));

        //每次休眠一段时间后连接基站都会失败，这里重试一次，确保数据可以同步成功
        if (retrySyncFlag == 1){
            if (bSyncInProgress) {
                uploadProcess("正在同步价签商品...");
                return;
            }

            if (GreenTagsApi.FULLSCALE_ENABLED) {
                ZLogger.d("全量同步");
                SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                        GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, "");
            }
            batchUploadGoodsInfo();
        }
    }

    /**
     * 批量推送商品数据<br>
     * 根据上一次同步游标同步订单数据
     */
    private void batchUploadGoodsInfo() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            uploadFailed("网络未连接，暂停同步价签商品。");
            return;
        }

        String lastCursor = SharedPreferencesManager.getText(GreenTagsApi.PREF_GREENTAGS,
                GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR);
        ZLogger.df(String.format("最后一次同步商品的更新时间(%s)。", lastCursor));

        ESLPushGoodsExInfoPackAsyncTask asyncTask = new ESLPushGoodsExInfoPackAsyncTask(lastCursor);
        asyncTask.execute();
    }

    /**
     * 推送商品
     */
    private class ESLPushGoodsExInfoPackAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String startCursor;//时间游标
        private Date newCursor = null;//
        private String sqlWhere;//查询条件
        private PageInfo pageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_PAGESIZE);//翻页


        public ESLPushGoodsExInfoPackAsyncTask(String startCursor) {
            this.startCursor = startCursor;
            this.sqlWhere = String.format("updatedDate >= '%s'", startCursor);
            this.newCursor = null;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                pageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
                //上传未同步并且已完成的订单
//        SELECT * FROM tb_pos_order_v2 WHERE updatedDate > '2016-02-17 14:38:43' and sellerId = '134221' and status = '4' and paystatus = '1' ORDER BY updatedDate asc limit 0,10
//                String strWhere = String.format("cateType = '%d' and updatedDate > '%s'",
//                        CateApi.BACKEND_CATE_BTYPE_FRESH, lastCursor);
                List<PosProductEntity> goodsList = PosProductService.get()
                        .queryAllAsc(sqlWhere, pageInfo);
                if (goodsList == null || goodsList.size() < 1) {
                    uploadFinished(String.format("没有商品需要推送(%s)", startCursor));
                    return false;
                }
                uploadProcess(String.format("查询到 %d 个商品需要同步，" +
                                "当前页数 %d/%d,每页最多 %d 个商品(%s)",
                        pageInfo.getTotalCount(), pageInfo.getPageNo(), pageInfo.getTotalPage(),
                        pageInfo.getPageSize(), startCursor));
                //上传第一页数据
                GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult exPackResult = makeEslPushRequest(goodsList, newCursor);
                if (!exPackResult.isResult()) {
                    retrySyncFlag++;
                    ZLogger.df(String.format("同步价签商品失败：%s",
                            TimeUtil.format(newCursor, TimeCursor.InnerFormat)));
                    return false;
                }
                newCursor = exPackResult.getCursor();

                //判断是否还有数据需要同步
                while (pageInfo.hasNextPage()) {
                    pageInfo.moveToNext();

                    List<PosProductEntity> goodsList2 = PosProductService.get()
                            .queryAllAsc(sqlWhere, pageInfo);
                    if (goodsList2 == null || goodsList2.size() < 1) {
                        uploadFinished(String.format("没有商品需要推送(%s)", startCursor));
                        return false;
                    }
                    uploadProcess(String.format("查询到 %d 个商品需要同步，" +
                                    "当前页数 %d/%d,每页最多 %d 个商品(%s)",
                            pageInfo.getTotalCount(), pageInfo.getPageNo(),
                            pageInfo.getTotalPage(), pageInfo.getPageSize(), startCursor));
                    GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult exPackResult2 =
                            makeEslPushRequest(goodsList2, newCursor);
                    if (!exPackResult2.isResult()) {
                        retrySyncFlag++;
                        return false;
                    }
                    newCursor = exPackResult2.getCursor();
                    ZLogger.df(String.format("同步价签商品成功：%s", TimeUtil.format(newCursor, TimeCursor.InnerFormat)));
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsInfoExPack failed, %s", e.toString()));
                retrySyncFlag++;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                uploadFinished("批量推送商品成功");

                // 保存批量上传订单时间
                String cursor = TimeUtil.format(newCursor, TimeCursor.InnerFormat);
                SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                        GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, cursor);
                if (GreenTagsApi.FULLSCALE_ENABLED){
                    SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                            GreenTagsApi.PK_B_GREENTAGS_FULLSCALE, false);
                    GreenTagsApi.FULLSCALE_ENABLED = false;
                }

                ZLogger.df(String.format("ESLPushGoodsInfoExPack 保存价签同步时间：%s", cursor));
                //继续上传订单
//                batchUploadGoodsInfo();
            } else {
                uploadFailed("批量推送商品失败");
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    private GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult makeEslPushRequest(List<PosProductEntity> goodsList, Date newCursor) {
        if (goodsList == null || goodsList.size() < 1) {
            return new GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult(false);
        }

        ArrayOfGoodsInfoEX arrayOfGoodsInfoEX = new ArrayOfGoodsInfoEX();
        for (PosProductEntity goods : goodsList) {
            //记录最大时间游标
            if (newCursor == null || goods.getUpdatedDate() == null
                    || newCursor.compareTo(goods.getUpdatedDate()) <= 0) {
                newCursor = goods.getUpdatedDate();
            }

            arrayOfGoodsInfoEX.add(createFromPosProductEntity(goods));
        }
        try {
            return GreenTagsApiImpl2.ESLPushGoodsInfoExPack2(arrayOfGoodsInfoEX, newCursor);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return new GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult(false);
        } catch (IOException e) {
            e.printStackTrace();
            return new GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult(false);
        }
    }

    public static GoodsInfoEX createFromPosProductEntity(PosProductEntity goods) {
        GoodsInfoEX googsInfoEX = new GoodsInfoEX();
        googsInfoEX.setGoodsCode(goods.getBarcode());

        //更新商品属性
        ArrayOfProperty propertyList = new ArrayOfProperty();
        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_GOODSCODE, goods.getBarcode()));
        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_NAME, goods.getName()));
//        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_ORIGIN, "米西厨房"));

        //2016-07-16 计重商品单位按“斤”显示，价格／2
        if (PriceType.WEIGHT.equals(goods.getPriceType())){
            propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_PRICE,
                    MUtils.formatDouble(goods.getCostPrice()/2, "0")));
            propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_UNIT, "斤"));
        }
        else{
            propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_PRICE,
                    MUtils.formatDouble(goods.getCostPrice(), "0")));
            propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_UNIT, goods.getUnit()));
        }

        googsInfoEX.setProperties(propertyList);

        return googsInfoEX;
    }

    public class EslSyncManagerEvent {
        public static final int EVENT_ID_SYNC_DATA_START = 0X03;//同步数据开始
        public static final int EVENT_ID_SYNC_DATA_PROCESS = 0X04;//同步数据处理中
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X05;//同步数据结束
        public static final int EVENT_ID_SYNC_DATA_FAILED = 0X06;//同步数据失败

        private int eventId;
        private Bundle args;//参数

        public EslSyncManagerEvent(int eventId) {
            this.eventId = eventId;
        }

        public EslSyncManagerEvent(int eventId, Bundle args) {
            this.eventId = eventId;
            this.args = args;
        }

        public int getEventId() {
            return eventId;
        }

        public Bundle getArgs() {
            return args;
        }
    }

}
