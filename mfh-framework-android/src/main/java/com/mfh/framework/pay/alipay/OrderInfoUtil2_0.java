package com.mfh.framework.pay.alipay;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * @see <a href="https://doc.open.alipay.com/docs/doc.htm?
 * spm=a219a.7629140.0.0.6agxNc&treeId=204&articleId=105465&docType=1">App支付请求参数说明</a>
 */
public class OrderInfoUtil2_0 {

	/**App支付接口:通过此接口传入订单参数，同时唤起支付宝客户端。*/
	public static final String ALIPAY_TRADE_APPPAY = "alipay.trade.app.pay";
	/**交易关闭接口:通过此接口关闭此前已创建的交易，关闭后，用户将无法继续付款。仅能关闭创建后未支付的交易。*/
	public static final String ALIPAY_TRADE_CLODE = "alipay.trade.close";
	/**交易状态查询接口:通过此接口查询某笔交易的状态，交易状态：交易创建，等待买家付款；未付款交易超时关闭，或支付完成后全额退款；交易支付成功；交易结束，不可退款。*/
	public static final String ALIPAY_TRADE_QUERY = "alipay.trade.query";
	/**交易退款接口:通过此接口对单笔交易完成退款操作。*/
	public static final String ALIPAY_TRADE_REFUND = "alipay.trade.refund";
	/**退款查询接口:查询退款订单的状态。*/
	public static final String ALIPAY_TRADE_REFUNDQUERY = "alipay.trade.fastpay.refund.query";
	/**账单查询接口:调用此接口获取账单的下载链接。*/
	public static final String ALIPAY_DOWNLOADURL_QUERY = "alipay.data.dataservice.bill.downloadurl.query";

	public static final String SIGNTYPE_RSA = "RSA";
	public static final String VERSION = "1.0";
	public static final String CHARSET_UTF8 = "UTF-8";

	/**
	 * 构造授权参数列表
	 * 
	 * @param pid
	 * @param app_id
	 * @param target_id
	 * @return
	 */
	public static Map<String, String> buildAuthInfoMap(String pid, String app_id, String target_id) {
		Map<String, String> keyValues = new HashMap<>();

		// 商户签约拿到的app_id，如：2013081700024223
		keyValues.put("app_id", app_id);

		// 商户签约拿到的pid，如：2088102123816631
		keyValues.put("pid", pid);

		// 服务接口名称， 固定值
		keyValues.put("apiname", "com.alipay.account.auth");

		// 商户类型标识， 固定值
		keyValues.put("app_name", "mc");

		// 业务类型， 固定值
		keyValues.put("biz_type", "openservice");

		// 产品码， 固定值
		keyValues.put("product_id", "APP_FAST_LOGIN");

		// 授权范围， 固定值
		keyValues.put("scope", "kuaijie");

		// 商户唯一标识，如：kkkkk091125
		keyValues.put("target_id", target_id);

		// 授权类型， 固定值
		keyValues.put("auth_type", "AUTHACCOUNT");

		// 签名类型
		keyValues.put("sign_type", "RSA");

		return keyValues;
	}

	/**
	 * 构造业务请求参数的集合
	 * @param timeout_express 该笔订单允许的最晚付款时间，逾期将关闭交易。(必填),最大长度6，
	 *                           取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天
	 *                           （1c-当天的情况下，无论交易何时创建，都在0点关闭）。
	 *                           该参数数值不接受小数点， 如 1.5h，可转换为 90m。
	 * @param product_code 销售产品码，商家和支付宝签约的产品码(必填),最大长度64，为固定值QUICK_MSECURITY_PAY
	 * @param total_amount 订单总金额(必填),最大长度9，单位为元，精确到小数点后两位，取值范围[0.01,100000000]
	 * @param subject 商品的标题/交易标题/订单标题/订单关键字等。(必填),最大长度256
	 * @param out_trade_no 商户网站唯一订单号(必填),最大长度64,如70501111111S001111119
	 * @param body 对一笔交易的具体描述信息。(非必填),最大长度128,如果是多种商品，请将商品描述字符串累加传给body。
	 * @param seller_id 收款支付宝用户ID。(非必填),最大长度16,如果该值为空，则默认为商户签约账号对应的支付宝用户ID
	 *
	 * 注意：支付请求中的订单金额total_amount，请务必依赖服务端，不要轻信客户端上行的数据（客户端本地上行数据在用户手机环境中无法确保一定安全）。
	 * */
	public static String buildBizContent(String body, String subject,
										 String out_trade_no, String timeout_express,
										 String total_amount, String seller_id){
		JSONObject jsonObject = new JSONObject();
		if (!StringUtils.isEmpty(body)){
			jsonObject.put("body", body);
		}
		jsonObject.put("subject", subject);
		jsonObject.put("out_trade_no", out_trade_no);
		jsonObject.put("timeout_express", timeout_express);
		jsonObject.put("total_amount", total_amount);
		if (!StringUtils.isEmpty(seller_id)){
			jsonObject.put("seller_id", seller_id);
		}
		jsonObject.put("product_code", "QUICK_MSECURITY_PAY");

		return jsonObject.toJSONString();
	}

