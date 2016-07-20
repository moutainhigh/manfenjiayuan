package com.mfh.enjoycity;

import android.content.Context;

import com.mfh.enjoycity.wxapi.WXConstants;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.AppException;
import com.mfh.framework.core.logger.ZLogger;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;

/**
 * Created by bingshanguxue on 2015/7/10.
 */
public class AppContext extends MfhApplication {

    @Override
    public void onCreate() {
        AppException.CRASH_FOLDER_PATH = getPackageName() + File.separator + "crash";

        super.onCreate();

        ZLogger.CRASH_FOLDER_PATH = getPackageName() + File.separator + "zlogger";

        //注册应用id到微信
        WXAPIFactory.createWXAPI(this, WXConstants.APP_ID, false).registerApp(WXConstants.APP_ID);

        initImageLoader(getApplicationContext());
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

}
