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
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Produce;

import io.operon.camel.model.CamelOperonHeaders;

/**
 * Tests for running Operon
 * 
 */
public class CamelOperonLanguage13Test extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate pt;

    @Produce
    protected ProducerTemplate userPt;

    @Test
    public void testOperonExpr() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.expectedBodiesReceived("\"Hello FOO\"");
        
        pt.sendBody("");
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct://start")
                    .log("Camel running direct:start")
                    .setHeader(CamelOperonHeaders.HEADER_PRODUCER_TEMPLATE, constant(userPt))
                    .setBody().language("operon", 
                        "Select: 123 -> call:camel:{params: {nullBody: true, uri: \"direct:foo\", readAs: \"String\"}} => upperCase() => raw() -> call:camel:{params: {uri: \"direct:setHello\", readAs: \"String\"}}")
                    .to("mock:result")
                ;
                
                from("direct:setHello")
                    .log("Setting value \"Hello\" to body")
                    .setBody(simple("Hello ${body}"))
                ;
                
                from("direct:foo")
                    .log("Setting value \"Foo\" to body. Body now :: ${body}")
                    .setBody(constant("Foo"))
                ;
            }
        };
    }
}

