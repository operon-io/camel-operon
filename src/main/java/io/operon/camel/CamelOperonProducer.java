/*
 *   Copyright 2022, operon.io
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.operon.runner.OperonContext;
import io.operon.runner.OperonRunner;
import io.operon.runner.model.OperonConfigs;
import io.operon.runner.node.type.OperonValue;
import io.operon.runner.util.JsonUtil;
import io.operon.runner.compiler.CompilerFlags;

public class CamelOperonProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(CamelOperonProducer.class);
    private CamelOperonEndpoint endpoint;
    private String queryFile = null;
    private String query = null;
    private OperonConfigs configs = null;
    private boolean debug = false;

    public CamelOperonProducer(CamelOperonEndpoint endpoint) throws IOException, URISyntaxException {
        super(endpoint);
        this.endpoint = endpoint;
        this.configs = new OperonConfigs();
        this.queryFile = endpoint.getQueryFile();
        this.debug = endpoint.isDebug();
        
        if (this.queryFile != null) {
            if (debug) {
                LOG.info("queryFile=" + this.queryFile);
            }
            
            Path path = null;
            
            if (queryFile.startsWith("file://")) {
                path = Paths.get(queryFile.substring(7, queryFile.length()));
            }
            
            else {
                URL qfUrl = ObjectHelper.loadResourceAsURL(queryFile);
                if (qfUrl == null) {
                    LOG.info("Could not load resource from classpath: " + queryFile);
                    throw new IOException("Could not load resource from classpath: " + queryFile);
                }
                URI qfUri = qfUrl.toURI();
                path = Paths.get(qfUri);
            }
            
            this.query = new String(Files.readAllBytes(path));
        }
        
        configs.setOutputResult(endpoint.isOutputResult());
        configs.setPrettyPrint(endpoint.isPrettyPrint());
        
        if (endpoint.getTimezone() != null && endpoint.getTimezone().isEmpty() == false) {
            configs.setTimezone(endpoint.getTimezone());
        }
        
        configs.setPrintDuration(endpoint.isPrintDuration());
    }

    public void process(Exchange exchange) throws Exception {
        Map<String, Object> headers = exchange.getIn().getHeaders();
        String initialValueJson = (String) headers.get("initialValue");
        OperonValue initialValue = null;
        
        if (initialValueJson != null) {
            if (endpoint.isIndexRoot()) {
                CompilerFlags[] flags = {CompilerFlags.INDEX_ROOT};
                initialValue = JsonUtil.operonValueFromString(initialValueJson, flags);
            }
            else {
                initialValue = JsonUtil.operonValueFromString(initialValueJson);
            }
        }
        
        else {
            
        }
        
        if (this.query == null) {
            this.query = exchange.getIn().getBody(String.class);
        }
        
        OperonValue resultValue = null;
        
        if (initialValue == null) {
            resultValue = OperonRunner.doQuery(query, configs);
        }
        else {
            resultValue = OperonRunner.doQueryWithInitialValue(query, initialValue, configs);
        }
        
        String result = resultValue.toString();
        if (configs.getPrettyPrint()) {
            result = OperonContext.serializeStringAsPrettyJson(result);
        }
        exchange.getIn().setBody(result);
    }

}
