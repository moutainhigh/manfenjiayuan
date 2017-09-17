package net.sourceforge.simcpux;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.manfenjiayuan.mixicook_vip.wxapi.WXUtil;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.BitmapUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.TimeUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXFileObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXMusicObject;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

import net.sourceforge.simcpux.wxapi.Constants;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 微信帮助类
 * Created by bingshanguxue on 2015/6/12.
 */
public class WXHelper {
    private static final String TAG = WXHelper.class.getSimpleName();

    private static final String TRANSACTION_TYPE_TEXT = "text";
    private static final String TRANSACTION_TYPE_WEBPAGE = "webpage";

    private Context context;
    private static IWXAPI api;

    private static WXHelper instance;

    private WXHelper(Context context) {
        this.context = context;
        regToWx();
    }

    public static void initialize(Context context) {
        if (instance == null) {
            synchronized (WXHelper.class) {
                if (instance == null) {
                    instance = new WXHelper(context.getApplicationContext());
                }
            }
        }
    }

    public static WXHelper getInstance() {
        return instance;
    }

    private boolean wxRegisted = false;

    private synchronized void regToWx() {
        if (!wxRegisted) {
            //通过WXAPIFactory工厂获取 IWXAPI的实例。
            api = WXAPIFactory.createWXAPI(context, Constants.APP_ID, true);
            //将应用的APP_ID注册到微信
            wxRegisted = api.registerApp(Constants.APP_ID);
            if (!wxRegisted) {
                ZLogger.w("register app failed for wechat app signature check failed : " + Constants.APP_ID);
            } else {
                ZLogger.d("registerApp " + Constants.APP_ID + " success");
            }
        } else {
            ZLogger.w("already register to wx");
        }
    }

    /**
     * 统一下单
     * 除被扫支付场景以外，商户系统先调用该接口在微信支付服务后台生成预支付交易单，返回正确的预支付
     * 交易回话标识后再按扫码、JSAPI、APP等不同场景生成交易串调起支付。
     */
    public void getPrepayId() {
        GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
        getPrepayId.execute();
    }

