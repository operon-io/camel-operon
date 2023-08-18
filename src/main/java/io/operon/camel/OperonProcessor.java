/*
 *   Copyright 2022-2023, operon.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.operon.camel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.MessageHelper;
import org.apache.camel.ProducerTemplate;

import io.operon.runner.OperonContext;
import io.operon.runner.Context;
import io.operon.runner.EmptyContext;
import io.operon.runner.statement.DefaultStatement;
import io.operon.runner.statement.Statement;
import io.operon.runner.OperonRunner;
import io.operon.runner.model.OperonConfigs;
import io.operon.runner.model.ModuleDefinition;
import io.operon.runner.model.exception.*;
import io.operon.runner.node.type.*;
import io.operon.runner.node.Node;
import io.operon.runner.util.JsonUtil;
import io.operon.runner.compiler.CompilerFlags;
import io.operon.runner.OperonFunction;

import io.operon.camel.util.QueryLoadUtil;
import io.operon.camel.model.CamelOperonHeaders;
import io.operon.camel.model.CamelOperonMimeTypes;
import io.operon.camel.model.exception.UnsupportedMimeTypeException;
import io.operon.camel.function.ProducerTemplateFunc;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperonProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(OperonProcessor.class);
    
    private List<String> modulePaths;
	
    // TODO: add support for named imports
    //private Map<String, String> namedImports = new HashMap<>();
    
    private List<String> supportedMimeTypes = new ArrayList<>(
    	Arrays.asList(CamelOperonMimeTypes.MIME_APPLICATION_JSON,
    	    CamelOperonMimeTypes.MIME_APPLICATION_JAVA,
    	    CamelOperonMimeTypes.MIME_APPLICATION_JAVA_OPERON,
    	    CamelOperonMimeTypes.MIME_APPLICATION_OCTET_STREAM)
    	);

    private String inputMimeType;
    private String outputMimeType;

    private String operonFile;
    private String operonScript;
    private List<ModuleDefinition> modules;
    private boolean debug = false;

    private ProducerTemplate pt;

    public void process(Exchange exchange) throws Exception {
        Object mappedBody = processMapping(exchange);
        exchange.getIn().setBody(mappedBody);
    }

    public void init() throws Exception {
        //
        // init-code here, e.g. load operon-modules
        //
        if (modules == null) {
            modules = new ArrayList<ModuleDefinition>();
        }
        
        if (modulePaths != null) {
            for (int i = 0; i < modulePaths.size(); i ++) {
                String modulePath = modulePaths.get(i);
                String [] modulePathParts = modulePath.split(":");
                String moduleBody = QueryLoadUtil.loadModuleFile(modulePathParts[1]);
                ModuleDefinition md = new ModuleDefinition();
                md.setBody(moduleBody);
                md.setNamespace(modulePathParts[0]);
                md.setFilePath(modulePathParts[1]);
                modules.add(md);
                if (isDebug()) {
                	System.out.println("=== Added module ===");
        		}
            }
        }
    }

    public List<String> getModulesPaths() {
        return modulePaths;
    }

    public void setModulePaths(List<String> mp) {
        this.modulePaths = mp;
    }

    public String getInputMimeType() {
        return inputMimeType;
    }

    public void setInputMimeType(String inputMimeType) {
        this.inputMimeType = inputMimeType;
    }

    public String getOutputMimeType() {
        return outputMimeType;
    }

    public void setOutputMimeType(String outputMimeType) {
        this.outputMimeType = outputMimeType;
    }

    public String getOperonFile() {
        return operonFile;
    }

    public void setOperonFile(String operonFile) {
        this.operonFile = operonFile;
    }

    public String getOperonScript() {
        return operonScript;
    }

    public void setOperonScript(String operonScript) {
        this.operonScript = operonScript;
    }

    public Object processMapping(Exchange exchange) throws Exception {
        return processMapping(exchange, null, null, null);
    }

    public Object processMapping(Exchange exchange, OperonConfigs operonConfigs, String _inputMimeType, String _outputMimeType) throws Exception {
        OperonConfigs configs = null;
        if (operonConfigs == null) {
            configs = new OperonConfigs();
        }
        else {
            configs = operonConfigs;
        }
        
        if (_inputMimeType == null || "".equalsIgnoreCase(_inputMimeType.trim())) {
			String overriddenInputMimeType = (String) exchange.getIn().getHeader(CamelOperonHeaders.INPUT_MIME_TYPE);
			
			if (overriddenInputMimeType == null) {
				overriddenInputMimeType = CamelOperonMimeTypes.UNKNOWN_MIME_TYPE;
			}
			
            _inputMimeType = overriddenInputMimeType;
        }
        
        if (!supportedMimeTypes.contains(_inputMimeType)) {
            //logger.warn("Input Mime Type " + _inputMimeType + " is not supported or suitable plugin not found, using application/json");
            if (isDebug()) {
            	System.out.println("inputMimeType :: not found :: set application/json");
            }
            _inputMimeType = CamelOperonMimeTypes.MIME_APPLICATION_JSON;
        }

        if (_outputMimeType == null || "".equalsIgnoreCase(_outputMimeType.trim())) {
			String overriddenOutputMimeType = (String) exchange.getIn().getHeader(CamelOperonHeaders.OUTPUT_MIME_TYPE);
			
			if (overriddenOutputMimeType == null) {
				overriddenOutputMimeType = CamelOperonMimeTypes.UNKNOWN_MIME_TYPE;
			}
			
            _outputMimeType = overriddenOutputMimeType;
        }
        
        if (!supportedMimeTypes.contains(_outputMimeType)) {
            //logger.warn("Output Mime Type " + _outputMimeType + " is not supported or suitable plugin not found, using application/json");
            if (isDebug()) {
            	System.out.println("outputMimeType :: not found :: set application/json");
            }
            _outputMimeType = CamelOperonMimeTypes.MIME_APPLICATION_JSON;
        }

		//
		// Resolve query
		//
		String query = this.getOperonScript();
		
		if (query == null) {
			query = (String) exchange.getIn().getHeader(CamelOperonHeaders.LANGUAGE_SCRIPT);
		}
		
		if (query == null) {
		    String queryFile = (String) exchange.getIn().getHeader(CamelOperonHeaders.LANGUAGE_SCRIPT_PATH);
		    if (queryFile != null && queryFile.length() > 0) {
		        query = QueryLoadUtil.loadQueryFile(queryFile);
		    }
		}
		
		boolean queryInBody = false;
		
		if (query == null) {
			query = exchange.getIn().getBody(String.class);
			queryInBody = true;
		}
		
		//
		// Resolve initial value
		//
		Object initialValueData = exchange.getIn().getHeader(CamelOperonHeaders.INITIAL_VALUE);
		
		if (initialValueData == null && queryInBody == false) {
			//logger.info("Resolve initial value from body");
			initialValueData = exchange.getIn().getBody();
			//logger.info("  - initial :: " + initialValueData);
		}
		
		OperonValue initialValue = null;
		OperonValue resultValue = null;
		
		//
		// Check headers for index-list, e.g. "$;$foo;$bar"
		// These values will be adviced to be indexed when parsed by the JsonUtil
		//
		String indexListStr = exchange.getIn().getHeader(CamelOperonHeaders.OPERON_INDEX_LIST, String.class);
		String[] indexList = {};
		
		if (indexListStr != null) {
		    indexList = indexListStr.split(";");
		}
		
		//
		// Check headers for value-bindings
		//
		Map valueBindings = exchange.getIn().getHeader(CamelOperonHeaders.OPERON_VALUE_BINDINGS, Map.class);
		if (valueBindings != null) {
		    HashMap<String, String> valueBindingsHM = (HashMap<String, String>) valueBindings;
		    // loop over, and set into configs.
		    for (Map.Entry<String, String> entry : valueBindingsHM.entrySet()) {
				OperonValue operonValue = null;
				
				boolean indexValue = false;
				
				for (int k = 0; k < indexList.length; k ++) {
				    if (indexList[k].trim().equals("$" + entry.getKey())) {
				        indexValue = true;
				        break;
				    }
				}
				if (indexValue) {
    				CompilerFlags[] flags = {CompilerFlags.INDEX_ROOT};
    				operonValue = JsonUtil.operonValueFromString(entry.getValue(), flags);
				}
				else {
				    operonValue = JsonUtil.operonValueFromString(entry.getValue());
				}
                
                configs.setNamedValue("$" + entry.getKey(), operonValue);
		    }
		}
		
		//
		// Check headers for value-bindings list, e.g. `bin=\"foo\";bai=\"bar\"`
		//
		String valueBindingsStr = exchange.getIn().getHeader(CamelOperonHeaders.OPERON_BIND_LIST, String.class);
		if (valueBindingsStr != null && valueBindingsStr.length() > 0) {
		    String[] kvs = valueBindingsStr.split(";");

		    // loop over, and set into configs.
		    for (int i = 0; i < kvs.length; i ++) {
                String[] kv = kvs[i].split("=");
                if (kv.length == 1) {
                	throw new IllegalArgumentException("OPERON_BIND_LIST does not contain '='");
                }
                
                OperonValue operonValue = null;
				
				boolean indexValue = false;
				
				// Check if this value should be indexed
				for (int k = 0; k < indexList.length; k ++) {
				    if (indexList[k].trim().equals("$" + kv[0])) {
				        indexValue = true;
				        break;
				    }
				}
				if (indexValue) {
    				CompilerFlags[] flags = {CompilerFlags.INDEX_ROOT};
    				operonValue = JsonUtil.operonValueFromString(kv[1], flags);
				}
				else {
				    operonValue = JsonUtil.operonValueFromString(kv[1]);
				}
                
                
                configs.setNamedValue("$" + kv[0], operonValue);
		    }
		}
		
		//
		// Register user-defined Java-functions
		//
		Map<String, OperonFunction> operonFunctionsMap = exchange.getIn().getHeader(CamelOperonHeaders.OPERON_FUNCTIONS, Map.class);
        if (operonFunctionsMap != null) {
            for (Map.Entry<String, OperonFunction> entry : operonFunctionsMap.entrySet()) {
                String fnKey =  entry.getKey();
                OperonFunction fn = entry.getValue();
                OperonRunner.registerFunction(fnKey, fn);
            }
        }
		
		//
		// Set the ProducerTemplate, that can be used from the query
		//
		ProducerTemplate userPt = exchange.getIn().getHeader(CamelOperonHeaders.PRODUCER_TEMPLATE, ProducerTemplate.class);
		
		if (userPt != null) {
    		ProducerTemplateFunc ptFunc = new ProducerTemplateFunc(userPt, exchange);
    		OperonRunner.registerFunction("camel", ptFunc);
		}
		
		if (initialValueData != null) {
			if (_inputMimeType.equalsIgnoreCase(CamelOperonMimeTypes.APPLICATION_JSON)) {
				String initialValueStr = (String) initialValueData;
				if (initialValueStr.isEmpty()) {
					initialValueStr = "empty";
				}
				
				boolean indexRoot = false;
				for (int i = 0; i < indexList.length; i ++) {
				    if (indexList[i].trim().equals("$")) {
				        indexRoot = true;
				        break;
				    }
				}
				if (indexRoot) {
    				CompilerFlags[] flags = {CompilerFlags.INDEX_ROOT};
    				initialValue = JsonUtil.operonValueFromString(initialValueStr, flags);
				}
				else {
				    initialValue = JsonUtil.operonValueFromString(initialValueStr);
				}
			}
			
			else if (_inputMimeType.equalsIgnoreCase(CamelOperonMimeTypes.APPLICATION_JAVA)) {
				// map from pojo / java type
				String jsonString = null;
				
				Gson gson = new Gson();
				jsonString = gson.toJson(initialValueData);
				
				if (jsonString.isEmpty()) {
					jsonString = "empty";
				}
				boolean indexRoot = false;
				for (int i = 0; i < indexList.length; i ++) {
				    if (indexList[i].trim().equals("$")) {
				        indexRoot = true;
				        break;
				    }
				}
				if (indexRoot) {
    				CompilerFlags[] flags = {CompilerFlags.INDEX_ROOT};
    				initialValue = JsonUtil.operonValueFromString(jsonString, flags);
				}
				else {
				    initialValue = JsonUtil.operonValueFromString(jsonString);
				}
			}
			
			else if (_inputMimeType.equalsIgnoreCase(CamelOperonMimeTypes.APPLICATION_OCTET_STREAM)) {
				byte[] initialValueBytes = (byte[]) initialValueData;
				if (initialValueBytes.length == 0) {
					initialValue = JsonUtil.operonValueFromString("empty");
				}
				else {
				    Context ctx = new EmptyContext();
				    Statement stmt = new DefaultStatement(ctx);
    				RawValue raw = new RawValue(stmt);
    				raw.setValue(initialValueBytes);
    				initialValue = raw;
				}
			}
			
			if (modules == null || modules.size() == 0) {
			    resultValue = OperonRunner.doQueryWithInitialValue(query, initialValue, configs);
			}
			else {
			    resultValue = OperonRunner.doQueryWithInitialValueAndModules(query, initialValue, configs, modules);
			}
		}
		
		else {
			// no initial value
			resultValue = OperonRunner.doQuery(query, configs);
		}
        
        if (isDebug()) {
	        System.out.println("inputMimeType :: " + _inputMimeType);
	        System.out.println("outputMimeType :: " + _outputMimeType);
        }
        
        if (_outputMimeType.equalsIgnoreCase(CamelOperonMimeTypes.MIME_APPLICATION_JSON)) {
        	return resultValue.toString();
        }
        else if (_outputMimeType.equalsIgnoreCase(CamelOperonMimeTypes.MIME_APPLICATION_JAVA_OPERON)) {
        	return resultValue;
        }
        else if (_outputMimeType.equalsIgnoreCase(CamelOperonMimeTypes.MIME_APPLICATION_JAVA)) {
        	resultValue = resultValue.evaluate(); // unbox the OperonValue
        	if (resultValue instanceof ArrayType) {
            	Gson gson = new Gson();
            	Object result = gson.fromJson(resultValue.toString(), ArrayList.class);
            	return result;
        	}
        	else if (resultValue instanceof ObjectType) {
            	Gson gson = new Gson();
            	Object result = gson.fromJson(resultValue.toString(), LinkedHashMap.class);
            	return result;
        	}
        	else if (resultValue instanceof StringType) {
        	    return ((StringType) resultValue).getJavaStringValue();
        	}
        	else if (resultValue instanceof NumberType) {
        	    return ((NumberType) resultValue).getDoubleValue();
        	}
        	else if (resultValue instanceof NullType) {
        	    return null;
        	}
        	else if (resultValue instanceof RawValue) {
        	    return ((RawValue) resultValue).getBytes();
        	}
        	else if (resultValue instanceof TrueType) {
        	    return true;
        	}
        	else if (resultValue instanceof FalseType) {
        	    return false;
        	}
        	else {
        	    // Path, EmptyType, StreamValue, EndValueType
        	    return resultValue;
        	}
        }
        else if (_outputMimeType.equalsIgnoreCase(CamelOperonMimeTypes.MIME_APPLICATION_OCTET_STREAM)) {
        	// Cast to raw type:
        	RawValue raw = (RawValue) resultValue;
        	return raw.getBytes();
        }
        else {
        	throw new UnsupportedMimeTypeException(_outputMimeType);
        }
    }

    public void setProducerTemplate(ProducerTemplate pt) {
        this.pt = pt;
    }
    
    public ProducerTemplate getProducerTemplate() {
        return this.pt;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

}