	/**
	 * 构造支付订单参数列表,正式发布时应该由后台创建，这里主要是为了演示功能。
	 * @param app_id 支付宝分配给开发者的应用ID(必填),最大长度32，例：2014072300007148
	 * @param method 接口名称(必填),最大长度128，例：alipay.trade.app.pay
	 * @param charset 请求使用的编码格式(必填),最大长度10，如utf-8,gbk,gb2312等
	 * @param sign_type 商户生成签名字符串所使用的签名算法类型(必填),最大长度10，目前支持RSA
	 * @param timestamp 发送请求的时间(必填),最大长度19，格式"yyyy-MM-dd HH:mm:ss"
	 * @param version 调用的接口版本(必填),最大长度3，固定为：1.0
	 * @param notify_url 支付宝服务器主动通知商户服务器里指定的页面http/https路径(必填),最大长度256，
	 *                   建议商户使用https,如https://api.xx.com/receive_notify.htm
	 * @param biz_content 业务请求参数的集合(必填),最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递
	 *
	 * @return
	 */
	public static Map<String, String> buildOrderParamMap(String app_id, String method, String charset,
														 String timestamp,
														 String notify_url, String biz_content) {
		Map<String, String> keyValues = new HashMap<>();

		keyValues.put("app_id", app_id);
		keyValues.put("charset", charset);
		keyValues.put("method", method);
		keyValues.put("sign_type", SIGNTYPE_RSA);
		keyValues.put("timestamp", timestamp);
		keyValues.put("version", VERSION);
		keyValues.put("notify_url", notify_url);
		keyValues.put("biz_content", biz_content);
		
		return keyValues;
	}
	
	/**
	 * 构造支付订单参数信息,主要包含商户的订单信息，key=value形式，以&连接。
	 * 
	 * @param map 支付订单参数
	 * @return 按照key=value&key=value方式拼接的未签名原始字符串
	 */
	public static String buildOrderParam(Map<String, String> map) {
		List<String> keys = new ArrayList<>(map.keySet());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			sb.append(buildKeyValue(key, value, true));
			sb.append("&");
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		sb.append(buildKeyValue(tailKey, tailValue, true));

		return sb.toString();
	}
	
	/**
	 * 拼接键值对
	 * 
	 * @param key
	 * @param value
	 * @param isEncode
	 * @return
	 */
	private static String buildKeyValue(String key, String value, boolean isEncode) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append("=");
		if (isEncode) {
			try {
				sb.append(URLEncoder.encode(value, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				sb.append(value);
			}
		} else {
			sb.append(value);
		}
		return sb.toString();
	}
	
	/**
	 * 对支付参数信息进行签名
	 * 注意：请求参数的sign字段请务必在服务端完成签名生成（不要在客户端本地签名）
	 * 
	 * @param map 待签名授权信息
	 * 
	 * @return
	 */
	public static String getSign(Map<String, String> map, String rsaKey) {
		List<String> keys = new ArrayList<>(map.keySet());
		// key排序
		Collections.sort(keys);

		StringBuilder authInfo = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			authInfo.append(buildKeyValue(key, value, false));
			authInfo.append("&");
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		authInfo.append(buildKeyValue(tailKey, tailValue, false));
		ZLogger.d("authInfo=" + authInfo);

		String encodedSign = "";

		try {
			String oriSign = SignUtils.sign(authInfo.toString(), rsaKey);
			ZLogger.d("oriSign=" + oriSign);

			if (!StringUtils.isEmpty(oriSign)){
				encodedSign = URLEncoder.encode(oriSign, CHARSET_UTF8);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ZLogger.d("encodedSign=" + encodedSign);

		return "sign=" + encodedSign;
	}
	
	/**
	 * 要求外部订单号必须唯一。
	 * @return
	 */
	private static String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

}