    /**
     * App支付生成预支付订单
     */
    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {

        private ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(context, "提示", "正在获取支付订单");
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            if (dialog != null) {
                dialog.dismiss();
            }
//            sb.append("prepay_id\n"+result.get("prepay_id")+"\n\n");
//            show.setText(sb.toString());

            //获取支付订单成功，可以支付
            sendPayReq(result.get("prepay_id"));

//            Intent intent = new Intent(Constants.ACTION_WXPAY_PAYID);
//            if(result != null){
//                intent.putExtra(Constants.BROADCAST_KEY_PREPAY_ID, result.get("prepay_id"));
//            }
////            Bundle bundle = new Bundle();
////            bundle.putString(OwnerConstants.BROADCAST_KEY_PREPAY_ID, resultunifiedorder.get("prepay_id"));
////            intent.putExtra(bundle);
//            context.sendBroadcast(intent);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            //统一下单
//            String entity = genProductArgs(1, new String("微信充值测试"), Constants.NOTIFY_URL, WXUtil.genOutTradNo());
//            String entity = genProductArgs(1, "weixin pay test", Constants.NOTIFY_URL, WXUtil.genOutTradNo());
            String entity = genProductArgs(1, "weixin", "http://121.40.35.3/test", WXUtil.genOutTradNo());
            byte[] buf = WXUtil.httpPost(Constants.URL_UNIFIED_ORDER, entity);

//            <return_msg><![CDATA[mch_id和appid没有关联关系]]></return_msg>
//            <xml>
//            <return_code><![CDATA[FAIL]]></return_code>
//            <return_msg><![CDATA[签名错误]]></return_msg>
//            </xml>
//            <xml>
//            <return_code><![CDATA[SUCCESS]]></return_code>
//            <return_msg><![CDATA[OK]]></return_msg>
//            <appid><![CDATA[wx1dbac2f50c918d7d]]></appid>
//            <mch_id><![CDATA[1250378401]]></mch_id>
//            <nonce_str><![CDATA[DpSAW5rWOVm1EKVS]]></nonce_str>
//            <sign><![CDATA[E04F4E6EC871B3ED158CDCA20B14D745]]></sign>
//            <result_code><![CDATA[SUCCESS]]></result_code>
//            <prepay_id><![CDATA[wx201507031430519f74ca7f140120529140]]></prepay_id>
//            <trade_type><![CDATA[APP]]></trade_type>
//            </xml>
            String content = new String(buf);

            Map<String, String> xml = WXUtil.decodeXml(content);

            return xml;
        }
    }

    /**
     * 生成产品参数
     *
     * @param fee       订单总金额，只能为整数, 支付金额单位为【分】，参数值不能带小数
     * @param body      商品或支付单简要描述
     * @param notifyUrl 回掉地址
     * @param tradeNo   商户系统内部的订单号,32个字符内、可包含字母,
     *                  <p>
     *                  格式如下：
     *                  //            <xml>
     *                  //            <appid>wx1dbac2f50c918d7d</appid>
     *                  //            <body>weixin</body>
     *                  //            <mch_id>1250378401</mch_id>
     *                  //            <nonce_str>3546ab441e56fa333f8b44b610d95691</nonce_str>
     *                  //            <notify_url>http://121.40.35.3/test</notify_url>
     *                  //            <out_trade_no>73f9ddba165b5c59c61dd64960ba8b2d</out_trade_no>
     *                  //            <spbill_create_ip>127.0.0.1</spbill_create_ip>
     *                  //            <total_fee>1</total_fee>
     *                  //            <trade_type>APP</trade_type>
     *                  //            <sign>8D0D16631F8EF447AD6BAFA20053A426</sign>
     *                  //            </xml>
     */
    private String genProductArgs(int fee, String body, String notifyUrl, String tradeNo) {
        try {
            List<NameValuePair> packageParams = new LinkedList<>();
            packageParams.add(new BasicNameValuePair("appid", Constants.APP_ID));//微信分配的公众账号ID
            packageParams.add(new BasicNameValuePair("body", body));//商品或支付单简要描述
            packageParams.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));//微信支付分配的商户号
            packageParams.add(new BasicNameValuePair("nonce_str", WXUtil.genNonceStr()));//随机字符串，不长于32位
            packageParams.add(new BasicNameValuePair("notify_url", notifyUrl));//通知地址，接收微信支付异步通知回调地址
            packageParams.add(new BasicNameValuePair("out_trade_no", tradeNo));//商户系统内部的订单号,32个字符内、可包含字母
            packageParams.add(new BasicNameValuePair("spbill_create_ip", "127.0.0.1"));//终端IP,APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
            packageParams.add(new BasicNameValuePair("total_fee", String.valueOf(fee)));//订单总金额，只能为整数
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));//交易类型，取值如下：JSAPI，NATIVE，APP，WAP,详细说明见

            //签名验证工具 https://pay.weixin.qq.com/wiki/tools/signverify/
//            String sign = WXUtil.genSian(WXUtil.toString(packageParams));
            packageParams.add(new BasicNameValuePair("sign", WXUtil.genSign(packageParams)));//签名

            String xmlstring = WXUtil.toXml(packageParams);
            ZLogger.d("ProductArgs: " + xmlstring);
            return xmlstring;
        } catch (Exception e) {
            ZLogger.e("genProductArgs fail, ex = " + e.getMessage());
            return null;
        }
    }

    /**
     * 生成App支付参数
     */
