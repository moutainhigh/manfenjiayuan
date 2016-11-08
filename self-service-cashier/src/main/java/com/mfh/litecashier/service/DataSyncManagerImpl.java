package com.mfh.litecashier.service;


import com.alibaba.fastjson.JSONArray;
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
import com.mfh.framework.api.CompanyHumanApi;
import com.mfh.framework.api.anon.sc.ProductCatalogApi;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.category.CateApiImpl;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.category.CategoryOption;
import com.mfh.framework.api.category.CategoryQueryInfo;
import com.mfh.framework.api.category.ScCategoryInfoApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.bean.CompanyHuman;
import com.bingshanguxue.cashier.database.dao.PosProductNetDao;
import com.bingshanguxue.cashier.database.dao.PosProductSkuNetDao;
import com.mfh.litecashier.database.logic.CompanyHumanService;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

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
 * POS--数据同步
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DataSyncManagerImpl extends DataSyncManager {
    public static final int SYNC_STEP_NA = -1;
    //收银相关：同步频率较高
    public static final int SYNC_STEP_FRONTEND_CATEGORY = 0;//前台类目(一级类目)
    public static final int SYNC_STEP_PRODUCTS = 1;//商品库
    public static final int SYNC_STEP_PRODUCT_SKU = 2;//一品多码
    public static final int SYNC_STEP_FRONTENDCATEGORY_GOODS = 3;//前台类目&商品库－关系表

    //管理相关：同步频率较低
    public static final int SYNC_STEP_BACKEND_CATEGORYINFO = 4;//后台类目信息
    public static final int SYNC_STEP_COMPANY_HUMAN = 5;//账号,交接班切换账号需要

    private static final int MAX_SYNC_PRODUCTS_PAGESIZE = 70;
    private static final int MAX_SYNC_PAGESIZE = 40;

    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PRODUCTS_PAGESIZE);
    private PosProductNetDao posProductNetDao = new PosProductNetDao();

    private PageInfo mPosSkuPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private PosProductSkuNetDao posProductSkuNetDao = new PosProductSkuNetDao();

    private PageInfo mCompanyHumanPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private PageInfo frontCatalogPageInfo = new PageInfo(1, 1);
    private PageInfo productCateslogPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private boolean bSyncInProgress = false;//是否正在同步
    private int rollback = -1;
    private static final int MAX_ROLLBACK = 10;
    //当前同步进度
    private int nextStep = SYNC_STEP_NA;

    private static DataSyncManagerImpl instance = null;

    /**
     * 返回 DataSyncManagerImpl 实例
     *
     * @return
     */
    public static DataSyncManagerImpl get() {
        if (instance == null) {
            synchronized (DataSyncManagerImpl.class) {
                if (instance == null) {
                    instance = new DataSyncManagerImpl();
                }
            }
        }
        return instance;
    }

    /**
     * 下载更新POS数据库
     */
    public synchronized void sync() {
        if (bSyncInProgress) {
            rollback++;
            ZLogger.df(String.format("正在同步POS数据..., rollback=%d/%d", rollback, MAX_ROLLBACK));

            //同步异常出现无法结束，自动恢复同步
            if (rollback > MAX_ROLLBACK) {
                bSyncInProgress = false;
            }
            if (nextStep >= SYNC_STEP_NA) {
                nextStep = SYNC_STEP_FRONTEND_CATEGORY;
            }
            return;
        }

        rollback = -1;
        processStep(SYNC_STEP_FRONTEND_CATEGORY, SYNC_STEP_PRODUCTS);
    }

    public void sync(int step) {
        if (bSyncInProgress) {
            rollback++;
            ZLogger.df(String.format("正在同步POS数据..., rollback=%d/%d", rollback, MAX_ROLLBACK));

            //自动恢复同步
            if (rollback > MAX_ROLLBACK) {
                bSyncInProgress = false;
            }
            if (nextStep > step) {
                nextStep = step;
            }
            return;
        }

        rollback = -1;
        processStep(step, SYNC_STEP_NA);
    }

    /**
     * 下一步
     */
    private void nextStep() {
        ZLogger.d(String.format("nextStep ＝ %d", nextStep));
        processStep(nextStep, nextStep + 1);
    }

    private void processStep(int step, int nextStep) {
        ZLogger.d(String.format("step ＝ %d， nextStep ＝ %d", step, nextStep));
        this.nextStep = nextStep;
        this.bSyncInProgress = true;

        switch (step) {
            case SYNC_STEP_FRONTEND_CATEGORY: {
//                downloadFrontendCategory();
                getTopFrontId();
            }
            break;
            case SYNC_STEP_PRODUCTS: {
                startSyncProducts();
            }
            break;
            case SYNC_STEP_PRODUCT_SKU: {
                startSyncProductSku();
            }
            break;
            case SYNC_STEP_COMPANY_HUMAN: {
                startSyncHuman();
            }
            break;
            case SYNC_STEP_BACKEND_CATEGORYINFO: {
                downloadBackendCategoryInfo();
            }
            break;
            case SYNC_STEP_FRONTENDCATEGORY_GOODS: {
                downLoadProductCatalog();
            }
            break;
            default: {
                ZLogger.df("同步POS数据结束");
                bSyncInProgress = false;
                EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED));
            }
            break;
        }
    }

    private void networkError() {
        ZLogger.df("网络未连接，暂停同步POS数据。");
        bSyncInProgress = false;
        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    private void sessionError() {
        ZLogger.df("会话已失效，暂停同步POS数据。");
        bSyncInProgress = false;
        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }


    /**
     * 同步商品库
     */
    private void startSyncProducts() {
        ZLogger.d("准备同步POS商品档案...");
        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_SYNC_DATA_PROGRESS));

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (!MfhLoginService.get().haveLogined()) {
                    sessionError();
                    return;
                }

                String startCursor = DataSyncManager.getPosLastUpdateCursor();
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
                        sessionError();
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
            networkError();
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

                nextStep();
            }
        }, "/scGoodsSku/downLoadPosProduct");
    }

    /**
     * 保存POS商品档案
     */
    private void savePosProducts(final RspQueryResult<PosGoods> rs, final PageInfo pageInfo,
                                 final String startCursor) {
        if (rs == null) {
            nextStep();
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
                    SharedPreferencesHelper.setSyncProductsCursor(cussor);
                    ZLogger.df(String.format("保存 %d/%d(%d/%d) 个商品（%s） 结束", rs.getReturnNum(),
                            rs.getTotalNum(), mPageInfo.getPageNo(),
                            mPageInfo.getTotalPage(), SharedPreferencesHelper.getSyncProductsCursor()));

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
                        sessionError();
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
            sessionError();
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
                                SharedPreferencesHelper.setSyncProductsCursor("");

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

                            nextStep();
                        } catch (Exception e) {
                            ZLogger.ef(String.format("查询指定网点可同步sku总数:%s", e.toString()));
                            nextStep();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询指定网点可同步sku总数失败：" + errMsg);
//                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN,
//                                null, "Validate--会话过期，自动重登录");
                        nextStep();
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
            sessionError();
            return;
        }

        EmbMsgService.getInstance().setAllRead(IMBizType.TENANT_SKU_UPDATE);

        mPosSkuPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        String lastCursor = SharedPreferencesHelper.getSyncProductSkuCursor();

        //从第一页开始请求，每页最多50条记录
        downloadProductSku(lastCursor, mPosSkuPageInfo);
        mPosSkuPageInfo.setPageNo(1);
    }

    private void downloadProductSku(final String lastCursor, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            networkError();
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
                nextStep();
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
                    SharedPreferencesHelper.setPosSkuLastUpdate(cursor);
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
                ZLogger.df("同步规格码表品结束:" + SharedPreferencesHelper.getSyncProductSkuCursor());

                nextStep();
            }
        }
    }


    private PageInfo mWorderBeanPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    /**
     * 下载部门员工数据
     */
    @Deprecated
    private void downloadWorkerBeanInfoOfCompany() {
        if (!MfhLoginService.get().haveLogined()) {
            sessionError();
            return;
        }

        mWorderBeanPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        //从第一页开始请求，每页最多50条记录
        downloadWorkerBeanInfoOfCompany(mWorderBeanPageInfo);
        mWorderBeanPageInfo.setPageNo(1);
    }

    private void downloadWorkerBeanInfoOfCompany(PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<CompanyHuman>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<CompanyHuman> rs) {
                        //此处在主线程中执行。
                        new WorderBeanInfoQueryAsyncTask(pageInfo).execute(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("同步部门员工数据失败:" + errMsg);
                        nextStep();
                    }
                }, CompanyHuman.class, CashierApp.getAppContext());


        ZLogger.df(String.format("同步部门员工数据开始(%d/%d)", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        CompanyHumanApi.listWorkerBeanInfoOfCompany(pageInfo, queryRsCallBack);
    }

    public class WorderBeanInfoQueryAsyncTask extends AsyncTask<RspQueryResult<CompanyHuman>, Integer, Long> {
        private PageInfo pageInfo;

        public WorderBeanInfoQueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<CompanyHuman>... params) {
            try {
                RspQueryResult<CompanyHuman> rs = params[0];
                mWorderBeanPageInfo = pageInfo;
                //第一页，清空旧数据
                if (mWorderBeanPageInfo.getPageNo() == 1) {
                    ZLogger.df("清空旧部门员工数据");
                    CompanyHumanService.get().clear();
                }

                if (rs == null) {
                    return -1L;
                }

                //保存下来
                int retSize = rs.getReturnNum();
                ZLogger.df(String.format("同步 %d/%d 个部门员工数据", retSize, rs.getTotalNum()));
                for (EntityWrapper<CompanyHuman> wrapper : rs.getRowDatas()) {
                    CompanyHumanService.get().saveOrUpdate(wrapper.getBean());
                }

            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.ef(String.format("同步部门员工数据失败: %s", ex.toString()));
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
                downloadWorkerBeanInfoOfCompany(pageInfo);
            } else {
                ZLogger.df("同步部门员工数据结束");
                nextStep();
            }
        }
    }

    /**
     * 同步账号数据
     */
    private void startSyncHuman() {
        if (!MfhLoginService.get().haveLogined()) {
            sessionError();
            return;
        }

        mCompanyHumanPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        //从第一页开始请求，每页最多50条记录
        downloadCompanyHuman(mCompanyHumanPageInfo);
        mCompanyHumanPageInfo.setPageNo(1);
    }

    public void downloadCompanyHuman(PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }


        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<CompanyHuman>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<CompanyHuman> rs) {
                //此处在主线程中执行。
                new CompanyHumanQueryAsyncTask(pageInfo).execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.df("加载自账号数据失败:" + errMsg);
                nextStep();
            }
        }, CompanyHuman.class, CashierApp.getAppContext());

        ZLogger.df(String.format("同步账号数据开始(%d/%d)", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        CompanyHumanApi.findCompUserPwdInfo(pageInfo, queryRsCallBack);
    }

    public class CompanyHumanQueryAsyncTask extends AsyncTask<RspQueryResult<CompanyHuman>, Integer, Long> {
        private PageInfo pageInfo;

        public CompanyHumanQueryAsyncTask(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }

        @Override
        protected Long doInBackground(RspQueryResult<CompanyHuman>... params) {
            saveQueryResult(params[0], pageInfo);
            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            //若还有继续发起请求
            if (pageInfo.hasNextPage()) {
                pageInfo.moveToNext();
                downloadCompanyHuman(pageInfo);
            } else {
                ZLogger.df("同步账号数据结束");
                nextStep();
            }
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(RspQueryResult<CompanyHuman> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
                mCompanyHumanPageInfo = pageInfo;
                //第一页，清空旧数据
                if (mCompanyHumanPageInfo.getPageNo() == 1) {
                    ZLogger.df("清空旧账号数据");
                    CompanyHumanService.get().clear();
                }

                if (rs == null) {
                    return;
                }

                //保存下来
                int retSize = rs.getReturnNum();
                ZLogger.df(String.format("保存 %d/%d 个账号数据", retSize, rs.getTotalNum()));
                for (EntityWrapper<CompanyHuman> wrapper : rs.getRowDatas()) {
                    CompanyHumanService.get().saveOrUpdate(wrapper.getBean());
                }

            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.ef(String.format("保存账号数据失败: %s", ex.toString()));
            }
        }
    }

    private void getTopFrontId() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            networkError();
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
                        nextStep();
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
            networkError();
            return;
        }

        NetCallBack.NetTaskCallBack createRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("创建前台类目失败, " + errMsg);
                        nextStep();
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
                                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_L_CATETYPE_POS_ID, code);

                                CategoryInfo categoryInfo = new CategoryInfo();
                                categoryInfo.setId(code);
                                categoryInfo.setNameCn("POS前台类目");
                                getCodeValue(categoryInfo);
                            }

                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
                        nextStep();
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
//            SharedPreferencesHelper.set(SharedPreferencesHelper.PK_L_CATETYPE_POS_ID, 0L);
//            nextStep();
            return;
        }
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_L_CATETYPE_POS_ID, categoryInfo.getId());
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
                        }

                        //通知刷新数据
                        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_FRONTEND_CATEGORY_UPDATED));

                        nextStep();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("加载前台根目录子类目 失败, " + errMsg);
                        nextStep();
                    }
                }
                , CategoryInfo.class
                , MfhApplication.getAppContext()) {
        };

        ScCategoryInfoApi.getCodeValue(categoryInfo.getId(), queryRsCallBack);
    }

    /**
     * 下载前台类目
     */
    private void downloadFrontendCategory() {
        if (!MfhLoginService.get().haveLogined()) {
            sessionError();
            return;
        }

        ZLogger.d("准备开始同步前台类目...");
        frontCatalogPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        PosLocalCategoryService.get().deactiveAll();
        //从第一页开始请求，每页最多50条记录
        downloadFrontendCategory2(frontCatalogPageInfo);
        frontCatalogPageInfo.setPageNo(1);
    }

    public void downloadFrontendCategory2(PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<CategoryInfo>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<CategoryInfo> rs) {
                        saveFrontendCategory(pageInfo, rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("加载前台类目数据失败:" + errMsg);
                        nextStep();
                    }
                }, CategoryInfo.class, CashierApp.getAppContext());

        ScCategoryInfoApi.list(CateApi.DOMAIN_TYPE_PROD, CateApi.PLAT,
                CateApi.CATE_POSITION_FRONT, 1, MfhLoginService.get().getSpid(), pageInfo, queryRsCallBack);
    }

    /**
     * 保存前台类目
     */
    private void saveFrontendCategory(final PageInfo pageInfo,
                                      final RspQueryResult<CategoryInfo> rspQueryResult) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    frontCatalogPageInfo = pageInfo;

                    //保存前台类目数据
                    if (rspQueryResult != null) {
                        int retSize = rspQueryResult.getReturnNum();
                        ZLogger.df(String.format("保存 %d/%d 个前台类目数据", retSize, rspQueryResult.getTotalNum()));
                        for (EntityWrapper<CategoryInfo> wrapper : rspQueryResult.getRowDatas()) {
                            CategoryInfo categoryInfo = wrapper.getBean();
                            PosLocalCategoryService.get().saveOrUpdate(categoryInfo);
                        }
                    }

                    //若还有继续发起请求
                    if (pageInfo.hasNextPage()) {
                        pageInfo.moveToNext();
                        downloadFrontendCategory2(pageInfo);
                    } else {
                        int count = PosLocalCategoryService.get().getCount();
                        int cloudNum = pageInfo.getTotalCount();
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
                        }

                        //通知刷新数据
                        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_FRONTEND_CATEGORY_UPDATED));

                        nextStep();
                    }

                } catch (Throwable ex) {
                    ZLogger.ef(String.format("保存前台类目数据失败: %s", ex.toString()));
                }

                subscriber.onNext(null);
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
                    }

                    @Override
                    public void onNext(String startCursor) {

                    }

                });
    }

    /**
     * 下载后台类目树
     */
    private void downloadBackendCategoryInfo() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        ZLogger.df("下载后台类目树开始");
        CateApiImpl.listBackendCategory(CateApi.DOMAIN_TYPE_PROD,
                "",//CateApi.BACKEND_CATE_BTYPE_NORMAL,
                CateApi.CATE_POSITION_BACKEND, 2,
                backendCategoryInfoRspCallback);
    }

    private NetCallBack.NetTaskCallBack backendCategoryInfoRspCallback = new NetCallBack.NetTaskCallBack<CategoryQueryInfo,
            NetProcessor.Processor<CategoryQueryInfo>>(
            new NetProcessor.Processor<CategoryQueryInfo>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("加载后台类目树失败, " + errMsg);
                    nextStep();
                }

                @Override
                public void processResult(IResponseData rspData) {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                    CategoryQueryInfo categoryQueryInfo = null;

                    if (rspData != null) {
                        RspBean<CategoryQueryInfo> retValue = (RspBean<CategoryQueryInfo>) rspData;
                        categoryQueryInfo = retValue.getValue();
                    }

                    if (categoryQueryInfo != null) {
                        //缓存数据
                        saveBackendCategoryInfoCache(categoryQueryInfo.getOptions());
                    } else {
                        saveBackendCategoryInfoCache(null);
                    }
                }
            }
            , CategoryQueryInfo.class
            , CashierApp.getAppContext()) {
    };

    /**
     * 保存后台类目树
     */
    private void saveBackendCategoryInfoCache(List<CategoryOption> options) {
        ZLogger.df(String.format("保存POS %d个后台类目",
                (options != null ? options.size() : 0)));
        //缓存数据
        JSONArray cacheArrays = new JSONArray();
        if (options != null && options.size() > 0) {
            for (CategoryOption option : options) {
                cacheArrays.add(option);
            }
        }
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(ACacheHelper.CK_STOCKGOODS_CATEGORY, cacheArrays.toJSONString());

        //通知刷新数据
        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO));

        nextStep();
    }


    /**
     * 同步商品和类目关系表
     */
    private void downLoadProductCatalog() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (!MfhLoginService.get().haveLogined()) {
                    sessionError();
                    return;
                }

                String startCursor = DataSyncManager.getProductCatalogStartCursor();
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
                        sessionError();
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
            networkError();
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
                        nextStep();
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
                    SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PRODUCTCATALOG_STARTCURSOR,
                            TimeCursor.InnerFormat.format(cursor));
                }
                ZLogger.df(String.format("保存 %d/%d 个商品类目关系表（%s）",
                        retSize, rs.getTotalNum(),
                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_SYNC_PRODUCTCATALOG_STARTCURSOR)));
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
            sessionError();
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
                            ZLogger.df(String.format("计算有多少可同步的商品类目关系:%d", skuNum));

                            int count = ProductCatalogService.getInstance().getCount();
                            if (count == skuNum) {
                                ZLogger.df(String.format("计算有多少可同步的商品类目关系结束,云端(%d)和本地数量(%d)一致。",
                                        skuNum, count));
                            } else {
                                ZLogger.df(String.format("计算有多少可同步的商品类目关系结束,云端(%d)和本地数量(%d)不一致，下次需要全量更新",
                                        skuNum, count));

                                SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PRODUCTCATALOG_STARTCURSOR,
                                        "");

                                //删除无效的数据
                                ProductCatalogService.getInstance()
                                        .deleteBy(String.format("isCloudActive = '%d'", 0));
                                ZLogger.d(String.format("删除无效的数据，本地类目数量:%d",
                                        ProductCatalogService.getInstance().getCount()));
                            }

                            EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_PRODUCT_CATALOG_UPDATED));


                            nextStep();
                        } catch (Exception e) {
                            ZLogger.ef(String.format("计算有多少可同步的商品类目关系失败:%s", e.toString()));
                            nextStep();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("计算有多少可同步的商品类目关系失败：" + errMsg);
//                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN,
//                                null, "Validate--会话过期，自动重登录");
                        nextStep();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        ProductCatalogApi.countProductCatalogSyncAbleNum(responseCallback);
    }

    public static class DataSyncEvent {
        public static final int EVENT_ID_REFRESH_BACKEND_CATEGORYINFO = 0X03;//刷新后台类目树
        public static final int EVENT_FRONTEND_CATEGORY_UPDATED = 0X04;//前台类目更新
        public static final int EVENT_PRODUCT_CATALOG_UPDATED = 0X05;//前台类目和商品库关系更新
        public static final int EVENT_ID_SYNC_DATA_PROGRESS = 0X11;//同步进度
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X12;//同步结束

        private int eventId;

        public DataSyncEvent(int eventId) {
            this.eventId = eventId;
        }

        public int getEventId() {
            return eventId;
        }
    }
}
