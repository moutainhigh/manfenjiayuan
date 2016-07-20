package com.mfh.litecashier.com;

import com.alibaba.fastjson.JSON;
import com.gprinter.command.EscCommand;
import com.gprinter.command.TscCommand;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.bean.AggItem;
import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.PosOrder;
import com.mfh.litecashier.bean.PosOrderItem;
import com.mfh.litecashier.bean.StockOutItem;
import com.mfh.litecashier.bean.wrapper.AccWrapper;
import com.mfh.litecashier.bean.wrapper.AggWrapper;
import com.mfh.litecashier.bean.wrapper.HandOverBill;
import com.mfh.litecashier.database.entity.DailysettleEntity;
import com.mfh.litecashier.event.SerialPortEvent;

import org.apache.commons.lang3.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import de.greenrobot.event.EventBus;

/**
 * Created by ZZN.NAT(bingshanguxue) on 15/11/17.
 */
public class SerialManager {

    //打印机（GPrinter）
    public static final String PORT_PRINTER = "/dev/ttymxc1";//"/dev/ttyS0";
    public static final String BAUDRATE_PRINTER = "9600";
    //屏显（POSLAB）
    public static final String PORT_SCREEN = "/dev/ttymxc4";
    public static final String BAUDRATE_SCREEN = "19200";
    //屏显（JOOYTEC）
//    public static final String PORT_SCREEN = "/dev/ttyS1";
//    public static final String BAUDRATE_SCREEN = "2400";

    //银联
    public static final String PORT_UMSIPS      = "/dev/ttymxc0";
    public static final String BAUDRATE_UMSIPS  = "9600";


    private static final String PREF_NAME_SERIAL = "PREF_NAME_SERIAL";
    private static final String PREF_KEY_PRINTER_PORT = "PREF_KEY_PRINTER_PORT";
    private static final String PREF_KEY_PRINTER_BAUDRATE = "PREF_KEY_PRINTER_BAUDRATE";
    private static final String PREF_KEY_LED_PORT = "PREF_KEY_LED_PORT";
    private static final String PREF_KEY_LED_BAUDRATE = "PREF_KEY_LED_BAUDRATE";
    private static final String PK_UMSIPS_PORT = "prefkey_umsips_port";
    private static final String PK_UMSIPS_BAUDRATE = "prefkey_umsips_baudrate";


