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
        headers.put("inputMimeType", "application/java");
        Foo foo = new Foo();
        pt.sendBodyAndHeaders(foo, headers);
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
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

