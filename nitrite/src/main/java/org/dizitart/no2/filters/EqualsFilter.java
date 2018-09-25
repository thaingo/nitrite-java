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
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;
import static org.dizitart.no2.exceptions.ErrorCodes.FE_EQUAL_NOT_COMPARABLE;
import static org.dizitart.no2.exceptions.ErrorCodes.FE_EQ_NOT_SPATIAL;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

@Getter
@ToString
class EqualsFilter extends BaseFilter {

    EqualsFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        Object value = getValue();

        if (getField().equals(DOC_ID)) {
            Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
            NitriteId nitriteId = null;
            if (value instanceof Long) {
                nitriteId = NitriteId.createId((Long) value);
            }

            if (nitriteId != null) {
                if (documentMap.containsKey(nitriteId)) {
                    nitriteIdSet.add(nitriteId);
                }
            }
            return nitriteIdSet;
        } else if (getIndexedQueryTemplate().hasIndex(getField())
                && !getIndexedQueryTemplate().isIndexing(getField())
                && value != null) {

            if (getIndexedQueryTemplate().findIndex(getField()).getIndexType() == IndexType.Spatial) {
                throw new FilterException(errorMessage("eq cannot be used as a spatial filter, " +
                        "use geoEq instead.", FE_EQ_NOT_SPATIAL));
            }

            if (value instanceof Comparable) {
                ComparableIndexer comparableIndexer = getIndexedQueryTemplate().getComparableIndexer();
                return comparableIndexer.findEqual(getField(), (Comparable) value);
            } else {
                throw new FilterException(errorMessage(value + " is not comparable",
                        FE_EQUAL_NOT_COMPARABLE));
            }
        } else {
            return matchedSet(documentMap);
        }
    }

    private Set<NitriteId> matchedSet(NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        Object value = getValue();

        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = document.get(getField());
            if (deepEquals(fieldValue, value)) {
                nitriteIdSet.add(entry.getKey());
            }
        }
        return nitriteIdSet;
    }
}
