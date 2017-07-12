package com.bingshanguxue.cashier.hardware.printer.emb;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierProvider;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.hardware.printer.IPrinter;
import com.bingshanguxue.cashier.hardware.printer.Printer;
import com.bingshanguxue.cashier.hardware.printer.PrinterManager;
import com.bingshanguxue.cashier.hardware.printer.gprinter.EscCommand;
import com.bingshanguxue.cashier.hardware.printer.gprinter.GPrinter;
import com.bingshanguxue.cashier.model.wrapper.DailysettleInfo;
import com.bingshanguxue.cashier.model.wrapper.HandOverBill;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.ProductAggDate;
import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.api.pmcstock.GoodsItem;
import com.mfh.framework.api.pmcstock.StockOutItem;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.bean.CommonAccountFlow;
import com.mfh.framework.rxapi.bean.GoodsOrder;
import com.mfh.framework.rxapi.bean.GoodsOrderItem;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;
import com.mfh.framework.rxapi.bean.GroupBuyOrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 嵌入式打印机
 * Created by bingshanguxue on 6/22/16.
 */
public class EmbPrinterManager extends PrinterManager {

    private static EmbPrinterManager instance = null;

    /**
     * 返回 PrintManagerImpl 实例
     *
     * @return PrintManagerImpl
     */
    public static EmbPrinterManager getInstance() {
        if (instance == null) {
            synchronized (EmbPrinterManager.class) {
                if (instance == null) {
                    instance = new EmbPrinterManager();
                }
            }
        }

        return instance;
    }

    public EmbPrinterManager() {
        mPrinter = create();
    }

    @Override
    public IPrinter create() {
        return new EmbPrinter();
    }


    @Override
    public EscCommand makeTopupEsc(QuickPayInfo mQuickPayInfo, String outTradeNo) {
        if (mQuickPayInfo == null || StringUtils.isEmpty(outTradeNo)) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        //打印并且走纸3行
        printAndLineFeed(esc, 2);

        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        if (!StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())) {
            //显示当前网点名称
            esc.addText(MfhLoginService.get().getCurOfficeName());
        }

        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        /**打印 机器设备号＋订单号*/
        esc.addText(String.format("设备编号：%s \n", SharedPrefesManagerFactory.getTerminalId()));
        /**打印 日期*/
        esc.addText(String.format("订单日期：%s \n", TimeUtil.format(TimeUtil.getCurrentDate(),
                TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("支付方式：%s \n", WayType.name(mQuickPayInfo.getPayType())));
        esc.addText(String.format("流水号  ：%s \n", outTradeNo));
        esc.addText(String.format("金额   ：%.2f \n", mQuickPayInfo.getAmount()));
        esc.addText("--------------------------------\n");//32个

        /**
         * 打印 结束语
         * */
        printAndLineFeed(esc, 1);
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        esc.addText("请注意保管好您的交易凭条!\n");
        //打印并且走纸3行
        printAndLineFeed(esc, 3);

        return esc;
    }

    @Override
    public EscCommand makePosOrderEsc(PosOrderEntity posOrderEntity, boolean waitReceiptEnabled) {
        if (posOrderEntity == null) {
            return null;
        }

        if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
            ZLogger.d(JSONObject.toJSONString(posOrderEntity));
        }
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        /**打印 标题*/
        printAndLineFeed(esc, 1);
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        esc.addText(String.format("%s\n", MfhLoginService.get().getCurOfficeName()));
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

        /**打印头部*/
        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_LEFT));
        makeOneLine(esc, "设备编号:", 10, SharedPrefesManagerFactory.getTerminalId(), 21, false);
        makeOneLine(esc, "下单时间:", 10, TimeUtil.format(posOrderEntity.getUpdatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM), 21, false);
        makeOneLine(esc, "收银员:", 10, MfhLoginService.get().getHumanName(), 21, false);


        /**打印 商品明细*/
        printAndLineFeed(esc, 1);
        List<PosOrderItemEntity> needWaitEntities = new ArrayList<>();

        List<PosOrderItemEntity> posOrderItemEntities = CashierProvider.fetchOrderItems(posOrderEntity.getId());
        if (posOrderItemEntities != null && posOrderItemEntities.size() > 0) {
            makeThressLine(esc, "商品", "数量", "单位", "", "原价", "小计", "", "会员价", "小计", true);
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            for (PosOrderItemEntity entity : posOrderItemEntities) {
                if (entity.getNeedWait().equals(1)) {
                    needWaitEntities.add(entity);
                }
                makeThressLine(esc, entity.getSkuName(),
                        String.format("%.3f", entity.getBcount()),
                        entity.getUnit(),
                        entity.getBarcode(),
                        MUtils.formatDouble(entity.getFinalPrice()),
                        String.format("%.3f", entity.getFinalAmount()),
                        "",
                        MUtils.formatDouble(entity.getFinalCustomerPrice()),
                        String.format("%.3f", MathCompact.mult(entity.getFinalCustomerPrice(), entity.getBcount())),
                        true);
            }
        }

        /**
         * 打印合计信息
         * */
