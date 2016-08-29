package com.manfenjiayuan.loveshopping.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;

import com.manfenjiayuan.loveshopping.AdvertisementPagerAdapter;
import com.manfenjiayuan.loveshopping.AutoScrollViewPager;
import com.manfenjiayuan.loveshopping.R;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.viewpagertransformer.DepthPageTransformer;
import com.mfh.framework.uikit.widget.OnTabReselectListener;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.api.MfhApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

//import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends BaseFragment implements
        OnTabReselectListener {

    @Bind(R.id.adv_viewpager)
    AutoScrollViewPager mAutoScrollViewPager;
    private AdvertisementPagerAdapter mAdvertisementPagerAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAutoScrollViewPager.setPageTransformer(true, new DepthPageTransformer());
        mAutoScrollViewPager.setCycle(true);// 是否自动循环轮播，默认为true
        mAutoScrollViewPager.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_CYCLE);// 滑动到第一个或最后一个Item的处理方式，支持没有任何操作、轮播以及传递到父View三种模式
        mAutoScrollViewPager.setBorderAnimation(false);//设置循环滚动时滑动到从边缘滚动到下一个是否需要动画，默认为true
        mAutoScrollViewPager.setStopScrollWhenTouch(false);//当手指碰到ViewPager时是否停止自动滚动，默认为true
        mAutoScrollViewPager.setAutoScrollDurationFactor(1.0);// 设置ViewPager滑动动画间隔时间的倍率，达到减慢动画或改变动画速度的效果
        List<String> advList = new ArrayList<>();
        //multi
        advList.add("http://resource.manfenjiayuan.cn/product/thumbnail_1294.jpg");
        advList.add("http://chunchunimage.b0.upaiyun.com/product/15863.jpg!small");
        advList.add("http://chunchunimage.b0.upaiyun.com/product/15866.jpg!small");
        advList.add(null);
//        advList.add("http://chunchunimage.b0.upaiyun.com/product/3655.JPG!small"));
//        advList.add("http://chunchunimage.b0.upaiyun.com/product/6167.JPG!small"));
//        //simple
//        advList.add("http://chunchunimage.b0.upaiyun.com/product/6167.JPG!small"));
        mAdvertisementPagerAdapter = new AdvertisementPagerAdapter(getActivity(),
                advList, null);
        mAutoScrollViewPager.setAdapter(mAdvertisementPagerAdapter);

        mAutoScrollViewPager.startAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop auto scroll when onPause
        mAutoScrollViewPager.stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        // start auto scroll when onResume
        mAutoScrollViewPager.startAutoScroll();
    }


    @Override
    public void onTabReselect() {

        ZLogger.d("HomeFragment.onTabReselect");
    }

    @OnClick(R.id.iv_scanner)
    public void scannerQR(){
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
}
