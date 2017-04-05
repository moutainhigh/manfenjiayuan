package com.bingshanguxue.cashier.hardware.printer.emb;

import com.alibaba.fastjson.JSONObject;
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
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.api.pmcstock.GoodsItem;
import com.mfh.framework.api.pmcstock.PosOrder;
import com.mfh.framework.api.pmcstock.PosOrderItem;
import com.mfh.framework.api.pmcstock.StockOutItem;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import java.util.ArrayList;
import java.util.Date;
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
        esc.addText(String.format("订单日期：%s \n", TimeUtil.format(new Date(),
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

        if (SharedPrefesManagerFactory.isSuperPermissionGranted()){
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
        esc.addText(String.format("设备编号:%s\n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText(String.format("下单时间：%s \n",
                TimeUtil.format(posOrderEntity.getUpdatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("收银员:%s/%s\n",
                MfhLoginService.get().getHumanName(), MfhLoginService.get().getTelephone()));


        /**打印 商品明细*/
        printAndLineFeed(esc, 1);
        List<PosOrderItemEntity> needWaitEntities = new ArrayList<>();;

        List<PosOrderItemEntity> posOrderItemEntities = CashierAgent.fetchOrderItems(posOrderEntity);
        if (posOrderItemEntities != null && posOrderItemEntities.size() > 0) {
//            esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
//            esc.addText("品名            单价 数量   小计\n");
            makeOrderItem7(esc, "商品", "原价", "会员价", "数量", "小计");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            esc.addText("--------------------------------\n");//32个
            for (PosOrderItemEntity entity : posOrderItemEntities) {
                if (entity.getNeedWait().equals(1)){
                    needWaitEntities.add(entity);
                }
//                makeOrderItem2(esc, entity.getSkuName(),
//                        String.format("%.2f", entity.getFinalPrice()),
//                        String.format("%.2f", entity.getBcount()),
//                        String.format("%.2f", entity.getFinalAmount()));
                makeOrderItem7(esc, entity.getSkuName(),
                        String.format("%.2f", entity.getFinalPrice()),
                        String.format("%.2f", entity.getCustomerPrice()),
                        String.format("%.3f%s", entity.getBcount(), entity.getUnit()),
                        String.format("%.3f", entity.getFinalAmount()));
                esc.addText("--------------------------------\n");//32个
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
                Printer.formatShort(String.format("%.2f", posOrderEntity.getFinalAmount()), 8, Printer.BLANK_GRAVITY.LEFT)));

        //支付记录
        appendPayWays(esc, posOrderEntity.getId());

        /** 打印 结束语*/
        printAndLineFeed(esc, 2);//打印并且走纸3行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));
        addFooter(esc);
        printAndLineFeed(esc, 3);//打印并且走纸3行

        if (waitReceiptEnabled && needWaitEntities.size() > 0){

            printAndLineFeed(esc, 2);//打印并且走纸3行
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_CENTER));
            esc.addText("客户联\n");
            esc.addText("--------------------------------\n");//32个
            esc.addText(String.format("%d\n", posOrderEntity.getFlowId()));

            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            for (PosOrderItemEntity entity : needWaitEntities){
                makeOrderItem1(esc, entity.getSkuName(),
                        MUtils.formatDouble(null, null, entity.getBcount(), "", "", entity.getUnit()),
                        MUtils.formatDouble(entity.getFinalAmount(), ""));
            }

            printAndLineFeed(esc, 5);//打印并且走纸3行
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_CENTER));              esc.addText("厨房联\n");
            esc.addText("--------------------------------\n");//32个

            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            for (PosOrderItemEntity entity : needWaitEntities){
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
        esc.addText(String.format("日期:%s \n", TimeCursor.InnerFormat.format(new Date())));
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
        esc.addText("业务类型            数量    金额\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        printDailySettleAggItem(esc, 1, dailysettleInfo.getAggItems());

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
        esc.addText("支付类型            数量    金额\n");
        printDailySettleAccItem(esc, 1, dailysettleInfo.getAccItems());

        /**
         * 打印合计信息
         * */
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("现金收取:%.2f\n", dailysettleInfo.getCash()));
        esc.addText(String.format("非现金收取:%.2f\n",
                dailysettleInfo.getTurnOver() - dailysettleInfo.getCash()));

        Double turnover = dailysettleInfo.getTurnOver();
        esc.addText(String.format("营业额合计:%.2f\n", turnover));

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
        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

//        List<HandoverAggItem> aggShiftList = handOverBill.getAggShiftList();
//        for (HandoverAggItem aggShift : aggShiftList){
//            makeHandoverTemp(esc, aggShift.get, String.format("%.2f", aggShift.getOrderNum()), String.format("%.2f", aggShift.getTurnover()));
//        }
        printDailySettleAggItem(esc, 1, handOverBill.getAggItems());

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行
        esc.addText("支付类型            数量    金额\n");
        printDailySettleAccItem(esc, 1, handOverBill.getAccItems());
        /**
         * 打印合计信息
         * */
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("账户新增:%.2f\n", handOverBill.getAmount() - handOverBill.getCash()));
        esc.addText(String.format("现金收取:%.2f\n", handOverBill.getCash()));
        esc.addText(String.format("营业额合计:%.2f\n", handOverBill.getAmount()));

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
    public EscCommand makePosOrderEsc1(PosOrder curOrder) {
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
        esc.addText(String.format("日期:%s \n",
                TimeUtil.format(curOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        esc.addText(String.format("订单金额:%.2f\n", curOrder.getAmount()));
        esc.addText(String.format("支付方式:%s\n", WayType.name(curOrder.getPayType())));

        /**打印 商品明细*/
        printAndLineFeed(esc, 1);
        List<PosOrderItem> oderItems = curOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            esc.addText("--------------------------------\n");//32个
//            esc.addText("品名         单价   数量  小计\n");
            esc.addText("品名            单价 数量   小计\n");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            for (PosOrderItem entity : oderItems) {
                makeOrderItem2(esc,
                        entity.getProductName(),
                        String.format("%.2f", entity.getPrice()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getAmount()));
//                makeOrderItem3(esc,
//                        String.format("%s", entity.getProductName()),
//                        String.format("%.2f", entity.getBcount()),
//                        String.format("%.2f", entity.getAmount()));
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
    public EscCommand makePosOrderEsc2(PosOrder posOrder) {
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
        esc.addText(String.format("下单时间: %s \n",
                TimeUtil.format(posOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));

        /**打印 订单明细*/
        printAndLineFeed(esc, 1);
        List<PosOrderItem> oderItems = posOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
//            esc.addText("品名                 数量   小计\n");
            esc.addText("品名            单价 数量   小计\n");
            esc.addText("--------------------------------\n");//32个
//            esc.addText("品名           数量   小计\n");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            for (PosOrderItem entity : oderItems) {
                makeOrderItem5(esc, entity.getProductName(),
                        String.format("%.2f", entity.getPrice()),
                        String.format("*%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getAmount()));
            }
        }

        /**打印 尾部*/
        esc.addText("--------------------------------\n");//32个
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_RIGHT));
        esc.addText(String.format("订单金额: %.2f\n", posOrder.getAmount()));

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
        esc.addText(String.format("下单时间：%s \n",
                TimeUtil.format(posOrderEntity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));

        /**打印 商品明细*/
        printAndLineFeed(esc, 1);
        List<PosOrderItemEntity> posOrderItemEntityList = CashierAgent.fetchOrderItems(posOrderEntity);
        if (posOrderItemEntityList != null && posOrderItemEntityList.size() > 0) {
//            esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
//            esc.addText("品名                 数量   小计\n");
            esc.addText("品名            单价 数量   小计\n");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                    PrinterConstants.Command.ALIGN_LEFT));
            esc.addText("--------------------------------\n");//32个
            for (PosOrderItemEntity entity : posOrderItemEntityList) {
//                String shortName = entity.getShortName();
//                String text1;
//                if (!StringUtils.isEmpty(shortName)){
//                    text1 = String.format("%s(%s)", entity.getSkuName(),entity.getShortName());
//                }
//                else{
//                    text1 = entity.getSkuName();
//                }
                makeOrderItem5(esc, entity.getSkuName(),
                        String.format("%.2f", entity.getFinalPrice()),
                        String.format("*%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getFinalAmount()));
            }
        }

        /**
         * 打印合计信息
         * */
        esc.addText("--------------------------------\n");//32个
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_RIGHT));
        esc.addText(String.format("订单金额:%.2f\n", posOrderEntity.getFinalAmount()));

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
    public EscCommand makePosOrderEsc3(PosOrder posOrder) {
        EscCommand esc = new EscCommand();

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
        esc.addText(String.format("客户: %s \n", posOrder.getBuyerName()));
        //配送地址
        esc.addText(String.format("配送地址: %s \n", posOrder.getAddress()));
        //下单时间
        esc.addText(String.format("下单时间: %s \n",
                TimeUtil.format(posOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        //配送时间
        esc.addText(String.format("配送时间: %s \n",
                TimeUtil.getCaptionTimeV2(posOrder.getDueDate(), false)));
        //备注
        esc.addText(String.format("备注: %s \n", posOrder.getRemark()));
        //买手
        esc.addText(String.format("买手: %s/%s \n",
                posOrder.getServiceHumanName(), posOrder.getServiceMobile()));


        /**打印 订单明细*/
        printAndLineFeed(esc, 1);
        List<PosOrderItem> oderItems = posOrder.getItems();
        Double goodsAmount = 0D;
        Double commitGoodsAmount = 0D;
        if (oderItems != null && oderItems.size() > 0) {
//            esc.addText("品名                 数量   小计\n");
            esc.addText("品名            单价 数量   小计\n");
            esc.addText("--------------------------------\n");//32个
//            esc.addText("品名           数量   小计\n");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            for (PosOrderItem entity : oderItems) {
                goodsAmount += MathCompact.mult(entity.getPrice(), entity.getBcount());
                commitGoodsAmount += MathCompact.mult(entity.getPrice(), entity.getCommitCount());

                makeOrderItem5(esc,
                        entity.getProductName(),
                        String.format("%.2f", entity.getPrice()),
                        String.format("*%.2f", entity.getCommitCount()),
                        String.format("%.2f", entity.getCommitAmount()));
            }
        }

        esc.addText("--------------------------------\n");//32个
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN,
                PrinterConstants.Command.ALIGN_CENTER));

        esc.addText(MUtils.formatDouble("订单金额", ": ", posOrder.getAmount(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("配送费", ": ", posOrder.getTransFee(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("优惠券", ": ", posOrder.getDisAmount(), "", null, null));
        esc.addText("\n");
        esc.addText(String.format("商品金额: %.2f\n", goodsAmount));
        esc.addText(String.format("拣货金额: %.2f\n", commitGoodsAmount));
        Double disAmount = MathCompact.sub(commitGoodsAmount, goodsAmount);
        if (disAmount < 0) {
            esc.addText(String.format("   差额: -%.2f\n", disAmount));
        } else {
            esc.addText(String.format("   差额: +%.2f\n", disAmount));
        }

        /**打印 结束语*/
        printAndLineFeed(esc, 2);
        addFooter(esc);
        //打印并且走纸3行
        printAndLineFeed(esc, 3);

        return esc;
    }

    /**
     * 组货打印配送单
     * */
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
                    String.format("%.2f", goodsItem.getBcount()),
                    String.format("%.2f", goodsItem.getFactAmount()));
        }

        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));//进纸一行

//        /**
//         * 打印合计信息
//         * */
//        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
//        esc.addText("--------------------------------\n");//32个
//        esc.addText(String.format("现金收取:%.2f\n", dailysettleInfo.getCash()));
//        esc.addText(String.format("非现金收取:%.2f\n",
//                dailysettleInfo.getTurnOver() - dailysettleInfo.getCash()));
//
//        Double turnover = dailysettleInfo.getTurnOver();
//        esc.addText(String.format("营业额合计:%.2f\n", turnover));
//
//        /**
//         * 打印 结束语
//         * */
//        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1));
//        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
//
//        addFooter(esc);
////        esc.addPrintAndLineFeed();
//        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 3));
//打印并且走纸3行

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
            esc.addText(String.format("%s \n", GPrinter.DATE_FORMAT.format(new Date())));
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
