package org.century.schemas;


import com.mfh.framework.core.utils.StringUtils;

import org.century.TableDefinition;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * 商品信息
 * Created by bingshanguxue on 4/21/16.
 */
public class GoodsInfoEX implements KvmSerializable, Serializable {

    public static final int TABLE_COLOUMNINDEX_GOODSCODE    = 1;//固定
    public static final int TABLE_COLOUMNINDEX_NAME         = 2;
    public static final int TABLE_COLOUMNINDEX_PRICE        = 3;
    public static final int TABLE_COLOUMNINDEX_UNIT         = 4;
//    public static final int TABLE_COLOUMNINDEX_ORIGIN       = 4;

    /**
     * 自定义类型(对象)所处的命名空间.
     * 从wsdl(xsd:schema节点targetNamespace属性)中可以找到
     *
     * 例如:
     * <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" attributeFormDefault="qualified" elementFormDefault="qualified"
     *         targetNamespace="http://model.ufologist.com">
     *     <xsd:complexType name="Foo">
     *         <xsd:sequence>
     *             <xsd:element minOccurs="0" name="id" type="xsd:int"/>
     *             <xsd:element minOccurs="0" name="name" nillable="true" type="xsd:string"/>
     *         </xsd:sequence>
     *     </xsd:complexType>
     * </xsd:schema>
     */
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";


    private String GoodsCode;//Code of merchandise [店内码(商品编码)]
    /**
     * 必须使用int类型, 因为当不设置此属性时, KSOAP2会得到默认值0.
     *
     * 如果是Integer类型, 未设置属性的情况下, KSOAP2只会得到null, 此时会报错
     * SoapFault - faultcode: 'soap:Server' faultstring: 'Illegal argument. For input string: ""' faultactor: 'null' detail: null
     */
    //    private int GoodsID;
    ArrayOfProperty Properties;//Pair of property ID and property value [属性值对]
//    private GoodStatus Status;
//    private TagInfoEX[] labels;
//    private TagInfoEX[] tagInfos;


    public String getGoodsCode() {
        return GoodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        GoodsCode = goodsCode;
    }
//
//    public int getGoodsID() {
//        return GoodsID;
//    }
//
//    public void setGoodsID(int goodsID) {
//        GoodsID = goodsID;
//    }

    public ArrayOfProperty getProperties() {
        return Properties;
    }

    public void setProperties(ArrayOfProperty properties) {
        Properties = properties;
    }


//    public GoodStatus getStatus() {
//        return Status;
//    }
//
//    public void setStatus(GoodStatus status) {
//        Status = status;
//    }

