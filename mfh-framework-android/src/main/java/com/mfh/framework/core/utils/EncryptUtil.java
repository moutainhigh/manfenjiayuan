package com.mfh.framework.core.utils;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.pay.alipay.Base64;

import net.tsz.afinal.core.Arrays;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;

/**
 * Created by bingshanguxue on 15/11/23.
 */
public class EncryptUtil {

    public static String decodePwd(String strSalt, String checkPwd){
        try {
            byte[] salt = decodeHex(strSalt.toCharArray());
            byte[] hashPassword = digest(checkPwd.getBytes(), "SHA-1", salt, 1024);

            ZLogger.d(Base64.encode(hashPassword));
            ZLogger.d(new String(hashPassword));
            return MD5Util.byteArrayToHexString(hashPassword);

        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 检查密码是否正确
     * @param credentials 加密后的原密码
     * @param strSalt 原密码的盐
     * @param checkPwd 待检查密码
     * @return
     * @author zhangyz created on 2015-11-16
     */
    public static boolean validPwd(String credentials, String strSalt, String checkPwd) {
        try {
            byte[] salt = decodeHex(strSalt.toCharArray());
            byte[] hashPassword = digest(checkPwd.getBytes(), "SHA-1", salt, 1024);
            byte[] orignalPassword = decodeHex(credentials.toCharArray());
            //检验旧密码
            return (Arrays.equals(hashPassword, orignalPassword));
        }
        catch(Throwable ex) {
            return false;
        }
    }

    /**
     * 对字符串进行散列, 支持md5与sha1算法.
     */
    private static byte[] digest(byte[] input, String algorithm, byte[] salt, int iterations) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            if (salt != null) {
                digest.update(salt);
            }

            byte[] result = digest.digest(input);

            for (int i = 1; i < iterations; i++) {
                digest.reset();
                result = digest.digest(result);
            }
            return result;
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

    /**
     * Converts an array of characters representing hexidecimal values into an
     * array of bytes of those same values. The returned array will be half the
     * length of the passed array, as it takes two characters to represent any
     * given byte. An exception is thrown if the passed char array has an odd
     * number of elements.
     *
     * @param data An array of characters containing hexidecimal digits
     * @return A byte array containing binary data decoded from
     *         the supplied char array.
     * @throws DecoderException Thrown if an odd number or illegal of characters
     *         is supplied
     */
    private static byte[] decodeHex(char[] data) throws Exception {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new Exception("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch A character to convert to an integer digit
     * @param index The index of the character in the source
     * @return An integer
     * @throws DecoderException Thrown if ch is an illegal hex character
     */
    private static int toDigit(char ch, int index) throws Exception {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new Exception("Illegal hexadecimal charcter " + ch + " at index " + index);
        }
        return digit;
    }
}
