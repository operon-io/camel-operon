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

public class CamelOperonMimeTypes {
	
	public static final String MIME_APPLICATION_JSON = "application/json";
	public static final String MIME_APPLICATION_JAVA = "application/java"; // true Java types, e.g. ArrayType --> ArrayList, ObjectType --> LinkedHashMap, NullType --> null, etc.
	public static final String MIME_APPLICATION_JAVA_OPERON = "application/java-operon"; // Java Operon-types, e.g. ArrayType, ObjectType, etc.
	public static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
	public static final String UNKNOWN_MIME_TYPE = "UNKNOWN_MIME_TYPE";

}