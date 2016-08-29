package com.manfenjiayuan.mixicook_vip.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.SimpleActivity;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.hybrid.HybridWebView;
import com.mfh.framework.hybrid.WebViewDelegate;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.adv.AdvLocalPic;
import com.mfh.framework.uikit.adv.AdvLocalPicAdapter;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.EmptyLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class HomeFragment extends BaseFragment{
    @Bind(R.id.viewpager_adv)
    AdvertisementViewPager advertiseViewPager;
    private AdvLocalPicAdapter mAdvLocalPicAdapter;

    @Bind(R.id.webview)
    HybridWebView mWebView;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;

    private WebViewJavascriptBridge bridge;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        List<AdvLocalPic> localAdvList = new ArrayList<>();
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.ic_adv_beef));
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.ic_adv_duanwu));
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.ic_adv_apple));
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.ic_adv_fresh));
        mAdvLocalPicAdapter = new AdvLocalPicAdapter(getActivity(), localAdvList, null);
        advertiseViewPager.setAdapter(mAdvLocalPicAdapter);
        //TODO,定时切换(每隔5秒切换一次)
        advertiseViewPager.startSlide(3 * 1000);

        initWebView();

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        mWebView.loadUrl("http://www.jd.com/");

        DialogUtil.showHint("首页");
    }

    @Override
    public void onResume(){
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWebView != null){
            mWebView.destroy();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UIHelper.ACTIVITY_REQUEST_CODE_ZXING_QRCODE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bundle bundle = data.getExtras();
                    String resultText = bundle.getString("result", "");
//                Bitmap barcode =  (Bitmap)bundle.getParcelable("bitmap");//扫描截图

                    if (StringUtils.isUrl(resultText) && resultText.contains(MfhApi.DOMAIN)) {
                        DialogUtil.showHint(resultText);
                    } else {
                        DialogUtil.showHint(String.format("非法的URL： %s", resultText));
                    }
                } catch (Exception ex) {
                    //TransactionTooLargeException
                    ZLogger.e(ex.toString());
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btn_pay)
    public void quickPay(){
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "支付");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_FRAGMENT_TYPE, SimpleActivity.FT_QUICK_PAY);
        Intent intent = new Intent(getActivity(), SimpleActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
//        startActivityForResult(intent, 0);
    }

    @OnClick(R.id.btn_scan)
    public void scannerQR() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            DialogUtil.showHint("拍照权限未打开！");
            return;
        }

        //需要处理扫描结果
        Intent intent = new Intent(getActivity(), ScanActivity.class);
        startActivityForResult(intent, UIHelper.ACTIVITY_REQUEST_CODE_ZXING_QRCODE);
    }

    /**
     * 初始化webview
     * */
    private void initWebView(){
//        mWebView.setScrollViewCallbacks(mWebViewScrollCallbacks);

        bridge= new WebViewJavascriptBridge(getActivity(), mWebView, new UserServerHandler(), webviewDelegate) ;
        registerHandle();
    }

    /**
     * register native method
     * */
    private void registerHandle(){

    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler{
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
            if (null != responseCallback) {
                responseCallback.callback("i have already received you data.");
            }
        }
    }

    WebViewDelegate webviewDelegate = new WebViewDelegate(){

        @Override
        public void onPageFinished(WebView view, String url) {
            onUrlFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            emptyView.setErrorType(EmptyLayout.NETWORK_ERROR);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLogger.d(String.format("shouldOverrideUrlLoading url=%s\ncookie=%s",
                    url, CookieManager.getInstance().getCookie(url)));

//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

//            url = Uri.decode(url);
//            view.loadUrl(url);//在当前webview中加载页面。

//            HybridActivity.actionStart(getActivity(), url, true, false, -1);
            return false;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
//            onWebTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
//            if (newProgress > 90) {
////                loadingImageView.toggle(false);
////                emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
//            }
        }
    };
    /**
     * 链接载入成功后会被调用
     *
     * @param view
     *            WebView
     * @param url
     *            链接地址
     */
    protected void onUrlFinished(WebView view, String url) {
//        mCurrentUrl = url;
//        ZLogger.d("onPageFinished, mCurrentUrl = " + mCurrentUrl);
        if(emptyView.getErrorState() == EmptyLayout.NETWORK_LOADING){
            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
        }
    }

    /**
     * 重新加载*/
    private void reload(){
        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

        if(mWebView != null){
            String url = mWebView.getUrl();
            if(TextUtils.isEmpty(url)){
                return;
            }
//            emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);
            mWebView.clearHistory();
            mWebView.loadUrl(url);
        }
    }

}
