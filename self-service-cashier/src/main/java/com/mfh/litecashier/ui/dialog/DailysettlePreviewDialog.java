package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PayWay;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 本地日结统计预览
 * TextView支持的HTML标签有：
 * {<br>,< p>,< div align=>,< strong>, <b>, <em>, <cite>, <dfn>, <i>, <big>, <small>,
 * <font size=>,  <font color=>, <blockquote>, <tt>, <a href=>, <u>, <sup>, <sub>,
 * <h1>,<h2>,<h3>,<h4>,<h5>,<h6>, <img src=>, <strike>}
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class DailysettlePreviewDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle;
    private Button btnSubmit;
    private ImageButton btnClose;
    private ImageButton fabSync;
    private ImageButton fabPrint;
    private WebView mWebview;

    private Date dailysettleDate;


    private DailysettlePreviewDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private DailysettlePreviewDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_order_printpreview, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        fabSync = (ImageButton) rootView.findViewById(R.id.fab_sync);
        fabPrint = (ImageButton) rootView.findViewById(R.id.fab_print);
        mWebview = (WebView) rootView.findViewById(R.id.webview);
        //设置编码
        mWebview.getSettings().setDefaultTextEncodingName("UTF -8");
//        //设置字库
//        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
//        Typeface tf = Typeface.createFromAsset(this.getAssets(),
//                "fonts/B.TTF");
//        tv.setTypeface(tf);


        tvTitle.setText("日结预览");

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
//                OrderSyncManager2.get().stepUploadPosOrder(mPosOrderEntity);
            }
        });
        fabPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PrintManager.printPosOrder(mPosOrderEntity, true);
            }
        });
        fabSync.setVisibility(View.INVISIBLE);
        fabPrint.setVisibility(View.INVISIBLE);
        setContent(rootView, 0);
    }

    public DailysettlePreviewDialog(Context context) {
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

    public void initialize(Date date) {
        this.dailysettleDate = date;
        refresh();
    }

    /**
     * 刷新会员信息
     */
    private void refresh() {
        LoadHtmlAsyncTask asyncTask = new LoadHtmlAsyncTask(dailysettleDate);
        asyncTask.execute();
    }

    /**
     * 推送商品
     */
    private class LoadHtmlAsyncTask extends AsyncTask<String, Void, String> {
        private Date dailysettleDate;

        public LoadHtmlAsyncTask(Date dailysettleDate) {
            this.dailysettleDate = dailysettleDate;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                StringBuilder sbHtml = new StringBuilder();
                sbHtml.append("<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "<meta charset=\"utf-8\">\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html\"; charset=\"UTF-8\">\n" +
                        "</head>\n" +
                        "<body>\n");

                //头部：订单编号
                sbHtml.append(String.format("<p><font color=#000000>" +
                                "<div align=\"center\">%s</div>\n" +
                                "<div align=\"left\">日结人：%s</div>\n" +
                                "<div align=\"left\">日结时间：%s </div>\n" +
                                "<div align=\"left\">设备编号：%s</div>\n" +
                                "</font></p>",
                        MfhLoginService.get().getCurOfficeName(),
                        MfhLoginService.get().getHumanName(),
                        TimeUtil.format(dailysettleDate,
                                TimeCursor.FORMAT_YYYYMMDDHHMMSS),
                        SharedPrefesManagerFactory.getTerminalId()));

                //明细：商品信息
                sbHtml.append("<p>--------------------------------</p>\n");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dailysettleDate);
                String endCursor = TimeCursor.InnerFormat.format(calendar.getTime());
//                calendar.add(Calendar.DATE, 0 - 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                String startCursor = TimeCursor.InnerFormat.format(calendar.getTime());

                String sqlOrder = String.format("sellerId = '%d' and bizType = '%d' " +
                                "and isActive = '%d' and status = '%d' " +
                                "and updatedDate >= '%s' and updatedDate <= '%s'",
                        MfhLoginService.get().getSpid(), BizType.POS,
                        PosOrderEntity.ACTIVE, PosOrderEntity.ORDER_STATUS_FINISH,
                        startCursor, endCursor);
                ZLogger.d(sqlOrder);
                List<PosOrderEntity> orderEntities = PosOrderService.get().queryAllBy(sqlOrder);
                if (orderEntities == null) {
                    orderEntities = new ArrayList<>();
                }
//                sbHtml.append(String.format("<p>" +
//                                "<div><font color=#000000>社区超市：%d</font></div>\n" +
//                                "</p>",
//                        orderEntities.size()));



                //流水分析
                Map<Integer, List<PosOrderEntity>> subTypeMap = new HashMap<>();
                OrderPayInfo orderPayInfo = new OrderPayInfo();
                for (PosOrderEntity entity : orderEntities) {
                    Integer subType = entity.getSubType();
                    List<PosOrderEntity> subTypeEntities = subTypeMap.get(subType);
                    if (subTypeEntities == null) {
                        subTypeEntities = new ArrayList<>();
                    }
                    subTypeEntities.add(entity);
                    subTypeMap.put(subType, subTypeEntities);

                    OrderPayInfo payInfo = OrderPayInfo.deSerialize(entity.getId());
                    if (payInfo == null) {
                        continue;
                    }

                    orderPayInfo.setPayType(orderPayInfo.getPayType() | payInfo.getPayType());
                    orderPayInfo.setPaidAmount(orderPayInfo.getPaidAmount() + payInfo.getPaidAmount());
                    orderPayInfo.setChange(orderPayInfo.getChange() + payInfo.getChange());
                    orderPayInfo.setRuleDiscount(orderPayInfo.getRuleDiscount() + payInfo.getRuleDiscount());
                    orderPayInfo.setCouponsIds(String.format("%s,%s",
                            orderPayInfo.getCouponsIds(), payInfo.getCouponsIds()));
                    orderPayInfo.setRuleIds(String.format("%s,%s",
                            orderPayInfo.getRuleIds(), payInfo.getRuleIds()));

                    List<PayWay> payWays = orderPayInfo.getPayWays();
                    if (payInfo.getPayWays() != null) {
                        payWays.addAll(payInfo.getPayWays());
                    }
                    orderPayInfo.setPayWays(payWays);

                    ZLogger.d(JSONObject.toJSONString(orderPayInfo));
                }
                sbHtml.append("<table border=\"0\">\n");
                sbHtml.append("<tr>\n" +
                        "  <th align=\"left\">业务类型</th>\n" +
                        "  <th>数量</th>\n" +
                        "  <th></th>\n" +
                        "</tr>\n");
                for (Integer subType : subTypeMap.keySet()) {
                    List<PosOrderEntity> subTypeEntities = subTypeMap.get(subType);
                    if (subTypeEntities == null) {
                        subTypeEntities = new ArrayList<>();
                    }

                    sbHtml.append(String.format("<tr>\n" +
                            "  <td>%s</td>\n" +
                            "  <td>%d</td>\n" +
//                            "  <td>暂未统计</td>\n" +
                            "</tr>\n",
                            PosType.name(subType),
                            subTypeEntities.size()));
                }
                sbHtml.append("</table>");


                //经营分析
                Double cash = 0D, ali = 0D, wx = 0D, account = 0D, bank = 0D, coupon = 0D;
                int cash2 = 0, ali2 = 0, wx2 = 0, account2 = 0, bank2 = 0, coupon2 = 0;
                List<PayWay> payWays = orderPayInfo.getPayWays();
                if (payWays != null && payWays.size() > 0) {
                    for (PayWay payWay : payWays) {
                        if (WayType.CASH.equals(payWay.getPayType())) {
                            cash += payWay.getAmount();
                            cash2 += 1;
                        } else if (WayType.ALI_F2F.equals(payWay.getPayType())) {
                            ali += payWay.getAmount();
                            ali2 += 1;
                        } else if (WayType.WX_F2F.equals(payWay.getPayType())) {
                            wx += payWay.getAmount();
                            wx2 += 1;
                        } else if (WayType.VIP.equals(payWay.getPayType())) {
                            account += payWay.getAmount();
                            account2 += 1;
                        } else if (WayType.BANKCARD.equals(payWay.getPayType())) {
                            bank += payWay.getAmount();
                            bank2 += 1;
                        } else if (WayType.RULES.equals(payWay.getPayType())) {
                            coupon += payWay.getAmount();
                            coupon2 += 1;
                        }
                    }
                }
                sbHtml.append("<p>--------------------------------</p>\n");
                sbHtml.append("<table border=\"0\">\n");
                sbHtml.append("<tr>\n" +
                        "  <th align=\"left\">支付类型</th>\n" +
                        "  <th>数量</th>\n" +
                        "  <th>金额</th>\n" +
                        "</tr>\n");
                sbHtml.append(String.format("<tr>\n" +
                        "  <td>现金</td>\n" +
                        "  <td>%d</td>\n" +
                        "  <td>%.2f</td>\n" +
                        "</tr>\n", cash2, cash));
                sbHtml.append(String.format("<tr>\n" +
                        "  <td>支付宝</td>\n" +
                        "  <td>%d</td>\n" +
                        "  <td>%.2f</td>\n" +
                        "</tr>\n", ali2, ali));
                sbHtml.append(String.format("<tr>\n" +
                        "  <td>微信</td>\n" +
                        "  <td>%d</td>\n" +
                        "  <td>%.2f</td>\n" +
                        "</tr>\n", wx2, wx));
                sbHtml.append(String.format("<tr>\n" +
                        "  <td>平台账户</td>\n" +
                        "  <td>%d</td>\n" +
                        "  <td>%.2f</td>\n" +
                        "</tr>\n", account2, account));

                sbHtml.append(String.format("<tr>\n" +
                        "  <td>银行卡</td>\n" +
                        "  <td>%d</td>\n" +
                        "  <td>%.2f</td>\n" +
                        "</tr>\n", bank2, bank));
                sbHtml.append(String.format("<tr>\n" +
                        "  <td>卡券</td>\n" +
                        "  <td>%d</td>\n" +
                        "  <td>%.2f</td>\n" +
                        "</tr>\n", coupon2, coupon));
                sbHtml.append("</table>");

//
//                //尾部:订单支付信息
//                Double payableAmount = mPosOrderEntity.getFinalAmount() - payWrapper.getRuleDiscount();
//                if (payableAmount < 0.01){
//                    payableAmount = 0D;
//                }
//                sbHtml.append(String.format("<p>" +
//                                "--------------------------------\n" +
//                                "<div><font color=#000000>合计：%.2f</font></div>\n" +
//                                "<div><font color=#000000>会员/卡券/促销优惠：%.2f</font></div>\n" +
//                                "<div><font color=#000000>应收：%.2f</font></div>\n" +
//                                "<div><font color=#000000>付款：%.2f</font></div>\n" +
//                                "<div><font color=#000000>找零：%.2f</font></div>\n" +
//                                "<div><font color=#000000>谢谢惠顾</font></div>\n" +
//                                "<div><font color=#000000>欢迎下次光临</font></div>\n" +
//                                "</p>",
//                        mPosOrderEntity.getFinalAmount(),
//                        payWrapper.getRuleDiscount(),
//                        payableAmount,
//                        mPosOrderEntity.getPaidAmount() - payWrapper.getRuleDiscount(),
//                        payWrapper.getChange()));

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
