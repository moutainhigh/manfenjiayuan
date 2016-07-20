package com.mfh.framework.hybrid;

import android.graphics.Bitmap;
import android.webkit.WebView;

/**
 * Created by Administrator on 2015/6/18.
 */
public interface WebViewDelegate {
    /**返回true则表示需要拦截*/
    boolean shouldOverrideUrlLoading(WebView view, String url);
    void onPageFinished(WebView view, String url);
    void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

    void onProgressChanged(WebView view, int newProgress);
    void onReceivedTitle(WebView view, String title);
    void onReceivedIcon(WebView view, Bitmap icon);
}
