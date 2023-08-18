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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import io.operon.camel.model.CamelOperonHeaders;
import io.operon.camel.model.CamelOperonMimeTypes;

public class CamelOperonComponentTest extends CamelTestSupport {

    @Test
    public void testCamelOperonComponent() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(5);
        
        for (int i = 0; i < 5; i ++) {
            template.sendBody("direct:case1", i);
        }
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent2() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case1", 1L);
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent3() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case1", (short) 1);
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent4() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case1", (double) 1);
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent5() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        Double doubleValue = new Double(3.141);
        template.sendBody("direct:case1", doubleValue);
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent6() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case1", (float) 1);
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent7() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case1", true);
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent8() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        template.sendBody("direct:case1", false);
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent9() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        List<String> list = new ArrayList<String>();
        list.add("\"bin\"");
        list.add("\"bai\"");
        list.add("\"baa\"");
        
        template.sendBody("direct:case1", list);
        
        mock.await();
    }

    @Test
    public void testCamelOperonComponent10() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("bin", "\"bin\"");
        map.put("bai", "\"bai\"");
        map.put("baa", "\"baa\"");
        
        template.sendBody("direct:case1", map);
        
        mock.await();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:case1")
                  .setHeader(CamelOperonHeaders.INITIAL_VALUE).body()
                  .setHeader(CamelOperonHeaders.INPUT_MIME_TYPE).constant(CamelOperonMimeTypes.APPLICATION_JAVA)
                  .setBody(constant("Select: $->out:debug"))
                  .to("operon://bar")
                  .to("mock:result");
            }
        };
    }

}
