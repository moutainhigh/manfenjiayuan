package com.bingshanguxue.cashier.hardware.printer.emb;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.hardware.printer.IPrinter;
import com.bingshanguxue.cashier.hardware.printer.PrintManager;
import com.bingshanguxue.cashier.hardware.printer.Printer;
import com.bingshanguxue.cashier.model.PosOrder;
import com.bingshanguxue.cashier.model.PosOrderItem;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PayWay;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.printer.sdk.Barcode;
import com.printer.sdk.PrinterConstants;

import java.util.Date;
import java.util.List;


/**
 * Created by bingshanguxue on 6/22/16.
 */
public class EmbPrintManager extends PrintManager {

    @Override
    public IPrinter create() {
        mPrinter = new EmbPrinter();
        return mPrinter;
    }

    @Override
    public void printBarcode(EscCommand esc, String barcode) {
//        super.printBarcode(esc, barcode);

        if (esc == null){
            return;
        }

        //设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);
        //设置条码单元宽度为1点
        esc.addSetBarcodeWidth((byte) 1);
        Barcode barcode1 = new Barcode(PrinterConstants.BarcodeType.CODE128, 3,
                80, 2, barcode);
        esc.addUserCommand(barcode1.getBarcodeData());
    }

    /**
     * 打印订单
     */
    @Override
    public EscCommand makePosOrderEsc1(PosOrder curOrder) {
        EscCommand esc = super.makePosOrderEsc1(curOrder);

        esc.addUserCommand(EmbPrinter.initPrinter());
        //打印并且走纸3行
        mPrinter.printAndLineFeed(esc, 2);
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));

        /**打印 标题*/
        esc.addText("收银小票\n");
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

        mPrinter.printAndLineFeed(esc, 1);
//进纸一行
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        /**打印 订单条码*/
        esc.addText(String.format("订单号:%d \n", curOrder.getId()));
        /**打印 应付款*/
