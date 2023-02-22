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

import io.operon.camel.OperonProcessor;
import org.apache.camel.spi.annotations.Language;
import org.apache.camel.support.LanguageSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Language("operon")
public class CamelOperonLanguage extends LanguageSupport {
     // no logger 

    @Override
    public OperonExpression createPredicate(String expression) {
        return createExpression(expression);
    }

    @Override
    public OperonExpression createExpression(String expression) {
        OperonExpression operonExpression = new OperonExpression(expression);
        return operonExpression;
    }
}