    public static java.text.SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US);
    public static final int PRINT_PRODUCT_NAME_MAX_LEN = 24;

    public static String getPrinterPort() {
        return SharedPreferencesManager.getText(
                PREF_NAME_SERIAL, PREF_KEY_PRINTER_PORT, PORT_PRINTER);
    }

    public static void setPrinterPort(String port){
        ZLogger.df(String.format("setPrinterPort(%s):%s", PREF_NAME_SERIAL, port));
        SharedPreferencesManager.set(PREF_NAME_SERIAL, PREF_KEY_PRINTER_PORT, port);
    }

    public static String getPrinterBaudrate() {
        return SharedPreferencesManager.getText(
                PREF_NAME_SERIAL, PREF_KEY_PRINTER_BAUDRATE, BAUDRATE_PRINTER);
    }

    public static void setPrinterBaudrate(String baudrate){
        ZLogger.df(String.format("setPrinterBaudrate(%s):%s", PREF_NAME_SERIAL, baudrate));
        SharedPreferencesManager.set(PREF_NAME_SERIAL, PREF_KEY_PRINTER_BAUDRATE, baudrate);
    }

    public static String getLedPort() {
        return SharedPreferencesManager.getText(
                PREF_NAME_SERIAL, PREF_KEY_LED_PORT, PORT_SCREEN);
    }

    public static void setLedPort(String port){
        ZLogger.df(String.format("setLedPort(%s):%s", PREF_NAME_SERIAL, port));
        SharedPreferencesManager.set(PREF_NAME_SERIAL, PREF_KEY_LED_PORT, port);
    }

    public static String getLedBaudrate() {
        return SharedPreferencesManager.getText(
                PREF_NAME_SERIAL, PREF_KEY_LED_BAUDRATE, BAUDRATE_SCREEN);
    }

    public static void setLedBaudrate(String baudrate){
        ZLogger.df(String.format("setLedBaudrate(%s):%s", PREF_NAME_SERIAL, baudrate));
        SharedPreferencesManager.set(PREF_NAME_SERIAL, PREF_KEY_LED_BAUDRATE, baudrate);
    }

    public static String getUmsipsPort() {
        return SharedPreferencesManager.getText(
                PREF_NAME_SERIAL, PK_UMSIPS_PORT, PORT_UMSIPS);
    }

    public static void setUmsipsPort(String port){
        ZLogger.df(String.format("setUMSIPSPort(%s):%s", PREF_NAME_SERIAL, port));
        SharedPreferencesManager.set(PREF_NAME_SERIAL, PK_UMSIPS_PORT, port);
    }

    public static String getUmsipsBaudrate() {
        return SharedPreferencesManager.getText(
                PREF_NAME_SERIAL, PK_UMSIPS_BAUDRATE, BAUDRATE_UMSIPS);
    }

    public static void setUmsipsBaudrate(String baudrate){
        ZLogger.df(String.format("setUMSIPSBaudrate(%s):%s", PREF_NAME_SERIAL, baudrate));
        SharedPreferencesManager.set(
                PREF_NAME_SERIAL, PK_UMSIPS_BAUDRATE, baudrate);
    }

    /**
     * 打印线上订单
     * */
    public static void printOrder(PosOrder curOrder, boolean withCode128){
        if (curOrder == null){
            return;
        }

        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        esc.addText("购物清单\n");
        //取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        /*打印一维条码code128*/
//        if (withCode128){
//            // 设置条码可识别字符位置在条码下方
//            esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
//            //设置条码高度为 60 点
//            esc.addSetBarcodeHeight((byte) 60);
//
//            String codeStr = String.format("{B%s", curOrder.getBarcode());
//
////            String cmdStr4 = "1D6B49" + String.format("%02X", (byte) (content2.length())) + DataConvertUtil.ByteArrToHex(content2.getBytes(), "");
////            String codeCmd = "1D6B49" + String.format("%02X", (byte) (codeStr.length())) + DataConvertUtil.ByteArrToHex(codeStr.getBytes(), "");
////            esc.addUserCommand(DataConvertUtil.HexToByteArr(codeCmd));
//            esc.addCODE128(codeStr); //打印 Code128 码
////            printBarcode(codeStr);
//        }

        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 订单条码*/
        esc.addText(String.format("订单号:%s \n", curOrder.getBarcode()));
        /**打印 应付款*/
//        esc.addText(String.format("应付款:%.2f \n", orderEntity.getAmount()));
        /**打印 订购日期*/
        esc.addText(String.format("日期:%s \n", TimeCursor.InnerFormat.format(curOrder.getCreatedDate())));
        esc.addPrintAndLineFeed();

        /**打印 商品明细
         *    8       8       4     6      6
         *01234567 89012345 6789 012345 678901 (32)
         * 商品ID    品名   数量   单价   金额
         * */
        esc.addText("商品名                  金额\n");
        esc.addText("--------------------------------\n");//32个
//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
//        Double totalQuantity = 0D, totalAmount = 0D;
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        /**打印 商品明细*/
        List<PosOrderItem> oderItems= curOrder.getItems();
        if (oderItems != null && oderItems.size() > 0){
            for (PosOrderItem entity : oderItems){
                makeTemp(esc, entity.getProductName(), String.format("%.2f", entity.getAmount()));
//                makeEnjoycityTemp(esc, entity.getProductName(), String.format("%.2f", entity.getAmount()));
            }
        }
        esc.addText(String.format("商品小计:%.2f\n", curOrder.getAmount()));

        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        /**打印 结束语*/
        esc.addText("谢谢惠顾!\n");
        esc.addText("欢迎下次光临\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        //获得打印命令
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }

    /**
     * 打印出库单(取件)
     * */
    public static void printStockOut(List<StockOutItem> oderItems){
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
        if (oderItems != null && oderItems.size() > 0){
            for (StockOutItem entity : oderItems){
//                /**打印 商品明细*/
//                makeTemp(esc, entity.getProductName(), String.format("%.2f", entity.getAmount()));

                esc.addText(String.format("%s/%s\n", entity.getHumanName(), entity.getHumanPhone()));
                esc.addText(String.format("%s/%s\n", entity.getBarcode(), entity.getTransportName()));
            }

            esc.addText("--------------------------------\n");//32个
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("总计:%d件\n", oderItems.size()));
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

        //获得打印命令
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }

    /**
     * 打印发货清单
     * */
    public static void printShipList(PosOrder order){
        List<PosOrderItem> orderItems = order.getItems();

        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        esc.addText("发货清单\n");
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
        esc.addText(String.format("收件人: %s\n", order.getReceiveName()));
        esc.addText(String.format("手机号: %s\n", order.getReceivePhone()));
        esc.addText(String.format("地址: %s\n", order.getAddress()));
        esc.addText(String.format("发货时间: %s\n", TimeCursor.InnerFormat.format(new Date())));
        esc.addText(String.format("金额: %.2f[%s]\n", order.getAmount(), order.getPaystatus().equals(1) ? "已支付" : "未支付"));


//        makeTemp(esc, "商品ID", "品名", "数量", "单价", "金额");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        if (orderItems != null && orderItems.size() > 0){
            esc.addText("商品: \n");
            for (PosOrderItem item : orderItems){
//                /**打印 商品明细*/
//                makeTemp(esc, entity.getProductName(), String.format("%.2f", entity.getAmount()));

                makeShipTemp(esc, item.getProductName(), item.getBcount());
            }

            esc.addText("--------------------------------\n");//32个
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        /**打印 结束语*/
//        esc.addText("本单据一式两份，取货发货方各执一份\n");
//        esc.addText("业务电话：400 886 6671\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        //获得打印命令
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }

    /**
     * 打印交接单
     * */
    public static void printHandoverBill(HandOverBill handOverBill){
        if (handOverBill == null){
            return;
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
        esc.addText(String.format("交班时间：%s \n", TimeCursor.InnerFormat.format(handOverBill.getEndDate())));
        esc.addText(String.format("设备编号：%s \n", SharedPreferencesManager.getTerminalId()));
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
        AggWrapper aggWrapper = handOverBill.getAggWrapper();
        int index = 1;
        index = printDailySettleAggItem(esc, index, aggWrapper.getPosItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getLaundryItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getLaundryItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getPijuItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getStockItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getSendItems());
        printDailySettleAggItem(esc, index, aggWrapper.getRechargeItems());

        esc.addPrintAndLineFeed();//进纸一行
        esc.addText("支付类型            数量    金额\n");

        AccWrapper accWrapper = handOverBill.getAccWrapper();
        if (accWrapper == null){
            accWrapper = new AccWrapper();
        }
        int accIndex = 1;
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getCashItem());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getAlipayItem());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getWxItem());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getAccountItem());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getBankItem());
        printDailySettleAccItem(esc, accIndex, accWrapper.getRuleItem());

        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("账户新增:%.2f\n", handOverBill.getIncome()));
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

        //获得打印命令
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }

    /**
     * 打印日结单
     * */
    public static void printDailySettleBill(DailysettleEntity dailysettleEntity){
        if (dailysettleEntity == null){
            return;
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
        esc.addText(dailysettleEntity.getOfficeName());
//        //取消倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);


        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        /**打印 抬头：日结信息*/
//        esc.addText(String.format("班次：%d \n", handOverBill.getShiftId()));
        esc.addText(String.format("日结人:%s\n", dailysettleEntity.getHumanName()));
        esc.addText(String.format("日结时间：%s \n",
                (dailysettleEntity.getDailysettleDate() != null
                        ? TimeCursor.InnerFormat.format(dailysettleEntity.getDailysettleDate())
                        : "")));
        esc.addText(String.format("设备编号：%s \n", SharedPreferencesManager.getTerminalId()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 日结单明细*/
        esc.addText("业务类型            数量    金额\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        AggWrapper aggWrapper = JSON.toJavaObject(JSON.parseObject(dailysettleEntity.getAggData()),
                AggWrapper.class);
        if (aggWrapper == null){
            aggWrapper = new AggWrapper();
        }
        int index = 1;
        index = printDailySettleAggItem(esc, index, aggWrapper.getPosItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getLaundryItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getLaundryItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getPijuItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getStockItems());
        index = printDailySettleAggItem(esc, index, aggWrapper.getSendItems());
        printDailySettleAggItem(esc, index, aggWrapper.getRechargeItems());

        esc.addPrintAndLineFeed();//进纸一行
        esc.addText("支付类型            数量    金额\n");

        AccWrapper accWrapper = JSON.toJavaObject(JSON.parseObject(dailysettleEntity.getAccData()),
                AccWrapper.class);
        if (accWrapper == null){
            accWrapper = new AccWrapper();
        }
        int accIndex = 1;
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getCashItem());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getAlipayItem());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getWxItem());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getAccountItem());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getBankItem());
        printDailySettleAccItem(esc, accIndex, accWrapper.getRuleItem());

        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("现金收取:%.2f\n", dailysettleEntity.getCash()));
        esc.addText(String.format("非现金收取:%.2f\n",
                dailysettleEntity.getTurnover() - dailysettleEntity.getCash()));

        Double turnover = dailysettleEntity.getTurnover();
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

        //获得打印命令
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }

    /**
     * 打印日结经营分析明细
     * */
    private static int printDailySettleAggItem(EscCommand esc, int startIndex, List<AggItem> aggItems){
        if (aggItems != null){
            for (AggItem aggItem : aggItems){
                makeHandoverTemp(esc,
                        String.format("%d %s/%s", startIndex, aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption()),
                        String.format("%.2f", aggItem.getOrderNum()),
                        String.format("%.2f", aggItem.getTurnover()));
                startIndex++;
            }
        }
        return startIndex;
    }

    /**
     * 打印日结流水分析明细
     * */
    private static int printDailySettleAccItem(EscCommand esc, int startIndex,
                                               AccItem accItem){
        if (accItem != null){
            makeHandoverTemp(esc,
                    String.format("%d %s", startIndex, accItem.getPayTypeCaption()),
                    String.format("%.2f", accItem.getOrderNum()),
                    String.format("%.2f", accItem.getAmount()));
            startIndex++;
        }
        return startIndex;
    }



    public static void printTestData(){
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        if (StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())){
            esc.addText("满分家园\n");
        }else{
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
        esc.addText(String.format("%s NO.%s \n", SharedPreferencesManager.getTerminalId(), MUtils.getOrderBarCode()));
        /**打印 订购日期*/
        esc.addText(String.format("%s \n", DATE_FORMAT.format(new Date())));
        //5个数字等于3个汉字（1个数字＝3/5个汉字）
        esc.addText("--------------------------------\n");//32(正确)
        esc.addText("01234567890123456789012345678901\n");//32(正确)
        esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
        esc.addText("零一二三四五六七八九零一二三四五六七八\n");//19.2(错误)

        esc.addText("货号/品名       单价 数量 小计\n");
        esc.addText("货号/品名       单价 数量   小计\n");
        esc.addText("货号/品名          00.0100.0200.03\n");//32=17+5+5+5

        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        for (int i = 0; i < 8; i++){
            makeEnjoycityTemp(esc, String.format("%s/%s", MUtils.getOrderBarCode(), StringUtils.genNonceStringByLength(8)),
                    "12.34", "23.45",  "34.56");
        }

        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("应收:%.2f\n", 88.88));
        esc.addText(String.format("优惠:%.2f\n", 77.77));
