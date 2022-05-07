package io.operon.camel.language;

import java.util.Map;
import java.util.HashMap;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Produce;

/**
 * Tests for running Operon
 * 
 */
public class CamelOperonLanguage2Test extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate pt;

    @Test
    public void testOperonExpr() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.expectedBodiesReceived("123");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("operonScript", "Select: 123");
        pt.sendBodyAndHeaders(null, headers);
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct://start")
                  .doTry()
                    .setBody().language("operon", null)
                    .to("mock:result")
                  .doCatch(Exception.class)
                    .log("ERROR OCCURED :: ${exception}")
                    .setBody().constant("Error occured.")
                    .to("mock:error")
                  .end();
            }
        };
    }
}

