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

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;

import java.util.Set;

import static org.dizitart.no2.util.ValidationUtils.validateSearchTerm;

class ComparisonFilter extends BaseFilter {
    private Comparable comparable;
    private Comparison comparison;

    public ComparisonFilter(String field, Comparable value, Comparison comparison) {
        super(field, value);
        this.comparable = value;
        this.comparison = comparison;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (isObjectFilter()) {
            validateSearchTerm(getNitriteMapper(), getField(), comparable);

            if (getNitriteMapper().isValueType(getValue())) {
                comparable = (Comparable) getNitriteMapper().asValue(getValue());
            }
        }

        switch (comparison) {
            case LessThan:
                break;
            case LessThanEqual:
                break;
            case GreaterThan:
                break;
            case GreaterThanEqual:

                break;
        }


        return null;
    }
}

enum Comparison {
    LessThan,
    LessThanEqual,
    GreaterThan,
    GreaterThanEqual
}
