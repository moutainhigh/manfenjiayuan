package com.mfh.framework.uikit.base;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.ProgressDialog;

/**
 * 同步数据
 * Created by bingshanguxue on 16/3/24.
 */
public abstract class BaseProgressFragment extends BaseFragment {

    protected boolean isLoadingMore;
    protected boolean bSyncInProgress = false;//是否正在同步
    /**
     * 加载中
     */
    public void onLoadProcess(String description){
        isLoadingMore = true;
        bSyncInProgress = true;
        ZLogger.d(description);
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, description, false);
    }
    /**
     * 加载失败
     */
    public void onLoadError(String errMessage){
        bSyncInProgress = false;
        isLoadingMore = false;
        if (!StringUtils.isEmpty(errMessage)){
            ZLogger.df(errMessage);
            showProgressDialog(ProgressDialog.STATUS_ERROR, errMessage, true);
        }
        else{
            hideProgressDialog();
        }
    }
    /**
     * 加载完成
     */
    public void onLoadFinished(){
        isLoadingMore = false;
        bSyncInProgress = false;
        hideProgressDialog();
    }

}
