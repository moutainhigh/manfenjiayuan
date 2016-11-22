package org.century;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import org.ksoap2.SoapEnvelope;

/**
 * 绿泰电子价签API
 * Created by bingshanguxue on 4/21/16.
 */
public class GreenTagsApi {
    public static final String PREF_GREENTAGS = "pref_greentags";
    public static final String PK_S_GREENTAGS_IP = "pk_greentags_ip";       //主机IP
    public static final String PK_I_GREENTAGS_PORT = "pk_greentags_port";   //主机端口号
    public static final String PK_I_GREENTAGS_SOAPVERSION = "pk_greentags_soapversion";//SOAP版本号
    public static final String PK_S_GREENTAGS_LASTCURSOR = "pk_greentags_lastcursor";//更新游标

    public static String LOCAL_SERVER_IP = "192.168.16.90";//"192.168.1.18";////"112.74.83.158";//"192.168.3.29";//
    public static int LOCAL_PORT = 3128;
    public static int SOAP_VERSION = SoapEnvelope.VER11;
    public static String SERVICE_NAME = "EslCoreService";


    static {
        initialize();
    }

    /**
     * NAMESPACE（HOST）, must end with '/'.
     * <p/>
     * 自定义类型(对象)所处的命名空间.
     * <ul>
     * <li>definitions是WSDL文档的根元素，definitions还声明各命名空间。</li>
     * <li>从wsdl(xsd:schema节点targetNamespace属性)中可以找到</li>
     * <li>message(2N)描述通信消息的数据结构的抽象类型化定义，使用types的描述的类型来定义整个消息的数据结构。</li>
     * <li>portType(n)和operation描述服务和服务的方法。operation包括输入和输出（使用message的描述）</li>
     * <li>binding描述Web Services的通信协议。 <soap:binding/>描述使用SOAP协议，binding还描述Web Services的方法、输入、输出。</li>
     * </ul>
     * <p/>
     * <p/>
     * <wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
     * xmlns:xsd="http://www.w3.org/2001/XMLSchema"
     * xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
     * xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
     * xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
     * xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/"
     * xmlns:tns="http://tempuri.org/"
     * xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * xmlns:wsx="http://schemas.xmlsoap.org/ws/2004/09/mex"
     * xmlns:wsap="http://schemas.xmlsoap.org/ws/2004/08/addressing/policy"
     * xmlns:wsaw="http://www.w3.org/2006/05/addressing/wsdl"
     * xmlns:msc="http://schemas.microsoft.com/ws/2005/12/wsdl/contract"
     * xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy"
     * xmlns:wsa10="http://www.w3.org/2005/08/addressing"
     * xmlns:wsam="http://www.w3.org/2007/05/addressing/metadata"
     * name="EslService"
     * targetNamespace="http://tempuri.org/">
     * <p/>
     * <wsdl:types>
     * <xsd:schema targetNamespace="http://tempuri.org/Imports">
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd0" namespace="http://tempuri.org/"/>
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd1" namespace="http://schemas.microsoft.com/2003/10/Serialization/"/>
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd2" namespace="http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX.Tag"/>
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd3" namespace="http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX"/>
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd4" namespace="http://schemas.microsoft.com/2003/10/Serialization/Arrays"/>
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd5" namespace="http://schemas.datacontract.org/2004/07/CENTURY_ESL.ESL_Service"/>
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd6" namespace="http://schemas.datacontract.org/2004/07/System.Drawing"/>
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd7" namespace="http://schemas.datacontract.org/2004/07/System.Net"/>
     * <xsd:import schemaLocation="http://localhost:3128/EslCoreService?xsd=xsd8" namespace="http://schemas.datacontract.org/2004/07/System.Net.Sockets"/>
     * </xsd:schema>
     * ...
     * </wsdl:types>
     * <p/>
     * <wsdl:message name="IEslService_ESLQueryGoods_InputMessage">
     * <wsdl:part name="parameters" element="tns:ESLQueryGoods"/>
     * </wsdl:message>
     * <wsdl:message name="IEslService_ESLQueryGoods_OutputMessage">
     * <wsdl:part name="parameters" element="tns:ESLQueryGoodsResponse"/>
     * </wsdl:message>
     * <wsdl:message name="IEslService_ESLPushGoodsInfoExPack_InputMessage">
     * <wsdl:part name="parameters" element="tns:ESLPushGoodsInfoExPack"/>
     * </wsdl:message>
     * <wsdl:message name="IEslService_ESLPushGoodsInfoExPack_OutputMessage">
     * <wsdl:part name="parameters" element="tns:ESLPushGoodsInfoExPackResponse"/>
     * </wsdl:message>
     * <wsdl:message name="IEslService_ESLBindTag2Goods_InputMessage">
     * <wsdl:part name="parameters" element="tns:ESLBindTag2Goods"/>
     * </wsdl:message>
     * <wsdl:message name="IEslService_ESLBindTag2Goods_OutputMessage">
     * <wsdl:part name="parameters" element="tns:ESLBindTag2GoodsResponse"/>
     * </wsdl:message>
     * <p/>
     * <wsdl:portType name="IEslService">
     * <wsdl:operation name="ESLQueryGoods">
     * <wsdl:input wsaw:Action="http://tempuri.org/IEslService/ESLQueryGoods"
     * message="tns:IEslService_ESLQueryGoods_InputMessage"/>
     * <wsdl:output wsaw:Action="http://tempuri.org/IEslService/ESLQueryGoodsResponse"
     * message="tns:IEslService_ESLQueryGoods_OutputMessage"/>
     * </wsdl:operation>
     * <wsdl:operation name="ESLPushGoodsInfoExPack">
     * <wsdl:input wsaw:Action="http://tempuri.org/IEslService/ESLPushGoodsInfoExPack" message="tns:IEslService_ESLPushGoodsInfoExPack_InputMessage"/>
     * <wsdl:output wsaw:Action="http://tempuri.org/IEslService/ESLPushGoodsInfoExPackResponse" message="tns:IEslService_ESLPushGoodsInfoExPack_OutputMessage"/>
     * </wsdl:operation>
     * ...
     * </wsdl:portType>
     * <p/>
     * <wsdl:binding name="BasicHttpBinding_IEslService" type="tns:IEslService">
     * <soap:binding transport="http://schemas.xmlsoap.org/soap/http"/>
     * <wsdl:operation name="ESLQueryGoods">
     * <soap:operation soapAction="http://tempuri.org/IEslService/ESLQueryGoods" style="document"/>
     * <wsdl:input>
     * <soap:body use="literal"/>
     * </wsdl:input>
     * <wsdl:output>
     * <soap:body use="literal"/>
     * </wsdl:output>
     * </wsdl:operation>
     * <wsdl:operation name="ESLPushGoodsInfoExPack">
     * <soap:operation soapAction="http://tempuri.org/IEslService/ESLPushGoodsInfoExPack" style="document"/>
     * <wsdl:input>
     * <soap:body use="literal"/>
     * </wsdl:input>
     * <wsdl:output>
     * <soap:body use="literal"/>
     * </wsdl:output>
     * </wsdl:operation>
     * ...
     * <p/>
     * </wsdl:binding>
     * <p/>
     * <wsdl:service name="EslService">
     * <wsdl:port name="BasicHttpBinding_IEslService" binding="tns:BasicHttpBinding_IEslService">
     * <soap:address location="http://localhost:3128/EslCoreService"/>
     * </wsdl:port>
     * ...</wsdl:service>
     * ...
     * </wsdl:definitions>
     */
    public static String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    //URL, eg. http://localhost:23027/SampleService/Service.asmx.
    //"http://192.168.1.100:8080/HelloWorldWebService/SayHelloService?WSDL";
    public static String URL = String.format("http://%s:%d/EslCoreService", LOCAL_SERVER_IP, LOCAL_PORT);//?wsdl
    //METHOD_NAME
    public static String ESL_BINDTAGS2GOODS = "ESLBindTag2Goods";//绑定商品与标签
    public static String ESL_DELETEBINDTAGS2GOODS = "ESLDeleteBindTag2Goods";//删除商品与标签的绑定
    public static String ESL_PUSHGOODSINFOEX = "ESLPushGoodsInfoEx";//推送商品信息
    public static String ESL_PUSHGOODSINFOEX_PACK = "ESLPushGoodsInfoExPack";//批量推送商品信息
    public static String ESL_ADDGOODS = "ESLAddGoods";
    /**
     * 查询商品信息 & 请求/响应模式
     * <p/>
     * <wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/" xmlns:tns="http://tempuri.org/" xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" xmlns:wsx="http://schemas.xmlsoap.org/ws/2004/09/mex" xmlns:wsap="http://schemas.xmlsoap.org/ws/2004/08/addressing/policy" xmlns:wsaw="http://www.w3.org/2006/05/addressing/wsdl" xmlns:msc="http://schemas.microsoft.com/ws/2005/12/wsdl/contract" xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy" xmlns:wsa10="http://www.w3.org/2005/08/addressing" xmlns:wsam="http://www.w3.org/2007/05/addressing/metadata" name="EslService" targetNamespace="http://tempuri.org/">
     * <wsdl:service name="EslService">
     * <wsdl:port name="BasicHttpBinding_IEslService" binding="tns:BasicHttpBinding_IEslService">
     * <soap:address location="http://192.168.1.18:3128/EslCoreService"/>
     * </wsdl:port>
     * </wsdl:service>
     * <p/>
     * <p/>
     * binding---一个endpoint的实际数据格式说明，一个binding元素定义如何将一个抽象消息映射到一个具体数据格式。该元素指明诸如参数顺序，返回值等信息。
     * <wsdl:binding name="BasicHttpBinding_IEslService" type="tns:IEslService">
     * <wsdl:operation name="ESLQueryGoods">
     * <soap:operation soapAction="http://tempuri.org/IEslService/ESLQueryGoods" style="document"/>
     * <wsdl:input>
     * <soap:body use="literal"/>
     * </wsdl:input>
     * <wsdl:output>
     * <soap:body use="literal"/>
     * </wsdl:output>
     * </wsdl:operation>
     * </wsdl:binding>
     * <p/>
     * <p/>
     * portType标签用来描述整个的web services，portType的name属性即为这个web services类的类名。这个标签下包含了所有的可用方法，每个operation标签表示一个方法。
     * <wsdl:portType name="IEslService">
     * 每一个operation 标签表示web services里的一个webmethod方法，operation标签的name属性是这个webmethod的方法名。
     * <wsdl:operation name="ESLQueryGoods">
     * Input和output标签分别表示一个operation（webmethod方法）的输入和输出的参数集合，这里叫做消息，不管输入参数有几个，每个参数有多么复杂，只有一个表示这些输入参数的消息，就是input标签的message属性表示的那个消息。对于输出消息也一样。
     * <wsdl:input wsaw:Action="http://tempuri.org/IEslService/ESLQueryGoods" message="tns:IEslService_ESLQueryGoods_InputMessage"/>
     * <wsdl:output wsaw:Action="http://tempuri.org/IEslService/ESLQueryGoodsResponse" message="tns:IEslService_ESLQueryGoods_OutputMessage"/>
     * </wsdl:operation>
     * </wsdl:portType>
     * <p/>
     * <p/>
     * 方法的输入输出参数都用一个消息来表示，message标签表示一个这样的消息，message标签按下面有个part标签，用来具体指示这个消息在schema中的类型，类型以element形式表现出来，即part标签的element属性指定的那个element。
     * 对于输入参数消息，part标签的element属性命名同webmethod方法名。
     * 对于输出参数消息，part标签的element属性命名同webmethod方法名 + response。
     * 表示类型的element都被集中放置在types标签内。
     * <wsdl:message name="IEslService_ESLQueryGoods_InputMessage">
     * <wsdl:part name="parameters" element="tns:ESLQueryGoods"/>
     * </wsdl:message>
     * <wsdl:message name="IEslService_ESLQueryGoods_OutputMessage">
     * <wsdl:part name="parameters" element="tns:ESLQueryGoodsResponse"/>
     * </wsdl:message>
     * <p/>
     * <p/>
     * 此标签用来描述所有webmethod所要用到的类型，都以element来描述类型
     * <wsdl:types>
     * <xs:element name="ESLQueryGoods">
     * <xs:complexType>
     * <xs:sequence>
     * <xs:element xmlns:q28="http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX" minOccurs="0" name="queryCondition" nillable="true" type="q28:QueryCondition"/>
     * </xs:sequence>
     * </xs:complexType>
     * </xs:element>
     * </wsdl:types>
     * <p/>
     * </wsdl:definitions>
     * <p/>
     * <p/>
     * <p/>
     * <p/>
     * public ESLQueryGoods(QueryCondition queryCondition)
     */

