package com.mfh.enjoycity.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.ReceiveAddressAdapter;
import com.mfh.enjoycity.adapter.AnonymousAddressDialogAdapter;
import com.mfh.enjoycity.database.ReceiveAddressEntity;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.database.AnonymousAddressEntity;
import com.mfh.enjoycity.database.AnonymousAddressService;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.uikit.dialog.CommonDialog;

import java.util.List;


/**
 * 选择地址
 * 
 * @author NAT.ZZN(bingshanguxue)
 * 
 */
public class SelectAddressDialog extends CommonDialog {

    public interface OnResponseCallback {
        void onRetryLocation();
        void onSearch();
        void onAdd();
        void onSelectAddress();
    }

    private OnResponseCallback mListener;
    public enum DialogType{
        LOCATION_ANONYMOUS,
        LOCATION_LOGIN
    }
    private DialogType dialogType = DialogType.LOCATION_ANONYMOUS;
    private View rootView;
    private View gpsFailedView;
    private ListView addressListView;
    private Button btnSearch, btnAdd;

    private static final int[] ATTRS = new int[]{
            android.R.attr.actionBarSize
    };

    private SelectAddressDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectAddressDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialog_select_address, null);
//        ButterKnife.bind(rootView);

        rootView.findViewById(R.id.view_gps_failed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mListener != null) {
                    mListener.onRetryLocation();
                }
            }
        });
        rootView.findViewById(R.id.ll_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                if (mListener != null) {
                    if (dialogType == DialogType.LOCATION_LOGIN) {
                        mListener.onAdd();
                    } else {
                        mListener.onSearch();
                    }
                }
            }
        });
        gpsFailedView = rootView.findViewById(R.id.view_gps_failed);
        addressListView = (ListView)rootView.findViewById(R.id.listview);
        addressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                if (mListener != null) {
                    if (dialogType == DialogType.LOCATION_LOGIN){
                        String addrId = ((ReceiveAddressEntity) parent.getAdapter().getItem(position)).getId();

                        ShopcartHelper.getInstance().refreshMemberOrderAddr(addrId);
                    }
                    else{
                        String addrId = ((AnonymousAddressEntity) parent.getAdapter().getItem(position)).getId();

                        ShopcartHelper.getInstance().refreshAnonymousOrderAddr(addrId);
                    }

                    mListener.onSelectAddress();
                }
            }
        });
        btnSearch = (Button) rootView.findViewById(R.id.button_search);
        btnAdd = (Button) rootView.findViewById(R.id.button_add);

        setContent(rootView, 0);
    }

    public SelectAddressDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.TOP);


        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth() * 5 / 6;
//        p.y = DensityUtil.dip2px(getContext(), 44);

        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
        p.y = (int)a.getDimension(0, 44);
        a.recycle();

        getWindow().setAttributes(p);
    }

    public void init(DialogType type, OnResponseCallback callback) {
        this.dialogType = type;
        this.mListener = callback;

        if(type == DialogType.LOCATION_LOGIN){
            gpsFailedView.setVisibility(View.GONE);
            addressListView.setVisibility(View.VISIBLE);
            btnSearch.setVisibility(View.GONE);
            btnAdd.setVisibility(View.VISIBLE);

            ReceiveAddressService dbService = ReceiveAddressService.get();
            List<ReceiveAddressEntity> entityList =  dbService.queryAll(new PageInfo(1, 100));
            addressListView.setAdapter(new ReceiveAddressAdapter(getContext(), entityList));
        }
        else{
            btnSearch.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.GONE);

            AnonymousAddressService dbService = ServiceFactory.getService(AnonymousAddressService.class.getName());
            List<AnonymousAddressEntity> entityList = dbService.queryAll(new PageInfo(1, 100));
            if(entityList != null && entityList.size() > 0){
                gpsFailedView.setVisibility(View.GONE);
                addressListView.setVisibility(View.VISIBLE);

                addressListView.setAdapter(new AnonymousAddressDialogAdapter(getContext(), entityList));
            }
            else{
                gpsFailedView.setVisibility(View.VISIBLE);
                addressListView.setVisibility(View.GONE);
            }
        }
    }
}
