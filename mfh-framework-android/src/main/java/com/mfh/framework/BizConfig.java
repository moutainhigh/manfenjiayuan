package com.mfh.framework;

import android.content.Context;


import com.mfh.framework.core.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * 应用程序配置类：用于保存用户相关信息及设置
 * Created by Administrator on 2015/5/26.
 */
public class BizConfig {
    //正式发布版本为true(production,beta,develop)
    public static boolean RELEASE = true;

    private final static String APP_CONFIG = "false";

    //com.mfh.owner "manfenhome"
    public static final String DEFAULT_ROOT_FOLDER = MfhApplication.getAppContext().getPackageName();
    public final static String DEFAULT_SAVE_PATH = FileUtil.getSDCardPath() + File.separator + DEFAULT_ROOT_FOLDER + File.separator;

    // 默认缓存路径
    public final static String DEFAULT_CACHE_PATH = DEFAULT_SAVE_PATH + "cache" + File.separator;
    // 默认缓存图片路径
    public final static String DEFAULT_KJBITMAP_CACHEPATH = DEFAULT_ROOT_FOLDER + File.separator + "imagecache";
    public final static String DEFAULT_CACHE_IMAGE_PATH = DEFAULT_SAVE_PATH + "cache" + File.separator + "image";
    // 默认存放截屏的路径
    public final static String DEFAULT_SCRESHOOT_PATH = DEFAULT_SAVE_PATH + "screenshoot" + File.separator;
    // 默认存放拍照的路径
    public final static String DEFAULT_SAVE_CAMERA_PATH = DEFAULT_SAVE_PATH + "Camera" + File.separator;
    // 默认存放裁剪的路径
    public final static String DEFAULT_SAVE_CROP_PATH = DEFAULT_SAVE_PATH + "crop" + File.separator;
    // 默认存放文件下载的路径
    public final static String DEFAULT_SAVE_FILE_PATH = DEFAULT_SAVE_PATH + "download" + File.separator;

    private static BizConfig instance;

    public static BizConfig getInstance() {
        if (instance == null) {
            instance = new BizConfig();
        }
        return instance;
    }

    public String get(Context context, String key) {
        Properties props = get(context);
        return (props != null) ? props.getProperty(key) : null;
    }

    public Properties get(Context context) {
        FileInputStream fis = null;
        Properties props = new Properties();
        try {
            // 读取files目录下的config
            // fis = activity.openFileInput(APP_CONFIG);

            // 读取app_config目录下的config
            File dirConf = context.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            fis = new FileInputStream(dirConf.getPath() + File.separator
                    + APP_CONFIG);

            props.load(fis);
        } catch (Exception e) {
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return props;
    }

    private void setProps(Context context, Properties p) {
        FileOutputStream fos = null;
        try {
            // 把config建在files目录下
            // fos = activity.openFileOutput(APP_CONFIG, Context.MODE_PRIVATE);

            // 把config建在(自定义)app_config的目录下
            File dirConf = context.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            File conf = new File(dirConf, APP_CONFIG);
            fos = new FileOutputStream(conf);

            p.store(fos, null);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    public void set(Context context, Properties ps) {
        Properties props = get(context);
        props.putAll(ps);
        setProps(context, props);
    }

    public void set(Context context, String key, String value) {
        Properties props = get(context);
        props.setProperty(key, value);
        setProps(context, props);
    }

    public void remove(Context context, String... key) {
        Properties props = get(context);
        for (String k : key){
            props.remove(k);
        }

        setProps(context, props);
    }



}
