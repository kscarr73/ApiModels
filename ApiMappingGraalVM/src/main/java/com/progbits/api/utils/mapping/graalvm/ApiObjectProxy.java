package com.progbits.api.utils.mapping.graalvm;

import com.progbits.api.model.ApiObject;
import java.util.ArrayList;
import java.util.List;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

/**
 *
 * @author scarr
 */
public class ApiObjectProxy implements ProxyObject {

    ApiObject intObj = null;

    public ApiObjectProxy(ApiObject obj) {
        intObj = obj;
    }

    public ApiObject getApiObject() {
        return intObj;
    }

    @Override
    public Object getMember(String key) {
        switch (intObj.getType(key)) {
            case ApiObject.TYPE_ARRAYLIST:
                return new ApiArrayProxy(key, intObj, (List<ApiObject>) intObj.getCoreObject(key));
            case ApiObject.TYPE_OBJECT:
                return new ApiObjectProxy(intObj.getObject(key));
            case ApiObject.TYPE_INTEGERARRAY:
                return new IntArrayProxy(key, intObj, (List<Integer>) intObj.getCoreObject(key));
            case ApiObject.TYPE_STRINGARRAY:
                return new StringArrayProxy(key, intObj, (List<String>) intObj.getCoreObject(key));

            default:
                return intObj.getCoreObject(key);
        }
    }

    @Override
    public Object getMemberKeys() {
        final List<String> keys = new ArrayList<>();

        intObj.getFields().keySet().forEach((field) -> {
            keys.add(field);
        });
        return keys;
    }

    @Override
    public boolean hasMember(String key) {
        return intObj.containsKey(key);
    }

    @Override
    public void putMember(String key, Value value) {
        // TODO: Use ApiClass if available to create objects

        if (value.isString()) {
            intObj.setString(key, value.asString());
        } else if (value.isNumber()) {
            intObj.put(key, value.as(Object.class));
        } else if (value.hasArrayElements()) {
            if (value.getArraySize() > 0) {
                int iType = ApiObject.TYPE_ARRAYLIST;

                for (long x = 0; x <= value.getArraySize(); x++) {
                    Value arrValue = value.getArrayElement(x);

                    if (arrValue.hasMembers()) {
                        if (intObj.getCoreObject(key) == null) {
                            intObj.createList(key);
                        }

                        final ApiObject newObj = intObj.newSubObject(key);
                        final ApiObjectProxy objWrapper = new ApiObjectProxy(newObj);

                        arrValue.getMemberKeys().forEach((memberKey) -> {
                            objWrapper.putMember(memberKey, arrValue.getMember(memberKey));
                        });

                        intObj.getList(key).add(newObj);
                    } else if (arrValue.isString()) {
                        if (intObj.getCoreObject(key) == null) {
                            intObj.createStringArray(key);
                        }

                        intObj.getStringArray(key).add(arrValue.asString());
                    } else if (arrValue.isNumber()) {
                        if (intObj.getCoreObject(key) == null) {
                            iType = testSingleArrayType(value);

                            if (iType == ApiObject.TYPE_INTEGERARRAY) {
                                intObj.createIntegerArray(key);
                            } else if (iType == ApiObject.TYPE_DOUBLEARRAY) {
                                intObj.createDoubleArray(key);
                            }
                        }

                        if (iType == ApiObject.TYPE_INTEGERARRAY) {
                            intObj.getIntegerArray(key).add(arrValue.asInt());
                        } else if (iType == ApiObject.TYPE_DOUBLEARRAY) {
                            intObj.getDoubleArray(key).add(arrValue.asDouble());
                        }
                    }
                }
            } else {
                intObj.createList(key);
            }
        } else if (value.hasMembers() && value.getMemberKeys().isEmpty()) {
            intObj.createObject(key);

            final ApiObjectProxy objWrapper = new ApiObjectProxy(intObj.getObject(key));

            value.getMemberKeys().forEach((memberKey) -> {
                objWrapper.putMember(memberKey, value.getMember(memberKey));
            });

        } else {

        }
    }

    private int testSingleArrayType(Value value) {
        int retInt = ApiObject.TYPE_INTEGERARRAY;

        for (long x = 0; x <= value.getArraySize(); x++) {
            Value arrValue = value.getArrayElement(x);

            if (arrValue.fitsInInt()) {
                retInt = ApiObject.TYPE_INTEGERARRAY;
            } else if (arrValue.fitsInDouble()) {
                retInt = ApiObject.TYPE_DOUBLEARRAY;  // TYPE_DOUBLEARRAY;
                break;
            }
        }

        return retInt;
    }
}
