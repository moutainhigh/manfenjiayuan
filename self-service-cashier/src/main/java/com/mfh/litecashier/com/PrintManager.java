package com.mfh.litecashier.com;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.hardware.printer.GPrinterAgent;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PayWay;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.gprinter.command.EscCommand;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.invSendOrder.InvSendOrder;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.bean.wrapper.FreshScheduleGoods;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by bingshanguxue on 6/22/16.
 */
public class PrintManager {
    public static java.text.SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US);

    /**
     * 空格对齐方式
     **/
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
            return (byte) this.value;
        }
    }

    /**
     * 返回字节数
     */
    public static int getLength(String text) {
        if (StringUtils.isEmpty(text)) {
            return 0;
        }

        int len = 0;
        if (!text.equals("")) {
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
     * */
    public static String formatShort(String raw, int maxWidth, BLANK_GRAVITY blankGravity) {
//        Pattern p = Pattern.compile("[0-9]*");
//        p=Pattern.compile("[\u4e00-\u9fa5]");

//        String formated = raw.trim();
//        char[] rawChars = raw.trim().toCharArray();
//        int len = rawChars.length;
        int len = getLength(raw);
        String subStr = DataConvertUtil.subString(raw, Math.min(len, maxWidth));//截取字符串 String.valueOf(rawChars, 0, len2)
        String blankStr = StringUtils.genBlankspace(Math.max(maxWidth - len, 0));
//        ZLogger.d(String.format("subString([%s%s]", subStr, blankStr));
        if (len > maxWidth) {
            return subStr;
        } else {
            //右对齐，在前面补空格
            if (blankGravity == BLANK_GRAVITY.LEFT) {
                return blankStr + subStr;
            }
            //左对齐，在后面补空格
            else if (blankGravity == BLANK_GRAVITY.RIGHT) {
                return subStr + blankStr;
            } else {
                return subStr;
            }
        }
    }



    /**
     * 订单明细模版1
     * */
    public static EscCommand makeOrderItem1(EscCommand rawEsc, String name, String unit,
                                            String bcount) {
        EscCommand esc = rawEsc;
        if (esc == null) {
            esc = new EscCommand();
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //最多显示17*0.6=10.2个汉字
        if (getLength(name) > 21) {
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s", formatShort(unit, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(bcount, 5, BLANK_GRAVITY.LEFT)));
        } else {
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s",
                    formatShort(name, 22, BLANK_GRAVITY.RIGHT),
                    formatShort(unit, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(bcount, 5, BLANK_GRAVITY.LEFT));
            esc.addText(printText);

//            ZLogger.d("printText:" + printText);
        }
        esc.addText("\n");

        return esc;
    }



    /**
     * 打印生鲜预定订单
     * */
    private static EscCommand makeScheduleOrderEsc(InvSendOrder invSendOrder,
                                                   List<FreshScheduleGoods> goodsList) {
        if (invSendOrder == null || goodsList == null || goodsList.size() < 1) {
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
        if (StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())) {
            esc.addText("线上预定订单\n");
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
        esc.addText(String.format("%s \n", invSendOrder.getName()));
        /**打印 订购日期*/
        esc.addText(String.format("下单时间：%s \n", TimeUtil.format(invSendOrder.getCreatedDate(),
                TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("姓名：%s \n", invSendOrder.getContact()));
        esc.addText(String.format("电话：%s \n", invSendOrder.getReceiveMobile()));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 商品明细*/
        esc.addText("货号/品名           单位   数量\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        for (FreshScheduleGoods entity : goodsList) {
            makeOrderItem1(esc, String.format("%s/%s", entity.getBarcode(), entity.getProductName()),
                    entity.getBuyUnit(), MUtils.formatDouble(entity.getAskTotalCount(), ""));
        }

//        /**
//         * 打印合计信息
//         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
//        esc.addText(String.format("金额:%.2f\n", invSendOrder.getGoodsFee()));
//        esc.addText(String.format("优惠:%.2f\n", orderEntity.getDiscountAmount() + orderEntity.getCouponDiscountAmount()));
////        esc.addText(String.format("代金券:%.2f\n", orderEntity.getCouponDiscountAmount()));
//        esc.addText(String.format("合计:%.2f\n", CashierHelper.getPayableAmount(orderEntity)));
//        esc.addText(String.format("付款:%.2f\n", orderEntity.getPaidMoney()));
//        esc.addText(String.format("找零:%.2f\n", Math.abs(orderEntity.getCharge())));

        /**
         * 打印 结束语
         * */
        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        esc.addText("请拣货时仔细核对商品数量!\n");
        esc.addText("谢谢配合\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }


    /**
     * 打印生鲜预定订单
     * 适用场景：线上订单－生鲜预定－确认订单并打印
     */
    public static void printScheduleOrder(final InvSendOrder invSendOrder, final List<FreshScheduleGoods> goodsList) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeScheduleOrderEsc(invSendOrder, goodsList));
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
                        GPrinterAgent.print(escCommand);
                    }
                });
    }

    private static EscCommand makePosOrderEsc(PosOrderEntity posOrderEntity){
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
        esc.addText(String.format("收银时间:%s/%s \n",
                MfhLoginService.get().getLoginName(), MfhLoginService.get().getTelephone()));
        esc.addPrintAndLineFeed();

        /**打印 商品明细*/
        esc.addText("货号/品名       单价 数量   小计\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
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
        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        String footerText = String.format("%s%s",
                formatShort("米西厨房", 20, BLANK_GRAVITY.RIGHT),
                formatShort("400 8866 671", 12, BLANK_GRAVITY.LEFT));
        esc.addText(footerText);
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
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
    private static EscCommand makePosOrderLine(EscCommand rawEsc, String name,
                                               String price, String bcount, String amount) {
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

    /**
     * 打印POS订单流水
     */
    public static void printPosOrder(final PosOrderEntity posOrderEntity, boolean withCode128) {

        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makePosOrderEsc(posOrderEntity));
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
                        GPrinterAgent.print(escCommand);
                    }
                });
    }

    private static EscCommand makeTopupEsc(QuickPayInfo mQuickPayInfo, String outTradeNo) {
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

//
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


    /**
     * 打印充值单据
     * */
    public static void printTopupReceipt(final QuickPayInfo mQuickPayInfo, final String outTradeNo){
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeTopupEsc(mQuickPayInfo, outTradeNo));
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
                        GPrinterAgent.print(escCommand);
                    }
                });
    }





}