//        esc.addText(String.format("应付款:%.2f \n", orderEntity.getAmount()));
        /**打印 订购日期*/
        esc.addText(String.format("日期:%s \n",
                TimeUtil.format(curOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        esc.addText(String.format("订单金额:%.2f\n", curOrder.getAmount()));
        esc.addText(String.format("支付方式:%s\n", WayType.name(curOrder.getPayType())));
        mPrinter.printAndLineFeed(esc, 1);

        esc.addText("--------------------------------\n");//32个
        esc.addText("货号/品名           数量   小计\n");
//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
//        Double totalQuantity = 0D, totalAmount = 0D;
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        /**打印 商品明细*/
        List<PosOrderItem> oderItems = curOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            for (PosOrderItem entity : oderItems) {
                makePosOrderTemp(esc,
                        String.format("%s/%s", entity.getBarcode(), entity.getProductName()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getAmount()));
            }
        }

        mPrinter.printAndLineFeed(esc, 1);
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        /**打印 结束语*/
        esc.addText("谢谢惠顾!\n");
        esc.addText("欢迎下次光临\n");

//        addFooter(esc);
        mPrinter.printAndLineFeed(esc, 3);
//打印并且走纸3行

        return esc;
    }

    /**
     * 平台配送单
     * */
    @Override
    public EscCommand makePosOrderEsc2(PosOrder posOrder) {
        EscCommand esc = super.makePosOrderEsc2(posOrder);

        esc.addUserCommand(EmbPrinter.initPrinter());
        //打印并且走纸2行
        mPrinter.printAndLineFeed(esc, 2);

        /**打印 一维条码*/
        printBarcode(esc, posOrder.getBarcode());


        /**打印 标题*/
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        esc.addText("外部平台配送单\n");
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

        /**打印 订单基本信息*/
        mPrinter.printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        //订单来源
        esc.addText(String.format("订单来源: %s \n", PosType.name(posOrder.getSubType())));
        //订单编号
        esc.addText(String.format("订单号: %d \n", posOrder.getId()));
        //发货时间
        esc.addText(String.format("发货时间: %s \n",
                TimeUtil.format(posOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));

        /**打印 订单明细*/
        mPrinter.printAndLineFeed(esc, 1);
        esc.addText("--------------------------------\n");//32个
        esc.addText("货号/品名           数量   小计\n");
//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
//        Double totalQuantity = 0D, totalAmount = 0D;
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        List<PosOrderItem> oderItems = posOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            for (PosOrderItem entity : oderItems) {
                makePosOrderTemp(esc,
                        String.format("%s/%s", entity.getBarcode(), entity.getProductName()),
                        String.format("%.2f", entity.getCommitCount()),
                        String.format("%.2f", entity.getCommitAmount()));
            }
        }
        esc.addText("--------------------------------\n");//32个
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        esc.addText(String.format("订单金额: %.2f\n", posOrder.getAmount()));

        mPrinter.printAndLineFeed(esc, 2);
        /**打印 结束语*/
        addFooter(esc);
        //打印并且走纸3行
        mPrinter.printAndLineFeed(esc, 3);

        return esc;
    }

    @Override
    public EscCommand makePosOrderEsc3(PosOrder posOrder) {
        EscCommand esc = super.makePosOrderEsc3(posOrder);

        esc.addUserCommand(EmbPrinter.initPrinter());
        //打印并且走纸2行
        mPrinter.printAndLineFeed(esc, 2);

        /**打印 一维条码*/
        printBarcode(esc, posOrder.getBarcode());

        /**打印 标题*/
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        esc.addText("配送单\n");
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

        /**打印 订单基本信息*/
        mPrinter.printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
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
        mPrinter.printAndLineFeed(esc, 1);
        esc.addText("--------------------------------\n");//32个
        esc.addText("货号/品名           数量   小计\n");
//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
//        Double totalQuantity = 0D, totalAmount = 0D;
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);

        List<PosOrderItem> oderItems = posOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            for (PosOrderItem entity : oderItems) {
                makePosOrderTemp(esc,
                        String.format("%s/%s", entity.getBarcode(), entity.getProductName()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getAmount()));
            }
        }
        esc.addText("--------------------------------\n");//32个
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        esc.addText(String.format("订单金额: %.2f\n", posOrder.getAmount()));
        esc.addText(String.format("拣货金额: %.2f\n", posOrder.getCommitAmount()));
        Double disAmount = MathCompact.sub(posOrder.getCommitAmount(), posOrder.getAmount());
        if (disAmount < 0){
            esc.addText(String.format("   差额: -%.2f\n", disAmount));
        }
        else{
            esc.addText(String.format("   差额: +%.2f\n", disAmount));
        }
        mPrinter.printAndLineFeed(esc, 2);
        /**打印 结束语*/
        addFooter(esc);
        //打印并且走纸3行
        mPrinter.printAndLineFeed(esc, 3);

        return esc;
    }


    /**
     * 订单明细模版1
     * */
    @Override
    public EscCommand makeOrderItem1(EscCommand rawEsc, String name, String unit, String bcount) {
        EscCommand esc = rawEsc;
        if (esc == null) {
            esc = new EscCommand();
        }
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));

        //最多显示17*0.6=10.2个汉字
        if (Printer.getLength(name) > 21) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            //设置打印左对齐
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_RIGHT));

            esc.addText(String.format("%s%s", Printer.formatShort(unit, 5, Printer.BLANK_GRAVITY.RIGHT),
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

        return esc;
    }

    /**
     * 拣货单明细模版
     */
    @Override
    public EscCommand createPrepareOrderItem(EscCommand rawEsc, String name, String bcount,
                                             String amount) {
        EscCommand esc = rawEsc;
        if (esc == null) {
            esc = new EscCommand();
        }
        esc.addUserCommand(EmbPrinter.initPrinter());
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));

        //最多显示17*0.6=10.2个汉字
        if (Printer.getLength(name) > 18) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_RIGHT));
            esc.addText(String.format("%s%s",
                    Printer.formatShort(bcount, 9, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 5, Printer.BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s",
                    Printer.formatShort(name, 18, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(bcount, 9, Printer.BLANK_GRAVITY.RIGHT),
                    Printer.formatShort(amount, 5, Printer.BLANK_GRAVITY.LEFT));
            esc.addText(printText);

//            ZLogger.d("printText:" + printText);
        }
        esc.addText("\n");

        return esc;
    }

    @Override
    public EscCommand makePosOrderEsc(PosOrderEntity posOrderEntity) {
        if (posOrderEntity == null) {
            return null;
        }

        ZLogger.d(JSONObject.toJSONString(posOrderEntity));
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());
        //打印并且走纸3行
        mPrinter.printAndLineFeed(esc, 2);
        //设置打印居中
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));

