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

//
// Function that adds "!" into input-string.
//
public class AcmeFunc implements OperonFunction {
	private static final Logger LOG = LoggerFactory.getLogger(AcmeFunc.class);

    public AcmeFunc() { }

    public OperonValue execute(Statement stmt, OperonValue params) throws OperonGenericException {
        LOG.debug("Calling AcmeFunc...");
        
        params = params.evaluate();
        
        OperonValue currentValue = stmt.getCurrentValue();
        OperonValue result = currentValue;
        
        if (params instanceof StringType) {
            String value = ((StringType) params.evaluate()).getJavaStringValue();
            value += "!";
            result = StringType.create(stmt, value);
        }
        
        else if (params instanceof ObjectType) {
            List<PairType> jsonPairs = ((ObjectType) params).getPairs();
            
            String value = "";
            
            for (PairType pair : jsonPairs) {
                String key = pair.getKey();
                OperonValue currentValueCopy2 = currentValue;
                pair.getStatement().setCurrentValue(currentValueCopy2);
                switch (key.toLowerCase()) {
                    case "\"value\"":
                        OperonValue valueNode = (OperonValue) pair.getEvaluatedValue();
                        value = ((StringType) valueNode).getJavaStringValue();
                        value += "!";
                        break;
                    
                    default:
                        LOG.debug("call-acme: no mapping for configuration key: " + key);
                        ErrorUtil.createErrorValueAndThrow(stmt, "CALL", "ERROR", "call-acme: no mapping for configuration key: " + key);
                }
            }
            
            result = StringType.create(stmt, value);
        }
        
        return result;
    }

}