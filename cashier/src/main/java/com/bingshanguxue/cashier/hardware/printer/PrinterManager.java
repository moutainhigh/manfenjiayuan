package com.bingshanguxue.cashier.hardware.printer;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.hardware.printer.gprinter.EscCommand;
import com.bingshanguxue.cashier.model.wrapper.DailysettleInfo;
import com.bingshanguxue.cashier.model.wrapper.HandOverBill;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PayWay;
import com.bingshanguxue.cashier.model.wrapper.PayWayType;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.TenantInfoWrapper;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.pmcstock.GoodsItem;
import com.mfh.framework.api.pmcstock.PosOrder;
import com.mfh.framework.api.pmcstock.StockOutItem;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;

import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * 通用的功能放在这里面
 * Created by bingshanguxue on 23/12/2016.
 */

public abstract class PrinterManager implements IPrinterManager {
    protected IPrinter mPrinter;


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
        TenantInfoWrapper hostServer = GlobalInstanceBase.getInstance().getHostServer();
        if (hostServer != null) {
            String footerText = String.format("%s%s\n",
                    Printer.formatShort(hostServer.getSaasName(), 20, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(hostServer.getMobilenumber(), 12, Printer.BLANK_GRAVITY.LEFT));
            esc.addText(footerText);
        }
    }

    /**
     * 打印条码，自动进纸一行
     */
    public void addCODE128(EscCommand esc, String barcode) {
        if (mPrinter != null) {
            mPrinter.addCODE128(esc, barcode);
            mPrinter.printAndLineFeed(esc, 1);
        }
    }

    public void printAndLineFeed(EscCommand esc, int lines) {
        if (mPrinter != null) {
            mPrinter.printAndLineFeed(esc, lines);
        }
    }

    public abstract IPrinter create();

