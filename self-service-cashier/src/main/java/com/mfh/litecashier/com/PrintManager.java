package com.mfh.litecashier.com;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.gprinter.command.EscCommand;
import com.manfenjiayuan.business.bean.InvSendOrder;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.bean.wrapper.FreshScheduleGoods;
import com.mfh.litecashier.event.SerialPortEvent;

import org.apache.commons.lang3.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import de.greenrobot.event.EventBus;

/**
 * Created by bingshanguxue on 6/22/16.
 */
public class PrintManager {

    /**
     * 打印生鲜预定订单
     * 适用场景：线上订单－生鲜预定－确认订单并打印
     * */
    public static void printScheduleOrder(InvSendOrder invSendOrder, List<FreshScheduleGoods> goodsList){
        if (invSendOrder == null || goodsList == null || goodsList.size() < 1){
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
        if (StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())){
            esc.addText("购物清单\n");
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
        esc.addText(String.format("%s \n", invSendOrder.getName()));
        /**打印 订购日期*/
        esc.addText(String.format("%s \n", TimeUtil.format(invSendOrder.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 商品明细*/
        esc.addText("货号/品名           单位   数量\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        for (FreshScheduleGoods entity : goodsList){
            makeSheduleLine(esc, String.format("%s/%s", entity.getBarcode(), entity.getProductName()),
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

        //获得打印命令
        Vector<Byte> datas = esc.getCommand();
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.SERIAL_TYPE_PRINTER, bytes));
    }


    private static EscCommand makeSheduleLine(EscCommand rawEsc, String name, String unit,
                                              String bcount){
        EscCommand esc = rawEsc;
        if (esc == null){
            esc = new EscCommand();
        }

        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐

        //最多显示17*0.6=10.2个汉字
        if (getLength(name) > 21){
            esc.addText(name);//显示名称
            esc.addText("\n");
            //另起一行显示单价/数量/小计，居右显示
            esc.addSelectJustification(EscCommand.JUSTIFICATION.RIGHT);//设置打印左对齐
            esc.addText(String.format("%s%s", formatShort(unit, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(bcount, 5, BLANK_GRAVITY.LEFT)));
        }
        else{
            //在名称后面显示单价/数量/小计
            String printText = String.format("%s%s%s",
                    formatShort(name, 21, BLANK_GRAVITY.RIGHT),
                    formatShort(unit, 5, BLANK_GRAVITY.RIGHT),
                    formatShort(bcount, 5, BLANK_GRAVITY.LEFT));
            esc.addText(printText);

//            ZLogger.d("printText:" + printText);
        }
        esc.addText("\n");

        return esc;
    }


    /**
     * 打印POS订单流水
     * */
    public static void printPosOrder(List<PosOrderEntity> orderEntities, boolean withCode128){
        if (orderEntities == null){
            return;
        }

        PosOrderEntity firstOrderEntity = orderEntities.get(0);

        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        if (StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())){
            esc.addText("购物清单\n");
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
        esc.addText(String.format("%s NO.%s \n",
                SharedPreferencesManager.getTerminalId(), firstOrderEntity.getBarCode()));
        /**打印 订购日期*/
        esc.addText(String.format("%s \n",
                TimeUtil.format(firstOrderEntity.getCreatedDate(),
                        TimeCursor.FORMAT_YYYYMMDDHHMMSS)));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 商品明细*/
        esc.addText("货号/品名       单价 数量   小计\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

        List<PosOrderItemEntity> posOrderItemEntityList = CashierFactory.fetchOrderItems(orderEntities);
        if (posOrderItemEntityList != null && posOrderItemEntityList.size() > 0){
            for (PosOrderItemEntity entity : posOrderItemEntityList){
//                makeTemp(esc, entity.getName(), String.format("%.2f", entity.getCostPrice() * entity.getBcount()));
                makePosOrderLine(esc, String.format("%s/%s", entity.getBarcode(),
                        entity.getName()), String.format("%.2f", entity.getFinalPrice()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getFinalAmount()));
            }
        }

        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
        Double finalAmount = 0D, ruleDis = 0D, paidAmount = 0D, payableAmount = 0D, change = 0D;
        for (PosOrderEntity orderEntity : orderEntities){
            ZLogger.df(JSONObject.toJSONString(orderEntity));
            finalAmount += orderEntity.getFinalAmount();
            ruleDis += orderEntity.getRuleDiscountAmount();
            paidAmount += orderEntity.getPaidAmount();
            Double payableTemp = orderEntity.getFinalAmount() - orderEntity.getRuleDiscountAmount();
            if (payableTemp < 0.01){
                payableTemp = 0D;
            }
            payableAmount += payableTemp;

            change += orderEntity.getChange();
        }
        esc.addText(String.format("合计:%.2f\n", finalAmount));
        esc.addText(String.format("优惠:%.2f\n", ruleDis));
//        esc.addText(String.format("代金券:%.2f\n", orderEntity.getCouponDiscountAmount()));
        esc.addText(String.format("应收:%.2f\n", payableAmount));
        esc.addText(String.format("付款:%.2f\n", paidAmount - ruleDis));
        esc.addText(String.format("找零:%.2f\n", Math.abs(change)));

        /**
         * 打印 结束语
         * */
        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
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
     * 打印POS订单流水
     * */
    public static void printPosOrder(PosOrderEntity posOrderEntity, boolean withCode128){
        ZLogger.df(JSONObject.toJSONString(posOrderEntity));
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        /**打印 标题*/
        if (StringUtils.isEmpty(MfhLoginService.get().getCurOfficeName())){
            esc.addText("购物清单\n");
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
        esc.addText(String.format("%s NO.%s \n", SharedPreferencesManager.getTerminalId(),
                posOrderEntity.getBarCode()));
        /**打印 订购日期*/
        esc.addText(String.format("%s \n",
                TimeUtil.format(posOrderEntity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMMSS)));
        esc.addText("--------------------------------\n");//32个
//        esc.addPrintAndLineFeed();

        /**打印 商品明细*/
        esc.addText("货号/品名       单价 数量   小计\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        List<PosOrderItemEntity> posOrderItemEntityList = CashierFactory.fetchOrderItems(posOrderEntity);
        if (posOrderItemEntityList != null && posOrderItemEntityList.size() > 0){
            for (PosOrderItemEntity entity : posOrderItemEntityList){
//                makeTemp(esc, entity.getName(), String.format("%.2f", entity.getCostPrice() * entity.getBcount()));
                makePosOrderLine(esc, String.format("%s/%s", entity.getBarcode(),
                        entity.getName()), String.format("%.2f", entity.getFinalPrice()),
                        String.format("%.2f", entity.getBcount()),
                        String.format("%.2f", entity.getFinalAmount()));
            }
        }

        /**
         * 打印合计信息
         * */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
        esc.addText("--------------------------------\n");//32个
        esc.addText(String.format("合计:%.2f\n", posOrderEntity.getFinalAmount()));
        esc.addText(String.format("优惠:%.2f\n", posOrderEntity.getRuleDiscountAmount()));
//        esc.addText(String.format("代金券:%.2f\n", orderEntity.getCouponDiscountAmount()));
        esc.addText(String.format("应收:%.2f\n", posOrderEntity.getFinalAmount() - posOrderEntity.getRuleDiscountAmount()));
        esc.addText(String.format("付款:%.2f\n", posOrderEntity.getPaidAmount() - posOrderEntity.getRuleDiscountAmount()));
        esc.addText(String.format("找零:%.2f\n", Math.abs(posOrderEntity.getChange())));

        /**
         * 打印 结束语
         * */
        esc.addPrintAndLineFeed();
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
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
    private static EscCommand makePosOrderLine(EscCommand rawEsc, String name, String price, String bcount, String amount){
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

    private static String makePosOrderLine(String name, String price, String bcount, String amount){
        StringBuilder sb = new StringBuilder();

        //最多显示17*0.6=10.2个汉字
        if (getLength(name) > 16){
            sb.append(name);//显示名称
            sb.append("\n");
            sb.append(String.format("%s%s%s", formatShort(price, 5, BLANK_GRAVITY.RIGHT),
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
            sb.append(printText);

//            ZLogger.d("printText:" + printText);
        }
        sb.append("\n");

        return sb.toString();
    }


    /**
     * 返回字节数
     * */
    public static int getLength(String text){
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
     * */
    public static String formatShort(String raw, int maxWidth, BLANK_GRAVITY blankGravity){
//        Pattern p = Pattern.compile("[0-9]*");
//        p=Pattern.compile("[\u4e00-\u9fa5]");

//        String formated = raw.trim();
//        char[] rawChars = raw.trim().toCharArray();
//        int len = rawChars.length;
        int len = getLength(raw);
        String subStr = DataConvertUtil.subString(raw, Math.min(len, maxWidth));//截取字符串 String.valueOf(rawChars, 0, len2)
        String blankStr = genBlankspace(Math.max(maxWidth - len, 0));
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
     * 添加空格
     * */
    private static String genBlankspace(int len){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++){
            sb.append(' ');
        }
        return sb.toString();
    }

}
