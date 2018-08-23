/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.mapper;

import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.JSON_SERIALIZATION_FAILED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * A jackson based {@link MapperFacade} implementation. It uses
 * jackson's {@link ObjectMapper} to convert an object into a
 * Nitrite {@link Document}.
 *
 * @author Anindya Chatterjee
 * @author Stefan Mandel
 * @since 3.0.1
 */
@Slf4j
public class JacksonFacade implements MapperFacade {
    private ObjectMapper objectMapper;
    private JacksonMapper jacksonMapper;

    /**
     * Instantiates a new {@link JacksonFacade}.
     */
    public JacksonFacade() {
        this.jacksonMapper = new JacksonMapper();
        this.objectMapper = jacksonMapper.getObjectMapper();
    }

    /**
     * Instantiates a new {@link JacksonFacade}.
     *
     * @param modules jackson {@link Module} to register
     */
    public JacksonFacade(Set<Module> modules) {
        this.jacksonMapper = new JacksonMapper(modules);
        this.objectMapper = jacksonMapper.getObjectMapper();
    }

    @Override
    public Document parse(String json) {
        try {
            JsonNode node = objectMapper.readValue(json, JsonNode.class);
            return jacksonMapper.loadDocument(node);
        } catch (IOException e) {
            log.error("Error while parsing json", e);
            throw new ObjectMappingException(errorMessage("failed to parse json " + json, OME_PARSE_JSON_FAILED));
        }
    }

    @Override
    public String toJson(Object object) {
        try {
            StringWriter stringWriter = new StringWriter();
            objectMapper.writeValue(stringWriter, object);
            return stringWriter.toString();
        } catch (IOException e) {
            log.error("Error while serializing object to json", e);
            throw new ObjectMappingException(JSON_SERIALIZATION_FAILED);
        }
    }
}
