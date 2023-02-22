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

import io.operon.camel.language.model.*;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;

public abstract class OperonRouteBuilder extends RouteBuilder {
    
    // Called from component (not from language)
    public ValueBuilder operon(String value) {
        return operon(value, null, null); // value, inputMimeType, outputMimeType
    }
    
    public ValueBuilder operon(Expression expression) {
        return operon(expression, null, null);
    }
    
    public ValueBuilder operon(String value, String inputMimeType, String outputMimeType) {
        OperonExpression exp = new OperonExpression(value);
        exp.setInputMimeType(inputMimeType);
        exp.setOutputMimeType(outputMimeType);
        return new ValueBuilder(exp);
    }
    
    public ValueBuilder operon(Expression expression, String inputMimeType, String outputMimeType) {
        OperonExpression exp = new OperonExpression(expression);
        exp.setInputMimeType(inputMimeType);
        exp.setOutputMimeType(outputMimeType);
        return new ValueBuilder(exp);
    }
}