//        esc.addText(String.format("代金券:%.2f\n", orderEntity.getCouponDiscountAmount()));
        esc.addText(String.format("合计:%.2f\n", 11.11));
        esc.addText(String.format("付款:%.2f\n", 33.33));
        esc.addText(String.format("找零:%.2f\n", 22.22));

        /**
         * 打印 结束语
         * */
        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        esc.addText("谢谢惠顾!\n");
        esc.addText("欢迎下次光临\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行



        /*打印一维条码code128*/
        // 一维条码：设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);
        //设置条码宽度
//            esc.addSetBarcodeWidth((byte)500);

        //最多打印8位数字
        //选择字符集B
        esc.addCODE128(String.format("{B%s", MUtils.getOrderBarCode())); //打印 Code128 码
        esc.addCODE128(String.format("{C%s", MUtils.getOrderBarCode())); //打印 Code128 码
        esc.addCODE39(String.format("*%s*", MUtils.getOrderBarCode())); //打印 Code39 码"{B" +
        esc.addCODE93(MUtils.getOrderBarCode()); //打印 Code93码"{B" +
        esc.addCODABAR(MUtils.getOrderBarCode()); //打印 Code93码"{B" +

        String codeStr2 = "12359";
        esc.addUserCommand(DataConvertUtil.HexToByteArr(String.format("1D6B49%02X", (byte) (codeStr2.length())) + DataConvertUtil.ByteArrToHex(codeStr2.getBytes(), "")));

        String codeStr3 = "{B01234567{C9876243";
        esc.addUserCommand(DataConvertUtil.HexToByteArr(String.format("1D6B49%02X", (byte) (codeStr3.length())) + DataConvertUtil.ByteArrToHex(codeStr3.getBytes(), "")));

//            String cmdStr4 = "1D6B49" + String.format("%02X", (byte) (content2.length())) + DataConvertUtil.ByteArrToHex(content2.getBytes(), "");
//            String codeCmd = "1D6B49" + String.format("%02X", (byte) (codeStr.length())) + DataConvertUtil.ByteArrToHex(codeStr.getBytes(), "");
//            esc.addUserCommand(DataConvertUtil.HexToByteArr(codeCmd));

//            printBarcode(codeStr);

        /*QRCode 命令打印
        此命令只在支持 QRCode 命令打印的机型才能使用。
        在不支持二维码指令打印的机型上,则需要发送二维条码图片
        */
//        esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); //设置纠错等级
//        esc.addSelectSizeOfModuleForQRCode((byte) 3);//设置 qrcode 模块大小
//        esc.addStoreQRCodeData("www.manfenjiayuan.cn");//设置 qrcode 内容
//        esc.addPrintQRCode();//打印 QRCode
//        esc.addPrintAndLineFeed();

//        /**打印 二维码图片*/
//        try {
//            Bitmap QRCodeBmp = QRCodeUtils.Create2DCode("www.manfenjiayuan.cn");
//            esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
//            esc.addPrintAndLineFeed();
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }

