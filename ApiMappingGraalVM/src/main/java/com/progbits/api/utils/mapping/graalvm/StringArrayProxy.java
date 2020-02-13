package com.progbits.api.utils.mapping.graalvm;

import com.progbits.api.model.ApiObject;
import java.util.List;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

/**
 *
 * @author scarr
 */
public class StringArrayProxy implements ProxyArray {

    private ApiObject origObj = null;
    private List<String> strObj = null;
    private String keyName = null;

    public StringArrayProxy(String key, ApiObject origObj, List<String> strObj) {
        this.keyName = key;
        this.origObj = origObj;
        this.strObj = strObj;
    }

    @Override
    public Object get(long index) {
        return strObj.get(Long.valueOf(index).intValue());
    }

    @Override
    public void set(long index, Value value) {
        strObj.add(Long.valueOf(index).intValue(), value.asString());
    }

    @Override
    public long getSize() {
        return strObj.size();
    }

}
