package org.century.ksoap2;

import com.mfh.framework.anlaysis.logger.ZLogger;

import org.ksoap2.SoapEnvelope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by bingshanguxue on 5/10/16.
 */
public class ZHttpTransportSE {
    private int bufferLength = 262144;
    private String xmlVersionTag = "<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->";
    private HashMap prefixes = new HashMap();

    private static ZHttpTransportSE instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static ZHttpTransportSE getInstance() {
        if (instance == null) {
            synchronized (ZHttpTransportSE.class) {
                if (instance == null) {
                    instance = new ZHttpTransportSE();
                }
            }
        }
        return instance;
    }

    private byte[] createRequestData(SoapEnvelope envelope, String encoding){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(this.bufferLength);
            Object result = null;
            bos.write(xmlVersionTag.getBytes());
            ZLogger.d(String.format("createRequestData 1: %s", new String(bos.toByteArray())));

            ZKXmlSerializer xw = new ZKXmlSerializer();
            xw.setOutput(bos, encoding);
            ZLogger.d(String.format("createRequestData 2: %s", new String(bos.toByteArray())));

            Iterator keysIter = this.prefixes.keySet().iterator();
            while(keysIter.hasNext()) {
                String key = (String)keysIter.next();
                xw.setPrefix(key, (String)this.prefixes.get(key));
            }

            envelope.write(xw);
            xw.flush();
            ZLogger.d(String.format("createRequestData 3: %s", new String(bos.toByteArray())));

            bos.write(13);
            bos.write(10);
            bos.flush();
            ZLogger.d(String.format("createRequestData 4: %s", new String(bos.toByteArray())));

            byte[] result1 = bos.toByteArray();
            ZLogger.d(String.format("createRequestData 5: %s", new String(bos.toByteArray())));
            xw = null;
            bos = null;
            return result1;
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(String.format("createRequestData failed: %s", e.toString()));
            return null;
        }
    }

    public void printDump(SoapEnvelope envelope){
        byte[] requestData = createRequestData(envelope, "UTF-8");
        ZLogger.d(String.format("printDump: %s", new String(requestData)));
    }
}
