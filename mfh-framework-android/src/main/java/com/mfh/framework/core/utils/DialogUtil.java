package com.mfh.framework.core.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.R;


/**
 * A tool to display system-level messages.
 * 
 * @author NAT.ZZN(bingshanguxue) created on 2013-4-12
 * @since Framework 1.0
 */
public class DialogUtil {
    private Context context;
    private static Toast mToast = null;

    public DialogUtil(Context context) {
        super();
        this.context = context;
    }
    
    /**
     * 提示消息，稍后自动关闭
     * @param message 提示消息
     * @author zhangyz created on 2013-4-12
     */
    public void showHint(CharSequence message) {
        if(mToast == null){
            mToast = Toast.makeText(context, message, Toast.LENGTH_LONG );
        }else{
            mToast.setText(message);
        }

        mToast.show();
    }

    public static void showHint(Context context, CharSequence message) {
        if(mToast == null){
            mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT );
        }else{
            mToast.setText(message);
        }

        mToast.show();
    }

    public static void showHint(String message) {
        if(mToast == null){
            mToast = Toast.makeText(MfhApplication.getAppContext(), message, Toast.LENGTH_SHORT );
        }else{
            mToast.setText(message);
        }

        mToast.show();
    }

    public static void showHint(int resId) {
        if(mToast == null){
            mToast = Toast.makeText(MfhApplication.getAppContext(), resId, Toast.LENGTH_SHORT );
        }else{
            mToast.setText(resId);
        }

        mToast.show();
    }

    public static void cancelHint( ){
        if(mToast != null){
            mToast.cancel();
        }
    }

    public static void showMessage(Context context, CharSequence message) {
        try {
            new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.comn_dlg_title))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                 }
                })
                .show();
        }
        catch (Throwable ex) {
        }
    }
    
    /** 
     * 提示消息，点击确定后关闭 
     * @param message 提示消息
     */
    public void showMessage(CharSequence message){ 
        new AlertDialog.Builder(context) 
        .setTitle(context.getString(R.string.comn_dlg_title))
        .setMessage(message) 
        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() { 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            } 
        }) 
        .show();
    }
    
    /**
     * 显示一个典型的两按钮的询问对话框
     * @param message
     * @param listen
     * @author zhangyz created on 2013-4-12
     */
    public void showYesNoDialog(CharSequence message, DialogInterface.OnClickListener listen) {
       showYesNoDialog(context.getString(R.string.comn_dlg_title), message, listen);
    }
    
    /**
     * 显示一个典型的两按钮的询问对话框
     * @param title 标题
     * @param message 询问内容
     * @author zhangyz created on 2013-4-5
     */
    public void showYesNoDialog(CharSequence title, CharSequence message, 
            DialogInterface.OnClickListener listen) {
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setIcon(android.R.drawable.btn_star)
            .setMessage(message)
            .setTitle(title)
            .setPositiveButton(context.getString(R.string.ok), listen)
            .setNegativeButton(context.getString(R.string.cancel), listen)
            .create();
        dialog.setCancelable(false);
        dialog.show();
    }



}
