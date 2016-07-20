package org.century;

import com.mfh.framework.core.logger.ZLogger;

import org.century.ksoap2.KSoapFactory;
import org.century.schemas.ArrayOfProperty;
import org.century.schemas.GoodsInfoEX;
import org.century.schemas.Property;
import org.century.schemas.TagInfoEX;
import org.ksoap2.serialization.SoapObject;

import java.util.Vector;

/**
 * Created by bingshanguxue on 4/21/16.
 */
public class KSoapFactoryImpl extends KSoapFactory{

    public static void fillMapping(){

    }

    /**
     * We must remember to call initMapping before even tring to serialize or deserialize
     * xxx class and we must remember to call initMapping for every class we will use. A custom place to call it, is in the method OnCreate of the main activity
     * */
    public static void initMapping(){
    }


    /**
     * @param soap - represents the entering Soap object
     * @return returns the list of categories extracted from the response
     */
    public static GoodsInfoEX[] retrieveGoodsInfoEXArray(SoapObject soap) {
        if (soap == null) {
            ZLogger.d("retrieveGoodsInfoEXArray failed: 数据为空");
            return null;
        }

        try {
            ZLogger.d(String.format("retrieveGoodsInfoEXArray: %s",  soap.toString()));
            GoodsInfoEX[] categories = new GoodsInfoEX[soap.getPropertyCount()];
            for (int i = 0; i < categories.length; i++) {
                Object object = soap.getProperty(i);
                if (object instanceof SoapObject) {
                    GoodsInfoEX goodsInfoEX = retrieveGoodsInfoEX((SoapObject) object);
                    if (goodsInfoEX != null) {
                        categories[i] = goodsInfoEX;
                    }
                } else {
                    ZLogger.d("skip pii not SoapObject");
                }
            }

            return categories;
        } catch (Exception e) {
            ZLogger.e("retrieveGoodsInfoEXArray failed, " + e.toString());
            return null;
        }
    }

    public static GoodsInfoEX retrieveGoodsInfoEX(SoapObject soap) {
        if (soap == null) {
            return null;
        }
        GoodsInfoEX goodsInfoEX = new GoodsInfoEX();
        try {

            ZLogger.d(String.format("retrieveGoodsInfoEX: %s", soap.toString()));
            goodsInfoEX.setGoodsCode(soap.getProperty(0).toString());
//            goodsInfoEX.setGoodsID(Integer.valueOf(soap.getProperty(1).toString()));
//            ArrayOfProperty arrayOfProperty = new ArrayOfProperty();
//            arrayOfProperty.setProperty(0, parsePropertyArray((SoapObject) soap.getProperty(2)));
            ArrayOfProperty propertyVector = new ArrayOfProperty();
            propertyVector.addAll(parsePropertyList((SoapObject) soap.getProperty(2)));
            goodsInfoEX.setProperties(propertyVector);
        } catch (Exception e) {
            ZLogger.e("retrieveGoodsInfoEX failed, " + e.toString());
        }

        return goodsInfoEX;
    }

    public static Vector<Property> parsePropertyList(SoapObject soap) {
        if (soap == null) {
            return null;
        }

        try {
            ZLogger.d(String.format("parsePropertyArray: %s", soap.toString()));
            Vector<Property> propertyList = new Vector<>();
            for (int i = 0; i < soap.getPropertyCount(); i++) {
                Object object = soap.getProperty(i);
                if (object instanceof SoapObject) {
                    propertyList.add(parseProperty((SoapObject) object));
                } else {
                    ZLogger.d("skip pii not SoapObject");
                }
            }

            return propertyList;
        } catch (Exception e) {
            ZLogger.e("parsePropertyArray failed, " + e.toString());
            return null;
        }
    }

    /**
     * Property=anyType{DataType=Text; ID=1; PropertyName=GoodsCode; Value=4605319002644; };
     */
    public static Property[] parsePropertyArray(SoapObject soap) {
        if (soap == null) {
            return null;
        }

        try {
            ZLogger.d(String.format("parsePropertyArray: %s", soap.toString()));
            Property[] properties = new Property[soap.getPropertyCount()];
            for (int i = 0; i < properties.length; i++) {
                Object object = soap.getProperty(i);
                if (object instanceof SoapObject) {
                    properties[i] = parseProperty((SoapObject) object);
                } else {
                    ZLogger.d("skip pii not SoapObject");
                }
            }

            return properties;
        } catch (Exception e) {
            ZLogger.e("parsePropertyArray failed, " + e.toString());
            return null;
        }
    }

