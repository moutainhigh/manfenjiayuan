/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tsz.afinal.http;

import android.os.SystemClock;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.http.entityhandler.EntityCallBack;
import net.tsz.afinal.http.entityhandler.FileEntityHandler;
import net.tsz.afinal.http.entityhandler.StringEntityHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.UnknownHostException;


public class  HttpHandler  <T> extends AsyncTask<Object, Object, Object> implements EntityCallBack {

    public static final String PARAM_JSESSIONID = "JSESSIONID";//传给服务器的会话Id
    public static final String POST = "POST";
    public static final String GET = "GET";
    private static final int MAX_RETRY_TIMES = 0;//最大重试MAX_RETRY_TIMES次

    private final AbstractHttpClient client;
    private final HttpContext context;

    private final StringEntityHandler mStrEntityHandler = new StringEntityHandler();
    private final FileEntityHandler mFileEntityHandler = new FileEntityHandler();

    private final AjaxCallBack<T> callback;

    private int executionCount = 0;
    private String targetUrl = null; //下载的路径
    private boolean isResume = false; //是否断点续传
    private String charset;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());//add by zhangyz

    public HttpHandler(AbstractHttpClient client, HttpContext context, AjaxCallBack<T> callback,String charset) {
        this.client = client;
        this.context = context;
        this.callback = callback;
        this.charset = charset;
    }

    private void makeRequestWithRetries(HttpUriRequest request) throws IOException {
        if(isResume && targetUrl!= null){
            File downloadFile = new File(targetUrl);
            long fileLen = 0;
            if(downloadFile.isFile() && downloadFile.exists()){
                fileLen = downloadFile.length();
            }
            if(fileLen > 0)
                request.setHeader("RANGE", "bytes="+fileLen+"-");
        }

        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();//自动重试机制
        while (retry) {
            try {
                if(isCancelled()){
                    return;
                }

                //POST:无参数;GET:有参数
                String requestUrl = request.getURI().toString();
                String requestMethod = request.getMethod();
                ZLogger.d(String.format("<%d><%s> %s",
                        executionCount, requestMethod, requestUrl));

                for(Header header : request.getAllHeaders()){
                    ZLogger.d(String.format("<%s>:<%s>",
                            header.getName(), header.getValue()));
                }

                //重新组合url
                StringBuilder requestParams = new StringBuilder();
                if (requestMethod.equals(POST)){
                    HttpEntityEnclosingRequestBase requestBase = (HttpEntityEnclosingRequestBase)request;
                    if(requestBase != null){
                        HttpEntity entity = requestBase.getEntity();
                        if(entity != null){
                            InputStream is = entity.getContent();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            String line = null;
                            try {
                                while ((line = reader.readLine()) != null) {
//                            sb.append(line + "/n");
//                                Log.d("Nat: makeRequestWithRetries.request.ApiParams.line", line);
                                    requestParams.append(line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                ZLogger.ef(e.toString());
                            } finally {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
//                        byte[] paramsArray = EntityUtils.toByteArray(entity);
//                        String paramsStr = EntityUtils.toString(entity, "UTF-8");
//                        Log.d("Nat: makeRequestWithRetries.request.ApiParams(2)", paramsStr);
//                        Log.d("Nat: makeRequestWithRetries.request.ApiParams(3)", EntityUtils.toString(entity));
                    }

//                    Log.d("Nat: makeRequestWithRetries.request.ApiParams(RAW)", requestParams.toString());
                }

                //执行网络请求
                HttpResponse response = client.execute(request, context);
//                for(Header header : response.getAllHeaders()){
//                    Log.d("Nat", String.format(" makeRequestWithRetries.respoonse.header <%s><%s>", header.getName(), header.getValue()));
//                }

                if (isCancelled()){
                    ZLogger.d("Nat: makeRequestWithRetries", "request caneled.");
                    return;
                }

                //POST:无参数;GET:有参数
                request.getRequestLine();//GET
                ZLogger.d(request.getRequestLine().toString());
                 /*if (request.getMethod().equals("post")) {
                 HttpParams params = request.getParams();
                  params.setParameter(NetFactory.CLIENTSESSION, "");
                 request.snetParams(params);
                 }*/

                //会话超时后重新自动登录并重新执行原来请求(具体业务)
                //executionCount < 1,重试过一次就不再需要重试，测试。
                if (response.containsHeader("needLogin")/* && executionCount < 1*/) {
                    //不论url中是否有JSSIONID字段都需要先执行重登录去刷新cookie.
                    //自动重新登录(同步执行),sessionId
                    String newSid = MfhLoginService.get().doLogin();
                    if(newSid == null){
                        //TODO
                        //登录失败,登录页面
//                        Intent intent = new Intent(Constants.ACTION_REDIRECT_TO_LOGIN_H5);
//                        BizApplication.getAppContext().sendBroadcast(intent);
//                        break;
                    }
                    else{
                        String cookie = String.format("%s=%s", PARAM_JSESSIONID, newSid);
                        request.addHeader(FinalHttp.HEADER_SET_COOKIE, cookie);
                        request.addHeader(FinalHttp.HEADER_COOKIE, cookie);
                        request.addHeader(FinalHttp.HEADER_cookie, cookie);
                        // 注册消息
//                        MsgBridgeUtil.register();

                        if (requestMethod.equals(POST)){
//                        //修改Entity中的JSSIONID字段
                            String newParams = replaceParam(requestParams.toString(), PARAM_JSESSIONID, newSid);
//                            HttpEntity entity = new StringEntity(newParams);
                            HttpEntity entity = convertToAjaxParams(newParams).getEntity();
                            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
                        }
                        else if(requestMethod.equals(GET)){
                            //修改URL中的JSSIONID字段
                            String newRequestUrl = replaceParam(requestUrl, PARAM_JSESSIONID, newSid);
//                            newRequestUrl = replaceParam(newRequestUrl, "lastupdate", "0");
                            URI uri = new URI(newRequestUrl);
//                            Log.d("Nat: makeRequestWithRetries.autoLogin.URI", uri.toString());
//                                HttpEntityEnclosingRequestBase requestFact = (HttpEntityEnclosingRequestBase)request;
//                                requestFact.setURI(uri);
                            ((HttpEntityEnclosingRequestBase) request).setURI(uri);
                        }
                    }

                    //TODO,重新执行请求，
                    retry = (++executionCount <= MAX_RETRY_TIMES)
                            || retryHandler.retryRequest(new IOException("Exception"),
                            executionCount, context);
//                    ZLogger.d(String.format("%s 执行请求 %d", retry ? "需要" : "不需要", executionCount));
                    if (retry){
                        continue;
                    }
                }

                //执行正常流程
                handleResponse(response);
                return;
            } catch (UnknownHostException e) {
                ZLogger.e("UnknownHostException:" + e.toString());
                publishProgress(UPDATE_FAILURE, e, "unknownHostException：can't resolve host");
                return;
            } catch (IOException e) {
                ZLogger.e("IOException: " + e.toString());
                cause = e;
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
                publishProgress(UPDATE_FAILURE, e, "unknownHostException：can't resolve host");
            } catch (NullPointerException e) {
                if(e != null){
                    ZLogger.e("NullPointerException: " + e.toString());
                    // here's a bug in HttpClient 4.0.x that on some occasions causes
                    // DefaultRequestExecutor to throw an NPE, see
                    // http://code.google.com/p/android/issues/detail?id=5255
                    cause = new IOException("NPE in HttpClient: " + e.getMessage());
                    retry = retryHandler.retryRequest(cause, ++executionCount, context);
                }else{
                    ZLogger.e("NullPointerException: e is null");
                }
                publishProgress(UPDATE_FAILURE, e, "unknownHostException：can't resolve host");
            }catch (Exception e) {
                ZLogger.e("Exception: " + e.toString());
                cause = new IOException("Unhandled Exception" + e.getMessage());
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
                publishProgress(UPDATE_FAILURE, e, "unknownHostException：can't resolve host");
            }
        }

        // cleaned up to throw IOException
        if(cause != null){
            throw cause;
        }
//        else{
//            //TODO
//            throw new IOException("未知网络错误");
//        }
    }

    /**
     * 替换字符串指定字符串对应的值
     * */
    private String replaceParam(String rawData, String paramName, String newParamValue){
        if (rawData == null){
            return "";
        }

        int index = StringUtils.indexOfIgnoreCase(rawData, paramName);
        if (index > 0) {
            int endIndex = rawData.indexOf("&", index + 1);

            String newData;
            if (endIndex > 0)
                newData = rawData.substring(0, index) + paramName + "=" + newParamValue + rawData.substring(endIndex);
            else
                newData = rawData.substring(0, index) + paramName + "=" + newParamValue;
            return newData;
        }

        return rawData;
    }

    /**
     * 将URL参数转换为AjaxParams
     * @param rawData name1:value1&name2:value2&...
     * */
    private AjaxParams convertToAjaxParams(String rawData){
        AjaxParams ajaxParams = new AjaxParams();

        try{
            if(rawData != null){
                return ajaxParams;
            }

            String[] params = rawData.split("&");
            if (params != null && params.length > 0){
                String name, value;
                for (String param : params){
                    if (!param.contains("=")){
                        continue;
                    }

                    int index = StringUtils.indexOfIgnoreCase(param, "=");
                    name = param.substring(0, index);
                    value = param.substring(index + 1);
                    ajaxParams.put(name, value);
                }
            }
        }
        catch(Exception e){
            ZLogger.e("convertToAjaxParams:" + e.toString());
        }

        return ajaxParams;
    }

    @Override
    protected Object doInBackground(Object... params) {
        ZLogger.d("InBackground");

        if(params!=null && params.length == 3){
            targetUrl = String.valueOf(params[1]);
            isResume = (Boolean) params[2];
        }
        try {
            publishProgress(UPDATE_START); // 开始
            makeRequestWithRetries((HttpUriRequest)params[0]);
        } catch (IOException e) {
            publishProgress(UPDATE_FAILURE,e,e.getMessage()); // 结束
        }

        return null;
    }

    private final static int UPDATE_START = 1;
    private final static int UPDATE_LOADING = 2;
    private final static int UPDATE_FAILURE = 3;
    private final static int UPDATE_SUCCESS = 4;

    @SuppressWarnings("unchecked")
    @Override
    protected void onProgressUpdate(Object... values) {
        try{
            if (values.length > 1){
                ZLogger.d(String.format("%s/%s", String.valueOf(values[0]), String.valueOf(values[1])));
            }else if (values.length > 0){
                ZLogger.d(String.valueOf(values[0]));
            }
            int update = Integer.valueOf(String.valueOf(values[0]));

            switch (update) {
                case UPDATE_START:
                    if(callback!=null)
                        callback.onStart();
                    break;
                case UPDATE_LOADING:
                    if(callback!=null)
                        callback.onLoading(Long.valueOf(String.valueOf(values[1])),
                                Long.valueOf(String.valueOf(values[2])));
                    break;
                case UPDATE_FAILURE:
                    if(callback!=null)
                        callback.onFailure((Throwable)values[1],(String)values[2]);
                    break;
                case UPDATE_SUCCESS:
                    if(callback!=null)
                        callback.onSuccess((T)values[1]);
                    break;
                default:
                    break;
            }
            super.onProgressUpdate(values);
        }
        catch (Exception e){
            e.printStackTrace();
            ZLogger.e(e.toString());
        }

    }

    public boolean isStop() {
        return mFileEntityHandler.isStop();
    }


    /**
     * 停止下载任务
     */
    public void stop() {
        mFileEntityHandler.setStop(true);
    }

    private void handleResponse(HttpResponse response) {
        StatusLine status = response.getStatusLine();
        ZLogger.d(String.format("handleResponse:%d/%s", status.getStatusCode(), status.getReasonPhrase()));
        if (status.getStatusCode() >= 300) {
            String errorMsg = "response status error code: "+status.getStatusCode();
            if(status.getStatusCode() == 416 && isResume){
                errorMsg += " \n maybe you have download complete.";
            }
            ZLogger.d(errorMsg);
            publishProgress(UPDATE_FAILURE, new HttpResponseException(status.getStatusCode(),
                    status.getReasonPhrase()),errorMsg);
        } else {
            try {
                HttpEntity entity = response.getEntity();
                Object responseBody = null;
                if (entity != null) {
                    time = SystemClock.uptimeMillis();
                    if(targetUrl!=null){
                        responseBody = mFileEntityHandler.handleEntity(entity,this,targetUrl,isResume);
                    }
                    else{
                        responseBody = mStrEntityHandler.handleEntity(entity,this,charset);
                    }
                    ZLogger.d("handleResponse 成功");
                    publishProgress(UPDATE_SUCCESS,responseBody);
                }
                else{
                    ZLogger.d("response.getEntity() 返回空");
                    publishProgress(UPDATE_SUCCESS,responseBody);
                }

            } catch (IOException e) {
                ZLogger.e(e.toString());
                publishProgress(UPDATE_FAILURE,e,e.getMessage());
            }

        }
    }
    private long time;
    @Override
    public void callBack(long count, long current,boolean mustNoticeUI) {
        ZLogger.d(String.format("callBack: count=%d, current=%d, mustNoticeUI=%b",
                count, current, mustNoticeUI));
        if(callback!=null && callback.isProgress()){
            if(mustNoticeUI){
                ZLogger.d("noticeUi, loading...");
                publishProgress(UPDATE_LOADING,count,current);
            }else{
                long thisTime = SystemClock.uptimeMillis();
                if(thisTime - time >= callback.getRate()){
                    time = thisTime ;
                    ZLogger.d("noticeUi, loading...");
                    publishProgress(UPDATE_LOADING,count,current);
                }
            }
        }
    }


}
