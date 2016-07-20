package com.manfenjiayuan.loveshopping.activity;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.loveshopping.R;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.viewpagerindicator.CirclePageIndicator;
import com.mfh.framework.uikit.UIHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class NavigationActivity extends BaseActivity {

    @Bind(R.id.indicator)
    CirclePageIndicator mCirclePageIndicator;
    @Bind(R.id.viewpager_navigation_activity)
    ViewPager mViewPager;
    private NavigationPagerAdapter mNavigationPagerAdapter;


    /**
     * 广告图片显示适配器
     * Created by Administrator on 2015/4/20.
     */
    public class NavigationPagerAdapter extends PagerAdapter {
        private NavigationActivity mNavigationActivity;
        private List<View> entityList = new ArrayList<>();

        public NavigationPagerAdapter(NavigationActivity navigationActivity, List<View> entityList) {
            this.mNavigationActivity = navigationActivity;
            this.entityList = entityList;
        }


        @Override
        public int getCount() {
            return entityList != null ? entityList.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        //java.lang.UnsupportedOperationException: Required method destroyItem was not overridden
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(this.entityList.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            container.addView(this.entityList.get(position));

            return this.entityList.get(position);
        }

    }


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_navigation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        mNavigationPagerAdapter = new NavigationPagerAdapter(this, retrieveViews());
        this.mViewPager.setAdapter(mNavigationPagerAdapter);
        this.mViewPager.setOffscreenPageLimit(5);
        this.mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

//        this.mRadioGroup.getChildAt(0).setSelected(true);
//        this.mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                for (int i2 = 0; i2 < 4; i2++) {
//                    if (i == mRadioGroup.getChildAt(i2).getId()) {
//                        mViewPager.setCurrentItem(i2);
//                        return;
//                    }
//                }
//            }
//        });
//        this.mRadioGroup.check(this.mRadioGroup.getChildAt(0).getId());

//        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mCirclePageIndicator.setViewPager(mViewPager);
        mCirclePageIndicator.setCurrentItem(0);
    }

    private List<View> retrieveViews() {
        int[] iArr = new int[]{R.mipmap.guide_title0, R.mipmap.guide_title1, R.mipmap.guide_title2, R.mipmap.guide_title3};
        List<View> arrayList = new ArrayList();
        String[] strArr = new String[]{"生鲜食品，应有尽有", "一双可以空下来的手", "一小时送达，新鲜每一刻", "支付宝轻松刷"};
        String[] strArr2 = new String[]{"", "和一颗快乐的心", "", ""};
        String[] strArr3 = new String[]{"水产海鲜，蔬菜水果，熟食料理，", "杀鱼、羊肉切卷、切成牛排、去皮、切丁、", "冷冻、冷藏、恒温、常温、高温、全温层", "支付宝轻松刷，再也不用带钱包了。"};
        String[] strArr4 = new String[]{"生鲜加工，精致烘培，样样都有。", "去骨切片，统统由我搞定。", "配送，新鲜有保障。", ""};
        int[] iArr2 = new int[]{R.mipmap.guide_page0, R.mipmap.guide_page1, R.mipmap.guide_page2, R.mipmap.guide_page3};
        for (int i = 0; i < 4; i++) {
            View inflate = getLayoutInflater().inflate(R.layout.item_navigation_layout, null);
            ((ImageView) inflate.findViewById(R.id.guide_title)).setImageResource(iArr[i]);
            ((ImageView) inflate.findViewById(R.id.imageview_item_navigation)).setImageResource(iArr2[i]);
            ((TextView) inflate.findViewById(R.id.guide_sub_title_one)).setText(strArr[i]);
            ((TextView) inflate.findViewById(R.id.guide_content_one)).setText(strArr3[i]);
            ((TextView) inflate.findViewById(R.id.guide_content_two)).setText(strArr4[i]);
            if (i == 1) {
                TextView textView = (TextView) inflate.findViewById(R.id.guide_sub_title_two);
                textView.setVisibility(View.VISIBLE);
                textView.setText(strArr2[1]);
            }
            if (i == 2) {
                inflate.findViewById(R.id.guide_view_tocenter).setVisibility(View.VISIBLE);
            }
            if (i == 3) {
                TextView textView = (TextView) inflate.findViewById(R.id.navigation_imagebutton);
                textView.setVisibility(View.VISIBLE);
                inflate.findViewById(R.id.guide_content_two).setVisibility(View.INVISIBLE);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIHelper.startActivity(NavigationActivity.this, MainActivity.class);
                        finish();
                    }
                });
            }
            arrayList.add(inflate);
        }
        return arrayList;
    }
}
