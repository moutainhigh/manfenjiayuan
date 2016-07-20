package org.century;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;

import org.century.ksoap2.KSoapHelper;
import org.century.ksoap2.ZHttpTransportSE;
import org.century.schemas.ArrayOfGoodsInfoEX;
import org.century.schemas.ArrayOfProperty;
import org.century.schemas.DataType;
import org.century.schemas.GoodStatus;
import org.century.schemas.GoodsInfoEX;
import org.century.schemas.GoodsInfoEXArray;
import org.century.schemas.ModelType;
import org.century.schemas.Property;
import org.century.schemas.QueryCondition;
import org.century.schemas.QueryType;
import org.century.schemas.ReaderInfoEX;
import org.century.schemas.ReaderStatus;
import org.century.schemas.TagInfoEX;
import org.century.schemas.TagStatus;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Date;

/**
 * 绿泰
 * Created by bingshanguxue on 5/3/16.
 */
public class GreenTagsApiImpl extends GreenTagsApi {
    /**
     * 批量推送商品信息（Push Merchandise in package）
     * 适用场景：批量提交商品信息到ESLWebService，相同的商品（goodscode相同），属性会被覆盖。
     *
     * @param goodsInfoExPack An array of Merchandise information [商品信息数组]。
     */
    public static void ESLPushGoodsInfoExPack(ArrayOfGoodsInfoEX arrayOfGoodsInfoEX) throws XmlPullParserException, IOException {
        ZLogger.d(String.format("准备推送%d个商品到ESL", arrayOfGoodsInfoEX.size()));
        GreenTagsApi.printDefault();

        //Step 1: Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK);

        //Add the property to request object(optional)
        GoodsInfoEXArray goodsInfoEXArray = new GoodsInfoEXArray();
        goodsInfoEXArray.setArrayGoodsInfoEx(arrayOfGoodsInfoEX);
        request.addProperty("goodsInfoExArray", goodsInfoEXArray);
//        request.addProperty("goodsInfoExArray", new ArrayOfGoodsInfoEX(arrayGoodsInfoEx));
        ZLogger.d(String.format("ESLPushGoodsInfoExPack.request: %s", request.toString()));

        //Step 2: Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodsInfoEXArray", GoodsInfoEXArray.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ArrayOfGoodsInfoEX", ArrayOfGoodsInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodsInfoEX", GoodsInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "Property", Property.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ArrayOfProperty", ArrayOfProperty.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "DataType", DataType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodStatus", GoodStatus.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagInfoEX", TagInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagStatus", TagStatus.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ModelType", ModelType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ReaderInfoEX", ReaderInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ReaderStatus", ReaderStatus.class);
        ZLogger.d(String.format("ESLPushGoodsInfoExPack.envelope: %s", envelope.toString()));

//        ZHttpTransportSE.getInstance().printDump(envelope);

        try {
            //Step 3: Create HTTP call object
            HttpTransportSE httpTransportSE = KSoapHelper.getHttpTransportSE(GreenTagsApi.URL);
            //Invole web service
            httpTransportSE.call(GreenTagsApi.SOAP_ACTION_ESLPushGoodsInfoExPack, envelope);

            ZLogger.d(String.format("ESLPushGoodsInfoExPack.requestDump: %s", httpTransportSE.requestDump));
            ZLogger.d(String.format("ESLPushGoodsInfoExPack.responseDump: %s", httpTransportSE.responseDump));
            //Get the response
            SoapObject response1 = (SoapObject) envelope.getResponse();
            ZLogger.d(String.format("ESLPushGoodsInfoExPack.response1: %s",
                    response1 != null ? response1.toString() : "[NULL]"));

//                ESLPushGoodsInfoExPackResponse{}
            SoapObject response2 = (SoapObject) envelope.bodyIn;
            ZLogger.d(String.format("ESLPushGoodsInfoExPack.response2: %s",
                    response2 != null ? response2.toString() : "[NULL]"));

            if (response2 != null) {
//                java.lang.RuntimeException: illegal property: ESLPushGoodsInfoExPackResult
//                SoapObject result = (SoapObject) response2.getProperty(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK + "Result");
//                ZLogger.d(String.format("%sResult: %s", GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK,
//                        result != null ? result.toString() : "NULL"));

//                SoapObject result2 = (SoapObject) response2.getProperty(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK + "Response");
//                ZLogger.d(String.format("%sResponse: %s", GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK,
//                        result2 != null ? result2.toString() : "NULL"));
            }
        } catch (Exception e) {
            ZLogger.e(String.format("ESLPushGoodsInfoExPack failed: %s", e.toString()));
        }
    }


