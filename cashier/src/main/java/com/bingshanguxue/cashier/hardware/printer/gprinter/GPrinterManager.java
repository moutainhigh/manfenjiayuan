package com.bingshanguxue.cashier.hardware.printer.gprinter;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.hardware.printer.IPrinter;
import com.bingshanguxue.cashier.hardware.printer.Printer;
import com.bingshanguxue.cashier.hardware.printer.PrinterManager;
import com.bingshanguxue.cashier.hardware.printer.emb.EmbPrinter;
import com.bingshanguxue.cashier.hardware.printer.emb.PrinterConstants;
import com.bingshanguxue.cashier.model.wrapper.DailysettleInfo;
import com.bingshanguxue.cashier.model.wrapper.HandOverBill;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.ProductAggDate;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
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
import java.util.HashMap;
import java.util.List;


/**
 * 分离式打印机
 * Created by bingshanguxue on 6/22/16.
 */
public class GPrinterManager extends PrinterManager {

    private static GPrinterManager instance = null;

    /**
     * 返回 PrintManagerImpl 实例
     *
     * @return PrintManagerImpl
     */
    public static GPrinterManager getInstance() {
        if (instance == null) {
            synchronized (GPrinterManager.class) {
                if (instance == null) {
                    instance = new GPrinterManager();
                }
            }
        }

        return instance;
    }

    public GPrinterManager() {
        mPrinter = create();
    }

    @Override
    public IPrinter create() {
        return new GPrinter();
    }