//        /**打印 订单号条形码code128图片*/
////        try {
////            Bitmap QRCodeBmp = QRCodeUtils.CreateCode128ForGPrinter(String.valueOf(orderEntity.getId()), 300, 60);
////            esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
////            esc.addPrintAndLineFeed();
////        } catch (WriterException e) {
////            e.printStackTrace();
////        }

//        /**打印 APP LOGO*/
//        Bitmap b = BitmapFactory.decodeResource(CashierApp.getInstance().getResources(), R.mipmap.ic_launcher);
//        esc.addRastBitImage(b, b.getWidth(), 0);


        //获得打印命令
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }


    /**
     * 打开钱箱
     * */
    public static void openMoneyBox(){
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_STX_M));

        EscCommand esc = new EscCommand();
        esc.addGeneratePluseAtRealtime(TscCommand.FOOT.F2, (byte)20);
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }

    /**
     * 走纸
     * */
    public static void feedPaper(){
        EscCommand esc = new EscCommand();
//                    esc.addPrintAndLineFeed();
        //打印并且走纸多少行
        esc.addPrintAndFeedLines((byte) 5);
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }

//    public EscCommand addCODE128(EscCommand rawEsc, String content) {
//        EscCommand esc = rawEsc;
//        if (esc == null){
//            esc = new EscCommand();
//        }
//        byte[] command = new byte[]{(byte)29, (byte)107, (byte)73, (byte)content.length()};
//        esc.addArrayToCommand(command);
//        esc.addText(content);
//
//        return esc;
//    }

    /**
     * format like "{B01234567"
     * 最多支持打印8位条码
     * */
    public static void printBarcode(String barCode){
        EscCommand esc = new EscCommand();
//        esc.addPrintAndLineFeed();
        /*打印一维条码code128*/
        // 设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);

        String cmdStr4 = "1D6B49" + String.format("%02X", (byte) (barCode.length())) + DataConvertUtil.ByteArrToHex(barCode.getBytes(), "");
//        ZLogger.d(String.format("printBarCode: %s, %s", barCode, cmdStr4));

        esc.addUserCommand(DataConvertUtil.HexToByteArr(cmdStr4));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);

        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }

    public static void printBarcode(){
        EscCommand esc = new EscCommand();
//                    esc.addPrintAndLineFeed();
        /**打印 订单号条形码code128图片*/
//        try {
//            Bitmap QRCodeBmp = QRCodeUtils.CreateCode128ForGPrinter(String.valueOf(orderEntity.getId()), 300, 60);
//            esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
//            esc.addPrintAndLineFeed();
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
        /*打印一维条码code128*/
        // 设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        //设置条码高度为 60 点
        esc.addSetBarcodeHeight((byte) 60);
//        esc.addCODE128("Gprinter"); //打印 Code128 码

//        esc.addText("{BNo.{C123456");

        //CODE B,使用转义字符; CODE C, DEC
        String content1 = "13579";
        String content2 = "{B13579";
//        esc.addText("1D6B49" + content1.length() + "7B42" + "4E6F46" + "7B43" + content1);
//        esc.addText("1D6B49" + "7B43" + content1.length() + "7B42" + "4E6F46" + "7B43" + content1);
//        esc.addCODE128("1D6B49" + "7B42" + "4E6F46" + "7B43" + "123456789");
//        esc.addText(ArrayUtils.toString(DataConvertUtil.HexToByteArr("1D6B49" + Integer.toHexString(content1.length() + 3) + "7B42" + "4E6F46" + "7B43" + content1)));

//        esc.addText(ArrayUtils.toString(DataConvertUtil.HexToByteArr("1D6B49" + Integer.toHexString(content1.length()) +  "7B43" + content1)));
//        esc.addCODE128("7B42" + "4E6F46" + "7B43" + "30313233343536");
        //CODE B,使用转义字符; CODE C, DEC
//        esc.addCODE128("1D6B49" + "7B42" + "4E6F46" + "7B43" + "30313233343536");

//        EventBus.getDefault().post(new SerialPortEvent(2, bytes));
//
//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr("1D6B49"
//                + DataConvertUtil.Byte2Hex(Byte.valueOf(String.valueOf(content1.length()))) +  "7B43" + DataConvertUtil.Chr2Hex(content1))));
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr("1D6B49" + "09" + "7B42" + "4E6F" + "7B43" + "30313233343536")));
//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr("1D6B49" + "09" + "7B42" + "35363031323334")));

        String lenStr = String.format("%02X", (byte)content1.length());
