package com.progbits.api.utils;

import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.util.List;
import java.util.Map;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
public class ApiMappingNashorn {
    
    Logger _log = LoggerFactory.getLogger(ApiMapping_Old.class);

    ScriptEngine _shell = null;
    CompiledScript _script = null;
    String _targetClass = null;
    String _sourceClass = null;
    ParserService _parser = null;
    WriterService _writer = null;

    ApiClasses _inModels = null;
    ApiClasses _outModels = null;

    public void setParser(ParserService parser) {
        this._parser = parser;
    }

    public void setWriter(WriterService writer) {
        this._writer = writer;
    }

    public String getSourceClass() {
        return _sourceClass;
    }

    public void setSourceClass(String _sourceClass) {
        this._sourceClass = _sourceClass;
    }

    public String getTargetClass() {
        return _targetClass;
    }

    public void setTargetClass(String _targetClass) {
        this._targetClass = _targetClass;
    }

    public ParserService getParser() {
        return _parser;
    }

    public WriterService getWriter() {
        return _writer;
    }

    public ApiMappingNashorn(String sourceClass, String targetClass, String javaScript)
            throws ApiException {
        List<ScriptEngineFactory> factories = new ScriptEngineManager().getEngineFactories();
        _shell = new ScriptEngineManager().getEngineByName("Graal.js");
        _targetClass = targetClass;
        _sourceClass = sourceClass;

        try {
            if (_shell == null) {
                throw new ApiException(
                        "Script Engine was Null.  Evaluate your ClassPath.", null);
            }

            //sb.append(strEnd);
            _script = ((Compilable) _shell).compile(javaScript);
        } catch (ScriptException scx) {
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

    public ApiObject map(ApiObject in, Map<String, Object> args) throws ApiException, ApiClassNotFoundException {
        ApiObject resp = null;

        ScriptContext context = new SimpleScriptContext();

        if (context == null) {
            throw new ApiException("ApiMappings Context Returned Null", null);
        }

        context.setBindings(_shell.createBindings(), ScriptContext.ENGINE_SCOPE);
        context.setAttribute("source", in,
                ScriptContext.ENGINE_SCOPE);
//        context.setAttribute("target",
//                _outModels.getInstance(_targetClass),
//                ScriptContext.ENGINE_SCOPE);

        if (args != null) {
            for (Map.Entry<String, Object> arg : args.entrySet()) {
                context.setAttribute(arg.getKey(), arg.getValue(),
                        ScriptContext.ENGINE_SCOPE);
            }
        }

        try {
            _script.eval(context);
        } catch (ScriptException ex) {
            throw new ApiException("Script Failure", ex);
        } catch (Exception ex2) {
            throw new ApiException("Script Failure", ex2);
        }

        resp = (ApiObject) context.getAttribute("target",
                ScriptContext.ENGINE_SCOPE);

        //processScriptObject(trgt, resp);
        // resp = Transform.toApiObject(_parser.getParser("JSON"), _outModels, _targetClass, sTarget);
        return resp;
    }
}
