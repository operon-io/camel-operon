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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Produce;
import org.apache.camel.Predicate;

import io.operon.runner.node.type.NumberType;

public class CamelOperonLanguage5OutputTypeTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate pt;

    // The output is set as application/java_operon, which should keep the result
    // as Operon typed value.
    @Test
    public void testOperonExpr() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.expectedBodiesReceived("123");
        
        pt.sendBody("");
        assertMockEndpointsSatisfied();
        
        assertEquals("io.operon.runner.node.type.NumberType", mock.getReceivedExchanges().get(0).
            getIn().getBody().getClass().getName());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct://start")
                  .doTry()
                    .setHeader("inputMimeType", constant("application/json"))
                    .setHeader("outputMimeType", constant("application/java-operon"))
                    .setBody().language("operon", "Select: 123")
                    .to("mock:result")
                  .doCatch(Exception.class)
                    .setBody().constant("Error occured.")
                    .to("mock:error")
                  .end();
            }
        };
    }
}

