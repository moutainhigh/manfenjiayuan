package com.mfh.litecashier.service;


import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.dao.PosProductNetDao;
import com.bingshanguxue.cashier.database.dao.PosProductSkuNetDao;
import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosLocalCategoryService;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.database.service.PosProductSkuService;
import com.bingshanguxue.cashier.database.service.ProductCatalogService;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.CompanyHuman;
import com.mfh.framework.api.anon.sc.ProductCatalog;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.category.CategoryOption;
import com.mfh.framework.api.category.CategoryQueryInfo;
import com.mfh.framework.api.constant.Priv;
import com.mfh.framework.api.scGoodsSku.PosGoods;
import com.mfh.framework.api.scGoodsSku.ProductSkuBarcode;
import com.mfh.framework.api.tenant.SassInfo;
import com.mfh.framework.core.utils.ACache;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.ProductCatalogManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.http.ScGoodsSkuHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.database.logic.CompanyHumanService;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import net.tsz.afinal.core.AsyncTask;

import org.century.GreenTagsApi;
import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.mfh.framework.api.category.CateApi.CATEGORY_TENANT_ID;


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
        currentStep = NA;
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

        Map<String, String> options = new HashMap<>();
        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));//部门编号
        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));//公司编号
        options.put("startCursor", lastCursor);//游标
        if (pageInfo != null) {
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        ScGoodsSkuHttpManager.getInstance().downLoadPosProduct(options,
                new MQuerySubscriber<PosGoods>(pageInfo) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        onNotifyNext(e.toString());
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<PosGoods> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        downLoadPosProductStep3(dataList, pageInfo, lastCursor);
                    }
                });
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
                        onNotifyNext("保存商品档案失败." + e.toString());
                    }

                    @Override
                    public void onNext(String startCursor) {
                        //从第一页开始请求，每页最多50条记录
                        if (mPageInfo.hasNextPage()) {
                            mPageInfo.moveToNext();
                            downLoadPosProductStep2(startCursor, mPageInfo);
                        } else {
                            countNetSyncAbleSkuNum();
                        }
                    }

                });
    }

    private void downLoadPosProductStep3(final List<PosGoods> goodses, final PageInfo pageInfo,
                                         final String startCursor) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mPageInfo = pageInfo;

                if (mPageInfo != null) {
                    ZLogger.df(String.format("保存 (%d/%d) 个商品档案（%s）开始",
                            mPageInfo.getPageNo(),
                            mPageInfo.getTotalPage(), startCursor));
                }
                Date cussor = null;
                //使用事务
                if (goodses != null && goodses.size() > 0) {
                    for (PosGoods goods : goodses) {
                        //保存商品到数据库
                        if (goods == null || goods.getId() == null) {
                            ZLogger.d("保存商品档案失败：商品参数无效。");
                            continue;
                        }
                        ZLogger.df(String.format("保存商品档案: %s",
                                JSONObject.toJSONString(goods)));

                        PosProductService.get().saveOrUpdate(goods);
                        cussor = goods.getUpdatedDate();
                    }
                }

                //更新游标
                SharedPreferencesUltimate.setSyncProductsCursor(cussor);

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
                        if (pageInfo != null && pageInfo.hasNextPage()) {
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
        Map<String, String> options = new HashMap<>();
        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ScGoodsSkuHttpManager.getInstance().countNetSyncAbleSkuNum(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ZLogger.df("查询指定网点可同步sku总数失败：" + e.toString());
                        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_POSPRODUCTS_UPDATED));

                        processQueue();
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);

                        int skuNum = 0;
                        if (data != null) {
                            skuNum = Integer.valueOf(data);
                        }

                        countNetSyncAbleSkuNumStep2(skuNum);
                    }
                });
    }

    private void countNetSyncAbleSkuNumStep2(final int skuNum) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                ZLogger.df(String.format("网点可同步sku总数:%d", skuNum));

                //删除无效的数据
                PosProductService.get().deleteBy(String.format("isCloudActive = '%d'",
                        0));
                // 比较本地商品数据库总数是否和可以同步的SKU总数一致，
                // 如果不一致，则重置时间戳，下次触发全量同步，否则继续按照时间戳同步。
                List<PosProductEntity> entities = PosProductService.get()
                        .queryAllByDesc(String.format("tenantId = '%d'",
                                MfhLoginService.get().getSpid()));
                int posNum = (entities != null ? entities.size() : 0);
                ZLogger.df(String.format("本地商品档案库sku总数:%d", posNum));

                if (posNum != skuNum) {
                    ZLogger.df(String.format("本地商品档案和云端数据不一致，重置时间戳，" +
                            "下一次需要全量同步商品库", posNum, skuNum));

                    //初始化游标并设置下次需要全量更新
                    SharedPreferencesUltimate.setSyncProductsCursor("");
                    //商品档案全量更新时也要重置寺冈电子秤和电子价签同步游标。
                    SharedPrefesManagerFactory.set(SMScaleSyncManager2.PREF_SMSCALE,
                            SMScaleSyncManager2.PK_S_SMSCALE_LASTCURSOR, "");
                    SharedPrefesManagerFactory.set(GreenTagsApi.PREF_GREENTAGS,
                            GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, "");
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
                        onNotifyCompleted(String.format("查询指定网点可同步sku总数:%s", e.toString()));
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

        if (pageInfo != null) {
            ZLogger.df(String.format("同步规格码表开始(%d/%d)", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        }

        Map<String, String> options = new HashMap<>();
        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));//公司编号
        options.put("startCursor", lastCursor);//游标
        if (pageInfo != null) {
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        RxHttpManager.getInstance().findShopOtherBarcodes(options,
                new MQuerySubscriber<ProductSkuBarcode>(pageInfo) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        onNotifyNext(e.toString());
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<ProductSkuBarcode> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        findShopOtherBarcodesStep3(dataList, pageInfo, lastCursor);
                    }
                });
    }

    private void findShopOtherBarcodesStep3(final List<ProductSkuBarcode> goodses, final PageInfo pageInfo,
                                            final String startCursor) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mPosSkuPageInfo = pageInfo;

                if (mPosSkuPageInfo != null) {
                    ZLogger.df(String.format("保存 (%d/%d) 个箱规（%s）开始",
                            mPosSkuPageInfo.getPageNo(),
                            mPosSkuPageInfo.getTotalPage(), startCursor));
                }
                Date cursor = null;
                //使用事务
                if (goodses != null && goodses.size() > 0) {
                    for (ProductSkuBarcode goods : goodses) {

                        if (goods != null) {
                            PosProductSkuService.get().saveOrUpdate(goods);
                            cursor = goods.getCreatedDate();
                        }
                    }

                    //更新游标
                    if (cursor != null) {
                        SharedPreferencesUltimate.setPosSkuLastUpdate(cursor);
                    }
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
                        if (pageInfo != null && pageInfo.hasNextPage()) {
                            pageInfo.moveToNext();
                            findShopOtherBarcodesStep2(startCursor, pageInfo);
                        } else {
                            ZLogger.df("同步规格码表品结束:" + SharedPreferencesUltimate.getSyncProductSkuCursor());

                            processQueue();
                        }
                    }

                });
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

        Map<String, String> options = new HashMap<>();
        options.put("cateType", String.valueOf(CateApi.POS));
        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        RxHttpManager.getInstance().getTopFrontId(options, new Subscriber<CategoryInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                onNotifyNext("加载前台根类目失败, " + e.toString());
            }

            @Override
            public void onNext(CategoryInfo categoryInfo) {
                getCodeValue(categoryInfo);
            }
        });
    }

    /**
     * 创建前台类目
     */
    private void createCategoryInfo() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停创建前台类目.");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("kind", "code");
        jsonObject.put("domain", String.valueOf(CateApi.DOMAIN_TYPE_PROD));
        jsonObject.put("nameCn", "POS前台类目");
        jsonObject.put("catePosition", String.valueOf(CateApi.CATE_POSITION_FRONT));
        jsonObject.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        jsonObject.put("cateType", String.valueOf(CateApi.POS));

        RxHttpManager.getInstance().create(MfhLoginService.get().getCurrentSessionId(),
                jsonObject, new MValueSubscriber<String>() {
                    @Override
                    public void onError(Throwable e) {
                        onNotifyCompleted("新建前台类目 失败, " + e.toString());
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        try {
                            if (data != null) {
                                Long code = Long.valueOf(data);
                                ZLogger.df("新建前台类目成功:" + data);
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

                });
    }

    /**
     * 加载前台类目树
     */
    private void getCodeValue(CategoryInfo categoryInfo) {
        if (categoryInfo == null) {
            createCategoryInfo();
            return;
        }
        SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_L_CATETYPE_POS_ID, categoryInfo.getId());
        PosLocalCategoryService.get().deactiveAll();

        Map<String, String> options = new HashMap<>();
        options.put("parentId", String.valueOf(categoryInfo.getId()));
        RxHttpManager.getInstance().getCodeValue(options, new Subscriber<List<CategoryInfo>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                onNotifyCompleted("加载前台类目树 失败, " + e.toString());
            }

            @Override
            public void onNext(List<CategoryInfo> categoryInfos) {
                if (categoryInfos != null) {
                    for (CategoryInfo item : categoryInfos) {
                        PosLocalCategoryService.get().saveOrUpdate(item);
                    }
                    //删除无效的数据
                    PosLocalCategoryService.get().deleteBy(String.format("isCloudActive = '%d'",
                            PosLocalCategoryEntity.CLOUD_DEACTIVE));

                    int count = PosLocalCategoryService.get().getCount();
                    int cloudNum = categoryInfos.size();
                    ZLogger.df(String.format("同步前台类目结束,云端类目(%d),本地类目数量(%d)",
                            cloudNum, count));
                }

                //通知刷新前台类目数据
                EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_FRONTEND_CATEGORY_UPDATED));

                processQueue();
            }
        });
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

    private void downLoadProductCatalog2(final String startCursor, PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步类目商品关系表.");
            return;
        }

        Map<String, String> optionss = new HashMap<>();
        if (!StringUtils.isEmpty(startCursor)) {
            optionss.put("startCursor", startCursor);
        }
        if (pageInfo != null) {
            optionss.put("page", Integer.toString(pageInfo.getPageNo()));
            optionss.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        optionss.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ProductCatalogManager.getInstance().downLoadProductCatalog(optionss,
                new MQuerySubscriber<ProductCatalog>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<ProductCatalog> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        downLoadProductCatalog3(dataList, pageInfo, startCursor);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ZLogger.df("同步商品和类目关系表失败:" + e.toString());
                        processQueue();
                    }
                });
    }

    private void downLoadProductCatalog3(final List<ProductCatalog> catalogs, final PageInfo pageInfo,
                                         final String startCursor) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                productCateslogPageInfo = pageInfo;

                if (productCateslogPageInfo != null) {
                    ZLogger.df(String.format("保存 (%d/%d) 个商品类目关系表（%s）开始",
                            productCateslogPageInfo.getPageNo(),
                            productCateslogPageInfo.getTotalPage(), startCursor));
                }
                Date cussor = null;
                //使用事务
                if (catalogs != null && catalogs.size() > 0) {
                    for (ProductCatalog catalog : catalogs) {
                        //保存商品到数据库
                        if (catalog == null || catalog.getId() == null) {
                            ZLogger.d("保存商品类目关系失败：商品类目关系无效。");
                            continue;
                        }
                        ZLogger.df(String.format("保存商品类目关系: %s",
                                JSONObject.toJSONString(catalog)));
                        ProductCatalogService.getInstance().saveOrUpdate(catalog);

                        cussor = catalog.getUpdatedDate();
                    }
                }

                //更新游标
                SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_SYNC_PRODUCTCATALOG_STARTCURSOR,
                        TimeUtil.format(cussor, TimeUtil.FORMAT_YYYYMMDDHHMMSS));

                ZLogger.df(String.format("保存商品类目关系表结束（%s）",
                        SharedPreferencesUltimate.getText(SharedPreferencesUltimate.PK_SYNC_PRODUCTCATALOG_STARTCURSOR)));

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
                        //若还有继续发起请求
                        if (pageInfo.hasNextPage()) {
                            pageInfo.moveToNext();
                            downLoadProductCatalog2(startCursor, pageInfo);
                        } else {
                            countProductCatalogSyncAbleNum();
                        }
                    }

                });
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
        Map<String, String> optionss = new HashMap<>();
        optionss.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ProductCatalogManager.getInstance().countProductCatalogSyncAbleNum(optionss,
                new MValueSubscriber<String>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        onNotifyNext("计算有多少可同步的商品类目关系失败：" + e.toString());
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        try {
                            int skuNum = 0;
                            if (data != null) {
                                skuNum = Integer.valueOf(data);
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
                });
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
        Map<String, String> options = new HashMap<>();
        options.put("kind", "code");
        options.put("domain", String.valueOf(CateApi.DOMAIN_TYPE_PROD));
        options.put("cateType", "");
        options.put("catePosition", String.valueOf(CateApi.CATE_POSITION_BACKEND));
        options.put("deep", "2");//层级
//        params.put("tenantId", MfhLoginService.get().getSpid() == null ? "0" : String.valueOf(MfhLoginService.get().getSpid()));
        options.put("tenantId", CATEGORY_TENANT_ID);//使用类目专属ID
        RxHttpManager.getInstance().comnQuery(options, new Subscriber<CategoryQueryInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.df("加载后台类目树失败, " + e.toString());
                processQueue();
            }

            @Override
            public void onNext(CategoryQueryInfo categoryQueryInfo) {
                if (categoryQueryInfo != null) {
                    //缓存数据
                    listBackendCategoryStep2(categoryQueryInfo.getOptions());
                } else {
                    listBackendCategoryStep2(null);
                }
            }
        });
    }

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
        if (hostServer != null) {
            RxHttpManager.getInstance().getSaasInfo(hostServer.getSaasId(),
                    new Subscriber<SassInfo>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            onNotifyNext(e.toString());
                        }

                        @Override
                        public void onNext(SassInfo sassInfo) {
                            GlobalInstanceBase.getInstance().updateHostServer(sassInfo);
                            onNotifyNext("下载租户信息成功");
                        }

                    });
        } else {
            onNotifyNext("租户信息已损坏");
        }
    }

    /**
     * 获取能力信息
     */
    private void queryPrivList() {
        currentStep = HUMAN_ABILITY;
        queue ^= HUMAN_ABILITY;
        EventBus.getDefault().post(new DataDownloadEvent(DataDownloadEvent.EVENT_ID_SYNC_DATA_PROGRESS));

        RxHttpManager.getInstance().queryPrivList(MfhLoginService.get().getCurrentSessionId(),
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onNotifyNext(e.toString());
                    }

                    @Override
                    public void onNext(String s) {
                        MfhLoginService.get().setModuleNames(s);
                        MfhUserManager.getInstance().updateModules();
                        if (MfhUserManager.getInstance().containsModule(Priv.FUNC_SUPPORT_BUY)) {
                            ZLogger.d("当前登录用户具有买手能力");
                        } else {
                            ZLogger.d("当前登录用户不具有买手能力");
                        }
                        processQueue();
                    }
                });
    }

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

    private void findCompUserPwdInfoStep2(final PageInfo pageInfo) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步账号数据.");
            return;
        }

        if (pageInfo != null) {
            ZLogger.df(String.format("同步账号数据开始(%d/%d)", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        } else {
            ZLogger.df("同步账号数据开始");
        }

        Map<String, String> options = new HashMap<>();
        options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null) {
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());

        RxHttpManager.getInstance().findCompUserPwdInfo(options,
                new MQuerySubscriber<CompanyHuman>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<CompanyHuman> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        findCompUserPwdInfoStep3(dataList, pageInfo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.df("加载自账号数据失败:" + e.toString());
                        processQueue();
                    }
                });
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
                    if (mCompanyHumanPageInfo != null && mCompanyHumanPageInfo.getPageNo() == 1) {
                        ZLogger.df("清空旧账号数据");
                        CompanyHumanService.get().clear();
                    }

                    int retSize = rs.getReturnNum();
                    ZLogger.df(String.format("保存 %d/%d 个账号数据", retSize, rs.getTotalNum()));
                    for (EntityWrapper<CompanyHuman> wrapper : rs.getRowDatas()) {
                        CompanyHumanService.get().saveOrUpdate(wrapper.getBean());
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

    private void findCompUserPwdInfoStep3(final List<CompanyHuman> companyHumen, final PageInfo pageInfo) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mCompanyHumanPageInfo = pageInfo;
                //第一页，清空旧数据
                if (mCompanyHumanPageInfo != null && mCompanyHumanPageInfo.getPageNo() == 1) {
                    ZLogger.df("清空旧账号数据");
                    CompanyHumanService.get().clear();
                }

//                    int retSize = rs.getReturnNum();
                if (companyHumen != null && companyHumen.size() > 0) {
                    ZLogger.df(String.format("保存 %d 个账号数据", companyHumen.size()));

                    for (CompanyHuman wrapper : companyHumen) {
                        CompanyHumanService.get().saveOrUpdate(wrapper);
                    }
                } else {
                    ZLogger.df("保存 0 个账号数据");
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