//        esc.addText("--------------------------------\n");//32个
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_RIGHT));
        esc.addText(String.format("%s%s\n",
                Printer.formatShort("原价合计:", 24, Printer.BLANK_GRAVITY.LEFT),
                Printer.formatShort(MUtils.formatDouble(posOrderEntity.getFinalAmount()), 8, Printer.BLANK_GRAVITY.LEFT)));

        //支付记录
        appendPayWays(esc, posOrderEntity.getId());

        /** 打印 结束语*/
        printAndLineFeed(esc, 2);//打印并且走纸3行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        addFooter(esc);
        printAndLineFeed(esc, 3);//打印并且走纸3行

        if (waitReceiptEnabled && needWaitEntities.size() > 0) {

            printAndLineFeed(esc, 2);//打印并且走纸3行
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_CENTER));
            esc.addText("客户联\n");
            esc.addText("--------------------------------\n");//32个
            esc.addText(String.format("%d\n", posOrderEntity.getFlowId()));

            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            for (PosOrderItemEntity entity : needWaitEntities) {
                makeOrderItem1(esc, entity.getSkuName(),
                        MUtils.formatDouble(null, null, entity.getBcount(), "", "", entity.getUnit()),
                        MUtils.formatDouble(entity.getFinalAmount(), ""));
            }

            printAndLineFeed(esc, 5);//打印并且走纸3行
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_CENTER));
            esc.addText("厨房联\n");
            esc.addText("--------------------------------\n");//32个

            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            for (PosOrderItemEntity entity : needWaitEntities) {
                makeOrderItem1(esc, entity.getSkuName(),
                        MUtils.formatDouble(null, null, entity.getBcount(), "", "", entity.getUnit()),
                        MUtils.formatDouble(entity.getFinalAmount(), ""));

            }
            printAndLineFeed(esc, 3);//打印并且走纸3行
        }

        return esc;
    }


    @Override
    public EscCommand makePrepareOrderEsc(ScOrder scOrder) {
        if (scOrder == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2));
//打印并且走纸3行
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));

        // 一维条码：设置条码可识别字符位置在条码下方
        Barcode barcode1 = new Barcode(PrinterConstants.BarcodeType.CODE128, 3,
                80, 2, scOrder.getBarcode());
        esc.addUserCommand(barcode1.getBarcodeData());

        //        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
        esc.addText("拣货单");
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行

        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_LEFT));
        esc.addText(String.format("客户:%s/%s \n",
                scOrder.getReceiveName(), scOrder.getReceivePhone()));
        esc.addText(String.format("配送地址:%s \n", scOrder.getAddress()));
        esc.addText(String.format("下单时间：%s \n",
                TimeUtil.format(scOrder.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("配送时间：%s \n",
                TimeUtil.format(scOrder.getDueDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("备注:%s \n", scOrder.getRemark()));
        printAndLineFeed(esc, 1);


        Double goodsAmount = 0D;
        List<ScOrderItem> items = scOrder.getItems();
        if (items != null && items.size() > 0) {
            /**打印 商品明细*/
            esc.addText("品名               数量   小计\n");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            //设置打印左对齐
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            esc.addText("--------------------------------\n");//32个
            for (ScOrderItem item : items) {
                makeOrderItem1(esc, item.getProductName(),
                        MUtils.formatDouble(null, null, item.getBcount(), "", "", item.getUnitName()),
                        MUtils.formatDouble(item.getAmount(), ""));

                goodsAmount += item.getAmount();
            }
        }
        esc.addText("--------------------------------\n");//32个
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_RIGHT));
        esc.addText(MUtils.formatDouble("商品金额", " ", goodsAmount, "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("配送费", " ", scOrder.getTransFee(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("优惠券", " ", scOrder.getDisAmount(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("订单金额", " ", scOrder.getAmount(), "", null, null));
        esc.addText("\n");


        /**
         * 打印 结束语
         * */
        printAndLineFeed(esc, 2);
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_LEFT));
        addFooter(esc);
        printAndLineFeed(esc, 3);//打印并且走纸3行

        return esc;
    }


    @Override
    public EscCommand makeGroupBuyOrderEsc(GroupBuyOrder scOrder) {
        if (scOrder == null) {
            return null;
        }
        EscCommand esc = new EscCommand();

        try {
            esc.addUserCommand(EmbPrinter.initPrinter());

            printAndLineFeed(esc, 1);
//打印并且走纸3行
            //设置打印居中
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
            //设置为倍高倍宽
            esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
            /**打印 标题*/
            esc.addText("提货单");
            //取消倍高倍宽
            esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));


            printAndLineFeed(esc, 2);
            //设置打印左对齐
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
            /**打印 订购日期*/
            makeOneLine(esc, "门店：", 8, MfhLoginService.get().getCurOfficeName(), 23, false);
            makeOneLine(esc, "拣货日期：", 8, TimeUtil.format(scOrder.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM), 23, false);
            makeOneLine(esc, "类型：", 8, scOrder.getBtypename(), 23, false);
            makeOneLine(esc, "会员账号：", 8, scOrder.getReceivePhone(), 23, false);

            printAndLineFeed(esc, 1);

            Double totalAmount = 0D;
            List<GroupBuyOrderItem> items = scOrder.getItems();
            if (items != null && items.size() > 0) {
                makeOneLine(esc, "商品", 14, "数量", 8, "金额", 8, true);
//                esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
                for (GroupBuyOrderItem item : items) {
                    totalAmount += item.getAmount();

                    makeOneLine(esc, item.getProductName(), 14,
                            MUtils.formatDouble(item.getBcount(), "", "", "件"), 8,
                            MUtils.formatDouble(item.getAmount()), 8, false);
//                esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
                }
            }

            esc.addText("--------------------------------\n");//32个
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_RIGHT));
            esc.addText(MUtils.formatDouble("合计", ": ", totalAmount, "", null, null));
            esc.addText("\n");


            /**打印 结束语*/
            printAndLineFeed(esc, 2);
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
            addFooter(esc);
            printAndLineFeed(esc, 3);
