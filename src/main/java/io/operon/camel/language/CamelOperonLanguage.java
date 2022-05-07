package io.operon.camel.language;

import io.operon.camel.OperonProcessor;
import org.apache.camel.spi.annotations.Language;
import org.apache.camel.support.LanguageSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Language("operon")
public class CamelOperonLanguage extends LanguageSupport {
     // no logger 

    @Override
    public OperonExpression createPredicate(String expression) {
        return createExpression(expression);
    }

    @Override
    public OperonExpression createExpression(String expression) {
        OperonExpression operonExpression = new OperonExpression(expression);
        return operonExpression;
    }
}