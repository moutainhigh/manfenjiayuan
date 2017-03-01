package com.manfenjiayuan.pda_supermarket.database.dao;


import com.mfh.framework.api.scGoodsSku.ProductSkuBarcode;
import com.mfh.framework.database.dao.BaseNetDao;
import com.mfh.framework.database.dao.DaoUrl;

/**
 * 基于网络访问
 * Created by bingshanguxue on 14-5-7.
 */
public class PosProductSkuNetDao extends BaseNetDao<ProductSkuBarcode, Long> {
    private boolean downLoading = false;//正在下载标志
    private int errorCount = 0;//连续出错次数

    @Override
    protected void initUrlInfo(DaoUrl daoUrl) {
        daoUrl.setListUrl("/scProductSkuBarcodes/findShopOtherBarcodes");
    }

    @Override
    protected Class<Long> initPkClass() {
        return Long.class;
    }

    public boolean isDownLoading() {
        return downLoading;
    }

    /**
     * 成功后复原下载状态
     */
    public void resetDownLoading() {
        this.downLoading = false;
        //通知结束
        //Intent intent = new Intent(MsgConstants.ACTION_DOWNLOAD_FINISH);
        //this.getContext().sendBroadcast(intent);
    }

    /**
     * 复原出错次数
     */
    protected void resetErrorCount() {
        errorCount = 0;
    }

    /**
     * 出错时复原下载状态
     */
    protected void resetDownLoadingOnError() {
        resetDownLoading();
        errorCount ++;
        if (errorCount <= 2) {//5次失败后停止轮询
//            try {
//                Intent intent = new Intent(MsgConstants.ACTION_MSG_SERVERERROR);
//                this.getContext().sendBroadcast(intent);
//            }
//            catch (Throwable ex) {
//                ;
//            }
        }
    }

    public void restDownLoadingWithNoIntent() {
        this.downLoading = false;
    }


    @Override
    protected Class<ProductSkuBarcode> initPojoClass() {
        return ProductSkuBarcode.class;
    }

}
