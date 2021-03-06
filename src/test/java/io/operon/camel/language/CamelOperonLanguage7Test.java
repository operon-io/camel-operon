package io.operon.camel.language;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Produce;

public class CamelOperonLanguage7Test extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate pt;

    // The output is set as application/octet-stream, which should keep the result
    // as an byte-array.
    @Test
    public void testOperonExpr() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.expectedBodiesReceived("RAW VALUE");
        
        pt.sendBody("");
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct://start")
                  .doTry()
                    .setHeader("INPUTMIMETYPE", constant("application/octet-stream"))
                    .setHeader("OUTPUTMIMETYPE", constant("application/octet-stream"))
                    .setHeader("INITIALVALUE", constant("RAW VALUE".getBytes()))
                    .setBody().language("operon", "Select: $")
                    .to("mock:result")
                  .doCatch(Exception.class)
                    .setBody().constant("Error occured.")
                    .to("mock:error")
                  .end();
            }
        };
    }
}

