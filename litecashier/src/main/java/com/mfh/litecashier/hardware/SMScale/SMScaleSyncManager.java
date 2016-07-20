package com.mfh.litecashier.hardware.SMScale;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.CateApi;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.Encoding;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.database.entity.PosProductEntity;
import com.mfh.litecashier.database.logic.PosProductService;
import com.mfh.litecashier.utils.SharedPreferencesHelper;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

/**
 * 寺冈电子秤数据同步管理
 * <ol>
 * 适用场景
 * <li>同步商品数据到寺冈电子秤管理后台</li>
 * </ol>
 * <p/>
 * <ol>
 * 同步策略
 * <li>全量更新，每次都会同步所有数据。同步前需要删除csv文件</li>
 * <li>增量更新，每天首次开机全量同步一次（保证数据一致性）,之后仅同步有更新的数据。</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class SMScaleSyncManager extends FTPManager {

    public static final String PREF_SMSCALE = "pref_smscale";
    public static final String PK_S_SMSCALE_HOST = "pk_smscale_host";       //主机IP
    public static final String PK_I_SMSCALE_PORT = "pk_smscale_port";   //主机端口号
    public static final String PK_S_SMSCALE_USERNAME = "pk_smscale_username";//用户名
    public static final String PK_S_SMSCALE_PASSWORD = "pk_smscale_password";//密码
    public static final String PK_S_SMSCALE_ENCODING = "pk_smscale_encoding";//文件编码
    public static final String PK_B_SMSCALE_FULLSCALE = "pk_smscale_fullscale";//全量更新
    public static final String PK_S_SMSCALE_LASTCURSOR = "pk_smscale_lastcursor";//更新游标


    public static boolean FULLSCALE_ENABLED = true;
    public static String SMSCALE_CSV_FILENAME = "smscale_mixicook.csv";
    public static String[] COLUMNS = new String[]{"PLU号码", "单价", "品名信息", "分类"};
    public static String[] ENCODING_CHARSET_SET = new String[]{Encoding.CHARSET_GBK, Encoding.CHARSET_UTF_8};

    /**
     * 同步状态
     */
    public static final Integer SYNC_STATUS_NA = 0;//未同步
    public static final Integer SYNC_STATUS_UPDATE = 1;//有更新
    public static final Integer SYNC_STATUS_LATEST = 2;//最新的

    public static final int SYNC_MODE_FULLSCALE = 0;//全量更新
    public static final int SYNC_MODE_INCREMENTAL = 1;//增量更新
    //同步模式
    private int syncMode = SYNC_MODE_FULLSCALE;

    //增量更新时保存上一次推送商品的更新时间
    private static final String PK_SMSCALE_GOODSINFO_LASTCURSOR = "pk_smscale_goodsinfo_lastCursor";
    //每次同步最大数量
    private static final int MAX_SYNC_PAGESIZE = 50;

    private boolean bSyncInProgress = false;//是否正在同步

    private static SMScaleSyncManager instance = null;

    static {
        initialize();
    }

    /**
     * 初始化*/
    public static void initialize(){
        FTP_HOST = SharedPreferencesManager
                .getText(PREF_SMSCALE, PK_S_SMSCALE_HOST, "");
        FTP_PORT = SharedPreferencesManager
                .getInt(PREF_SMSCALE, PK_I_SMSCALE_PORT, 21);
        FTP_USER = SharedPreferencesManager
                .getText(PREF_SMSCALE, PK_S_SMSCALE_USERNAME, "");
        FTP_PASS = SharedPreferencesManager
                .getText(PREF_SMSCALE, PK_S_SMSCALE_PASSWORD, "");
        ENCODING_CHARSET = SharedPreferencesManager
                .getText(PREF_SMSCALE, PK_S_SMSCALE_ENCODING, "");
        FULLSCALE_ENABLED = SharedPreferencesManager
                .getBoolean(PREF_SMSCALE, PK_B_SMSCALE_FULLSCALE, true);
    }

    /**
     * 返回 EslSyncManager 实例
     *
     * @return
     */
    public static SMScaleSyncManager getInstance() {
        if (instance == null) {
            synchronized (SMScaleSyncManager.class) {
                if (instance == null) {
                    instance = new SMScaleSyncManager();
                }
            }
        }
        return instance;
    }

    public SMScaleSyncManager() {
        initialize();
    }

    public int getSyncMode() {
        return syncMode;
    }

    /**
     * 设置同步方式
     */
    public void setSyncMode(int syncMode) {
        this.syncMode = syncMode;

        SharedPreferencesHelper.set(PK_SMSCALE_GOODSINFO_LASTCURSOR, "");
    }

    /**
     * 上传POS订单
     */
    public synchronized void sync() {
        if (bSyncInProgress) {
            uploadProcess("EslSyncManager--正在同步价签商品...");
            return;
        }

        if (syncMode == SYNC_MODE_FULLSCALE) {
            ZLogger.d("EslSyncManager--全量同步");
            SharedPreferencesHelper.set(PK_SMSCALE_GOODSINFO_LASTCURSOR, "");
        }
        writeFile();
    }


    /**
     * 上传结束
     */
    private void uploadProcess(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = true;
        EventBus.getDefault().post(new SMScaleSyncManagerEvent(SMScaleSyncManagerEvent.EVENT_ID_SYNC_DATA_PROCESS));
    }

    /**
     * 上传结束
     */
    private void uploadFinished(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new SMScaleSyncManagerEvent(SMScaleSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    /**
     * 上传结束失败
     */
    private void uploadFailed(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new SMScaleSyncManagerEvent(SMScaleSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED));
    }

    /**
     * 保存商品数据到文件<br>
     * 根据上一次同步游标同步订单数据
     */
    private void writeFile() {
        String lastCursor = SharedPreferencesHelper.getText(PK_SMSCALE_GOODSINFO_LASTCURSOR);
        ZLogger.d(String.format("EslSyncManager--最后一次同步商品的更新时间(%s)。", lastCursor));

        SMScaleFileAsyncTask asyncTask = new SMScaleFileAsyncTask(lastCursor);
        asyncTask.execute();
    }

    private class SMScaleFileAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String lastCursor;
        private Date newCursor = null;

        public SMScaleFileAsyncTask(String lastCursor) {
            this.lastCursor = lastCursor;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String strWhere = String.format("cateType = '%d' and updatedDate > '%s'",
                        CateApi.BACKEND_CATE_BTYPE_FRESH, lastCursor);
                List<PosProductEntity> goodsList = PosProductService.get()
                        .queryAllAsc(strWhere, new PageInfo(1, MAX_SYNC_PAGESIZE));
                if (goodsList == null || goodsList.size() < 1) {
                    uploadProcess(String.format("SMScaleFileAsyncTask--没有商品需要下发(%s)", lastCursor));
                    return false;
                }

                File file = SMScaleSyncManager.getCSVFile();
                CSVWriter csvWriter;
                if (StringUtils.isEmpty(ENCODING_CHARSET)) {
                    csvWriter = new CSVWriter(new FileWriter(file));
                } else {
                    csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file),
                            ENCODING_CHARSET));
                }

                List<String[]> allElements = new ArrayList<>();
