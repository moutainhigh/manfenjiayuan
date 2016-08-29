package com.mfh.litecashier.service;


import android.os.AsyncTask;
import android.os.Bundle;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.CateApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.database.entity.PosProductEntity;
import com.mfh.litecashier.database.logic.PosProductService;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import org.century.GreenTagsApi;
import org.century.GreenTagsApiImpl;
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
public class EslSyncManager {
    /**
     * 同步状态
     */
    public static final Integer SYNC_STATUS_NA = 0;//未同步
    public static final Integer SYNC_STATUS_UPDATE = 1;//有更新
    public static final Integer SYNC_STATUS_LATEST = 2;//最新的

    public static final int SYNC_MODE_FULLSCALE = 0;//全量更新
    public static final int SYNC_MODE_INCREMENTAL = 1;//增量更新

    //每次同步最大数量
    private static final int MAX_SYNC_PAGESIZE = 50;

    private boolean bSyncInProgress = false;//是否正在同步

    private static EslSyncManager instance = null;


    /**
     * 返回 EslSyncManager 实例
     *
     * @return
     */
    public static EslSyncManager getInstance() {
        if (instance == null) {
            synchronized (EslSyncManager.class) {
                if (instance == null) {
                    instance = new EslSyncManager();
                }
            }
        }
        return instance;
    }

    public EslSyncManager() {
    }


    /**
     * 上传POS订单
     */
    public synchronized void sync() {
        if (bSyncInProgress) {
            uploadProcess("EslSyncManager--正在同步价签商品...");
            return;
        }

        if (GreenTagsApi.FULLSCALE_ENABLED) {
            ZLogger.d("EslSyncManager--全量同步");
            SharedPreferencesHelper.set(GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, "");
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
    }

    /**
     * 批量推送商品数据<br>
     * 根据上一次同步游标同步订单数据
     */
    private void batchUploadGoodsInfo() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            uploadFailed("EslSyncManager--网络未连接，暂停同步价签商品。");
            return;
        }

        String lastCursor = SharedPreferencesHelper.getText(GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR);
        ZLogger.d(String.format("EslSyncManager--最后一次同步商品的更新时间(%s)。", lastCursor));

        ESLPushGoodsExInfoPackAsyncTask asyncTask = new ESLPushGoodsExInfoPackAsyncTask(lastCursor);
        asyncTask.execute();
    }

    private class ESLPushGoodsExInfoPackAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String lastCursor;
        private Date newCursor = null;

        public ESLPushGoodsExInfoPackAsyncTask(String lastCursor) {
            this.lastCursor = lastCursor;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                //上传未同步并且已完成的订单
//        SELECT * FROM tb_pos_order_v2 WHERE updatedDate > '2016-02-17 14:38:43' and sellerId = '134221' and status = '4' and paystatus = '1' ORDER BY updatedDate asc limit 0,10
                String strWhere = String.format("cateType = '%d' and updatedDate > '%s'",
                        CateApi.BACKEND_CATE_BTYPE_FRESH, lastCursor);
                List<PosProductEntity> goodsList = PosProductService.get()
                        .queryAllAsc(strWhere, new PageInfo(1, MAX_SYNC_PAGESIZE));
                if (goodsList == null || goodsList.size() < 1) {
                    uploadProcess(String.format("EslSyncManager--没有商品需要推送(%s)", lastCursor));
                    return false;
                }

                ArrayOfGoodsInfoEX arrayOfGoodsInfoEX = new ArrayOfGoodsInfoEX();
                for (PosProductEntity goods : goodsList) {
                    //保存最大时间游标
                    if (newCursor == null || goods.getUpdatedDate() == null
                            || newCursor.compareTo(goods.getUpdatedDate()) <= 0) {
                        newCursor = goods.getUpdatedDate();
                    }

                    arrayOfGoodsInfoEX.add(createFromPosProductEntity(goods));
                }

                GreenTagsApiImpl.ESLPushGoodsInfoExPack(arrayOfGoodsInfoEX);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsInfoExPack failed, %s", e.toString()));
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsInfoExPack failed, %s", e.toString()));
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                uploadFailed("EslSyncManager--等待继续推送商品");
                // 保存批量上传订单时间
                SharedPreferencesHelper.set(GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR,
                        TimeUtil.format(newCursor, TimeCursor.InnerFormat));
                //继续上传订单
                batchUploadGoodsInfo();
            } else {
                uploadFailed("EslSyncManager--批量推送商品结束");
            }
        }


        @Override
        protected void onPreExecute() {
            uploadProcess(String.format("EslSyncManager--准备推送商品到价签(%s)", lastCursor));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }


    public static GoodsInfoEX createFromPosProductEntity(PosProductEntity goods) {
        GoodsInfoEX googsInfoEX = new GoodsInfoEX();
        googsInfoEX.setGoodsCode(goods.getBarcode());

        //更新商品属性
        ArrayOfProperty propertyList = new ArrayOfProperty();
        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_GOODSCODE, goods.getBarcode()));
        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_NAME, goods.getName()));
        propertyList.add(new Property(GoodsInfoEX.TABLE_COLOUMNINDEX_PRICE, MUtils.formatDouble(goods.getCostPrice(), "0")));
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
