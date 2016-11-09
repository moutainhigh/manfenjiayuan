package com.mfh.litecashier.hardware.SMScale;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.utils.Encoding;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.utils.SharedPreferencesHelper;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 寺冈电子秤数据同步管理
 * <ol>
 * 适用场景
 * <li>同步生鲜数据到寺冈电子秤管理后台</li>
 * <li>同步水果数据到寺冈电子秤管理后台</li>
 * </ol>
 * <p/>
 * <ol>
 * 同步策略
 * <li>全量更新，每次都会同步所有数据。同步前需要删除csv文件</li>
 * <li>增量更新，每天首次开机全量同步一次（保证数据一致性）,之后仅同步有更新的数据。</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class SMScaleSyncManager2 extends FTPManager {

    public static final String PREF_SMSCALE = "pref_smscale";
    public static final String PK_S_SMSCALE_HOST = "pk_smscale_host";   //主机IP
    public static final String PK_I_SMSCALE_PORT = "pk_smscale_port";   //主机端口号
    public static final String PK_S_SMSCALE_USERNAME = "pk_smscale_username";//用户名
    public static final String PK_S_SMSCALE_PASSWORD = "pk_smscale_password";//密码
    public static final String PK_S_SMSCALE_ENCODING = "pk_smscale_encoding";//文件编码
    public static final String PK_S_SMSCALE_LASTCURSOR = "pk_smscale_lastcursor";//更新游标

    public static String SMSCALE_CSV_FILENAME = "smscale_mixicook";//.csv
    public static String FOLDER_PATH_SMSCALE = "smscale";
    public static final SimpleDateFormat FORMAT_YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /**
     * Digistore @Label 模版
     * <ol>
     * <li>"商品编号",最多6位</li>
     * <li>"单价",精确到分，例1.88元应该写成188</li>
     * <li>"项目编号",和“商品编号”一样传值</li>
     * <li>"条码标志位1和2",和“商品编号”一样传值</li>
     * <li>"分类",商品所属类目</li>
     * <li>"品名信息",商品名称，字符串</li>
     * </ol>
     */
    public static String[] TEMPLATE_COLUMNS = new String[]{"商品编号", "单价",
            "项目编号", "条码标志位1和2", "分类", "品名信息", "称重标志"};
    public static String FLAG_F12 = "02";

    public static String[] ENCODING_CHARSET_SET = new String[]{Encoding.CHARSET_GBK, Encoding.CHARSET_UTF_8};

    //每次同步最大数量
    private static final int MAX_SYNC_PAGESIZE = 40;

    private boolean bSyncInProgress = false;//是否正在同步

    private static SMScaleSyncManager2 instance = null;

    static {
        initialize();
    }

    /**
     * 初始化
     */
    public static void initialize() {
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
    }

    /**
     * 返回 SMScaleSyncManager 实例
     *
     * @return
     */
    public static SMScaleSyncManager2 getInstance() {
        if (instance == null) {
            synchronized (SMScaleSyncManager2.class) {
                if (instance == null) {
                    instance = new SMScaleSyncManager2();
                }
            }
        }
        return instance;
    }

    public SMScaleSyncManager2() {
        initialize();
    }


    /**
     * 获取电子秤同步开始游标
     */
    public String getScaleStartCursor() {
        String startCursor = SharedPreferencesManager.getText(SMScaleSyncManager2.PREF_SMSCALE,
                SMScaleSyncManager2.PK_S_SMSCALE_LASTCURSOR);
        ZLogger.df(String.format("最后一次电子秤同步的更新时间(%s)。", startCursor));

//        //得到指定模范的时间
        if (!StringUtils.isEmpty(startCursor)) {
            try {
                Date d1 = TimeCursor.InnerFormat.parse(startCursor);
                Date rightNow = new Date();
                if (d1.compareTo(rightNow) > 0) {
                    startCursor = TimeCursor.InnerFormat.format(rightNow);
                    ZLogger.df(String.format("上次电子秤同步更新游标大于当前时间，使用当前时间(%s)。", startCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(String.format("获取电子秤同步开始游标失败: %s", e.toString()));
            }
        }

        return startCursor;
    }

    /**
     * 正在同步
     */
    private void syncProcess(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = true;
        EventBus.getDefault().post(new SMScaleSyncManagerEvent(SMScaleSyncManagerEvent.EVENT_ID_SYNC_DATA_PROCESS));
    }

    /**
     * 上传结束
     */
    private void syncFinished(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new SMScaleSyncManagerEvent(SMScaleSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    /**
     * 上传结束失败
     */
    private void syncFailed(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new SMScaleSyncManagerEvent(SMScaleSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED));
    }

    /**
     * 上传POS商品库
     */
    public synchronized void sync() {
        if (!SharedPreferencesHelper
                .getBoolean(SharedPreferencesHelper.PK_B_SYNC_SMSCALE_FTP_ENABLED, false)){
            syncFailed("请在设置中打开同步商品库到电子秤同步开关。");
            return;
        }

//        if (StringUtils.isEmpty(GreenTagsApi.LOCAL_SERVER_IP)){
//            syncFailed("LOCAL_SERVER_IP不能为空，请先在设置页面设置ip地址。");
//            return;
//        }

        if (bSyncInProgress) {
            syncProcess("正在同步电子秤商品...");
            return;
        }

        String lastCursor = getScaleStartCursor();

        WriteFileAsyncTask asyncTask = new WriteFileAsyncTask(lastCursor);
        asyncTask.execute();
    }

    /**
     * 写入CSV文件
     */
    private class WriteFileAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String startCursor;
        private Date newCursor = null;
        private String sqlWhere;//查询条件
        private PageInfo pageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_PAGESIZE);//翻页
        private File file;

        public WriteFileAsyncTask(String startCursor) {
            this.startCursor = startCursor;
            this.file = SMScaleSyncManager2.getCSVFile2();
            //同步生鲜／水果／水台商品到电子秤
            this.sqlWhere = String.format("(cateType = '%d' or cateType = '%d'" +
                    " or cateType = '%d') " +
                            "and updatedDate >= '%s'",
                    CateApi.BACKEND_CATE_BTYPE_FRESH,
                    CateApi.BACKEND_CATE_BTYPE_FRUIT, CateApi.BACKEND_CATE_BTYPE_WARTER,
                    startCursor);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                pageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
                List<PosProductEntity> goodsList = PosProductService.get()
                        .queryAllAsc(sqlWhere, pageInfo);
                if (goodsList == null || goodsList.size() < 1) {
                    syncFinished(String.format("没有商品需要写入CSV文件(%s)", startCursor));
                    return false;
                }
                syncProcess(String.format("查询到 %d 个商品需要同步，" +
                                "当前页数 %d/%d,每页最多 %d 个商品(%s)",
                        pageInfo.getTotalCount(), pageInfo.getPageNo(), pageInfo.getTotalPage(),
                        pageInfo.getPageSize(), startCursor));

                CSVWriter csvWriter;
                if (StringUtils.isEmpty(ENCODING_CHARSET)) {
                    csvWriter = new CSVWriter(new FileWriter(file));
                } else {
                    csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file),
                            ENCODING_CHARSET));
                }

                List<String[]> allElements = new ArrayList<>();
//        PLU号码,单价,品名信息,称重标识（1非称重，0称重）,标签格式,分类,表示位（F1，F2)
                allElements.add(TEMPLATE_COLUMNS);

                for (PosProductEntity goods : goodsList) {
                    //保存最大时间游标
                    if (newCursor == null || goods.getUpdatedDate() == null
                            || newCursor.compareTo(goods.getUpdatedDate()) <= 0) {
                        newCursor = goods.getUpdatedDate();
                    }

                    String plu = goods.getBarcode();
                    Double costPrice = goods.getCostPrice();
                    //过滤掉无效的商品
                    if (StringUtils.isEmpty(plu) || costPrice == null) {
                        continue;
                    }
                    //商品名称太长无法打印（打印空白）
                    if (plu.length() > 8) {
                        plu = plu.substring(0, 8);
                    }
                    allElements.add(new String[]{plu,
                            String.format("%.0f", costPrice * 100),
                            plu,
                            FLAG_F12,
                            String.valueOf(goods.getCateType()),
                            goods.getName(),
                            PriceType.WEIGHT.equals(goods.getPriceType()) ? "0" : "1"});
                }
                syncProcess(String.format("开始写入%d个商品到CSV文件", goodsList.size()));
                csvWriter.writeAll(allElements);


                //判断是否还有数据需要同步
                while (pageInfo.hasNextPage()) {
                    pageInfo.moveToNext();

                    List<PosProductEntity> goodsList2 = PosProductService.get()
                            .queryAllAsc(sqlWhere, pageInfo);
                    if (goodsList2 == null || goodsList2.size() < 1) {
                        syncFinished(String.format("没有商品需要写入CSV文件(%s)", startCursor));
                        return false;
                    }
                    syncProcess(String.format("查询到 %d 个商品需要同步，" +
                                    "当前页数 %d/%d,每页最多 %d 个商品(%s)",
                            pageInfo.getTotalCount(), pageInfo.getPageNo(), pageInfo.getTotalPage(),
                            pageInfo.getPageSize(), startCursor));

                    for (PosProductEntity goods : goodsList2) {
                        //保存最大时间游标
                        if (newCursor == null || goods.getUpdatedDate() == null
                                || newCursor.compareTo(goods.getUpdatedDate()) <= 0) {
                            newCursor = goods.getUpdatedDate();
                        }

                        String plu = goods.getBarcode();
                        Double costPrice = goods.getCostPrice();
                        //过滤掉无效的商品
                        if (StringUtils.isEmpty(plu) || costPrice == null) {
                            continue;
                        }
                        //商品名称太长无法打印（打印空白）
                        if (plu.length() > 8) {
                            plu = plu.substring(0, 8);
                        }
                        allElements.add(new String[]{plu,
                                String.format("%.0f", costPrice * 100),
                                plu,
                                FLAG_F12,
                                String.valueOf(goods.getCateType()),
                                goods.getName(),
                                PriceType.WEIGHT.equals(goods.getPriceType()) ? "0" : "1"});
                    }
                    syncProcess(String.format("开始写入%d个商品到CSV文件", goodsList2.size()));
                    csvWriter.writeAll(allElements);
                    ZLogger.df(String.format("写入CSV文件成功：%s",
                            TimeUtil.format(newCursor, TimeCursor.InnerFormat)));
                }

                csvWriter.close();
//                ZLogger.d("\n\nGenerated CSV File:\n\n");
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("写入CSV文件失败, %s", e.toString()));
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                syncProcess("写入CSV文件成功");
                //继续上传订单
                uploadFile2Ftp(file, newCursor);
            } else {
                syncFailed("写入CSV文件结束（失败/异常/）");
            }
        }


        @Override
        protected void onPreExecute() {
            syncProcess(String.format("准备推送商品到电子秤(%s)", startCursor));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    /**
     * 上传CSV文件到FTP
     */
    private void uploadFile2Ftp(File file, Date cursor) {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            syncFailed("网络未连接，暂停上传CSV文件到FTP服务器。");
            return;
        }

        syncProcess("准备上传CSV文件到FTP服务器");
//Create instance for AsyncCallWS
        UploadFile2FtpTask task = new UploadFile2FtpTask(file, cursor);
        //Call execute
        task.execute();
    }

    /**
     * 上传CSV文件到FTP
     */
    class UploadFile2FtpTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;
        private File file;
        private Date cursor;

        public UploadFile2FtpTask(File file, Date cursor) {
            this.file = file;
            this.cursor = cursor;
        }

        protected Integer doInBackground(String... urls) {
//            File file = SMScaleSyncManager.getCSVFile();
            upload2Ftp(file, new MyTransferListener(cursor));

            return 0;
        }

        protected void onPostExecute(Integer progress) {
            // TODO: check this.exception
            // TODO: do something with the feed
//            uploadStepFinished(cursor);
        }
    }


    public class MyTransferListener implements FTPDataTransferListener {

        private Date cursor;

        public MyTransferListener(Date cursor) {
            this.cursor = cursor;
        }

        public void started() {
            // Transfer started
            ZLogger.d("Upload FTP Started ...");
        }

        public void transferred(final int length) {
            // Yet other length bytes has been transferred since the last time this
            // method was called
            ZLogger.d("Upload FTP transferred ..." + length);
        }

        public void completed() {
            String currentCursor = TimeUtil.format(cursor, TimeCursor.InnerFormat);
            SharedPreferencesManager.set(SMScaleSyncManager2.PREF_SMSCALE,
                    SMScaleSyncManager2.PK_S_SMSCALE_LASTCURSOR, currentCursor);
            ZLogger.df(String.format("保存商品同步电子秤游标:%s", currentCursor));

            // Transfer completed
            syncFinished("Upload FTP completed ...");
        }

        public void aborted() {
            // Transfer aborted
            syncFailed("Upload FTPtransfer aborted , please try again...");
        }

        public void failed() {
            // Transfer failed
            syncFailed("transfer failed , please try again...");
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    System.out.println(" failed ..." );
//                }
//            });
        }
    }

    /**
     * 获取CSV文件，提交成功后需要手动删除文件，程序内部采用增量的方式更新。
     * smscale_mixicook2016-06-15-1465997239811.csv
     */
    @Deprecated
    public static File getCSVFile() {
        long timestamp = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String fileName = SMSCALE_CSV_FILENAME + time + "-" + timestamp + ".csv";//
        ZLogger.df("csvFile: " + fileName);

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        return new File(exportDir, fileName);
    }

    public static File getCSVFile2() {
        String time = FORMAT_YYYYMMDD.format(new Date());
        long timestamp = System.currentTimeMillis();
        String fileName = SMSCALE_CSV_FILENAME + time + "-" + timestamp + ".csv";//
        ZLogger.df("csvFile: " + fileName);

        return FileUtil.getSaveFile(FOLDER_PATH_SMSCALE, fileName);
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


    /**
     * 删除过期文件
     */
    public static void deleteOldFiles(final int saveDate) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 0 - saveDate);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
