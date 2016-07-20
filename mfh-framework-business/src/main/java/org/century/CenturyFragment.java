package org.century;

import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import org.century.ksoap2.KSoapHelper;
import org.century.schemas.ArrayOfGoodsInfoEX;
import org.century.schemas.GoodsInfoEX;
import org.century.schemas.QueryCondition;
import org.century.schemas.QueryType;
import org.century.schemas.ReaderInfoEX;
import org.century.schemas.TagInfoEX;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by bingshanguxue on 5/3/16.
 */
public abstract class CenturyFragment extends BaseFragment {
    protected TagInfoEX mTagInfoEX = new TagInfoEX();
    protected GoodsInfoEX mGoodsInfoEX = new GoodsInfoEX();

    public void bindDefaultTag2Goods(String goodsCode, String tagNo){
        GoodsInfoEX googsInfoEX = GoodsInfoEX.createDefault(goodsCode, false);
        TagInfoEX tagInfoEX = TagInfoEX.createDefault(tagNo);
//        tagInfoEX.tagId = 1;
//        propertyList[0] = new Property("name", StringUtils.genNonceChinease(4));
//        propertyList[1] = new Property("origin", StringUtils.genNonceChinease(4));

        ESLBindTag2GoodsAsyncTask task = new ESLBindTag2GoodsAsyncTask(tagInfoEX, googsInfoEX);
        task.execute();
    }

    public class ESLBindTag2GoodsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private TagInfoEX tagInfoEX = new TagInfoEX();
        private GoodsInfoEX googsInfoEX = new GoodsInfoEX();
        private ReaderInfoEX readerInfoEX = null;//new ReaderInfoEX();//deprecated

