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

package io.operon.camel;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.operon.camel.model.CamelOperonHeaders;

public class CamelOperonComponentValueBindingsTest extends CamelTestSupport {

    @Test
    public void testCamelOperonComponent() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case1", null);
        
        List<Exchange> exchanges = mock.getExchanges();
        mock.await();
        for (Exchange ex : exchanges) {
            String messageBody = ex.getIn().getBody(String.class);
            assertEquals("\"bar\"", messageBody);
        }
    }

    @Test
    public void testCamelOperonComponent2() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result2");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case2", null);
        
        List<Exchange> exchanges = mock.getExchanges();
        mock.await();
        for (Exchange ex : exchanges) {
            String messageBody = ex.getIn().getBody(String.class);
            assertEquals("100", messageBody);
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        
        Map<String, String> valueBindings = new HashMap<String, String>();
        valueBindings.put("foo", "\"bar\"");
        valueBindings.put("bin", "100");
        
        return new RouteBuilder() {
            public void configure() {
                from("direct:case1")
                  .setHeader("OPERONVALUEBINDINGS", constant(valueBindings))
                  .setBody(constant("Select: $foo"))
                  .to("operon://bar")
                  .to("mock:result");
                  
                from("direct:case2")
                  .setHeader(CamelOperonHeaders.HEADER_OPERON_VALUE_BINDINGS, constant(valueBindings))
                  .setBody(constant("Select: $bin"))
                  .to("operon://bar2")
                  .to("mock:result2");
            }
        };
    }

}