//        PLU号码,单价,品名信息,称重标识（1非称重，0称重）,标签格式,分类,表示位（F1，F2)
                allElements.add(COLUMNS);
                for (PosProductEntity goods : goodsList) {
                    //保存最大时间游标
                    if (newCursor == null || goods.getUpdatedDate() == null
                            || newCursor.compareTo(goods.getUpdatedDate()) <= 0) {
                        newCursor = goods.getUpdatedDate();
                    }

                    allElements.add(new String[]{goods.getBarcode(),
                            String.format("%.0f", goods.getCostPrice() * 100), goods.getName(),
                            "1"});
                }
                csvWriter.writeAll(allElements);
                csvWriter.close();
                ZLogger.d("\n\nGenerated CSV File:\n\n");
            } catch (IOException e) {
                e.printStackTrace();
                ZLogger.e(String.format("SMScaleFileAsyncTask failed, %s", e.toString()));
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("SMScaleFileAsyncTask failed, %s", e.toString()));
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                // 保存批量上传订单时间
                SharedPreferencesHelper.set(PK_SMSCALE_GOODSINFO_LASTCURSOR,
                        TimeUtil.format(newCursor, TimeCursor.InnerFormat));
//                uploadFailed("EslSyncManager--等待继续推送商品");
                //继续上传订单
                uploadFile2Ftp();
            } else {
                uploadFailed("EslSyncManager--批量推送商品结束");
            }
        }


        @Override
        protected void onPreExecute() {
            uploadProcess(String.format("EslSyncManager--准备推送商品到电子秤(%s)", lastCursor));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }


    private void uploadFile2Ftp() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            uploadFailed("EslSyncManager--网络未连接，暂停同步电子秤商品。");
            return;
        }

