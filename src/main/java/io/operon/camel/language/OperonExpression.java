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
package io.operon.camel.language;

import java.util.List;
import java.util.ArrayList;

import io.operon.camel.OperonProcessor;
import io.operon.camel.model.CamelOperonHeaders;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.RuntimeExpressionException;
import org.apache.camel.support.ExpressionAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.operon.runner.model.OperonConfigs;


public class OperonExpression extends ExpressionAdapter { //implements GeneratedPropertyConfigurer {
    private String expression;

    private String inputMimeType;
    private String outputMimeType;

    private OperonProcessor processor;
    private Expression innerExpression;
    private OperonConfigs configs;

    // no logger

    public OperonExpression(String expression) {
        this.expression = expression;
        this.processor = new OperonProcessor();
        this.processor.setOperonScript(expression);
        this.configs = new OperonConfigs();
        this.configs.setOutputResult(false);
        
        try {
            processor.init();
        } catch (Exception e) {
            throw new RuntimeExpressionException("Unable to initialize Operon processor : ", e);
        }
    }

    public OperonExpression(Expression expression) {
        this.innerExpression = expression;
        processor = new OperonProcessor();
        try {
            processor.init();
        } catch (Exception e) {
            throw new RuntimeExpressionException("Unable to initialize Operon processor : ", e);
        }
    }

    @Override
    public String toString() {
        return "operon: " + expression;
    }

    @Override
    public <T> T evaluate(Exchange exchange, Class<T> type) {
        OperonConfigs overrideConfigs = exchange.getIn().getHeader(CamelOperonHeaders.HEADER_OPERON_CONFIGS, OperonConfigs.class);
        
        if (overrideConfigs != null) {
            this.configs = overrideConfigs;
        }

        if (inputMimeType != null) {
            exchange.setProperty("inputMimeType", inputMimeType);
        }
        else {
            String inMimeType = (String) exchange.getIn().getHeader(CamelOperonHeaders.HEADER_INPUT_MIME_TYPE);
            if (inMimeType != null) {
                inputMimeType = inMimeType;
            }
        }

        if (outputMimeType != null) {
            exchange.setProperty("outputMimeType", outputMimeType);
        }
        else {
            String outMimeType = (String) exchange.getIn().getHeader(CamelOperonHeaders.HEADER_OUTPUT_MIME_TYPE);
            if (outMimeType != null) {
                outputMimeType = outMimeType;
            }
        }
        try {
            if (innerExpression != null) {
                String script = innerExpression.evaluate(exchange, String.class);
                expression = script;
                processor.setOperonScript(script);
            }
            String modulePathsStr = exchange.getIn().getHeader(CamelOperonHeaders.HEADER_OPERON_MODULES, String.class);
            if (modulePathsStr != null) {
                List<String> modulePaths = new ArrayList<String>();
                String [] paths = modulePathsStr.split(",");
                for (int i = 0; i < paths.length; i ++) {
                    String p = paths[i].trim();
                    modulePaths.add(p);
                }
                processor.setModulePaths(modulePaths);
            }
            Object value = processor.processMapping(exchange, this.configs, inputMimeType, outputMimeType);
            return exchange.getContext().getTypeConverter().convertTo(type, value);
        } catch (Exception e) {
            throw new RuntimeExpressionException("Unable to evaluate Operon expression : " + expression, e);
        }
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

    public Expression getInnerExpression() {
        return innerExpression;
    }

    public void setInnerExpression(Expression innerExpression) {
        this.innerExpression = innerExpression;
    }
}