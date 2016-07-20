package com.mfh.framework.update;

/**
 * Created by bingshanguxue on 5/18/16.
 */
public interface MfhDownloadListener {
    void onDownloadEnd(int i, String str);

    void onDownloadStart();

    void onDownloadUpdate(int i);
}