    public static String ESL_QUERYGOODS = "ESLQueryGoods";
    public static String ESL_QUERYTAGEX = "ESLQueryTagEX";//查询标签信息
    public static String ESL_QUERYTAG = "ESLQueryTag";//查询标签信息
    //SOAP_ACTION = NAMESPACE + METHOD_NAME;
    public static String SOAP_ACTION_ESLBindTag2Goods = WSDL_TARGET_NAMESPACE + "IEslService/" + ESL_BINDTAGS2GOODS;
    public static String SOAP_ACTION_ESLDeleteBindTag2Goods = WSDL_TARGET_NAMESPACE + "IEslService/" + ESL_DELETEBINDTAGS2GOODS;
    public static String SOAP_ACTION_ESLPushGoodsInfoEx = WSDL_TARGET_NAMESPACE + "IEslService/" + ESL_PUSHGOODSINFOEX;
    public static String SOAP_ACTION_ESLPushGoodsInfoExPack = WSDL_TARGET_NAMESPACE + "IEslService/" + ESL_PUSHGOODSINFOEX_PACK;
    public static String SOAP_ACTION_ESLAddGoods = WSDL_TARGET_NAMESPACE + "IEslService/" + ESL_ADDGOODS;
    public static String SOAP_ACTION_ESLQueryGoods = WSDL_TARGET_NAMESPACE + "IEslService/" + ESL_QUERYGOODS;
    public static String SOAP_ACTION_ESLQueryTagEX = WSDL_TARGET_NAMESPACE + "IEslService/" + ESL_QUERYTAGEX;
    public static String SOAP_ACTION_ESLQueryTag = WSDL_TARGET_NAMESPACE + "IEslService/" + ESL_QUERYTAG;

    public static final String SOAP_ENTITYEX_NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";

    /**
     * 初始化
     */
    public static void initialize() {
        LOCAL_SERVER_IP = SharedPrefesManagerFactory
                .getString(PREF_GREENTAGS, PK_S_GREENTAGS_IP, "");
        LOCAL_PORT = SharedPrefesManagerFactory
                .getInt(PREF_GREENTAGS, PK_I_GREENTAGS_PORT, 3128);
        SOAP_VERSION = SharedPrefesManagerFactory
                .getInt(PREF_GREENTAGS, PK_I_GREENTAGS_SOAPVERSION, SoapEnvelope.VER11);
        URL = String.format("http://%s:%d/EslCoreService", LOCAL_SERVER_IP, LOCAL_PORT);
    }

    public static boolean validate() {
        return !StringUtils.isEmpty(LOCAL_SERVER_IP);
    }

    public static void printDefault() {
        ZLogger.d(String.format("GreenTagsApi:\nURL = %s\nnamespace = %s",
                URL, WSDL_TARGET_NAMESPACE));
    }
}
