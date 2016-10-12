package com.manfenjiayuan.mixicook_vip.ui.location;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.address.AddressBrief;


/**
 * 分享位置
 * Created by bingshanguxue on 2015/5/10.
 */
public class LocationHeaderView extends FrameLayout{

    private View rootView;
    private TextView tvTitle, tvSubTitle;
    private ImageView ivMarker;

    private AddressBrief mAddressBrief = new AddressBrief();

    public LocationHeaderView(Context context) {
        super(context);
        rootView = LayoutInflater.from(context).inflate(R.layout.location_headerview, this, true);
        this.initAndSetUpView();
    }

    public LocationHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rootView = LayoutInflater.from(context).inflate(R.layout.listview_header_conversation, this, true);
        this.initAndSetUpView();
    }

    public LocationHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        rootView = LayoutInflater.from(context).inflate(R.layout.listview_header_conversation, this, true);
        this.initAndSetUpView();
    }

    public interface ConversationHeaderViewListerner{
        void onItemClicked(int i);
    }
    private ConversationHeaderViewListerner listener;
    public void setListener(ConversationHeaderViewListerner listener){
        this.listener = listener;
    }


    private void initAndSetUpView() {
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        tvSubTitle = (TextView) rootView.findViewById(R.id.tv_subtitle);
        ivMarker = (ImageView) rootView.findViewById(R.id.iv_marker);

        tvTitle.setText("[当前位置]");
//        rootView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    public AddressBrief getAddressBrief() {
        return mAddressBrief;
    }

    public void setAddressBrief(AddressBrief addressBrief) {
        mAddressBrief = addressBrief;

        if (mAddressBrief != null){
            tvSubTitle.setText(mAddressBrief.getAddress());
        }
        else{
            tvSubTitle.setText("对不起，没有搜索到相关数据！");
        }

    }


    public void setMarkerEnabled(boolean enabled){
        ivMarker.setSelected(enabled);
        if (enabled){
            ivMarker.setVisibility(View.VISIBLE);
        }else{
            ivMarker.setVisibility(View.INVISIBLE);
        }
    }
}