    public static void ESLPushGoodsInfoExPack(ArrayOfGoodsInfoEX arrayOfGoodsInfoEX, Date startCursor)
            throws XmlPullParserException, IOException {
        ZLogger.d(String.format("准备推送%d个商品到ESL", arrayOfGoodsInfoEX.size()));
        GreenTagsApi.printDefault();

        //Step 1: Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK);

        //Add the property to request object(optional)
        GoodsInfoEXArray goodsInfoEXArray = new GoodsInfoEXArray();
        goodsInfoEXArray.setArrayGoodsInfoEx(arrayOfGoodsInfoEX);
        request.addProperty("goodsInfoExArray", goodsInfoEXArray);
//        request.addProperty("goodsInfoExArray", new ArrayOfGoodsInfoEX(arrayGoodsInfoEx));
        ZLogger.d(String.format("ESLPushGoodsInfoExPack.request: %s", request.toString()));

        //Step 2: Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodsInfoEXArray", GoodsInfoEXArray.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ArrayOfGoodsInfoEX", ArrayOfGoodsInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodsInfoEX", GoodsInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "Property", Property.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ArrayOfProperty", ArrayOfProperty.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "DataType", DataType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodStatus", GoodStatus.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagInfoEX", TagInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagStatus", TagStatus.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ModelType", ModelType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ReaderInfoEX", ReaderInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ReaderStatus", ReaderStatus.class);
        ZLogger.d(String.format("ESLPushGoodsInfoExPack.envelope: %s", envelope.toString()));

//        ZHttpTransportSE.getInstance().printDump(envelope);

        try {
            //Step 3: Create HTTP call object
            HttpTransportSE httpTransportSE = KSoapHelper.getHttpTransportSE(GreenTagsApi.URL);
            //Invole web service
            httpTransportSE.call(GreenTagsApi.SOAP_ACTION_ESLPushGoodsInfoExPack, envelope);

            ZLogger.d(String.format("ESLPushGoodsInfoExPack.requestDump: %s", httpTransportSE.requestDump));
            ZLogger.d(String.format("ESLPushGoodsInfoExPack.responseDump: %s", httpTransportSE.responseDump));
            //Get the response
            SoapObject response1 = (SoapObject) envelope.getResponse();
            ZLogger.d(String.format("ESLPushGoodsInfoExPack.response1: %s",
                    response1 != null ? response1.toString() : "[NULL]"));

//                ESLPushGoodsInfoExPackResponse{}
            SoapObject response2 = (SoapObject) envelope.bodyIn;
            ZLogger.d(String.format("ESLPushGoodsInfoExPack.response2: %s",
                    response2 != null ? response2.toString() : "[NULL]"));

            if (response2 != null) {
//                java.lang.RuntimeException: illegal property: ESLPushGoodsInfoExPackResult
//                SoapObject result = (SoapObject) response2.getProperty(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK + "Result");
//                ZLogger.d(String.format("%sResult: %s", GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK,
//                        result != null ? result.toString() : "NULL"));

//                SoapObject result2 = (SoapObject) response2.getProperty(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK + "Response");
//                ZLogger.d(String.format("%sResponse: %s", GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK,
//                        result2 != null ? result2.toString() : "NULL"));
            }

            // 保存批量上传订单时间
//            SharedPreferencesHelper.set(GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR,
//                    TimeUtil.format(startCursor, TimeCursor.InnerFormat));

        } catch (Exception e) {
            ZLogger.e(String.format("ESLPushGoodsInfoExPack failed: %s", e.toString()));
        }
    }
    /**
     * 推送商品
     */
    public static void ESLPushGoodsInfoEx(String barcode) throws XmlPullParserException, IOException {
        GoodsInfoEX goodsInfoEX = GoodsInfoEX.createDefault(barcode, true);
        GreenTagsApi.printDefault();

        //Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_PUSHGOODSINFOEX);

        //Add the property to request object
        request.addProperty("goodsInfoEx", goodsInfoEX);
        ZLogger.d(String.format("ESLPushGoodsInfoEx.request: %s", request.toString()));

        //Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodsInfoEX", GoodsInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "Property", Property.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ArrayOfProperty", ArrayOfProperty.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodStatus", GoodStatus.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagInfoEX", TagInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagStatus", TagStatus.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ModelType", ModelType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "DataType", DataType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ReaderInfoEX", ReaderInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ReaderStatus", ReaderStatus.class);
        ZLogger.d(String.format("ESLPushGoodsInfoEx.envelope: %s", envelope.toString()));

//        ZHttpTransportSE.getInstance().printDump(envelope);

        //Create HTTP call object
        HttpTransportSE httpTransportSE = KSoapHelper.getHttpTransportSE(GreenTagsApi.URL);

        //Invole web service
        httpTransportSE.call(GreenTagsApi.SOAP_ACTION_ESLPushGoodsInfoEx, envelope);
        //Get the response
        SoapObject response1 = (SoapObject) envelope.getResponse();
        ZLogger.d(String.format("ESLPushGoodsInfoEx.response1: %s",
                response1 != null ? response1.toString() : "[NULL]"));

//                ESLPushGoodsInfoExPackResponse{}
        SoapObject response2 = (SoapObject) envelope.bodyIn;
        ZLogger.d(String.format("ESLPushGoodsInfoEx.response2: %s",
                response2 != null ? response2.toString() : "[NULL]"));

        if (response2 != null) {
//                java.lang.RuntimeException: illegal property: ESLPushGoodsInfoExPackResult
//                SoapObject result = (SoapObject) response2.getProperty(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK + "Result");
//                ZLogger.d(String.format("%sResult: %s", GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK,
//                        result != null ? result.toString() : "NULL"));

//                SoapObject result2 = (SoapObject) response2.getProperty(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK + "Response");
//                ZLogger.d(String.format("%sResponse: %s", GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK,
//                        result2 != null ? result2.toString() : "NULL"));
        }
    }

