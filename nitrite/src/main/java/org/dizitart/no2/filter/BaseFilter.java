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

package org.dizitart.no2.filter;

import org.dizitart.no2.collection.objects.ObjectFilter;
import org.dizitart.no2.index.IndexedQueryTemplate;
import org.dizitart.no2.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee
 */
public abstract class BaseFilter implements ObjectFilter {
    private NitriteMapper nitriteMapper;
    private IndexedQueryTemplate indexedQueryTemplate;
    private String field;
    private Object value;

    public BaseFilter(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public void setNitriteMapper(NitriteMapper nitriteMapper) {
        this.nitriteMapper = nitriteMapper;
    }

    @Override
    public void setIndexedQueryTemplate(IndexedQueryTemplate indexedQueryTemplate) {
        this.indexedQueryTemplate = indexedQueryTemplate;
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

    protected String getField() {
        return field;
    }

    protected Object getValue() {
        return value;
    }
}

