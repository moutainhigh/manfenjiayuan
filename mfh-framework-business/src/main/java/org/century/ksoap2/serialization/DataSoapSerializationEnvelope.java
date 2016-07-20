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

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Vector;

/**
 * @author Tiziana Cimoli
 * 
 */
public class DataSoapSerializationEnvelope extends SoapSerializationEnvelope {

	/*
	 * Attenzione: per poter essere deserializzato correttamente ogni oggetto composto 
	 * puo' contenere al massimo *unsolo* array di oggetti,
	 * 
	 * Questo eprche' tutti gli item dell'array hano lo stesso nome convenzionale ARRAY_ITEM_LIST
	 * e un array nella rappresentazione soap-xml NON HA un wrapper che lo rinchiuda 
	 */
	private static final String ARRAY_ITEM_LABEL = "list";
	private static final Class<?> FLOAT_CLASS = new Float(0).getClass();
	private static final Class<?> DOUBLE_CLASS = new Double(0).getClass();
	private static final String TYPE_LABEL = "type";

	public DataSoapSerializationEnvelope(int version) {
		super(version);
		register();
	}

	/* Nel metodo ZSoapSerializationEnvelope#register manca la creazione del marshal per i tipi
	 * SoapPrimitivi float e double 
	 *  
	 */
	public void register() {
	        addMapping(xsd, "float", FLOAT_CLASS, DEFAULT_MARSHAL);
	        addMapping(xsd, "double", DOUBLE_CLASS, DEFAULT_MARSHAL);
	}
	
	/*
	 * Ridefinisco il metodo della superclasse 
	 * (non-Javadoc)
	 * @see org.ksoap2.serialization.ZSoapSerializationEnvelope#writeObjectBody(org.xmlpull.v1.XmlSerializer, org.ksoap2.serialization.KvmSerializable)
	 */
	public void writeObjectBody(XmlSerializer writer, KvmSerializable obj)
			throws IOException {
		PropertyInfo info = new PropertyInfo();
		int cnt = obj.getPropertyCount();
		for (int i = 0; i < cnt; i++) {
			obj.getPropertyInfo(i, properties, info);
			if ((info.flags & PropertyInfo.TRANSIENT) == 0) {
				Object curr = obj.getProperty(i);
				Class<?> type = curr.getClass();
				if (type.isArray()) {
					serializeArrayElement(writer, curr, info);
				} else {
					writer.startTag(info.namespace, info.name);
					writeProperty(writer, curr, info);
					writer.endTag(info.namespace, info.name);
				}
			}
		}
	}

	/*
	 * Serializzo l'array: ogni elemento nell'array, viene inviato sul
	 * soapEnvelope con un nome convezionale ARRAY_ITEM_LABEL 
	 * Alternativamente si puo' dare all'item il nome, in minuscolo della classe 
	 * if(size >0) elName =
	 * Array.get(obj_array, 1).getClass().getSimpleName().toLowerCase();
	 */
	public void serializeArrayElement(XmlSerializer writer, Object obj_array,
			PropertyInfo info) throws IOException {

		int size = Array.getLength(obj_array);
		String elName = null;
		// if(size >0) elName = Array.get(obj_array,
		// 1).getClass().getSimpleName().toLowerCase();
		elName = ARRAY_ITEM_LABEL;
		for (int j = 0; j < size; j++) {
			Class<?> componentType = obj_array.getClass().getComponentType();
			Object curr = Array.get(obj_array, j);
			writer.startTag(info.namespace, elName);
				
			Object[] qName = getInfo(null, curr);
			String prefix = writer.getPrefix((String) qName[QNAME_NAMESPACE], true);
			writer.attribute(xsi, TYPE_LABEL, prefix + ":" + qName[QNAME_TYPE]);				
			writeElement(writer, curr, info, qName[QNAME_MARSHAL]);
			
			writer.endTag(info.namespace, elName);
			
		}
	}

	/*
             * Questo metodo e' uguale a
             * @see org.ksoap2.serialization.ZSoapSerializationEnvelope#writeElement(XmlSerializer writer, Object element,
                    PropertyInfo type, Object marshal) throws IOException
             *Lo riscrivo qui per accedervi anche da qui,  poiche' esso e' privato
             */
	@Override
	protected void writeElement(XmlSerializer writer, Object element, PropertyInfo type, Object marshal) throws IOException {
//		super.writeElement(writer, element, type, marshal);
		if (marshal != null)
			((Marshal) marshal).writeInstance(writer, element);
		else if (element instanceof SoapObject)
			writeObjectBody(writer, (SoapObject) element);
		else if (element instanceof KvmSerializable)
			writeObjectBody(writer, (KvmSerializable) element);
		else if (element instanceof Vector<?>)
			writeVectorBody(writer, (Vector<?>) element, type.elementType);
		else
			throw new RuntimeException("Cannot serialize: " + element);
	}

}
