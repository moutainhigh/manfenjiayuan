package com.mfh.framework.core.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.mfh.framework.core.logger.ZLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文件操作工具包
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class FileUtil {

	// /data/data/ or "/data/data/package_name/files", without File.seperator
//	public static final String DATA = ComnApplication.getAppContext().getFilesDir().getPath();
	public final static String SDCARD_MNT = "/mnt/sdcard";

	static {
	}

	/**
	 * 判断是否存在SD卡
	 * determine whether sd card is exist
	 * */
	public final static boolean IS_SDCARD_EXIST = Environment.getExternalStorageState().equals(
			Environment.MEDIA_MOUNTED);

	/**
	 * 获取SD卡的根目录
	 * <ol>
	 *     <li>
	 *         返回 "/scard" or "/storage/emulated/0"，以'/'结尾.
	 *     </li>
	 * </ol>
	 * */
	public static String getSDCardPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 获取SD卡的根目录
	 * <ol>
	 *     <li>
	 *         返回 "/scard" or "/storage/emulated/0"，不以'/'结尾.
	 *     </li>
	 * </ol>
	 * */
	public static String getSDCardAbsPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	public static File getSaveFile(String folderPath, String fileNmae) {
		File file = new File(getSavePath(folderPath) + File.separator + fileNmae);

		try {
			file.createNewFile();
		} catch (IOException var4) {
			//java.io.IOException: open failed: ENOENT (No such file or directory)
			var4.printStackTrace();
		}

		return file;
	}

	public static String getSavePath(String folderName) {
		return getSaveFolder(folderName).getAbsolutePath();
	}

	public static File getSaveFolder(String folderName) {
		File file = new File(getSDCardAbsPath() + File.separator + folderName + File.separator);
		file.mkdirs();
		return file;
	}

    /**
	 * 写文本文件 在Android系统中，文件保存在 /data/data/PACKAGE_NAME/files 目录下
	 * 
	 * @param context
	 * @param fileName
	 * @param content
	 */
	public static void write(Context context, String fileName, String content) {
		if (content == null)
			content = "";

		try {
			FileOutputStream fos = context.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			fos.write(content.getBytes());

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取文本文件
	 * 
	 * @param context
	 * @param fileName or path
	 * @return
	 */
	public static String read(Context context, String fileName) {
		try {
			//The openFileInput method doesn't accept path separators.
//			FileInputStream in = context.openFileInput(fileName);
			FileInputStream in = new FileInputStream(new File(fileName));
			return readInStream(in);
		} catch (Exception e) {
			e.printStackTrace();
			ZLogger.e("read failed:" + e.toString());
		}

		return null;
	}

	public static String readInStream(InputStream inStream) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}

			outStream.close();
			inStream.close();
			return outStream.toString();
		} catch (IOException e) {
			ZLogger.i("readInStream: " + e.getMessage());
		}
		return null;
	}


	/**
	 * 创建文件
	 * */
	public static File createFile(String folderPath, String fileName) {
		File destDir = new File(folderPath);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		return new File(folderPath, fileName);
	}

	/**
	 * 向手机写图片
	 * 
	 * @param buffer
	 * @param folder
	 * @param fileName
	 * @return
	 */
	public static boolean writeFile(byte[] buffer, String folder,
			String fileName) {
		boolean writeSucc = false;

		String folderPath = "";
		if (IS_SDCARD_EXIST) {
			folderPath = Environment.getExternalStorageDirectory()
					+ File.separator + folder + File.separator;
		} else {
			writeSucc = false;
		}

//		File fileDir = new File(folderPath);
//		if (!fileDir.exists()) {
//			fileDir.mkdirs();
//		}
//
//		File file = new File(folderPath + fileName);
		File file = createFile(folderPath, fileName);

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(buffer);
			writeSucc = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null){
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return writeSucc;
	}

	/**
	 * 根据文件绝对路径获取文件名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath) {
		if (StringUtils.isEmpty(filePath))
			return "";
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}

	/**
	 * 根据文件的绝对路径获取文件名但不包含扩展名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileNameNoFormat(String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			return "";
		}
		int point = filePath.lastIndexOf('.');
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1,
				point);
	}

	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (StringUtils.isEmpty(fileName))
			return "";

		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

	/**
	 * 获取文件大小
	 * 
	 * @param filePath
	 * @return
	 */
	public static long getFileSize(String filePath) {
		long size = 0;

		File file = new File(filePath);
		if (file.exists()) {
			size = file.length();
		}
		return size;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param size
	 *            字节
	 * @return
	 */
	public static String getFileSize(long size) {
		if (size <= 0)
			return "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float) size / 1024;
		if (temp >= 1024) {
			return df.format(temp / 1024) + "M";
		} else {
			return df.format(temp) + "K";
		}
	}

	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 * @return B/KB/MB/GB
	 */
	public static String formatFileSize(long fileS) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		String fileSizeString;
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取目录文件大小
	 * 
	 * @param dir
	 * @return
	 */
	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return 0;
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file); // 递归调用继续统计
			}
		}
		return dirSize;
	}

	/**
	 * 获取目录文件个数
	 * 
	 * @param dir
	 * @return
	 */
	public long getFileList(File dir) {
		long count = 0;
		File[] files = dir.listFiles();
		count = files.length;
		for (File file : files) {
			if (file.isDirectory()) {
				count = count + getFileList(file);// 递归
				count--;
			}
		}
		return count;
	}

	public static byte[] toBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int ch;
		while ((ch = in.read()) != -1) {
			out.write(ch);
		}
		byte buffer[] = out.toByteArray();
		out.close();
		return buffer;
	}

	/**
	 * 检查文件是否存在
	 * 
	 * @param name
	 * @return
	 */
	public static boolean checkFileExists(String name) {
		boolean status;
		if (!name.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + name);
			status = newPath.exists();
		} else {
			status = false;
		}
		return status;
	}

	/**
	 * 检查路径是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkFilePathExists(String path) {
		return new File(path).exists();
	}

	/**
	 * 计算SD卡的剩余空间
	 * 
	 * @return 返回-1，说明没有安装sd卡
	 */
	public static long getFreeDiskSpace() {
		long freeSpace = 0;
		if (IS_SDCARD_EXIST) {
			try {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				freeSpace = availableBlocks * blockSize / 1024;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return -1;
		}
		return (freeSpace);
	}

	/**
	 * 新建目录
	 * 
	 * @param directoryName
	 * @return
	 */
	public static boolean createDirectory(String directoryName) {
		boolean status;
		if (!directoryName.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + directoryName);
			status = newPath.mkdir();
		} else
			status = false;
		return status;
	}

	
	/**
	 * 检查是否安装外置的SD卡
	 * 
	 * @return
	 */
	public static boolean checkExternalSDExists() {
		Map<String, String> evn = System.getenv();
		return evn.containsKey("SECONDARY_STORAGE");
	}

	/**
	 * 删除目录(包括：目录里的所有文件)
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean deleteDirectory(String fileName) {
		if(TextUtils.isEmpty(fileName)){
			return false;
		}

		File path = Environment.getExternalStorageDirectory();
		File newPath = new File(path.toString() + fileName);
		if (!newPath.isDirectory()){
			return false;
		}

		boolean status;
		SecurityManager checker = new SecurityManager();
		checker.checkDelete(newPath.toString());
		String[] listfile = newPath.list();
		try {
			for (String aListfile : listfile) {
				File deletedFile = new File(newPath.toString() + "/"
						+ aListfile);
				deletedFile.delete();
			}
			status = newPath.delete();
			ZLogger.i(String.format("deleteDirectory %s,%s", fileName, String.valueOf(status)));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean deleteFile(String fileName) {
		boolean status;
		SecurityManager checker = new SecurityManager();

		if (!fileName.equals("")) {

			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isFile()) {
				try {
					ZLogger.i("deleteFile:" + fileName);
					newPath.delete();
					status = true;
				} catch (SecurityException se) {
					se.printStackTrace();
					status = false;
				}
			} else
				status = false;
		} else
			status = false;
		return status;
	}

	/**
	 * 删除空目录
	 * 
	 * 返回 0代表成功 ,1 代表没有删除权限, 2代表不是空目录,3 代表未知错误
	 * 
	 * @return
	 */
	public static int deleteBlankPath(String path) {
		File f = new File(path);
		if (!f.canWrite()) {
			return 1;
		}
		if (f.list() != null && f.list().length > 0) {
			return 2;
		}
		if (f.delete()) {
			return 0;
		}
		return 3;
	}

	/**
	 * 重命名
	 * 
	 * @param oldName
	 * @param newName
	 * @return
	 */
	public static boolean reNamePath(String oldName, String newName) {
		File f = new File(oldName);
		return f.renameTo(new File(newName));
	}

	/**
	 * 删除文件
	 * 
	 * @param filePath
	 */
	public static boolean deleteFileWithPath(String filePath) {
		if(StringUtils.isEmpty(filePath)){
			return false;
		}

		SecurityManager checker = new SecurityManager();
		File f = new File(filePath);
		checker.checkDelete(filePath);
		if (f.isFile()) {
			f.delete();
			ZLogger.i(String.format("deleteFile %s", filePath));
			return true;
		}
		return false;
	}
	
	/**
	 * 清空一个文件夹
	 * @param filePath
	 */
	public static void clearFileWithPath(String filePath) {
		List<File> files = FileUtil.listPathFiles(filePath);
		if (files.isEmpty()) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				clearFileWithPath(f.getAbsolutePath());
			} else {
				f.delete();
			}
		}
	}

	/**
	 * 获取sd卡根路径
	 * @return
	 */
	public static String getSDRootPath() {
		if (IS_SDCARD_EXIST) {
			// get the root directory
			File sdDir = Environment.getExternalStorageDirectory();
			return sdDir.toString();
		}else{
			ZLogger.d("sd card not exist");
		}

		return null;
	}

	/**
	 * 获取手机外置SD卡的根目录
	 * 
	 * @return
	 */
	public static String getExternalSDRoot() {

		Map<String, String> evn = System.getenv();

		return evn.get("SECONDARY_STORAGE");
	}

	/**
	 * 列出root目录下所有子目录
	 * 
	 * @param root
	 * @return 绝对路径
	 */
	public static List<String> listPath(String root) {
		List<String> allDir = new ArrayList<String>();
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		// 过滤掉以.开始的文件夹
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				if (f.isDirectory() && !f.getName().startsWith(".")) {
					allDir.add(f.getAbsolutePath());
				}
			}
		}
		return allDir;
	}
	
	/**
	 * 获取一个文件夹下的所有文件
	 * @param root
	 * @return
	 */
	public static List<File> listPathFiles(String root) {
		List<File> allDir = new ArrayList<File>();
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		File[] files = path.listFiles();
		for (File f : files) {
			if (f.isFile())
				allDir.add(f);
			else 
				listPath(f.getAbsolutePath());
		}
		return allDir;
	}

	public enum PathStatus {
		SUCCESS, EXITS, ERROR
	}

	/**
	 * 创建目录
	 * 
	 * @param newPath
	 */
	public static PathStatus createPath(String newPath) {
		File path = new File(newPath);
		if (path.exists()) {
			return PathStatus.EXITS;
		}
		if (path.mkdir()) {
			return PathStatus.SUCCESS;
		} else {
			return PathStatus.ERROR;
		}
	}

	/**
	 * 截取路径名
	 * 
	 * @return
	 */
	public static String getPathName(String absolutePath) {
		int start = absolutePath.lastIndexOf(File.separator) + 1;
		int end = absolutePath.length();
		return absolutePath.substring(start, end);
	}
	
	/**
	 * 获取应用程序缓存文件夹下的指定目录
	 * @param context
	 * @param dir
	 * @return
	 */
	public static String getAppCache(Context context, String dir) {
		String savePath = context.getCacheDir().getAbsolutePath() + "/" + dir + "/";
		File savedir = new File(savePath);
		if (!savedir.exists()) {
			savedir.mkdirs();
		}
//		savedir = null;
		return savePath;
	}

	/**
	 * 读取文本数据
	 *
	 * @param context
	 *            程序上下文
	 * @param fileName
	 *            文件名
	 * @return String, 读取到的文本内容，失败返回null
	 */
	public static String readAssets(Context context, String fileName)
	{
		InputStream is = null;
		String content = null;
		try
		{
			is = context.getAssets().open(fileName);
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			while (true)
			{
				int readLength = is.read(buffer);
				if (readLength == -1) break;
				arrayOutputStream.write(buffer, 0, readLength);
			}
			is.close();
			arrayOutputStream.close();
			content = new String(arrayOutputStream.toByteArray());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			content = null;
		}
		finally
		{
			try
			{
				if (is != null) is.close();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
		return content;
	}

	/**
	 * 拷贝Assets文件夹的内容到SD卡
	 * */
	public static void copyAssetsFolder(Context context, String fromPath, String toPath) {
		ZLogger.d(String.format("copyAssetsFolder from %s to %s", fromPath, toPath));
		AssetManager assetManager = context.getAssets();
		String[] files = null;
		try {
			files = assetManager.list(fromPath);
		} catch (IOException e) {
			ZLogger.e("copyAssetsFolder failed " + e.toString());
		}

		if(files == null){
			return;
		}

		//create folder (required)
		File dir = new File(toPath);
		if(!dir.exists()){
			boolean result = dir.mkdirs();
			ZLogger.e("Result of directory creation" + result);
		}

		for(String filename : files) {
			if (filename.contains("."))
				copyAssets(context,
						fromPath + File.separator + filename,
						toPath + File.separator + filename);
			else{
				copyAssetsFolder(context,
						fromPath + File.separator + filename,
						toPath + File.separator + filename);
			}
		}
	}
	public static void copyAssets(Context context, String fromPath, String toPath) {
		ZLogger.d(String.format("copyAssets from %s to %s", fromPath, toPath));
		AssetManager assetManager = context.getAssets();
		InputStream in = null;
		OutputStream out = null;

		try {
			in = assetManager.open(fromPath);
		} catch (IOException e) {
			ZLogger.e("open asset failed: " + e.toString());
		}

		File outFile = new File(toPath);
		ZLogger.d("outFilePath=" + outFile.getPath());
		//TODO
		try {
			boolean ret = outFile.createNewFile();
			ZLogger.e("createNewFile: " + String.valueOf(ret));
		} catch (IOException e) {
			ZLogger.e("createFile failed: " + e.toString());
		}

//			File outFile = new File(context.getExternalFilesDir(null), filename);
			try {
				out = new FileOutputStream(outFile);
			} catch (FileNotFoundException e1) {
				ZLogger.e(e1.toString());
			}

		try {

			copyFile(in, out);

		} catch(IOException e) {
			ZLogger.e(e.toString());
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// NOOP
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// NOOP
				}
			}
		}
	}
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

}