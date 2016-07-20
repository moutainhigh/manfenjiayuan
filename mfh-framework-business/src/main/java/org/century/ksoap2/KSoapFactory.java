package org.century.ksoap2;

import org.ksoap2.serialization.PropertyInfo;

/**
 * Created by bingshanguxue on 5/10/16.
 */
public class KSoapFactory {
    /***
     * Property which holds input parameters
     */
    public static PropertyInfo makePropertyInfo(String name, Object value, Object type) {

        PropertyInfo propertyInfo = new PropertyInfo();
        //Set Name
        propertyInfo.setName(name);
        //Set Value
        propertyInfo.setValue(value);
        //Set dataType
        propertyInfo.setType(type);

        return propertyInfo;
    }

    public static PropertyInfo makePropertyInfo(String name, Object value, Object type, String namespace) {

        PropertyInfo propertyInfo = new PropertyInfo();
        //Set Name
        propertyInfo.setName(name);
        //Set Value
        propertyInfo.setValue(value);
        //Set dataType
        propertyInfo.setType(type);
        //Set namespace
        propertyInfo.setNamespace(namespace);

        return propertyInfo;
    }
}
