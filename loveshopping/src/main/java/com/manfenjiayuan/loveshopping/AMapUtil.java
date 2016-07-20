package com.manfenjiayuan.loveshopping;

import com.amap.api.location.AMapLocation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bingshanguxue on 16/4/7.
 */
public class AMapUtil {
    /**
     *  开始定位
     */
    public final static int MSG_LOCATION_START = 0;
    /**
     * 定位完成
     */
    public final static int MSG_LOCATION_FINISH = 1;
    /**
     * 停止定位
     */
    public final static int MSG_LOCATION_STOP= 2;

    public final static String KEY_URL = "URL";
    public final static String URL_H5LOCATION = "file:///android_asset/location.html";
    /**
     * 根据定位结果返回定位信息的字符串
     * @param loc
     * @return
     */
    public synchronized static String getLocationStr(AMapLocation location){
        if(null == location){
            return null;
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date = new Date(location.getTime());//定位时间

        StringBuffer sb = new StringBuffer();
        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if(location.getErrorCode() == 0){

            sb.append("定位成功 " + df.format(date) + "\n");
            sb.append("定位类型: " + location.getLocationType() + "\n");
            sb.append("经    度   : " + location.getLongitude() + "\n");
            sb.append("纬    度   : " + location.getLatitude() + "\n");
            sb.append("精    度   : " + location.getAccuracy() + "米" + "\n");
            sb.append("提供者      : " + location.getProvider() + "\n");

            if (location.getProvider().equalsIgnoreCase(
                    android.location.LocationManager.GPS_PROVIDER)) {
                // 以下信息只有提供者是GPS时才会有
                sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                sb.append("角    度    : " + location.getBearing() + "\n");
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : "
                        + location.getSatellites() + "\n");
            } else {
                // 提供者是GPS时是没有以下信息的
                sb.append("国    家   : " + location.getCountry() + "\n");
                sb.append("省        : " + location.getProvince() + "\n");
                sb.append("市        : " + location.getCity() + "\n");
                sb.append("城市编码 : " + location.getCityCode() + "\n");
                sb.append("区        : " + location.getDistrict() + "\n");
                sb.append("区域码      : " + location.getAdCode() + "\n");
                sb.append("街   道    : " + location.getStreet() + "\n");
                sb.append("门牌号      : " + location.getStreetNum() + "\n");
                //地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                sb.append("地    址   : " + location.getAddress() + "\n");
                sb.append("兴趣点      : " + location.getPoiName() + "\n");
                sb.append("AOI      : " + location.getAoiName() + "\n");//获取当前定位点的AOI信息
            }
        } else {
            //定位失败
            sb.append("定位失败 " + df.format(date) + "\n");
            sb.append("错误码  : " + location.getErrorCode() + "\n");
            sb.append("错误信息 : " + location.getErrorInfo() + "\n");
            sb.append("错误描述 : " + location.getLocationDetail() + "\n");
        }
        return sb.toString();
    }
}