//        String cmdStr = "1D6B49" + lenStr + "7B43" + DataConvertUtil.ByteArrToHex(content1.getBytes(), "");
//        String cmdStr2 = "1D6B49" + lenStr + "7B42" + DataConvertUtil.ByteArrToHex(content1.getBytes(), "");
//        String cmdStr3 = "7B42" + DataConvertUtil.ByteArrToHex(content1.getBytes(), "");
        String cmdStr4 = "1D6B49" + String.format("%02X", (byte) (content2.length())) + DataConvertUtil.ByteArrToHex(content2.getBytes(), "");
//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr(cmdStr)));

//        esc.addText(cmdStr);
        esc.addUserCommand(DataConvertUtil.HexToByteArr(cmdStr4));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));

//        EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr("1D6B49" + Integer.toHexString(content1.length()) + "7B43" + DataConvertUtil.Chr2Hex(content1))));
//        EventBus.getDefault().post(new SerialPortEvent(3, "1D6B490A7B424E6F7B4330313233343536"));
//        EventBus.getDefault().post(new SerialPortEvent(3, cmdStr));
//        try {
//            EventBus.getDefault().post(new SerialPortEvent(2, cmdStr.getBytes("GB2312")));
//            EventBus.getDefault().post(new SerialPortEvent(2, DataConvertUtil.HexToByteArr(cmdStr)));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 打印 商品明细
     *
     *012345678901234567890123 45678901
     *         品名               金额
     * */
    private static EscCommand makeShipTemp(EscCommand rawEsc, String name, Double count){
        EscCommand esc = rawEsc;
        if (esc == null){
            esc = new EscCommand();
        }

        int maxLine = Math.max(1, (name == null ? 0 : (getLength(name) - 1) / PRINT_PRODUCT_NAME_MAX_LEN + 1));

//        ZLogger.d(String.format("maxLine=%d", maxLine));
        String nameTemp = name;

        int mid = maxLine / 2;
        for (int i = 0; i < maxLine; i++){
            StringBuilder line = new StringBuilder();

//            ZLogger.d(String.format("nameTemp=%s", nameTemp));
//            ZLogger.d(String.format("nameTemp: %d(%d)", getLength(nameTemp), nameTemp.toCharArray().length));
            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, getLength(nameTemp)));
