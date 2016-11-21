package com.mfh.litecashier.service;


import com.bingshanguxue.cashier.database.dao.PosProductNetDao;
import com.bingshanguxue.cashier.database.dao.PosProductSkuNetDao;
import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosLocalCategoryService;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.database.service.PosProductSkuService;
import com.bingshanguxue.cashier.database.service.ProductCatalogService;
import com.bingshanguxue.cashier.model.PosGoods;
import com.bingshanguxue.cashier.model.ProductCatalog;
import com.bingshanguxue.cashier.model.ProductSkuBarcode;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.anon.sc.ProductCatalogApi;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.category.ScCategoryInfoApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * POS--同步频率较高，商品档案发生变化就会触发一次同步
 * <ol>
 *     <li>同步商品档案</li>
 *     <li>同步商品档案——一品多玛</li>
 *     <li>同步商品档案——前台类目</li>
 *     <li>同步商品档案——前台类目&商品档案关联表</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class GoodsSyncManager extends BaseSyncManager {
    public static final int NA = 0;
    public static final int POSPRODUCTS = 1;//商品档案
    public static final int POSPRODUCTS_SKU = 2;//一品多码
    public static final int FRONTENDCATEGORY = 4;//前台类目(一级类目)
    public static final int FRONTENDCATEGORY_GOODS = 8;//前台类目&商品库－关系表
    public static final int LAUNCHER = 15;//应用启动
    private int queue = NA;//默认同步所有数据

    public static class GoodsSyncEvent {
        public static final int EVENT_ID_SYNC_DATA_PROGRESS = 0X11;//同步进度
        public static final int EVENT_FRONTEND_CATEGORY_UPDATED = 0X04;//前台类目更新
        public static final int EVENT_PRODUCT_CATALOG_UPDATED = 0X05;//前台类目和商品库关系更新
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X12;//同步结束

        private int eventId;

        public GoodsSyncEvent(int eventId) {
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
    private PageInfo productCateslogPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private boolean bSyncInProgress = false;//是否正在同步
    private int rollback = -1;
    private static final int MAX_ROLLBACK = 10;

    private static GoodsSyncManager instance = null;

    /**
     * 返回 GoodsSyncManager 实例
     *
     * @return
     */
    public static GoodsSyncManager get() {
        if (instance == null) {
            synchronized (GoodsSyncManager.class) {
                if (instance == null) {
                    instance = new GoodsSyncManager();
                }
            }
        }
        return instance;
    }

    /**
     * 同步数据
     */
    public synchronized void sync() {
        sync(LAUNCHER);
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
     * */
    private void processQueue() {
        ZLogger.d(String.format("queue ＝ %d", queue));

        rollback = -1;
        this.bSyncInProgress = true;

        if ((queue & POSPRODUCTS) == POSPRODUCTS){
            queue ^= POSPRODUCTS;
            startSyncProducts();
        }
        else if ((queue & POSPRODUCTS_SKU) == POSPRODUCTS_SKU){
            queue ^= POSPRODUCTS_SKU;
            startSyncProductSku();
        }
        else if ((queue & FRONTENDCATEGORY) == FRONTENDCATEGORY){
            queue ^= FRONTENDCATEGORY;
            getTopFrontId();
        }
        else if ((queue & FRONTENDCATEGORY_GOODS) == FRONTENDCATEGORY_GOODS){
            queue ^= FRONTENDCATEGORY_GOODS;
            downLoadProductCatalog();
        }
        else{
            ZLogger.df("同步数据结束");
            bSyncInProgress = false;
            EventBus.getDefault().post(new GoodsSyncEvent(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED));
        }
    }

    /**
     * 结果更新
     * */
    private void onUpdate(int eventId, String message){
        if (StringUtils.isEmpty(message)){
            ZLogger.df(message);
        }
        bSyncInProgress = false;
        EventBus.getDefault().post(new GoodsSyncEvent(eventId));
    }

    /**
     * 同步商品库
     */
    private void startSyncProducts() {
        ZLogger.d("准备同步POS商品档案...");
        EventBus.getDefault().post(new GoodsSyncEvent(GoodsSyncEvent.EVENT_ID_SYNC_DATA_PROGRESS));

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (!MfhLoginService.get().haveLogined()) {
                    onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "会话已失效，暂停同步POS数据.");
                    return;
                }

                String startCursor = getPosLastUpdateCursor();
                if (StringUtils.isEmpty(startCursor)) {
                    ZLogger.df("同步商品库：全量更新，重置游标，删除旧数据");
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
                        onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "会话已失效，暂停同步POS数据.");
                    }

                    @Override
                    public void onNext(String startCursor) {
                        //从第一页开始请求，每页最多50条记录
                        mPageInfo = new PageInfo(-1, MAX_SYNC_PRODUCTS_PAGESIZE);
                        downloadProducts(startCursor, mPageInfo);
                        mPageInfo.setPageNo(1);
                    }
                });
    }

    private void downloadProducts(final String lastCursor, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "网络未连接，暂停同步POS数据.");
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
                savePosProducts(rs, pageInfo, lastCursor);
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
    private void savePosProducts(final RspQueryResult<PosGoods> rs, final PageInfo pageInfo,
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
                    SharedPreferencesUltimate.setSyncProductsCursor(cussor);
                    ZLogger.df(String.format("保存 %d/%d(%d/%d) 个商品（%s） 结束", rs.getReturnNum(),
                            rs.getTotalNum(), mPageInfo.getPageNo(),
                            mPageInfo.getTotalPage(), SharedPreferencesUltimate.getSyncProductsCursor()));

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
                        onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "会话已失效，暂停同步POS数据.");
                    }

                    @Override
                    public void onNext(String startCursor) {
                        //从第一页开始请求，每页最多50条记录
                        if (pageInfo.hasNextPage()) {
                            pageInfo.moveToNext();
                            downloadProducts(startCursor, pageInfo);
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
            onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "会话已失效，暂停同步POS数据.");
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
                            ZLogger.df(String.format("指定网点可同步sku总数:%d", skuNum));

                            // 比较本地商品数据库总数是否和可以同步的SKU总数一致，
                            // 如果不一致，则重置时间戳，下次触发全量同步，否则继续按照时间戳同步。
                            List<PosProductEntity> entityList = PosProductService.get()
                                    .queryAllByDesc(String.format("tenantId = '%d'",
                                            MfhLoginService.get().getSpid()));
                            int posNum = (entityList != null ? entityList.size() : 0);
                            if (posNum != skuNum) {
                                ZLogger.df(String.format("pos本地商品数目(%d)和后台商品数目(%d)不一致," +
                                        "下一次需要全量同步商品库", posNum, skuNum));

                                //初始化游标并设置下次需要全量更新
                                SharedPreferencesUltimate.setSyncProductsCursor("");

                                //删除无效的数据
                                PosProductService.get().deleteBy(String.format("isCloudActive = '%d'",
                                        0));
                                List<PosProductEntity> entityList1 = PosProductService.get()
                                        .queryAllByDesc(String.format("tenantId = '%d'",
                                                MfhLoginService.get().getSpid()));
                                ZLogger.d(String.format("删除无效的数据，本地类目数量:%d",
                                        (entityList1 != null ? entityList1.size() : 0)));
                            } else {
                                ZLogger.df(String.format("pos本地商品数目(%d)和后台商品数目(%d)一致",
                                        posNum, skuNum));
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
                , CashierApp.getAppContext()) {
        };

        ScGoodsSkuApiImpl.countNetSyncAbleSkuNum(responseCallback);
    }

    /**
     * 同步规格商品码表
     */
    private void startSyncProductSku() {
        if (!MfhLoginService.get().haveLogined()) {
            onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "会话已失效，暂停同步POS数据.");
            return;
        }

        EmbMsgService.getInstance().setAllRead(IMBizType.TENANT_SKU_UPDATE);

        mPosSkuPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        String lastCursor = SharedPreferencesUltimate.getSyncProductSkuCursor();

        //从第一页开始请求，每页最多50条记录
        downloadProductSku(lastCursor, mPosSkuPageInfo);
        mPosSkuPageInfo.setPageNo(1);
    }

    private void downloadProductSku(final String lastCursor, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "网络未连接，暂停同步POS数据.");
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

                if (rs == null) {
                    return -1L;
                }

                //保存下来
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
                    SharedPreferencesUltimate.setPosSkuLastUpdate(cursor);
                }
                ZLogger.df(String.format("同步 %d/%d 个箱规", retSize, rs.getTotalNum()));

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
                downloadProductSku(lastCursor, pageInfo);
            } else {
                ZLogger.df("同步规格码表品结束:" + SharedPreferencesUltimate.getSyncProductSkuCursor());

                processQueue();
            }
        }
    }

    private void getTopFrontId() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "网络未连接，暂停同步POS数据.");
            return;
        }

        NetCallBack.NetTaskCallBack queryRsCallBack = new NetCallBack.NetTaskCallBack<CategoryInfo,
                NetProcessor.Processor<CategoryInfo>>(
                new NetProcessor.Processor<CategoryInfo>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        CategoryInfo categoryInfo = null;
                        if (rspData != null) {
                            RspBean<CategoryInfo> retValue = (RspBean<CategoryInfo>) rspData;
                            categoryInfo = retValue.getValue();
                        }

                        getCodeValue(categoryInfo);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("加载前台根类目 失败, " + errMsg);
                        processQueue();
                    }
                }
                , CategoryInfo.class
                , MfhApplication.getAppContext()) {
        };

        ScCategoryInfoApi.getTopFrontId(CateApi.POS, queryRsCallBack);
    }

    /**
     * 创建前台类目
     */
    private void createCategoryInfo() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "网络未连接，暂停同步POS数据.");
            return;
        }

        NetCallBack.NetTaskCallBack createRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("创建前台类目失败, " + errMsg);
                        processQueue();
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        //新建类目成功，保存类目信息，并触发同步。
                        try {
                            if (rspData != null) {
                                RspValue<String> retValue = (RspValue<String>) rspData;
                                String result = retValue.getValue();
                                Long code = Long.valueOf(result);
                                ZLogger.df("新建前台类目成功:" + code);
                                SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_L_CATETYPE_POS_ID, code);

                                CategoryInfo categoryInfo = new CategoryInfo();
                                categoryInfo.setId(code);
                                categoryInfo.setNameCn("POS前台类目");
                                getCodeValue(categoryInfo);
                            }

                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
                        processQueue();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        ScCategoryInfoApi.create(CateApi.DOMAIN_TYPE_PROD,
                CateApi.CATE_POSITION_FRONT, MfhLoginService.get().getSpid(),
                "POS前台类目", CateApi.POS, createRC);
    }

    private void getCodeValue(CategoryInfo categoryInfo) {
        if (categoryInfo == null) {
            createCategoryInfo();
            return;
        }
        SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_L_CATETYPE_POS_ID, categoryInfo.getId());
        PosLocalCategoryService.get().deactiveAll();

        NetCallBack.NetTaskCallBack queryRsCallBack = new NetCallBack.NetTaskCallBack<CategoryInfo,
                NetProcessor.Processor<CategoryInfo>>(
                new NetProcessor.Processor<CategoryInfo>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        List<CategoryInfo> items = new ArrayList<>();
                        if (rspData != null) {
                            RspListBean<CategoryInfo> retValue = (RspListBean<CategoryInfo>) rspData;
                            items = retValue.getValue();
                        }

                        for (CategoryInfo item : items) {
                            PosLocalCategoryService.get().saveOrUpdate(item);
                        }

                        int count = PosLocalCategoryService.get().getCount();
                        int cloudNum = items.size();
                        if (count == cloudNum) {
                            ZLogger.df(String.format("同步前台类目结束,云端类目(%d)和本地类目数量(%d)一致。",
                                    cloudNum, count));
                        } else {
                            ZLogger.df(String.format("同步前台类目结束,云端类目(%d)和本地类目数量(%d)不一致",
                                    cloudNum, count));

                            //删除无效的数据
                            PosLocalCategoryService.get().deleteBy(String.format("isCloudActive = '%d'",
                                    PosLocalCategoryEntity.CLOUD_DEACTIVE));
                            ZLogger.d(String.format("删除无效的数据，本地类目数量:%d",
                                    PosLocalCategoryService.get().getCount()));
                            // TODO: 21/11/2016 删除类目对应的商品
                        }

                        //通知刷新前台类目数据
                        EventBus.getDefault().post(new GoodsSyncEvent(GoodsSyncEvent.EVENT_FRONTEND_CATEGORY_UPDATED));

                        processQueue();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("加载前台根目录子类目 失败, " + errMsg);
                        processQueue();
                    }
                }
                , CategoryInfo.class
                , MfhApplication.getAppContext()) {
        };

        ScCategoryInfoApi.getCodeValue(categoryInfo.getId(), queryRsCallBack);
    }


    /**
     * 同步商品和类目关系表
     */
    private void downLoadProductCatalog() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (!MfhLoginService.get().haveLogined()) {
                    onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "会话已失效，暂停同步POS数据.");
                    return;
                }

                String startCursor = getProductCatalogStartCursor();
                if (StringUtils.isEmpty(startCursor)) {
                    // 全量更新，锁定商品
                    ProductCatalogService.getInstance().deactiveAll();
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
                        onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "会话已失效，暂停同步POS数据.");
                    }

                    @Override
                    public void onNext(String startCursor) {
                        //从第一页开始请求，每页最多50条记录
                        productCateslogPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
                        //从第一页开始请求，每页最多50条记录
                        downLoadProductCatalog2(startCursor, productCateslogPageInfo);
                        productCateslogPageInfo.setPageNo(1);
                    }

                });
    }

    private void downLoadProductCatalog2(final String startCusror, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "网络未连接，暂停同步POS数据.");
            return;
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ProductCatalog>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ProductCatalog> rs) {
                        new saveProductCatalogAsync(pageInfo, startCusror).execute(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("同步商品和类目关系表失败:" + errMsg);
                        processQueue();
                    }
                }, ProductCatalog.class, CashierApp.getAppContext());


        ZLogger.df(String.format("同步类目商品关系表开始(%d/%d)%s",
                pageInfo.getPageNo(), pageInfo.getTotalPage(), startCusror));

        ProductCatalogApi.downLoadProductCatalog(startCusror, pageInfo, queryRsCallBack);
    }

    private class saveProductCatalogAsync extends AsyncTask<RspQueryResult<ProductCatalog>, Integer, Long> {
        private PageInfo pageInfo;
        private String lastCursor;


        public saveProductCatalogAsync(PageInfo pageInfo, String lastCursor) {
            this.pageInfo = pageInfo;
            this.lastCursor = lastCursor;
        }

        @Override
        protected Long doInBackground(RspQueryResult<ProductCatalog>... params) {
            try {
                RspQueryResult<ProductCatalog> rs = params[0];
                productCateslogPageInfo = pageInfo;

                if (rs == null) {
                    return -1L;
                }

                //保存下来
                int retSize = rs.getReturnNum();
                Date cursor = null;
                for (EntityWrapper<ProductCatalog> wrapper : rs.getRowDatas()) {
                    ProductCatalog bean = wrapper.getBean();
                    if (bean != null) {
                        ProductCatalogService.getInstance().saveOrUpdate(bean);
                        cursor = bean.getCreatedDate();
                    }
                }

                //更新游标
                if (cursor != null) {
                    SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_SYNC_PRODUCTCATALOG_STARTCURSOR,
                            TimeCursor.InnerFormat.format(cursor));
                }
                ZLogger.df(String.format("保存 %d/%d 个商品类目关系表（%s）",
                        retSize, rs.getTotalNum(),
                        SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_SYNC_PRODUCTCATALOG_STARTCURSOR)));
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.ef(String.format("保存商品类目关系表失败: %s", ex.toString()));
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
                downLoadProductCatalog2(lastCursor, pageInfo);
            } else {
                countProductCatalogSyncAbleNum();
            }
        }
    }

    /**
     * 查询指定网点可同步sku总数
     * 商品库增量同步后检查pos本地商品数目和后台商品数目是否一致，如果不一致，则自动触发一次全量同步。
     */
    private void countProductCatalogSyncAbleNum() {
        if (!MfhLoginService.get().haveLogined()) {
            onUpdate(GoodsSyncEvent.EVENT_ID_SYNC_DATA_FINISHED, "会话已失效，暂停同步POS数据.");
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"701"}}
                        try {
                            int skuNum = 0;
                            if (rspData != null){
                                RspValue<String> retValue = (RspValue<String>) rspData;
                                skuNum = Integer.valueOf(retValue.getValue());
                            }
                            ZLogger.df(String.format("计算有多少可同步的商品类目关系:%d", skuNum));

                            //删除无效的数据
                            ProductCatalogService.getInstance()
                                    .deleteBy(String.format("isCloudActive = '%d'", 0));
                            int count = ProductCatalogService.getInstance().getCount();

                            ZLogger.df(String.format("商品类目关系表,（云端／本地）= (%d／%d)", skuNum, count));
                            if (count != skuNum) {
                                SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_SYNC_PRODUCTCATALOG_STARTCURSOR,
                                        "");
                            }

                            EventBus.getDefault().post(new GoodsSyncEvent(GoodsSyncEvent.EVENT_PRODUCT_CATALOG_UPDATED));
                        } catch (Exception e) {
                            ZLogger.ef(String.format("计算有多少可同步的商品类目关系失败:%s", e.toString()));
                        }
                        processQueue();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("计算有多少可同步的商品类目关系失败：" + errMsg);
