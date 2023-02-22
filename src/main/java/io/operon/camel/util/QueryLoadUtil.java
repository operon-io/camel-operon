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
package io.operon.camel.util;

import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;

import io.operon.runner.OperonContext;
import io.operon.runner.model.exception.OperonGenericException;
import io.operon.runner.processor.function.core.module.ModuleAdd;

import io.operon.camel.model.exception.*;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryLoadUtil {
	private static final Logger LOG = LoggerFactory.getLogger(QueryLoadUtil.class);

    public static String loadQueryFile(String queryFile) throws IOException, URISyntaxException {
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
            try {
                path = Paths.get(qfUri);
            } catch (java.nio.file.FileSystemNotFoundException fsnfe) {
                LOG.error("File-system not found. Path was: " + qfUri);
            }
        }
        
        String result = new String(Files.readAllBytes(path));
        return result;
    }

    public static String loadModuleFile(String moduleFile) throws IOException, URISyntaxException {
        Path path = null;
        
        if (moduleFile.startsWith("file://")) {
            path = Paths.get(moduleFile.substring(7, moduleFile.length()));
        }
        
        else {
            URL qfUrl = ObjectHelper.loadResourceAsURL(moduleFile);
            if (qfUrl == null) {
                LOG.info("Could not load resource from classpath: " + moduleFile);
                throw new IOException("Could not load resource from classpath: " + moduleFile);
            }
            URI qfUri = qfUrl.toURI();
            path = Paths.get(qfUri);
        }
        
        String result = new String(Files.readAllBytes(path));
        return result;
    }

}