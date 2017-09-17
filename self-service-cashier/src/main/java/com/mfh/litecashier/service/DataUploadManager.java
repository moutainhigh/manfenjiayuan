package com.mfh.litecashier.service;


import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.model.PayStatus;
import com.bingshanguxue.cashier.model.SyncStatus;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.entity.PosTopupEntity;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.database.service.PosTopupService;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.CashierProvider;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.analysis.AnalysisApiImpl;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.posorder.BatchInOrder;
import com.mfh.framework.api.posorder.BatchInOrderItem;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rx.Subscriber;


/**
 * 上传数据，确保POS机数据能正确的上传到云端
 * <ol>
 * <li>多台POS机如果同时有大量数据提交到后台，可能会影响性能，所以订单同步不需要要实时同步</li>
 * <li>定时任务每隔10分钟同步一次。{@link TimeTaskManager#syncPosOrderTimer}</li>
 * <li>如果没有订单需要同步，则取消定时器</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DataUploadManager {
    public static final int NA = 0;
    public static final int INCOME_DISTRIBUTION_TOPUP = 1;//清分充值
    public static final int CASH_QUOTA_TOPUP = 2;//现金授权充值
    public static final int POS_ORDER = 4;//收银订单／外部订单

    private int rollback = -1;
    private static final int MAX_ROLLBACK = 5;
    private boolean bSyncInProgress = false;//是否正在同步
    private int queue = NA;//默认同步所有数据

    private PageInfo incomeDistributionPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, 1);//翻页
    private PageInfo commitCashPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, 1);//翻页
    public static final int MAX_SYNC_ORDER_PAGESIZE = 4;
    protected PageInfo mOrderPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_ORDER_PAGESIZE);//翻页
    protected String orderSqlWhere;

    /**
     * 定时同步订单
     */
    private static final int MSG_WHAT_SYNC_POSORDER = 1;
    private static Timer syncPosOrderTimer = new Timer();

    private static DataUploadManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static DataUploadManager getInstance() {
        if (instance == null) {
            synchronized (DataUploadManager.class) {
                if (instance == null) {
                    instance = new DataUploadManager();
                }
            }
        }
        return instance;
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SYNC_POSORDER: {
                    ZLogger.d("定时任务激活：上传收银订单");
                    DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
                }
                break;
            }

            // 要做的事情
             super.handleMessage(msg);
        }
    };

    /**
     * 定时同步POS订单
     */
    public void startTimer() {
        cancelTimer();
        ZLogger.d("定时任务开启...");
        if (syncPosOrderTimer == null) {
            syncPosOrderTimer = new Timer();
        }

        syncPosOrderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = MSG_WHAT_SYNC_POSORDER;
                handler.sendMessage(message);
            }
        }, 10 * 1000, 10 * 60 * 1000);
    }

    private void cancelTimer() {
        ZLogger.d("取消定时任务...");
        if (syncPosOrderTimer != null) {
            syncPosOrderTimer.cancel();
        }
        syncPosOrderTimer = null;
    }


    /**
     * 下载更新POS数据库
     */
    public synchronized void syncDefault() {
        sync(7);
    }

    public void sync(int step) {
        queue |= step;
        if (bSyncInProgress) {
            rollback++;
            ZLogger.i(String.format("正在同步POS数据..., rollback=%d/%d", rollback, MAX_ROLLBACK));
            EventBus.getDefault().post(new UploadSyncManagerEvent(UploadSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));

            //自动恢复同步
            if (rollback >= MAX_ROLLBACK) {
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

        if ((queue & INCOME_DISTRIBUTION_TOPUP) == INCOME_DISTRIBUTION_TOPUP) {
            uploadIncomeDistribution();
        } else if ((queue & CASH_QUOTA_TOPUP) == CASH_QUOTA_TOPUP) {
            uploadCashQuota();
        } else if ((queue & POS_ORDER) == POS_ORDER) {
            uploadPosOrders();
        } else {
            onNotifyCompleted("没有上传任务待执行");
        }
    }


    private void onNotifyNext(String message) {
        if (!StringUtils.isEmpty(message)) {
            ZLogger.d(message);
        }
        processQueue();
    }

    /**
     * 完成
     */
    private void onNotifyCompleted(String message) {
        if (!StringUtils.isEmpty(message)) {
            ZLogger.d(message);
        }
        bSyncInProgress = false;
        EventBus.getDefault().post(new UploadSyncManagerEvent(UploadSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    public static class UploadSyncManagerEvent {
        public static final int EVENT_ID_SYNC_DATA_PROGRESS = 0X11;//同步进度
        public static final int EVENT_ID_SYNC_DATA_ERROR = 0X12;//同步失败
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X13;//同步结束

        private int eventId;

        public UploadSyncManagerEvent(int eventId) {
            this.eventId = eventId;
        }

        public int getEventId() {
            return eventId;
        }
    }


    /**
     * 提交清分充值记录
     */
    private void uploadIncomeDistribution() {
        queue ^= INCOME_DISTRIBUTION_TOPUP;

        incomeDistributionPageInfo = new PageInfo(1, 1);//翻页

        commintCashAndTrigDateEnd();
    }

    /**
     * 提交营业现金，并触发一次日结操作
     */
    private void commintCashAndTrigDateEnd() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步清分充值支付记录。");
            return;
        }

        String sqlWhere = String.format("bizType = '%d' and subBizType = '%d' " +
                        "and paystatus = '%d' and syncStatus = '%d'",
                BizType.DAILYSETTLE, BizType.INCOME_DISTRIBUTION,
                PayStatus.FINISH, SyncStatus.INIT);
        List<PosTopupEntity> entities = PosTopupService.get().queryAll(sqlWhere, incomeDistributionPageInfo);
        if (entities == null || entities.size() <= 0) {
            onNotifyNext("没有清分充值支付记录需要上传");
            return;
        }

        final PosTopupEntity topupEntity = entities.get(0);
        ZLogger.d(String.format("提交清分充值支付记录:%s (%d/%d %d)",
                topupEntity.getOutTradeNo(), incomeDistributionPageInfo.getPageNo(),
                incomeDistributionPageInfo.getTotalPage(), incomeDistributionPageInfo.getTotalCount()));

        if (RxHttpManager.isUseRx) {
            RxHttpManager.getInstance().commintCashAndTrigDateEnd(topupEntity.getOutTradeNo(),
                    new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            ZLogger.e("提交清分充值支付记录失败：" + e.toString());
                            if (incomeDistributionPageInfo.hasNextPage()) {
                                incomeDistributionPageInfo.moveToNext();
                                commintCashAndTrigDateEnd();
                            } else {
                                processQueue();
                            }
                        }

                        @Override
                        public void onNext(String s) {
                            ZLogger.d("提交清分充值支付记录成功:" + s);
                            topupEntity.setSyncStatus(SyncStatus.SYNCED);
                            PosTopupService.get().saveOrUpdate(topupEntity);

                            //继续上传订单
                            if (incomeDistributionPageInfo.hasNextPage()) {
                                incomeDistributionPageInfo.moveToNext();
                                commintCashAndTrigDateEnd();
                            } else {
                                processQueue();
                            }
                        }
                    });
        } else {
            NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        public void processResult(IResponseData rspData) {
                            //{"code":"0","msg":"操作成功!","version":"1","data":false}
                            //java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Boolean
                            //RspValue<Boolean> retValue = (RspValue<Boolean>) rspData;
                            if (rspData != null) {
                                RspValue<String> retValue = (RspValue<String>) rspData;
                                ZLogger.d("提交清分充值支付记录成功:" + retValue.getValue());
                                topupEntity.setSyncStatus(SyncStatus.SYNCED);
                                PosTopupService.get().saveOrUpdate(topupEntity);

                                //继续上传订单
                                if (incomeDistributionPageInfo.hasNextPage()) {
                                    incomeDistributionPageInfo.moveToNext();
                                    commintCashAndTrigDateEnd();
                                } else {
                                    processQueue();
                                }
                            } else {
                                processQueue();
                            }
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            ZLogger.e("提交清分充值支付记录失败：" + errMsg);
                            if (incomeDistributionPageInfo.hasNextPage()) {
                                incomeDistributionPageInfo.moveToNext();
                                commintCashAndTrigDateEnd();
                            } else {
                                processQueue();
                            }
                        }
                    }
                    , String.class
                    , CashierApp.getAppContext()) {
            };

            AnalysisApiImpl.commintCashAndTrigDateEnd(topupEntity.getOutTradeNo(), responseRC);
        }
    }

    private void uploadCashQuota() {
        queue ^= CASH_QUOTA_TOPUP;
        commitCashPageInfo = new PageInfo(1, 1);//翻页

        commintCash();
    }

    /**
     * 提交营业现金
     */
    private void commintCash() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步营业额现金支付记录。");
            return;
        }

        String sqlWhere = String.format("bizType = '%d' and subBizType = '%d' and paystatus = '%d' and syncStatus = '%d'",
                BizType.DAILYSETTLE, BizType.CASH_QUOTA, PayStatus.FINISH, SyncStatus.INIT);


        List<PosTopupEntity> entities = PosTopupService.get().queryAll(sqlWhere, commitCashPageInfo);
        if (entities == null || entities.size() <= 0) {
            onNotifyNext("没有现金授权支付记录需要上传");
            return;
        }

        final PosTopupEntity topupEntity = entities.get(0);
        ZLogger.d(String.format("提交现金授权支付记录:%s (%d/%d %d)",
                topupEntity.getOutTradeNo(), commitCashPageInfo.getPageNo(),
                commitCashPageInfo.getTotalPage(), commitCashPageInfo.getTotalCount()));

        NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":false}
                        //java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Boolean
                        //RspValue<Boolean> retValue = (RspValue<Boolean>) rspData;
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        ZLogger.d("提交现金授权支付记录成功:" + retValue.getValue());
                        topupEntity.setSyncStatus(SyncStatus.SYNCED);
                        PosTopupService.get().saveOrUpdate(topupEntity);

                        //继续上传订单
                        if (commitCashPageInfo.hasNextPage()) {
                            commitCashPageInfo.moveToNext();
                            commintCash();
                        } else {
                            processQueue();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        {"code":"1","msg":"未找到支付交易号:2016-08-02","data":null,"version":1}
                        ZLogger.ef("提交现金授权支付记录失败：" + errMsg);
                        //提交失败，仍继续上传订单
                        if (commitCashPageInfo.hasNextPage()) {
                            commitCashPageInfo.moveToNext();
                            commintCash();
                        } else {
                            processQueue();
                        }
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        AnalysisApiImpl.commintCash(topupEntity.getOutTradeNo(), responseRC);
    }

    /**
     * 上传未同步的已完成订单
     * */
    public void uploadPosOrders(){
        queue ^= POS_ORDER;

        List<PosOrderEntity> orderEntities = DataManagerHelper.getSyncPosOrders(PosOrderEntity.SYNC_STATUS_NONE);
        if (orderEntities != null && orderEntities.size() > 0) {
            stepUploadPosOrder(orderEntities.get(0));
        } else {
            ZLogger.i("没有找到未同步的已完成订单");
            uploadErrorPosOrders(true);
        }
    }

    /**
     * 上传POS订单
     * 根据上一次同步游标同步订单数据，上传同步失败的订单，相当于进行一次重试操作。
     */
    public synchronized void uploadErrorPosOrders(boolean isReload) {
        if (isReload) {
            mOrderPageInfo = new PageInfo(1, MAX_SYNC_ORDER_PAGESIZE);
            String orderStartCursor = DataManagerHelper.getPosOrderStartCursor();
            //上传未同步并且已完成的订单
            orderSqlWhere = String.format("updatedDate >= '%s' and sellerId = '%d' " +
                            "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                    orderStartCursor, MfhLoginService.get().getSpid(),
                    PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                    PosOrderEntity.SYNC_STATUS_ERROR);
        }

        List<PosOrderEntity> orderEntities = PosOrderService.get()
                .queryAllAsc(orderSqlWhere, mOrderPageInfo);
        ZLogger.d(String.format("查询到 %d 个同步失败的POS订单，" +
                        "当前页数 %d/%d,每页最多 %d 个订单",
                mOrderPageInfo.getTotalCount(), mOrderPageInfo.getPageNo(),
                mOrderPageInfo.getTotalPage(), mOrderPageInfo.getPageSize()));

        if (orderEntities != null && orderEntities.size() > 0) {
            uploadErrorOrder(orderEntities.get(0));
        } else {
            onNotifyNext("没有找到同步失败的已完成订单");
        }
    }


    private BatchInOrder wrapper2(PosOrderEntity orderEntity) {
        ZLogger.d(String.format("准备同步订单 : %s", JSONObject.toJSONString(orderEntity)));
        BatchInOrder order = new BatchInOrder();

        order.setId(orderEntity.getId());//pos机订单编号
        order.setBarCode(orderEntity.getBarCode());
        order.setStatus(orderEntity.getStatus());
        order.setRemark(orderEntity.getRemark());//备注
        order.setBcount(orderEntity.getBcount());//数量
        order.setAdjPrice(MathCompact.sub(orderEntity.getRetailAmount(), orderEntity.getFinalAmount())); //调价金额
        order.setPaystatus(orderEntity.getPaystatus());
        order.setSubType(orderEntity.getSubType());//业务子类型
        order.setOuterNo(orderEntity.getOuterTradeNo());//外部订单编号（外部平台订单组货功能特有）
        order.setPosId(orderEntity.getPosId());//机器编号
        order.setSellOffice(orderEntity.getSellOffice());//curoffice id
        order.setSellerId(orderEntity.getSellerId());//spid
        order.setHumanId(orderEntity.getHumanId());//会员编号
        //由后台计算折扣
//        if (orderEntity.getRetailAmount() == 0D) {
//            order.put("discount", Double.valueOf(String.valueOf(Integer.MAX_VALUE)));
//        } else {
//            order.put("discount", (orderEntity.getRetailAmount() - orderEntity.getDiscountAmount())
//                    / orderEntity.getRetailAmount());
//        }

        //使用订单最后更新日期作为订单生效日期
        Date createdDate = orderEntity.getUpdatedDate();
        if (createdDate == null) {
            createdDate = orderEntity.getCreatedDate();
        }
        order.setCreatedDate(TimeUtil.format(createdDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        order.setCreatedBy(orderEntity.getCreatedBy());

        //读取订单商品明细
        List<PosOrderItemEntity> orderItemEntities = CashierProvider.fetchOrderItems(orderEntity.getId());
        List<BatchInOrderItem> items = new ArrayList<>();
        for (PosOrderItemEntity entity : orderItemEntities) {
            BatchInOrderItem item = new BatchInOrderItem();
            item.setGoodsId(entity.getGoodsId());
            item.setProductId(entity.getProductId());
            item.setSkuId(entity.getProSkuId());
            item.setBarcode(entity.getBarcode());
            item.setBcount(entity.getBcount());
            item.setPrice(entity.getCostPrice());//原价（零售价）
            item.setCustomerPrice(entity.getFinalCustomerPrice());// 会员价（服务端备存）
            item.setAmount(entity.getAmount());//商品预设的原始价格
            item.setFactAmount(entity.getFinalAmount());//订单明细的实际折后销售价格，商品本次销售原价金额(例如抹零)
            //// TODO: 19/04/2017
            //saleAmount，根据ruleAmountMap去计算
            item.setSaleAmount(MathCompact.sub(entity.getFinalAmount(), entity.getVipAmount()));//实际销售金额(扣除了会员优惠后)
            //该条订单明细流水具体的会员折扣规则优惠情况，可能会有多条会员折扣规则适用，其中key是规则id，value是该规则的产生的优惠金额
            String ruleAmountMap = entity.getRuleAmountMap();
            if (ruleAmountMap != null) {
                item.setRuleAmountMap(JSONObject.parse(ruleAmountMap));
            }
//            item.put("cateType", entity.getCateType());//按类目进行账务清分
            item.setProdLineId(entity.getProdLineId());//按产品线进行账务清分
            items.add(item);
        }
        order.setItems(items);
        String rpDisAmountMap = orderEntity.getRpDisAmountMap();
        if (rpDisAmountMap != null) {
            order.setRpDisAmountMap(JSONObject.parse(rpDisAmountMap));
        }

        //2016-07-01 上传订单支付记录到后台
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        if (payWrapper != null) {
            //注意这里上传的支付记录不包括现金找零和会员账户余额
            order.setPayWays(payWrapper.getUploadPayWays());
            //优惠金额（促销规则+卡券）
            Double disAmount = payWrapper.getVipDiscount() + payWrapper.getPromotionDiscount() + payWrapper.getCouponDiscount();
            order.setDisAmount(disAmount);
            //卡券核销
            order.setCouponsIds(payWrapper.getCouponsIds());
            order.setRuleIds(payWrapper.getRuleIds());
            order.setPayType(payWrapper.getPayType());//支付方式
            Double amount = orderEntity.getFinalAmount() - disAmount;
            order.setAmount(amount);//订单金额，负数表示退单
            if (amount >= 0.01) {
                order.setScore(amount / 2);
            } else {
                order.setScore(0D);
            }
        } else {
            order.setAmount(orderEntity.getFinalAmount());//实际支付金额
        }

        return order;
    }

    /**
     * 批量上传POS订单
     */
    private void batchUploadPosOrder(List<PosOrderEntity> orderEntities) {
        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步POS订单数据。");
            return;
        }

        if (orderEntities == null || orderEntities.size() < 1) {
            onNotifyNext("没有POS订单需要上传");
            return;
        }

        Date newCursor = null;
//        List<BatchInOrder> orders = new ArrayList<>();
        JSONArray orders = new JSONArray();
        for (PosOrderEntity orderEntity : orderEntities) {
            //保存最大时间游标
            if (newCursor == null || orderEntity.getUpdatedDate() == null
                    || newCursor.compareTo(orderEntity.getUpdatedDate()) <= 0) {
                newCursor = orderEntity.getUpdatedDate();
            }

            orders.add(CashierProvider.wrapperUploadOrder(orderEntity));
        }

        final Date finalNewCursor = newCursor;
        RxHttpManager.getInstance().batchInOrders(MfhLoginService.get().getCurrentSessionId(),
                orders,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef(e.toString());
                        onNotifyNext(String.format("上传POS订单失败: %s", e.getMessage()));
                    }

                    @Override
                    public void onNext(String s) {
                        SharedPreferencesUltimate.setPosOrderLastUpdate(finalNewCursor);
                        if (mOrderPageInfo.hasNextPage()) {
                            mOrderPageInfo.moveToNext();
                            uploadErrorPosOrders(false);
                        } else {
                            onNotifyNext(String.format("上传POS订单数据完成。%s",
                                    SharedPreferencesUltimate.getPosOrderLastUpdate()
                            ));
                        }
                    }
                });
    }

    /**
     * 提交单条订单
     * 适用场景：订单完成后立即上传，上传成功只会影响订单同步状态，不会影响同步游标
     */
    public void stepUploadPosOrder(final PosOrderEntity orderEntity) {
        if (orderEntity == null) {
            onNotifyNext("订单无效，不需要同步...");
            return;
        }
        if (orderEntity.getStatus() != PosOrderEntity.ORDER_STATUS_FINISH) {
            onNotifyNext(String.format("订单未完成(%d)，不需要同步...", orderEntity.getStatus()));
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步POS订单数据。");
            return;
        }

//        List<BatchInOrder> orders = new ArrayList<>();
//        orders.add(wrapper2(orderEntity));
        JSONArray orders = new JSONArray();
        orders.add(CashierProvider.wrapperUploadOrder(orderEntity));

//            Map<String, String> options = new HashMap<>();
//            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//            options.put("jsonStr", orders.toJSONString());
        RxHttpManager.getInstance().batchInOrders(MfhLoginService.get().getCurrentSessionId(),
                orders,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef(e.toString());
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_ERROR);
//                        orderEntity.setUpdatedDate(new Date());
                        PosOrderService.get().saveOrUpdate(orderEntity);
                        uploadPosOrders();
                    }

                    @Override
                    public void onNext(String s) {
                        //修改订单同步状态
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_SYNCED);
//                        orderEntity.setUpdatedDate(new Date());
                        PosOrderService.get().saveOrUpdate(orderEntity);

                        uploadPosOrders();
                    }
                });
    }

    /**同步单个异常订单，同步完成后的流程和正常订单的流程不一致*/
    public void uploadErrorOrder(final PosOrderEntity orderEntity) {
        if (orderEntity == null) {
            onNotifyNext("订单无效，不需要同步...");
            return;
        }
        if (orderEntity.getStatus() != PosOrderEntity.ORDER_STATUS_FINISH) {
            onNotifyNext(String.format("订单未完成(%d)，不需要同步...", orderEntity.getStatus()));
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步POS订单数据。");
            return;
        }

        JSONArray orders = new JSONArray();
        orders.add(CashierProvider.wrapperUploadOrder(orderEntity));
//        List<BatchInOrder> orders = new ArrayList<>();
//        orders.add(wrapper2(orderEntity));

//            Map<String, String> options = new HashMap<>();
//            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//            options.put("jsonStr", orders.toJSONString());
        RxHttpManager.getInstance().batchInOrders(MfhLoginService.get().getCurrentSessionId(),
                orders,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef(e.toString());
                        //同步失败两次不会再同步
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_FATAL);
//                        orderEntity.setUpdatedDate(new Date());
                        PosOrderService.get().saveOrUpdate(orderEntity);
                        uploadErrorPosOrders(true);
                    }

                    @Override
                    public void onNext(String s) {
                        //修改订单同步状态
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_SYNCED);
//                        orderEntity.setUpdatedDate(new Date());
                        PosOrderService.get().saveOrUpdate(orderEntity);

                        uploadErrorPosOrders(true);
                    }
                });
    }

    /**
     * 提交遗漏的订单
     */
    private void uploadMissingOrders() {
        String sqlWhere = String.format("sellerId = '%d' " +
                        "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                PosOrderEntity.SYNC_STATUS_NONE);
        List<PosOrderEntity> orderEntities = PosOrderService.get()
                .queryAllAsc(sqlWhere, null);
        if (orderEntities != null && orderEntities.size() > 0) {
            PosOrderEntity orderEntity = orderEntities.get(0);
            stepUploadPosOrder(orderEntity);
        } else {
            onNotifyNext(String.format("上传POS订单数据完成。%s",
                    SharedPreferencesUltimate.getPosOrderLastUpdate()
            ));
        }
    }

}