    @Override
    public EscCommand makeTopupEsc(QuickPayInfo mQuickPayInfo, String outTradeNo) {
        if (mQuickPayInfo == null || StringUtils.isEmpty(outTradeNo)) {
            return null;
        }
        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        if (!StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())) {
            //显示当前网点名称
            esc.addText(MfhLoginService.get().getCurOfficeName());
        }
//        //取消倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 机器设备号＋订单号*/
        esc.addText(String.format("设备编号：%s \n", SharedPrefesManagerFactory.getTerminalId()));
        /**打印 日期*/
        esc.addText(String.format("订单日期：%s \n", TimeUtil.format(new Date(),
                TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("支付方式：%s \n", WayType.name(mQuickPayInfo.getPayType())));
        esc.addText(String.format("流水号  ：%s \n", outTradeNo));
        esc.addText(String.format("金额   ：%.2f \n", mQuickPayInfo.getAmount()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**
         * 打印 结束语
         * */
        printAndLineFeed(esc, 1);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        esc.addText("请注意保管好您的交易凭条!\n");
        printAndLineFeed(esc, 3);//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makePosOrderEsc(PosOrderEntity posOrderEntity, boolean waitReceiptEnabled) {
        if (posOrderEntity == null) {
            return null;
        }
        try {
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.d(String.format("打印收银小票: %s", JSONObject.toJSONString(posOrderEntity)));
            }
            EscCommand esc = new EscCommand();

            /**打印 标题*/
            printAndLineFeed(esc, 1);
            //设置打印居中
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
            esc.addText(String.format("%s\n", MfhLoginService.get().getCurOfficeName()));
            //取消倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

            /**打印头部*/
            printAndLineFeed(esc, 1);//进纸一行
            //设置打印左对齐
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            esc.addText(String.format("设备编号:%s\n", SharedPrefesManagerFactory.getTerminalId()));
            esc.addText(String.format("下单时间：%s \n",
                    TimeUtil.format(posOrderEntity.getUpdatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
            esc.addText(String.format("收银员:%s/%s\n",
                    MfhLoginService.get().getHumanName(), MfhLoginService.get().getTelephone()));

            /**打印 商品明细*/
            printAndLineFeed(esc, 1);
            List<PosOrderItemEntity> needWaitEntities = new ArrayList<>();
            List<PosOrderItemEntity> posOrderItemEntities = CashierAgent.fetchOrderItems(posOrderEntity);
            if (posOrderItemEntities != null && posOrderItemEntities.size() > 0) {
                makeOrderItem7(esc, "商品", "数量", "单位",
                        "", "原价", "小计",
                        "", "会员价", "小计", true);
                esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
                esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
                for (PosOrderItemEntity entity : posOrderItemEntities) {
                    if (entity.getNeedWait().equals(1)) {
                        needWaitEntities.add(entity);
                    }
                    makeOrderItem7(esc, entity.getSkuName(),
                            String.format("%.3f", entity.getBcount()),
                            entity.getUnit(),
                            entity.getBarcode(),
                            String.format("%.2f", entity.getFinalPrice()),
                            String.format("%.3f", entity.getFinalAmount()),
                            "",
                            String.format("%.2f", entity.getCustomerPrice()),
                            String.format("%.3f", MathCompact.mult(entity.getCustomerPrice(), entity.getBcount())),
                            true);
                }
            }

            /**
             * 打印合计信息
             * */
//            esc.addText("--------------------------------\n");//32个
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s\n",
                    Printer.formatShort("原价合计:", 24, Printer.BLANK_GRAVITY.LEFT),
                    Printer.formatShort(String.format("%.2f", posOrderEntity.getFinalAmount()), 8, Printer.BLANK_GRAVITY.LEFT)));

            //支付记录
            appendPayWays(esc, posOrderEntity.getId());
//            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(posOrderEntity.getId());
//            List<PayWay> payWays = payWrapper.getPayWays();
//            if (payWays != null && payWays.size() > 0) {
//                for (PayWay payWay : payWays) {
//                    //现金支付（订单支付金额＋找零金额）
//                    if (WayType.CASH.equals(payWay.getAmountType())) {
//                        esc.addText(String.format("%s:%.2f\n",
//                                WayType.getWayTypeName(payWay.getAmountType()),
//                                payWay.getAmount() + payWrapper.getChange()));
//                    } else {
//                        esc.addText(String.format("%s:%.2f\n",
//                                WayType.getWayTypeName(payWay.getAmountType()), payWay.getAmount()));
//                    }
//                }
//            }
//            esc.addText(String.format("找零:%.2f\n", payWrapper.getChange()));

            /** 打印 结束语*/
            printAndLineFeed(esc, 2);//打印并且走纸3行
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
            addFooter(esc);
            printAndLineFeed(esc, 3);//打印并且走纸3行

            if (waitReceiptEnabled && needWaitEntities.size() > 0) {

                ZLogger.d("打印客户联");
                printAndLineFeed(esc, 2);//打印并且走纸3行
                esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
                esc.addText("客户联\n");
                esc.addText("--------------------------------\n");//32个
                esc.addText(String.format("%d\n", posOrderEntity.getFlowId()));

                esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
                for (PosOrderItemEntity entity : needWaitEntities) {
                    makeOrderItem1(esc, entity.getSkuName(),
                            MUtils.formatDouble(null, null, entity.getBcount(), "", "", entity.getUnit()),
                            MUtils.formatDouble(entity.getFinalAmount(), ""));

                }
                ZLogger.d("打印厨房联");
                printAndLineFeed(esc, 5);//打印并且走纸3行
                esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
                esc.addText("厨房联\n");
                esc.addText("--------------------------------\n");//32个
                esc.addText(String.format("%d\n", posOrderEntity.getFlowId()));

                esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
                for (PosOrderItemEntity entity : needWaitEntities) {
                    makeOrderItem1(esc, entity.getSkuName(),
                            MUtils.formatDouble(null, null, entity.getBcount(), "", "", entity.getUnit()),
                            MUtils.formatDouble(entity.getFinalAmount(), ""));

                }
                printAndLineFeed(esc, 3);//打印并且走纸3行
            }

            return esc;
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.ef(e.toString());
        }

        return null;
    }

    @Override
    public EscCommand makePrepareOrderEsc(ScOrder scOrder) {
        if (scOrder == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);

        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);

        addCODE128(esc, scOrder.getBarcode());

        printAndLineFeed(esc, 1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        esc.addText("拣货单");
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        printAndLineFeed(esc, 1);


        printAndLineFeed(esc, 1);
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
        printAndLineFeed(esc, 1);

        /**打印 商品明细*/
        Double goodsAmount = 0D;
        List<ScOrderItem> items = scOrder.getItems();
        if (items != null && items.size() > 0) {
            esc.addText("品名               数量   小计\n");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
            esc.addText("--------------------------------\n");//32个
            for (ScOrderItem item : items) {
                makeOrderItem1(esc, item.getProductName(),
                        MUtils.formatDouble(null, null, item.getBcount(), "", "", item.getUnitName()),
                        MUtils.formatDouble(item.getAmount(), ""));
                goodsAmount += item.getAmount();
            }
        }

        esc.addText("--------------------------------\n");//32个
        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);
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
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        addFooter(esc);
        printAndLineFeed(esc, 3);

        return esc;
    }

    @Override
    public EscCommand makeStockOutOrderEsc(List<StockOutItem> orderItems) {
        if (orderItems == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);
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


        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 订购日期*/
        esc.addText(String.format("日期:%s \n", TimeCursor.InnerFormat.format(new Date())));
        printAndLineFeed(esc, 1);

        /**打印 商品明细
         *    8       8       4     6      6
         *01234567 89012345 6789 012345 678901 (32)
         * 商品ID    品名   数量   单价   金额
         * */
//        esc.addText("收货人                    手机号\n");
//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        if (orderItems.size() > 0) {
            esc.addText("--------------------------------\n");//32个
            for (StockOutItem entity : orderItems) {
                esc.addText(String.format("%s/%s\n", entity.getHumanName(), entity.getHumanPhone()));
                esc.addText(String.format("%s/%s\n", entity.getBarcode(), entity.getTransportName()));
            }

            esc.addText("--------------------------------\n");//32个
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("总计:%d件\n", orderItems.size()));
        }

        printAndLineFeed(esc, 1);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("签字：\n");
        printAndLineFeed(esc, 1);


        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        /**打印 结束语*/
        addFooter(esc);
        printAndLineFeed(esc, 3);

        return esc;
    }