//打印并且走纸3行
        } catch (Exception e) {
            e.printStackTrace();
        }

        return esc;
    }

    @Override
    public EscCommand makeStockOutOrderEsc(List<StockOutItem> orderItems) {
        if (orderItems == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2));
//打印并且走纸3行
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        /**打印 标题*/
        esc.addText("商品取件配送单\n");
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));


        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        /**打印 订购日期*/
        esc.addText(String.format("日期:%s \n", TimeCursor.InnerFormat.format(TimeUtil.getCurrentDate())));
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));

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
//        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        if (orderItems.size() > 0) {
            for (StockOutItem entity : orderItems) {
//                /**打印 商品明细*/
//                makeTemp(esc, entity.getProductName(), String.format("%.2f", entity.getAmount()));

                esc.addText(String.format("%s/%s\n", entity.getHumanName(), entity.getHumanPhone()));
                esc.addText(String.format("%s/%s\n", entity.getBarcode(), entity.getTransportName()));
            }

            esc.addText("--------------------------------\n");//32个
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_RIGHT));
            esc.addText(String.format("总计:%d件\n", orderItems.size()));
        }

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        esc.addText("签字：\n");
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));


        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER
        ));
        /**打印 结束语*/
        addFooter(esc);
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 3));
//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makeDailySettleEsc(DailysettleInfo dailysettleInfo) {
        if (dailysettleInfo == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        //打印并且走纸3行
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2));

        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        //显示当前网点名称
        esc.addText(dailysettleInfo.getOfficeName());
//        //取消倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        /**打印 抬头：日结信息*/
//        esc.addText(String.format("班次：%d \n", handOverBill.getShiftId()));
        esc.addText(String.format("日结人:%s\n", dailysettleInfo.getHumanName()));
        esc.addText(String.format("日结时间：%s \n",
                TimeUtil.format(dailysettleInfo.getCreatedDate(), TimeCursor.InnerFormat)));
        esc.addText(String.format("设备编号：%s \n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 日结单明细*/

//        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        List<AggItem> aggItems = dailysettleInfo.getAggItems();
        if (aggItems != null && aggItems.size() > 0) {
            esc.addText("产品线            数量    金额\n");
            printDailySettleAggItem(esc, 1, aggItems);
        }

        esc.addText("--------------------------------\n");//32个
        List<AccItem> accItems1 = dailysettleInfo.getAccItems();
        if (accItems1 != null && accItems1.size() > 0) {
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
            esc.addText("支付渠道            数量    金额\n");
            printDailySettleAccItem(esc, 1, accItems1);

        }

        esc.addText("--------------------------------\n");//32个
        List<AccItem> accItems2 = dailysettleInfo.getAccItems2();
        if (accItems2 != null && accItems2.size() > 0) {
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
            esc.addText("支付渠道            数量    金额\n");
            printDailySettleAccItem(esc, 1, accItems2);
        }

        /**
         * 打印 结束语
         * */
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));

        addFooter(esc);
//        esc.addPrintAndLineFeed();
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 3));
//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makeHandoverEsc(HandOverBill handOverBill) {
        if (handOverBill == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2));
//打印并且走纸3行
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题：网点名称*/
        esc.addText(MfhLoginService.get().getCurOfficeName());