//            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, nameTemp.toCharArray().length));
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
            nameTemp = nameTemp.substring(sub2.length(), nameTemp != null ? nameTemp.length() : 0).trim();
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
//            line.append(formatLong(sub2, 8));
            line.append(sub2).append(addBlank(Math.max(PRINT_PRODUCT_NAME_MAX_LEN - getLength(sub2), 0)));

            if (i == mid){
                line.append(formatShort(String.valueOf(count), PRINT_PRODUCT_NAME_MAX_LEN, BLANK_GRAVITY.NONE));
            }

            line.append("\n");
//            ZLogger.d(String.format("print line(%d/%d):%s" , i, mid, line.toString()));
            esc.addText(line.toString());
        }

        return esc;
    }

    /**
     * 打印 商品明细（城市之间）
     * 5个数字等于3个汉字（1个数字＝3/5个汉字）
     * esc.addText("--------------------------------\n");//32(正确)
     * esc.addText("01234567890123456789012345678901\n");//32(正确)
     * esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
     * esc.addText("零一二三四五六七八九零一二三四五六七八\n");//19.2(错误)
     *
     * esc.addText("01234567890123456789012345678901\n");//32(正确)
     * esc.addText("商品名           00.0100.02000.03\n");32=16+5+5+6
     *
     * esc.addText("零一二三四五六七八九零一二三四五\n");//16(正确)
     * esc.addText("货号/品名       单价 数量 小计\n");
     * esc.addText("货号/品名       单价 数量   小计\n");
     * esc.addText("01234567890123456789012345678901\n");//32(正确)
     * esc.addText("货号/品名           单价数量   小计\n");
     * esc.addText("货号/品名        单价数量   小计\n");
     * esc.addText("货号/品名       单价 数量   小计\n");
     * esc.addText("业务类型            数量    金额\n");
     * */
    private static EscCommand makeEnjoycityTemp(EscCommand rawEsc, String name, String price, String bcount, String amount){
        EscCommand esc = rawEsc;
        if (esc == null){
            esc = new EscCommand();
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //最多显示17*0.6=10.2个汉字
        if (getLength(name) > 16){
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s%s", formatShort(price, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(bcount, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(amount, 6, BLANK_GRAVITY.LEFT)));
        }
        else{
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s%s",
                    formatShort(name, 16, BLANK_GRAVITY.RIGHT),
                    formatShort(price, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(bcount, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(amount, 6, BLANK_GRAVITY.LEFT));
            esc.addText(printText);

//            ZLogger.d("printText:" + printText);
        }
        esc.addText("\n");

        return esc;
    }

    /**
     * 打印 商品明细（城市之间）
     *
     * esc.addText("012345678901234567890123456789\n");//20+6+6=32
     * esc.addText("商品名          00.0011.1122.22\n");
     * esc.addText("商品名               数量  金额\n");
     *012345678901234567890123 45678901
     *        商品名               金额
     * */
    private static EscCommand makeHandoverTemp(EscCommand rawEsc, String name, String bcount, String amount){
        EscCommand esc = rawEsc;
        if (esc == null){
            esc = new EscCommand();
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //计算名称行数
        if (getLength(name) > 20){
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s", formatShort(bcount, 6, BLANK_GRAVITY.RIGHT),
                    formatShort(amount, 6, BLANK_GRAVITY.LEFT)));
        }
        else{
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s", formatShort(name, 20, BLANK_GRAVITY.RIGHT),
                    formatShort(bcount, 6, BLANK_GRAVITY.RIGHT),
                    formatShort(amount, 6, BLANK_GRAVITY.LEFT));
//            ZLogger.d("printText:" + printText);
            esc.addText(printText);
        }
        esc.addText("\n");

        return esc;
    }

    private static EscCommand makeTemp(EscCommand rawEsc, String name, String amount){
        EscCommand esc = rawEsc;
        if (esc == null){
            esc = new EscCommand();
        }

        //计算行数
        int maxLine = Math.max(1, (name == null ? 0 : (getLength(name) - 1) / PRINT_PRODUCT_NAME_MAX_LEN + 1));
//        ZLogger.d(String.format("maxLine=%d", maxLine));

        String nameTemp = name;

        int mid = maxLine / 2;
        for (int i = 0; i < maxLine; i++){
            StringBuilder line = new StringBuilder();

//            ZLogger.d(String.format("nameTemp: %d(%d)", getLength(nameTemp), nameTemp.toCharArray().length));
            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, getLength(nameTemp)));
//            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, nameTemp.toCharArray().length));
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
            nameTemp = nameTemp.substring(sub2.length(),
                    nameTemp != null ? nameTemp.length() : 0).trim();
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
//            line.append(formatLong(sub2, 8));

            //插入名称，不足补空格
            line.append(sub2).append(addBlank(Math.max(PRINT_PRODUCT_NAME_MAX_LEN - getLength(sub2), 0)));

            //中间一行插入金额
            if (i == mid){
                line.append(formatShort(amount, PRINT_PRODUCT_NAME_MAX_LEN, BLANK_GRAVITY.NONE));
            }

            line.append("\n");
