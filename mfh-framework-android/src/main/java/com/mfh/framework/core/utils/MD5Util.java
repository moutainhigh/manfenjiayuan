package com.mfh.framework.core.utils;

import java.security.MessageDigest;

/**
 * 1. 转换字节数组为16进制字串 {@link #byteArrayToHexString(byte[])}<br>
 * */
public class MD5Util {

	private static final String hexDigits[] = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/**
	 * 通用加密算法
	 * @param algorithm can be "MD5", "SHA-1", "SHA-256"
	 * */
	public static String getMessageDigest(String algorithm, byte[] salt) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdTemp = MessageDigest.getInstance(algorithm);
			// 使用指定的字节更新摘要
			if (salt != null) {
				mdTemp.update(salt);
			}
			// 获得密文
			byte[] md = mdTemp.digest();
			// 把密文转换成十六进制的字符串形式(convert hash bytes to string (usually in hexadecimal form))
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (byte byte0 : md) {
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 通用加密算法
	 * @param algorithm can be "MD5", "SHA-1", "SHA-256"
	 * */
	public static String getMessageDigest(String algorithm, byte[] salt, byte[] input) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdTemp = MessageDigest.getInstance(algorithm);
			// 使用指定的字节更新摘要
			if (salt != null) {
				mdTemp.update(salt);
			}
			// 获得密文
			byte[] md = mdTemp.digest(input);
			// 把密文转换成十六进制的字符串形式(convert hash bytes to string (usually in hexadecimal form))
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (byte byte0 : md) {
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getMessageDigest(byte[] buffer) {
		return getMessageDigest("MD5", buffer);
	}

	/**
	 * 转换字节数组为16进制字串
	 * @param b 字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToHexString(byte b[]) {
		StringBuilder resultSb = new StringBuilder();
		for (byte aB : b) {
			resultSb.append(byteToHexString(aB));
		}

		return resultSb.toString();
	}

	/**
	 * 转换byte到16进制
	 * @param b 要转换的byte
	 * @return 16进制格式
	 */
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n += 256;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	/**
	 * MD5编码
	 * @param origin 原始字符串
	 * @param charsetname 编码类型
	 * @return 经过MD5加密之后的结果
	 */
	public static String MD5Encode(String origin, String charsetname) {
		String resultString = null;
		try {
			resultString = origin;

			MessageDigest md = MessageDigest.getInstance("MD5");
			if (charsetname == null || "".equals(charsetname))
				resultString = byteArrayToHexString(md.digest(origin
						.getBytes()));
			else
				resultString = byteArrayToHexString(md.digest(origin
						.getBytes(charsetname)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultString;
	}


}
