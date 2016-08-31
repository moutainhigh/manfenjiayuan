package com.mfh.litecashier.hardware.SMScale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.R;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;


/**
 * 绿泰电子价签
 * {@link http://www.sauronsoftware.it/projects/home/}
 * A placeholder fragment containing a simple view.
 */
public class SMScaleFragmenet extends BaseFragment {
    //Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
    private static final String ADDRESS_FILE = "bingshanguxue/smscale.csv";

    @Bind(R.id.tv_url)
    TextView tvUrl;

    public SMScaleFragmenet() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_smscale;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

    }

    @OnClick(R.id.button_write_csv)
    public void writeCSV() {
        File file = SMScaleSyncManager2.getCSVFile2();

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            writer.writeAll(generateTestData());
            writer.close();
            ZLogger.df("\n\nGenerated CSV File:\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

    @OnClick(R.id.button_write_csv_encoding)
    public void writeCSVEncoding() {
        try {
            File file = SMScaleSyncManager2.getCSVFile2();
            CSVWriter csvWriter;
            if (StringUtils.isEmpty(FTPManager.ENCODING_CHARSET)) {
                csvWriter = new CSVWriter(new FileWriter(file));
            } else {
                csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), FTPManager.ENCODING_CHARSET));
            }
            csvWriter.writeAll(generateTestData());
            csvWriter.close();
            ZLogger.df("\n\nGenerated CSV File:\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }


    @OnClick(R.id.button_bean2csv)
    public void writeBean2CSV() {
        File file = SMScaleSyncManager2.getCSVFile2();
        //mapping of columns with their positions
//        ColumnPositionMappingStrategy<SMGoods> strat = new ColumnPositionMappingStrategy();
//        //Set mappingStrategy type to SMGoods Type
//        strat.setType(SMGoods.class);
//        //Setting the colums for mappingStrategy
//        strat.setColumnMapping(COLUMNS)
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            // write to csv file
//            BeanToCsv<SMGoods> csv = new BeanToCsv<>();

            //Writing SMGoods to csv file
//            csv.write(strat, writer, goodsList);

            writer.writeAll(toStringArray(generateTestBean()));
            writer.close();
            ZLogger.df("\n\nGenerated CSV File:\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

    @OnClick(R.id.button_csv2bean)
    public void readCSV2Bean() {
        File file = SMScaleSyncManager2.getCSVFile2();
        ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
        strat.setType(SMGoods.class);
        strat.setColumnMapping(SMScaleSyncManager2.TEMPLATE_COLUMNS);

        CsvToBean csv = new CsvToBean();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file));
//            List list = csv.parse(strat, reader); // list of Order bean
//            for (SMGoods goods : list.)
//            ZLogger.d(list.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
        String[] nextLine;
        try {
            if (reader != null) {
                while ((nextLine = reader.readNext()) != null) {

                    StringBuilder sb = new StringBuilder();
                    for (String str : nextLine) {
                        sb.append("[").append(str).append("]");
                    }
                    sb.append("\n");
                    ZLogger.df(sb.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

    @OnClick(R.id.button_read_csv)
    public void readCSV() {
        // To ignore Processing of 1st row
//        CSVReader reader = new CSVReader(new FileReader(ADDRESS_FILE), ',', '\"', 1);

//        CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open("test.csv")));//Specify asset file name
//        - See more at: http://www.theappguruz.com/blog/parse-csv-file-in-android-example-sample-code#sthash.ubT9iaWd.dpuf
        File file = SMScaleSyncManager2.getCSVFile2();
        if (!file.exists()) {
            Snackbar.make(tvUrl, "指定的CSV文件不存在", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
        String[] nextLine;
        try {
            if (reader != null) {
                while ((nextLine = reader.readNext()) != null) {

                    StringBuilder sb = new StringBuilder();
                    for (String str : nextLine) {
                        sb.append("[").append(str).append("]");
                    }
                    sb.append("\n");
                    ZLogger.df(sb.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

    /**
     * 上传文件到FTP
     */
    @OnClick(R.id.button_upload2ftp)
    public void upload2Ftp() {
        //Create instance for AsyncCallWS
        UploadFile2FtpTask task = new UploadFile2FtpTask();
        //Call execute
        task.execute();
    }


    class UploadFile2FtpTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;

        protected Integer doInBackground(String... urls) {
            File file = SMScaleSyncManager2.getCSVFile2();
            FTPManager.upload2Ftp(file, new MyTransferListener());

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
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.showHint("Upload Started...");
                    }
                });
            }
        }

        public void transferred(final int length) {

            // Yet other length bytes has been transferred since the last time this
            // method was called
            ZLogger.d(String.format("transferred %d...", length));
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.showHint(String.format("transferred %d...", length));
                    }
                });
            }
        }

        public void completed() {

//            btn.setVisibility(View.VISIBLE);
            // Transfer completed
            ZLogger.df(" completed ...");
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.showHint("completed ...");
                    }
                });
            }

            //System.out.println(" completed ..." );
        }

        public void aborted() {

//            btn.setVisibility(View.VISIBLE);
            // Transfer aborted
            ZLogger.df("  transfer aborted , please try again...");
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.showHint("transfer aborted , please try again...");
                    }
                });
            }
        }

        public void failed() {

//            btn.setVisibility(View.VISIBLE);
            // Transfer failed
            ZLogger.d("failed...");
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.showHint("failed...");
                    }
                });
            }
        }
    }

    private List<SMGoods> generateTestBean() {
        List<SMGoods> entityList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            SMGoods smGoods = new SMGoods();
            smGoods.setBarcode(StringUtils.getNonceDecimalString(6));
//            SMGoods.setProductId(1L);
            smGoods.setName(StringUtils.genNonceChinease(12));
            smGoods.setUnit("测试商品单位");
            smGoods.setPrice(1.11 * i);
            smGoods.setCategory(i * 2);
            smGoods.setFlag1(1);
            smGoods.setFlag2(1);
//            SMGoods.setTenantId(1L);
//            SMGoods.setProviderId(1L);
            entityList.add(smGoods);
        }

        return entityList;
    }

    private List<String[]> toStringArray(List<SMGoods> goodsList) {
        List<String[]> records = new ArrayList<>();
        //add header record
        records.add(SMScaleSyncManager2.TEMPLATE_COLUMNS);
        for (SMGoods goods : goodsList) {
            records.add(new String[]{goods.getBarcode(), String.valueOf(goods.getPrice()),
                    goods.getBarcode(), "20", String.valueOf(goods.getCategory()), goods.getName()});
        }
        return records;
    }


    private List<String[]> generateTestData() {
//        beanToCsv.write()
        List<String[]> allElements = new ArrayList<>();
//        PLU号码,单价,品名信息,称重标识（1非称重，0称重）,标签格式,分类,表示位（F1，F2)
        allElements.add(SMScaleSyncManager2.TEMPLATE_COLUMNS);
        for (int i = 0; i < 5; i++) {
            String plu = StringUtils.getNonceDecimalString(6);
            allElements.add(new String[]{plu,
                    StringUtils.getNonceDecimalString(3), plu,
                    StringUtils.getNonceDecimalString(2), StringUtils.genNonceChinease(4),
                    StringUtils.getNonceDecimalString(1)});
        }
        return allElements;
    }

}
