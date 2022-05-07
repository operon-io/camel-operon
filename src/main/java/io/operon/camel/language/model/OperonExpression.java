package io.operon.camel.language.model;

import org.apache.camel.Expression;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.spi.Metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * To use Operon scripts in Camel expressions or predicates.
 */
@Metadata(firstVersion = "3.3.0", label = "language,operon", title = "Operon")
@XmlRootElement(name = "operon")
@XmlAccessorType(XmlAccessType.FIELD)
public class OperonExpression extends ExpressionDefinition {

    @XmlAttribute(name = "inputMimeType")
    private String inputMimeType;

    @XmlAttribute(name = "outputMimeType")
    private String outputMimeType;

    public OperonExpression() {
    }

    public OperonExpression(String expression) {
        super(expression);
    }

    public OperonExpression(Expression expression) {
        setExpressionValue(expression);
    }

    @Override
    public String getLanguage() {
        return "operon";
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

}
