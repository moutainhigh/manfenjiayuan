package com.mfh.framework.pay.alipay;

import com.mfh.framework.anlaysis.logger.ZLogger;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

public class SignUtils {
    public static final String ALGORITHM = "RSA";

    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * sign
     * @param content
     * @param privateKey
     * */
    public static String sign(String content, String privateKey) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
                    Base64.decode(privateKey));

            //java.security.spec.InvalidKeySpecException: java.lang.RuntimeException:
            // error:0c0890ba:ASN.1 encoding routines:asn1_check_tlen:WRONG_TAG
//            KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);

            //ClassCastException: com.android.org.bouncycastle.asn1.DLSequence cannot be
            // cast to com.android.org.bouncycastle.asn1.ASN1Integer
            KeyFactory keyf = KeyFactory.getInstance(ALGORITHM, "BC");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));

            byte[] signed = signature.sign();

            return Base64.encode(signed);
        } catch (Exception e) {

            //

            e.printStackTrace();
            ZLogger.e(e.toString());
        }

        return null;
    }

}
