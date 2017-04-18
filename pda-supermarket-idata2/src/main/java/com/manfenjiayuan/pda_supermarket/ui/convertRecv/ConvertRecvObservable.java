package com.manfenjiayuan.pda_supermarket.ui.convertRecv;

import java.util.Observable;

/**
 * 转换收货
 * Created by bingshanguxue on 08/02/2017.
 */

public class ConvertRecvObservable extends Observable {

    private static ConvertRecvObservable instance = null;

    /**
     * 返回 ConvertRecvObservable 实例
     *
     * @return
     */
    public static ConvertRecvObservable getInstance() {
        if (instance == null) {
            synchronized (ConvertRecvObservable.class) {
                if (instance == null) {
                    instance = new ConvertRecvObservable();
                }
            }
        }
        return instance;
    }



    private void notifyDatasetChanged(){
        setChanged();    //标记此 Observable对象为已改变的对象
        notifyObservers();    //通知所有观察者
    }

}
