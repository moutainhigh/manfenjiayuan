package com.mfh.litecashier.service;


import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.bingshanguxue.cashier.database.dao.PosProductNetDao;
import com.bingshanguxue.cashier.database.dao.PosProductSkuNetDao;
import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosLocalCategoryService;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.database.service.PosProductSkuService;
import com.bingshanguxue.cashier.database.service.ProductCatalogService;
import com.mfh.framework.api.scGoodsSku.PosGoods;
import com.bingshanguxue.cashier.model.ProductCatalog;
import com.bingshanguxue.cashier.model.ProductSkuBarcode;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.HostServer;
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
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.api.anon.sc.ProductCatalogApi;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.category.CateApiImpl;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.category.CategoryOption;
import com.mfh.framework.api.category.CategoryQueryInfo;
import com.mfh.framework.api.category.ScCategoryInfoApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.api.tenant.SassInfo;
import com.mfh.framework.api.tenant.TenantApi;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.bean.CompanyHuman;
import com.mfh.litecashier.database.logic.CompanyHumanService;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.AjaxParams;

import org.century.GreenTagsApi;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
public class DataDownloadManager {
    public static final int NA = 0;
    public static final int HUMAN_ABILITY = 1;//用户能力信息
    public static final int COMPANY_HUMAN = 2;//用户子账号信息
    public static final int POSPRODUCTS = 4;//商品档案
    public static final int POSPRODUCTS_SKU = 8;//一品多码
    public static final int FRONTENDCATEGORY = 16;//前台类目(一级类目)
    public static final int FRONTENDCATEGORY_GOODS = 32;//前台类目&商品库－关系表
    public static final int BACKENDCATEGORYINFO = 64;//后台类目树信息,部分按类目查询商品页面需要该数据
    public static final int TENANT_SAASINFO = 128;//租户信息


    public static final int LAUNCHER = 255;//应用启动
    public static final int MANUAL = 60;//手动点击同步

    private int queue = NA;//默认同步所有数据
    private int currentStep = NA;//当前步骤

    public static class DataDownloadEvent {
        public static final int EVENT_ID_SYNC_DATA_PROGRESS = 0X11;//同步进度
        public static final int EVENT_POSPRODUCTS_UPDATED = 0X03;//商品档案
        public static final int EVENT_FRONTEND_CATEGORY_UPDATED = 0X04;//前台类目更新
        public static final int EVENT_PRODUCT_CATALOG_UPDATED = 0X05;//前台类目和商品库关系更新
        public static final int EVENT_BACKEND_CATEGORYINFO_UPDATED = 0X06;//后台类目树更新
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
    private PageInfo mPosSkuPageInfo = new PageInfo(1, DataManagerHelper.MAX_SYNC_PAGESIZE);
    private PosProductSkuNetDao posProductSkuNetDao = new PosProductSkuNetDao();
    private PageInfo productCateslogPageInfo = new PageInfo(1, DataManagerHelper.MAX_SYNC_PAGESIZE);
    private PageInfo mCompanyHumanPageInfo = new PageInfo(1, DataManagerHelper.MAX_SYNC_PAGESIZE);


    private boolean bSyncInProgress = false;//是否正在同步

    //定时同步
    private static final int MSG_WHAT_SYNC_TIMER = 1;
    private Timer mTimer;//


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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SYNC_TIMER: {
                    ZLogger.df("定时任务激活：同步商品库");
//                    DataDownloadManager.get().syncProducts();
                }
                break;
            }