    /**
     * 新增商品
     *
     * @param googsInfoEX Merchandise information [
     */
    private void ESLAddGoods(GoodsInfoEX googsInfoEX) {
        GreenTagsApi.printDefault();
        ZLogger.d("ESLAddGoods start");

        //Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_ADDGOODS);
        //Add the property to request object(optional)
        request.addProperty(KSoapFactoryImpl.makePropertyInfo("GoodsInfoEX", googsInfoEX, GoodsInfoEX.class));
//        request.addProperty("GoodsInfoEX", googsInfoEX);
        ZLogger.d(String.format("request: %s", request.toString()));

        //Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);

        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(GreenTagsApi.URL);

        try {
            //Invole web service
            androidHttpTransport.call(GreenTagsApi.SOAP_ACTION_ESLAddGoods, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
//            List COOKIE_HEADER = androidHttpTransport.getServiceConnection().getResponseProperties();
//            for (int i = 0; i < COOKIE_HEADER.size(); i++) {
//                String key = COOKIE_HEADER.get(i).getKey();
//                String value = COOKIE_HEADER.get(i).getValue();
//
//                if (key != null && key.equalsIgnoreCase("set-cookie")) {
//                    SoapRequests.SESSION_ID = value.trim();
//                    Log.v("SOAP RETURN", "Cookie :" + SoapRequests.SESSION_ID);
//                    break;
//                }
//            }

            ZLogger.d(String.format("response: %s-%s-%s", response.getNamespace(), response.getName(), response.toString()));
            //Assign it to fahren static variable
//            fahren = response.toString();
//            ZLogger.d(fahren);
        } catch (Exception e) {
            e.printStackTrace();
            //04-19 22:27:03.793 11951-12026/? I/bingshanguxue: java.net.ConnectException: failed to connect to www.w3schools.com/37.61.54.158 (port 80): connect failed: ETIMEDOUT (Connection timed out)
            ZLogger.d(e.toString());
        }
    }

    /**
     * Merchandise & Tag Binding [绑定商品与标签]
     * bool ESLBindTag2Goods(TagInfoEX tagInfoex,GoogsInfoEX goodsInfoex, ReaderIofoEX readerInfoex)
     *
     * @param tagInfoEX    Tag information [标签信息]
     * @param googsInfoEX  Merchandise Information [商品信息]
     * @param readerInfoEX Unnecessary [不用,参数作废]
     * @return 当 isUpdateTag 为 true 时,需要更新标签成功,才将绑定关系数据库写入,然后才能 返回 true;
     * 否则,当 isUpdateTag 为 false 时,只要将绑定关系写入数据库,便返回 true。
     */
    public static boolean ESLBindTag2Goods(TagInfoEX tagInfoEX, GoodsInfoEX googsInfoEX,
                                           ReaderInfoEX readerInfoEX, String shelve, String readerNo)
            throws IOException, XmlPullParserException {
        if (tagInfoEX == null || googsInfoEX == null) {
            ZLogger.d("ESLBindTag2Goods failed: 参数无效。");
            return false;
        }

        GreenTagsApi.printDefault();
        //Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_BINDTAGS2GOODS);

        //Add the property to request object
        request.addProperty("tagInfoEX", tagInfoEX);
        request.addProperty("goodsInfoEX", googsInfoEX);
        request.addProperty("readerInfoEX", readerInfoEX);
        request.addProperty("shelve", shelve);
        request.addProperty("readerNo", readerNo);

        ZLogger.d(String.format("ESLBindTag2Goods.request: %s", request.toString()));
        //Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);
        //添加本地数据类型与服务端数据类型的映射（在调用webService服务的HttpTransportSE对象调用call()方法之前）
//          第一个参数是服务端自定义类型参数所处的命名空间对应的引用，
//          第二个参数是此类型参数在服务端的参数名称，
//          第三个参数是客户端(即本地)该自定义类型的参数所对应的类型。
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagInfoEX", TagInfoEX.class);
//        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagStatus", TagStatus.class);
//        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ModelType", ModelType.class);
//        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "DataType", DataType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodsInfoEX", GoodsInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ArrayOfProperty", ArrayOfProperty.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "Property", Property.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ReaderInfoEX", ReaderInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ReaderStatus", ReaderStatus.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodStatus", GoodStatus.class);

        //Create HTTP call object
        HttpTransportSE httpTransportSE = KSoapHelper.getHttpTransportSE(GreenTagsApi.URL);

//        ZHttpTransportSE.getInstance().printDump(envelope);

        //Invole web service
        httpTransportSE.call(GreenTagsApi.SOAP_ACTION_ESLBindTag2Goods, envelope);
        ZLogger.d(String.format("ESLBindTag2Goods.requestDump: %s", httpTransportSE.requestDump));
        ZLogger.d(String.format("ESLBindTag2Goods.responseDump: %s", httpTransportSE.responseDump));

        //Get the response
        try {

//        java.lang.ClassCastException: org.ksoap2.serialization.SoapPrimitive cannot be cast to org.ksoap2.serialization.SoapObject
            SoapPrimitive response1 = (SoapPrimitive) envelope.getResponse();
            if (response1 == null){
                ZLogger.d("ESLBindTag2Goods, no response");
                return false;
            }
            ZLogger.d(String.format("ESLBindTag2Goods.response1: %s", response1.toString()));
            return Boolean.valueOf(response1.getValue().toString());
//            Object response2 = envelope.bodyIn;
//            if (response2 == null) {
//                ZLogger.d("ESLBindTag2Goods.response2: no response.");
//                return false;
//            }
//            if (response2 instanceof SoapFault) {
//                SoapFault soapFault = (SoapFault) response2;
//                ZLogger.d(String.format("ESLBindTag2Goods.response2: %s",
//                        soapFault.toString()));
//                return false;
//            } else {
//                SoapObject soapObject = (SoapObject) response2;
//                ZLogger.d(String.format("ESLBindTag2Goods.response2: %s",
//                        soapObject.toString()));
//
////                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
////                <s:Body>
////                <ESLBindTag2GoodsResponse xmlns="http://tempuri.org/">
////                <ESLBindTag2GoodsResult>true</ESLBindTag2GoodsResult>
////                </ESLBindTag2GoodsResponse>
////                </s:Body>
////                </s:Envelope>
//                SoapPrimitive result2 = (SoapPrimitive) soapObject.getProperty(GreenTagsApi.ESL_BINDTAGS2GOODS + "Result");
//                ZLogger.d(String.format("%sResult: %s", GreenTagsApi.ESL_BINDTAGS2GOODS,
//                        result2 != null ? result2.getValue() : "NULL"));
//                return Boolean.valueOf(result2.getValue().toString());
//            }
        } catch (Exception e) {
            ZLogger.e("ESLBindTag2Goods exception: " + e.toString());
            return false;
        }
    }

    /**
     * Delete binding relation [删除商品与标签的绑定]
     *
     * @param tagInfoEX   标签信息,null 表示删除所有 googsInfoEX 对应的绑定关系
     * @param googsInfoEX 商品信息,null 表示删除所有 tagInfoex 对应的绑定关系
     */
    public static void ESLDeleteBindTag2Goods(TagInfoEX tagInfoEX, GoodsInfoEX googsInfoEX) throws IOException, XmlPullParserException {
        GreenTagsApi.printDefault();
        ZLogger.d("ESLBindTag2Goods start");
        //Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_DELETEBINDTAGS2GOODS);

        //Add the property to request object
        request.addProperty(KSoapFactoryImpl.makePropertyInfo("tagInfoex", tagInfoEX, TagInfoEX.class));
        request.addProperty(KSoapFactoryImpl.makePropertyInfo("goodsInfoex", googsInfoEX, GoodsInfoEX.class));
//        request.addProperty(KSoapFactoryImpl.makePropertyInfo("ReaderInfoEX", readerInfoEX, ReaderInfoEX.class));
//        request.addProperty("TagInfoEX", tagInfoEX);
//        request.addProperty("GoodsInfoEX", googsInfoEX);
//        request.addProperty("ReaderInfoEX", readerInfoEX);
        ZLogger.d(String.format("request: %s", request.toString()));
        //Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);

        //Create HTTP call object
        HttpTransportSE androidHttpTransport = KSoapHelper.getHttpTransportSE(GreenTagsApi.URL);

        //Invole web service
        androidHttpTransport.call(GreenTagsApi.SOAP_ACTION_ESLDeleteBindTag2Goods, envelope);
        //Get the response
        SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
//            List COOKIE_HEADER = androidHttpTransport.getServiceConnection().getResponseProperties();
//            for (int i = 0; i < COOKIE_HEADER.size(); i++) {
//                String key = COOKIE_HEADER.get(i).getKey();
//                String value = COOKIE_HEADER.get(i).getValue();
//
//                if (key != null && key.equalsIgnoreCase("set-cookie")) {
//                    SoapRequests.SESSION_ID = value.trim();
//                    Log.v("SOAP RETURN", "Cookie :" + SoapRequests.SESSION_ID);
//                    break;
//                }
//            }
        ZLogger.d("androidHttpTransport: ",
                androidHttpTransport.toString());
        ZLogger.d(String.format("response: %s-%s-%s", response.getNamespace(),
                response.getName(), response.toString()));
    }

    /**
     * <wsdl:portType name="IEslService">...</wsdl:portType>
     * <p/>
     * 查询商品
     *
     * @param queryCondition 查询条件
     * @throws NullPointerException java.net.SocketTimeoutException
     */
    public static GoodsInfoEX[] ESLQueryGoods(QueryCondition queryCondition) throws IOException, XmlPullParserException {
        GreenTagsApi.printDefault();
        if (queryCondition == null) {
            ZLogger.d("ESLQueryGoods failed, 查询条件不能为空");
            return null;
        }

        //Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_QUERYGOODS);


        //Add the property to request object
//        request.addProperty("UserName", "admin");
//        request.addProperty("LoginName", "admin");
//        request.addProperty("Password", "123");
//        request.addProperty(KSoapFactoryImpl.makePropertyInfo("queryCondition",
//                queryCondition, QueryCondition.class, SOAP_ENTITYEX_NAMESPACE));
        request.addProperty("queryCondition", queryCondition);

        ZLogger.d(String.format("ESLQueryGoods.request: \n%s", request.toString()));
        //Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(GreenTagsApi.SOAP_VERSION, request);
//        Regarding the return type, if your web method returns a complex object (such as ours),
// you need to tell KSOAP how to handle the response. That is done with the following code:
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "QueryCondition", QueryCondition.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "QueryType", QueryType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "queryCondition", QueryCondition.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodsInfoEX", GoodsInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "Property", Property.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "DataType", DataType.class);
//        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ESLQueryGoodsResult", GoodsInfoEX[].class);
//        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ESLQueryGoodsResponse", GoodsInfoEX[].class);

//        ZHttpTransportSE.getInstance().printDump(envelope);

        //Create HTTP call object
        HttpTransportSE httpTransportSE = KSoapHelper.getHttpTransportSE(GreenTagsApi.URL);
        //Invole web service
        httpTransportSE.call(GreenTagsApi.SOAP_ACTION_ESLQueryGoods, envelope);
        // <v:Envelope xmlns:i="http://www.w3.org/2001/XMLSchema-instance" xmlns:d="http://www.w3.org/2001/XMLSchema" xmlns:c="http://schemas.xmlsoap.org/soap/encoding/" xmlns:v="http://schemas.xmlsoap.org/soap/envelope/">
        //     <v:Header />
        //     <v:Body>
        //         <helloFoo xmlns="http://communication.service.server" id="o0" c:root="1">
        //             <n0:foo i:type="n0:Foo" xmlns:n0="http://model.ufologist.com">
        //                 <n0:id i:type="d:int">0</n0:id>
        //                 <n0:name i:type="d:string">&#36828;&#31243;WebService&#26041;</n0:name>
        //             </n0:foo>
        //         </helloFoo>
        //     </v:Body>
        // </v:Envelope>
        ZLogger.d(String.format("ESLQueryGoods.requestDump: %s", httpTransportSE.requestDump));
        // <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        //     <soap:Body>
        //         <ns1:helloFooResponse xmlns:ns1="http://communication.service.server">
        //             <ns1:out>
        //                 <id xmlns="http://model.ufologist.com">5784348</id>
        //                 <name xmlns="http://model.ufologist.com">杩滅▼璋冪敤鍚刉ebService鏂规硶</name>
        //             </ns1:out>
        //         </ns1:helloFooResponse>
        //     </soap:Body>
        // </soap:Envelope>
        ZLogger.d(String.format("ESLQueryGoods.responseDump: %s", httpTransportSE.responseDump));
//        List COOKIE_HEADER = httpTransportSE.getServiceConnection().getResponseProperties();
//            for (int i = 0; i < COOKIE_HEADER.size(); i++) {
//                String key = COOKIE_HEADER.get(i);
////                String value = COOKIE_HEADER.get(i).getValue();
////
////                if (key != null && key.equalsIgnoreCase("set-cookie")) {
////                    SoapRequests.SESSION_ID = value.trim();
////                    Log.v("SOAP RETURN", "Cookie :" + SoapRequests.SESSION_ID);
////                    break;
////                }
//            }

        //Get the response
        SoapObject response1 = (SoapObject) envelope.getResponse();
        ZLogger.d(String.format("ESLQueryGoods.response1: %s",
                response1 != null ? response1.toString() : "NULL"));

//        java.lang.ClassCastException: org.ksoap2.serialization.SoapObject cannot be cast to org.ksoap2.serialization.SoapPrimitive
////        java.net.ConnectException: failed to connect to /192.161.191.1 (port 3128) after 60000ms:
////        isConnected failed: ECONNREFUSED (Connection refused)
//            return KSoapFactoryImpl.retrieveGoodsInfoEXArray(response);
//        } else {
//            return null;
//        }
        SoapObject response2 = (SoapObject) envelope.bodyIn;
        ZLogger.d(String.format("ESLQueryGoods.response2: %s",
                response2 != null ? response2.toString() : "NULL"));

        if (response2 != null) {
            for (int i = 0; i < response2.getPropertyCount(); i++) {
                SoapObject property = (SoapObject) response2.getProperty(i);
                if (property != null && property.getPropertyCount() > 0) {
                    Object propertyItem = property.getProperty(0);
                    ZLogger.d(String.format("%d-[%s]=%s", i, property.getName(),
                            propertyItem != null ? propertyItem.toString() : ""));
                } else {
                    ZLogger.d("empty property");
                }
            }
//            java.lang.RuntimeException: illegal property: ESLQueryGoodsResponse
//            SoapObject result1 = (SoapObject) response2.getProperty(GreenTagsApi.ESL_QUERYGOODS + "Response");
//            ZLogger.d(String.format("%sResponse: %s", GreenTagsApi.ESL_QUERYGOODS,
//                    result1 != null ? result1.toString() : "NULL"));
            SoapObject result = (SoapObject) response2.getProperty(GreenTagsApi.ESL_QUERYGOODS + "Result");
            ZLogger.d(String.format("%sResult: %s", GreenTagsApi.ESL_QUERYGOODS,
                    result != null ? result.toString() : "NULL"));

//        java.lang.RuntimeException: illegal property: ESLQueryGoodsResponse
//        SoapObject result2 = (SoapObject) response.getProperty(GreenTagsApi.ESL_QUERYGOODS + "Response");
//        ZLogger.d(String.format("%sResponse:\n%s", GreenTagsApi.ESL_QUERYGOODS,
//                result2 != null ? result2.toString() : ""));

            return KSoapFactoryImpl.retrieveGoodsInfoEXArray(result);
        }

        return null;
    }

    /**
     * 查询标签信息
     *
     * @param queryCondition 查询条件
     */
    public static TagInfoEX[] ESLQueryTagEX(QueryCondition queryCondition) throws IOException, XmlPullParserException {
        if (queryCondition == null) {
            ZLogger.d("ESLQueryTagEX failed, 查询条件不能为空");
            return null;
        }
        //Create request

        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_QUERYTAGEX);

        //Add the property to request object
        request.addProperty(KSoapFactoryImpl.makePropertyInfo("queryCondition", queryCondition, QueryCondition.class, SOAP_ENTITYEX_NAMESPACE));
//        request.addProperty("QueryCondition", queryCondition);
        ZLogger.d(String.format("request: %s", request.toString()));
        //Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);
//        Regarding the return type, if your web method returns a complex object (such as ours),
// you need to tell KSOAP how to handle the response. That is done with the following code:
        envelope.addMapping(GreenTagsApi.WSDL_TARGET_NAMESPACE, "GoodsInfoEX", GoodsInfoEX.class);

        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(GreenTagsApi.URL);
        //Invole web service
        androidHttpTransport.call(GreenTagsApi.SOAP_ACTION_ESLQueryTagEX, envelope);
        //Get the response
        SoapObject response = (SoapObject) envelope.getResponse();

        return KSoapFactoryImpl.retrieveTagInfoEXArray(response);
    }