    @Override
    public EscCommand makeDailySettleEsc(DailysettleInfo dailysettleInfo) {
        if (dailysettleInfo == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);
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


        printAndLineFeed(esc, 1);
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

        printAndLineFeed(esc, 1);
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
        printAndLineFeed(esc, 1);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        addFooter(esc);
        printAndLineFeed(esc, 3);

        return esc;
    }

    @Override
    public EscCommand makeHandoverEsc(HandOverBill handOverBill) {
        if (handOverBill == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);
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

        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 抬头：交接班信息*/
        esc.addText(String.format("班次：%d \n", handOverBill.getShiftId()));
        esc.addText(String.format("交班人:%s\n", handOverBill.getHumanName()));
        //打印上班时间，来核对交班信息
//        esc.addText(String.format("上班时间：%s \n", TimeCursor.InnerFormat.format(handOverBill.getStartDate())));
        esc.addText(String.format("交班时间：%s \n",
                TimeUtil.format(handOverBill.getEndDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
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
        printAndLineFeed(esc, 1);
        esc.addText("支付类型            数量    金额\n");
        printDailySettleAccItem(esc, 1, handOverBill.getAccItems());
        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("账户新增:%.2f\n", handOverBill.getTurnover() - handOverBill.getCash()));
        esc.addText(String.format("现金收取:%.2f\n", handOverBill.getCash()));
        esc.addText(String.format("原价金额:%.2f\n", handOverBill.getOrigionAmount()));
        esc.addText(String.format("营业额:%.2f\n", handOverBill.getTurnover()));

        /**
         * 打印 结束语
         * */
        printAndLineFeed(esc, 1);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        addFooter(esc);
        printAndLineFeed(esc, 3);

        return esc;
    }

    @Override
    public void makeHandoverTemp(EscCommand esc, String name, String bcount, String amount) {
        if (esc == null) {
            return;
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //计算名称行数
        if (Printer.getLength(name) > 20) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
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
     * 打印收银订单
     */
    @Override
    public EscCommand makePosOrderEsc1(PosOrder curOrder) {
        EscCommand esc = new EscCommand();

        /**打印 标题*/
        printAndLineFeed(esc, 1);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        esc.addText(String.format("%s\n", MfhLoginService.get().getCurOfficeName()));
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        /**打印头部*/
        printAndLineFeed(esc, 1);//设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
//        esc.addText(String.format("订单号:%d \n", curOrder.getId()));
        esc.addText(String.format("日期:%s \n",
                TimeUtil.format(curOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        esc.addText(String.format("订单金额:%.2f\n", curOrder.getAmount()));
        esc.addText(String.format("支付方式:%s\n", WayType.name(curOrder.getPayType())));

        /**打印 商品明细*/
        printAndLineFeed(esc, 1);
        List<PosOrderItem> oderItems = curOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            esc.addText("品名            单价 数量   小计\n");
            esc.addText("--------------------------------\n");//32个

            for (PosOrderItem entity : oderItems) {
                makeOrderItem2(esc,
                        entity.getProductName(),
                        String.format("%.2f", entity.getPrice()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getAmount()));

//                makeOrderItem4(esc,
//                        String.format("%s", entity.getProductName()),
//                        String.format("*%.2f", entity.getBcount()),
//                        String.format("%.2f", entity.getAmount()));
            }
        }

        /**打印 结束语*/
        esc.addText("--------------------------------\n");//32个
        printAndLineFeed(esc, 1);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        addFooter(esc);
        printAndLineFeed(esc, 3);

        return esc;
    }

    /**
     * 打印外部配送单
     */
    @Override
    public EscCommand makePosOrderEsc2(PosOrder posOrder) {
        EscCommand esc = new EscCommand();

        /**打印 标题*/
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        printAndLineFeed(esc, 1);//打印并且走纸2行
        addCODE128(esc, posOrder.getOuterNo());/**一维条码*/
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        esc.addText("配送单\n");
        esc.addText(String.format("%s\n", PosType.name(posOrder.getSubType())));
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        /**打印 头部*/
        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
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
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            for (PosOrderItem entity : oderItems) {
                makeOrderItem5(esc, entity.getProductName(),
                        String.format("%.2f", entity.getPrice()),
                        String.format("*%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getAmount()));
            }
        }

        /**打印 尾部*/
//        printAndLineFeed(esc, 1);
        esc.addText("--------------------------------\n");//32个
        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
        esc.addText(String.format("订单金额: %.2f\n", posOrder.getAmount()));

        /**打印 结束语*/
        printAndLineFeed(esc, 1);
        addFooter(esc);
        printAndLineFeed(esc, 2);

        return esc;
    }

    @Override
    public EscCommand makeSendOrder3pEsc(PosOrderEntity posOrderEntity) {
        if (posOrderEntity == null) {
            return null;
        }

        EscCommand esc = new EscCommand();

        /**打印 标题*/
        printAndLineFeed(esc, 1);//打印并且走纸2行

        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //打印 一维条码
        addCODE128(esc, posOrderEntity.getOuterTradeNo());
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        esc.addText("配送单\n");
        esc.addText(String.format("%s\n", PosType.name(posOrderEntity.getSubType())));
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        /**打印头部*/
        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
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
        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
        esc.addText(String.format("订单金额:%.2f\n", posOrderEntity.getFinalAmount()));

        /** 打印 结束语*/
        printAndLineFeed(esc, 2);//打印并且走纸3行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        addFooter(esc);
        printAndLineFeed(esc, 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 打印平台配送订单
     */
    @Override
    public EscCommand makePosOrderEsc3(PosOrder posOrder) {
        EscCommand esc = new EscCommand();

        /**打印 标题*/
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        printAndLineFeed(esc, 1);//打印并且走纸3行
        addCODE128(esc, posOrder.getBarcode());/**一维条码*/
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        esc.addText("配送单\n");
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        /**打印 订单基本信息*/
        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
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
        Double goodsAmount = 0D, commitGoodsAmount = 0D;
        if (oderItems != null && oderItems.size() > 0) {
//            esc.addText("品名                 数量   小计\n");
            esc.addText("品名            单价 数量   小计\n");
            esc.addText("--------------------------------\n");//32个
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
        printAndLineFeed(esc, 1);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐

        esc.addText(MUtils.formatDouble("订单金额", ": ", posOrder.getAmount(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("配送费", ": ", posOrder.getTransFee(), "", null, null));
        esc.addText("\n");
        esc.addText(MUtils.formatDouble("优惠券", ": ", posOrder.getDisAmount(), "", null, null));
        esc.addText("\n");
        esc.addText(String.format("商品金额: %.2f\n", goodsAmount));
        esc.addText(String.format("拣货金额: %.2f\n", commitGoodsAmount));
        Double disAmount = MathCompact.sub(commitGoodsAmount, goodsAmount);
        //负数表示退款
        if (disAmount < 0) {
            esc.addText(String.format("   差额: -%.2f\n", disAmount));
        } else {
            esc.addText(String.format("   差额: +%.2f\n", disAmount));
        }

        /**打印 结束语*/
        printAndLineFeed(esc, 2);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        addFooter(esc);
        printAndLineFeed(esc, 3);

        return esc;
    }

    @Override
    public EscCommand makeSendOrderEsc(ScOrder scOrder) {
        if (scOrder == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        addCODE128(esc, scOrder.getBarcode());
        printAndLineFeed(esc, 1);
//        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        esc.addText("配送单");
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        printAndLineFeed(esc, 2);


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
        printAndLineFeed(esc, 2);

        /**打印 商品明细*/

        Double goodsAmount = 0D, commitGoodsAmount = 0D;
        List<ScOrderItem> items = scOrder.getItems();
        if (items != null && items.size() > 0) {
            esc.addText("品名                数量   小计\n");
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
                esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            }
        }

        esc.addText("--------------------------------\n");//32个
        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);
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
//        esc.addText(MUtils.formatDouble("差额", ": ", MathCompact.sub(amount, actualAmount), "", null, null));
        esc.addText("\n");

        /**
         * 打印 结束语
         * */
        printAndLineFeed(esc, 2);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        addFooter(esc);
        printAndLineFeed(esc, 3);

        return esc;
    }


    @Override
    public EscCommand makeGoodsFlowEsc(List<GoodsItem> goodsItems) {
        if (goodsItems == null) {
            return null;
        }

        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        //显示当前网点名称
//        esc.addText(dailysettleInfo.getOfficeName());
//        //取消倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 抬头：日结信息*/
//        esc.addText(String.format("班次：%d \n", handOverBill.getShiftId()));
//        esc.addText(String.format("日结人:%s\n", dailysettleInfo.getHumanName()));
//        esc.addText(String.format("日结时间：%s \n",
//                TimeCursor.InnerFormat.format(dailysettleInfo.getCreatedDate())));
//        esc.addText(String.format("设备编号：%s \n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 日结单明细*/
        esc.addText("商品               数量    金额\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);

        for (GoodsItem goodsItem : goodsItems) {
            makeHandoverTemp(esc,
                    String.format("%s/%s", goodsItem.getBarcode(),
                            goodsItem.getProductName()),
                    String.format("%.2f", goodsItem.getBcount()),
                    String.format("%.2f", goodsItem.getFactAmount()));
        }

        printAndLineFeed(esc, 1);

//        /**
//         * 打印合计信息
//         * */
//        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
//        esc.addText("--------------------------------\n");//32个
//        esc.addText(String.format("现金收取:%.2f\n", dailysettleInfo.getCash()));
//        esc.addText(String.format("非现金收取:%.2f\n",
//                dailysettleInfo.getTurnOver() - dailysettleInfo.getCash()));
//
//        Double turnover = dailysettleInfo.getTurnOver();
//        esc.addText(String.format("营业额合计:%.2f\n", turnover));

        /**
         * 打印 结束语
         * */
//        printAndLineFeed(esc, 1);
//        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
//        addFooter(esc);
//        printAndLineFeed(esc, 3);

        return esc;
    }


    @Override
    public EscCommand makeHeaderEsc(String title, HashMap<String, String> headers) {
        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);

        /**打印 标题*/
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印居中
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

        //打印表头内容
        if (footers != null && footers.size() > 0) {
            for (String key : footers.keySet()) {
                preparefooter1(esc, key, footers.get(key), false);
            }
        }

        printAndLineFeed(esc, 1);
        return esc;
    }


    @Override
    public EscCommand makeReconcileHeaderEsc(String title, HashMap<String, String> headers) {
        EscCommand esc = new EscCommand();
        printAndLineFeed(esc, 2);


        /**打印 标题*/
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
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
    public List<EscCommand> makeReconcileEsc(List<ProductAggDate> goodsItems) {
        if (goodsItems == null) {
            return null;
        }

        List<EscCommand> commands = new ArrayList<>();

        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        Double turnOver = 0D;
        int len = goodsItems.size();
        for (int i = 0; i < len; i++) {
            ProductAggDate goodsItem = goodsItems.get(i);

            turnOver += goodsItem.getTurnover();
            prepareContent1(esc, goodsItem.getTenantSkuIdWrapper(),
                    String.format("%.2f", goodsItem.getProductNum()),
                    String.format("%.2f", goodsItem.getTurnover()), true);

            if (i == len - 1) {
                prepareContent1(esc, "", "销售额合计：",
                        String.format("%.2f", turnOver), false);
                printAndLineFeed(esc, 2);
                addFooter(esc);
                commands.add(esc);

                esc = new EscCommand();
            } else if (i != 0 && i % 20 == 0) {
                commands.add(esc);
                esc = new EscCommand();
            }
        }

        return commands;
    }

    @Override
    public EscCommand makeTestPageEsc() {
        try {
            EscCommand esc = new EscCommand();
            printAndLineFeed(esc, 2);
            //设置打印居中
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
            esc.addText("打印测试");
            printAndLineFeed(esc, 1);


//        //取消倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
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

            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            //设置为倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
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
//            // 一维条码：设置条码可识别字符位置在条码下方
//            esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
//            //设置条码高度为 60 点
//            esc.addSetBarcodeHeight((byte) 60);
//            //设置条码单元宽度为1点
//            esc.addSetBarcodeWidth((byte) 1);
//            esc.addCODE128(esc.genCode128("123456"));  //打印Code128码
//            esc.addCODE128(esc.genCode128("123456789"));  //打印Code128码
//            esc.addCODE128(esc.genCode128(MUtils.getOrderBarCode()));
//            esc.addCODE128(esc.genCodeB("Gprinter"));
//            esc.addCODE128(esc.genCodeB(MUtils.getOrderBarCode()));
//            esc.addCODE128(esc.genCodeC(MUtils.getOrderBarCode()));

        /*QRCode 命令打印
        此命令只在支持 QRCode 命令打印的机型才能使用。
        在不支持二维码指令打印的机型上,则需要发送二维条码图片
        */
            esc.addText("Print QRcode\n");   //  打印文字
            esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); //设置纠错等级
            esc.addSelectSizeOfModuleForQRCode((byte) 3);//设置 qrcode 模块大小
            esc.addStoreQRCodeData("www.manfenjiayuan.cn");//设置 qrcode 内容
            esc.addPrintQRCode();//打印 QRCode
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
            esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
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

    public void makeTestTemp(EscCommand esc, String name,
                             String price, String bcount, String amount) {
        if (esc == null) {
            return;
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //最多显示17*0.6=10.2个汉字
        if (Printer.getLength(name) > 16) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
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
    public void makeOrderItem1(EscCommand esc, String name, String unit,
                               String bcount) {
        if (esc == null) {
            return;
        }
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //最多显示17*0.6=10.2个汉字
        if (Printer.getLength(name) > 20) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
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
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
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

//            ZLogger.d("printText:" + printText);
        }
        esc.addText("\n");
    }
}