//        //取消倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        /**打印 抬头：交接班信息*/
        esc.addText(String.format("班次：%d \n", handOverBill.getShiftId()));
        esc.addText(String.format("交班人:%s\n", handOverBill.getHumanName()));
        //打印上班时间，来核对交班信息
        esc.addText(String.format("交班时间：%s \n",
                TimeCursor.InnerFormat.format(handOverBill.getEndDate())));
        esc.addText(String.format("设备编号：%s \n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 交接单明细*/
        esc.addText("业务类型            数量    金额\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        printDailySettleAggItem(esc, 1, handOverBill.getAggItems());

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
        esc.addText("支付类型            数量    金额\n");
        printDailySettleAccItem(esc, 1, handOverBill.getAccItems());
        /**
         * 打印合计信息
         * */
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("账户新增:%.2f\n", handOverBill.getTurnover() - handOverBill.getCash()));
        esc.addText(String.format("现金收取:%.2f\n", handOverBill.getCash()));
        esc.addText(String.format("原价金额:%.2f\n", handOverBill.getOrigionAmount()));
        esc.addText(String.format("营业额合计:%.2f\n", handOverBill.getTurnover()));

        /**
         * 打印 结束语
         * */
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        esc.addText("辛苦了!\n");
        esc.addText("祝您生活愉快!\n");
//        esc.addPrintAndLineFeed();
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 3));
//打印并且走纸3行

        return esc;
    }

    @Override
    public void makeHandoverTemp(EscCommand esc, String name, String bcount, String amount) {
        if (esc == null) {
            return;
        }
        esc.addUserCommand(EmbPrinter.initPrinter());

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));

        //计算名称行数
        if (Printer.getLength(name) > 20) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_RIGHT));
            esc.addText(String.format("%s%s",
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 6, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s",
                    Printer.formatShort(name, 20, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 6, Printer.BLANK_GRAVITY.LEFT));
//            ZLogger.d("printText:" + printText);
            esc.addText(printText);
        }
        esc.addText("\n");
    }

    /**
     * 打印订单
     */
    @Override
    public EscCommand makePosOrderEsc1(GoodsOrder curOrder) {
        EscCommand esc = new EscCommand();

        esc.addUserCommand(EmbPrinter.initPrinter());
        /**打印 标题*/
        printAndLineFeed(esc, 1);//打印并且走纸3行
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        esc.addText(String.format("%s\n", MfhLoginService.get().getCurOfficeName()));
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

        /**打印头部*/
        printAndLineFeed(esc, 1);//进纸一行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_LEFT));
//        esc.addText(String.format("订单号:%d \n", curOrder.getId()));
        makeOneLine(esc, "日期：", 10, TimeUtil.format(curOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS), 21, false);
        makeOneLine(esc, "订单金额：", 10, MUtils.formatDouble(curOrder.getAmount()), 21, false);
        makeOneLine(esc, "支付方式：", 10, WayType.name(curOrder.getPayType()), 21, false);

        /**打印 商品明细*/
        printAndLineFeed(esc, 1);
        List<GoodsOrderItem> oderItems = curOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            esc.addText("--------------------------------\n");//32个
//            esc.addText("品名         单价   数量  小计\n");
            makeOrderItem2(esc, "品名", "单价", "数量", "小计");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            for (GoodsOrderItem entity : oderItems) {
                makeOrderItem2(esc,
                        entity.getProductName(),
                        MUtils.formatDouble(entity.getPrice(), ""),
                        MUtils.formatDouble(entity.getBcount(), ""),
                        MUtils.formatDouble(entity.getAmount(), ""));
            }
        }

        /**打印 结束语*/
        esc.addText("--------------------------------\n");//32个
        printAndLineFeed(esc, 1);
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        addFooter(esc);
        printAndLineFeed(esc, 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 外部配送单
     */
    @Override
    public EscCommand makePosOrderEsc2(GoodsOrder posOrder) {
        EscCommand esc = new EscCommand();

        esc.addUserCommand(EmbPrinter.initPrinter());

        /**打印 标题*/
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        printAndLineFeed(esc, 1);//打印并且走纸2行
        addCODE128(esc, posOrder.getOuterNo());//打印 一维条码
        // 设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        esc.addText("配送单\n");
        esc.addText(String.format("%s\n", PosType.name(posOrder.getSubType())));
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

        /**打印 头部*/
        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_LEFT));
        //订单编号
//        esc.addText(String.format("订单号: %d \n", posOrder.getId()));
        //发货时间
        makeOneLine(esc, "下单时间：", 10, TimeUtil.format(posOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS), 21, false);


        /**打印 订单明细*/
        printAndLineFeed(esc, 1);
        List<GoodsOrderItem> oderItems = posOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            makeOrderItem5(esc, "品名", "单价", "数量", "小计");
            esc.addText("--------------------------------\n");//32个