    /**
     * 查询标签信息
     *
     * @param queryCondition 查询条件
     */
    public static TagInfoEX[] ESLQueryTag(QueryCondition queryCondition) throws IOException, XmlPullParserException {
        if (queryCondition == null) {
            ZLogger.d("ESLQueryTag failed, 查询条件不能为空");
            return null;
        }
        //Create request

        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_QUERYTAG);
        request.addProperty("UserName", "admin");
        request.addProperty("LoginName", "admin");
        request.addProperty("Password", "123");


        //Add the property to request object
//        request.addProperty(KSoapFactoryImpl.makePropertyInfo("queryCondition", queryCondition,
//                QueryCondition.class, SOAP_ENTITYEX_NAMESPACE));
        request.addProperty("queryCondition", queryCondition);
        ZLogger.d(String.format("request: %s", request.toString()));
        //Create envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);
//        Regarding the return type, if your web method returns a complex object (such as ours),
// you need to tell KSOAP how to handle the response. That is done with the following code:

        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "queryCondition", QueryCondition.class);

        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "QueryType", QueryType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "QueryCondition", QueryCondition.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagInfoEX", TagInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "ModelType", ModelType.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "TagStatus", TagStatus.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "GoodsInfoEX", GoodsInfoEX.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "Property", Property.class);
        envelope.addMapping(SOAP_ENTITYEX_NAMESPACE, "DataType", DataType.class);

