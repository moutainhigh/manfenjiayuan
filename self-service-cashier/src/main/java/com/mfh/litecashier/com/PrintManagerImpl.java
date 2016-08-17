package com.mfh.litecashier.com;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.database.entity.DailysettleEntity;
import com.gprinter.command.EscCommand;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.AggItem;
import com.mfh.litecashier.bean.PosOrder;
import com.mfh.litecashier.bean.PosOrderItem;
import com.mfh.litecashier.bean.StockOutItem;
import com.mfh.litecashier.bean.wrapper.AccWrapper;
import com.mfh.litecashier.bean.wrapper.AggWrapper;
import com.mfh.litecashier.bean.wrapper.HandOverBill;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by bingshanguxue on 8/12/16.
 */
public class PrintManagerImpl extends PrintManager {

    private static EscCommand makeEsc(PosOrder curOrder) {
        if (curOrder == null) {
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
        List<PosOrderItem> oderItems = curOrder.getItems();
        if (oderItems != null && oderItems.size() > 0) {
            for (PosOrderItem entity : oderItems) {
                makePosOrderTemp(esc, entity.getProductName(), String.format("%.2f", entity.getAmount()));
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

        return esc;
    }

    private static EscCommand makePosOrderTemp(EscCommand rawEsc, String name, String amount) {
        EscCommand esc = rawEsc;
        if (esc == null) {
            esc = new EscCommand();
        }
        try {
//计算行数
            int maxLine = Math.max(1, (name == null ? 0 : (getLength(name) - 1) / PRINT_PRODUCT_NAME_MAX_LEN + 1));
//        ZLogger.d(String.format("maxLine=%d", maxLine));

            String nameTemp = name;

            int mid = maxLine / 2;
            for (int i = 0; i < maxLine; i++) {
                StringBuilder line = new StringBuilder();

//            ZLogger.d(String.format("nameTemp: %d(%d)", getLength(nameTemp), nameTemp.toCharArray().length));
                String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, getLength(nameTemp)));
//            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, nameTemp.toCharArray().length));
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
                assert nameTemp != null;
                nameTemp = nameTemp.substring(sub2.length(),
                        nameTemp != null ? nameTemp.length() : 0).trim();
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
//            line.append(formatLong(sub2, 8));

                //插入名称，不足补空格
                line.append(sub2).append(genBlankspace(Math.max(PRINT_PRODUCT_NAME_MAX_LEN - getLength(sub2), 0)));

                //中间一行插入金额
                if (i == mid) {
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

        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }


        return esc;
    }

    /**
     * 打印线上订单
     */
    public static void printPosOrder(final PosOrder curOrder, boolean withCode128) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeEsc(curOrder));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        print(escCommand);
                    }
                });
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
    private static EscCommand makeTestTemp(EscCommand rawEsc, String name, String price, String bcount, String amount) {
        EscCommand esc = rawEsc;
        if (esc == null) {
            esc = new EscCommand();
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //最多显示17*0.6=10.2个汉字
        if (getLength(name) > 16) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s%s", formatShort(price, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(bcount, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(amount, 6, BLANK_GRAVITY.LEFT)));
        } else {
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

    private static EscCommand makeEsc() {
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        if (StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())) {
            esc.addText("满分家园\n");
        } else {
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
        for (int i = 0; i < 8; i++) {
            makeTestTemp(esc, String.format("%s/%s", MUtils.getOrderBarCode(), StringUtils.genNonceStringByLength(8)),
                    "12.34", "23.45", "34.56");
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

        return esc;
    }

    /**
     * 打印测试
     */
    public static void printTest() {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeEsc());
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        print(escCommand);
                    }
                });
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
        if (accWrapper == null) {
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

        return esc;
    }

    /**
     * 打印日结经营分析明细
     */
    private static int printDailySettleAggItem(EscCommand esc, int startIndex, List<AggItem> aggItems) {
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
                                               AccItem accItem) {
        if (accItem != null) {
            makeHandoverTemp(esc,
                    String.format("%d %s", startIndex, accItem.getPayTypeCaption()),
                    String.format("%.2f", accItem.getOrderNum()),
                    String.format("%.2f", accItem.getAmount()));
            startIndex++;
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
    private static EscCommand makeHandoverTemp(EscCommand rawEsc, String name, String bcount, String amount) {
        EscCommand esc = rawEsc;
        if (esc == null) {
            esc = new EscCommand();
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //计算名称行数
        if (getLength(name) > 20) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s", formatShort(bcount, 6, BLANK_GRAVITY.RIGHT),
                    formatShort(amount, 6, BLANK_GRAVITY.LEFT)));
        } else {
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        print(escCommand);
                    }
                });
    }

    private static EscCommand makeEsc(DailysettleEntity dailysettleEntity) {
        if (dailysettleEntity == null) {
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
        if (aggWrapper == null) {
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
        if (accWrapper == null) {
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

        return esc;
    }

    /**
     * 打印日结单
     */
    public static void printDailySettleBill(final DailysettleEntity dailysettleEntity) {

        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeEsc(dailysettleEntity));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        print(escCommand);
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<EscCommand>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EscCommand escCommand) {
                        print(escCommand);
                    }
                });
    }


}
