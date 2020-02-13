package com.progbits.api.utils.mapping.graalvm;

import com.progbits.api.model.ApiObject;
import java.util.List;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

/**
 *
 * @author scarr
 */
public class IntArrayProxy implements ProxyArray {

private ApiObject origObj = null;
    private List<Integer> intObj = null;
    private String keyName = null;

    public IntArrayProxy(String key, ApiObject origObj, List<Integer> intObj) {
        this.keyName = key;
        this.origObj = origObj;
        this.intObj = intObj;
    }

    @Override
    public Object get(long index) {
        return intObj.get(Long.valueOf(index).intValue());
    }

    @Override
    public void set(long index, Value value) {
        intObj.add(Long.valueOf(index).intValue(), value.asInt());
    }

    @Override
    public long getSize() {
        return intObj.size();
    }

}