//            esc.addText("品名           数量   小计\n");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            for (GoodsOrderItem entity : oderItems) {
                makeOrderItem5(esc,
                        entity.getProductName(),
                        MUtils.formatDouble(entity.getPrice()),
                        MUtils.formatDouble(entity.getBcount()),
                        MUtils.formatDouble(entity.getAmount()));
            }
        }

        /**打印 尾部*/
        esc.addText("--------------------------------\n");//32个
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_RIGHT));
        esc.addText(String.format("订单金额: %s\n", MUtils.formatDouble(posOrder.getAmount())));

        /**打印 结束语*/
        printAndLineFeed(esc, 1);
        addFooter(esc);
        //打印并且走纸3行
        printAndLineFeed(esc, 3);

        return esc;
    }

    @Override
    public EscCommand makeSendOrder3pEsc(PosOrderEntity posOrderEntity) {
        if (posOrderEntity == null) {
            return null;
        }

        EscCommand esc = new EscCommand();

        esc.addUserCommand(EmbPrinter.initPrinter());

        /**打印 标题*/
        printAndLineFeed(esc, 1);//打印并且走纸2行
        addCODE128(esc, posOrderEntity.getOuterTradeNo());//打印 一维条码
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        esc.addText("配送单\n");
        esc.addText(String.format("%s\n", PosType.name(posOrderEntity.getSubType())));
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));


        /**打印头部*/
        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_LEFT));
        makeOneLine(esc, "下单时间：", 10, TimeUtil.format(posOrderEntity.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMM), 21, false);


        /**打印 商品明细*/
        printAndLineFeed(esc, 1);
        List<PosOrderItemEntity> posOrderItemEntityList = CashierProvider.fetchOrderItems(posOrderEntity.getId());
        if (posOrderItemEntityList != null && posOrderItemEntityList.size() > 0) {
//            esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
            makeOrderItem5(esc, "品名", "单价", "数量", "小计");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            esc.addText("--------------------------------\n");//32个
            for (PosOrderItemEntity entity : posOrderItemEntityList) {
                makeOrderItem5(esc,
                        entity.getSkuName(),
                        MUtils.formatDouble(entity.getFinalPrice()),
                        MUtils.formatDouble(entity.getBcount()),
                        MUtils.formatDouble(entity.getFinalAmount()));
            }
        }

        /**
         * 打印合计信息
         * */
        esc.addText("--------------------------------\n");//32个
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_RIGHT));
        esc.addText(String.format("订单金额:%s\n", MUtils.formatDouble(posOrderEntity.getFinalAmount())));

        /** 打印 结束语*/
        printAndLineFeed(esc, 2);//打印并且走纸3行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        addFooter(esc);
        printAndLineFeed(esc, 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 平台配送单
     */
    @Override
    public EscCommand makePosOrderEsc3(GoodsOrder posOrder) {
        EscCommand esc = new EscCommand();

        try {
            esc.addUserCommand(EmbPrinter.initPrinter());


            /**打印 标题*/
            //打印并且走纸2行
            printAndLineFeed(esc, 1);
            //设置打印居中
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_CENTER));
            /**打印 一维条码*/
            addCODE128(esc, posOrder.getBarcode());
            //设置为倍高倍宽
            esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
            esc.addText("配送单\n");
            //取消倍高倍宽
            esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

            /**打印 订单基本信息*/
            printAndLineFeed(esc, 1);
            //设置打印左对齐
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            //客户
            makeOneLine(esc, "客户：", 10, posOrder.getBuyerName(), 21, false);
            //配送地址
            makeOneLine(esc, "配送地址：", 10, posOrder.getAddress(), 21, false);
            //下单时间
            makeOneLine(esc, "下单时间：", 10, TimeUtil.format(posOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS), 21, false);
            //配送时间
            makeOneLine(esc, "配送时间：", 10, TimeUtil.getCaptionTimeV2(posOrder.getDueDate(), false), 21, false);
            //备注
            makeOneLine(esc, "备注：", 10, posOrder.getRemark(), 21, false);
            //买手
            makeOneLine(esc, "买手：", 10, String.format("%s/%s",
                    posOrder.getServiceHumanName(), posOrder.getServiceMobile()), 21, false);


            /**打印 订单明细*/
            printAndLineFeed(esc, 1);
            List<GoodsOrderItem> oderItems = posOrder.getItems();
            Double goodsAmount = 0D, commitGoodsAmount = 0D;
            if (oderItems != null && oderItems.size() > 0) {
                makeOrderItem5(esc, "品名", "单价", "数量", "小计");
                esc.addText("--------------------------------\n");//32个
//            esc.addText("品名           数量   小计\n");
                esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
                for (GoodsOrderItem entity : oderItems) {
                    goodsAmount += MathCompact.mult(entity.getPrice(), entity.getBcount());
                    commitGoodsAmount += MathCompact.mult(entity.getPrice(), entity.getCommitCount());

                    makeOrderItem5(esc,
                            entity.getProductName(),
                            MUtils.formatDouble(entity.getPrice()),
                            MUtils.formatDouble(entity.getCommitCount()),
                            MUtils.formatDouble(entity.getCommitAmount()));
                }
            }

            esc.addText("--------------------------------\n");//32个
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_RIGHT));

            esc.addText(MUtils.formatDouble("订单金额", ": ", posOrder.getAmount(), "", null, null));
            esc.addText("\n");
            esc.addText(MUtils.formatDouble("配送费", ": ", posOrder.getTransFee(), "", null, null));
            esc.addText("\n");
            esc.addText(MUtils.formatDouble("优惠券", ": ", posOrder.getDisAmount(), "", null, null));
            esc.addText("\n");
            esc.addText(MUtils.formatDouble("商品金额:", "", goodsAmount, "", "", ""));
            esc.addText(MUtils.formatDouble("商品金额:", "", commitGoodsAmount, "", "", ""));
            Double disAmount = MathCompact.sub(commitGoodsAmount, goodsAmount);
            if (disAmount < 0) {
                esc.addText(MUtils.formatDouble("   差额:-", "", disAmount, "", "", ""));
            } else {
                esc.addText(MUtils.formatDouble("   差额:+", "", disAmount, "", "", ""));
            }

            /**打印 结束语*/
            printAndLineFeed(esc, 2);
            addFooter(esc);
            //打印并且走纸3行
            printAndLineFeed(esc, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return esc;
    }

    /**
     * 组货打印配送单
     */
    @Override
    public EscCommand makeSendOrderEsc(ScOrder scOrder) {
        if (scOrder == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        printAndLineFeed(esc, 2);
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        addCODE128(esc, scOrder.getBarcode());

        printAndLineFeed(esc, 1);//进纸一行
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));//设置为倍高倍宽
        esc.addText("配送单");
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));//设置为倍高倍宽
        printAndLineFeed(esc, 2);//打印并且走纸3行

        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_LEFT));
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
        printAndLineFeed(esc, 2);
