package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mfh.litecashier.CashierApp;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.ExpressCompanyAdapter;
import com.mfh.litecashier.bean.HumanCompanyOption;
import com.mfh.litecashier.bean.ReceiveOrderCompanyWrapper;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.dialog.CommonDialog;

import java.util.List;


/**
 * 快递公司
 * 
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class ExpressCompanyDialog extends CommonDialog {

    public static final int DT_CREATE = 0;
    public static final int DT_SELECT = 1;

    public interface OnResponseCallback {
        void saveHumanFdCompany(HumanCompanyOption option);
        void onSelectCompany(String value);
    }

    private int dialogType = DT_CREATE;

    private View rootView;

    private TextView tvTitle;
    private ImageButton btnClose;
    private ListView listView;

    private OnResponseCallback mListener;

    private ExpressCompanyDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ExpressCompanyDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_express_company, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        listView = (ListView)rootView.findViewById(R.id.listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                if (mListener != null) {
                    if (dialogType == DT_CREATE){
                                            //创建
                        HumanCompanyOption option = (HumanCompanyOption)parent.getAdapter().getItem(position);
                        mListener.saveHumanFdCompany(option);
                    }else{
                        HumanCompanyOption option = (HumanCompanyOption)parent.getAdapter().getItem(position);
//
                        mListener.onSelectCompany(option.getValue());
                    }
                }
            }
        });
        tvTitle.setText("调单");
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public ExpressCompanyDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);


//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);
    }

    public void init(int dialogType, List<HumanCompanyOption> options, OnResponseCallback callback) {
        this.dialogType = dialogType;
        this.mListener = callback;

        try{
            listView.setAdapter(new ExpressCompanyAdapter(getContext(), options));

            if (dialogType == DT_CREATE){
                CashierApiImpl.comnQueryExpressCompany(queryRspCallback);
            }
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }

    }

    @Override
    public void show() {
        super.show();

        if (dialogType == DT_CREATE){
              tvTitle.setText("选择快递公司");
        }else{
            tvTitle.setText("选择快递公司");
        }
    }

    //查询所有快递公司
    private NetCallBack.NetTaskCallBack queryRspCallback = new NetCallBack.NetTaskCallBack<ReceiveOrderCompanyWrapper,
            NetProcessor.Processor<ReceiveOrderCompanyWrapper>>(
            new NetProcessor.Processor<ReceiveOrderCompanyWrapper>() {
                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData == null){
                        ZLogger.df("未查询到结果");
                        return;
                    }

                    try{
//                        {"code":"0","msg":"新增成功!","version":"1","data":{"val":"40513"}}
//                        java.lang.ClassCastException: java.lang.Integer cannot be cast to com.alibaba.fastjson.JSONObject
                        RspBean<ReceiveOrderCompanyWrapper> retValue = (RspBean<ReceiveOrderCompanyWrapper>) rspData;
                        ReceiveOrderCompanyWrapper wrapper = retValue.getValue();

                        if (wrapper != null){
                            listView.setAdapter(new ExpressCompanyAdapter(getContext(), wrapper.getOptions()));
                        }
                    }catch(Exception ex){
                        ZLogger.e("createBatchResponseCallback, " + ex.toString());
                    }
                }
            }
            , ReceiveOrderCompanyWrapper.class
            , CashierApp.getAppContext())
    {
    };

}
