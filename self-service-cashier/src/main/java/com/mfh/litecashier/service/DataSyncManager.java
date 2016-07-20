package com.mfh.litecashier.service;


import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.CashierApi;
import com.mfh.framework.api.CateApi;
import com.mfh.framework.api.impl.CateApiImpl;
import com.mfh.framework.api.impl.ScGoodsSkuApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetFactory;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.manfenjiayuan.business.bean.CategoryInfo;
import com.manfenjiayuan.business.bean.CategoryOption;
import com.mfh.litecashier.bean.CompanyHuman;
import com.mfh.litecashier.bean.PosCategory;
import com.bingshanguxue.cashier.model.PosGoods;
import com.mfh.litecashier.bean.ProductSkuBarcode;
import com.mfh.litecashier.database.dao.PosProductNetDao;
import com.mfh.litecashier.database.dao.PosProductSkuNetDao;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.mfh.litecashier.database.logic.CompanyHumanService;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.mfh.litecashier.database.logic.PosProductSkuService;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * POS--数据同步
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DataSyncManager {
    public static final int SYNC_STEP_NA = -1;
    public static final int SYNC_STEP_BACKEND_CATEGORYINFO          = 0;//后台类目信息
    public static final int SYNC_STEP_BACKEND_CATEGORYINFO_FRESH    = 1;//后台生鲜类目信息
//    public static final int SYNC_STEP_FRONTEND_CATEGORYINFO_FRESH   = 2;//前台生鲜类目信息（和微信端一致）
    public static final int SYNC_STEP_PUBLIC_FRONTEND_CATEGORYINFO  = 2;//公共前台类目信息
    public static final int SYNC_STEP_COSTOM_FRONTEND_CATEGORYINFO  = 3;//自定义前台类目信息
    public static final int SYNC_STEP_PRODUCT_SKU   = 4;//一品多码
    public static final int SYNC_STEP_PRODUCTS      = 5;//商品库
    public static final int SYNC_STEP_COMPANY_HUMAN = 6;//账号


    private static final int MAX_SYNC_PRODUCTS_PAGESIZE = 50;
    private static final int MAX_SYNC_PAGESIZE = 20;

    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PRODUCTS_PAGESIZE);
    private PosProductNetDao posProductNetDao = new PosProductNetDao();

    private PageInfo mPosSkuPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private PosProductSkuNetDao posProductSkuNetDao = new PosProductSkuNetDao();

    private PageInfo mCompanyHumanPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private boolean bSyncInProgress = false;//是否正在同步
    //当前同步进度
    private int nextStep = SYNC_STEP_NA;

    private static DataSyncManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static DataSyncManager get() {
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
     * 下载更新POS数据库
     */
    public synchronized void sync() {
        if (bSyncInProgress) {
            ZLogger.df("正在同步POS数据...");
            if (nextStep > SYNC_STEP_BACKEND_CATEGORYINFO){
                nextStep = SYNC_STEP_BACKEND_CATEGORYINFO;
            }
            return;
        }

        processStep(SYNC_STEP_BACKEND_CATEGORYINFO, SYNC_STEP_BACKEND_CATEGORYINFO_FRESH);
    }

    public void sync(int step) {
        if (bSyncInProgress) {
            ZLogger.df("正在同步POS数据...");
            if (nextStep > step){
                nextStep = step;
            }
            return;
        }

        processStep(step, SYNC_STEP_NA);
    }

    /**
     * 下一步
     */
    private void nextStep() {
        processStep(nextStep, nextStep + 1);
    }

    private void processStep(int step, int nextStep) {
        this.nextStep = nextStep;
        this.bSyncInProgress = true;

        switch (step) {
            case SYNC_STEP_BACKEND_CATEGORYINFO: {
                downloadBackendCategoryInfo();
            }
            break;
            case SYNC_STEP_BACKEND_CATEGORYINFO_FRESH: {
                downloadBackendFreshCategoryInfo();
            }
            break;
//            case SYNC_STEP_FRONTEND_CATEGORYINFO_FRESH: {
//                downloadFrontendFreshCategoryInfo();
//            }
//            break;
            case SYNC_STEP_PUBLIC_FRONTEND_CATEGORYINFO: {
                downloadPublicFrontCategory();
            }
            break;
            case SYNC_STEP_COSTOM_FRONTEND_CATEGORYINFO: {
                downloadCustomFrontCategory();
            }
            break;
            case SYNC_STEP_PRODUCT_SKU: {
                startSyncProductSku();
            }
            break;
            case SYNC_STEP_PRODUCTS: {
                startSyncProducts();
            }
            break;
            case SYNC_STEP_COMPANY_HUMAN: {
                startSyncHuman();
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
     * 获取POS商品库同步时间
     */
    private String getPosLastUpdateCursor() {
        String lastCursor = SharedPreferencesHelper.getSyncProductsCursor();
        ZLogger.df(String.format("上次商品更新时间(%s)。", lastCursor));

//        //得到指定模范的时间
//        if (!StringUtils.isEmpty(lastCursor)) {
//            try {
//                Date d1 = TimeCursor.InnerFormat.parse(lastCursor);
//                Date d2 = new Date();
//                if (d1.compareTo(d2) > 0) {
//                    lastCursor = TimeCursor.InnerFormat.format(d2);
//                    SharedPreferencesHelper.setSyncProductsCursor(d2);
//                    ZLogger.df(String.format("上次商品更新时间大于当前时间，使用当前时间(%s)。", lastCursor));
//                }
//            } catch (ParseException e) {
////            e.printStackTrace();
//                ZLogger.ef(String.format("获取POS商品库同步时间失败: %s", e.toString()));
//            }
//        }

        return lastCursor;
    }

    /**
     * 同步商品库
     */
    private void startSyncProducts() {
        if (!MfhLoginService.get().haveLogined()) {
            sessionError();
            return;
        }

        //需要全量同步
        if (SharedPreferencesHelper.getSyncProductsMode() == 0) {
            //设置时间游标
            SharedPreferencesHelper.setSyncProductsCursor("");
            //删除旧数据
            PosProductService.get().clear();

            ZLogger.df("同步商品库：全量更新，重置游标，删除旧数据");
        }
        //从第一页开始请求，每页最多50条记录
        mPageInfo = new PageInfo(-1, MAX_SYNC_PRODUCTS_PAGESIZE);
        downloadProducts(getPosLastUpdateCursor(), mPageInfo);
        mPageInfo.setPageNo(1);
    }

    private void downloadProducts(final String lastCursor, PageInfo pageInfo) {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PRODUCTS_ENABLED, false);

        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));//部门编号
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));//公司编号
        params.put("startCursor", lastCursor);//游标
