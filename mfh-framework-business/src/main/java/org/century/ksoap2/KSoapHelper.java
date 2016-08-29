package org.century.ksoap2;

import com.mfh.framework.anlaysis.logger.ZLogger;

import org.century.GreenTagsApi;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.Proxy;

/**
 * Created by bingshanguxue on 4/25/16.
 */
public class KSoapHelper {
    /**
     * Create request
     */
    public static SoapObject createSoapRequest(String namespace, String methodName) {
        //Create request
        return new SoapObject(namespace, methodName);
    }

    /**
     * Create request
     */
    public static SoapObject createSoapRequest(String methodName) {
        //Create request
        return new SoapObject(GreenTagsApi.WSDL_TARGET_NAMESPACE, methodName);
    }

    /**
     * 生成调用W    ebService方法的SOAP请求信息<br>
     * 创建SoapSerializationEnvelope对象时需要通过SoapSerializationEnvelope类的构造方法设置SOAP协 议的版本号。该版本号需要根据服务端WebService的版本号设置。
     * */
    public static SoapSerializationEnvelope getSoapSerializationEnvelope(SoapObject request) {
        return getSoapSerializationEnvelope(SoapEnvelope.VER11, request);
    }

    public static SoapSerializationEnvelope getSoapSerializationEnvelope(int soapVersion, SoapObject request) {
        // SoapEnvelope.VER11: SOAP 1.1
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(soapVersion);
        // set the envelope's dotNet property to true as the web service we'll be consuming
        // runs on Microsoft's .NET framework.
        envelope.dotNet = true;
        envelope.implicitTypes = true;//
        envelope.setAddAdornments(false);//
        envelope.bodyOut = request;
        //Set output SOAP object(necessary)
        envelope.setOutputSoapObject(request);

        return envelope;
    }

    /**
     * create an HTTP transport object
     * <?xml version="1.0" encoding="utf-8"?>
     * <?xml version="1.0" encoding="utf-16"?>
     */
    public static HttpTransportSE getHttpTransportSE(String url) {
        HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY, url, 60 * 1000);
        //// debug为true时调用httpTransport.requestDump/responseDump才有值,
        // 否则为null, 可以将生成的SOAP协议全部打印出来以供排错
        //this is optional, use it if you don't want to use a packet sniffer to check what the sent message was (httpTransport.requestDump)
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");

        return ht;
    }

    /**
     * 发送请求
     * */
    public static SoapObject call(SoapSerializationEnvelope envelope) {
        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(GreenTagsApi.URL);

        try { //Invole web service
            androidHttpTransport.call(GreenTagsApi.SOAP_ACTION_ESLQueryGoods, envelope);
            //Get the response
            return (SoapObject) envelope.getResponse();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            ZLogger.e(String.format("call failed : %s", e.toString()));
            return null;
        }
    }
}
