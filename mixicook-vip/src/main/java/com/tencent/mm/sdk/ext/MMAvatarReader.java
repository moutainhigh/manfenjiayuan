package com.tencent.mm.sdk.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

import com.mfh.framework.anlaysis.logger.ZLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MMAvatarReader {
    public static final int AVATAR_HEIGHT = 96;
    public static final int AVATAR_SAVE_SIZE = 96;
    public static final int AVATAR_WIDTH = 96;
    private static final int MAX_BM_SIZE = 36864;
    private static final String TAG = "MicroMsg.ext.MMAvatarReader";

    public static Bitmap getSmallBitmap(String paramString) {
        int i;
        try {
            File localFile = new File(paramString);
            if ((localFile == null) || (!localFile.exists())) {
                ZLogger.d("MicroMsg.ext.MMAvatarReader", "small bm not exsit");
                return null;
            }
            i = (int) localFile.length();
            if (i <= 0) {
                ZLogger.e("MicroMsg.ext.MMAvatarReader", "get small bm invalid size");
                return null;
            }
        } catch (Exception localException1) {
            localException1.printStackTrace();
            return null;
        }

        if (i > 73728) {
            ZLogger.e("MicroMsg.ext.MMAvatarReader", "get small bm invalid size:" + i);
            return null;
        }

        ByteBuffer localByteBuffer = null;
        Bitmap localBitmap;
        try {
            localByteBuffer = ByteBuffer.allocateDirect(i);
            FileInputStream localFileInputStream = new FileInputStream(paramString);
            localByteBuffer.position(0);
            FileChannel localFileChannel = localFileInputStream.getChannel();
            localFileChannel.read(localByteBuffer);
            localFileChannel.close();
            localFileInputStream.close();
            localByteBuffer.position(0);
            return null;
        } catch (OutOfMemoryError localOutOfMemoryError1) {
            try {
                localBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
                localBitmap.copyPixelsFromBuffer(localByteBuffer);
                return localBitmap;
            } catch (Exception localException2) {
                ZLogger.e("MicroMsg.ext.MMAvatarReader", "decode as ARGB_8888 failed" + localException2.getMessage());
                return null;
            } catch (OutOfMemoryError localOutOfMemoryError2) {
            }
            localOutOfMemoryError1 = localOutOfMemoryError1;
            ZLogger.w("MicroMsg.ext.MMAvatarReader", "error in getSmallBitmap 1, " + localOutOfMemoryError1.getMessage());
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
