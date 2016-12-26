package com.mfh.litecashier.service;


import com.bingshanguxue.pda.DataSyncManager;
import com.bingshanguxue.pda.utils.SharedPrefesManagerUltimate;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.bean.PosGoods;
import com.manfenjiayuan.pda_supermarket.bean.ProductSkuBarcode;
import com.manfenjiayuan.pda_supermarket.database.dao.PosProductNetDao;
import com.manfenjiayuan.pda_supermarket.database.dao.PosProductSkuNetDao;
import com.manfenjiayuan.pda_supermarket.database.entity.PosProductEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.PosProductService;
import com.manfenjiayuan.pda_supermarket.database.logic.PosProductSkuService;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * POS--同步频率较高，商品档案发生变化就会触发一次同步
 * <ol>
 * <li>同步商品档案</li>
 * <li>同步商品档案——一品多玛</li>
 * <li>同步商品档案——前台类目</li>
 * <li>同步商品档案——前台类目&商品档案关联表</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DataDownloadManager extends DataSyncManager {
    public static final int NA = 0;
    public static final int HUMAN_ABILITY = 1;//用户能力信息
    public static final int POSPRODUCTS = 4;//商品档案
    public static final int POSPRODUCTS_SKU = 8;//一品多码

    public static final int LAUNCHER = 13;//应用启动
    public static final int MANUAL = 12;//手动点击同步

    private int queue = NA;//默认同步所有数据

    public static class DataDownloadEvent {
        public static final int EVENT_ID_SYNC_DATA_PROGRESS = 0X11;//同步进度
        public static final int EVENT_POSPRODUCTS_UPDATED = 0X03;//商品档案
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X12;//同步结束

        private int eventId;

        public DataDownloadEvent(int eventId) {
            this.eventId = eventId;
        }

        public int getEventId() {
            return eventId;
        }
    }

    private static final int MAX_SYNC_PRODUCTS_PAGESIZE = 70;

    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PRODUCTS_PAGESIZE);
    private PosProductNetDao posProductNetDao = new PosProductNetDao();
    private PageInfo mPosSkuPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private PosProductSkuNetDao posProductSkuNetDao = new PosProductSkuNetDao();

    private boolean bSyncInProgress = false;//是否正在同步
    private int rollback = -1;
    private static final int MAX_ROLLBACK = 5;

    private static DataDownloadManager instance = null;

    /**
     * 返回 DataDownloadManager 实例
     *
     * @return
     */
    public static DataDownloadManager get() {
        if (instance == null) {
            synchronized (DataDownloadManager.class) {
                if (instance == null) {
                    instance = new DataDownloadManager();
                }
            }
        }
        return instance;
    }

    /**
     * 同步数据
     */
    public void manualSync() {
        sync(MANUAL);
    }

    public void launcherSync() {
        sync(LAUNCHER);
    }

    /**
     * 同步商品档案
     * */
    public void syncProducts(){
        sync(DataDownloadManager.POSPRODUCTS | DataDownloadManager.POSPRODUCTS_SKU);
    }


    public void sync(int step) {
        queue |= step;
        if (bSyncInProgress) {
            rollback++;
            ZLogger.df(String.format("正在同步POS数据..., rollback=%d/%d", rollback, MAX_ROLLBACK));

            //自动恢复同步
            if (rollback > MAX_ROLLBACK) {
                bSyncInProgress = false;
            }
            return;
        }

        processQueue();
    }

    /**
     * 同步
     */
    private void processQueue() {
        ZLogger.d(String.format("queue ＝ %d", queue));

        rollback = -1;
        this.bSyncInProgress = true;

        if ((queue & HUMAN_ABILITY) == HUMAN_ABILITY) {
            queryPrivList();
        } else if ((queue & POSPRODUCTS) == POSPRODUCTS) {
            downLoadPosProductStep1();
        } else if ((queue & POSPRODUCTS_SKU) == POSPRODUCTS_SKU) {
            findShopOtherBarcodesStep1();
        } else {
            onNotifyCompleted("没有下载任务待执行");
        }
    }

    /**
     * 结果更新
     */
    private void onUpdate(int eventId, String message) {
        if (!StringUtils.isEmpty(message)) {
            ZLogger.df(message);
        }
        bSyncInProgress = false;
        EventBus.getDefault().post(new DataDownloadEvent(eventId));
    }

    /**
     * 完成
     */
    private void onNotifyCompleted(String message) {
        if (!StringUtils.isEmpty(message)) {
            ZLogger.df(message);
        }
        bSyncInProgress = false;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    /**
     * 同步商品库
     */
    private void downLoadPosProductStep1() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (!MfhLoginService.get().haveLogined()) {
                    onNotifyCompleted("会话已失效，暂停同步商品档案.");
                    return;
                }

                queue ^= POSPRODUCTS;
                EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));
                EmbMsgService.getInstance().setAllRead(IMBizType.TENANT_SKU_UPDATE);

                String startCursor = getPosLastUpdateCursor();
                if (StringUtils.isEmpty(startCursor)) {
                    ZLogger.df("商品档案时间戳为空，全量更新，假删除旧数据");
                    PosProductService.get().deactiveAll();
                }

                subscriber.onNext(startCursor);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef(e.toString());
                        onNotifyCompleted("同步商品档案失败.");
                    }

                    @Override
                    public void onNext(String startCursor) {
                        //从第一页开始请求，每页最多50条记录
                        mPageInfo = new PageInfo(-1, MAX_SYNC_PRODUCTS_PAGESIZE);
                        downLoadPosProductStep2(startCursor, mPageInfo);
                        mPageInfo.setPageNo(1);
                    }
                });
    }

    private void downLoadPosProductStep2(final String lastCursor, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步商品档案.");
            return;
        }

        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));//部门编号
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));//公司编号
        params.put("startCursor", lastCursor);//游标