    @Override
    public void openMoneyBox() {
        if (mPrinter != null) {
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

    public abstract EscCommand makeTopupEsc(QuickPayInfo mQuickPayInfo, String outTradeNo);

    /**
     * 打印POS订单流水
     */
    @Override
    public void printPosOrder(final PosOrderEntity posOrderEntity) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                EscCommand escCommand = null;
                if (posOrderEntity != null) {
                    if (PosType.POS_STANDARD.equals(posOrderEntity.getSubType())) {
                        escCommand = makePosOrderEsc(posOrderEntity, false);
                    } else {
                        escCommand = makeSendOrder3pEsc(posOrderEntity);
                    }
                }
                subscriber.onNext(escCommand);
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
                        Printer.print(escCommand, PrinterAgent.getInstance()
                                .getPrinterTimes(PrinterContract.Receipt.CASHIER_ORDER));
                    }
                });
    }

    /**
     * 打印收银流水订单
     */
    @Override
    public void printCashierOrder(final PosOrderEntity posOrderEntity) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                EscCommand escCommand = makePosOrderEsc(posOrderEntity, true);
                subscriber.onNext(escCommand);
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
                        Printer.print(escCommand, PrinterAgent.getInstance()
                                .getPrinterTimes(PrinterContract.Receipt.CASHIER_ORDER));
                    }
                });
    }

    public abstract EscCommand makePosOrderEsc(PosOrderEntity posOrderEntity, boolean waitReceiptEnabled);

    /**
     * 外部平台配送单
     */
    public abstract EscCommand makeSendOrder3pEsc(PosOrderEntity posOrderEntity);


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
                        Printer.print(escCommand, PrinterAgent.getInstance()
                                .getPrinterTimes(PrinterContract.Receipt.PREPARE_ORDER));
                    }
                });
    }

    public abstract EscCommand makePrepareOrderEsc(ScOrder scOrder);

    @Override
    public void printSendOrder(final ScOrder scOrder) {
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
                        Printer.print(escCommand, PrinterAgent.getInstance()
                                .getPrinterTimes(PrinterContract.Receipt.SEND_ORDER));
                    }
                });
    }

    public abstract EscCommand makeSendOrderEsc(ScOrder scOrder);

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
                        Printer.print(escCommand, PrinterAgent.getInstance()
                                .getPrinterTimes(PrinterContract.Receipt.STOCKOUT));
                    }
                });
    }

    public abstract EscCommand makeStockOutOrderEsc(List<StockOutItem> orderItems);

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
                        Printer.print(escCommand, PrinterAgent.getInstance().
                                getPrinterTimes(PrinterContract.Receipt.ANALYSIS));
                    }
                });
    }

    public abstract EscCommand makeDailySettleEsc(DailysettleInfo dailysettleInfo);

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
                        Printer.print(escCommand, PrinterAgent.getInstance()
                                .getPrinterTimes(PrinterContract.Receipt.ANALYSIS));
                    }
                });
    }

    public abstract EscCommand makeHandoverEsc(HandOverBill handOverBill);

    /**
     * 打印 商品明细（城市之间）
     * <p/>
     * esc.addText("012345678901234567890123456789\n");//20+6+6=32
     * esc.addText("商品名          00.0011.1122.22\n");
     * esc.addText("商品名               数量  金额\n");
     * 012345678901234567890123 45678901
     * 商品名               金额
     */
    public abstract void makeHandoverTemp(EscCommand rawEsc, String name, String bcount, String amount);

    /**
     * 打印日结经营分析明细
     */
    public void printDailySettleAggItem(EscCommand esc, int startIndex,
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
    }

    /**
     * 打印日结流水分析明细
     */
    public void printDailySettleAccItem(EscCommand esc, int startIndex,
                                        List<AccItem> accItems) {
        if (accItems != null && accItems.size() > 0) {
            for (AccItem accItem : accItems) {
                makeHandoverTemp(esc,
                        String.format("%d %s", startIndex, accItem.getPayTypeCaption()),
                        String.format("%.2f", accItem.getOrderNum()),
                        String.format("%.2f", accItem.getAmount()));
                startIndex++;
            }
        }
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
                        ZLogger.ef(e.toString());
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
    public EscCommand makePosOrderEsc(PosOrder posOrder) {
        EscCommand esc = new EscCommand();

        if (posOrder == null) {
            ZLogger.d("订单数据无效，生成打印数据失败");
            return null;
        }
        ZLogger.d(String.format("准备生成打印数据：%s", JSONObject.toJSONString(posOrder)));

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

    //收银订单
    public abstract EscCommand makePosOrderEsc1(PosOrder posOrder);

    //平台配送单
    public abstract EscCommand makePosOrderEsc2(PosOrder posOrder);

    //外部平台配送单
    public abstract EscCommand makePosOrderEsc3(PosOrder posOrder);

    @Override
    public void printGoodsFlow(final List<GoodsItem> goodsItems) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeGoodsFlowEsc(goodsItems));
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
                        Printer.print(escCommand, 1);
                    }
                });
    }

    public abstract EscCommand makeGoodsFlowEsc(List<GoodsItem> goodsItems);


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

    public abstract EscCommand makeTestPageEsc();

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
    public abstract void makeTestTemp(EscCommand esc, String name, String price, String bcount, String amount);

    public abstract void makeOrderItem1(EscCommand esc, String name, String unit, String bcount);

    /**
     * 打印 商品明细（城市之间）
     * 5个数字等于3个汉字（1个数字＝3/5个汉字）
     * esc.addText("--------------------------------\n");//32(正确)
     * esc.addText("01234567890123456789012345678901\n");//32(正确)
     * esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
     * esc.addText("货号/品名     单价   数量  小计\n");
     * esc.addText("商品名        00.01  00.020  00.03\n");32=16+5+5+6
     */
    public abstract void makeOrderItem2(EscCommand esc, String name, String price, String bcount, String amount);

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

    public void makeOrderItem4(EscCommand esc, String text1, String text2, String text3) {
        if (esc == null) {
            return;
        }

        try {
            //计算行数
            int nameLen = 18;
            int maxLine = Math.max(1, (text1 == null ? 0 : (Printer.getLength(text1) - 1) / nameLen + 1));

            String nameTemp = text1;
            for (int i = 0; i < maxLine; i++) {
                String nameLine = DataConvertUtil.subString(nameTemp,
                        Math.min(nameLen, Printer.getLength(nameTemp)));
                StringBuilder line = new StringBuilder();
                //插入名称，不足补空格
                //中间一行插入数量&金额
                if (i == 0) {
                    line.append(Printer.formatShort(nameLine, nameLen, Printer.BLANK_GRAVITY.RIGHT));
                    line.append(Printer.formatShort(text2, 7, Printer.BLANK_GRAVITY.LEFT));
                    line.append(Printer.formatShort(text3, 7, Printer.BLANK_GRAVITY.LEFT));
                } else {
                    line.append(Printer.formatShort(nameLine, nameLen, Printer.BLANK_GRAVITY.RIGHT));
                }
                line.append("\n");
                esc.addText(line.toString());

//                assert nameTemp != null;
                if (!StringUtils.isEmpty(nameTemp)) {
                    nameTemp = nameTemp.substring(nameLine.length(), nameTemp.length()).trim();
                }
            }
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }

    public void makeOrderItem5(EscCommand esc, String text1, String text2, String text3, String text4) {
        if (esc == null) {
            return;
        }

        try {
            //计算行数
            int nameLen = 11;
            int maxLine = Math.max(1, (text1 == null ? 0 : (Printer.getLength(text1) - 1) / nameLen + 1));

            String nameTemp = text1;
            for (int i = 0; i < maxLine; i++) {
                String nameLine = DataConvertUtil.subString(nameTemp,
                        Math.min(nameLen, Printer.getLength(nameTemp)));
                StringBuilder line = new StringBuilder();
                //插入名称，不足补空格
                //中间一行插入数量&金额
                if (i == 0) {
                    line.append(Printer.formatShort(nameLine, nameLen, Printer.BLANK_GRAVITY.RIGHT));
                    line.append(Printer.formatShort(text2, 7, Printer.BLANK_GRAVITY.LEFT));
                    line.append(Printer.formatShort(text3, 7, Printer.BLANK_GRAVITY.LEFT));
                    line.append(Printer.formatShort(text4, 7, Printer.BLANK_GRAVITY.LEFT));
                } else {
                    line.append(Printer.formatShort(nameLine, nameLen, Printer.BLANK_GRAVITY.RIGHT));
                }
                line.append("\n");
                esc.addText(line.toString());

//                assert nameTemp != null;
                if (!StringUtils.isEmpty(nameTemp)) {
                    nameTemp = nameTemp.substring(nameLine.length(), nameTemp.length()).trim();
                }
            }
//            esc.addText("\n");
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }

    public void makeOrderItem6(EscCommand esc, String name, String bcount, String amount) {
        if (esc == null) {
            return;
        }

        try {
            //计算行数
            int maxLine = Math.max(1, (name == null ? 0 : (Printer.getLength(name) - 1) / 20 + 1));
//        ZLogger.d(String.format("maxLine=%d", maxLine));

            String nameTemp = name;
            int mid = maxLine / 2;
            for (int i = 0; i < maxLine; i++) {
//            ZLogger.d(String.format("nameTemp: %d(%d)", getLength(nameTemp), nameTemp.toCharArray().length));
                String nameLine = DataConvertUtil.subString(nameTemp,
                        Math.min(20, Printer.getLength(nameTemp)));
//            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, nameTemp.toCharArray().length));
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
                StringBuilder line = new StringBuilder();
                //插入名称，不足补空格
                //中间一行插入数量&金额
                if (i == mid) {
                    line.append(Printer.formatShort(nameLine, 20, Printer.BLANK_GRAVITY.RIGHT));
                    line.append(Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.RIGHT));
                    line.append(Printer.formatShort(amount, 6, Printer.BLANK_GRAVITY.LEFT));
                } else {
                    line.append(Printer.formatShort(nameLine, 20, Printer.BLANK_GRAVITY.RIGHT));
                }
                line.append("\n");
//            ZLogger.d(String.format("print line(%d/%d):%s" , i, mid, line.toString()));
                esc.addText(line.toString());

//                assert nameTemp != null;
                if (!StringUtils.isEmpty(nameTemp)) {
                    nameTemp = nameTemp.substring(nameLine.length(), nameTemp.length()).trim();
                }
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
//            line.append(formatLong(sub2, 8));
            }
//        esc.addText("--------------------------------\n");//32个
//            esc.addText("\n");

        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }


    /**
     * 收银订单明细打印样式7
     * <ol>
     * 三行显示
     * <li>text1    text2   text3</li>
     * <li>text4    text5   text6</li>
     * <li>text7    text8   text9</li>
     * </ol>
     */
    public void makeOrderItem7(EscCommand esc,
                               String text1, String text2, String text3,
                               String text4, String text5, String text6,
                               String text7, String text8, String text9,
                               boolean bottomLineEnabled) {
        if (esc == null) {
            return;
        }

        esc.addText(String.format("%s%s%s\n",
                Printer.formatShort(text1, 16, Printer.BLANK_GRAVITY.RIGHT),
                Printer.formatShort(text2, 8, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(text3, 8, Printer.BLANK_GRAVITY.LEFT)));
        esc.addText(String.format("%s%s%s\n",
                Printer.formatShort(text4, 16, Printer.BLANK_GRAVITY.RIGHT),
                Printer.formatShort(text5, 8, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(text6, 8, Printer.BLANK_GRAVITY.LEFT)));
        esc.addText(String.format("%s%s%s\n",
                Printer.formatShort(text7, 16, Printer.BLANK_GRAVITY.RIGHT),
                Printer.formatShort(text8, 8, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(text9, 8, Printer.BLANK_GRAVITY.LEFT)));
        if (bottomLineEnabled) {
            esc.addText("--------------------------------\n");//32个
        }
    }

    /**
     * 收银订单明细打印样式8
     */
    public void makeOrderItem8(EscCommand esc, String text1, String text2, String text3,
                               String text4, String text5) {
        if (esc == null) {
            return;
        }

//        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText(String.format("%s%s%s\n",
                Printer.formatShort(text1, 16, Printer.BLANK_GRAVITY.RIGHT),
                Printer.formatShort(text2, 8, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(text3, 8, Printer.BLANK_GRAVITY.LEFT)));

        esc.addText(String.format("%s%s%s\n",
                Printer.formatShort("", 16, Printer.BLANK_GRAVITY.RIGHT),
                Printer.formatShort(text4, 8, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(text5, 8, Printer.BLANK_GRAVITY.LEFT)));
    }

    private Double getPayWayAmount(Integer amountType, Map<Integer, PayWay> payWayMap) {
        if (amountType == null || payWayMap == null) {
            return 0D;
        }

        PayWay payWay = payWayMap.get(amountType);

        return payWay != null ? payWay.getAmount() : 0D;
    }

    /**
     * 支付记录
     */
    public void appendPayWays(EscCommand esc, Long orderId) {
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderId);
        Map<Integer, PayWay> payWayMap = OrderPayInfo.getPayWays(payWrapper);

        Double vipDiscount = getPayWayAmount(PayWayType.TYPE_VIP_DISCOUNT, payWayMap);
        payWayMap.remove(PayWayType.TYPE_VIP_DISCOUNT);
        Double vipPromotion = getPayWayAmount(PayWayType.TYPE_VIP_PROMOTION, payWayMap);
        payWayMap.remove(PayWayType.TYPE_VIP_PROMOTION);
        Double vipCoupons = getPayWayAmount(PayWayType.TYPE_VIP_COUPONS, payWayMap);
        payWayMap.remove(PayWayType.TYPE_VIP_COUPONS);
        Double cashAmount = getPayWayAmount(PayWayType.TYPE_CASH, payWayMap);
        payWayMap.remove(PayWayType.TYPE_CASH);
        Double cashChangeAmount = getPayWayAmount(PayWayType.TYPE_CASH_CHANGE, payWayMap);
        payWayMap.remove(PayWayType.TYPE_CASH_CHANGE);
        Double vipAmount = getPayWayAmount(PayWayType.TYPE_VIP, payWayMap);
        payWayMap.remove(PayWayType.TYPE_VIP);
        Double vipBalanceAmount = getPayWayAmount(PayWayType.TYPE_VIP_BALANCE, payWayMap);
        payWayMap.remove(PayWayType.TYPE_VIP_BALANCE);

        esc.addText(String.format("%s%s\n",
                Printer.formatShort("会员优惠:", 24, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(String.format("%.3f", vipDiscount), 8, Printer.BLANK_GRAVITY.LEFT)));
        esc.addText(String.format("%s%s\n",
                Printer.formatShort("促销优惠:", 24, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(String.format("%.3f", vipPromotion), 8, Printer.BLANK_GRAVITY.LEFT)));
        esc.addText(String.format("%s%s\n",
                Printer.formatShort("优惠券:", 24, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(String.format("%.3f", vipCoupons), 8, Printer.BLANK_GRAVITY.LEFT)));
        esc.addText(String.format("%s%s\n",
                Printer.formatShort("优惠合计:", 24, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(String.format("%.3f", MathCompact.add(vipDiscount, vipCoupons)),
                        8, Printer.BLANK_GRAVITY.LEFT)));

        //现金支付（订单支付金额＋找零金额）
        Integer wayType = payWrapper.getPayType();
        if ((wayType & WayType.CASH) == WayType.CASH) {
            esc.addText(String.format("%s%s\n",
                    Printer.formatShort("现金支付:", 24, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(String.format("%.3f", MathCompact.add(cashAmount, cashChangeAmount)),
                            8, Printer.BLANK_GRAVITY.LEFT)));
            esc.addText(String.format("%s%s\n",
                    Printer.formatShort("找零:", 24, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(String.format("%.3f", cashChangeAmount),
                            8, Printer.BLANK_GRAVITY.LEFT)));
        }
        //会员支付（会员支付金额＋账户余额）
        else if ((wayType & WayType.VIP) == WayType.VIP) {
            esc.addText(String.format("%s%s\n",
                    Printer.formatShort(String.format("%s:",
                            PayWayType.getWayTypeName(PayWayType.TYPE_VIP)),
                            24, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(String.format("%.3f", vipAmount),
                            8, Printer.BLANK_GRAVITY.LEFT)));
            esc.addText(String.format("%s%s\n",
                    Printer.formatShort(String.format("%s:",
                            PayWayType.getWayTypeName(PayWayType.TYPE_VIP_BALANCE)),
                            24, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(String.format("%.2f", vipBalanceAmount),
                            8, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            for (Integer key : payWayMap.keySet()) {
                PayWay payWay = payWayMap.get(key);
                esc.addText(String.format("%s%s\n",
                        Printer.formatShort(PayWayType.getWayTypeName(payWay.getAmountType()), 24, Printer.BLANK_GRAVITY.LEFT),
                        Printer.formatShort(String.format("%.3f", payWay.getAmount()), 8, Printer.BLANK_GRAVITY.LEFT)));
            }
        }
    }

}