//    public PayReq genPayReq(String prepayId) {
//        PayReq req = new PayReq();
//
//        req.appId = Constants.APP_ID;
//        req.partnerId = Constants.MCH_ID;
//        req.prepayId = prepayId;
//        req.packageValue = "Sign=WXPay";
//        req.nonceStr = WXUtil.genNonceStr();
//        req.timeStamp = String.valueOf(TimeUtil.genTimeStamp());
//
//        //按签名规范重新生成签名
//        List<NameValuePair> signParams = new LinkedList<>();
//        signParams.add(new BasicNameValuePair("appid", req.appId));
//        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
//        signParams.add(new BasicNameValuePair("package", req.packageValue));
//        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
//        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
//        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));//时间戳
//
////        req.sign = WXUtil.genSian(WXUtil.toString(signParams));
//        req.sign = WXUtil.genSign(signParams);
//
//        return req;
//    }


    /**
     * APP发送微信支付请求，需要重新签名
     */
    public void sendPayReq(String prepayId) {
        if (prepayId != null) {
            regToWx();
//            DialogUtil.showHint("发送支付请求");
//            api.registerApp(Constants.APP_ID);
//            api.sendReq(genPayReq(prepayId));
        } else {
            DialogUtil.showHint("prepayId 不能为空");
        }
    }

    /**
     * 发送授权登录信息，来获取code
     */
    public void sendAuthReq() {
        final SendAuth.Req req = new SendAuth.Req();
        //应用的作用域，获取个人信息
        req.scope = "snsapi_userinfo, snsapi_message";
        /**
         * 用于保持请求和回调的状态，授权请求后原样带回给第三方
         * 为了防止csrf攻击（跨站请求伪造攻击），后期改为随机数加session来校验
         */
        req.state = "mixiyun_vip";

        regToWx();
        if (!api.isWXAppInstalled()) {
            DialogUtil.showHint("请先安装微信");
        } else if (!api.isWXAppSupportAPI()) {
            DialogUtil.showHint("微信App不支持");
        } else {
            ZLogger.d("sendAuthReq");
            api.sendReq(req);
        }
    }

    /**
     * 分享·发送文本到微信
     *
     * @param text  content
     * @param scene SendMessageToWX.Req.WXSceneTimeline/SendMessageToWX.Req.WXSceneSession
     */
    public void sendTextToWX(String text, int scene) {
        // 初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "Will be ignored";
        msg.description = text;

        sendReq(TRANSACTION_TYPE_TEXT, msg, scene);

    }

    /**
     * 分享·发送网页到微信
     *
     * @param webpageUrl content
     * @param scene      SendMessageToWX.Req.WXSceneTimeline/SendMessageToWX.Req.WXSceneSession
     */
    public void sendWebpageToWX(String webpageUrl, String title, String description, Bitmap thumb, int scene) {
        // 初始化一个WXTextObject对象
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = webpageUrl;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;
        msg.thumbData = BitmapUtils.bmpToByteArray(thumb, true);

        sendReq(TRANSACTION_TYPE_WEBPAGE, msg, scene);

    }

//    private void sendReq(String type, ) {
//        regToWx();
//    }

    /**
     * 分享·发送网页到微信
     *
     * @param webpageUrl content
     * @param scene      SendMessageToWX.Req.WXSceneTimeline/SendMessageToWX.Req.WXSceneSession
     */
    public void sendWebpageToWX(final String webpageUrl, final String title,
                                final String description, final String imageUrl, final int scene) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                // 初始化一个WXTextObject对象
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = webpageUrl;

                // 用WXTextObject对象初始化一个WXMediaMessage对象
                WXMediaMessage msg = new WXMediaMessage(webpage);
                msg.title = title;
                msg.description = description;

