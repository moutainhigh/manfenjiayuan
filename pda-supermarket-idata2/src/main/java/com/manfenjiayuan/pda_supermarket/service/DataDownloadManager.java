package com.manfenjiayuan.pda_supermarket.service;


import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.DataSyncManager;
import com.bingshanguxue.pda.utils.SharedPrefesManagerUltimate;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.cashier.database.entity.PosProductEntity;
import com.manfenjiayuan.pda_supermarket.cashier.database.service.PosProductService;
import com.manfenjiayuan.pda_supermarket.cashier.database.service.PosProductSkuService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.Priv;
import com.mfh.framework.api.scGoodsSku.PosGoods;
import com.mfh.framework.api.scGoodsSku.ProductSkuBarcode;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.http.ScGoodsSkuHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private PageInfo mPosSkuPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

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

        Map<String, String> options = new HashMap<>();
        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));//部门编号
        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));//公司编号
        options.put("startCursor", lastCursor);//游标
        if (pageInfo != null) {
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
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
                //TODO 使用事务
                if (goodses != null && goodses.size() > 0) {
                    for (PosGoods goods : goodses) {
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
                SharedPrefesManagerUltimate.setSyncProductsStartCursor(cussor);

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
        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停查询指定网点可同步sku总数");
            return;
        }

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

                        countNetSyncAbleSkuNumStep2(data);
                    }
                });
    }

    private void countNetSyncAbleSkuNumStep2(final String data) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                ZLogger.df(String.format("网点可同步sku总数:%s", data));

                int skuNum = 0;
                if (data != null) {
                    skuNum = Integer.valueOf(data);
                }

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
                    SharedPrefesManagerUltimate.setSyncProductsStartcursor("");
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
                new MQuerySubscriber<com.mfh.framework.api.scGoodsSku.ProductSkuBarcode>(pageInfo) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        onNotifyNext(e.toString());
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<com.mfh.framework.api.scGoodsSku.ProductSkuBarcode> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        findShopOtherBarcodesStep3(dataList, pageInfo, lastCursor);
                    }
                });
    }

    private void findShopOtherBarcodesStep3(final List<com.mfh.framework.api.scGoodsSku.ProductSkuBarcode> goodses, final PageInfo pageInfo,
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
                        SharedPrefesManagerUltimate.setSyncProductSkuStartcursor(cursor);
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
                        if (pageInfo != null && pageInfo.hasNextPage()) {
                            pageInfo.moveToNext();
                            findShopOtherBarcodesStep2(startCursor, pageInfo);
                        } else {
                            onNotifyNext("同步规格码表品结束:" + SharedPrefesManagerUltimate.getSyncProductSkuCursor());
                        }
                    }

                });
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
                        ZLogger.d("moduleNames>> " + s);
                        MfhLoginService.get().setModuleNames(s);
                        MfhUserManager.getInstance().updateModules();
                        if (MfhUserManager.getInstance().containsModule(Priv.FUNC_SUPPORT_BUY)) {
                            ZLogger.d("当前登录用户具有买手能力");
                        } else {
                            ZLogger.d("当前登录用户不具有买手能力");
                        }
                        //todo
                        processQueue();
                    }
                });
    }

}