            // 要做的事情
            super.handleMessage(msg);
        }
    };

    public void startTimer() {
        cancelTimer();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = Message.obtain();
                message.what = MSG_WHAT_SYNC_TIMER;
                handler.sendMessage(message);
            }
        }, 10 * 1000, 8 * 60 * 60 * 1000);
        ZLogger.df("开启定时下载数据任务...");
    }

    public void cancelTimer() {
        ZLogger.df("取消定时下载数据任务...");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 同步数据
     */
    public void manualSync() {
        sync(MANUAL);
    }

    /**
     * 应用启动
     */
    public void launcherSync() {
        sync(LAUNCHER);
    }

    /**
     * 同步商品档案
     */
    public void syncProducts() {
        sync(DataDownloadManager.POSPRODUCTS | DataDownloadManager.POSPRODUCTS_SKU);
    }

    public void sync(int step) {
        queue |= step;

        if ((currentStep & step) == step) {
            ZLogger.df(String.format(Locale.US, "正在同步%d，跳过该步骤，并将该步骤加到队列中", step));
        } else {
            processQueue();
        }
    }

    /**
     * 同步
     */
    private void processQueue() {
        ZLogger.df(String.format(Locale.US, "当前同步队列: %d", queue));

        this.bSyncInProgress = true;

        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停下载数据");
            return;
        }

        if ((queue & TENANT_SAASINFO) == TENANT_SAASINFO) {
            getSaasInfo();
        } else if ((queue & HUMAN_ABILITY) == HUMAN_ABILITY) {
            queryPrivList();
        } else if ((queue & COMPANY_HUMAN) == COMPANY_HUMAN) {
            findCompUserPwdInfoStep1();
        } else if ((queue & POSPRODUCTS) == POSPRODUCTS) {
            downLoadPosProductStep1();
        } else if ((queue & POSPRODUCTS_SKU) == POSPRODUCTS_SKU) {
            findShopOtherBarcodesStep1();
        } else if ((queue & FRONTENDCATEGORY) == FRONTENDCATEGORY) {
            getTopFrontId();
        } else if ((queue & FRONTENDCATEGORY_GOODS) == FRONTENDCATEGORY_GOODS) {
            downLoadProductCatalog();
        } else if ((queue & BACKENDCATEGORYINFO) == BACKENDCATEGORYINFO) {
            listBackendCategoryStep1();
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
    private void onNotifyNext(String message) {
        if (!StringUtils.isEmpty(message)) {
            ZLogger.df(message);
        }
        processQueue();
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
                currentStep = POSPRODUCTS;
                queue ^= POSPRODUCTS;
                EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));
                EmbMsgService.getInstance().setAllRead(IMBizType.TENANT_SKU_UPDATE);

                String startCursor = DataManagerHelper.getPosLastUpdateCursor();
                if (StringUtils.isEmpty(startCursor)) {
                    ZLogger.df("商品档案同步游标为空，需要全量更新，假删除数据");
                    PosProductService.get().pretendDelete();
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
                        onNotifyNext(String.format("同步商品档案失败. %s", e.toString()));
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
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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

        ZLogger.df(String.format("同步商品档案开始(%d/%d/%s)",
                pageInfo.getPageNo(), pageInfo.getTotalPage(), lastCursor));
        posProductNetDao.query(params, new NetProcessor.QueryRsProcessor<PosGoods>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<PosGoods> rs) {
                downLoadPosProductStep3(rs, pageInfo, lastCursor);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                onNotifyNext(errMsg);
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
                            ZLogger.d("保存商品档案失败：商品参数无效。");
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
                    ZLogger.ef(String.format("保存商品档案失败: %s", ex.toString()));
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
                        onNotifyNext("保存商品档案失败.");
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
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"701"}}
                        int skuNum = 0;
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            skuNum = Integer.valueOf(retValue.getValue());
                        }

                        countNetSyncAbleSkuNumStep2(skuNum);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询指定网点可同步sku总数失败：" + errMsg);