//                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN,
//                                null, "Validate--会话过期，自动重登录");
                        processQueue();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        ProductCatalogApi.countProductCatalogSyncAbleNum(responseCallback);
    }


//    /**
//     * 下载前台类目
//     */
//    private void downloadFrontendCategory() {
//        if (!MfhLoginService.get().haveLogined()) {
//            sessionError();
//            return;
//        }
//
//        ZLogger.d("准备开始同步前台类目...");
//        frontCatalogPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
//        PosLocalCategoryService.get().deactiveAll();
//        //从第一页开始请求，每页最多50条记录
//        downloadFrontendCategory2(frontCatalogPageInfo);
//        frontCatalogPageInfo.setPageNo(1);
//    }
//
//    public void downloadFrontendCategory2(PageInfo pageInfo) {
//        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
//            networkError();
//            return;
//        }
//
//        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
//                new NetProcessor.QueryRsProcessor<CategoryInfo>(pageInfo) {
//                    @Override
//                    public void processQueryResult(RspQueryResult<CategoryInfo> rs) {
//                        saveFrontendCategory(pageInfo, rs);
//                    }
//
//                    @Override
//                    protected void processFailure(Throwable t, String errMsg) {
//                        super.processFailure(t, errMsg);
//                        ZLogger.df("加载前台类目数据失败:" + errMsg);
//                        nextStep();
//                    }
//                }, CategoryInfo.class, CashierApp.getAppContext());
//
//        ScCategoryInfoApi.list(CateApi.DOMAIN_TYPE_PROD, CateApi.PLAT,
//                CateApi.CATE_POSITION_FRONT, 1, MfhLoginService.get().getSpid(), pageInfo, queryRsCallBack);
//    }
//
//    /**
//     * 保存前台类目
//     */
//    private void saveFrontendCategory(final PageInfo pageInfo,
//                                      final RspQueryResult<CategoryInfo> rspQueryResult) {
//        Observable.create(new Observable.OnSubscribe<String>() {
//            @Override
//            public void call(Subscriber<? super String> subscriber) {
//                try {
//                    frontCatalogPageInfo = pageInfo;
//
//                    //保存前台类目数据
//                    if (rspQueryResult != null) {
//                        int retSize = rspQueryResult.getReturnNum();
//                        ZLogger.df(String.format("保存 %d/%d 个前台类目数据", retSize, rspQueryResult.getTotalNum()));
//                        for (EntityWrapper<CategoryInfo> wrapper : rspQueryResult.getRowDatas()) {
//                            CategoryInfo categoryInfo = wrapper.getBean();
//                            PosLocalCategoryService.get().saveOrUpdate(categoryInfo);
//                        }
//                    }
//
//                    //若还有继续发起请求
//                    if (pageInfo.hasNextPage()) {
//                        pageInfo.moveToNext();
//                        downloadFrontendCategory2(pageInfo);
//                    } else {
//                        int count = PosLocalCategoryService.get().getCount();
//                        int cloudNum = pageInfo.getTotalCount();
//                        if (count == cloudNum) {
//                            ZLogger.df(String.format("同步前台类目结束,云端类目(%d)和本地类目数量(%d)一致。",
//                                    cloudNum, count));
//                        } else {
//                            ZLogger.df(String.format("同步前台类目结束,云端类目(%d)和本地类目数量(%d)不一致",
//                                    cloudNum, count));
//
//                            //删除无效的数据
//                            PosLocalCategoryService.get().deleteBy(String.format("isCloudActive = '%d'",
//                                    PosLocalCategoryEntity.CLOUD_DEACTIVE));
//                            ZLogger.d(String.format("删除无效的数据，本地类目数量:%d",
//                                    PosLocalCategoryService.get().getCount()));
//                        }
//
//                        //通知刷新数据
//                        EventBus.getDefault().post(new DataSyncManager.DataSyncEvent(DataSyncManager.DataSyncEvent.EVENT_FRONTEND_CATEGORY_UPDATED));
//
//                        nextStep();
//                    }
//
//                } catch (Throwable ex) {
//                    ZLogger.ef(String.format("保存前台类目数据失败: %s", ex.toString()));
//                }
//
//                subscriber.onNext(null);
//                subscriber.onCompleted();
//            }
//        })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<String>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                    }
//
//                    @Override
//                    public void onNext(String startCursor) {
//
//                    }
//
//                });
//    }
}