//Create instance for AsyncCallWS
        UploadFile2FtpTask task = new UploadFile2FtpTask();
        //Call execute
        task.execute();
    }


    class UploadFile2FtpTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;

        protected Integer doInBackground(String... urls) {
            File file = SMScaleSyncManager.getCSVFile();
            upload2Ftp(file, new MyTransferListener());

            return 0;
        }

        protected void onPostExecute(Integer progress) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }


    public class MyTransferListener implements FTPDataTransferListener {

        public void started() {
//            btn.setVisibility(View.GONE);
            // Transfer started
            ZLogger.d(" Upload Started ...");

            //System.out.println(" Upload Started ...");
        }

        public void transferred(final int length) {

            // Yet other length bytes has been transferred since the last time this
            // method was called
            ZLogger.d(" transferred ...");

            //System.out.println(" transferred ..." + length);
        }

        public void completed() {

//            btn.setVisibility(View.VISIBLE);
            // Transfer completed
            ZLogger.d(" completed ...");

            //System.out.println(" completed ..." );
        }

        public void aborted() {

//            btn.setVisibility(View.VISIBLE);
            // Transfer aborted
            ZLogger.d("  transfer aborted , please try again...");

            //System.out.println(" aborted ..." );
        }

        public void failed() {

//            btn.setVisibility(View.VISIBLE);
            // Transfer failed
            ZLogger.d("failed...");
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    System.out.println(" failed ..." );
//                }
//            });
        }
    }

    private List<String[]> genFileData(List<PosProductEntity> goodsList) {
//        beanToCsv.write()
        List<String[]> allElements = new ArrayList<>();
//        PLU号码,单价,品名信息,称重标识（1非称重，0称重）,标签格式,分类,表示位（F1，F2)
        allElements.add(COLUMNS);
        for (PosProductEntity goods : goodsList) {
            allElements.add(new String[]{goods.getBarcode(),
                    String.format("%.0f", goods.getCostPrice() * 100), goods.getName(),
                    "1"});
        }

        return allElements;
    }

    /**
     * 获取CSV文件，提交成功后需要手动删除文件，程序内部采用增量的方式更新。
     */
    public static File getCSVFile() {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

//        long timestamp = System.currentTimeMillis();
//        String time = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
//        String fileName = SMSCALE_CSV_FILENAME;// + time + ".csv";// + "-" + timestamp

        return new File(exportDir, SMSCALE_CSV_FILENAME);
    }

    public class SMScaleSyncManagerEvent {
        public static final int EVENT_ID_SYNC_DATA_START = 0X03;//同步数据开始
        public static final int EVENT_ID_SYNC_DATA_PROCESS = 0X04;//同步数据处理中
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X05;//同步数据结束
        public static final int EVENT_ID_SYNC_DATA_FAILED = 0X06;//同步数据失败

        private int eventId;
        private Bundle args;//参数

        public SMScaleSyncManagerEvent(int eventId) {
            this.eventId = eventId;
        }

        public SMScaleSyncManagerEvent(int eventId, Bundle args) {
            this.eventId = eventId;
            this.args = args;
        }

        public int getEventId() {
            return eventId;
        }

        public Bundle getArgs() {
            return args;
        }
    }

}
