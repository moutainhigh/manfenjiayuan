package com.mfh.litecashier.ui.fragment.settings;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.chinaums.mis.bank.BankDAO;
import com.chinaums.mis.bank.ICallBack;
import com.chinaums.mis.bean.RequestPojo;
import com.chinaums.mis.bean.ResponsePojo;
import com.chinaums.mis.bean.TransCfx;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.pay.umsips.TransType;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.ui.dialog.UmsipsDialog;
import com.mfh.litecashier.utils.CashierHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 设置－－账号
 * Created by kun on 15/8/31.
 */
public class SettingsPayFragment extends BaseFragment {

    @Bind(R.id.switchCompat_hybrid_payment)
    SwitchCompat scHybridPayment;
    @Bind(R.id.switchCompat_cash)
    SwitchCompat cashSwitchCompact;
    @Bind(R.id.switchCompat_alipay)
    SwitchCompat alipaySwitchCompact;
    @Bind(R.id.switchCompat_bankcard)
    SwitchCompat bankcardSwitchCompact;
    @Bind(R.id.tv_umsips_configs)
    TextView tvUmsipsConfigs;

    @Bind(R.id.switchCompat_mfcard)
    SwitchCompat mfcardSwitchCompact;
    @Bind(R.id.switchCompat_mfaccount)
    SwitchCompat mfaccountSwitchCompact;


    private BankDAO bankDAO;
    private boolean isRunningThread;
    private RequestPojo request;
    private ResponsePojo response;
    private TransCfx transCFX;
    private UmsipsDialog mUmsipsDialog = null;

    private class BackCall implements ICallBack {
        private BackCall() {
        }

        public void getCallBack(String stateCode, String stateTips) {
            ZLogger.d("stateCode=" + stateCode + "|" + "stateTips=" + stateTips);
//            DialogUtil.showHint(String.format("%s--%s", stateCode, stateTips));
        }
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_pay;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        scHybridPayment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesHelper.set(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, isChecked);
            }
        });
        cashSwitchCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                }
//                else{
//                }
            }
        });
        alipaySwitchCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                }
//                else{
//                }
            }
        });
        bankcardSwitchCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                }
//                else{
//                }
            }
        });
        mfcardSwitchCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                }
//                else{
//                }
            }
        });
        mfaccountSwitchCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                }
//                else{
//                }
            }
        });

        refresh();
    }

    @OnClick(R.id.tv_umsips_configs)
    public void setUmsipsPort(){
        if (mUmsipsDialog == null) {
            mUmsipsDialog = new UmsipsDialog(getActivity());
            mUmsipsDialog.setCancelable(false);
            mUmsipsDialog.setCanceledOnTouchOutside(false);
        }
        mUmsipsDialog.init(new UmsipsDialog.onDialogClickListener() {
            @Override
            public void onDatasetChanged() {
                StringBuilder sb = new StringBuilder();
                sb.append("参数（点击可修改）：\n");
                sb.append(String.format("主机IP：%s\n",
                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_IP)));
                sb.append(String.format("主机端口号：%s\n",
                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_PORT)));
                sb.append(String.format("商户号：%s\n",
                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_MCHTID)));
                sb.append(String.format("终端号：%s\n",
                        SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_TERMID)));
                sb.append(String.format("串口值：%s\n", SerialManager.getUmsipsPort()));
                sb.append(String.format("波特率：%s\n", SerialManager.getUmsipsBaudrate()));
                tvUmsipsConfigs.setText(sb.toString());
            }
        });
        if (!mUmsipsDialog.isShowing()) {
            mUmsipsDialog.show();
        }
    }

    private void refresh(){
        scHybridPayment.setChecked(SharedPreferencesHelper
                .getBoolean(SharedPreferencesHelper.PREF_KEY_HYBRID_PAYMENT_ENABLED, false));

        StringBuilder sb = new StringBuilder();
        sb.append("参数（点击可修改）：\n");
        sb.append(String.format("主机IP：%s\n",
                SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_IP)));
        sb.append(String.format("主机端口号：%s\n",
                SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_PORT)));
        sb.append(String.format("商户号：%s\n",
                SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_MCHTID)));
        sb.append(String.format("终端号：%s\n",
                SharedPreferencesHelper.getText(SharedPreferencesHelper.PK_UMSIPS_TERMID)));
        sb.append(String.format("串口值：%s\n", SerialManager.getUmsipsPort()));
        sb.append(String.format("波特率：%s\n", SerialManager.getUmsipsBaudrate()));
        tvUmsipsConfigs.setText(sb.toString());
    }

    /**
     * 签到
     */
    @OnClick(R.id.button_sign_pos)
    public void sign() {


        initBasicValue();
        ZLogger.d("正在签到，请稍候......");
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
                    if (response != null){
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
    public void initBasicValue() {
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
        if (mchtId.equalsIgnoreCase("898320554115217")){
            this.transCFX.setAuthSN("277797D4DE797832B650C201EE08DC5300C1771A372DCC6E08E57799A377CF91");
        }
        else if (mchtId.equalsIgnoreCase("898320554115269")){
            this.transCFX.setAuthSN("9907666AAB467BBF3A3067A9BE71FD7B54AC0A6D7B8AB996BB5C097417D453B6");
        }
        else{
            this.transCFX.setAuthSN("277797D4DE797832B650C201EE08DC5300C1771A372DCC6E08E57799A377CF91");
        }
        //串口值(devPath)	String		50	是	串口值
        this.transCFX.setDevPath(SerialManager.getUmsipsPort());
        //波特率(baudRate)	int		8	是	波特率
        this.transCFX.setBaudRate(Integer.valueOf(SerialManager.getUmsipsBaudrate()));
//        ZLogger.d("ums.transCFX: " + this.transCFX.toString());
    }

    /**
     * 初始化交易请求
     */
    public void initBankRequestValue(String transType) {
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
        ZLogger.d("ums.request: " + this.request.toString());
    }

}
