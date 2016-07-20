package com.mfh.framework.uikit.base;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.dialog.ProgressDialog;

/**
 * 同步数据
 * Created by bingshanguxue on 16/3/24.
 */
public abstract class BaseProgressFragment extends BaseFragment {
    /**
     * 加载中
     */
    public void onLoadProcess(String description){
        ZLogger.d(description);
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, description, false);
    }
    /**
     * 加载失败
     */
    public void onLoadError(String errMessage){
        ZLogger.d(errMessage);
        showProgressDialog(ProgressDialog.STATUS_ERROR, errMessage, true);
//        hideProgressDialog();
//        DialogUtil.showHint(errMessage);
    }
    /**
     * 加载完成
     */
    public void onLoadFinished(){
        hideProgressDialog();
    }

}
