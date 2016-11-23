package com.mfh.owner.ui.map;

import android.content.Context;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.owner.R;

import java.util.Map;


/**
 * 分享位置
 * Created by Administrator on 2015/5/10.
 */
public class LocationHeaderView extends FrameLayout{

    private View rootView;
    private TextView tvTitle, tvSubTitle;
    private ImageView ivMarker;

    private Map<String, String> data = new ArrayMap<>();

    public LocationHeaderView(Context context) {
        super(context);
        rootView = LayoutInflater.from(context).inflate(R.layout.listitem_share_location, this, true);
        this.initAndSetUpView();
    }

    public LocationHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rootView = LayoutInflater.from(context).inflate(R.layout.listitem_share_location, this, true);
        this.initAndSetUpView();
    }

    public LocationHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        rootView = LayoutInflater.from(context).inflate(R.layout.listitem_share_location, this, true);
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

        tvTitle.setText("[位置]");
//        rootView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    public void setData(Map<String, String> data){
        this.data = data;
        tvSubTitle.setText(data.get("ADDRESS"));
    }

    public void appendData(String key, String value){
        this.data.put(key, value);
        tvSubTitle.setText(data.get("ADDRESS"));
    }

    public Map<String, String> getData() {
        return data;
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
