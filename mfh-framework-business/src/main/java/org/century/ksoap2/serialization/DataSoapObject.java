/*
 * Copyright (c) 2010 Tiziana Cimoli, t.cimoli@gmail.com, Cagliari, Italy
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.ksoap2.serialization;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tiziana Cimoli
 * 
 */
public class DataSoapObject extends SoapObject {

	public DataSoapObject(String namespace, String name) {
		super(namespace, name);
	}

	/* ***********************************************************************************************************************
	 * Per convertire dall'oggetto "vero" al SoapObject: viene ridefinito il
	 * metodo getPropertyCount, perche' e' il primo ad essere invocato da KSOAP2
	 * per scrivere l'oggetto sulla SoapEnveloppe. Qui dentro viene invocata la
	 * init() per sincronizzare l'oggetto con il SoapObject da scrivere.
	 */
	public int getPropertyCount() {
		init();
		return properties.size();
	}

	/*
	 * Init() scrive tutti i campi pubblici dell'oggetto dentro il vettore
	 * SoapObject.property. Questo metodo NON richiama ricorsivamente la init
	 * degli oggetti SoapObject che l'oggetto contiene, perche' cio' viene gia'
	 * fatto dalla write ZSoapSerializationEnvelope.writeElement
	 * 
	 * Attenzione che i campi vengono scritti in ordine sparso (la
	 * getDeclaredField non mantiene l'ordine) e cio' e' male se il web service
	 * si aspetta i campi "in ordine".
	 */
	public void init() {
		Field[] fields = this.getClass().getFields(); //DeclaredFields();
		try {
			for (Field field : fields) {
				try {
					if (field.get(this) != null)
						addProperty(namespace, field.getName(), field.get(this));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/* *****************************************************************************************************************************************
	 * Per convertire dal SoapObject all' ogetto "vero"
	 */

	public static DataSoapObject fromSoapObject(SoapObject soapobj) {
		DataSoapObject obj = null;

		Class<?> c;
		try {
			String className = System.getProperty(soapobj.getName());
			if(className == null)
				throw new ClassNotFoundException("Unable to find class "+soapobj.getName()+". Please insert it into System Properties");
			
			c = Class.forName(className);
			

			obj = (DataSoapObject) c.newInstance();
			fillObj(soapobj, obj);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return obj;
	}


	/*
	 * Valorizza i campi pubblici interni di un SoapObject. Ricorsiva.
	 */
	private static void fillObj(SoapObject soapobj, DataSoapObject obj) {
		Object value = null;
		Class<?> c = obj.getClass();
		Field[] fields = c.getFields();
		for (Field field : fields) {
			value = soapobj.getProperty(field.getName());
			try {
				field.set(obj, createObjField(soapobj, obj, value, field.getType(), field.getName()));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * Crea il singolo oggetto per il campo "fieldName", se esso e' complesso
	 * NOTA: i tipi primitivi sono automaticamente unwrappati nella field.set e nella Array.set
	 */
	private static Object createObjField(SoapObject soapobj,
			DataSoapObject obj, Object value, Class<?> type, String fieldName) {
		try {
			if (DataSoapObject.class.isAssignableFrom(type))
				return fromSoapObject((SoapObject) value);
			else if (type.isArray())
				return arrayFromSoapObject(soapobj, obj, type, fieldName);
			else if (value instanceof SoapPrimitive) {
				if (type.getName().equals("int"))
					return new Integer(value.toString());
				else if (type.getName().equals("long"))
					return new Long(value.toString());
				else if (type.getName().equals("float"))
					return new Float(value.toString());
				else if (type.getName().equals("double"))
					return new Double(value.toString());
				else if (type.getName().equals("boolean"))
					return new Boolean(value.toString());

				else {// Integer, Float, Double, Long, Boolean, String
					Class<?> clazz = Class.forName(type.getName());
					Constructor<?> con = clazz.getConstructor(String.class);
					Object instance = con.newInstance(value.toString());
					return instance;
				}
			}

			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Deserializzo l' array: cerco tutte le proprieta' del soapObject
	 * corrispondenti ad elementi dell'array e le memorizzo nella variabile
	 * "list", poi istanzio l'array e lo restituisco In teoria funziona anche
	 * con array bidimensionali. Da testare Restituisce l'array
	 */
	private static Object arrayFromSoapObject(SoapObject soapobj,
			DataSoapObject obj, Class<?> type, String fieldName)
			throws IllegalAccessException {
		Class<?> componentType = type.getComponentType();
		List<?> list = getPropertyList(soapobj, fieldName);
		Object[] a = null;
		if (list.size() > 0)
			a = (Object[]) Array.newInstance(componentType, list.size());

		int i = 0;
		for (Object value : list) {
			Array.set(a,i, createObjField(soapobj, obj, value, componentType, fieldName)) ;
			i++;
		}
		return a;
	}

	/*
	 * Restituisce tutte le proprieta' del SoapObject con il nome "name", se ce
	 * n'e' piu' d'una. Nel caso di serializzazione di un array di oggetti, i
	 * suoi elementi hanno tutti lo stesso "name"
	 */
	private static List<?> getPropertyList(SoapObject soapobj, String name) {
		List list = new ArrayList();
		for (int i = 0; i < soapobj.getPropertyCount(); i++) {
			if (name.equals(((PropertyInfo) soapobj.properties.elementAt(i))
					.getName()))
				list.add(soapobj.getProperty(i));
		}
		return list;
	}

}
