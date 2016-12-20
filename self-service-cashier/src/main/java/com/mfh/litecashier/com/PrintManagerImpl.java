package com.mfh.litecashier.com;

import com.bingshanguxue.cashier.hardware.printer.PrintManager;
import com.bingshanguxue.cashier.hardware.printer.Printer;
import com.gprinter.command.EscCommand;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.AggItem;
import com.mfh.litecashier.bean.StockOutItem;
import com.mfh.litecashier.ui.fragment.dailysettle.DailysettleInfo;
import com.mfh.litecashier.ui.fragment.dailysettle.HandOverBill;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by bingshanguxue on 8/12/16.
 */
public class PrintManagerImpl extends PrintManager {

    private static PrintManagerImpl instance = null;

    /**
     * 返回 PrintManagerImpl 实例
     *
     * @return PrintManagerImpl
     */
    public static PrintManagerImpl getInstance() {
        if (instance == null) {
            synchronized (PrintManagerImpl.class) {
                if (instance == null) {
                    instance = new PrintManagerImpl();
                }
            }
        }
        return instance;
    }


    private static EscCommand makeEsc(HandOverBill handOverBill) {
        if (handOverBill == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题：网点名称*/
        esc.addText(MfhLoginService.get().getCurOfficeName());
//        //取消倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 抬头：交接班信息*/
        esc.addText(String.format("班次：%d \n", handOverBill.getShiftId()));
        esc.addText(String.format("交班人:%s\n", handOverBill.getHumanName()));
        //打印上班时间，来核对交班信息
//        esc.addText(String.format("上班时间：%s \n", TimeCursor.InnerFormat.format(handOverBill.getStartDate())));
        esc.addText(String.format("交班时间：%s \n",
                TimeCursor.InnerFormat.format(handOverBill.getEndDate())));
        esc.addText(String.format("设备编号：%s \n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 交接单明细*/
        esc.addText("业务类型            数量    金额\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

//        List<HandoverAggItem> aggShiftList = handOverBill.getAggShiftList();
//        for (HandoverAggItem aggShift : aggShiftList){
//            makeHandoverTemp(esc, aggShift.get, String.format("%.2f", aggShift.getOrderNum()), String.format("%.2f", aggShift.getTurnover()));
//        }
        printDailySettleAggItem(esc, 1, handOverBill.getAggItems());
        esc.addPrintAndLineFeed();//进纸一行
        esc.addText("支付类型            数量    金额\n");
        printDailySettleAccItem(esc, 1, handOverBill.getAccItems());
        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("账户新增:%.2f\n", handOverBill.getAmount() - handOverBill.getCash()));
        esc.addText(String.format("现金收取:%.2f\n", handOverBill.getCash()));
        esc.addText(String.format("营业额合计:%.2f\n", handOverBill.getAmount()));

        /**
         * 打印 结束语
         * */
        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        esc.addText("辛苦了!\n");
        esc.addText("祝您生活愉快!\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 打印日结经营分析明细
     */
    private static int printDailySettleAggItem(EscCommand esc, int startIndex,
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
    private static int printDailySettleAccItem(EscCommand esc, int startIndex,
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

    /**
     * 打印 商品明细（城市之间）
     * <p/>
     * esc.addText("012345678901234567890123456789\n");//20+6+6=32
     * esc.addText("商品名          00.0011.1122.22\n");
     * esc.addText("商品名               数量  金额\n");
     * 012345678901234567890123 45678901
     * 商品名               金额
     */
    private static EscCommand makeHandoverTemp(EscCommand rawEsc, String name,
                                               String bcount, String amount) {
        EscCommand esc = rawEsc;
        if (esc == null) {
            esc = new EscCommand();
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //计算名称行数
        if (Printer.getLength(name) > 20) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s", Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 6, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s", Printer.formatShort(name, 20, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 6,Printer. BLANK_GRAVITY.LEFT));
//            ZLogger.d("printText:" + printText);
            esc.addText(printText);
        }
        esc.addText("\n");

        return esc;
    }

    /**
     * 打印交接单
     */
    public static void printHandoverBill(final HandOverBill handOverBill) {

        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeEsc(handOverBill));
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

    private static EscCommand makeEsc(DailysettleInfo dailysettleInfo) {
        if (dailysettleInfo == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        //显示当前网点名称
        esc.addText(dailysettleInfo.getOfficeName());
//        //取消倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 抬头：日结信息*/
//        esc.addText(String.format("班次：%d \n", handOverBill.getShiftId()));
        esc.addText(String.format("日结人:%s\n", dailysettleInfo.getHumanName()));
        esc.addText(String.format("日结时间：%s \n",
                TimeCursor.InnerFormat.format(dailysettleInfo.getCreatedDate())));
        esc.addText(String.format("设备编号：%s \n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 日结单明细*/
        esc.addText("业务类型            数量    金额\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        printDailySettleAggItem(esc, 1, dailysettleInfo.getAggItems());

        esc.addPrintAndLineFeed();//进纸一行
        esc.addText("支付类型            数量    金额\n");
        printDailySettleAccItem(esc, 1, dailysettleInfo.getAccItems());

        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("现金收取:%.2f\n", dailysettleInfo.getCash()));
        esc.addText(String.format("非现金收取:%.2f\n",
                dailysettleInfo.getTurnOver() - dailysettleInfo.getCash()));

        Double turnover = dailysettleInfo.getTurnOver();
        esc.addText(String.format("营业额合计:%.2f\n", turnover));

        /**
         * 打印 结束语
         * */
        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        esc.addText("辛苦了!\n");
        esc.addText("祝您生活愉快!\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 打印日结单
     */
    public static void printDailySettleBill(final DailysettleInfo dailysettleInfo) {

        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeEsc(dailysettleInfo));
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

    private static EscCommand makeEsc(List<StockOutItem> orderItems) {
        if (orderItems == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        esc.addText("商品取件配送单\n");
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 订购日期*/
        esc.addText(String.format("日期:%s \n", TimeCursor.InnerFormat.format(new Date())));
        esc.addPrintAndLineFeed();

        /**打印 商品明细
         *    8       8       4     6      6
         *01234567 89012345 6789 012345 678901 (32)
         * 商品ID    品名   数量   单价   金额
         * */
//        esc.addText("收货人                    手机号\n");
        esc.addText("--------------------------------\n");//32个
//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        if (orderItems.size() > 0) {
            for (StockOutItem entity : orderItems) {
//                /**打印 商品明细*/
//                makeTemp(esc, entity.getProductName(), String.format("%.2f", entity.getAmount()));

                esc.addText(String.format("%s/%s\n", entity.getHumanName(), entity.getHumanPhone()));
                esc.addText(String.format("%s/%s\n", entity.getBarcode(), entity.getTransportName()));
            }

            esc.addText("--------------------------------\n");//32个
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("总计:%d件\n", orderItems.size()));
        }

        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("签字：\n");
        esc.addPrintAndLineFeed();


        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        /**打印 结束语*/
        esc.addText("本单据一式两份，取货发货方各执一份\n");
        esc.addText("业务电话：400 886 6671\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 打印出库单(取件)
     */
    public static void printStockOut(final List<StockOutItem> orderItems) {

        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeEsc(orderItems));
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

    /**
     * 打印拣货单
     */
    private  EscCommand makePrepareOrderEsc(ScOrder scOrder) {
        if (scOrder == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);

        // 一维条码：设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);
        //设置条码单元宽度为1点
        esc.addSetBarcodeWidth((byte) 1);
        esc.addCODE128(esc.genCode128(scOrder.getBarcode()));

        esc.addPrintAndLineFeed();//进纸一行
        esc.addText("拣货单");
        esc.addPrintAndLineFeed();//进纸一行

        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText(String.format("客户:%s/%s \n",
                scOrder.getReceiveName(), scOrder.getReceivePhone()));
        esc.addText(String.format("配送地址:%s \n", scOrder.getAddress()));
        esc.addText(String.format("下单时间：%s \n",
                TimeUtil.format(scOrder.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("配送时间：%s \n",
                TimeUtil.format(scOrder.getDueDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("备注:%s \n", scOrder.getRemark()));
        esc.addPrintAndLineFeed();//进纸一行

        /**打印 商品明细*/
        esc.addText("品名               数量   小计\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        List<ScOrderItem> items = scOrder.getItems();
        if (items != null && items.size() > 0) {
            esc.addText("--------------------------------\n");//32个
            for (ScOrderItem item : items) {
                createPrepareOrderItem(esc, item.getProductName(),
                        MUtils.formatDouble(null, null, item.getBcount(), "", "", item.getUnitName()),
                        MUtils.formatDouble(item.getAmount(), ""));
            }
            esc.addText("--------------------------------\n");//32个
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);
        esc.addText(MUtils.formatDouble("订单金额", " ", scOrder.getAmount(), "", null, null));

        /**
         * 打印 结束语
         * */
        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        HostServer hostServer = GlobalInstanceBase.getInstance().getHostServer();
        if (hostServer != null){
            String footerText = String.format("%s%s",
                    Printer.formatShort(hostServer.getSaasName(), 20, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(hostServer.getMobilenumber(), 12, Printer.BLANK_GRAVITY.LEFT));
            esc.addText(footerText);
        }
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 打印拣货单
     */
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

    /**
     * 打印配送单
     */
    private EscCommand makeSendOrderEsc(ScOrder scOrder) {
        if (scOrder == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);

        // 一维条码：设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);
        //设置条码单元宽度为1点
        esc.addSetBarcodeWidth((byte) 1);
        esc.addCODE128(esc.genCode128(scOrder.getBarcode()));

        esc.addPrintAndLineFeed();//进纸一行
        esc.addText("配送单");
        esc.addPrintAndLineFeed();//进纸一行

        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText(String.format("客户:%s/%s \n",
                scOrder.getReceiveName(), scOrder.getReceivePhone()));
        esc.addText(String.format("配送地址:%s \n", scOrder.getAddress()));
        esc.addText(String.format("下单时间：%s \n",
                TimeUtil.format(scOrder.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("配送时间：%s \n",
                TimeUtil.format(scOrder.getDueDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("备注:%s \n", scOrder.getRemark()));
        esc.addText(String.format("买手:%s/%s \n",
                scOrder.getServiceHumanName(), scOrder.getServiceMobile()));
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行

        /**打印 商品明细*/
        esc.addText("品名                数量   小计\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        Double amount = 0D, actualAmount = 0D;
        List<ScOrderItem> items = scOrder.getItems();
        if (items != null && items.size() > 0) {
            esc.addText("--------------------------------\n");//32个
            for (ScOrderItem item : items) {
                amount += MathCompact.mult(item.getPrice(), item.getBcount());
                actualAmount += MathCompact.mult(item.getPrice(), item.getQuantityCheck());

                esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
                makeOrderItem1(esc, item.getProductName(),
                        MUtils.formatDouble(item.getBcount(), ""),
                        MUtils.formatDouble(item.getAmount(), ""));
                esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);
                esc.addText(MUtils.formatDouble(MathCompact.mult(item.getCommitCount(), item.getPrice()), ""));//32个
                esc.addText("\n");
            }
            esc.addText("--------------------------------\n");//32个
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);
        esc.addText(MUtils.formatDouble("订单金额", ": ", amount, "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("拣货金额", ": ", actualAmount, "", null, null));
        esc.addText("\n");
        Double disAmount = MathCompact.sub(actualAmount, amount);
        if (disAmount < 0){
            esc.addText(String.format("   差额: -%.2f\n", disAmount));
        }
        else{
            esc.addText(String.format("   差额: +%.2f\n", disAmount));
        }
//        esc.addText(MUtils.formatDouble("差额", ": ", MathCompact.sub(amount, actualAmount), "", null, null));
        esc.addText("\n");

        /**
         * 打印 结束语
         * */
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        addFooter(esc);

//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 5);//打印并且走纸3行

        return esc;
    }

    /**
     * 打印配送单
     * 配送单有点像出库单，需要打印打三份，用户一份，门店一份，骑手一份
     */
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


}