//        try {
//            HttpClient client = new DefaultHttpClient();
//            HttpGet get = new HttpGet(imageUrl);
//            HttpResponse response = client.execute(get);
//            if(response.getStatusLine().getStatusCode() == 200) {
//                Bitmap bmp = BitmapFactory.decodeStream(response.getEntity().getContent());
//                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
//                bmp.recycle();
//
//                msg.setThumbImage(thumbBmp);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

                //android.os.NetworkOnMainThreadException
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(input);
                    if (bmp != null) {
                        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                        bmp.recycle();
                        msg.setThumbImage(thumbBmp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sendReq(TRANSACTION_TYPE_WEBPAGE, msg, scene);

            }
        });
        thread.start();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void openWXApp() {
        regToWx();
        api.openWXApp();
    }

    /**
     * * 发送文本到微信
     *
     * @param text  content
     * @param scene SendMessageToWX.Req.WXSceneTimeline/SendMessageToWX.Req.WXSceneSession
     */
    public void sendMusicToWX(String url, String title, String text, int scene) {
        // 初始化一个WXTextObject对象
        WXMusicObject musicObject = new WXMusicObject();
        musicObject.musicUrl = url;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = musicObject;
        // 发送文本类型的消息时，title字段不起作用
        msg.title = title;
        msg.description = text;

        sendReq(TRANSACTION_TYPE_TEXT, msg, scene);

    }

    /**
     * * 发送文本到微信
     *
     * @param text  content
     * @param scene SendMessageToWX.Req.WXSceneTimeline/SendMessageToWX.Req.WXSceneSession
     */
    public void sendAudioToWX(String fileName, String text, int scene) {
        // 初始化一个WXTextObject对象
        WXAppExtendObject appExtendObject = new WXAppExtendObject();
        appExtendObject.fileData = WXUtil.readFromFile(fileName, 0, -1);
        appExtendObject.extInfo = "this is extra infor";

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = appExtendObject;
        // 发送文本类型的消息时，title字段不起作用
        msg.title = "audio";
        msg.description = text;

        sendReq(TRANSACTION_TYPE_TEXT, msg, scene);
    }

    /**
     * * 发送文本到微信
     *
     * @param text  content
     * @param scene SendMessageToWX.Req.WXSceneTimeline/SendMessageToWX.Req.WXSceneSession
     */
    public void sendFileToWX(String fileName, String text, int scene) {
        // 初始化一个WXTextObject对象
        WXFileObject wxFileObject = new WXFileObject();
        wxFileObject.fileData = WXUtil.readFromFile(fileName, 0, -1);
        wxFileObject.filePath = fileName;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = wxFileObject;
        // 发送文本类型的消息时，title字段不起作用
        msg.title = "audio";
        msg.description = text;

        sendReq(TRANSACTION_TYPE_TEXT, msg, scene);
    }


    private void sendReq(String type, WXMediaMessage message, int scene) {
        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction(type);
        req.message = message;
        req.scene = scene;

        // 调用api接口发送数据到微信
        api.sendReq(req);
    }

//    public boolean registerMsgListener(long scene, long msgType, int msgState) {
//        String urlString = new StringBuilder(REGISTER_MSG_LISTENER)
//                .append("?appid=").append(WXConstants.APP_ID)
//                .append("&op=1&scene=").append(scene)
//                .append("&msgType=").append(msgType)
//                .append("&msgState=").append(msgState).toString();
//        Cursor cursor = mContext.getContentResolver()
//                .query(Uri.parse(urlString), null, null, null, null);
//        if (cursor != null) {
//            cursor.close();
//            ZLogger.d("registerMsgListener done");
//            return true;
//        } else {
//            ZLogger.d("registerMsgListener failed");
//            return false;
//        }
//    }
//
//    public boolean unregisterMsgListener() {
//        String urlString = new StringBuilder(REGISTER_MSG_LISTENER).append("?appid=")
//                .append(WXConstants.APP_ID).append("&op=2").toString();
//
//        Cursor cursor = mContext.getContentResolver()
//                .query(Uri.parse(urlString),
//                        null, null, null, null);
//        if (cursor != null) {
//            cursor.close();
//            ZLogger.d("unregisterMsgListener done");
//            return true;
//        } else {
//            ZLogger.d("unregisterMsgListener failed");
//            return false;
//        }
//    }
//    }


}
