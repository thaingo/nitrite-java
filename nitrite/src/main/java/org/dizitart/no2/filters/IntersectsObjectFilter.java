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

import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.store.NitriteMap;
import org.locationtech.jts.geom.Geometry;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@ToString
class IntersectsObjectFilter extends BaseObjectFilter {
    private String field;
    private Geometry geometry;

    IntersectsObjectFilter(String field, Geometry geometry) {
        this.field = field;
        this.geometry = geometry;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        Filter intersects = new IntersectsFilter(field, geometry);
        intersects.setIndexedQueryTemplate(indexedQueryTemplate);
        return intersects.apply(documentMap);
    }
}
