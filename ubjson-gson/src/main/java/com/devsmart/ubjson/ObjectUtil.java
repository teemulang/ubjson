package com.devsmart.ubjson;

import java.lang.reflect.Array;
import java.util.*;

@SuppressWarnings("unchecked")
public class ObjectUtil {

    public static boolean isPrimitive( Object o){
        return o.getClass().isPrimitive() ||
                o instanceof Boolean || o instanceof Number ||
                o instanceof CharSequence ;
    }

    public static boolean isCollection( Object o){
        return o.getClass().isArray() ||
                o instanceof Collection ;
    }

    public static Collection<Object> toCollection( Object o ){
        if ( o.getClass().isArray() ){
            final int len = Array.getLength(o) ;
            final List<Object> l = new ArrayList<>(len);
            for ( int i=0; i < len;i++){
                l.add( Array.get(o,i));
            }
            return l;
        }
        if ( o instanceof Collection ){
            return (Collection<Object>)o;
        }
        return Collections.singleton(o);
    }


    public static UBValue toUBValue(Object element) {
        if(element == null ) {
            return UBValueFactory.createNull();
        }
        if( isPrimitive(element)  ) {
            if( element instanceof  Boolean ){
                return UBValueFactory.createBool( (boolean)element );
            } else if( element instanceof CharSequence ) {
                return UBValueFactory.createString( element.toString());
            } else if( element instanceof Number ) {
                return UBValueFactory.createNumber( (Number) element);
            }
        } else if( isCollection(element) ) {
            Collection<Object> collection = toCollection(element);
            final int size = collection.size();
            UBValue[] ubArray = new UBValue[size];
            Iterator<Object> iterator = collection.iterator();
            for(int i=0;i<size;i++) {
                ubArray[i] = toUBValue(iterator.next());
            }
            return UBValueFactory.createArray(ubArray);
        } else if(element instanceof Map ) {
            Map<Object,Object> jsonObject = (Map<Object,Object>)element;
            UBObject retval = UBValueFactory.createObject();
            for(Map.Entry<Object,Object> entry : jsonObject.entrySet()) {
                retval.put(entry.getKey().toString(), toUBValue(entry.getValue()));
            }
            return retval;
        } else {
            throw new RuntimeException("unknown JVM Type element: " + element);
        }

        return null;
    }

    public static Object toObject( UBValue value ){
        if ( value.isNull() ) return null;
        if ( value.isString() ) return value.asString();
        if ( value.isInteger() ) return value.asInt();
        if ( value.isChar() ) return value.asChar();
        if ( value.isFloat() ) return value.asFloat64();
        if ( value.isBool() ) return value.asBool();
        // now we find the composite types
        if ( value.isArray() ){
            UBArray arr = value.asArray();
            final int size = arr.size();
            List<Object> l = new ArrayList<>(size);
            for ( int i=0; i < size; i++){
                Object inner = toObject(arr.get(i));
                l.add(inner);
            }
            return l;
        }
        if ( value.isObject()){
            UBObject obj = value.asObject();
            Map<String,Object> map = new LinkedHashMap<>();
            for(Map.Entry<String,UBValue> entry : obj.entrySet()) {
                map.put(entry.getKey(), toObject(entry.getValue()));
            }
            return map;
        }
        throw new RuntimeException("unknown JSON Type element: " + value);
    }
}
