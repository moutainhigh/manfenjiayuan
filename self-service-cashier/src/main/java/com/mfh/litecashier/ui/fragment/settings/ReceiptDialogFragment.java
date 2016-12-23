package com.mfh.litecashier.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.hardware.printer.PrinterAgent;
import com.bingshanguxue.cashier.hardware.printer.PrinterContract;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.R;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 打印设置
 * Created by bingshanguxue on 22/12/2016.
 */

public class ReceiptDialogFragment extends DialogFragment {

    @BindView(R.id.item_cashier)
    SettingsItem cashierItem;
    @BindView(R.id.item_prepareorder)
    SettingsItem prepareOrderItem;
    @BindView(R.id.item_sendorder)
    SettingsItem sendOrderItem;
    @BindView(R.id.item_sendorder_3p)
    SettingsItem sendOrder3pItem;
    @BindView(R.id.item_alalysis)
    SettingsItem analysisItem;
    @BindView(R.id.item_stockout)
    SettingsItem stockOutItem;

    public ReceiptDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_recept, container);
        ButterKnife.bind(this, rootView);

//        getDialog().setTitle("Hello");
//        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        PrinterAgent.getInstance().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                refresh();
            }
        });

        refresh();

        return rootView;

//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.dialog_common);

    }

//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//    }
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//
//        // request a window without the title
//        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        return dialog;
//    }


    @OnClick(R.id.button_header_close)
    @Override
    public void dismiss() {
        super.dismiss();
    }

    @OnClick(R.id.item_cashier)
    public void setCashierItem() {
        show(PrinterContract.Receipt.CASHIER_ORDER);
    }

    @OnClick(R.id.item_prepareorder)
    public void setPrepareOrderItem() {
        show(PrinterContract.Receipt.PREPARE_ORDER);
    }

    @OnClick(R.id.item_sendorder)
    public void setSendOrderItem() {
        show(PrinterContract.Receipt.SEND_ORDER);
    }

    @OnClick(R.id.item_sendorder_3p)
    public void setSendOrder3pItem() {
        show(PrinterContract.Receipt.SEND_ORDER_3P);
    }

    @OnClick(R.id.item_alalysis)
    public void setAnalysisItem() {
        show(PrinterContract.Receipt.ANALYSIS);
    }

    @OnClick(R.id.item_stockout)
    public void setStockOutItem() {
        show(PrinterContract.Receipt.STOCKOUT);
    }

    private void refresh() {
        cashierItem.setSubTitle(String.format("%d 张",
                PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.CASHIER_ORDER)));
        prepareOrderItem.setSubTitle(String.format("%d 张",
                PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.PREPARE_ORDER)));
        sendOrderItem.setSubTitle(String.format("%d 张",
                PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.SEND_ORDER)));
        sendOrder3pItem.setSubTitle(String.format("%d 张",
                PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.SEND_ORDER_3P)));
        analysisItem.setSubTitle(String.format("%d 张",
                PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.ANALYSIS)));
        stockOutItem.setSubTitle(String.format("%d 张",
                PrinterAgent.getInstance().getPrinterTimes(PrinterContract.Receipt.STOCKOUT)));
    }

    private NumberInputDialog mInputDialog = null;

    private void show(final int receipt) {
        if (mInputDialog == null) {
            mInputDialog = new NumberInputDialog(getActivity());
            mInputDialog.setCancelable(false);
            mInputDialog.setCanceledOnTouchOutside(false);
        }

        mInputDialog.initializeDecimalNumber(EditInputType.NUMBER, "打印次数",
                "0", 0, "", new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        try {
                            int times = Integer.parseInt(value);
                            PrinterAgent.getInstance().setCashierPrinterTimes(receipt, times);
                        } catch (Exception e) {
                            ZLogger.ef(e.toString());
                        }
                    }

                    @Override
                    public void onNext(Double value) {
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onCompleted() {

                    }

                });

        if (!mInputDialog.isShowing()) {
            mInputDialog.show();
        }
    }
}