//            ZLogger.d(String.format("print line(%d/%d):%s" , i, mid, line.toString()));
            esc.addText(line.toString());
        }
//        esc.addText("--------------------------------\n");//32个

//        Vector<Byte> datas = esc.getCommand();
//        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
//        byte[] bytes = ArrayUtils.toPrimitive(Bytes);

        return esc;
    }

    /**
     * 打印 商品明细
     *
     *01234567 89012345 6789 012345 678901
     *  商品ID    品名   数量   单价   金额
     * */
    private static EscCommand makeTemp2(EscCommand rawEsc, String id,
                                        String name, String quantity, String price, String amount){
        EscCommand esc = rawEsc;
        if (esc == null){
            esc = new EscCommand();
        }

//        int maxLine = Math.max(1, (name == null ? 0 : (getLength(name) - 1) / 4 + 1));
//        int nameLen = name.length();
        char[] nameChars = name.trim().toCharArray();
        int len = nameChars.length;

        getLength(name);
//        ZLogger.d(String.format("text=%s, len=%d", name, name.length()));
//        int maxLine = Math.max(1, len/7);
        int maxLine = Math.max(1,((getLength(name) - 1) / 8 + 1));

//        ZLogger.d(String.format("maxLine=%d", maxLine));
        String nameTemp = name;

        int mid = maxLine / 2;
        for (int i = 0; i < maxLine; i++){
            StringBuilder line = new StringBuilder();
            if (i == mid){
                line.append(formatShort(id, 8, BLANK_GRAVITY.LEFT));
            }else{
                line.append(addBlank(8));
            }
//            String subId = DataConvertUtil.subString(id, Math.min(8, getLength(nameTemp)));

//            ZLogger.d(String.format("nameTemp=%s", nameTemp));
            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(8, getLength(nameTemp)));
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
            nameTemp = nameTemp.substring(sub2.length(), nameTemp.length()).trim();
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
//            line.append(formatLong(sub2, 8));
            line.append(sub2).append(addBlank(Math.max(8 - getLength(sub2), 0)));
//
//            try {
//                byte[] nameBs = name.getBytes("GB2312");
//                if (i == maxLine -1){
//                    byte[] subBs = Arrays.copyOfRange(nameBs, 8 * i, nameBs.length - 1);
////                    String sub = new String(Base64.encode(nameBs, 8 * i, nameBs.length - 8*i, 0), "GB2312");
//                    String sub = new String(subBs, "GB2312");
//                    ZLogger.d(String.format("name=%s subName=%s subName2=%s nameTemp=%s", name, sub, sub2, nameTemp));
//                    line.append(formatShort(sub, 8, true));
//                }else{
//                    byte[] subBs = Arrays.copyOfRange(nameBs, 8 * i, Math.min(8 * i+8, nameBs.length-1));
////                    String sub = new String(Base64.encode(nameBs, 8 * i, 8, 0), "GB2312");
//                    String sub = new String(subBs, "GB2312");
//                    ZLogger.d(String.format("name=%s subName=%s subName2=%s nameTemp=%s", name, sub, sub2, nameTemp));
//                    line.append(formatShort(sub, 8, true));
//                }
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }

//            String subName = name.substring(8*i, Math.min(8*i + 8, name.length()-1));
////            String subName = String.valueOf(nameChars, 7 * i, 7);
//            ZLogger.d(String.format("name=%s subName=%s", name, subName));
//            line.append(formatShort(subName, 8, true));

            if (i == mid){
                line.append(formatShort(quantity, 4, BLANK_GRAVITY.LEFT));
                line.append(formatShort(price, 6, BLANK_GRAVITY.LEFT));
                line.append(formatShort(amount, 6, BLANK_GRAVITY.NONE));
            }

            line.append("\n");
//            ZLogger.d(String.format("print line(%d/%d):%s" , i, mid, line.toString()));
            esc.addText(line.toString());
        }
        esc.addText("--------------------------------\n");//32个

