package io.operon.camel.language;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Produce;
import io.operon.camel.model.CamelOperonHeaders;
import io.operon.camel.model.CamelOperonMimeTypes;
import io.operon.runner.node.type.*;

public class CamelOperonLanguage10OutputTypeMapTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate pt;


    @Test
    public void testOperonExpr() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(3);
        
        pt.sendBody("call to direct:start");
        assertMockEndpointsSatisfied();
        
        List<Exchange> exs = mock.getReceivedExchanges();

        assertEquals("java.util.LinkedHashMap$Entry", exs.get(0).getIn().getBody().getClass().getName());

        assertEquals("10.0", exs.get(0).getIn().getBody(Map.Entry.class).getValue().toString());
        assertEquals("20.0", exs.get(1).getIn().getBody(Map.Entry.class).getValue().toString());
        assertEquals("30.0", exs.get(2).getIn().getBody(Map.Entry.class).getValue().toString());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct://start")
                    .setHeader(CamelOperonHeaders.HEADER_OUTPUT_MIME_TYPE)
                        .constant(CamelOperonMimeTypes.MIME_APPLICATION_JAVA)
                    
                    .setHeader(CamelOperonHeaders.HEADER_INITIAL_VALUE)
                        .constant("{bin: 10, bai: 20, baa: 30}")
                    
                    .split()
                        .language("operon", "Select: $")
                        .to("mock:result")
                    .end()
                    ;
            }
        };
    }
}

