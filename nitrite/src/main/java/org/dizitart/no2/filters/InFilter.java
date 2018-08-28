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

import lombok.Getter;
import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;

import static org.dizitart.no2.exceptions.ErrorCodes.FE_IN_SEARCH_TERM_NOT_COMPARABLE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.ValidationUtils.validateInFilterValue;

@Getter
@ToString
class InFilter extends BaseFilter {
    private List<Comparable> objectList;

    InFilter(String field, Comparable... values) {
        super(field, values);
        this.objectList = Arrays.asList(values);
    }

    @Override
    public Set<NitriteId> applyFilter(NitriteMap<NitriteId, Document> documentMap) {
        validateInFilterValue(getField(), objectList);

        if (isObjectFilter()) {
            for (int i = 0; i < objectList.size(); i++) {
                if (objectList.get(i) == null
                        || !getNitriteMapper().isValueType(objectList.get(i))) {
                    throw new FilterException(errorMessage("search term " + objectList.get(i)
                                    + " is not a comparable", FE_IN_SEARCH_TERM_NOT_COMPARABLE));
                }

                if (getNitriteMapper().isValueType(objectList.get(i))) {
                    Comparable comparable = (Comparable) getNitriteMapper().asValue(objectList.get(i));
                    objectList.set(i, comparable);
                }
            }
        }

        if (getIndexedQueryTemplate().hasIndex(getField())
                && !getIndexedQueryTemplate().isIndexing(getField()) && objectList != null) {
            ComparableIndexer comparableIndexer = getIndexedQueryTemplate().getComparableIndexer();
            return comparableIndexer.findIn(getField(), objectList);
        } else {
            return matchedSet(documentMap);
        }
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = getFieldValue(document, getField());

            if (fieldValue instanceof Comparable) {
                Comparable comparable = (Comparable) fieldValue;
                if (objectList.contains(comparable)) {
                    nitriteIdSet.add(entry.getKey());
                }
            }
        }
        return nitriteIdSet;
    }
}
