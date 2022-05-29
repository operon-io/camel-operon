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
import java.util.List;
import java.util.ArrayList;
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

import io.operon.camel.OperonProcessor;
import io.operon.camel.util.QueryLoadUtil;

import com.google.gson.Gson;

public class CamelOperonProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(CamelOperonProducer.class);
    
    private final String HEADER_OPERON_MODULES = "operonModules";
    private CamelOperonEndpoint endpoint;
    private String queryFile = null;
    private String query = null;
    private OperonConfigs configs = null;
    private boolean debug = false;
    private OperonProcessor processor;

    public CamelOperonProducer(CamelOperonEndpoint endpoint) throws IOException, URISyntaxException {
        super(endpoint);
        this.endpoint = endpoint;
        this.configs = new OperonConfigs();
        this.queryFile = endpoint.getQueryFile();
        this.debug = endpoint.isDebug();
        processor = new OperonProcessor();
        processor.setDebug(this.debug);
        
        if (this.queryFile != null) {
            if (debug) {
                LOG.info("queryFile=" + this.queryFile);
            }
            
            this.query = QueryLoadUtil.loadQueryFile(this.queryFile);
        }
        
        configs.setOutputResult(endpoint.isOutputResult());
        configs.setPrettyPrint(endpoint.isPrettyPrint());
        
        if (endpoint.getTimezone() != null && endpoint.getTimezone().isEmpty() == false) {
            configs.setTimezone(endpoint.getTimezone());
        }
        
        configs.setPrintDuration(endpoint.isPrintDuration());
    }

    public void process(Exchange exchange) throws Exception {
        String inputMimeType = null;
        String outputMimeType = null;
        processor.setOperonScript(this.query);
        
        String modulePathsStr = exchange.getIn().getHeader(HEADER_OPERON_MODULES, String.class);
        if (modulePathsStr != null) {
            List<String> modulePaths = new ArrayList<String>();
            String [] paths = modulePathsStr.split(",");
            for (int i = 0; i < paths.length; i ++) {
                String p = paths[i].trim();
                modulePaths.add(p);
            }
            processor.setModulePaths(modulePaths);
            processor.init();
        }
        
        Object result = processor.processMapping(exchange, configs, inputMimeType, outputMimeType);
        exchange.getIn().setBody(result);
    }

}
