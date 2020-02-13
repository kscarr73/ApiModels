package com.progbits.api;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.util.Map;

/**
 *
 * @author scarr
 */
public interface ApiMapping {
    public String getTargetClass();
    public void setTargetClass(String _targetClass);
    
    public String getSourceClass();
    public void setSourceClass(String _sourceClass);
    
    public boolean setScript(String script) throws ApiException;
    
    public ApiClasses getInModels();
    public void setInModels(ApiClasses inModels);
    
    public ApiClasses getOutModels();
    public void setOutModels(ApiClasses outModels);
    
    public ApiMapping getClone();
    
    public ApiObject map(ApiObject in, Map<String, Object> args) throws ApiException, ApiClassNotFoundException;
}
