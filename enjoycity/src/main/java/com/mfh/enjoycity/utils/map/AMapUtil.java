/**
 * 
 */
package com.mfh.enjoycity.utils.map;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.EditText;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AMapUtil {

	public static final String PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 判断edittext是否null
	 */
	public static String checkEditText(EditText editText) {
		if (editText != null && editText.getText() != null
				&& !(editText.getText().toString().trim().equals(""))) {
			return editText.getText().toString().trim();
		} else {
			return "";
		}
	}

	public static Spanned stringToSpan(String src) {
		return src == null ? null : Html.fromHtml(src.replace("\n", "<br />"));
	}

	public static String colorFont(String src, String color) {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append("<font color=").append(color).append(">").append(src)
				.append("</font>");
		return strBuf.toString();
	}

	public static String makeHtmlNewLine() {
		return "<br />";
	}

	public static String makeHtmlSpace(int number) {
		final String space = "&nbsp;";
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < number; i++) {
			result.append(space);
		}
		return result.toString();
	}

	public static String getFriendlyLength(int lenMeter) {
		if (lenMeter > 10000) // 10 km
		{
			int dis = lenMeter / 1000;
			return dis + ChString.Kilometer;
		}

		if (lenMeter > 1000) {
			float dis = (float) lenMeter / 1000;
			DecimalFormat fnum = new DecimalFormat("##0.0");
			String dstr = fnum.format(dis);
			return dstr + ChString.Kilometer;
		}

		if (lenMeter > 100) {
			int dis = lenMeter / 50 * 50;
			return dis + ChString.Meter;
		}

		int dis = lenMeter / 10 * 10;
		if (dis == 0) {
			dis = 10;
		}

		return dis + ChString.Meter;
	}

	public static boolean IsEmptyOrNullString(String s) {
		return (s == null) || (s.trim().length() == 0);
	}

//	/**
//	 * 把LatLng对象转化为LatLonPoint对象
//	 */
//	public static LatLonPoint convertToLatLonPoint(LatLng latlon) {
//		return new LatLonPoint(latlon.latitude, latlon.longitude);
//	}
//
//	/**
//	 * 把LatLonPoint对象转化为LatLon对象
//	 */
//	public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
//		return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
//	}
//
//	/**
//	 * 把集合体的LatLonPoint转化为集合体的LatLng
//	 */
//	public static ArrayList<LatLng> convertArrList(List<LatLonPoint> shapes) {
//		ArrayList<LatLng> lineShapes = new ArrayList<>();
//		for (LatLonPoint point : shapes) {
//			LatLng latLngTemp = AMapUtil.convertToLatLng(point);
//			lineShapes.add(latLngTemp);
//		}
//		return lineShapes;
//	}

	/**
	 * long类型时间格式化
	 */
	public static String convertToTime(long time) {
		SimpleDateFormat df = new SimpleDateFormat(PATTERN_DATE_TIME);
		Date date = new Date(time);
		return df.format(date);
	}

	public static final String HtmlBlack = "#000000";
	public static final String HtmlGray = "#808080";

//	/**
//	 *显示位置信息
//	 * */
//	public static String toString(AMapLocation aLocation){
//		Double geoLat = aLocation.getLatitude();
//		Double geoLng = aLocation.getLongitude();
//		String cityCode = "";
//		String desc = "";
//		Bundle locBundle = aLocation.getExtras();
//		if (locBundle != null) {
//			cityCode = locBundle.getString("citycode");
//			desc = locBundle.getString("desc");
//		}
//
//		String str = ("定位成功:(" + geoLng + "," + geoLat + ")"
//				+ "\n精    度    :" + aLocation.getAccuracy() + "米"
//				+ "\n定位方式:" + aLocation.getProvider() + "\n定位时间:"
//				+ AMapUtil.convertToTime(aLocation.getTime()) + "\n城市编码:"
//				+ cityCode + "\n位置描述:" + desc + "\n省:"
//				+ aLocation.getProvince() + "\n市:" + aLocation.getCity()
//				+ "\n区(县):" + aLocation.getDistrict() + "\n区域编码:" + aLocation
//				.getAdCode());
//		return str;
////            myLocation.setText(str);
////            Log.d("Nat:", "locationInfo: " + str);
//	}
}