//                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN,
//                                null, "Validate--会话过期，自动重登录");

                        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_POSPRODUCTS_UPDATED));

                        processQueue();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        ScGoodsSkuApiImpl.countNetSyncAbleSkuNum(responseCallback);
    }

    private void countNetSyncAbleSkuNumStep2(final int skuNum) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
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
                        SharedPreferencesUltimate.setSyncProductsCursor("");
                        //商品档案全量更新时也要重置寺冈电子秤和电子价签同步游标。
                        SharedPrefesManagerFactory.set(SMScaleSyncManager2.PREF_SMSCALE,
                                SMScaleSyncManager2.PK_S_SMSCALE_LASTCURSOR, "");
                        SharedPrefesManagerFactory.set(GreenTagsApi.PREF_GREENTAGS,
                                GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, "");
                    }
                } catch (Exception e) {
                    ZLogger.ef(String.format("查询指定网点可同步sku总数:%s", e.toString()));
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
                        ZLogger.ef(e.toString());
                        onNotifyCompleted("查询指定网点可同步sku总数失败.");
                    }

                    @Override
                    public void onNext(String startCursor) {
                        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_POSPRODUCTS_UPDATED));

                        processQueue();
                    }
                });
    }

    /**
     * 同步规格商品码表
     */
    private void findShopOtherBarcodesStep1() {
        currentStep = POSPRODUCTS_SKU;
        queue ^= POSPRODUCTS_SKU;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));

        String lastCursor = SharedPreferencesUltimate.getSyncProductSkuCursor();

        mPosSkuPageInfo = new PageInfo(-1, DataManagerHelper.MAX_SYNC_PAGESIZE);
        findShopOtherBarcodesStep2(lastCursor, mPosSkuPageInfo);
        mPosSkuPageInfo.setPageNo(1);
    }

    /**
     * 同步规格商品码表
     */
    private void findShopOtherBarcodesStep2(final String lastCursor, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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
                        SharedPreferencesUltimate.setPosSkuLastUpdate(cursor);
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
                ZLogger.df("同步规格码表品结束:" + SharedPreferencesUltimate.getSyncProductSkuCursor());

                processQueue();
            }
        }
    }

    private void getTopFrontId() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停查询pos前台类目.");
            return;
        }

        currentStep = FRONTENDCATEGORY;
        queue ^= FRONTENDCATEGORY;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));
        EmbMsgService.getInstance().setAllRead(IMBizType.FRONTCATEGORY_UPDATE);

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
            onNotifyCompleted("网络未连接，暂停创建前台类目.");
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

                        //删除无效的数据
                        PosLocalCategoryService.get().deleteBy(String.format("isCloudActive = '%d'",
                                PosLocalCategoryEntity.CLOUD_DEACTIVE));

                        int count = PosLocalCategoryService.get().getCount();
                        int cloudNum = items.size();
                        ZLogger.df(String.format("同步前台类目结束,云端类目(%d),本地类目数量(%d)",
                                cloudNum, count));

                        //通知刷新前台类目数据
                        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_FRONTEND_CATEGORY_UPDATED));

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
                currentStep = FRONTENDCATEGORY_GOODS;
                queue ^= FRONTENDCATEGORY_GOODS;
                EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));
                EmbMsgService.getInstance().setAllRead(IMBizType.FRONGCATEGORY_GOODS_UPDATE);

                String startCursor = DataManagerHelper.getProductCatalogStartCursor();
                if (StringUtils.isEmpty(startCursor)) {
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
                        onNotifyCompleted("同步商品和类目关系表失败.");
                    }

                    @Override
                    public void onNext(String startCursor) {
                        //从第一页开始请求，每页最多50条记录
                        productCateslogPageInfo = new PageInfo(-1, DataManagerHelper.MAX_SYNC_PAGESIZE);
                        downLoadProductCatalog2(startCursor, productCateslogPageInfo);
                        productCateslogPageInfo.setPageNo(1);
                    }

                });
    }

    private void downLoadProductCatalog2(final String startCusror, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步类目商品关系表.");
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
     * 查询指定网点可同步类目商品关系表
     * 商品库增量同步后检查pos本地商品数目和后台商品数目是否一致，如果不一致，则自动触发一次全量同步。
     */
    private void countProductCatalogSyncAbleNum() {
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"701"}}
                        try {
                            int skuNum = 0;
                            if (rspData != null) {
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

                            EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_PRODUCT_CATALOG_UPDATED));
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

    /**
     * 下载后台类目树
     */
    private void listBackendCategoryStep1() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停下载后台类目树.");
            return;
        }

        currentStep = BACKENDCATEGORYINFO;
        queue ^= BACKENDCATEGORYINFO;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));

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
                    processQueue();
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
                        listBackendCategoryStep2(categoryQueryInfo.getOptions());
                    } else {
                        listBackendCategoryStep2(null);
                    }
                }
            }
            , CategoryQueryInfo.class
            , CashierApp.getAppContext()) {
    };

    /**
     * 保存后台类目树
     */
    private void listBackendCategoryStep2(List<CategoryOption> options) {
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
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_BACKEND_CATEGORYINFO_UPDATED));

        processQueue();
    }

    /**
     * 获取能力信息
     */
    private void getSaasInfo() {
        currentStep = TENANT_SAASINFO;
        queue ^= TENANT_SAASINFO;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));

        HostServer hostServer = GlobalInstanceBase.getInstance().getHostServer();
        if (hostServer != null){
            TenantApi.getSaasInfo(hostServer.getSaasId(), responseCallback);
        }
        else{
            onNotifyNext("租户信息已损坏");
        }
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<SassInfo,
            NetProcessor.Processor<SassInfo>>(
            new NetProcessor.Processor<SassInfo>() {
                @Override
                public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","dat"}}
                    SassInfo sassInfo;
                    try {
                        if (rspData != null){
                            RspBean<SassInfo> retValue = (RspBean<SassInfo>) rspData;
                            sassInfo = retValue.getValue();

                            GlobalInstanceBase.getInstance().updateHostServer(sassInfo);
                        }
                    }
                    catch (Exception e){
                        ZLogger.ef(e.toString());
                    }
                    processQueue();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    onNotifyNext(errMsg);
                }
            }
            , SassInfo.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * 获取能力信息
     */
    private void queryPrivList() {
        currentStep = HUMAN_ABILITY;
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
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            moduleNames = retValue.getValue();
                        }
                        MfhLoginService.get().setModuleNames(moduleNames);
                    } catch (Exception ex) {
                        ZLogger.ef("parseUserProfile, " + ex.toString());
                    } finally {
                    }
                    processQueue();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    onNotifyNext(errMsg);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * 同步账号数据
     */
    private void findCompUserPwdInfoStep1() {
        currentStep = COMPANY_HUMAN;
        queue ^= COMPANY_HUMAN;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));

        mCompanyHumanPageInfo = new PageInfo(-1, DataManagerHelper.MAX_SYNC_PAGESIZE);
        findCompUserPwdInfoStep2(mCompanyHumanPageInfo);
        mCompanyHumanPageInfo.setPageNo(1);
    }

    private void findCompUserPwdInfoStep2(PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步账号数据.");
            return;
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<CompanyHuman>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<CompanyHuman> rs) {
                findCompUserPwdInfoStep3(rs, pageInfo);
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.df("加载自账号数据失败:" + errMsg);
                processQueue();
            }
        }, CompanyHuman.class, CashierApp.getAppContext());

        ZLogger.df(String.format("同步账号数据开始(%d/%d)", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        CompanyHumanApi.findCompUserPwdInfo(pageInfo, queryRsCallBack);
    }

    /**
     * 保存账号数据
     */
    private void findCompUserPwdInfoStep3(final RspQueryResult<CompanyHuman> rs, final PageInfo pageInfo) {
        if (rs == null) {
            processQueue();
            return;
        }

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    mCompanyHumanPageInfo = pageInfo;
                    //第一页，清空旧数据
                    if (mCompanyHumanPageInfo.getPageNo() == 1) {
                        ZLogger.df("清空旧账号数据");
                        CompanyHumanService.get().clear();
                    }

                    if (rs != null) {
                        int retSize = rs.getReturnNum();
                        ZLogger.df(String.format("保存 %d/%d 个账号数据", retSize, rs.getTotalNum()));
                        for (EntityWrapper<CompanyHuman> wrapper : rs.getRowDatas()) {
                            CompanyHumanService.get().saveOrUpdate(wrapper.getBean());
                        }
                    }
                } catch (Throwable ex) {
                    ZLogger.ef(String.format("保存账号数据失败: %s", ex.toString()));
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
                        onNotifyCompleted("保存账号数据失败.");
                    }

                    @Override
                    public void onNext(String s) {
                        //若还有继续发起请求
                        if (pageInfo.hasNextPage()) {
                            pageInfo.moveToNext();
                            findCompUserPwdInfoStep2(pageInfo);
                        } else {
                            ZLogger.df("同步账号数据结束");
                            processQueue();
                        }
                    }

                });
    }


}
