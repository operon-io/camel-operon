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
