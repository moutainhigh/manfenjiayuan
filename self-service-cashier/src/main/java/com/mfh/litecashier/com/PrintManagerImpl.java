package com.mfh.litecashier.com;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.database.entity.DailysettleEntity;
import com.bingshanguxue.cashier.hardware.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.printer.GPrinterAgent;
import com.google.zxing.WriterException;
import com.gprinter.command.EscCommand;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.QrCodeUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.AggItem;
import com.mfh.litecashier.bean.PosOrder;
import com.mfh.litecashier.bean.PosOrderItem;
import com.mfh.litecashier.bean.StockOutItem;
import com.mfh.litecashier.bean.wrapper.AccWrapper;
import com.mfh.litecashier.bean.wrapper.AggWrapper;
import com.mfh.litecashier.bean.wrapper.HandOverBill;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by bingshanguxue on 8/12/16.
 */
public class PrintManagerImpl extends PrintManager {

    /**
     * 打印线上订单
     */
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
        esc.addText(curOrder.getOfficeName());
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
        esc.addText(String.format("订单号:%d \n", curOrder.getId()));
        /**打印 应付款*/
//        esc.addText(String.format("应付款:%.2f \n", orderEntity.getAmount()));
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
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

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
     * 打印线上订单明细
     */
    private static EscCommand makePosOrderTemp(EscCommand rawEsc, String name, String bcount, String amount) {
        EscCommand esc = rawEsc;
        if (esc == null) {
            esc = new EscCommand();
        }

        try {
//计算行数
            int maxLine = Math.max(1, (name == null ? 0 : (getLength(name) - 1) / 20 + 1));
//        ZLogger.d(String.format("maxLine=%d", maxLine));

            String nameTemp = name;
            int mid = maxLine / 2;
            for (int i = 0; i < maxLine; i++) {
//            ZLogger.d(String.format("nameTemp: %d(%d)", getLength(nameTemp), nameTemp.toCharArray().length));
                String nameLine = DataConvertUtil.subString(nameTemp,
                        Math.min(20, getLength(nameTemp)));
//            String sub2 = DataConvertUtil.subString(nameTemp, Math.min(PRINT_PRODUCT_NAME_MAX_LEN, nameTemp.toCharArray().length));
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
                StringBuilder line = new StringBuilder();
                //插入名称，不足补空格
                //中间一行插入数量&金额
                if (i == mid) {
                    line.append(formatShort(nameLine, 20, BLANK_GRAVITY.RIGHT));
                    line.append(formatShort(bcount, 6, BLANK_GRAVITY.RIGHT));
                    line.append(formatShort(amount, 6, BLANK_GRAVITY.LEFT));
                } else {
                    line.append(formatShort(nameLine, 20, BLANK_GRAVITY.RIGHT));
                }
                line.append("\n");
//            ZLogger.d(String.format("print line(%d/%d):%s" , i, mid, line.toString()));
                esc.addText(line.toString());

//                assert nameTemp != null;
                if (!StringUtils.isEmpty(nameTemp)) {
                    nameTemp = nameTemp.substring(nameLine.length(), nameTemp.length()).trim();
                }
//            ZLogger.d(String.format("subName2=%s nameTemp=%s", sub2, nameTemp));
//            line.append(formatLong(sub2, 8));
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
                        GPrinterAgent.print(escCommand);
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

    /**
     * 打印测试
     */
    private static EscCommand makeTestEsc() {
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

            /**打印 APP LOGO*/
            esc.addText("Print bitmap!\n");   //  打印文字
            Bitmap b = BitmapFactory.decodeResource(CashierApp.getAppContext().getResources(),
                    R.mipmap.ic_launcher);
            esc.addRastBitImage(b, b.getWidth(), 0);


            /**打印 机器设备号＋订单号*/
            esc.addText(String.format("%s NO.%s \n", SharedPreferencesManager.getTerminalId(),
                    MUtils.getOrderBarCode()));
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

//        esc.addPrintAndLineFeed();
            esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行


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

/*打印一维条码code128*/
            esc.addText("Print code128\n");   //  打印文字
            // 一维条码：设置条码可识别字符位置在条码下方
            esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
            //设置条码高度为 60 点
            esc.addSetBarcodeHeight((byte) 60);
            //设置条码宽度
//            esc.addSetBarcodeWidth((byte)500);
            esc.addCODE128("Gprinter");  //打印Code128码
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

            try {
                Bitmap QRCodeBmp = QrCodeUtils.Create2DCode("www.manfenjiayuan.cn");
                esc.addRastBitImage(QRCodeBmp, QRCodeBmp.getWidth(), 0); //打印图片
                esc.addPrintAndLineFeed();
            } catch (WriterException e) {
                e.printStackTrace();
            }

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
            ZLogger.ef(e.toString());
            return null;
        }
    }


//    void sendLabel(){
//        TscCommand tsc = new TscCommand();
//        tsc.addSize(60, 60); //设置标签尺寸，按照实际尺寸设置
//        tsc.addGap(0);           //设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
//        tsc.addDirection(TscCommand.DIRECTION.BACKWARD, TscCommand.MIRROR.NORMAL);//设置打印方向
//        tsc.addReference(0, 0);//设置原点坐标
//        tsc.addTear(EscCommand.ENABLE.ON); //撕纸模式开启
//        tsc.addCls();// 清除打印缓冲区
//        //绘制简体中文
//        tsc.addText(20,20, TscCommand.FONTTYPE.SIMPLIFIED_CHINESE, TscCommand.ROTATION.ROTATION_0, TscCommand.FONTMUL.MUL_1, TscCommand.FONTMUL.MUL_1,"Welcome to use Gprinter!");
//        //绘制图片
//        Bitmap b = BitmapFactory.decodeResource(CashierApp.getAppContext().getResources(),
//                R.mipmap.ic_launcher);
//        tsc.addBitmap(20,50, TscCommand.BITMAP_MODE.OVERWRITE, b.getWidth()*2,b);
//
//        tsc.addQRCode(250, 80, TscCommand.EEC.LEVEL_L,5, TscCommand.ROTATION.ROTATION_0, " www.gprinter.com.cn");
//        //绘制一维条码
//        tsc.add1DBarcode(20,250, TscCommand.BARCODETYPE.CODE128, 100, TscCommand.READABEL.EANBEL, TscCommand.ROTATION.ROTATION_0, "Gprinter");
//        tsc.addPrint(1,1); // 打印标签
//        tsc.addSound(2, 100); //打印标签后 蜂鸣器响
//        Vector<Byte> datas = tsc.getCommand(); //发送数据
//        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
//        byte[] bytes = ArrayUtils.toPrimitive(Bytes);
//        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
//        int rel;
//        try {
//            rel = mGpService.sendTscCommand(mPrinterIndex, str);
//            GpCom.ERROR_CODE r=GpCom.ERROR_CODE.values()[rel];
//            if(r != GpCom.ERROR_CODE.SUCCESS){
//                Toast.makeText(getApplicationContext(),GpCom.getErrorText(r),
//                        Toast.LENGTH_SHORT).show();
//            }
//        } catch (RemoteException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    /**
     * 打印测试
     */
    public static void printTest() {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeTestEsc());
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
//                        GPrinterAgent.print(escCommand);
                        if (escCommand != null) {
                            //获得打印命令
                            Vector<Byte> datas = escCommand.getCommand();//发送数据
                            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
                            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
                            EventBus.getDefault().post(new SerialPortEvent(SerialPortEvent.GPRINTER_SEND_DATA_V2, bytes));
                        }
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
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getCash());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getAlipay());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getWx());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getAccount());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getBank());
        printDailySettleAccItem(esc, accIndex, accWrapper.getRule());

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
                        GPrinterAgent.print(escCommand);
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
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getCash());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getAlipay());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getWx());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getAccount());
        accIndex = printDailySettleAccItem(esc, accIndex, accWrapper.getBank());
        printDailySettleAccItem(esc, accIndex, accWrapper.getRule());

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
                        GPrinterAgent.print(escCommand);
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
                        GPrinterAgent.print(escCommand);
                    }
                });
    }

    /**
     * 打印商城订单
     */
    private static EscCommand makeScOrderEsc(ScOrder scOrder) {
        if (scOrder == null) {
            return null;
        }
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        //设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
//        //设置为倍高倍宽
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);

        esc.addPrintAndLineFeed();//进纸一行
        //设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText(String.format("顾客姓名:%s \n", scOrder.getBuyerName()));
        esc.addText(String.format("下单网点:%s \n", scOrder.getOfficeName()));
        esc.addText(String.format("下单时间：%s \n", TimeUtil.format(scOrder.getCreatedDate(),
                TimeCursor.FORMAT_YYYYMMDDHHMM)));
        esc.addText(String.format("买手姓名:%s \n", scOrder.getServiceHumanName()));
        esc.addText(String.format("买手电话:%s \n", scOrder.getServiceMobile()));
        esc.addPrintAndLineFeed();//进纸一行
        esc.addText(String.format("收件人:%s \n", scOrder.getReceiveName()));
        esc.addText(String.format("收件人电话:%s \n", scOrder.getReceivePhone()));
        esc.addText(String.format("收件人地址:%s \n", scOrder.getAddress()));
        esc.addText(String.format("物流费:%s \n", MUtils.formatDouble(scOrder.getTransFee(), "")));
//        esc.addPrintAndLineFeed();//进纸一行
        esc.addText(String.format("订单金额:%s \n", MUtils.formatDouble(scOrder.getAmount(), "")));

        /**打印 商品明细*/
        esc.addText("品名               单位   数量\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        //设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF,
                EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        List<ScOrderItem> items = scOrder.getItems();
        if (items != null && items.size() > 0) {
            esc.addText("--------------------------------\n");//32个
            for (ScOrderItem item : items) {
                makeOrderItem1(esc, item.getProductName(), item.getUnitName(),
                        MUtils.formatDouble(item.getBcount(), ""));
            }
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
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 2);//打印并且走纸3行
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        esc.addText("让生活更美好\n");
//        esc.addPrintAndLineFeed();
        esc.addPrintAndFeedLines((byte) 3);//打印并且走纸3行

        return esc;
    }


    /**
     * 打印商城订单
     */
    public static void printScOrder(final ScOrder scOrder) {
        Observable.create(new Observable.OnSubscribe<EscCommand>() {
            @Override
            public void call(Subscriber<? super EscCommand> subscriber) {
                subscriber.onNext(makeScOrderEsc(scOrder));
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
