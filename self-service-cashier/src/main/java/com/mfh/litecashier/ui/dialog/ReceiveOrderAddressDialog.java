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

import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.ReceiveOrderAddressAdapter;
import com.mfh.litecashier.bean.ReceiveOrderHumanInfo;
import com.mfh.framework.uikit.dialog.CommonDialog;

import java.util.List;


/**
 * 快递－－选择收货地址
 * 
 * @author NAT.ZZN
 * 
 */
public class ReceiveOrderAddressDialog extends CommonDialog {

    public interface OnResponseCallback {
        void onItemClick(ReceiveOrderHumanInfo entity);
    }

    private View rootView;

    private TextView tvTitle;
    private ImageButton btnClose;
    private ListView listView;

    private OnResponseCallback mListener;

    private ReceiveOrderAddressDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private ReceiveOrderAddressDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_hanguporder, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        listView = (ListView)rootView.findViewById(R.id.listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                if (mListener != null) {
                    ReceiveOrderHumanInfo entity = (ReceiveOrderHumanInfo)parent.getAdapter().getItem(position);
//
                    mListener.onItemClick(entity);
                }
            }
        });
        tvTitle.setText("选择收货地址");
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public ReceiveOrderAddressDialog(Context context) {
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

    public void init(List<ReceiveOrderHumanInfo> humanInfos, OnResponseCallback callback) {
        this.mListener = callback;
        listView.setAdapter(new ReceiveOrderAddressAdapter(getContext(), humanInfos));
    }
}