//        ZHttpTransportSE.getInstance().printDump(envelope);

        //Create HTTP call object
        HttpTransportSE httpTransportSE = new HttpTransportSE(GreenTagsApi.URL);
        //Invole web service
        httpTransportSE.call(GreenTagsApi.SOAP_ACTION_ESLQueryTag, envelope);
        ZLogger.d(String.format("ESLQueryTag.requestDump: %s", httpTransportSE.requestDump));
        ZLogger.d(String.format("ESLQueryTag.responseDump: %s", httpTransportSE.responseDump));
        //Get the response

        SoapObject response1 = (SoapObject) envelope.getResponse();
        ZLogger.d(String.format("ESLQueryTag.response1: %s", response1 != null ? response1.toString() : "NULL"));

        //ESLQueryTagResponse{ESLQueryTagResult=null; }
        SoapObject response2 = (SoapObject) envelope.bodyIn;
        ZLogger.d(String.format("ESLQueryTag.response2: %s", response2 != null ? response2.toString() : "NULL"));
        if (response2 == null) {
            ZLogger.d("ESLQueryTag.response:响应为空。");
            return null;
        }

        SoapObject result = (SoapObject) response2.getProperty(GreenTagsApi.ESL_QUERYTAG + "Result");
        ZLogger.d(String.format("%sResult: %s", GreenTagsApi.ESL_QUERYTAG, result != null ? result.toString() : "NULL"));
//        SoapObject result2 = (SoapObject) response.getProperty(GreenTagsApi.ESL_QUERYTAG + "Response");
//        ZLogger.d(String.format("%sResponse:\n%s", GreenTagsApi.ESL_QUERYTAG, result2 != null ? result2.toString() : ""));

        return KSoapFactoryImpl.retrieveTagInfoEXArray(result);
    }


}

