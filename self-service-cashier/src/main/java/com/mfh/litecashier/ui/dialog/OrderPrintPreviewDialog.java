package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PayWay;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.PrintManager;
import com.mfh.litecashier.service.UploadSyncManager;

import java.util.List;


/**
 * 订单打印预览
 * TextView支持的HTML标签有：
 * {<br>,< p>,< div align=>,< strong>, <b>, <em>, <cite>, <dfn>, <i>, <big>, <small>,
 * <font size=>,  <font color=>, <blockquote>, <tt>, <a href=>, <u>, <sup>, <sub>,
 * <h1>,<h2>,<h3>,<h4>,<h5>,<h6>, <img src=>, <strike>}
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class OrderPrintPreviewDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private Button btnSubmit;
    private ImageButton btnClose;
    private FloatingActionButton fabSync;
    private FloatingActionButton fabPrint;
    private WebView mWebview;

    private PosOrderEntity mPosOrderEntity;


    private OrderPrintPreviewDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private OrderPrintPreviewDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_order_printpreview, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        fabSync = (FloatingActionButton) rootView.findViewById(R.id.fab_sync);
        fabPrint = (FloatingActionButton) rootView.findViewById(R.id.fab_print);
        mWebview = (WebView) rootView.findViewById(R.id.webview);
        //设置编码
        mWebview.getSettings().setDefaultTextEncodingName("UTF -8");
//        //设置字库
//        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
//        Typeface tf = Typeface.createFromAsset(this.getAssets(),
//                "fonts/B.TTF");
//        tv.setTypeface(tf);


        tvTitle.setText("打印预览");

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                submit();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        fabSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadSyncManager.getInstance().stepUploadPosOrder(mPosOrderEntity);
            }
        });
        fabPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintManager.printPosOrder(mPosOrderEntity, true);
            }
        });
        setContent(rootView, 0);
    }

    public OrderPrintPreviewDialog(Context context) {
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

    public void initialize(PosOrderEntity orderEntity) {
        this.mPosOrderEntity = orderEntity;
        refresh();
    }

    /**
     * 刷新会员信息
     */
    private void refresh() {
        LoadHtmlAsyncTask asyncTask = new LoadHtmlAsyncTask(mPosOrderEntity);
        asyncTask.execute();
        if (mPosOrderEntity.getStatus() == PosOrderEntity.ORDER_STATUS_FINISH) {
            fabSync.setVisibility(View.VISIBLE);
        } else {
            fabSync.setVisibility(View.GONE);
        }
    }

    /**
     * 推送商品
     */
    private class LoadHtmlAsyncTask extends AsyncTask<String, Void, String> {
        private PosOrderEntity mPosOrderEntity;

        public LoadHtmlAsyncTask(PosOrderEntity posOrderEntity) {
            mPosOrderEntity = posOrderEntity;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                ZLogger.d(JSONObject.toJSONString(mPosOrderEntity));
                StringBuilder sbHtml = new StringBuilder();
                sbHtml.append("<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "<meta charset=\"utf-8\">\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html\"; charset=\"UTF-8\">\n" +
                        "</head>\n" +
                        "<body>\n");

                if (mPosOrderEntity != null) {
                    //头部：订单编号
                    sbHtml.append(String.format("<p><font color=#000000>" +
                                    "<div align=\"center\">%s</div>\n" +
                                    "<div align=\"left\">%s No. %s</div>\n" +
                                    "<div align=\"left\">%s</div>\n" +
                                    "</font></p>",
                            MfhLoginService.get().getCurOfficeName(),
                            SharedPreferencesManager.getTerminalId(), mPosOrderEntity.getBarCode(),
                            TimeUtil.format(mPosOrderEntity.getUpdatedDate(),
                                    TimeCursor.FORMAT_YYYYMMDDHHMMSS)));

                    //明细：商品信息
                    sbHtml.append("<p>--------------------------------</p>\n");
                    List<PosOrderItemEntity> posOrderItemEntityList = CashierAgent
                            .fetchOrderItems(mPosOrderEntity);
                    if (posOrderItemEntityList != null && posOrderItemEntityList.size() > 0) {
                        sbHtml.append("<table border=\"0\">\n");
                        sbHtml.append("<tr>\n" +
                                "  <th align=\"left\">货号/品名</th>\n" +
                                "  <th>单价</th>\n" +
                                "  <th>数量</th>\n" +
                                "  <th>小计</th>\n" +
                                "</tr>\n");

                        for (PosOrderItemEntity entity : posOrderItemEntityList) {
                            sbHtml.append(String.format("<tr>\n" +
                                            "  <td>%s\n%s</td>\n" +
                                            "  <td>%.2f</td>\n" +
                                            "  <td>%.2f</td>\n" +
                                            "  <td>%.2f</td>\n" +
                                            "</tr>\n", entity.getBarcode(), entity.getName(),
                                    entity.getFinalPrice(), entity.getBcount(), entity.getFinalAmount()));
                        }
                        sbHtml.append("</table>");
                    }

                    //支付记录
                    OrderPayInfo payWrapper = OrderPayInfo.deSerialize(mPosOrderEntity.getId());

                    //尾部:订单支付信息
                    Double payableAmount = mPosOrderEntity.getFinalAmount() - payWrapper.getRuleDiscount();
                    if (payableAmount < 0.01) {
                        payableAmount = 0D;
                    }

                    sbHtml.append(String.format("<p>" +
                                    "--------------------------------\n" +
                                    "<div><font color=#000000>合计：%.2f</font></div>\n" +
                                    "<div><font color=#000000>会员/卡券/促销优惠：%.2f</font></div>\n" +
                                    "<div><font color=#000000>应收：%.2f</font></div>\n" +
                                    "<div><font color=#000000>付款：%.2f</font></div>\n" +
                                    "<div><font color=#000000>找零：%.2f</font></div>\n" +
                                    "</p>",
                            mPosOrderEntity.getFinalAmount(),
                            payWrapper.getRuleDiscount(),
                            payableAmount,
                            mPosOrderEntity.getPaidAmount()
                                    - payWrapper.getRuleDiscount()
                                    + payWrapper.getChange(),
                            payWrapper.getChange()));

                    sbHtml.append("--------------------------------\n");
                    sbHtml.append(String.format(
                                    "<div><font color=#000000>合计：%.2f</font></div>\n",
                            mPosOrderEntity.getFinalAmount()));
                    sbHtml.append(String.format("<div><font color=#000000>会员/卡券/促销优惠：%.2f</font></div>\n",
                            payWrapper.getRuleDiscount()));
                    sbHtml.append(String.format("<div><font color=#000000>应收：%.2f</font></div>\n",
                            payableAmount));
                    List<PayWay> payWays = payWrapper.getPayWays();
                    if (payWays != null && payWays.size() > 0){
                        for (PayWay payWay : payWays){
                            sbHtml.append(String.format("<div><font color=#000000>%s：%.2f</font></div>\n" ,
                                    WayType.name(payWay.getPayType()), payWay.getAmount()));
                        }
                    }
//                    sbHtml.append(String.format("<div><font color=#000000>付款：%.2f</font></div>\n" ,
//                            mPosOrderEntity.getPaidAmount()
//                                    - payWrapper.getRuleDiscount()
//                                    + payWrapper.getChange()));
                    sbHtml.append(String.format("<div><font color=#000000>找零：%.2f</font></div>\n",
                            payWrapper.getChange()));
                    sbHtml.append("\n\n<div align=\"center\"><font color=#000000>谢谢惠顾</font></div>\n" +
                                    "<div align=\"center\"><font color=#000000>欢迎下次光临</font></div>\n" +
                                    "</p>");
                }

                sbHtml.append("</body>\n" +
                        "</html>");
                ZLogger.d(sbHtml.toString());
                return sbHtml.toString();
            } catch (Exception e) {
                ZLogger.e("generate html failed, " + e.toString());
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            try {
                if (StringUtils.isEmpty(s)) {
                    return;
                }

//            mWebview.loadData(sbHtml.toString(), "text/html", "UTF-8");//API提供的标准用法，无法解决乱码问题
                mWebview.loadDataWithBaseURL(null, s, "text/html", "UTF-8", null);//解决乱码问题
//                getOwnerActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // Code for WebView goes here
//
//                    }
//                });

            } catch (Exception ex) {
                ZLogger.e(ex.toString());
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }


}