//        //设置为倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
        /**打印 标题*/
        esc.addText("收银小票\n");
        //取消倍高倍宽
        esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));

        mPrinter.printAndLineFeed(esc, 1);
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
        esc.addText(String.format("下单时间：%s \n",
                TimeUtil.format(posOrderEntity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("机器设备号:%s\n", SharedPrefesManagerFactory.getTerminalId()));
        esc.addText(String.format("收银时间:%s/%s \n",
                MfhLoginService.get().getLoginName(), MfhLoginService.get().getTelephone()));
        mPrinter.printAndLineFeed(esc, 1);

        /**打印 商品明细*/
        esc.addText("货号/品名       单价 数量   小计\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));

        List<PosOrderItemEntity> posOrderItemEntityList = CashierAgent.fetchOrderItems(posOrderEntity);
        if (posOrderItemEntityList != null && posOrderItemEntityList.size() > 0) {
            esc.addText("--------------------------------\n");//32个
            for (PosOrderItemEntity entity : posOrderItemEntityList) {
//                makeTemp(esc, entity.getName(), String.format("%.2f", entity.getCostPrice() * entity.getBcount()));
                makePosOrderLine(esc, String.format("%s/%s", entity.getBarcode(),
                        entity.getName()), String.format("%.2f", entity.getFinalPrice()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getFinalAmount()));
            }
            esc.addText("--------------------------------\n");//32个
        }

        /**
         * 打印合计信息
         * */
        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_RIGHT));

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
        if (payWays != null && payWays.size() > 0){
            for (PayWay payWay : payWays){
                //现金支付（订单支付金额＋找零金额）
                if (WayType.CASH.equals(payWay.getPayType())){
                    esc.addText(String.format("%s:%.2f\n", WayType.name(payWay.getPayType()),
                            payWay.getAmount() + payWrapper.getChange()));
                }
                else{
                    esc.addText(String.format("%s:%.2f\n",
                            WayType.name(payWay.getPayType()) , payWay.getAmount()));
                }
            }
        }

        esc.addText(String.format("找零:%.2f\n", payWrapper.getChange()));

        /**
         * 打印 结束语
         * */
        //打印并且走纸3行
        mPrinter.printAndLineFeed(esc, 3);

        //设置打印左对齐
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));

        addFooter(esc);

        //打印并且走纸3行
        mPrinter.printAndLineFeed(esc, 3);

        return esc;
    }


    @Override
    public EscCommand makeTopupEsc(QuickPayInfo mQuickPayInfo, String outTradeNo) {
        if (mQuickPayInfo == null || StringUtils.isEmpty(outTradeNo)) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addUserCommand(EmbPrinter.initPrinter());

        //打印并且走纸3行
        mPrinter.printAndLineFeed(esc, 2);

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

        mPrinter.printAndLineFeed(esc, 1);
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
//        esc.addPrintAndLineFeed();
//
        /**
         * 打印 结束语
         * */
        mPrinter.printAndLineFeed(esc, 1);
        esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
        esc.addText("请注意保管好您的交易凭条!\n");
        //打印并且走纸3行
        mPrinter.printAndLineFeed(esc, 3);

        return esc;
    }

    /**
     * 打印测试
     */
    @Override
    public EscCommand makeTestEsc() {
        try {
            EscCommand esc = new EscCommand();
            esc.addUserCommand(EmbPrinter.initPrinter());

            mPrinter.printAndLineFeed(esc, 2);
//打印并且走纸3行
            //设置打印居中
            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER));
//        //设置为倍高倍宽
            esc.addUserCommand(EmbPrinter.setFont(0, 1, 1, 0, 0));
            esc.addText("打印测试");
//        //取消倍高倍宽
            esc.addUserCommand(EmbPrinter.setFont(0, 0, 0, 0, 0));
            mPrinter.printAndLineFeed(esc, 1);

            esc.addUserCommand(EmbPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT));
            esc.addText("Welcome to use Gprinter!\n");   //  打印文字
            mPrinter.printAndLineFeed(esc, 1);

//        /*打印繁体中文  需要打印机支持繁体字库*/
//        String message = GPrinterAgent.SimToTra("佳博票据打印机\n");
//        //	esc.addText(message,"BIG5");
//        esc.addText(message,"GB2312");
//        esc.addPrintAndLineFeed();


            /**打印 机器设备号＋订单号*/
            esc.addText(String.format("%s NO.%s \n", SharedPrefesManagerFactory.getTerminalId(),
                    MUtils.getOrderBarCode()));
            /**打印 订购日期*/
            esc.addText(String.format("%s \n", Printer.DATE_FORMAT.format(new Date())));
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

            mPrinter.printAndLineFeed(esc, 1);

/*打印一维条码code128*/
            esc.addText("Print code128\n");   //  打印文字
            printBarcode(esc, MUtils.getOrderBarCode());

            mPrinter.printAndLineFeed(esc, 1);


        /*QRCode 命令打印
        此命令只在支持 QRCode 命令打印的机型才能使用。
        在不支持二维码指令打印的机型上,则需要发送二维条码图片
        */
            esc.addText("Print QRcode\n");   //  打印文字
            Barcode barcode = new Barcode(PrinterConstants.BarcodeType.QRCODE, 2, 3, 6,
                    MobileApi.DOMAIN);
            esc.addUserCommand(barcode.getBarcodeData());
//打印 QRCode
            mPrinter.printAndLineFeed(esc, 1);

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

}