        public ESLBindTag2GoodsAsyncTask(TagInfoEX tagInfoEX, GoodsInfoEX googsInfoEX) {
            this.tagInfoEX = tagInfoEX;
            this.googsInfoEX = googsInfoEX;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            ZLogger.d("doInBackground");
//            getCountryCityByIp("http://www.manfenjiayuan.cn");
//            getCountryCityByIp("221.224.34.30");

            try {
                return GreenTagsApiImpl.ESLBindTag2Goods(tagInfoEX, googsInfoEX, readerInfoEX, null, null);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLBindTags2Goods failed, %s", e.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLBindTags2Goods failed, %s", e.toString()));
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            ZLogger.d("onPostExecute");
            DialogUtil.showHint("绑定" + (aBoolean ? "成功" : "失败"));
            hideProgressDialog();
        }


        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在绑定标签...", false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    public void queryGoods(String goodsCode){
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setStartTime("");
        queryCondition.setEndTime("");
        queryCondition.setQueryType(QueryType.Goods);
        queryCondition.setQueryConditionSql("GoodsCode like '%" + goodsCode + "%'");

        //Create instance for AsyncCallWS
        ESLQueryGoodsAsyncTask task = new ESLQueryGoodsAsyncTask(queryCondition);
        //Call execute
        task.execute();
    }

    public class ESLQueryGoodsAsyncTask extends AsyncTask<String, Void, Void> {
        private QueryCondition queryCondition;
        private GoodsInfoEX[] mGoodsInfoices;

        public ESLQueryGoodsAsyncTask(QueryCondition queryCondition) {
            this.queryCondition = queryCondition;
        }

        @Override
        protected Void doInBackground(String... params) {
            ZLogger.d("doInBackground");
//            getCountryCityByIp("http://www.manfenjiayuan.cn");
//            getCountryCityByIp("221.224.34.30");

            try {
                mGoodsInfoices = GreenTagsApiImpl.ESLQueryGoods(queryCondition);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsExInfo failed, %s", e.toString()));
                mGoodsInfoices = null;
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsExInfo failed, %s", e.toString()));
                mGoodsInfoices = null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ZLogger.d("onPostExecute");
            hideProgressDialog();


// pii=anyType{
// GoodsCode=4605319002644;
// GoodsID=0;
// Properties=anyType{
//      Property=anyType{DataType=Text; ID=1; PropertyName=GoodsCode; Value=4605319002644; };
//      Property=anyType{DataType=Text; ID=2; PropertyName=名称; Value=哇哈哈; };
//      Property=anyType{DataType=Text; ID=3; PropertyName=价格; Value=16.00; };
//      Property=anyType{DataType=Text; ID=4; PropertyName=单位; Value=瓶; };
//      Property=anyType{DataType=Text; ID=5; PropertyName=规格; Value=500ML; };
//      Property=anyType{DataType=Text; ID=6; PropertyName=产地; Value=上海; };
//      Property=anyType{DataType=Text; ID=7; PropertyName=是否促销; Value=YES; };
//      Property=anyType{DataType=Text; ID=8; PropertyName=开始时间; Value=anyType{}; };
//      Property=anyType{DataType=Text; ID=9; PropertyName=结束时间; Value=anyType{}; };
//      Property=anyType{DataType=Text; ID=10; PropertyName=促销价格; Value=anyType{}; };
// };
// Status=ALLCOMPLETE;
// labels=anyType{
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE836C_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=16000441; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=LT154EB; OKState=null; Status=NORMAL; TagID=0; TagNo=28000245; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE832N_A_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=24000021; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE832N_A_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=24012f50; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=LT154A; OKState=null; Status=NORMAL; TagID=0; TagNo=2a000201; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE836N_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=02200f14; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=LT420A_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=2e000255; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE819N_3B; OKState=null; Status=NORMAL; TagID=0; TagNo=de02ca; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE819N_3B; OKState=null; Status=NORMAL; TagID=0; TagNo=d201f3; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE843N_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=20010227; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
// };
// tagInfos=anyType{
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE836C_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=16000441; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=LT154EB; OKState=null; Status=NORMAL; TagID=0; TagNo=28000245; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=null; };
//      TagInfoEX=anyType{FnState=null; Humidity=0.0; LineStatus=false; LowpowerStatus=0; ModelType=TE832N_A_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=24000021; Temperature=0.0; TempleteID=-1; Voltage=0.0; goodsInfoEX=null; readerInfoEX=nul
            if (mGoodsInfoices != null && mGoodsInfoices.length > 0) {
                mGoodsInfoEX = mGoodsInfoices[0];
                DialogUtil.showHint(String.format("共找到%d个商品", mGoodsInfoices.length));
                for (GoodsInfoEX goodsInfoEX : mGoodsInfoices) {
                    ZLogger.d(goodsInfoEX.toString());
                }
            } else {
                DialogUtil.showHint("未找到商品");
                mGoodsInfoEX = null;
            }

        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品信息...", false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }


    public void queryTag(String tagNo){
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setQueryType(QueryType.Tag);
        queryCondition.setQueryConditionSql("TagNo like '%" + tagNo + "%'");

        //Create instance for AsyncCallWS
        ESLQueryTagEXAsyncTask task = new ESLQueryTagEXAsyncTask(queryCondition);
        //Call execute
        task.execute();
    }
    public class ESLQueryTagEXAsyncTask extends AsyncTask<String, Void, Void> {
        private QueryCondition queryCondition;
        private TagInfoEX[] mTagInfoices;

        public ESLQueryTagEXAsyncTask(QueryCondition queryCondition) {
            this.queryCondition = queryCondition;
        }

        @Override
        protected Void doInBackground(String... params) {
            ZLogger.d("doInBackground");
//            getCountryCityByIp("http://www.manfenjiayuan.cn");
//            getCountryCityByIp("221.224.34.30");

            try {
                mTagInfoices = GreenTagsApiImpl.ESLQueryTag(queryCondition);
            } catch (XmlPullParserException | IOException e) {
                mTagInfoices = null;
                e.printStackTrace();
                ZLogger.e(String.format("ESLQueryTag failed, %s", e.toString()));
            } catch (Exception e) {
                mTagInfoices = null;
                e.printStackTrace();
                ZLogger.e(String.format("ESLQueryTag failed, %s", e.toString()));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ZLogger.d("onPostExecute");
            hideProgressDialog();

            if (mTagInfoices != null && mTagInfoices.length > 0) {
                mTagInfoEX = mTagInfoices[0];
                DialogUtil.showHint(String.format("共找到%d个标签", mTagInfoices.length));
                for (TagInfoEX tagInfoEX : mTagInfoices) {
                    ZLogger.d(String.format("tagInfoEX:%s", tagInfoEX.getTagNo()));
//                    Property[] propertyList = tagInfoEX.getProperties();
//                    if (propertyList != null){
//                        for (Property property : propertyList){
//                            ZLogger.d(String.format("property: %d-%s", property.getId(), property.getValue()));
//                        }
//                    }
//                    else{
//                        ZLogger.d("属性为空");
//                    }
                }
            } else {
                mTagInfoEX = null;
                DialogUtil.showHint("未找到标签");
            }
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询标签信息...", false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    public class getCountryCityByIpAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            ZLogger.d("doInBackground");
//            getCountryCityByIp("http://www.manfenjiayuan.cn");
//            getCountryCityByIp("221.224.34.30");
            getCountryCityByIp("120.199.20.208");

//            HelloWorld();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ZLogger.d("onPostExecute");
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }

    }

    /**
     * 通过输入IP地址查询国家、城市、所有者等信息。没有注明国家的为中国
     *
     * @param theIpAddress 请输入标准IP格式：*.*.*.*，http://www.webxml.com.cn
     *                     输入参数：IP地址（自动替换 " 。" 为 "."），
     *                     返回数据： 一个一维字符串数组String(1)，String(0) = IP地址；String(1) = 查询结果或提示信息
     *                     <p/>
     *                     商业用户不能通过验证。联系我们：http://www.webxml.com.cn/
     *                     POST /WebServices/IpAddressSearchWebService.asmx HTTP/1.1
     *                     Host: www.webxml.com.cn
     *                     Content-Type: text/xml; charset=utf-8
     *                     Content-Length: length
     *                     SOAPAction: "http://WebXml.com.cn/getCountryCityByIp"
     *                     <?xml version="1.0" encoding="utf-8"?>
     *                     <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
     *                     <soap:Body>
     *                     <getCountryCityByIp xmlns="http://WebXml.com.cn/">
     *                     <theIpAddress>string</theIpAddress>
     *                     </getCountryCityByIp>
     *                     </soap:Body>
     *                     </soap:Envelope>
     */
    private void getCountryCityByIp(String theIpAddress) {
        String namespace = "http://WebXml.com.cn/";
        String methodName = "getCountryCityByIp";
        String url = "http://www.webxml.com.cn/WebServices/IpAddressSearchWebService.asmx";
        //SOAP_ACTION = NAMESPACE + METHOD_NAME;
        String soapAction = "http://WebXml.com.cn/getCountryCityByIp";//namespace + methodName;
        //Create request
        SoapObject request = new SoapObject(namespace, methodName);

        //Add the property to request object
//        request.addProperty(KSoapFactoryImpl.makePropertyInfo("theIpAddress", theIpAddress, String.class));
        request.addProperty("theIpAddress", theIpAddress);

        //Create a SOAP envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);

        //Create HTTP call object
        HttpTransportSE httpTransportSE = KSoapHelper.getHttpTransportSE(url);

        try {
            //Invole web service
            //SOAP 1.1
            httpTransportSE.call(soapAction, envelope);
            //HTTP POST
//            androidHttpTransport.call("http://www.webxml.com.cn/WebServices/IpAddressSearchWebService.asmx/getCountryCityByIp", envelope);
            //Get the response,I'm using a SoapPrimitive type, but you can also use a SoapObject
            // instance if the response from the web service is XML.
            // java.lang.ClassCastException: org.ksoap2.serialization.SoapObject
            // cannot be cast to org.ksoap2.serialization.SoapPrimitive
            SoapObject response = (SoapObject) envelope.getResponse();


            ZLogger.d(String.format("getCountryCityByIp.requestDump:\n%s", httpTransportSE.requestDump));
            ZLogger.d(String.format("getCountryCityByIp.responseDump:\n%s", httpTransportSE.responseDump));
            //namespace:http://www.w3.org/2001/XMLSchema
//            name:anyType
//            anyType{string=120.199.20.208; string=浙江省 移动; }
            ZLogger.d(String.format("getCountryCityByIp response: " +
                            "\nnamespace:%s" +
                            "\nname:%s" +
                            "\n%s",
                    response.getNamespace(), response.getName(), response.toString()));
//            ArrayList list = new ArrayList(response.getPropertyCount());
//            String[] lv_arr = new String[response.getPropertyCount()];
//            for (int i = 0; i < response.getPropertyCount(); i++) {
//                Object property = response.getProperty(i);
//                if (property instanceof SoapObject) {
//                    SoapObject countryObj = (SoapObject) property;
//                    String countryName = countryObj.getProperty("countryName").toString();
//                    list.add(countryName );
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
            //java.net.ConnectException: failed to connect to www.w3schools.com/37.61.54.158 (port 80): connect failed: ETIMEDOUT (Connection timed out)
            //SoapFault - faultcode: 'soap:Server' faultstring: '服务器无法处理请求。 ---> 未将对象引用设置到对象的实例。' faultactor: 'null' detail: ce.b@41ff1858
            ZLogger.e("getCountryCityByIp failed, " + e.toString());
        }
    }

    public class getSumOfTowIntsAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            ZLogger.d("doInBackground");
//            getCountryCityByIp("http://www.manfenjiayuan.cn");
            getSumOfTowInts(12, 34);
//            HelloWorld();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ZLogger.d("onPostExecute");
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }

    }

    private void getSumOfTowInts(int Operand1, int Operand2) {
//        scoolbag.somee.com/31.13.79.244 (port 80)
        String namespace = "http://www.niceald.in/";
        String methodName = "GetSumOfTwoInts";
        String url = "http://scoolbag.somee.com/service.asmx";
        //SOAP_ACTION = NAMESPACE + METHOD_NAME;
        String soapAction = "http://www.niceald.in/GetSumOfTwoInts";//namespace + methodName;
        //Create request
        SoapObject request = new SoapObject(namespace, methodName);

        //Add the property to request object
//        request.addProperty(KSoapFactoryImpl.makePropertyInfo("theIpAddress", theIpAddress, String.class));
        request.addProperty("Operand1", Operand1);
        request.addProperty("Operand2", Operand2);

        //Create a SOAP envelope
        SoapSerializationEnvelope envelope = KSoapHelper.getSoapSerializationEnvelope(request);

        //Create HTTP call object
        HttpTransportSE androidHttpTransport = KSoapHelper.getHttpTransportSE(url);

        try {
            //Invole web service
            //SOAP 1.1
            androidHttpTransport.call(soapAction, envelope);
            //HTTP POST
//            androidHttpTransport.call("http://www.webxml.com.cn/WebServices/IpAddressSearchWebService.asmx/getCountryCityByIp", envelope);
            //Get the response,I'm using a SoapPrimitive type, but you can also use a SoapObject instance if the response from the web service is XML.
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            ZLogger.d("getSumOfTowInts response");
            ZLogger.d(String.format("getSumOfTowInts response: %s-%s-%s", response.getNamespace(), response.getName(), response.toString()));
            //Assign it to fahren static variable
//            ZLogger.d(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            //java.net.ConnectException: failed to connect to www.w3schools.com/37.61.54.158 (port 80): connect failed: ETIMEDOUT (Connection timed out)
            //SoapFault - faultcode: 'soap:Server' faultstring: '服务器无法处理请求。 ---> 未将对象引用设置到对象的实例。' faultactor: 'null' detail: ce.b@41ff1858
            ZLogger.e("getSumOfTowInts failed, " + e.toString());
        }
    }



    public void pushDefaultGoodsInfoEx(String goodsCode){
        ESLPushGoodsInfoExAsyncTask task = new ESLPushGoodsInfoExAsyncTask(GoodsInfoEX.createDefault(goodsCode, true));
        task.execute();
    }

    public class ESLPushGoodsInfoExAsyncTask extends AsyncTask<String, Void, Void> {
        private GoodsInfoEX mGoodsInfoEX;

        public ESLPushGoodsInfoExAsyncTask(GoodsInfoEX mGoodsInfoEX) {
            this.mGoodsInfoEX = mGoodsInfoEX;
        }

        @Override
        protected Void doInBackground(String... params) {
            ZLogger.d("doInBackground");

            try {
                GreenTagsApiImpl.ESLPushGoodsInfoEx(mGoodsInfoEX.getGoodsCode());
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsInfoEx failed, %s", e.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsInfoEx failed, %s", e.toString()));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ZLogger.d("onPostExecute");
            hideProgressDialog();
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在批量推送商品信息...", false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    public void pushDefaultGoodsInfoPackEx(int goodsNumber){
        ArrayOfGoodsInfoEX arrayOfGoodsInfoEX = new ArrayOfGoodsInfoEX();
        for (int i = 0; i < goodsNumber; i++) {
            GoodsInfoEX googsInfoEX = GoodsInfoEX.createDefault(null, true);
            ZLogger.d(String.format("goodsInfoEx: %s", JSON.toJSONString(googsInfoEX)));

            arrayOfGoodsInfoEX.add(googsInfoEX);
        }

        //Create instance for AsyncCallWS
        ESLPushGoodsExInfoPackAsyncTask task = new ESLPushGoodsExInfoPackAsyncTask(arrayOfGoodsInfoEX);
        //Call execute
        task.execute();
    }

    public class ESLPushGoodsExInfoPackAsyncTask extends AsyncTask<String, Void, Void> {
        private ArrayOfGoodsInfoEX arrayOfGoodsInfoEX;
        public ESLPushGoodsExInfoPackAsyncTask(ArrayOfGoodsInfoEX arrayOfGoodsInfoEX) {
            this.arrayOfGoodsInfoEX = arrayOfGoodsInfoEX;
        }

        @Override
        protected Void doInBackground(String... params) {
            ZLogger.d("doInBackground");

            try {
                GreenTagsApiImpl.ESLPushGoodsInfoExPack(arrayOfGoodsInfoEX);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsInfoExPack failed, %s", e.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                ZLogger.e(String.format("ESLPushGoodsInfoExPack failed, %s", e.toString()));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ZLogger.d("onPostExecute");
            hideProgressDialog();
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在批量推送商品信息...", false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }
}
