package com.mfh.framework.core.utils;

import android.content.Context;
import android.os.Environment;

import com.mfh.framework.anlaysis.logger.ZLogger;

import java.io.File;

/**
 * 数据删除工具类
 * @author FireAnt（http://my.oschina.net/LittleDY）
 * @version 创建时间：2014年10月27日 上午10:18:22
 *
 * /data/data/package_name/cache/
 * /data/data/package_name/cache/afinalCache
 * /data/data/package_name/cache/locationCache
 * /data/data/package_name/cache/uil-images
 * /data/data/package_name/files/carrierdata/
 *
 * webview缓存构成：
 * /data/data/package_name/cache/webviewCache URL内容
 * /data/data/package_name/database/webview.db
 * /data/data/package_name/database/webviewCache.db 请求的URL记录
 *
 */
public class DataCleanManager {
	public static void clearCache(Context context){
		cleanInternalCache(context);
		cleanExternalCache(context);
	}

	/**
	 * 清除本应用内部缓存
	 * (/data/data/com.xxx.xxx/cache)
	 * @param context
	 */
	public static void cleanInternalCache(Context context) {
		ZLogger.d("cleanInternalCache:" + context.getCacheDir());
		ZLogger.d("cleanInternalCache:" + context.getFilesDir());

		deleteFilesByDirectory(context.getCacheDir());
		deleteFilesByDirectory(context.getFilesDir());
	}

	/**
	 * 清楚本应用所有数据库
	 * (/data/data/com.xxx.xxx/databases)
	 * @param context
	 */
	public static void cleanDatabases(Context context) {
//		String path = context.getFilesDir().getPath()
//				+ context.getPackageName() + File.separator + "databases";
//		String path = "/data/data/"
//				+ context.getPackageName() + File.separator + "databases";
		File dbFile = context.getDatabasePath("cashier");
		ZLogger.d(String.format("path=%s, absolutePath=%s, parent=%s",
				dbFile.getPath(), dbFile.getAbsolutePath(), dbFile.getParent()));
		String path = dbFile.getParent();

		ZLogger.d("cleanDatabases:" + path);
		deleteFilesByDirectory(new File(path));
	}

	/**
	 * 清除本应用SharedPreference
	 * (/data/data/com.xxx.xxx/shared_prefs)
	 * @param context
	 */
	public static void cleanSharedPreference(Context context) {
		String path = context.getFilesDir().getPath()
				+ context.getPackageName() + File.separator + "shared_prefs";
		ZLogger.d("cleanSharedPreference:" + path);
		deleteFilesByDirectory(new File(path));
	}
	
	/**
	 * 按名字清除本应用数据库
	 * @param context
	 * @param dbName
	 */
	public static void cleanDatabaseByName(Context context, String dbName) {
		context.deleteDatabase(dbName);
	}

	/**
	 * 清除/data/data/com.xxx.xxx/files下的内容
	 * @param context
	 */
	public static void cleanFiles(Context context) {
		deleteFilesByDirectory(context.getFilesDir());
	}

	/**
	 * 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
	 * @param context
	 */
	public static void cleanExternalCache(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			deleteFilesByDirectory(context.getExternalCacheDir());
		}
	}

	/**
	 * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除
	 * @param filePath
	 */
	public static void cleanCustomCache(String filePath) {
		deleteFilesByDirectory(new File(filePath));
	}
	
	/**
	 * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除
	 * @param file
	 */
	public static void cleanCustomCache(File file) {
		deleteFilesByDirectory(file);
	}

	/**
	 * 清除本应用所有的数据
	 * @param context
	 * @param filepath
	 */
	public static void cleanApplicationData(Context context, String... filepath) {
		cleanInternalCache(context);
		cleanExternalCache(context);
		cleanDatabases(context);
		cleanSharedPreference(context);
		cleanFiles(context);
		for (String filePath : filepath) {
			cleanCustomCache(filePath);
		}
	}

	/**
	 * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理 
	 * @param directory
	 */
	public static void deleteFilesByDirectory(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null && files.length > 0){
				for (File child : directory.listFiles()) {
					if (child.isDirectory()) {
						deleteFilesByDirectory(child);
					}
					else{
						child.delete();
					}
				}
			}
		}
	}
}
