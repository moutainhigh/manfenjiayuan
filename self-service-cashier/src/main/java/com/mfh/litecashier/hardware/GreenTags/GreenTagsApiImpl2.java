package com.mfh.litecashier.hardware.GreenTags;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;

import org.century.GreenTagsApi;
import org.century.ksoap2.KSoapHelper;
import org.century.schemas.ArrayOfGoodsInfoEX;
import org.century.schemas.ArrayOfProperty;
import org.century.schemas.DataType;
import org.century.schemas.GoodStatus;
import org.century.schemas.GoodsInfoEX;
import org.century.schemas.GoodsInfoEXArray;
import org.century.schemas.ModelType;
import org.century.schemas.Property;
import org.century.schemas.ReaderInfoEX;
import org.century.schemas.ReaderStatus;
import org.century.schemas.TagInfoEX;
import org.century.schemas.TagStatus;
import org.ksoap2.HeaderProperty;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * 绿泰
 * Created by bingshanguxue on 5/3/16.
 */
public class GreenTagsApiImpl2 extends GreenTagsApi {

    public interface GreenTagsSyncListener{
        void syncSucceed(Date startCursor);
        void syncFailed(String msg);
    }

    public static class ESLPushGoodsInfoExPackResult{
        private boolean result = false;
        private Date cursor = null;

        public ESLPushGoodsInfoExPackResult(boolean result) {
            this.result = result;
        }

        public ESLPushGoodsInfoExPackResult(boolean result, Date cursor) {
            this.result = result;
            this.cursor = cursor;
        }

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public Date getCursor() {
            return cursor;
        }

        public void setCursor(Date cursor) {
            this.cursor = cursor;
        }
    }

    public static ESLPushGoodsInfoExPackResult ESLPushGoodsInfoExPack2(ArrayOfGoodsInfoEX arrayOfGoodsInfoEX,
                                                 Date startCursor)
            throws XmlPullParserException, IOException {
        ZLogger.df(String.format("准备推送%d个商品到ESL", arrayOfGoodsInfoEX.size()));
        GreenTagsApi.printDefault();

        //Step 1: Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK);

        //Add the property to request object(optional)
        GoodsInfoEXArray goodsInfoEXArray = new GoodsInfoEXArray();
        goodsInfoEXArray.setArrayGoodsInfoEx(arrayOfGoodsInfoEX);
        request.addProperty("goodsInfoExArray", goodsInfoEXArray);
//        request.addProperty("goodsInfoExArray", new ArrayOfGoodsInfoEX(arrayGoodsInfoEx));
        ZLogger.df(String.format("request: %s", request.toString()));

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
//        ZLogger.d(String.format("envelope: %s", envelope.toString()));

//        ZHttpTransportSE.getInstance().printDump(envelope);

        try {
            //Step 3: Create HTTP call object
            HttpTransportSE httpTransportSE = KSoapHelper.getHttpTransportSE(GreenTagsApi.URL);
            //Invole web service
            httpTransportSE.call(GreenTagsApi.SOAP_ACTION_ESLPushGoodsInfoExPack, envelope);

            ZLogger.d(String.format("requestDump: %s", httpTransportSE.requestDump));
            ZLogger.df(String.format("responseDump: %s", httpTransportSE.responseDump));
            //Get the response
            SoapObject response1 = (SoapObject) envelope.getResponse();
            ZLogger.df(String.format("response1: %s",
                    response1 != null ? response1.toString() : "[NULL]"));

//                ESLPushGoodsInfoExPackResponse{}
            SoapObject response2 = (SoapObject) envelope.bodyIn;
            ZLogger.df(String.format("response2: %s",
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

            return new ESLPushGoodsInfoExPackResult(true, startCursor);
        } catch (Exception e) {
            ZLogger.e(String.format("ESLPushGoodsInfoExPack failed: %s", e.toString()));
            return new ESLPushGoodsInfoExPackResult(false);
        }
    }


    public static boolean ESLPushGoodsInfoExPack(ArrayOfGoodsInfoEX arrayOfGoodsInfoEX,
                                                 Date startCursor)
            throws XmlPullParserException, IOException {
        ZLogger.df(String.format("准备推送%d个商品到ESL", arrayOfGoodsInfoEX.size()));
        GreenTagsApi.printDefault();

        //Step 1: Create request
        SoapObject request = KSoapHelper.createSoapRequest(GreenTagsApi.ESL_PUSHGOODSINFOEX_PACK);

        //Add the property to request object(optional)
        GoodsInfoEXArray goodsInfoEXArray = new GoodsInfoEXArray();
        goodsInfoEXArray.setArrayGoodsInfoEx(arrayOfGoodsInfoEX);
        request.addProperty("goodsInfoExArray", goodsInfoEXArray);
//        request.addProperty("goodsInfoExArray", new ArrayOfGoodsInfoEX(arrayGoodsInfoEx));
        ZLogger.df(String.format("request: %s", request.toString()));

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
        ZLogger.df(String.format("envelope: %s", envelope.toString()));

//        ZHttpTransportSE.getInstance().printDump(envelope);

        try {
            //Step 3: Create HTTP call object
            HttpTransportSE httpTransportSE = KSoapHelper.getHttpTransportSE(GreenTagsApi.URL);

            ArrayList headerPropertyArrayList = new ArrayList();
            headerPropertyArrayList.add(new HeaderProperty("Connection", "close"));

            //Invole web service
            httpTransportSE.call(GreenTagsApi.SOAP_ACTION_ESLPushGoodsInfoExPack,
                    envelope, headerPropertyArrayList);

            ZLogger.df(String.format("requestDump: %s", httpTransportSE.requestDump));
            ZLogger.df(String.format("responseDump: %s", httpTransportSE.responseDump));
            //Get the response
            SoapObject response1 = (SoapObject) envelope.getResponse();
            ZLogger.df(String.format("response1: %s",
                    response1 != null ? response1.toString() : "[NULL]"));

//                ESLPushGoodsInfoExPackResponse{}
            SoapObject response2 = (SoapObject) envelope.bodyIn;
            ZLogger.df(String.format("response2: %s",
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
            String cursor = TimeUtil.format(startCursor, TimeCursor.InnerFormat);
            SharedPreferencesManager.set(GreenTagsApi.PREF_GREENTAGS,
                    GreenTagsApi.PK_S_GREENTAGS_LASTCURSOR, cursor);

            ZLogger.df(String.format("保存价签同步时间：%s", cursor));
            return true;
        } catch (Exception e) {
            ZLogger.e(String.format("failed: %s", e.toString()));
            return false;
        }
    }

}

