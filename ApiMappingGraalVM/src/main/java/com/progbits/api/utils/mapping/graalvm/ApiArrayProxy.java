package com.progbits.api.utils.mapping.graalvm;

import com.progbits.api.model.ApiObject;
import java.util.List;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

/**
 *
 * @author scarr
 */
public class ApiArrayProxy implements ProxyArray {

    
    private ApiObject origObj = null;
    private List<ApiObject> arrObj = null;
    private String keyName = null;

    public ApiArrayProxy(String key, ApiObject origObj, List<ApiObject> arrObj) {
        this.keyName = key;
        this.origObj = origObj;
        this.arrObj = arrObj;
    }

    @Override
    public Object get(long index) {
        return new ApiObjectProxy(arrObj.get(Long.valueOf(index).intValue()));
    }

    @Override
    public void set(long index, Value value) {
        ApiObject apiObj = origObj.newSubObject(keyName);

        if (value.hasMembers()) {
            final ApiObjectProxy objWrapper = new ApiObjectProxy(apiObj);

            value.getMemberKeys().forEach((memberKey) -> {
                objWrapper.putMember(memberKey, value.getMember(memberKey));
            });
        }

        arrObj.add(Long.valueOf(index).intValue(), apiObj);
    }

    @Override
    public long getSize() {
        return arrObj.size();
    }
    
}
