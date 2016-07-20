package com.mfh.litecashier.hardware.SMScale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mfh.framework.core.logger.ZLogger;
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
        File file = SMScaleSyncManager.getCSVFile();

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            writer.writeAll(generateTestData());
            writer.close();
            ZLogger.d("\n\nGenerated CSV File:\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
        }
    }

    @OnClick(R.id.button_write_csv_encoding)
    public void writeCSVEncoding() {
        try {
            File file = SMScaleSyncManager.getCSVFile();
            CSVWriter csvWriter;
            if (StringUtils.isEmpty(FTPManager.ENCODING_CHARSET)) {
                csvWriter = new CSVWriter(new FileWriter(file));
            } else {
                csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), FTPManager.ENCODING_CHARSET));
            }
            csvWriter.writeAll(generateTestData());
            csvWriter.close();
            ZLogger.d("\n\nGenerated CSV File:\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
        }
    }


    @OnClick(R.id.button_bean2csv)
    public void writeBean2CSV() {
        File file = SMScaleSyncManager.getCSVFile();
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
            ZLogger.d("\n\nGenerated CSV File:\n\n");
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
        }
    }

    @OnClick(R.id.button_csv2bean)
    public void readCSV2Bean() {
        File file = SMScaleSyncManager.getCSVFile();
        ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
        strat.setType(SMGoods.class);
        strat.setColumnMapping(SMScaleSyncManager.COLUMNS);

        CsvToBean csv = new CsvToBean();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file));
            List list = csv.parse(strat, reader); // list of Order bean
//            for (SMGoods goods : list.)
//            ZLogger.d(list.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
        }
        String[] nextLine;
        try {
            if (reader != null) {
                while ((nextLine = reader.readNext()) != null) {

                    StringBuilder sb = new StringBuilder();
                    for (String str : nextLine) {
                        sb.append("[" + str + "]");
                    }
                    sb.append("\n");
                    ZLogger.d(sb.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
        }
    }

    @OnClick(R.id.button_read_csv)
    public void readCSV() {
        // To ignore Processing of 1st row
//        CSVReader reader = new CSVReader(new FileReader(ADDRESS_FILE), ',', '\"', 1);

//        CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open("test.csv")));//Specify asset file name
//        - See more at: http://www.theappguruz.com/blog/parse-csv-file-in-android-example-sample-code#sthash.ubT9iaWd.dpuf
        File file = SMScaleSyncManager.getCSVFile();
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
            ZLogger.d(e.toString());
        }
        String[] nextLine;
        try {
            if (reader != null) {
                while ((nextLine = reader.readNext()) != null) {

                    StringBuilder sb = new StringBuilder();
                    for (String str : nextLine) {
                        sb.append("[" + str + "]");
                    }
                    sb.append("\n");
                    ZLogger.d(sb.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
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
            File file = SMScaleSyncManager.getCSVFile();
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), " Upload Started ...", Toast.LENGTH_SHORT).show();
                }
            });
            //System.out.println(" Upload Started ...");
        }

        public void transferred(final int length) {

            // Yet other length bytes has been transferred since the last time this
            // method was called
            ZLogger.d(" transferred ...");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), " transferred ..." + length, Toast.LENGTH_SHORT).show();
                }
            });
            //System.out.println(" transferred ..." + length);
        }

        public void completed() {

//            btn.setVisibility(View.VISIBLE);
            // Transfer completed
            ZLogger.d(" completed ...");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), " completed ...", Toast.LENGTH_SHORT).show();
                }
            });
            //System.out.println(" completed ..." );
        }

        public void aborted() {

//            btn.setVisibility(View.VISIBLE);
            // Transfer aborted
            ZLogger.d("  transfer aborted , please try again...");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), " transfer aborted , please try again...", Toast.LENGTH_SHORT).show();
                }
            });
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

    private List<SMGoods> generateTestBean() {
        List<SMGoods> entityList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            SMGoods smGoods = new SMGoods();
            smGoods.setBarcode(StringUtils.getNonceDecimalString(12));
//            SMGoods.setProductId(1L);
            smGoods.setName(StringUtils.genNonceChinease(12));
            smGoods.setUnit("测试商品单位");
            smGoods.setPrice(1.11 * i);
            smGoods.setCategory(i * 2);
//            SMGoods.setTenantId(1L);
//            SMGoods.setProviderId(1L);
            entityList.add(smGoods);
        }

        return entityList;
    }

    private List<String[]> toStringArray(List<SMGoods> goodsList) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(SMScaleSyncManager.COLUMNS);
        for (SMGoods goods : goodsList) {
            records.add(new String[]{String.valueOf(goods.getBarcode()), String.valueOf(goods.getPrice()),
                    goods.getName(), String.valueOf(goods.getCategory())});
        }
        return records;
    }


    private List<String[]> generateTestData() {
//        beanToCsv.write()
        List<String[]> allElements = new ArrayList<>();
//        PLU号码,单价,品名信息,称重标识（1非称重，0称重）,标签格式,分类,表示位（F1，F2)
        allElements.add(SMScaleSyncManager.COLUMNS);
        allElements.add(new String[]{"0001",
                StringUtils.getNonceDecimalString(3), StringUtils.genNonceChinease(4),
                "1"});
        allElements.add(new String[]{"0002",
                StringUtils.getNonceDecimalString(3), StringUtils.genNonceChinease(4),//"萝卜",
                "1"});
        allElements.add(new String[]{"0003",
                StringUtils.getNonceDecimalString(3), StringUtils.genNonceChinease(4),//"玉米",
                "3"});
        allElements.add(new String[]{"0004",
                StringUtils.getNonceDecimalString(3), StringUtils.genNonceChinease(4),//"樱桃",
                "1"});
        allElements.add(new String[]{"0005",
                StringUtils.getNonceDecimalString(3), StringUtils.genNonceChinease(4),//"香蕉",
                "1"});
        return allElements;
    }

}
