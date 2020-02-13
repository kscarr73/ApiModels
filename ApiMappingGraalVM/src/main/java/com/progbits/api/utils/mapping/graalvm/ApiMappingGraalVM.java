package com.progbits.api.utils.mapping.graalvm;

import com.progbits.api.ApiMapping;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.util.Map;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a Script Engine to run Adhoc Mapping between ApiObjects
 * 
 * Uses GraalVM to handle JavaScipt code
 * 
 * @author scarr
 */
@Component(name="ApiMappingGraalVM", immediate = true, property = { "name=ApiMappingGraalVM" })
public class ApiMappingGraalVM implements ApiMapping {

    Logger _log = LoggerFactory.getLogger(ApiMappingGraalVM.class);

    Engine _engine = null;
    Source _script = null;
    String _targetClass = null;
    String _sourceClass = null;

    ApiClasses _inModels = null;
    ApiClasses _outModels = null;

    /**
     * The Source Model for this Mapping Instance
     * @return 
     */
    public String getSourceClass() {
        return _sourceClass;
    }

    /**
     * Set the Source Class for this Mapping Instance
     * @param _sourceClass 
     */
    public void setSourceClass(String _sourceClass) {
        this._sourceClass = _sourceClass;
    }

    /**
     * 
     * @return 
     */
    public String getTargetClass() {
        return _targetClass;
    }

    public void setTargetClass(String _targetClass) {
        this._targetClass = _targetClass;
    }

    public ApiMappingGraalVM() {
        _engine = Engine.create();
    }

    public boolean setScript(String script) throws ApiException {
        try {
            if (_engine == null) {
                throw new ApiException(
                        "Script Engine was Null.  Evaluate your ClassPath.", null);
            }

            //sb.append(strEnd);
            _script = Source.create("js", script);
            
            return true;
        } catch (ApiException scx) {
            throw new ApiException("Script Could Not Be Compiled", scx);
        }
    }
    
    public ApiClasses getInModels() {
        return _inModels;
    }

    public void setInModels(ApiClasses inModels) {
        this._inModels = inModels;
    }

    public ApiClasses getOutModels() {
        return _outModels;
    }

    public void setOutModels(ApiClasses outModels) {
        this._outModels = outModels;
    }

    /**
     * Map a Source ApiObject using the defined Arguments.
     * 
     * This call links the Source with a new context, to return the target.
     * 
     * @param in
     * @param args
     * @return
     * @throws ApiException
     * @throws ApiClassNotFoundException 
     */
    public ApiObject map(ApiObject in, Map<String, Object> args) throws ApiException, ApiClassNotFoundException {
        ApiObject resp = null;

        try (Context context = Context.newBuilder().engine(_engine).build()) {

            if (context == null) {
                throw new ApiException("ApiMappings Context Returned Null", null);
            }

            Value bindings = context.getBindings("js");

            bindings.putMember("source", new ApiObjectProxy(in));
            bindings.putMember("target", new ApiObjectProxy(_outModels.getInstance(_targetClass)));

            if (args != null) {
                args.entrySet().forEach((arg) -> {
                    bindings.putMember(arg.getKey(), arg.getValue());
                });
            }

            try {
                context.eval(_script);
            } catch (Exception ex) {
                throw new ApiException("Script Failure", ex);
            }

            ApiObjectProxy target = bindings.getMember("target").asProxyObject();

            resp = target.getApiObject();
        }

        return resp;
    }

    @Override
    public ApiMapping getClone() {
        ApiMappingGraalVM cloneObj = new ApiMappingGraalVM();
        
        return cloneObj;
    }
}
