package com.mfh.owner.ui.shake;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.uikit.widget.AvatarView;
import com.mfh.owner.R;

/**
 * 自定义摇一摇结果对话框
 * Created by ZZN on 2015/4/30.
 */
public class ShakeResultDialog  extends Dialog {
    private Context context;

    private AvatarView ivShopBrand;
    private TextView tvShopTitle;
    private TextView tvShopDescription;

    private Object data;

    public interface DialogListener{
        void onRedirectTo(Object data);
    }
    private DialogListener listener;
    public void setDialogListener(DialogListener listener){
        this.listener = listener;
    }

    public ShakeResultDialog(Context context) {
        super(context);
        this.context = context;
    }

    public ShakeResultDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    protected ShakeResultDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.shake_result);
        View contentView = View.inflate(context, R.layout.shake_result, null);
        setContentView(contentView);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onRedirectTo(data);
                }

                dismiss();
            }
        });

        //LinearLayout.LayoutParams.MATCH_PARENT
//        contentView.setLayoutParams(new LinearLayout.LayoutParams(DensityUtil.dip2px(getContext(), 150), DensityUtil.dip2px(getContext(), 80)));
        contentView.setLayoutParams(new FrameLayout.LayoutParams(DensityUtil.dip2px(getContext(), 240),
                DensityUtil.dip2px(getContext(), 70)));
        //setup views
        ivShopBrand = ((AvatarView) findViewById(R.id.iv_bitmap));
        tvShopTitle = ((TextView) findViewById(R.id.tv_name));
        tvShopDescription = ((TextView) findViewById(R.id.tv_description));

        this.setCanceledOnTouchOutside(true);//设置点击Dialog外部任意区域关闭Dialog

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;   //新位置X坐标
        lp.y = 280; //新位置Y坐标
        this.onWindowAttributesChanged(lp);
//        window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.anim_menu_bottombar);  //添加动画
    }

    @Override
    protected void onStart() {
        super.onStart();

//        tvShopTitle.setText(title);
    }

    /**
     * 显示对话框
     * */
    public void show(ShakeHistoryEntity entity){
        data = entity;

        this.show();

        ivShopBrand.setAvatarUrl(entity.getIconUrl());
        tvShopTitle.setText(entity.getTitle());
        tvShopDescription.setText(entity.getDescription());

//        Window window = getWindow();
//        window.setWindowAnimations(R.anim.push_right_in);
    }
}