//        Vector<Byte> datas = esc.getCommand();
//        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
//        byte[] bytes = ArrayUtils.toPrimitive(Bytes);

        return esc;
    }

    /**
     * 空格对齐方式
     * **/
    public enum BLANK_GRAVITY {
        NONE(0),
        LEFT(1),
        CENTER(2),
        RIGHT(3);

        private final int value;

        BLANK_GRAVITY(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)this.value;
        }
    }
    /**
     * */
    public static String formatShort(String raw, int maxWidth, BLANK_GRAVITY blankGravity){
//        Pattern p = Pattern.compile("[0-9]*");
//        p=Pattern.compile("[\u4e00-\u9fa5]");

//        String formated = raw.trim();
//        char[] rawChars = raw.trim().toCharArray();
//        int len = rawChars.length;
        int len = getLength(raw);
        String subStr = DataConvertUtil.subString(raw, Math.min(len, maxWidth));//截取字符串 String.valueOf(rawChars, 0, len2)
        String blankStr = addBlank(Math.max(maxWidth - len, 0));
//        ZLogger.d(String.format("subString([%s%s]", subStr, blankStr));
        if (len > maxWidth){
            return subStr;
        }
        else{
            //右对齐，在前面补空格
            if (blankGravity == BLANK_GRAVITY.LEFT){
                return blankStr +  subStr;
            }
            //左对齐，在后面补空格
            else if (blankGravity == BLANK_GRAVITY.RIGHT){
                return subStr + blankStr;
            }else{
                return subStr;
            }
        }
    }


    /**
     * 添加空格
     * */
    private static String addBlank(int len){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++){
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * 返回字节数
     * */
    private static int getLength(String text){
        if (StringUtils.isEmpty(text)){
            return 0;
        }

        int len = 0;
        if(!text.equals("")) {
            try {
                byte[] bs = text.getBytes("GB2312");
                len = bs.length;
            } catch (UnsupportedEncodingException var4) {
                var4.printStackTrace();
                ZLogger.e(var4.toString());
            }
        }
//        ZLogger.d(String.format("text=%s, len=%d", text, len));

        return len;
    }

    /**
     * 显示 标签 ＋ 金额
     * */
    public static void vfdShow(String text){
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD_BYTE, SerialManager.VFD_CH(text)));
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD, text));
    }


//    Rockchip
    public static void show(int sn, Double amount){
        if (sn == 1){
        //更新显示屏,显示'单价'字符
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_ESC_S_1));
        }
        else if (sn == 2){
            //更新显示屏,显示'总计'字符
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_ESC_S_2));
        }
        else if (sn == 3){
            //更新显示屏,显示'收款'字符
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_ESC_S_3));
        }
        else if (sn == 4){
            //更新显示屏,显示'找零'字符
            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_ESC_S_4));
        }
//        //更新显示屏,显示'单价''总计'字符
//        EventBus.getDefault().post(new SerialPortEvent(0, CommandConstants.CMD_HEX_STX_L
//                + CommandConstants.HEX_0 + CommandConstants.HEX_1
//                + CommandConstants.HEX_1 + CommandConstants.HEX_0));

        //更新显示屏,显示商品价格
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_ESC_Q_A
                + showNumber(amount) + CommandConstants.HEX_CR));
    }

    public static void clear(){
        //清除屏幕上的字符
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_DISPLAY, CommandConstants.CMD_HEX_CLR));
//        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_VFD, ""));
        SerialManager.vfdShow("WELCOME");
    }

    public static String showNumber(Double number){
        if (number == null){
            return "";
        }

        StringBuilder sb = new StringBuilder();

        if (number > 0){
            String numberStr = String.format("%.2f", number);
            char[] numberCharArr = numberStr.toCharArray();
            int len = numberCharArr.length;
            for (char c : numberCharArr){
//                sb.append(String.valueOf(numberCharArr[i]));
                sb.append(Integer.toHexString((int)c));
            }
        }else{
            String numberStr = MUtils.formatDouble(number, "");
            char[] numberCharArr = numberStr.toCharArray();
            int len = numberCharArr.length;
            for (char c : numberCharArr){
//                sb.append(String.valueOf(numberCharArr[i]));
                sb.append(Integer.toHexString((int)c));
            }
        }

        return sb.toString();
    }

    /**
     * 将字符串转换成VFD格式显示
     *
     * 0x1b0x40 -- Max add : Initialize device(清空)
     */
    public static byte[] VFD(String displayText){
        EscCommand esc = new EscCommand();

//        String cmdStr4 = "1B40" + String.format("%02X", (byte) (displayText.length())) + DataConvertUtil.ByteArrToHex(displayText.getBytes(), "");
        String cmdStr4 = "1B40" + DataConvertUtil.ByteArrToHex(displayText.getBytes(), "");
//        ZLogger.d(String.format("VFD: %s, %s", displayText, cmdStr4));
        esc.addUserCommand(DataConvertUtil.HexToByteArr(cmdStr4));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        return ArrayUtils.toPrimitive(Bytes);
    }

    /**
     * */
    public static byte[] VFD_CH(String displayText){
        EscCommand esc = new EscCommand();

        String cmdStr4  = "1B40" + DataConvertUtil.ByteArrToHex(displayText.getBytes(), "");
//        String cmdStr4;
//        try {
//            cmdStr4 = "1B40" + DataConvertUtil.ByteArrToHex(displayText.getBytes("GB2312"), "");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            cmdStr4 = "1B40";
//        }
//        ZLogger.d(String.format("VFD: %s, %s", displayText, cmdStr4));
        esc.addUserCommand(DataConvertUtil.HexToByteArr(cmdStr4));
//        esc.addUserCommand(new byte[]{'\r'});
//        esc.addUserCommand(new byte[]{'\n'});
//        esc.addUserCommand(DataConvertUtil.HexToByteArr("111"));

        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        return ArrayUtils.toPrimitive(Bytes);
    }

}
