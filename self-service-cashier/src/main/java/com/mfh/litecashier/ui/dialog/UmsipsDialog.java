package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.chinaums.mis.bank.BankDAO;
import com.chinaums.mis.bank.ICallBack;
import com.chinaums.mis.bean.RequestPojo;
import com.chinaums.mis.bean.ResponsePojo;
import com.chinaums.mis.bean.TransCfx;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.pay.umsips.TransType;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;


/**
 * 银联参数设置
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class UmsipsDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private EditText etIp, etPort, etMchtId, etTermId;
    private Spinner mPortSpinner, mBaudrateSpinner;
    private Button btnSubmit, btnSign;
    private ImageButton btnClose;
    private ProgressBar progressBar;

    private ArrayAdapter<String> aspnDevices;
    private ArrayAdapter<CharSequence> adapter;

    public interface onDialogClickListener {
        void onDatasetChanged();
    }

    private onDialogClickListener mListener;


    private UmsipsDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private UmsipsDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_umsips, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        etIp = (EditText) rootView.findViewById(R.id.et_ip);
        etPort = (EditText) rootView.findViewById(R.id.et_port);
        etMchtId = (EditText) rootView.findViewById(R.id.et_mchtId);
        etTermId = (EditText) rootView.findViewById(R.id.et_termId);
        btnSign = (Button) rootView.findViewById(R.id.button_footer_negative);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        mPortSpinner = (Spinner) rootView.findViewById(R.id.spinner_port);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);
//        mPortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view,
//                                       int position, long id) {
//                String str = parent.getItemAtPosition(position).toString();
//                mPortSpinner.setPrompt(str);
//                ZLogger.d("onItemSelected " + position);
//                if (position == 0) {
//                    timeAdapter = new ArrayAdapter<>(context, R.layout.view_spinner_item, mTimes1);
//                } else {
//                    timeAdapter = new ArrayAdapter<>(context, R.layout.view_spinner_item, mTimes2);
//                }
//
//                mBaudrateSpinner.setAdapter(timeAdapter);
//                mBaudrateSpinner.setPrompt(timeAdapter.getItem(0));
////                timeAdapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // TODO Auto-generated method stub
//                ZLogger.d("onNothingSelected ");
//            }
//        });
        mBaudrateSpinner = (Spinner) rootView.findViewById(R.id.spinner_baudrate);
//        mBaudrateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view,
//                                       int position, long id) {
//                String str = parent.getItemAtPosition(position).toString();
//                mBaudrateSpinner.setPrompt(str);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // TODO Auto-generated method stub
//            }
//        });

        aspnDevices = new ArrayAdapter<>(context,
                R.layout.mfh_spinner_item_text, SerialManager.getInstance().getAvailablePath(null));
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPortSpinner.setAdapter(aspnDevices);
        mPortSpinner.setSelection(0);

        adapter = ArrayAdapter.createFromResource(context,
                R.array.baudrates_value, R.layout.mfh_spinner_item_text);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBaudrateSpinner.setAdapter(adapter);
        mBaudrateSpinner.setSelection(0);

        tvTitle.setText("银联参数设置");
        etIp.setEnabled(false);
        etPort.setEnabled(false);

//        etIp.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    DeviceUtils.hideSoftInput(getContext(), etIp);
//                }
//                etIp.requestFocus();
////                etInput.setSelection(etInput.length());
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
//        etAmount.setFilters(new InputFilter[]{new DecimalInputFilter(2)});
//        etPort.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    DeviceUtils.hideSoftInput(getContext(), etPort);
//                }
//                etPort.requestFocus();
////                etInput.setSelection(etInput.length());
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
//        etTermId.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    DeviceUtils.hideSoftInput(getContext(), etTermId);
//                }
//                etTermId.requestFocus();
////                etInput.setSelection(etInput.length());
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public UmsipsDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = d.getHeight();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();

        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    public void init(onDialogClickListener listener) {
        this.mListener = listener;

        refresh();
    }

    /**
     * 刷新会员信息
     */
    private void refresh() {
        etIp.setText(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_IP, "10.139.93.98"));
        etPort.setText(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_PORT, "19003"));
        etMchtId.setText(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_MCHTID, "898320554115217"));
        etTermId.setText(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_TERMID));

        String umsipsPort = SerialManager.getUmsipsPort();
        aspnDevices = new ArrayAdapter<>(getContext(),
                R.layout.mfh_spinner_item_text, SerialManager.getInstance().getAvailablePath(umsipsPort));
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPortSpinner.setAdapter(aspnDevices);

        mPortSpinner.setSelection(aspnDevices.getPosition(umsipsPort));
        mBaudrateSpinner.setSelection(adapter.getPosition(SerialManager.getUmsipsBaudrate()));
    }

    /**
     * 保存参数
     */
    private void submit() {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        String ip = etIp.getText().toString();
        if (StringUtils.isEmpty(ip)) {
            DialogUtil.showHint("请输入IP");
            btnSubmit.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        String port = etPort.getText().toString();
        if (StringUtils.isEmpty(port)) {
            DialogUtil.showHint("请输入端口号");
            btnSubmit.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        String mchtId = etMchtId.getText().toString();
        if (StringUtils.isEmpty(mchtId)) {
            DialogUtil.showHint("请输入商户号");
            btnSubmit.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        String termId = etTermId.getText().toString();
        if (StringUtils.isEmpty(termId)) {
            DialogUtil.showHint("请输入终端编号");
            btnSubmit.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_UMSIPS_IP, ip);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_UMSIPS_MCHTID, mchtId);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_UMSIPS_PORT, port);
        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_UMSIPS_TERMID, termId);
        SerialManager.setUmsipsPort(mPortSpinner.getSelectedItem().toString());
        SerialManager.setUmsipsBaudrate(mBaudrateSpinner.getSelectedItem().toString());

        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);

        if (mListener != null) {
            mListener.onDatasetChanged();
        }

        dismiss();
    }

    private BankDAO bankDAO;
    private boolean isRunningThread;
    private RequestPojo request;
    private ResponsePojo response;
    private TransCfx transCFX;

    private class BackCall implements ICallBack {
        private BackCall() {
        }

        public void getCallBack(String stateCode, String stateTips) {
            ZLogger.d("stateCode=" + stateCode + "|" + "stateTips=" + stateTips);
//            DialogUtil.showHint(String.format("%s--%s", stateCode, stateTips));
        }
    }

    /**
     * 签到
     */
    public void sign() {
        ZLogger.d("正在签到，请稍候......");
        initBasicValue();
        initBankRequestValue(TransType.SIGN);
        new Thread(new Runnable() {
            public void run() {
                try {
                    isRunningThread = true;
                    ZLogger.d("正在签到，请稍候......");
                    bankDAO = new BankDAO();
                    bankDAO.getCallBack(new BackCall());
                    //同步
                    response = bankDAO.bankall(transCFX, request);
                    if (response != null) {
                        ZLogger.d(String.format("签到结束：%s", response.toString()));
                    }
                    isRunningThread = false;
                } catch (Exception e) {
                    ZLogger.e(e.toString());
                    isRunningThread = false;
                }
            }

        }
        )
                .start();
    }

    /**
     * 初始化交易基础信息
     * 域名：upos.chinaums.com
     * ULINK传统：端口 19003，TPDU 6000030000
     */
    private void initBasicValue() {
        ZLogger.d("正在初始化交易基础信息，请稍候......");
        this.transCFX = new TransCfx();
        //公网标志位(ssl_on)	int		8	是	0-表示专线 1-表示公网
        this.transCFX.setSsl_on(0);
        //主机ip地址(ip)	String		16	是	主机ip地址
        this.transCFX.setIp(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_IP, "10.139.93.98"));//upos.chinaums.com
        //主机端口号(port)	int		8	是	主机端口号
        this.transCFX.setPort(Integer.valueOf(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_PORT, "19003")));//19003
        //终端号(termId)	String	8	是	终端号
        this.transCFX.setTermId(SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_TERMID, "55877236"));
        //终端信息(term_info)   	String		64	否	终端信息（公网相关）
        this.transCFX.setTerm_info("");
        //终端序列号(ssl_sn)	String		39	否	终端序列号（公网相关）
        this.transCFX.setSsl_sn("");
        //数字证书路径(ssl_cert)	String		200	否	数字证书路径（公网相关）
        this.transCFX.setSsl_cert("");
        //TPDU传送协议数据单元(tpdu)    String		10	是
        this.transCFX.setTpdu("6000030000");
        //商户号(mchtId)  String	15	是	商户号
        String mchtId = SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_MCHTID, "898320554115217");
        this.transCFX.setMchtId(mchtId);
        //SN密文(authSN)	String		60	是	SN密文
        if (mchtId.equalsIgnoreCase("898320554115217")) {
            this.transCFX.setAuthSN("277797D4DE797832B650C201EE08DC5300C1771A372DCC6E08E57799A377CF91");
        } else if (mchtId.equalsIgnoreCase("898320554115269")) {
            this.transCFX.setAuthSN("9907666AAB467BBF3A3067A9BE71FD7B54AC0A6D7B8AB996BB5C097417D453B6");
        } else {
            this.transCFX.setAuthSN("277797D4DE797832B650C201EE08DC5300C1771A372DCC6E08E57799A377CF91");
        }
        //串口值(devPath)	String		50	是	串口值
        this.transCFX.setDevPath(SerialManager.getUmsipsPort());
        //波特率(baudRate)	int		8	是	波特率
        this.transCFX.setBaudRate(Integer.valueOf(SerialManager.getUmsipsBaudrate()));
        ZLogger.df("初始化交易基础信息: " + this.transCFX.toString());
    }

    /**
     * 初始化交易请求
     */
    private void initBankRequestValue(String transType) {
        this.request = new RequestPojo();
        //操作员号(operid)  String	operId	8	必填	左对齐，不足右补空格
//        MfhLoginService.get().getCurrentGuId();
        this.request.setOperId(CashierHelper.getOperateId(8));
        //POS机号(posId)    String    8   必填	左对齐，不足右补空格(0)
        this.request.setPosId(CashierHelper.getTerminalId(8));
        //交易类型(transtype)   String  2   必填
        this.request.setTransType(transType);
        //交易金额(amount)  String  12  必填  精确到分，不足左补0。比如1.23元应填写000000000123。
        this.request.setAmount("000000000001");
        /**交易附加域(transMemo)	String		VAR	是	"交易类型附加信息域，域格式为data1&data2&......&dataN
         例如：手机充值需要传入运营商和手机号，则填充：01&15866668888"*/
        this.request.setTransMemo("01");
        ZLogger.df("初始化交易请求: " + this.request.toString());
    }



    /**
     * 打印参数
     */
    private void print() {
//        String sb = "参数（点击可修改）：\n" +
//                String.format("主机IP：%s\n",
//                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_IP)) +
//                String.format("主机端口号：%s\n",
//                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_PORT)) +
//                String.format("商户号：%s\n",
//                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_MCHTID)) +
//                String.format("终端号：%s\n",
//                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_TERMID)) +
//                String.format("串口值：%s\n", SerialManager.getUmsipsPort()) +
//                String.format("波特率：%s\n", SerialManager.getUmsipsBaudrate());
//        tvUmsipsConfigs.setText(sb);
    }

}
