package com.mfh.litecashier.service;


import android.os.AsyncTask;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.hardware.GreenTags.GreenTagsApiImpl2;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import org.century.GreenTagsApi;

import java.util.Date;
import java.util.List;

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
public class EslSyncManager2 extends EslSyncManager{

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

        if (!SharedPreferencesHelper
                .getBoolean(SharedPreferencesHelper.PK_B_SYNC_ESL_ENABLED, false)){
            uploadFinished("请在设置中打开同步商品库到电子价签同步开关。");
            return;
        }

        if (bSyncInProgress) {
            uploadProcess("正在同步价签商品...");
            return;
        }

        batchUploadGoodsInfo();
    }


    /**
     * 上传结束
     */
    private void uploadProcess(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = true;
  }

    /**
     * 上传结束
     */
    private void uploadFinished(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
    }

    /**
     * 上传结束失败
     */
    private void uploadFailed(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;

        //每次休眠一段时间后连接基站都会失败，这里重试一次，确保数据可以同步成功
        if (retrySyncFlag == 1){
            batchUploadGoodsInfo();
        }
    }

    /**
     * 批量推送商品数据<br>
     * 根据上一次同步游标同步订单数据
     */
    private void batchUploadGoodsInfo() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            uploadFailed("网络未连接，暂停同步价签商品。");
            return;
        }

        String lastCursor = getEslStartCursor();

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
        private PageInfo pageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, ESL_MAX_SYNC_PAGESIZE);//翻页


        public ESLPushGoodsExInfoPackAsyncTask(String startCursor) {
            this.startCursor = startCursor;
            this.sqlWhere = String.format("updatedDate >= '%s'", startCursor);
            this.newCursor = null;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                pageInfo = new PageInfo(1, ESL_MAX_SYNC_PAGESIZE);
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
                GreenTagsApiImpl2.ESLPushGoodsInfoExPackResult exPackResult =
                        makeEslPushRequest(goodsList, newCursor);
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
                        ZLogger.df(String.format("同步价签商品失败：%s",
                                TimeUtil.format(newCursor, TimeCursor.InnerFormat)));
                        return false;
                    }
                    newCursor = exPackResult2.getCursor();
                    ZLogger.df(String.format("同步价签商品成功：%s",
                            TimeUtil.format(newCursor, TimeCursor.InnerFormat)));
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.ef(String.format("ESLPushGoodsInfoExPack failed, %s", e.toString()));
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
        }
    }

}