    //
//    public TagInfoEX[] getLabels() {
//        return labels;
//    }
//
//    public void setLabels(TagInfoEX[] labels) {
//        this.labels = labels;
//    }
//
//    public TagInfoEX[] getTagInfos() {
//        return tagInfos;
//    }
//
//    public void setTagInfos(TagInfoEX[] tagInfos) {
//        this.tagInfos = tagInfos;
//    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return getGoodsCode();
            case 1:
//                return getGoodsID();
//            case 2:
                return getProperties();
//            case 2:
//                return Status;
//            case 4:
//                return labels;
//            case 5:
//                return tagInfos;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 2;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i) {
            case 0:
                GoodsCode = o.toString();
                break;
            case 1:
//                GoodsID = Integer.parseInt(o.toString());
//                break;
//            case 2:
                Properties = (ArrayOfProperty) o;
                break;
//            case 2:
//            Status = (GoodStatus) o;
//                break;
//            case 4:
//                labels = (TagInfoEX[]) o;
//                break;
//            case 5:
//                tagInfos = (TagInfoEX[]) o;
//                break;
            default:
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        switch (i) {
            case 0:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "GoodsCode";
                // 必须设置对象属性所处的命名空间.
                // KSOAP2将对象序列化成SOAP XML时才会在对应的命名空间下.
                // 生成的XML类似:
                // <n0:foo i:type="n0:Foo" xmlns:n0="http://model.ufologist.com">
                //     <n0:id i:type="d:int">0</n0:id>
                // </n0:foo>
                //
                // 如果没有设置命名空间, 生成的XML类似(foo处于默认命名空间下明显不对):
                // <foo i:type="n0:Foo" xmlns:n0="http://model.ufologist.com">
                //     <id i:type="d:int">0</id>
                // </foo>
                // 通过KSOAP2调用WebService会返回错误信息
                // <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                //     <soap:Body>
                //         <soap:Fault>
                //             <faultcode>soap:Server</faultcode>
                //             <faultstring>Fault: java.lang.NullPointerException</faultstring>
                //         </soap:Fault>
                //     </soap:Body>
                // </soap:Envelope>
                // 抛出异常
                // SoapFault - faultcode: 'soap:Server' faultstring: 'Fault: java.lang.NullPointerException' faultactor: 'null' detail: null
                //
                // XXX 只要设置一个属性的namespace, 其他属性就可以不设置了
                propertyInfo.namespace = NAMESPACE;
                break;
            case 1:
//                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
//                propertyInfo.name = "GoodsID";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 2:
                propertyInfo.type = ArrayOfProperty.class;
                propertyInfo.name = "Properties";
                propertyInfo.namespace = NAMESPACE;
                break;
//            case 2:
//                propertyInfo.type = PropertyInfo.OBJECT_CLASS;
//                propertyInfo.name = "Status";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 4:
//                propertyInfo.type = PropertyInfo.VECTOR_CLASS;
//                propertyInfo.name = "labels";
//            propertyInfo.namespace = NAMESPACE;
//                break;
//            case 5:
//                propertyInfo.type = PropertyInfo.VECTOR_CLASS;
//                propertyInfo.name = "tagInfos";
//            propertyInfo.namespace = NAMESPACE;
//                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
//        return super.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tGoodsCode:").append(GoodsCode).append("\n");
//        sb.append("\tGoodsID:").append(GoodsID).append("\n");
        sb.append("\tProperties:[\t");
//        sb.append("\tStatus:").append(Status.value).append("\n");
        ArrayOfProperty arrayOfProperty = getProperties();
        if (arrayOfProperty != null) {
            for (int i = 0; i < arrayOfProperty.getPropertyCount(); i++) {
                Property property = arrayOfProperty.get(i);
                sb.append(property.toString());
                if (i < arrayOfProperty.getPropertyCount()-1) {
                    sb.append(",");
                }
            }
        }
        sb.append("\t]\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static GoodsInfoEX createDefault(String barcode, boolean propertyEnabled) {
        if (StringUtils.isEmpty(barcode)) {
            barcode = StringUtils.getNonceDecimalString(6);
        }
        GoodsInfoEX googsInfoEX = new GoodsInfoEX();
        googsInfoEX.setGoodsCode(barcode);

        //更新商品属性
        if (propertyEnabled){
            ArrayOfProperty propertyList = new ArrayOfProperty();
            propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_GOODSCODE, barcode));
            propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_NAME, StringUtils.genNonceChinease(4)));
            propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_PRICE, StringUtils.getNonceDecimalString(3)));
//        propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_NAME, StringUtils.genNonceChinease(4)));
//        propertyList.add(new Property(DataType.Text, TableDefinition.TB_GOODS_COLOUMNINDEX_GOODSCODE,
//                "GoodsCode", barcode));
//        propertyList.add(new Property(DataType.Text, TableDefinition.TB_GOODS_COLOUMNINDEX_NAME,
//                "name", StringUtils.genNonceChinease(4)));
//        propertyList.add(new Property(2, "origin", StringUtils.genNonceChinease(4)));
            googsInfoEX.setProperties(propertyList);
        }

//        googsInfoEX.setStatus(GoodStatus.ALLCOMPLETE);

        return googsInfoEX;
    }
}
