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

public class CamelOperonComponent3Test extends CamelTestSupport {

    @Test
    public void testCamelOperonComponent() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case1", null);
        
        List<Exchange> exchanges = mock.getExchanges();
        mock.await();
        for (Exchange ex : exchanges) {
            String messageBody = ex.getIn().getBody(String.class);
            assertTrue(messageBody.startsWith("\""));
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:case1")
                  // query http-api:
                  .setBody(constant("Select: -> http:{url: \"https://api.chucknorris.io/jokes/random\"}"))
                  .to("operon://quotes1?outputResult=true&prettyPrint=true")
                  
                  // set the result of the first query as context of the next query:
                  .setHeader(CamelOperonHeaders.HEADER_INITIAL_VALUE, body())
                  .setBody(constant("Select: $.body.value"))
                  .to("operon://quotes2")
                  .to("mock:result")
                  ;
            }
        };
    }

}