//        ZLogger.d("" + DATE_FORMAT.format(calendar.getTime()));

//        /storage/emulated/0/smscale
                String path = FileUtil.getSavePath(FOLDER_PATH_SMSCALE);
                ZLogger.d(path);
                File file = new File(path);
                if (!file.exists()) {
//            ZLogger.d(String.format("%s 不存在", file.getAbsolutePath()));
                    return;
                }

                if (!file.isDirectory()) {
//            ZLogger.d(String.format("%s 不是目录", file.getAbsolutePath()));
                    return;
                }

                for (File child : file.listFiles()) {
                    if (child.isDirectory()) {
//                ZLogger.d(String.format("%s是目录，不需要删除", child.getName()));
                        continue;
                    }

                    try {
                        String name = child.getName();
                        //文件名格式是smscale_mixicook2016-06-15-1465997239811.csv，所以格式不对的，也要删除。系统中可能存在很多2016-0001-01.log等等这样的文件。
                        if (StringUtils.isEmpty(name) || name.length() != 44) {
                            ZLogger.df(String.format("删除无效csv文件 %s", name));
                            child.delete();
                            continue;
                        }
                        String dateStr = name.substring(16, 26);
                        Date date = FORMAT_YYYYMMDD.parse(dateStr);
//                ZLogger.d(" child:" + DATE_FORMAT.format(date));
                        if (date.before(calendar.getTime())) {
                            ZLogger.df(String.format("csv文件已经过期，删除%s", name));
                            child.delete();
                        }
                    } catch (ParseException e) {
                        child.delete();
//                e.printStackTrace();
                        ZLogger.ef(String.format("%s", e.toString()));
                    }
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String escCommand) {
                    }
                });
    }

}
