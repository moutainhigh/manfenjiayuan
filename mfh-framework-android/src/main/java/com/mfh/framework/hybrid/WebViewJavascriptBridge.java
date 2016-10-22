package com.mfh.framework.hybrid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * Created by Administrator on 2015/5/20.
 * https://github.com/wheam/WebViewJavascriptBridge
 *
 * 每个WebViewJavascriptBridge必须对应一个webview
 * 因为安全问题，在Android4.2中(如果应用的android:targetSdkVersion数值为17+)JS只能访问带有 @JavascriptInterface注解的Java函数。
 */
public class WebViewJavascriptBridge implements Serializable {
    public static final String GLOBAL_INTERFACE_NAME = "_WebViewJavascriptBridge";//接口名，Native要和H5保持一致
    public static final String JAVASCRIPT = "javascript:";
    public static final String CALL_BACK_ID_PREFIX = "java_cb_";
    public static final String KEY_DATA = "data";
    public static final String KEY_CALLBACK_ID = "callbackId";
    public static final String KEY_RESPONSE_ID = "responseId";
    public static final String KEY_RESPONSE_DATA = "responseData";
    public static final String KEY_HANDLER_NAME = "handlerName";


    Activity mContext;
    private WebView webView;
    private WVJBHandler messageHandler;
    private Map<String, WVJBHandler> messageHandlers = new HashMap<>();
    private Map<String, WVJBResponseCallback> responseCallbacks = new HashMap<>();
    private long uniqueId;

    private WebViewDelegate delegate;

    public WebViewJavascriptBridge(Activity context, WebView webView, WVJBHandler messageHandler,
                                   WebViewDelegate delegate) {
        this.mContext = context;
        this.webView = webView;
        this.messageHandler = messageHandler;

        messageHandlers=new HashMap<>();
        responseCallbacks = new HashMap<>();
        uniqueId = 0;

        this.delegate = delegate;

        initWebSettings();

        this.webView.addJavascriptInterface(this, GLOBAL_INTERFACE_NAME);
        this.webView.setWebViewClient(new MyWebViewClient());
        this.webView.setWebChromeClient(new MyWebChromeClient());     //optional, for show console and alert
    }

