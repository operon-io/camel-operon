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
public class CamelOperonLanguage3ContentTypeTest extends CamelTestSupport {

    public class Foo {
        private String name = "Bar";
        private Map<String, String> bins;
        public Foo() {
            this.bins = new HashMap<String, String>();
            this.bins.put("bin", "bai");
            this.bins.put("baa", "baba");
            
        }
    }

    @Produce(uri = "direct:start")
    protected ProducerTemplate pt;

    @Test
    public void testOperonExpr() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.expectedBodiesReceived("{\"name\": \"Bar\", \"bins\": {\"baa\": \"baba\", \"bin\": \"bai\"}}");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("operonScript", "Select: $");
        headers.put("content_type", "application/java");
        Foo foo = new Foo();
        pt.sendBodyAndHeaders(foo, headers);
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

