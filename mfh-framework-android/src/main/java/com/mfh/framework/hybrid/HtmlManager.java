package com.mfh.framework.hybrid;

import android.content.Context;
import android.util.Log;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.file.FileNetDao;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.ZipUtils;

import net.tsz.afinal.http.AjaxParams;

import java.io.File;
import java.io.IOException;

/**
 * Created by Nat.ZZN(bingshanguxue) on 2015/6/11.
 */
public class HtmlManager {
    public static final String MIME_TYPE = "text/html";
    public static final String ENCODING_UTF8 = "utf-8";
    public static final String HOME = "HOME";

    // /storage/emulated/0/Android/data/packagename
    public static final String ROOT_DIR = FileUtil.SDCARD
//            + "Android" + File.separator + "data"
             + File.separator + MfhApplication.getAppContext().getPackageName();
    //默认保存目录Environment.getExternalStorageDirectory()
    public static final String ROOT_DIRECTORY_HTML_DEF = ROOT_DIR
//            + File.separator + "mfh"
            + File.separator + "localHtml";

    //默认下载目录 /data/data/
    public static final String ROOT_DIRECTORY_DOWNLOAD_DEF = MfhApplication.getAppContext().getFilesDir().getPath()
//            + File.separator
            + "mfh" + File.separator + "version" + File.separator + "localHtml";

    private Context context;
    private FileNetDao fileNetDao;
    private String versionUrl = "";

    //TODO,根据版本号区分
    private String downLoadUrl = "";//下载保存路径,非资源加载路径

    private static HtmlManager instance;
    public static HtmlManager getInstance(Context context){
        if(instance == null){
            return new HtmlManager(context);
        }
        return instance;
    }

    public HtmlManager(Context context){
        this.context = context;

        fileNetDao = new FileNetDao("download", downLoadUrl, ROOT_DIRECTORY_DOWNLOAD_DEF);
        fileNetDao.setUseLocalFirst(false);//每次都重新下载
    }
    /**
     * 检查更新
     * */
    public void checkUpdate(){

    }
    /**
     * 检查服务器端版本号，若有新版本则启动下载并安装
     * @return
     */
    public void checkServVersionCode(AjaxParams param) {
//        NetFactory.getHttp().get(versionUrl, param,
//                new NetCallBack.NormalNetTask<UpdateResponse>(UpdateResponse.class) {
//                    @Override
//                    public void processResult(IResponseData rspData) {
//                        //TODO,改写AppInfo
//                        RspBean<UpdateResponse> result = (RspBean<UpdateResponse>) rspData;
//                        final UpdateResponse appInfo = result.getValue();
//
//                        //有新版本更新，自动下载
//                        if (appInfo.getVersionCode() > ComnApplication.getVersionCode()) {
//                            doDownLoad(appInfo.getApkName());
//                            //save current version
//                        }
//                    }
//                });
    }

    /**
     * 下载文件
     * @param fileName
     *          文件名(zip包名，区分版本，eg: **0.01.zip)。直接在此文件
     */
    public void doDownLoad(String fileName) {
        //执行下载
        fileNetDao.processFile(fileName, new FileNetDao.CallBack() {
            @Override
            public void processFile(File file) {
                try {
                    ZLogger.d("processFile " + file.getName());
                    saveFile(file);
                } catch (Throwable e) {
                    ZLogger.e("HtmlManager.processFile", e.toString());
                }
            }

            @Override
            public void onFailure(String fileName, Throwable e) {

            }
        });
    }

    /**
     * 保存新版本的文件
     */
    private void saveFile(File file) {
        if (!file.exists()) {
            return;
        }

        //解压文件到指定的目录并更新版本号;
        String folderPath = ROOT_DIRECTORY_HTML_DEF + File.separator + file.getName();
        FileUtil.PathStatus status = FileUtil.createPath(folderPath);
        if(status != FileUtil.PathStatus.ERROR){
            try {
                ZipUtils.upZipFile(file, folderPath);
                //TODO，保存新版本，并刷新页面
                //视情况刷新页面
                //TODO,动态加载根据版本号，保存完之后需要刷新页面。
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//        context.startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    ///storage/emulated/0/mfh/local_html/m/auth/login.html
    public String getLocalFilePath(String fileName){
        if(fileName == null){
            return null;
        }

        ///storage/emulated/0/mfh/local_html/m/auth/login.html
        String folderPath;
        if(fileName.startsWith(File.separator)){
            folderPath = ROOT_DIRECTORY_HTML_DEF + fileName;
        }else{
            folderPath = ROOT_DIRECTORY_HTML_DEF + File.separator + fileName;
        }
        Log.d("Nat", "getLocalFilePath:" + folderPath);
        return folderPath;
    }
    //TODO,根据当前版本号，创建相应的文件目录
    public String getLocalData(String fileName){
        String folderPath = getLocalFilePath(fileName);
        if(folderPath == null){
            return null;
        }

        //,check file exist
        if(FileUtil.checkFilePathExists(folderPath)){
            // read file
            return FileUtil.read(context, folderPath);
        }else{
            Log.d("Nat", "getLocalData:" + folderPath + " not exist.");
        }
        return null;
    }
}