//        params.put("page", Integer.toString(pageInfo.getPageNo()));
//        params.put("rows", Integer.toString(pageInfo.getPageSize()));
//        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());

        ZLogger.df(String.format("同步商品开始(%d/%d/%s)",
                pageInfo.getPageNo(), pageInfo.getTotalPage(), lastCursor));
        posProductNetDao.query(params, new NetProcessor.QueryRsProcessor<PosGoods>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<PosGoods> rs) {
                //此处在主线程中执行。
//                new ProductsQueryAsyncTask(pageInfo, lastCursor)
//                        .execute(rs);
                downLoadPosProductStep3(rs, pageInfo, lastCursor);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);

                processQueue();
            }
        }, "/scGoodsSku/downLoadPosProduct");
    }

    /**
     * 保存POS商品档案
     */
    private void downLoadPosProductStep3(final RspQueryResult<PosGoods> rs, final PageInfo pageInfo,
                                         final String startCursor) {
        if (rs == null) {
            processQueue();
            return;
        }

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    mPageInfo = pageInfo;
                    ZLogger.df(String.format("保存 %d/%d(%d/%d) 个商品（%s） 开始", rs.getReturnNum(),
                            rs.getTotalNum(), mPageInfo.getPageNo(),
                            mPageInfo.getTotalPage(), startCursor));

                    Date cussor = null;
                    //使用事务
                    for (EntityWrapper<PosGoods> wrapper : rs.getRowDatas()) {
                        //保存商品到数据库
                        PosGoods posGoods = wrapper.getBean();
                        if (posGoods == null || posGoods.getId() == null) {
                            ZLogger.d("保存POS商品库失败：商品参数无效。");
                            continue;
                        }

                        PosProductService.get().saveOrUpdate(posGoods);
                        cussor = posGoods.getUpdatedDate();
                    }

                    //更新游标
                    SharedPrefesManagerUltimate.setSyncProductsStartCursor(cussor);
                    ZLogger.df(String.format("保存 %d/%d(%d/%d) 个商品（%s） 结束", rs.getReturnNum(),
                            rs.getTotalNum(), mPageInfo.getPageNo(),
                            mPageInfo.getTotalPage(), SharedPrefesManagerUltimate.getSyncProductsStartcursor()));

                } catch (Throwable ex) {
                    ZLogger.ef(String.format("保存商品库失败: %s", ex.toString()));
                }

                subscriber.onNext(startCursor);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onNotifyCompleted( "保存商品档案失败.");
                    }

                    @Override
                    public void onNext(String startCursor) {
                        //从第一页开始请求，每页最多50条记录
                        if (pageInfo.hasNextPage()) {
                            pageInfo.moveToNext();
                            downLoadPosProductStep2(startCursor, pageInfo);
                        } else {
                            countNetSyncAbleSkuNum();
                        }
                    }

                });
    }

    /**
     * 查询指定网点可同步sku总数
     * 商品库增量同步后检查pos本地商品数目和后台商品数目是否一致，如果不一致，则自动触发一次全量同步。
     */
    private void countNetSyncAbleSkuNum() {
        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停查询指定网点可同步sku总数");
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"701"}}
                        try {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            int skuNum = Integer.valueOf(retValue.getValue());
                            ZLogger.df(String.format("网点可同步sku总数:%d", skuNum));

                            //删除无效的数据
                            PosProductService.get().deleteBy(String.format("isCloudActive = '%d'",
                                    0));
                            // 比较本地商品数据库总数是否和可以同步的SKU总数一致，
                            // 如果不一致，则重置时间戳，下次触发全量同步，否则继续按照时间戳同步。
                            List<PosProductEntity> entityList = PosProductService.get()
                                    .queryAllByDesc(String.format("tenantId = '%d'",
                                            MfhLoginService.get().getSpid()));
                            int posNum = (entityList != null ? entityList.size() : 0);
                            ZLogger.df(String.format("本地商品档案库sku总数:%d", posNum));


                            if (posNum != skuNum) {
                                ZLogger.df(String.format("本地商品档案和云端数据不一致，重置时间戳，下一次需要全量同步商品库", posNum, skuNum));

                                //初始化游标并设置下次需要全量更新
                                SharedPrefesManagerUltimate.setSyncProductsStartcursor("");
                            }
                        } catch (Exception e) {
                            ZLogger.ef(String.format("查询指定网点可同步sku总数:%s", e.toString()));
                        }

                        processQueue();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询指定网点可同步sku总数失败：" + errMsg);
