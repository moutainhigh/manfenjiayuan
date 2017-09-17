package com.manfenjiayuan.mixicook_vip.record;

/**
 * Created by bingshanguxue on 09/08/2017.
 */

public interface RecordCompleteListener {
    void onComplete(String path);
    void onError(int code);
}
