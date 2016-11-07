package com.mfh.framework.core.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Created by shengkun on 15/6/12.
 */
public class ClipboardUtils {

    public static void copyText(Context context, String text) {
        if (context == null){
            return;
        }
        // 得到剪贴板管理器
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("copy text", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static CharSequence pasterText(Context context){
        if (context == null){
            return null;
        }

        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Examines the item on the clipboard. If getText() does not return null, the clip item contains the
// text. Assumes that this application can only handle one item at a time.
        ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

// Gets the clipboard as text.
        return item.getText();
    }
}