//                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN,
//                                null, "Validate--会话过期，自动重登录");

                        processQueue();
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };

        ScGoodsSkuApiImpl.countNetSyncAbleSkuNum(responseCallback);
    }

    /**
     * 同步规格商品码表
     */
    private void findShopOtherBarcodesStep1() {
        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停同步规格商品码表.");
            return;
        }

        queue ^= POSPRODUCTS_SKU;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));

        String lastCursor = SharedPrefesManagerUltimate.getSyncProductSkuCursor();

        mPosSkuPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        findShopOtherBarcodesStep2(lastCursor, mPosSkuPageInfo);
        mPosSkuPageInfo.setPageNo(1);
    }

    private void findShopOtherBarcodesStep2(final String lastCursor, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步一品多码关系表");
            return;
        }

        AjaxParams params = new AjaxParams();
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        params.put("startCursor", String.valueOf(lastCursor));//游标
//        params.put("page", Integer.toString(pageInfo.getPageNo()));
//        params.put("rows", Integer.toString(pageInfo.getPageSize()));
//        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());

        ZLogger.df(String.format("同步规格码表开始(%d/%d)", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        posProductSkuNetDao.query(params, new NetProcessor.QueryRsProcessor<ProductSkuBarcode>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ProductSkuBarcode> rs) {
                new ProductSkuQueryAsyncTask(pageInfo, lastCursor)
                        .execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.e(errMsg);
                //同步账号数据
                processQueue();
            }
        }, "/scProductSkuBarcodes/findShopOtherBarcodes");
    }

    private class ProductSkuQueryAsyncTask extends AsyncTask<RspQueryResult<ProductSkuBarcode>, Integer, Long> {
        private PageInfo pageInfo;
        private String lastCursor;


        public ProductSkuQueryAsyncTask(PageInfo pageInfo, String lastCursor) {
            this.pageInfo = pageInfo;
            this.lastCursor = lastCursor;
        }

        @Override
        protected Long doInBackground(RspQueryResult<ProductSkuBarcode>... params) {
//            saveQueryResult(params[0], pageInfo);
            RspQueryResult<ProductSkuBarcode> rs = params[0];
            try {
                mPosSkuPageInfo = pageInfo;

                if (rs != null) {
                    int retSize = rs.getReturnNum();
                    Date cursor = null;
                    for (EntityWrapper<ProductSkuBarcode> wrapper : rs.getRowDatas()) {
                        ProductSkuBarcode bean = wrapper.getBean();
                        if (bean != null) {
                            PosProductSkuService.get().saveOrUpdate(bean);
                            cursor = bean.getCreatedDate();
                        }
                    }

                    //更新游标
                    if (cursor != null) {
                        SharedPrefesManagerUltimate.setSyncProductSkuStartcursor(cursor);
                    }
                    ZLogger.df(String.format("同步 %d/%d 个箱规", retSize, rs.getTotalNum()));
                }
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.ef(String.format("同步码表失败: %s", ex.toString()));
            }

            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            //若还有继续发起请求
            if (pageInfo.hasNextPage()) {
                pageInfo.moveToNext();
                findShopOtherBarcodesStep2(lastCursor, pageInfo);
            } else {
                ZLogger.df("同步规格码表品结束:" + SharedPrefesManagerUltimate.getSyncProductSkuCursor());

                processQueue();
            }
        }
    }


    /**
     * 获取能力信息
     */
    private void queryPrivList() {
        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停同步账号数据.");
            return;
        }

        queue ^= HUMAN_ABILITY;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));

        UserApiImpl.queryPrivList(getPartnerRC);
    }

    private NetCallBack.NetTaskCallBack getPartnerRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    try {
                        String moduleNames = null;
                        if (rspData != null){
                            RspBean<String> retValue = (RspBean<String>) rspData;
                            moduleNames = retValue.getValue();
                        }
                        MfhLoginService.get().setModuleNames(moduleNames);
                    } catch (Exception ex) {
                        ZLogger.e("parseUserProfile, " + ex.toString());
                    } finally {
                    }
                    processQueue();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.e(errMsg);
                    processQueue();
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

}
