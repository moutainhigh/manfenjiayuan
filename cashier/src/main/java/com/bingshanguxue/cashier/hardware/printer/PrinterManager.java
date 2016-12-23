package com.bingshanguxue.cashier.hardware.printer;

import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.hardware.printer.gprinter.EscCommand;
import com.bingshanguxue.cashier.model.PosOrder;
import com.bingshanguxue.cashier.model.wrapper.DailysettleInfo;
import com.bingshanguxue.cashier.model.wrapper.HandOverBill;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.pmcstock.StockOutItem;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * 通用的功能放在这里面
 * Created by bingshanguxue on 23/12/2016.
 */

public class PrinterManager implements IPrinterManager{
    protected IPrinter mPrinter;

    private static PrinterManager instance = null;

    /**
     * 返回 PrintManagerImpl 实例
     *
     * @return PrintManagerImpl
     */
    public static PrinterManager getInstance() {
        if (instance == null) {
            synchronized (PrinterManager.class) {
                if (instance == null) {
                    instance = new PrinterManager();
                }
            }
        }

        return instance;
    }

    public PrinterManager() {
        mPrinter = create();
    }

    public IPrinter getPrinter() {
        return mPrinter;
    }


    /**
     * 添加小票底部租户名称&联系方式
     */
    public void addFooter(EscCommand esc) {
        if (esc == null) {
            return;
        }
        HostServer hostServer = GlobalInstanceBase.getInstance().getHostServer();
        if (hostServer != null) {
            String footerText = String.format("%s%s\n",
                    Printer.formatShort(hostServer.getSaasName(), 20, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(hostServer.getMobilenumber(), 12, Printer.BLANK_GRAVITY.LEFT));
            esc.addText(footerText);
        }
    }

    public void addCODE128(EscCommand esc, String barcode) {
        if (mPrinter != null){
            mPrinter.addCODE128(esc, barcode);
        }
    }

    @Override
    public IPrinter create() {
        return null;
    }

    @Override
    public void openMoneyBox() {
        if (mPrinter != null){
            mPrinter.openMoneyBox();
        }
    }

    @Override
    public void printTopupReceipt(final QuickPayInfo mQuickPayInfo, final String outTradeNo) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeTopupEsc(mQuickPayInfo, outTradeNo));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand);
                    }
                });
    }

    @Override
    public EscCommand makeTopupEsc(QuickPayInfo mQuickPayInfo, String outTradeNo) {
        return null;
    }

    /**
     * 打印POS订单流水
     */
    @Override
    public void printPosOrder(final PosOrderEntity posOrderEntity) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makePosOrderEsc(posOrderEntity));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand);
                    }
                });
    }

    @Override
    public EscCommand makePosOrderEsc(PosOrderEntity posOrderEntity) {
        return null;
    }


    /**
     * 打印拣货单
     */
    @Override
    public void printPrepareOrder(final ScOrder scOrder) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makePrepareOrderEsc(scOrder));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand);
                    }
                });
    }

    @Override
    public EscCommand makePrepareOrderEsc(ScOrder scOrder) {
        return null;
    }

    @Override
    public void printSendOrder(final ScOrder scOrder, final int printTimes) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeSendOrderEsc(scOrder));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand, printTimes);
                    }
                });
    }

    @Override
    public EscCommand makeSendOrderEsc(ScOrder scOrder) {
        return null;
    }

    /**
     * 打印出库单(取件)
     */
    @Override
    public void printStockOutOrder(final List<StockOutItem> orderItems) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeStockOutOrderEsc(orderItems));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand);
                    }
                });
    }

    @Override
    public EscCommand makeStockOutOrderEsc(List<StockOutItem> orderItems) {
        return null;
    }

    @Override
    public void printDailySettleReceipt(final DailysettleInfo dailysettleInfo) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeDailySettleEsc(dailysettleInfo));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand);
                    }
                });
    }

    @Override
    public EscCommand makeDailySettleEsc(DailysettleInfo dailysettleInfo) {
        return null;
    }

    @Override
    public void printHandoverBill(final HandOverBill handOverBill) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeHandoverEsc(handOverBill));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand);
                    }
                });
    }

    @Override
    public EscCommand makeHandoverEsc(HandOverBill handOverBill) {
        return null;
    }

    /**
     * 打印 商品明细（城市之间）
     * <p/>
     * esc.addText("012345678901234567890123456789\n");//20+6+6=32
     * esc.addText("商品名          00.0011.1122.22\n");
     * esc.addText("商品名               数量  金额\n");
     * 012345678901234567890123 45678901
     * 商品名               金额
     */
    @Override
    public EscCommand makeHandoverTemp(EscCommand rawEsc, String name, String bcount, String amount) {
        return null;
    }

    /**
     * 打印日结经营分析明细
     */
    public int printDailySettleAggItem(EscCommand esc, int startIndex,
                                               List<AggItem> aggItems) {
        if (aggItems != null) {
            for (AggItem aggItem : aggItems) {
                makeHandoverTemp(esc,
                        String.format("%d %s/%s", startIndex, aggItem.getBizTypeCaption(),
                                aggItem.getSubTypeCaption()),
                        String.format("%.2f", aggItem.getOrderNum()),
                        String.format("%.2f", aggItem.getTurnover()));
                startIndex++;
            }
        }
        return startIndex;
    }

    /**
     * 打印日结流水分析明细
     */
    public int printDailySettleAccItem(EscCommand esc, int startIndex,
                                               List<AccItem> accItems) {
        if (accItems != null && accItems.size() > 0) {
            for (AccItem accItem : accItems){
                makeHandoverTemp(esc,
                        String.format("%d %s", startIndex, accItem.getPayTypeCaption()),
                        String.format("%.2f", accItem.getOrderNum()),
                        String.format("%.2f", accItem.getAmount()));
                startIndex++;
            }
        }
        return startIndex;
    }

    @Override
    public void printPosOrder(final PosOrder posOrder, final int printTimes) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makePosOrderEsc(posOrder));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand, printTimes);
                    }
                });
    }
    /**
     * 打印订单
     */
    private EscCommand makePosOrderEsc(PosOrder posOrder) {
        EscCommand esc = new EscCommand();

        if (posOrder == null) {
            return null;
        }

        Integer bizType = posOrder.getBtype();
        Integer subType = posOrder.getSubType();
        if (BizType.POS.equals(bizType)) {
            if (PosType.POS_STANDARD.equals(subType)) {
                //收银
                return makePosOrderEsc1(posOrder);
            } else {
                //外部订单
                return makePosOrderEsc2(posOrder);
            }
        } else if (BizType.SC.equals(bizType)) {
            //平台订单
            return makePosOrderEsc3(posOrder);
        }
        return esc;
    }

    @Override
    public EscCommand makePosOrderEsc1(PosOrder posOrder) {
        return null;
    }

    @Override
    public EscCommand makePosOrderEsc2(PosOrder posOrder) {
        return null;
    }

    @Override
    public EscCommand makePosOrderEsc3(PosOrder posOrder) {
        return null;
    }

    @Override
    public void printTestPage() {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeTestPageEsc());
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        Printer.print(escCommand);
                    }
                });
    }

    @Override
    public EscCommand makeTestPageEsc() {
        return null;
    }
    /**
     * 打印 商品明细（城市之间）
     * 5个数字等于3个汉字（1个数字＝3/5个汉字）
     * esc.addText("--------------------------------\n");//32(正确)
     * esc.addText("01234567890123456789012345678901\n");//32(正确)
     * esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
     * esc.addText("零一二三四五六七八九零一二三四五六七八\n");//19.2(错误)
     * <p/>
     * esc.addText("01234567890123456789012345678901\n");//32(正确)
     * esc.addText("商品名           00.0100.02000.03\n");32=16+5+5+6
     * <p/>
     * esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
     * esc.addText("货号/品名       单价 数量 小计\n");
     * esc.addText("货号/品名       单价 数量   小计\n");
     * esc.addText("01234567890123456789012345678901\n");//32(正确)
     * esc.addText("货号/品名           单价数量   小计\n");
     * esc.addText("货号/品名        单价数量   小计\n");
     * esc.addText("货号/品名       单价 数量   小计\n");
     * esc.addText("业务类型            数量    金额\n");
     */
    @Override
    public void makeTestTemp(EscCommand esc, String name, String price, String bcount, String amount) {

    }

    @Override
    public void makeOrderItem1(EscCommand esc, String name, String unit, String bcount) {

    }

    /**
     * 打印 商品明细（城市之间）
     * 5个数字等于3个汉字（1个数字＝3/5个汉字）
     * esc.addText("--------------------------------\n");//32(正确)
     * esc.addText("01234567890123456789012345678901\n");//32(正确)
     * esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
     * esc.addText("货号/品名     单价   数量  小计\n");
     * esc.addText("商品名        00.01  00.020  00.03\n");32=16+5+5+6
     */
    @Override
    public void makeOrderItem2(EscCommand esc, String name, String price, String bcount, String amount) {

    }

    @Override
    public void makeOrderItem3(EscCommand esc, String name, String bcount, String amount) {
        if (esc == null) {
            return;
        }

        try {
            //计算行数
            int nameL = (name == null ? 0 : (Printer.getLength(name) - 1) / 20 + 1);
            int maxLine = Math.max(2, nameL);

            String nameTemp = name;
            for (int i = 0; i < maxLine; i++) {
                String nameLine = DataConvertUtil.subString(nameTemp,
                        Math.min(20, Printer.getLength(nameTemp)));
                StringBuilder line = new StringBuilder();
                //插入名称，不足补空格
                line.append(Printer.formatShort(nameLine, 22, Printer.BLANK_GRAVITY.RIGHT));
                if (i == 0) {
                    line.append(Printer.formatShort(bcount, 10, Printer.BLANK_GRAVITY.LEFT));
                } else if (i == 1) {
                    line.append(Printer.formatShort(amount, 10, Printer.BLANK_GRAVITY.LEFT));
                }
                line.append("\n");
                esc.addText(line.toString());

                if (!StringUtils.isEmpty(nameTemp)) {
                    nameTemp = nameTemp.substring(nameLine.length(), nameTemp.length()).trim();
                }
            }
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }

    @Override
    public void makeOrderItem4(EscCommand esc, String name, String bcount, String amount) {

    }
}