    /**
     * anyType{DataType=Text; ID=3; PropertyName=价格; Value=anyType{}; }
     * */
    public static Property parseProperty(SoapObject soap) {
        if (soap == null) {
            return null;
        }
        Property property = new Property();
        try {
            ZLogger.d(String.format("parseProperty:  %s", soap.toString()));
            // TODO: 5/4/16
//            java.lang.ClassCastException: org.ksoap2.serialization.SoapPrimitive cannot be cast to org.century.schemas.DataType
            Object dataTypeObject = soap.getProperty(0);
            property.ID = Integer.valueOf(soap.getProperty(1).toString());
            property.setValue(soap.getProperty(3).toString());
        } catch (Exception e) {
            ZLogger.e("parseProperty failed, " + e.toString());
        }

        return property;
    }


    /**
     * anyType{FnState=null; Humidity=0; LineStatus=false; LowpowerStatus=0; ModelType=TE843N_4B; OKState=null; Status=NORMAL; TagID=0; TagNo=20010227; Temperature=0; TempleteID=-1; Voltage=0; goodsInfoEX=anyType{GoodsCode=4605319002644; GoodsID=0; Properties=anyType{Property=anyType{DataType=Text; ID=1; PropertyName=GoodsCode; Value=4605319002644; }; Property=anyType{DataType=Text; ID=2; PropertyName=名称; Value=哇哈哈; }; Property=anyType{DataType=Text; ID=3; PropertyName=价格; Value=16.00; }; Property=anyType{DataType=Text; ID=4; PropertyName=单位; Value=瓶; }; Property=anyType{DataType=Text; ID=5; PropertyName=规格; Value=500ML; }; Property=anyType{DataType=Text; ID=6; PropertyName=产地; Value=上海; }; Property=anyType{DataType=Text; ID=7; PropertyName=是否促销; Value=YES; }; Property=anyType{DataType=Text; ID=8; PropertyName=开始时间; Value=anyType{}; }; Property=anyType{DataType=Text; ID=9; PropertyName=结束时间; Value=anyType{}; }; Property=anyType{DataType=Text; ID=10; PropertyName=促销价格; Value=anyType{}; }; }; Status=ALLCOMPLETE; labels=anyType{}; tagInfos=anyType{}; }; readerInfoEX=null; }
     * @param soap - represents the entering Soap object
     * @return returns the list of categories extracted from the response
     */
    public static TagInfoEX[] retrieveTagInfoEXArray(SoapObject soap) {
        if (soap == null) {
            ZLogger.d("retrieveTagInfoEXArray failed: 数据为空");
            return null;
        }

        try {
            ZLogger.d(String.format("retrieveTagInfoEXArray: %s-%s-%s", soap.getNamespace(),
                    soap.getName(), soap.toString()));
            TagInfoEX[] categories = new TagInfoEX[soap.getPropertyCount()];
            for (int i = 0; i < categories.length; i++) {
                Object object = soap.getProperty(i);
                if (object instanceof SoapObject) {
//            category.Description = pii.getProperty(2).toString();
                    categories[i] = retrieveTagInfoEX((SoapObject) object);;
                } else {
                    ZLogger.d("skip pii not SoapObject");
                }
            }
            return categories;
        } catch (Exception e) {
            return null;
        }

    }

    public static TagInfoEX retrieveTagInfoEX(SoapObject soap) {
        if (soap == null) {
            return null;
        }
        TagInfoEX tagInfoEX = new TagInfoEX();
        try {
            ZLogger.d(String.format("retrieveTagInfoEX: namespace:%s\nname:%s\n%s", soap.getNamespace(),
                    soap.getName(), soap.toString()));

            tagInfoEX.setTagNo(soap.getProperty(8).toString());
//                    category.tagNo = pii.getProperty(1).toString();
//            category.Name = pii.getProperty(1).toString();

//            tagInfoEX.goodscode = soap.getProperty(0).toString();
//            tagInfoEX.goodsid = Integer.valueOf(soap.getProperty(1).toString());
//            tagInfoEX.properties = parsePropertyArray((SoapObject) soap.getProperty(2));
//            category.Name = pii.getProperty(1).toString();
//            category.Description = pii.getProperty(2).toString();
        } catch (Exception e) {
            ZLogger.e("retrieveGoodsInfoEX failed, " + e.toString());
        }

        return tagInfoEX;
    }

}
