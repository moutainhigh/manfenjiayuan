package com.mfh.framework.core.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QrCodeUtils {

    /**
     * 传入字符串生成二维码
     *
     * @param content 内容
     * @return
     * @throws WriterException
     */
    public static Bitmap Create2DCode(String content) throws WriterException {
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, 300, 300);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public void createQR() {
        //配置参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //容错级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        //设置空白边距的宽度
//        hints.put(EncodeHintType.MARGIN, 2); //default is 4
    }

    /**
     * 传入字符串生成二维码
     *
     * @param content 内容
     * @param width   width in pixel
     * @param height  height in pixel
     * @param logo    二维码中心的Logo图标（可以为null）
     * @return Bitmap
     * @throws WriterException
     */
    public static Bitmap Create2DCode(String content, int width, int height,
                                      Bitmap logo)
            throws WriterException {
        return Create2DCode(content, BarcodeFormat.QR_CODE, width, height, logo);
    }

    /**
     * 传入字符串生成二维码
     *
     * @param content 内容
     * @param width   barcode format
     * @param width   width in pixel
     * @param height  height in pixel
     * @param logo    二维码中心的Logo图标（可以为null）
     * @return Bitmap
     * @throws WriterException
     */
    public static Bitmap Create2DCode(String content, BarcodeFormat format,
                                      int width, int height,
                                      Bitmap logo)
            throws WriterException {
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(content,
                format, width, height);
//        int width = matrix.getWidth();
//        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {//如果有黑块点，记录信息
                    pixels[y * width + x] = 0xff000000;//记录黑块信息
                } else {
                    //TODO ,其他颜色
                    pixels[y * width + x] = 0xffffffff;//记录白块信息
                }
            }
        }

        // 生成二维码图片的格式，使用ARGB_8888
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        //添加LOGO图标
        if (logo != null) {
            bitmap = addLogo(bitmap, logo);
        }

        return bitmap;
    }

    /**
     * 传入字符串生成二维码
     *
     * @param content  内容
     * @param width    width in pixel
     * @param height   height in pixel
     * @param logo     二维码中心的Logo图标（可以为null）
     * @param filePath 用于存储二维码图片的文件路径
     * @return Bitmap
     * @throws WriterException
     */
    public static boolean Create2DCode(String content, int width, int height,
                                       Bitmap logo, String filePath) {
        //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
        try {
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Create2DCode(content, width, height, logo);

            return bitmap != null
                    && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
        } catch (WriterException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }
}