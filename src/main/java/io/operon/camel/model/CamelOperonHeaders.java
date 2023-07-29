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
package io.operon.camel.model;

public class CamelOperonHeaders {
	
    public static final String HEADER_OUTPUT_MIME_TYPE = "outputMimeType";
    public static final String HEADER_INPUT_MIME_TYPE = "inputMimeType";
    public static final String HEADER_LANGUAGE_SCRIPT = "operonScript";
    public static final String HEADER_LANGUAGE_SCRIPT_PATH = "operonScriptPath";
    public static final String HEADER_INITIAL_VALUE = "initialValue";
    public static final String HEADER_OPERON_INDEX_LIST = "operonIndexList"; // List of values to be indexed
    public static final String HEADER_OPERON_VALUE_BINDINGS = "operonValueBindings";
    public static final String HEADER_OPERON_BIND_LIST = "operonBindList";
    public static final String HEADER_OPERON_MODULES = "operonModules";
    public static final String HEADER_PRODUCER_TEMPLATE = "operonProducerTemplate";
    public static final String HEADER_OPERON_FUNCTIONS = "operonFunctions";
    public static final String HEADER_OPERON_CONFIGS = "operonConfigs";
}