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

package org.dizitart.no2.filters;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.index.IndexedQueryTemplate;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteMap;

import java.util.Set;

import static org.dizitart.no2.util.ValidationUtils.validateSearchTerm;

/**
 * An abstract implementation of {@link Filter}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Slf4j
public abstract class BaseFilter implements Filter {
    private String field;
    private Object value;
    private IndexedQueryTemplate indexedQueryTemplate;
    private NitriteMapper nitriteMapper;

    protected BaseFilter(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    protected NitriteMapper getNitriteMapper() {
        return nitriteMapper;
    }

    protected IndexedQueryTemplate getIndexedQueryTemplate() {
        return indexedQueryTemplate;
    }

    protected boolean isObjectFilter() {
        return this.nitriteMapper != null;
    }

    @Override
    public void setIndexedQueryTemplate(IndexedQueryTemplate indexedQueryTemplate) {
        this.indexedQueryTemplate = indexedQueryTemplate;
    }

    @Override
    public void setNitriteMapper(NitriteMapper nitriteMapper) {
        this.nitriteMapper = nitriteMapper;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        if (isObjectFilter()) {
            validateSearchTerm(getNitriteMapper(), field, value);
            if (getNitriteMapper().isValueType(value)) {
                value = getNitriteMapper().asValue(value);
            }
        }
        return value;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (isObjectFilter()) {
            validateSearchTerm(nitriteMapper, field, value);
        }
        return applyFilter(documentMap);
    }

    public abstract Set<NitriteId> applyFilter(NitriteMap<NitriteId, Document> documentMap);
}
