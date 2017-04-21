package com.bingshanguxue.cashier.v1;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesUltimate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;

/**
 * 收银台
 * Created by bingshanguxue on 05/02/2017.
 */

public class CashierDesktopObservable extends Observable {
    public static final String PREF_NAME_CASHIER = "pref_cashier_base";
    private static final String PK_LAST_CASHIER_DATETIME = "last_cashier_datetime";  //上一次收银时间
    private static final String PK_LAST_CASHIER_FLOWNUMBER = "last_cashier_flownumber";   //上一次收银流水编号


    /**
     * 当前收银台收银商品,用列表的形式显示
     */
    private List<CashierShopcartEntity> shopcartEntities = new ArrayList<>();
    /**
     * 商品总数量
     */
    private Double bcount = 0D;
    /**
     * 商品总金额
     */
    private Double amount = 0D;
    /**
     * 收银信息
     */
    private CashierOrderInfo cashierOrderInfo = null;

//    /**优惠券金额*/
//    private Double couponDiscountAmount = 0D;
//    /**积分*/
//    private Double amount = 0D;
//    /**实付金额*/
//    private Double amount = 0D;
//    /**会员*/
//    private Human vipMember;

    private static CashierDesktopObservable instance = null;

    /**
     * 返回 CashierDesktopObservable 实例
     *
     * @return
     */
    public static CashierDesktopObservable getInstance() {
        if (instance == null) {
            synchronized (CashierDesktopObservable.class) {
                if (instance == null) {
                    instance = new CashierDesktopObservable();
                }
            }
        }
        return instance;
    }

    public List<CashierShopcartEntity> getShopcartEntities() {
        return shopcartEntities;
    }

    public void setShopcartEntities(List<CashierShopcartEntity> shopcartEntities) {
        this.shopcartEntities = shopcartEntities;

        onDatasetChanged();
    }

    public Double getBcount() {
        if (bcount == null) {
            return 0D;
        }
        return bcount;
    }

    public Double getAmount() {
        if (amount == null) {
            return 0D;
        }
        return amount;
    }

    public CashierOrderInfo getCashierOrderInfo() {
        return cashierOrderInfo;
    }

    public void setCashierOrderInfo(CashierOrderInfo cashierOrderInfo) {
        this.cashierOrderInfo = cashierOrderInfo;
        notifyDatasetChanged();
    }

    /**
     * 清空数据
     */
    public void clear() {
        this.shopcartEntities = new ArrayList<>();
        this.cashierOrderInfo = null;
        onDatasetChanged();
    }

    /**
     * 数据发生改变
     */
    public void onDatasetChanged() {
        Double bcountTemp = 0D, amountTemp = 0D;
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            for (CashierShopcartEntity entity : shopcartEntities) {
                bcountTemp += entity.getBcount();
                amountTemp += entity.getFinalAmount();
            }
        }
        this.bcount = bcountTemp;
        this.amount = amountTemp;

        notifyDatasetChanged();
    }

    private void notifyDatasetChanged() {
        setChanged();    //标记此 Observable对象为已改变的对象
        notifyObservers();    //通知所有观察者
    }


    /**
     * 更新并返回最新的流水编号
     */
    public Long getNextFlowId() {
        Date now = new Date();
        Long flowId;

        Date saveDate;
        String saveDateStr = SharedPrefesUltimate
                .getString(PREF_NAME_CASHIER, PK_LAST_CASHIER_DATETIME);
        if (StringUtils.isEmpty(saveDateStr)) {
            saveDate = now;
            flowId = 1L;
        } else {
            saveDate = TimeUtil.parse(saveDateStr, TimeUtil.FORMAT_YYYYMMDDHHMMSS);
            if (TimeUtil.isSameDay(now, saveDate)) {
                ZLogger.d("上一次收银时间和当前时间是同一天，流水编号 ＋1");
                Long saveId = SharedPrefesUltimate
                        .getLong(PREF_NAME_CASHIER, PK_LAST_CASHIER_FLOWNUMBER, 1L);

                flowId = saveId + 1;
            } else {
                ZLogger.d("上一次收银时间和当前时间不是同一天，需要重置流水编号");
                flowId = 1L;
            }
        }

        SharedPrefesUltimate
                .set(PREF_NAME_CASHIER, PK_LAST_CASHIER_DATETIME
                        , TimeUtil.format(saveDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        SharedPrefesUltimate
                .set(PREF_NAME_CASHIER, PK_LAST_CASHIER_FLOWNUMBER, flowId);

        return flowId;
    }
}