    public WebViewJavascriptBridge(Activity context, WebView webView, WVJBHandler messageHandler) {
        this(context, webView, messageHandler, null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebSettings(){
        WebSettings webSettings = this.webView.getSettings();

        webSettings.setUserAgentString(MfhApplication.getUserAgent());
        webSettings.setJavaScriptEnabled(true);
        if(NetworkUtils.isConnect(mContext)){
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }else{
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        webSettings.setAllowFileAccess(true);

        webSettings.setAppCacheEnabled(true);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2){
            webSettings.setAppCacheMaxSize(1024 * 8);
        }

        //Set whether the DOM storage API is enabled.
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);//enable database storage
        webSettings.setPluginState(WebSettings.PluginState.ON);

        // these settings speed up page load into the webview
//        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
//        webView.requestFocus(View.FOCUS_DOWN);
    }
    /**
     * 向webview中注入js文件
     * */
    private void loadWebViewJavascriptBridgeJs(WebView webView) {
        InputStream is = mContext.getResources().openRawResource(R.raw.webviewjavascriptbridge);
//            AssetManager assets = webView.getContext().getAssets();
//            InputStream is = assets.open("webviewjavascriptbridge.js");

        String script=convertStreamToString(is);
        String javascript = JAVASCRIPT + script;

        //解决在API>=19设备上不起作用问题。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            // run JavaScript asynchronously
            webView.evaluateJavascript(javascript, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    ZLogger.d("onReceiveValue:" + value);
                }
            });
        }else{
            webView.loadUrl(javascript);
        }
    }

    public static String convertStreamToString(InputStream is) {
        String s="";
        try{
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext()) s= scanner.next();
            is.close();
        } catch (IOException e) {
            ZLogger.e(e.toString());
            e.printStackTrace();
        }
        return s;
    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            ZLogger.d(String.format("网页加载开始：%s", url));
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            ZLogger.d(String.format("网页加载结束：%s", url));
            loadWebViewJavascriptBridgeJs(view);
            if (delegate != null){
                delegate.onPageFinished(view, url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLogger.d(String.format("url=%s\ncookie=",
                    url, CookieManager.getInstance().getCookie(url)));

            if (delegate != null){
                boolean innerFlag = delegate.shouldOverrideUrlLoading(view, url);
                if (innerFlag){
                    return true;//需要拦截
                }
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                boolean flag =  super.shouldOverrideUrlLoading(view, url);
                return flag;
            }
            else{
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            ZLogger.d(String.format("errorCode=%d, description=%s, failingUrl=%s",
                    errorCode, description, failingUrl));
            if (delegate != null){
                delegate.onReceivedError(view, errorCode, description, failingUrl);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            super.onReceivedSslError(view, handler, error);
            ZLogger.d(error.getUrl());
            //handler.cancel(); 默认的处理方式，WebView变成空白页
//                        //接受证书
            handler.proceed();
            //handleMessage(Message msg); 其他处理
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            //Uncaught SyntaxError: Unexpected token var line:1
            if(cm != null){
                DialogUtil.showHint(cm.message());
                ZLogger.d(cm.message() + "-- From line:"+ cm.lineNumber()
                        + " of " + cm.sourceId());
            }else{
                ZLogger.d("null");
            }
            return true;
        }


        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            // if don't cancel the alert, webview after onJsAlert not responding taps
            // you can check this :
            // http://stackoverflow.com/questions/15892644/android-webview-after-onjsalert-not-responding-taps
            result.cancel();
            ZLogger.d(String.format("url=%s, message=%s"));
            DialogUtil.showHint(message);
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if(delegate != null){
                delegate.onProgressChanged(view, newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if(delegate != null){
                delegate.onReceivedTitle(view, title);
            }
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            if(delegate != null){
                delegate.onReceivedIcon(view, icon);
            }
        }
    }

    public interface WVJBHandler{
        void handle(String data, WVJBResponseCallback responseCallback);
    }
    public interface WVJBResponseCallback{
        void callback(String data);
    }

    /**
     * 将方法注册进js桥
     * @param handlerName
     * @param handler
     */
    public void registerHandler(String handlerName, WVJBHandler handler) {
        messageHandlers.put(handlerName, handler);
    }

    private class CallbackJs implements WVJBResponseCallback{
        private final String callbackIdJs;

        public  CallbackJs(String callbackIdJs){
            this.callbackIdJs=callbackIdJs;
        }
        @Override
        public void callback(String data) {
            _callbackJs(callbackIdJs,data);
        }
    }

    private void _callbackJs(String callbackIdJs,String data) {
        //TODO: CALL js to call back;
        Map<String,String> message=new HashMap<String,String>();
        message.put(KEY_RESPONSE_ID,callbackIdJs);
        message.put(KEY_RESPONSE_DATA,data);
        dispatchMessage(message);
    }

    @JavascriptInterface
    public void _handleMessageFromJs(String data,String responseId,
                                     String responseData,String callbackId,String handlerName){
        if (null != responseId) {
            ZLogger.d("_handleMessageFromJs.responseId=" + responseId);
            WVJBResponseCallback responseCallback = responseCallbacks.get(responseId);
            responseCallback.callback(responseData);
            responseCallbacks.remove(responseId);
        } else {
            WVJBResponseCallback responseCallback = null;
            if (null!=callbackId) {
                ZLogger.d("_handleMessageFromJs.callbackId=" + callbackId);
                responseCallback=new CallbackJs(callbackId);
            }
            WVJBHandler handler;
            if (null!=handlerName) {
                ZLogger.d("_handleMessageFromJs.handlerName=" + handlerName);
                handler = messageHandlers.get(handlerName);
                if (null==handler) {
                    ZLogger.e("WVJB Warning: No handler for " + handlerName);
                    return ;
                }
            } else {
                handler = messageHandler;
            }
            try {
                if(data != null) {
                    ZLogger.d("_handleMessageFromJs.data=" + data);
                }
                final WVJBHandler handler1 = handler;
                final String data1 = data;
                final WVJBResponseCallback responseCallback1 = responseCallback;
                mContext.runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        handler1.handle(data1, responseCallback1);
                    }
                });
            }catch (Exception exception) {
                ZLogger.e("WARNING: java handler threw. " + exception.getMessage());
            }
        }
    }


    /**
     * Nativie 和 Js 端发送消息
     * */
    public void send(String data) {
        send(data, null);
    }
    /**
     * Nativie 和 Js 端发送消息
     * */
    public void send(String data, WVJBResponseCallback responseCallback) {
        send(data, responseCallback, null);
    }

    /**
     * 向js端发送消息
     * @param data
     * @param responseCallback
     * @param handlerName
     */
    private void send(String data, WVJBResponseCallback responseCallback, String handlerName) {
        Map <String, String> message = new HashMap<>();

        if (null != data) {
            message.put(KEY_DATA, data);
        }

        if (null != responseCallback) {
            String callbackId = CALL_BACK_ID_PREFIX + (++uniqueId);
            responseCallbacks.put(callbackId,responseCallback);
            message.put(KEY_CALLBACK_ID, callbackId);
        }

        if (null != handlerName) {
            message.put(KEY_HANDLER_NAME, handlerName);
        }

        dispatchMessage(message);
    }

    /**
     * 分发消息， 将消息传递给js
     * @param message
     */
    private void dispatchMessage(Map <String, String> message) {
        //String messageJSON = JSONObject.toJSONString(message);
		String messageJSON = new JSONObject(message).toString();
        ZLogger.d(String.format("WVJB SEND: (%d) %s", messageJSON.length(), messageJSON));

        //javascript:表示Nativie调用JS方法
        final String javascriptCommand = String.format("%sWebViewJavascriptBridge._handleMessageFromJava('%s');"
                , JAVASCRIPT, doubleEscapeString(messageJSON));
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ZLogger.d("javascriptCommand: " + javascriptCommand);
                webView.loadUrl(javascriptCommand);
            }
        });
    }


    /**
     * 调用js端的方法
     * @param handlerName js端注册的方法名
     */
    public void callHandler(String handlerName) {
        callHandler(handlerName, null, null);
    }

    /**
     * 调用js中的方法
     * @param handlerName
     * @param data 发送的数据
     */
    public void callHandler(String handlerName, String data) {
        callHandler(handlerName, data, null);
    }

    /**
     * 调用js中的方法
     * @param handlerName 方法名
     * @param data 发送给js端de数据
     * @param responseCallBack 调用js完成回调的接口
     */
    public void callHandler(String handlerName, String data,
                            WVJBResponseCallback responseCallBack) {
        send(data, responseCallBack, handlerName);
    }
    /*
          * you must escape the char \ and  char ", or you will not recevie a correct json object in
          * your javascript which will cause a exception in chrome.
          *
          * please check this and you will know why.
          * http://stackoverflow.com/questions/5569794/escape-nsstring-for-javascript-input
          * http://www.json.org/
        */
    private String doubleEscapeString(String javascript) {
        String result;
        result = javascript.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        result = result.replace("\'", "\\\'");
        result = result.replace("\n", "\\n");
        result = result.replace("\r", "\\r");
        result = result.replace("\f", "\\f");
        return result;
    }


}
