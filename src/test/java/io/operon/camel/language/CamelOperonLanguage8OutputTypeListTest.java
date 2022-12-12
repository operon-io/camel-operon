package io.operon.camel.language;

import java.util.List;
import java.util.ArrayList;
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

public class CamelOperonLanguage8OutputTypeListTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate pt;


    @Test
    public void testOperonExpr() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        pt.sendBody("call to direct:start");
        assertMockEndpointsSatisfied();
        
        List<Exchange> exs = mock.getReceivedExchanges();
        assertTrue(exs.get(0).getIn().getBody() instanceof ArrayList);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct://start")
                  .doTry()
                    .setHeader(CamelOperonHeaders.HEADER_INPUT_MIME_TYPE)
                        .constant(CamelOperonMimeTypes.MIME_APPLICATION_JSON)
                    
                    .setHeader(CamelOperonHeaders.HEADER_OUTPUT_MIME_TYPE)
                        .constant(CamelOperonMimeTypes.MIME_APPLICATION_JAVA)
                    
                    .setHeader(CamelOperonHeaders.HEADER_INITIAL_VALUE)
                        .constant("[10,20,30]")
                    
                    .setBody()
                        .language("operon", "Select: $")
                    
                    .to("mock:result")
                  .doCatch(Exception.class)
                    .setBody().constant("Error occured.")
                    .to("mock:error")
                  .end();
            }
        };
    }
}

