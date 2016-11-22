package com.bingshanguxue.vector_uikit;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.framework.core.utils.BitmapUtils;
import com.bingshanguxue.vector_uikit.widget.AvatarView;

/***
 * 自定义View（组合原有安卓控件或者布局）
 * 样式：TITLE + ICON + ARROW
 */
public class AvatarSettingItem extends LinearLayout {
    private TextView    tvTitle;
    private AvatarView ivHeader;
    private ImageView   ivArrow;
//    private View        vSeperateTop;//上分割线
//    private View        vSeperateBottom;//下分割线

	public AvatarSettingItem(Context context) {
		super(context);;
		init();
	}

	public AvatarSettingItem(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
	}

    public AvatarSettingItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

	private void init() {
		View.inflate(getContext(), R.layout.widget_avatar, this);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        ivHeader = (AvatarView) findViewById(R.id.iv_header);
        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));
        ivArrow = (ImageView) findViewById(R.id.iv_arrow_right);
	}

    public void setHeaderBitmap(Bitmap bmp){
        if (bmp != null){
            ivHeader.setImageBitmap(BitmapUtils.toRoundBitmap(bmp));
        }
    }

    public void setHeaderUrl(String url){
        ivHeader.setAvatarUrl(url);
    }



}
