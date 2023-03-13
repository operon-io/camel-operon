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
package io.operon.camel.function;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.ProducerTemplate;

import io.operon.runner.node.type.*;
import io.operon.runner.model.exception.OperonGenericException;
import io.operon.runner.OperonFunction;
import io.operon.runner.statement.Statement;

import io.operon.camel.model.exception.*;
import io.operon.runner.util.JsonUtil;
import io.operon.runner.util.ErrorUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerTemplateFunc implements OperonFunction {
	private static final Logger LOG = LoggerFactory.getLogger(ProducerTemplateFunc.class);

    // https://www.javadoc.io/doc/org.apache.camel/camel-api/latest/org/apache/camel/ProducerTemplate.html
    private ProducerTemplate pt;
    
    private Exchange ex;

    public ProducerTemplateFunc(ProducerTemplate pt, Exchange ex) {
        this.pt = pt;
        this.ex = ex;
    }

    public OperonValue execute(Statement stmt, OperonValue params) throws OperonGenericException {
        LOG.debug("Calling ProducerTemplate...");
        
        if (this.pt == null) {
            ErrorUtil.createErrorValueAndThrow(stmt, "CALL", "ERROR", "call -producer: ProducerTemplate was not set. Please use setProducerTemplate(ProducerTemplate pt) before calling this function.");
        }
        
        params = params.evaluate();
        
        OperonValue currentValue = stmt.getCurrentValue();
        OperonValue result = currentValue;
        
        if (params instanceof StringType) {
            String uri = ((StringType) params.evaluate()).getJavaStringValue();
            
            String resultBody = "";
            
            if (currentValue instanceof RawValue == false) {
                resultBody = pt.requestBody(uri, currentValue, String.class);
            }
            else {
                resultBody = pt.requestBody(uri, ((RawValue) currentValue).getBytes(), String.class);
            }
            
            result = StringType.create(stmt, resultBody);
        }
        
        else if (params instanceof ObjectType) {
            List<PairType> jsonPairs = ((ObjectType) params).getPairs();
            
            String uri = "";
            Map<String, Object> headers = null;
            boolean nullBody = false;
            boolean async = false;
            ReadAsType readAs = ReadAsType.JSON;
            
            for (PairType pair : jsonPairs) {
                String key = pair.getKey();
                OperonValue currentValueCopy2 = currentValue;
                pair.getStatement().setCurrentValue(currentValueCopy2);
                switch (key.toLowerCase()) {
                    case "\"uri\"":
                        OperonValue uriNode = (OperonValue) pair.getEvaluatedValue();
                        String sUri = ((StringType) uriNode).getJavaStringValue();
                        uri = sUri;
                        break;
                    
                    // This controls if the headers are mapped and pt.requestBodyAndHeaders will be used instead of requestBody
                    case "\"headers\"":
                        ObjectType headersNode = (ObjectType) pair.getEvaluatedValue();
                        headers = mapHeaders(currentValue, headersNode);
                        break;
                    
                    // This controls if the body is sent as empty (i.e. null in Java)
                    case "\"nullbody\"":
                        OperonValue nb = pair.getEvaluatedValue();
                        if (nb instanceof FalseType) {
                            nullBody = false;
                        }
                        else {
                            nullBody = true;
                        }
                        break;
                    
                    // This controls if we use request or send with ProducerTemplate (request = sync, send = async)
                    case "\"async\"":
                        OperonValue as = pair.getEvaluatedValue();
                        if (as instanceof FalseType) {
                            async = false;
                        }
                        else {
                            async = true;
                        }
                        break;
                    
                    case "\"readas\"":
                        String sRa = ((StringType) pair.getEvaluatedValue()).getJavaStringValue();
                        try {
                            readAs = ReadAsType.valueOf(sRa.toUpperCase());
                        } catch(Exception e) {
                            System.err.println("ERROR SIGNAL: invalid readAs-property in camel-operon component");
                        }
                        break;
                    
                    default:
                        LOG.debug("call -producer: no mapping for configuration key: " + key);
                        System.err.println("call -producer: no mapping for configuration key: " + key);
                        ErrorUtil.createErrorValueAndThrow(stmt, "CALL", "ERROR", "call -producer: no mapping for configuration key: " + key);
                }
            }
            
            Object resultBody = null;
            if (nullBody == false) {
                if (async == false) {
                    if (currentValue instanceof RawValue == false) {
                        if (headers == null) {
                            resultBody = pt.requestBody(uri, currentValue);
                        }
                        else {
                            resultBody = pt.requestBodyAndHeaders(uri, currentValue, headers);
                        }
                    }
                    else {
                        if (headers == null) {
                            resultBody = pt.requestBody(uri, ((RawValue) currentValue).getBytes());
                        }
                        else {
                            resultBody = pt.requestBodyAndHeaders(uri, ((RawValue) currentValue).getBytes(), headers);
                        }
                    }
                }
                else {
                    if (headers == null) {
                        pt.sendBody(uri, currentValue);
                    }
                    else {
                        pt.sendBodyAndHeaders(uri, currentValue, headers);
                    }
                }
            }
            
            else {
                if (async == false) {
                    if (headers == null) {
                        resultBody = pt.requestBody(uri, (Object) null);
                    }
                    else {
                        resultBody = pt.requestBodyAndHeaders(uri, (Object) null, headers);
                    }
                }
                else {
                    if (headers == null) {
                        pt.sendBody(uri, currentValue);
                    }
                    else {
                        pt.sendBodyAndHeaders(uri, currentValue, headers);
                    }
                }
            }
            
            if (readAs == ReadAsType.JSON) {
                result = JsonUtil.operonValueFromString(
                    this.ex.getContext().getTypeConverter().convertTo(String.class, resultBody)
                );
            }
            else if (readAs == ReadAsType.RAW) {
                String resultStr = this.ex.getContext().getTypeConverter().convertTo(String.class, resultBody);
                result = RawValue.createFromString(stmt, resultStr);
            }
            else if (readAs == ReadAsType.STRING) {
                String resultStr = this.ex.getContext().getTypeConverter().convertTo(String.class, resultBody);
                result = StringType.create(stmt, resultStr);
            }
            else if (readAs == ReadAsType.DOUBLE) {
                Double resultDouble = this.ex.getContext().getTypeConverter().convertTo(Double.class, resultBody);
                result = NumberType.create(stmt, resultDouble, (byte) -1); // -1 for automatic precision resolution
            }
            else if (readAs == ReadAsType.INTEGER) {
                Double resultDouble = new Double(this.ex.getContext().getTypeConverter().convertTo(Integer.class, resultBody));
                result = NumberType.create(stmt, resultDouble, (byte) 0);
            }
            else {
                result = JsonUtil.operonValueFromString(
                    this.ex.getContext().getTypeConverter().convertTo(String.class, resultBody)
                );
            }
        }    
        
        else {
            ErrorUtil.createErrorValueAndThrow(stmt, "CALL", "ERROR", "call -producer: requires params to be String or Object.");
            throw new OperonGenericException("camel-operon :: ");
        }    
        
        return result;
    }

    private Map mapHeaders(OperonValue currentValue, ObjectType headers) {
        List<PairType> headerPairs = ((ObjectType) headers).getPairs();

        Map<String, Object> headerMap = new HashMap<String, Object>();

        for (PairType pair : headerPairs) {
            String key = pair.getKey();
            OperonValue currentValueCopy2 = currentValue;
            pair.getStatement().setCurrentValue(currentValueCopy2);
            
            if (pair.getEvaluatedValue() instanceof StringType) {
                headerMap.put(key.substring(1, key.length() - 1), ((StringType) pair.getEvaluatedValue()).getJavaStringValue());
            }
            else if (pair.getEvaluatedValue() instanceof RawValue) {
                headerMap.put(key.substring(1, key.length() - 1), ((RawValue) pair.getEvaluatedValue()).getBytes());
            }
            else {
                headerMap.put(key.substring(1, key.length() - 1), pair.getEvaluatedValue());
            }
        }
        return headerMap;
    }

    public void setProducerTemplate(ProducerTemplate pt) {
        this.pt = pt;
    }


    private enum ReadAsType {
        JSON("json"), RAW("raw"), STRING("string"), DOUBLE("double"), INTEGER("integer");
        private String readAsType = "json";
        ReadAsType(String type) {
            this.readAsType = type;
        }
        public String getReadAsType() { return this.readAsType; }
    }
}