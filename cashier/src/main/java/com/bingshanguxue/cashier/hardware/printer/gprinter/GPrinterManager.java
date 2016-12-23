package com.bingshanguxue.cashier.hardware.printer.gprinter;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.hardware.printer.IPrinter;
import com.bingshanguxue.cashier.hardware.printer.Printer;
import com.bingshanguxue.cashier.hardware.printer.PrinterManager;
import com.bingshanguxue.cashier.model.PosOrder;
import com.bingshanguxue.cashier.model.PosOrderItem;
import com.bingshanguxue.cashier.model.wrapper.DailysettleInfo;
import com.bingshanguxue.cashier.model.wrapper.HandOverBill;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PayWay;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.pmcstock.StockOutItem;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import java.util.Date;
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
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
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


        esc.addPrintAndLineFeed();//进纸一行
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
        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        esc.addText("请注意保管好您的交易凭条!\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makePosOrderEsc(PosOrderEntity posOrderEntity) {
        if (posOrderEntity == null) {
            return null;
        }

        ZLogger.d(JSONObject.toJSONString(posOrderEntity));
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        esc.addText("收银小票\n");
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText(String.format("下单时间：%s \n",
                TimeUtil.format(posOrderEntity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("机器设备号:%s\n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText(String.format("收银员:%s/%s \n",
                MfhLoginService.get().getLoginName(), MfhLoginService.get().getTelephone()));
        esc.addPrintAndLineFeed();

        /**打印 商品明细*/
//        esc.addText("货号/品名       单价 数量   小计\n");
        esc.addText("货号/品名     单价   数量  小计\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        List<PosOrderItemEntity> posOrderItemEntityList = CashierAgent.fetchOrderItems(posOrderEntity);
        if (posOrderItemEntityList != null && posOrderItemEntityList.size() > 0) {
            esc.addText("--------------------------------\n");//32个
            for (PosOrderItemEntity entity : posOrderItemEntityList) {
                makeOrderItem2(esc, String.format("%s/%s", entity.getBarcode(),
                        entity.getName()), String.format("%.2f", entity.getFinalPrice()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getFinalAmount()));
            }
            esc.addText("--------------------------------\n");//32个
        }

        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
        esc.addText(String.format("订单金额:%.2f\n", posOrderEntity.getFinalAmount()));

        //支付记录
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(posOrderEntity.getId());
//        Double payableAmount = posOrderEntity.getFinalAmount() - payWrapper.getRuleDiscount();
//        if (payableAmount < 0.01) {
//            payableAmount = 0D;
//        }
//        esc.addText(String.format("优惠:%.2f\n", payWrapper.getRuleDiscount()));
////        esc.addText(String.format("代金券:%.2f\n", orderEntity.getCouponDiscountAmount()));
//        esc.addText(String.format("应收:%.2f\n", payableAmount));

        List<PayWay> payWays = payWrapper.getPayWays();
        if (payWays != null && payWays.size() > 0) {
            for (PayWay payWay : payWays) {
                //现金支付（订单支付金额＋找零金额）
                if (WayType.CASH.equals(payWay.getPayType())) {
                    esc.addText(String.format("%s:%.2f\n", WayType.name(payWay.getPayType()),
                            payWay.getAmount() + payWrapper.getChange()));
                } else {
                    esc.addText(String.format("%s:%.2f\n",
                            WayType.name(payWay.getPayType()), payWay.getAmount()));
                }
            }
        }

        esc.addText(String.format("找零:%.2f\n", payWrapper.getChange()));

        /**
         * 打印 结束语
         * */
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        addFooter(esc);
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makePrepareOrderEsc(ScOrder scOrder) {
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
                makeOrderItem1(esc, item.getProductName(),
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
        addFooter(esc);
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makeSendOrderEsc(ScOrder scOrder) {
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
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        addFooter(esc);

//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 5);//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makeStockOutOrderEsc(List<StockOutItem> orderItems) {
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
        addFooter(esc);
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makeDailySettleEsc(DailysettleInfo dailysettleInfo) {
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

    @Override
    public EscCommand makeHandoverEsc(HandOverBill handOverBill) {
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

        //打印并且走纸3行
        esc.addPrintAndFeedLines((byte) 2);
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        esc.addText("收银小票\n");
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 订单条码*/
//        esc.addText(String.format("订单号:%d \n", curOrder.getId()));
        /**打印 订购日期*/
        esc.addText(String.format("日期:%s \n",
                TimeUtil.format(curOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        esc.addText(String.format("订单金额:%.2f\n", curOrder.getAmount()));
        esc.addText(String.format("支付方式:%s\n", WayType.name(curOrder.getPayType())));
        esc.addPrintAndLineFeed();

        esc.addText("--------------------------------\n");//32个
        esc.addText("货号/品名           数量   小计\n");
//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
//        Double totalQuantity = 0D, totalAmount = 0D;
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);

        /**打印 商品明细*/
        List<PosOrderItem> oderItems = curOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            for (PosOrderItem entity : oderItems) {
                makeOrderItem3(esc,
                        String.format("%s/%s", entity.getBarcode(), entity.getProductName()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getAmount()));
            }
        }

        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        /**打印 结束语*/
        esc.addText("谢谢惠顾!\n");
        esc.addText("欢迎下次光临\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 打印平台配送单
     */
    @Override
    public EscCommand makePosOrderEsc2(PosOrder posOrder) {
        EscCommand esc = new EscCommand();

        //打印并且走纸2行
        esc.addPrintAndFeedLines((byte) 2);

        /**一维条码*/
        addCODE128(esc, posOrder.getBarcode());

        /**打印 标题*/
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        esc.addText("外部平台配送单\n");
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        /**打印 订单信息*/
        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        //订单来源
        esc.addText(String.format("订单来源: %s \n", PosType.name(posOrder.getSubType())));
        //订单编号
        esc.addText(String.format("订单号: %d \n", posOrder.getId()));
        //发货时间
        esc.addText(String.format("发货时间: %s \n",
                TimeUtil.format(posOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));

        /**打印 订单明细*/
        esc.addPrintAndLineFeed();//进纸一行
        esc.addText("--------------------------------\n");//32个
//        esc.addText("货号/品名           数量   小计\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);

        List<PosOrderItem> oderItems = posOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            for (PosOrderItem entity : oderItems) {
                makeOrderItem3(esc,
                        String.format("%s/%s", entity.getBarcode(), entity.getProductName()),
                        String.format("%.2f", entity.getCommitCount()),
                        String.format("%.2f", entity.getCommitAmount()));
            }
        }

        esc.addPrintAndLineFeed();
        esc.addText("--------------------------------\n");//32个
        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
        esc.addText(String.format("订单金额: %.2f\n", posOrder.getAmount()));

        /**打印 结束语*/
        addFooter(esc);
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    /**
     * 打印外部配送订单
     */
    @Override
    public EscCommand makePosOrderEsc3(PosOrder posOrder) {
        EscCommand esc = new EscCommand();

        //打印并且走纸3行
        esc.addPrintAndFeedLines((byte) 2);

        /**一维条码*/
        addCODE128(esc, posOrder.getBarcode());


        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        esc.addText("配送单\n");
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        /**打印 订单基本信息*/
        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText(String.format("平台: %s \n", PosType.name(posOrder.getSubType())));
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
        esc.addPrintAndLineFeed();
        esc.addText("--------------------------------\n");//32个
        esc.addText("货号/品名           数量   小计\n");
//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
//        Double totalQuantity = 0D, totalAmount = 0D;
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);

        List<PosOrderItem> oderItems = posOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            for (PosOrderItem entity : oderItems) {
                makeOrderItem3(esc,
                        String.format("%s/%s", entity.getBarcode(), entity.getProductName()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getAmount()));
            }
        }
        esc.addText("--------------------------------\n");//32个
        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
        esc.addText(String.format("订单金额: %.2f\n", posOrder.getAmount()));
        esc.addText(String.format("拣货金额: %.2f\n", posOrder.getCommitAmount()));
        Double disAmount = MathCompact.sub(posOrder.getCommitAmount(), posOrder.getAmount());
        //负数表示退款
        if (disAmount < 0) {
            esc.addText(String.format("   差额: -%.2f\n", disAmount));
        } else {
            esc.addText(String.format("   差额: +%.2f\n", disAmount));
        }

        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        /**打印 结束语*/
        addFooter(esc);
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }

    @Override
    public EscCommand makeTestPageEsc() {
        try {
            EscCommand esc = new EscCommand();
            esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
            //设置打印居中
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
            esc.addText("打印测试");
            esc.addPrintAndLineFeed();//进纸一行


//        //取消倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                    EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
            esc.addText("Welcome to use Gprinter!\n");   //  打印文字
            esc.addPrintAndLineFeed();//进纸一行

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

            esc.addPrintAndLineFeed();

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

            esc.addPrintAndLineFeed();


        /*QRCode 命令打印
        此命令只在支持 QRCode 命令打印的机型才能使用。
        在不支持二维码指令打印的机型上,则需要发送二维条码图片
        */
            esc.addText("Print QRcode\n");   //  打印文字
            esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); //设置纠错等级
            esc.addSelectSizeOfModuleForQRCode((byte) 3);//设置 qrcode 模块大小
            esc.addStoreQRCodeData("www.manfenjiayuan.cn");//设置 qrcode 内容
            esc.addPrintQRCode();//打印 QRCode
            esc.addPrintAndLineFeed();

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
        if (Printer.getLength(name) > 21) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s",
                    Printer.formatShort(unit, 5, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 5, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s",
                    Printer.formatShort(name, 22, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(unit, 5, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 5, Printer.BLANK_GRAVITY.LEFT));
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
                    Printer.formatShort(price, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 7, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s%s",
                    Printer.formatShort(name, 13, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(price, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 6, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 7, Printer.BLANK_GRAVITY.LEFT));
            esc.addText(printText);

//            ZLogger.d("printText:" + printText);
        }
        esc.addText("\n");
    }


//    @Deprecated
//    private static EscCommand makePosOrderEsc(List<PosOrderEntity> orderEntities){
//        if (orderEntities == null) {
//            return null;
//        }
//
//        PosOrderEntity firstOrderEntity = orderEntities.get(0);
//
//        EscCommand esc = new EscCommand();
//        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
//        //设置打印居中
//        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
////        //设置为倍高倍宽
////        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
////                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
//        /**打印 标题*/
//        if (StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())) {
//            esc.addText("购物清单\n");
//        } else {
//            //显示当前网点名称
//            esc.addText(MfhLoginService.get().getCurOfficeName());
//        }
////        //取消倍高倍宽
////        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
////                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
//
//
//        esc.addPrintAndLineFeed();//进纸一行
//        //设置打印左对齐
//        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
//        /**打印 机器设备号＋订单号*/
//        esc.addText(String.format("%s NO.%s \n",
//                SharedPrefesManagerFactory.getTerminalId(), firstOrderEntity.getBarCode()));
//        /**打印 订购日期*/
//        esc.addText(String.format("%s \n",
//                TimeUtil.format(firstOrderEntity.getCreatedDate(),
//                        TimeCursor.FORMAT_YYYYMMDDHHMMSS)));
//        esc.addText("--------------------------------\n");//32个
////        esc.addPrintAndLineFeed();
//
//        /**打印 商品明细*/
//        esc.addText("货号/品名       单价 数量   小计\n");
//        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
//
//        List<PosOrderItemEntity> posOrderItemEntityList = CashierAgent.fetchOrderItems(orderEntities);
//        if (posOrderItemEntityList != null && posOrderItemEntityList.size() > 0) {
//            for (PosOrderItemEntity entity : posOrderItemEntityList) {
////                makeTemp(esc, entity.getName(), String.format("%.2f", entity.getCostPrice() * entity.getBcount()));
//                makePosOrderLine(esc, String.format("%s/%s", entity.getBarcode(),
//                        entity.getName()), String.format("%.2f", entity.getFinalPrice()),
//                        String.format("%.2f", entity.getBcount()),
//                        String.format("%.2f", entity.getFinalAmount()));
//            }
//        }
//
//        /**
//         * 打印合计信息
//         * */
//        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
//        esc.addText("--------------------------------\n");//32个
//        Double finalAmount = 0D, ruleDis = 0D, paidAmount = 0D, payableAmount = 0D, change = 0D;
//        List<PayWay> payWays = new ArrayList<>();
//        for (PosOrderEntity orderEntity : orderEntities) {
//            ZLogger.d(JSONObject.toJSONString(orderEntity));
//            finalAmount += orderEntity.getFinalAmount();
//            ruleDis += orderEntity.getRuleDiscountAmount();
//            paidAmount += (orderEntity.getPaidAmount() + orderEntity.getChange());
//            Double payableTemp = orderEntity.getFinalAmount() - orderEntity.getRuleDiscountAmount();
//            if (payableTemp < 0.01) {
//                payableTemp = 0D;
//            }
//            payableAmount += payableTemp;
//
//            change += orderEntity.getChange();
//
//            //支付记录
//            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
//            if (payWrapper.getPayWays() != null) {
//                payWays.addAll(payWrapper.getPayWays());
//            }
//        }
//        esc.addText(String.format("合计:%.2f\n", finalAmount));
//        esc.addText(String.format("优惠:%.2f\n", ruleDis));
////        esc.addText(String.format("代金券:%.2f\n", orderEntity.getCouponDiscountAmount()));
//        esc.addText(String.format("应收:%.2f\n", payableAmount));
////        esc.addText(String.format("付款:%.2f\n", paidAmount - ruleDis));
//        if (payWays.size() > 0) {
//            for (PayWay payWay : payWays) {
//                // TODO: 8/2/16 ，以后如果考虑多种支付方式多次支付，应该要做一次合并统计，一种支付方式暂时可以不做。
//                esc.addText(String.format("%s:%.2f\n",
//                        WayType.name(payWay.getPayType()), payWay.getAmount()));
//            }
//        } else {
//            esc.addText(String.format("付款:%.2f\n", paidAmount - ruleDis));
//        }
//        esc.addText(String.format("找零:%.2f\n", Math.abs(change)));
//
//        /**
//         * 打印 结束语
//         * */
//        esc.addPrintAndLineFeed();
//        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
//        esc.addText("谢谢惠顾!\n");
//        esc.addText("欢迎下次光临\n");
////        esc.addPrintAndLineFeed();
//        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行
//
//        return esc;
//    }
//
//
//    /**
//     * 打印POS订单流水
//     */
//    @Deprecated
//    public static void printPosOrder(final List<PosOrderEntity> orderEntities, boolean withCode128) {
//        Observable.create(new Observable.OnSubscribe<EscCommand>() {
//            @Override
//            public void call(Subscriber<? super EscCommand> subscriber) {
//                subscriber.onNext(makePosOrderEsc(orderEntities));
//                subscriber.onCompleted();
//            }
//        })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<EscCommand>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(EscCommand escCommand) {
//                        print(escCommand);
//                    }
//                });
//    }

}