//打印并且走纸3行

        /**打印 商品明细*/
        //设置打印左对齐
        Double goodsAmount = 0D, commitGoodsAmount = 0D;
        List<ScOrderItem> items = scOrder.getItems();
        if (items != null && items.size() > 0) {
//            esc.addText("品名                数量   小计\n");
            esc.addText("品名            单价 数量   小计\n");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            esc.addText("--------------------------------\n");//32个
            for (ScOrderItem item : items) {
                goodsAmount += MathCompact.mult(item.getPrice(), item.getBcount());
                commitGoodsAmount += MathCompact.mult(item.getPrice(), item.getQuantityCheck());

                makeOrderItem5(esc,
                        item.getProductName(),
                        String.format("%.2f", item.getPrice()),
                        String.format("*%.2f", item.getQuantityCheck()),
                        String.format("%.2f", MathCompact.mult(item.getPrice(), item.getQuantityCheck())));
            }
        }

        esc.addText("--------------------------------\n");//32个
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_RIGHT));
        esc.addText(MUtils.formatDouble("订单金额", ": ", scOrder.getAmount(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("配送费", ": ", scOrder.getTransFee(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("优惠券", ": ", scOrder.getDisAmount(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("商品金额", ": ", goodsAmount, "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("拣货金额", ": ", commitGoodsAmount, "", null, null));
        esc.addText("\n");
        Double disAmount = MathCompact.sub(commitGoodsAmount, goodsAmount);
        if (disAmount < 0) {
            esc.addText(String.format("   差额: -%.2f\n", disAmount));
        } else {
            esc.addText(String.format("   差额: +%.2f\n", disAmount));
        }
        esc.addText("\n");

        /**
         * 打印 结束语
         * */
        printAndLineFeed(esc, 2);
//打印并且走纸3行
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        addFooter(esc);
        printAndLineFeed(esc, 3);
//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makeGoodsFlowEsc(List<GoodsItem> goodsItems) {
        if (goodsItems == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        //打印并且走纸3行
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2));

        //设置打印居中
//        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        //显示当前网点名称
//        esc.addText(dailysettleInfo.getOfficeName());
//        //取消倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        /**打印 抬头：日结信息*/
//        esc.addText(String.format("班次：%d \n", handOverBill.getShiftId()));
//        esc.addText(String.format("日结人:%s\n", dailysettleInfo.getHumanName()));
//        esc.addText(String.format("日结时间：%s \n",
//                TimeUtil.format(dailysettleInfo.getCreatedDate(), TimeCursor.InnerFormat)));
//        esc.addText(String.format("设备编号：%s \n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 日结单明细*/
        esc.addText("业务类型            数量    金额\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        for (GoodsItem goodsItem : goodsItems) {
            makeHandoverTemp(esc,
                    String.format("%s/%s", goodsItem.getBarcode(),
                            goodsItem.getProductName()),
                    MUtils.formatDouble(goodsItem.getBcount()),
                    MUtils.formatDouble(goodsItem.getFactAmount()));
        }

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行


        return esc;
    }

    @Override
    public EscCommand makeHeaderEsc(String title, HashMap<String, String> headers) {
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2));

        /**打印 标题*/
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        esc.addText(title + "\n");

        //打印表头内容
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                prepareHeader1(esc, key, headers.get(key), false);
            }
        }

        printAndLineFeed(esc, 1);
        return esc;
    }

    @Override
    public EscCommand makeFooterEsc(HashMap<String, String> footers) {
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
//        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2));

        //打印表头内容
        if (footers != null && footers.size() > 0) {
            for (String key : footers.keySet()) {
                prepareFooter1(esc, key, footers.get(key), false);
            }
        }

        printAndLineFeed(esc, 1);
        return esc;
    }


    @Override
    public EscCommand makeReconcileHeaderEsc(String title, HashMap<String, String> headers) {
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2));

        /**打印 标题*/
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        esc.addText(title + "\n");

        //打印表头内容
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                prepareHeader1(esc, key, headers.get(key), false);
            }
        }

        printAndLineFeed(esc, 1);

        prepareContent1(esc, "商品", "小计", "营业额", true);

        return esc;
    }

    @Override
    public EscCommand makeReconcileContentEsc(List<ProductAggDate> goodsItems) {
        if (goodsItems == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
//        printAndLineFeed(esc, 1);
        for (ProductAggDate item : goodsItems) {
            prepareContent1(esc, item.getTenantSkuIdWrapper(),
                    MUtils.formatDouble(item.getProductNum()),
                    MUtils.formatDouble(item.getTurnover()), true);
        }

        return esc;
    }


    @Override
    public EscCommand makeReconcileFooterEsc(Double turnover) {
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        printAndLineFeed(esc, 1);
        prepareFooter1(esc, "销售额合计：",
                MUtils.formatDouble(turnover), false);
        printAndLineFeed(esc, 2);

        return esc;
    }

    @Override
    public EscCommand makeCustomerGoodsOrderContentEsc(List<GoodsOrder> goodsOrders) {
        if (goodsOrders == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
//        printAndLineFeed(esc, 1);
        for (GoodsOrder item : goodsOrders) {
            esc.addText(String.format("%s\n", TimeUtil.format(item.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
            if (BizType.POS.equals(item.getBtype()) && !PosType.POS_STANDARD.equals(item.getSubType())) {
                makeOneLine(esc, PosType.name(item.getSubType()), 14, Printer.BLANK_GRAVITY.RIGHT,
                        WayType.name(item.getPayType()), 8, Printer.BLANK_GRAVITY.RIGHT,
                        MUtils.formatDouble(item.getAmount()), 8, Printer.BLANK_GRAVITY.LEFT, true);
            } else {
                makeOneLine(esc, BizType.name(item.getBtype()), 14, Printer.BLANK_GRAVITY.RIGHT,
                        WayType.name(item.getPayType()), 8, Printer.BLANK_GRAVITY.RIGHT,
                        MUtils.formatDouble(item.getAmount()), 8, Printer.BLANK_GRAVITY.LEFT, true);
            }
        }

        return esc;
    }

    @Override
    public EscCommand makeCustomerAccountFlowContentEsc(List<CommonAccountFlow> accountFlows) {
        if (accountFlows == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
//        printAndLineFeed(esc, 1);
        for (CommonAccountFlow item : accountFlows) {
            esc.addText(String.format("%s\n", TimeUtil.format(item.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
            makeOneLine(esc, BizType.name(item.getBizType()), 16, Printer.BLANK_GRAVITY.RIGHT,
                    MUtils.formatDouble(item.getConCash()), 14, Printer.BLANK_GRAVITY.CENTER, true);
        }


        return esc;
    }

    @Override
    public EscCommand makeTestPageEsc() {
        try {
            EscCommand esc = new EscCommand();
            esc.addUserCommand(EmbPrinter.initPrinter());

            printAndLineFeed(esc, 2);
//打印并且走纸3行
            //设置打印居中
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
//        //设置为倍高倍宽
            esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
            esc.addText("打印测试");
//        //取消倍高倍宽
            esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));
            printAndLineFeed(esc, 1);

            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
            esc.addText("Welcome to use Gprinter!\n");   //  打印文字
            printAndLineFeed(esc, 1);

//        /*打印繁体中文  需要打印机支持繁体字库*/
//        String message = GPrinterAgent.SimToTra("佳博票据打印机\n");
//        //	esc.addText(message,"BIG5");
//        esc.addText(message,"GB2312");
//        esc.addPrintAndLineFeed();


            /**打印 机器设备号＋订单号*/
            esc.addText(String.format("%s NO.%s \n", SharedPrefesManagerFactory.getTerminalId(),
                    MUtils.getOrderBarCode()));
            /**打印 订购日期*/
            esc.addText(String.format("%s \n", GPrinter.DATE_FORMAT.format(TimeUtil.getCurrentDate())));
            //5个数字等于3个汉字（1个数字＝3/5个汉字）
            esc.addText("--------------------------------\n");//32(正确)
            esc.addText("01234567890123456789012345678901\n");//32(正确)
            esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
            esc.addText("零一二三四五六七八九零一二三四五六七八\n");//19.2(错误)
            esc.addText("货号/品名       单价 数量 小计\n");
            esc.addText("货号/品名       单价 数量   小计\n");
            esc.addText("货号/品名      00.0100.0200.03\n");//32=17+5+5+5

//            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            //设置为倍高倍宽
//            esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                    EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
            makeTestTemp(esc, String.format("%s/%s", MUtils.getOrderBarCode(),
                    StringUtils.genNonceChinease(8)),
                    "12.34", "23.45", "34.56");
            makeTestTemp(esc, String.format("%s/%s", MUtils.getOrderBarCode(),
                    StringUtils.genNonceStringByLength(8)),
                    "12.34", "23.45", "34.56");

            printAndLineFeed(esc, 1);

/*打印一维条码code128*/
            esc.addText("Print code128\n");   //  打印文字
            addCODE128(esc, MUtils.getOrderBarCode());

        /*QRCode 命令打印
        此命令只在支持 QRCode 命令打印的机型才能使用。
        在不支持二维码指令打印的机型上,则需要发送二维条码图片
        */
            esc.addText("Print QRcode\n");   //  打印文字
            Barcode barcode = new Barcode(PrinterConstants.BarcodeType.QRCODE, 2, 3, 6,
                    MobileApi.DOMAIN);
            esc.addUserCommand(barcode.getBarcodeData());
//打印 QRCode
            printAndLineFeed(esc, 1);

            /**打印 APP LOGO*/
//            esc.addText("Print bitmap!\n");   //  打印文字
//            Bitmap b = BitmapFactory.decodeResource(CashierApp.getAppContext().getResources(),
//                    R.mipmap.ic_launcher);
//            esc.addRastBitImage(b, b.getWidth(), 0);


//            /*打印图片*/
//            try {
//                Bitmap QRCodeBmp = QrCodeUtils.Create2DCode("www.manfenjiayuan.cn");
//                esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
//                esc.addPrintAndLineFeed();
//            } catch (WriterException e) {
//                e.printStackTrace();
//            }

            //开钱箱
//            esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
            //立即开钱箱
            //esc.addGeneratePluseAtRealtime(LabelCommand.FOOT.F2, (byte) 8);

            /**打印 订单号条形码code128图片*/
//        try {
//            Bitmap QRCodeBmp = QrCodeUtils.CreateCode128ForGPrinter(String.valueOf(orderEntity.getId()), 300, 60);
//            esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
//            esc.addPrintAndLineFeed();
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }

            return esc;
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.ef(e.toString());
            return null;
        }
    }

    @Override
    public void makeTestTemp(EscCommand esc, String name, String price, String bcount, String amount) {
        if (esc == null) {
            return;
        }

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_LEFT));

        //最多显示17*0.6=10.2个汉字
        if (Printer.getLength(name) > 16) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_RIGHT));
            esc.addText(String.format("%s%s%s",
                    Printer.formatShort(price, 5, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 5, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 6, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s%s",
                    Printer.formatShort(name, 16, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(price, 5, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 5, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 6, Printer.BLANK_GRAVITY.LEFT));
            esc.addText(printText);

//            ZLogger.d("printText:" + printText);
        }
        esc.addText("\n");
    }

    /**
     * 订单明细模版1
     */
    @Override
    public void makeOrderItem1(EscCommand esc, String name, String unit, String bcount) {
        if (esc == null) {
            return;
        }
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));

        //最多显示17*0.6=10.2个汉字
        if (Printer.getLength(name) > 20) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            //设置打印左对齐
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_RIGHT));

            esc.addText(String.format("%s%s",
                    Printer.formatShort(unit, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s",
                    Printer.formatShort(name, 20, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(unit, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.LEFT));
            esc.addText(printText);

//            ZLogger.d("printText:" + printText);
        }
        esc.addText("\n");
    }

    @Override
    public void makeOrderItem2(EscCommand esc, String name, String price, String bcount, String amount) {
        if (esc == null) {
            return;
        }

        //最多显示17*0.6=10.2个汉字
        if (Printer.getLength(name) > 13) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_RIGHT));//设置打印左对齐
            esc.addText(String.format("%s%s%s",
                    Printer.formatShort(price, 6, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(amount, 7, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s%s",
                    Printer.formatShort(name, 13, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(price, 6, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(amount, 7, Printer.BLANK_GRAVITY.LEFT));
            esc.addText(printText);
        }
        esc.addText("\n");
    }
}