//        params.put("page", Integer.toString(pageInfo.getPageNo()));
//        params.put("rows", Integer.toString(pageInfo.getPageSize()));
//        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());

        bSyncInProgress = true;
        ZLogger.df(String.format("同步商品开始(%d/%d/%s)", pageInfo.getPageNo(), pageInfo.getTotalPage(), lastCursor));
        posProductNetDao.query(params, new NetProcessor.QueryRsProcessor<PosGoods>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<PosGoods> rs) {
                //此处在主线程中执行。
                new ProductsQueryAsyncTask(pageInfo, lastCursor)
                        .execute(rs);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);

                nextStep();
            }
        }, "/scGoodsSku/downLoadPosProduct");
    }

    private class ProductsQueryAsyncTask extends AsyncTask<RspQueryResult<PosGoods>, Integer, Long> {
        private PageInfo pageInfo;
        private String lastCursor;
        private boolean isInterrupted;

        public ProductsQueryAsyncTask(PageInfo pageInfo, String lastCursor) {
            this.pageInfo = pageInfo;
            this.lastCursor = lastCursor;
        }

        @Override
        protected Long doInBackground(RspQueryResult<PosGoods>... params) {
            saveQueryResult(params[0], pageInfo);
            return -1L;
//        return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            //若还有继续发起请求
            if (!isInterrupted && pageInfo.hasNextPage()) {
                pageInfo.moveToNext();
                downloadProducts(lastCursor, pageInfo);
            } else {
                ZLogger.df("同步商品库结束" + SharedPreferencesHelper.getSyncProductsCursor());
                //商品同步结束后不立刻执行下一步操作，而是去检查商品数是否和后台商品数一致。
//                nextStep();
                countNetSyncAbleSkuNum();
            }
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(RspQueryResult<PosGoods> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
                mPageInfo = pageInfo;

                if (rs == null) {
                    return;
                }

                //使用事务
                for (EntityWrapper<PosGoods> wrapper : rs.getRowDatas()) {
                    //保存商品到数据库
                    PosGoods product = wrapper.getBean();
                    PosProductService.get().saveOrUpdate(product);

                    //更新游标
                    SharedPreferencesHelper.setSyncProductsCursor(product.getUpdatedDate());
                    //设置增量更新
                    SharedPreferencesHelper.setSyncProductsMode(1);
                }
                ZLogger.df(String.format("同步 %d/%d 个商品（%s）", rs.getReturnNum(),
                        rs.getTotalNum(), SharedPreferencesHelper.getSyncProductsCursor()));
            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("保存商品库失败: %s", ex.toString()));
                isInterrupted = true;
            }
        }
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
                                    .queryAllByDesc(String.format("tenantId = '%d'", MfhLoginService.get().getSpid()));
                            int posNum = (entityList != null ? entityList.size() : 0);
                            if (posNum != skuNum) {
                                ZLogger.df(String.format("pos本地商品数目(%d)和后台商品数目(%d)不一致,下一次需要全量同步商品库", posNum, skuNum));

//                                SharedPreferencesHelper.
                                //初始化游标并设置下次需要全量更新
                                SharedPreferencesHelper.setSyncProductsCursor("");
                                SharedPreferencesHelper.setSyncProductsMode(0);
                            } else {
                                ZLogger.df(String.format("pos本地商品数目(%d)和后台商品数目(%d)一致", posNum, skuNum));
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

        mPosSkuPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);

        String lastCursor = SharedPreferencesHelper.getSyncProductSkuCursor();

        //从第一页开始请求，每页最多50条记录
        downloadProductSku(lastCursor, mPosSkuPageInfo);
        mPosSkuPageInfo.setPageNo(1);
    }

    private void downloadProductSku(final String lastCursor, PageInfo pageInfo) {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
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
                //此处在主线程中执行。
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
                downloadProductSku(lastCursor, pageInfo);
            } else {
                ZLogger.df("同步规格码表品结束:" + SharedPreferencesHelper.getSyncProductSkuCursor());

                nextStep();
            }
        }

        /**
         * 将后台返回的结果集保存到本地,同步执行
         *
         * @param rs       结果集
         * @param pageInfo 分页信息
         */
        private void saveQueryResult(RspQueryResult<ProductSkuBarcode> rs, PageInfo pageInfo) {//此处在主线程中执行。
            try {
                mPosSkuPageInfo = pageInfo;

                if (rs == null) {
                    return;
                }

                //保存下来
                int retSize = rs.getReturnNum();
                for (EntityWrapper<ProductSkuBarcode> wrapper : rs.getRowDatas()) {
                    ProductSkuBarcode bean = wrapper.getBean();
                    if (bean != null) {
                        PosProductSkuService.get().saveOrUpdate(bean);

                        //更新游标
                        SharedPreferencesHelper.setPosSkuLastUpdate(bean.getCreatedDate());
                    }
                }
                ZLogger.df(String.format("同步 %d/%d 个箱规", retSize, rs.getTotalNum()));

            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.ef(String.format("同步码表失败: %s", ex.toString()));
            }
        }
    }


    private PageInfo mWorderBeanPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    /**
     * 下载部门员工数据
     */
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

    public void downloadWorkerBeanInfoOfCompany(PageInfo pageInfo) {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        AjaxParams params = new AjaxParams();
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<CompanyHuman>(pageInfo) {
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
        AfinalFactory.postDefault(CashierApi.URL_COMPANYHUMAN_LIST_WORKERBEANINFO_OFCOMPANY, params, queryRsCallBack);
    }

    public class WorderBeanInfoQueryAsyncTask extends AsyncTask<RspQueryResult<CompanyHuman>, Integer, Long> {
        private PageInfo pageInfo;

        public WorderBeanInfoQueryAsyncTask(PageInfo pageInfo) {
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
                downloadWorkerBeanInfoOfCompany(pageInfo);
            } else {
                ZLogger.df("同步部门员工数据结束");
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
                mWorderBeanPageInfo = pageInfo;
                //第一页，清空旧数据
                if (mWorderBeanPageInfo.getPageNo() == 1) {
                    ZLogger.df("清空旧部门员工数据");
                    CompanyHumanService.get().clear();
                }

                if (rs == null) {
                    return;
                }

                //保存下来
                int retSize = rs.getReturnNum();
                ZLogger.df(String.format("同步 %d/%d 个部门员工数据", retSize, rs.getTotalNum()));
                for (EntityWrapper<CompanyHuman> wrapper : rs.getRowDatas()) {
                    CompanyHumanService.get().saveOrUpdate(wrapper.getBean());
                }

            } catch (Throwable ex) {
//            throw new RuntimeException(ex);
                ZLogger.e(String.format("同步部门员工数据失败: %s", ex.toString()));
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
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        AjaxParams params = new AjaxParams();
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
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
        AfinalFactory.postDefault(CashierApi.URL_COMPANYHUMAN_FIND_COMPUSERPWDINFO, params, queryRsCallBack);
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

    /**
     * 下载后台类目树
     */
    private void downloadBackendCategoryInfo() {
        if (!SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, true)) {
            ZLogger.df("使用后台类目缓存数据，暂不加载新数据");
            nextStep();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        ZLogger.df("下载后台类目树开始");
        CateApiImpl.listBackendCategory(CateApi.DOMAIN_TYPE_PROD, CateApi.BACKEND_CATE_BTYPE_NORMAL,
                CateApi.CATE_POSITION_BACKEND, 2, backendCategoryInfoRspCallback);
    }

    private NetCallBack.NetTaskCallBack backendCategoryInfoRspCallback = new NetCallBack.NetTaskCallBack<CategoryInfo,
            NetProcessor.Processor<CategoryInfo>>(
            new NetProcessor.Processor<CategoryInfo>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("加载后台类目树失败, " + errMsg);
                    nextStep();
                }

                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null) {
                        saveBackendCategoryInfoCache(null);
                        nextStep();
                        return;
                    }
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                    RspBean<CategoryInfo> retValue = (RspBean<CategoryInfo>) rspData;
                    CategoryInfo categoryInfo = retValue.getValue();

                    if (categoryInfo != null) {
                        //缓存数据
                        saveBackendCategoryInfoCache(categoryInfo.getOptions());
                    } else {
                        saveBackendCategoryInfoCache(null);
                    }

                    //通知刷新数据
                    EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO));

                    nextStep();
                }
            }
            , CategoryInfo.class
            , CashierApp.getAppContext()) {
    };

    /**
     * 缓存后台类目树
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

            SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_ENABLED, false);
        }
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(ACacheHelper.CK_STOCKGOODS_CATEGORY, cacheArrays.toJSONString());
    }

    /**
     * 下载生鲜后台类目树
     */
    private void downloadBackendFreshCategoryInfo() {
        if (!SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_FRESH_ENABLED, true)) {
            ZLogger.df("使用后台生鲜类目缓存数据，暂不加载新数据");
            nextStep();
            //通知刷新数据
            EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO_FRESH));

            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            //通知刷新数据
            EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO_FRESH));
            return;
        }

        ZLogger.df("下载后台生鲜类目树开始");
        CateApiImpl.listBackendCategory(CateApi.DOMAIN_TYPE_PROD, CateApi.BACKEND_CATE_BTYPE_FRESH,
                CateApi.CATE_POSITION_BACKEND, 2, freshBackendCategoryInfoRspCallback);
    }

    private NetCallBack.NetTaskCallBack freshBackendCategoryInfoRspCallback = new NetCallBack.NetTaskCallBack<CategoryInfo,
            NetProcessor.Processor<CategoryInfo>>(
            new NetProcessor.Processor<CategoryInfo>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("加载后台生鲜类目树失败, " + errMsg);
                    //通知刷新数据
                    EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO_FRESH));

                    nextStep();
                }

                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null) {
                        saveBackendFreshCategoryInfoCache(null);
                        return;
                    }
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                    RspBean<CategoryInfo> retValue = (RspBean<CategoryInfo>) rspData;
                    CategoryInfo categoryInfo = retValue.getValue();

                    if (categoryInfo != null) {
                        //缓存数据
                        saveBackendFreshCategoryInfoCache(categoryInfo.getOptions());
                    } else {
                        saveBackendFreshCategoryInfoCache(null);
                    }

                    //通知刷新数据
                    EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_BACKEND_CATEGORYINFO_FRESH));

                    nextStep();
                }
            }
            , CategoryInfo.class
            , CashierApp.getAppContext()) {
    };

    /**
     * 缓存后台生鲜类目树
     */
    private void saveBackendFreshCategoryInfoCache(List<CategoryOption> options) {

        //缓存数据
        JSONArray cacheArrays = new JSONArray();
        if (options != null && options.size() > 0) {
            ZLogger.df(String.format("保存 %d个后台生鲜类目", options.size()));
            for (CategoryOption option : options) {
                cacheArrays.add(option);
            }

            SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_BACKEND_CATEGORYINFO_FRESH_ENABLED, false);
        }
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(ACacheHelper.CK_BACKEND_CATEGORY_FRESH, cacheArrays.toJSONString());
    }



    /**
     * 下载公共前台类目
     */
    private void downloadPublicFrontCategory() {
        if (!SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_PUBLIC_FRONTCATEGORY_ENABLED, true)) {
            ZLogger.df("使用前台公共类目缓存数据，暂不加载新数据");
            //更新UI
//            EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_CLOUD_CATEGORY_UPDATED));
            nextStep();
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            sessionError();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        ZLogger.df("同步前台公共类目开始");
        CateApiImpl.comnqueryCategory(CateApi.DOMAIN_TYPE_PROD, CateApi.POS,
                CateApi.CATE_POSITION_FRONT,
                1, null, publicFrontCategoryRespCallback);
    }

    private NetCallBack.NetTaskCallBack publicFrontCategoryRespCallback = new NetCallBack.NetTaskCallBack<CategoryInfo,
            NetProcessor.Processor<CategoryInfo>>(
            new NetProcessor.Processor<CategoryInfo>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("加载前台公共类目树失败, " + errMsg);
                    nextStep();
                }

                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null) {
                        savePublicFrontCategoryInfoCache(null);
                        return;
                    }

                    ZLogger.df("加载前台公共类目树成功, ");
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                    RspBean<CategoryInfo> retValue = (RspBean<CategoryInfo>) rspData;
                    CategoryInfo categoryInfo = retValue.getValue();

                    downloadPublicFrontCategory2(categoryInfo);
                }
            }
            , CategoryInfo.class
            , CashierApp.getAppContext()) {
    };


    private void downloadPublicFrontCategory2(CategoryInfo categoryInfo) {
        if (categoryInfo == null) {
            savePublicFrontCategoryInfoCache(null);

            //通知刷新数据
//            EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_CLOUD_CATEGORY_UPDATED));
            nextStep();
            return;
        }

        List<CategoryOption> options = categoryInfo.getOptions();
        if (options == null || options.size() < 1) {
            ZLogger.df("前台公共类目为空");
            savePublicFrontCategoryInfoCache(null);
//            EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_CLOUD_CATEGORY_UPDATED));
            nextStep();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        CategoryOption option = options.get(0);
        ZLogger.df(String.format("同步前台公共二级类目(%s)开始", option.getValue()));
        NetCallBack.NetTaskCallBack queryRsCallBack = new NetCallBack.NetTaskCallBack<PosCategory,
                NetProcessor.Processor<PosCategory>>(
                new NetProcessor.Processor<PosCategory>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        List<PosCategory> items = new ArrayList<>();
                        if (rspData != null) {
                            RspListBean<PosCategory> retValue = (RspListBean<PosCategory>) rspData;
                            items = retValue.getValue();
                        }

                        savePublicFrontCategoryInfoCache(items);

                        //更新UI
//                        EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_CLOUD_CATEGORY_UPDATED));

                        nextStep();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        1:12345卡芯片号不存在，请重新输入!
                        ZLogger.df("加载POS前台公共二级类目 失败, " + errMsg);
                        nextStep();
                    }
                }
                , PosCategory.class
                , CashierApp.getAppContext()) {
        };

        CateApiImpl.listPublicCategory(option.getCode(), queryRsCallBack);
    }


    /**
     * 缓存前台公共类目树
     */
    private void savePublicFrontCategoryInfoCache(List<PosCategory> options) {
        ZLogger.df(String.format("加载POS %d个前台公共二级类目",
                (options != null ? options.size() : 0)));
        //缓存数据
        JSONArray cacheArrays = new JSONArray();
        if (options != null && options.size() > 0) {
            for (PosCategory option : options) {
//                PosCategory category = PosCategory.generateCloud(option.getCode(), option.getValue(), option.);
                cacheArrays.add(option);
            }
        }
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(ACacheHelper.CK_PUBLIC_FRONT_CATEGORY, cacheArrays.toJSONString());

        //设置下次不需要自动更新商品类目，可以在收银页面点击同步按钮修改
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_PUBLIC_FRONTCATEGORY_ENABLED, false);
    }

    /**
     * 下载私有前台类目
     */
    private void downloadCustomFrontCategory() {
        if (!SharedPreferencesHelper.getBoolean(SharedPreferencesHelper.PK_SYNC_CUSTOM_FRONTCATEGORY_ENABLED, true)) {
            ZLogger.df("使用前台自定义（私有）类目缓存数据，暂不加载新数据");
            //更新UI
//            EventBus.getDefault().post(new CashierAffairEvent(CashierAffairEvent.EVENT_ID_CLOUD_CATEGORY_UPDATED));
            nextStep();
            return;
        }
        if (!MfhLoginService.get().haveLogined()) {
            sessionError();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        ZLogger.df("同步前台自定义（私有）类目开始");
        CateApiImpl.comnqueryCategory(CateApi.DOMAIN_TYPE_PROD, CateApi.POS,
                CateApi.CATE_POSITION_FRONT,
                1, MfhLoginService.get().getSpid(), customFrontCategoryRespCallback);
    }

    private NetCallBack.NetTaskCallBack customFrontCategoryRespCallback = new NetCallBack.NetTaskCallBack<CategoryInfo,
            NetProcessor.Processor<CategoryInfo>>(
            new NetProcessor.Processor<CategoryInfo>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("加载前台自定义（私有）类目树失败, " + errMsg);
                    nextStep();
                }

                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null) {
                        saveCustomFrontCategoryInfoCache(null);
                        return;
                    }
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                    RspBean<CategoryInfo> retValue = (RspBean<CategoryInfo>) rspData;
                    CategoryInfo categoryInfo = retValue.getValue();

                    downloadCustomFrontCategory2(categoryInfo);
                }
            }
            , CategoryInfo.class
            , CashierApp.getAppContext()) {
    };

    private void downloadCustomFrontCategory2(CategoryInfo categoryInfo) {
        if (categoryInfo == null) {
            saveCustomFrontCategoryInfoCache(null);

            //通知刷新数据
            EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_FRONT_CATEGORYINFO));
            nextStep();
            return;
        }

        List<CategoryOption> options = categoryInfo.getOptions();
        if (options == null || options.size() < 1) {
            ZLogger.df("前台自定义（私有）类目为空");
            saveCustomFrontCategoryInfoCache(null);
            EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_FRONT_CATEGORYINFO));
            nextStep();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        CategoryOption option = options.get(0);
        ZLogger.df(String.format("同步前台自定义（私有）二级类目(%s)开始", option.getValue()));
        NetCallBack.NetTaskCallBack queryRsCallBack = new NetCallBack.NetTaskCallBack<PosCategory,
                NetProcessor.Processor<PosCategory>>(
                new NetProcessor.Processor<PosCategory>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        List<PosCategory> items = new ArrayList<>();
                        if (rspData != null) {
                            RspListBean<PosCategory> retValue = (RspListBean<PosCategory>) rspData;
                            items = retValue.getValue();
                        }

                        saveCustomFrontCategoryInfoCache(items);

                        //更新UI
                        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_REFRESH_FRONT_CATEGORYINFO));

                        nextStep();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("加载前台自定义（私有）二级类目 失败, " + errMsg);
                        nextStep();
                    }
                }
                , PosCategory.class
                , CashierApp.getAppContext()) {
        };

        CateApiImpl.listPublicCategory(option.getCode(), queryRsCallBack);
    }

    /**
     * 缓存前台私有类目树
     */
    private void saveCustomFrontCategoryInfoCache(List<PosCategory> options) {
        ZLogger.df(String.format("保存POS %d个前台自定义（私有）二级类目",
                (options != null ? options.size() : 0)));
        //缓存数据
        JSONArray cacheArrays = new JSONArray();
        if (options != null && options.size() > 0) {
            for (PosCategory option : options) {
                cacheArrays.add(option);
            }
        }
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME).put(ACacheHelper.CK_CUSTOM_FRONT_CATEGORY, cacheArrays.toJSONString());

        //设置下次不需要自动更新商品类目，可以在收银页面点击同步按钮修改
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_CUSTOM_FRONTCATEGORY_ENABLED, false);
    }

    public static class DataSyncEvent {
        public static final int EVENT_ID_REFRESH_FRONT_CATEGORYINFO         = 0X01;//刷新前台类目树
        public static final int EVENT_ID_REFRESH_FRONTEND_CATEGORYINFO_FRESH = 0X02;//刷新前台生鲜类目树
        public static final int EVENT_ID_REFRESH_BACKEND_CATEGORYINFO       = 0X03;//刷新后台类目树
        public static final int EVENT_ID_REFRESH_BACKEND_CATEGORYINFO_FRESH = 0X04;//刷新后台生鲜类目树
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
