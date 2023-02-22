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

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.support.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

import java.util.concurrent.ExecutorService;

/**
 * camel-operon-component which executes operon-queries. Please find out https://operon.io.
 *
 */
@UriEndpoint(firstVersion = "1.0-SNAPSHOT", scheme = "operon", title = "camel-operon-component", syntax="operon:name",
             category = {Category.JAVA}, producerOnly = true)
public class CamelOperonEndpoint extends DefaultEndpoint {

    @UriPath(defaultValue = "", description = "Identifies the operon, used in logging.") @Metadata(required = false)
    private String name;
    
    @UriParam(defaultValue = "", description = "file://path/to/queryfile.op or classpath://classpath/to/queryFile.op. Omitting file:// or classpath:// assumes that queryFile is in the classpath.")
    private String queryFile;

    @UriParam(description = "Output the result in to the System.out")
    private boolean outputResult = false;

    @UriParam(description = "Formats the result")
    private boolean prettyPrint = false;

    @UriParam(defaultValue = "", description = "Timezone for date-functions")
    private String timezone;

    @UriParam(description = "Prints how long the query took to execute")
    private boolean printDuration = false;

    @UriParam(description = "Prints some debug-information during the execution")
    private boolean debug = false;

    @UriParam(description = "Set to index the root-object")
    private boolean indexRoot = false;

    @UriParam(description = "Throws Java-exception when Operon Error-value was returned")
    private boolean throwOnError = false;

    public CamelOperonEndpoint() {}

    public CamelOperonEndpoint(String uri, CamelOperonComponent component) {
        super(uri, component);
    }

    public Producer createProducer() throws Exception {
        return new CamelOperonProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new Exception("camel-operon does not support consumer");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setQueryFile(String queryFile) {
        this.queryFile = queryFile;
    }
    
    public String getQueryFile() {
        return this.queryFile;
    }

    public void setOutputResult(boolean or) {
        this.outputResult = or;
    }

    public boolean isOutputResult() {
        return this.outputResult;
    }

    public void setPrettyPrint(boolean pp) {
        this.prettyPrint = pp;
    }

    public boolean isPrettyPrint() {
        return this.prettyPrint;
    }

    public void setTimezone(String tz) {
        this.timezone = tz;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public void setPrintDuration(boolean pd) {
        this.printDuration = pd;
    }

    public boolean isPrintDuration() {
        return this.printDuration;
    }

    public void setIndexRoot(boolean ir) {
        this.indexRoot = ir;
    }

    public boolean isIndexRoot() {
        return this.indexRoot;
    }

    public void setThrowOnError(boolean t) {
        this.throwOnError = t;
    }

    public boolean isThrowOnError() {
        return this.throwOnError;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public ExecutorService createExecutor() {
        return getCamelContext().getExecutorServiceManager().newSingleThreadExecutor(this, "CamelOperonConsumer");
    }
}
