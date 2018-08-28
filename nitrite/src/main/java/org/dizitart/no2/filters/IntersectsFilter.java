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
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.SpatialIndexer;
import org.dizitart.no2.store.NitriteMap;
import org.locationtech.jts.geom.Geometry;

import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.FE_INTERSECTS_FILTER_FIELD_NOT_INDEXED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee
 */
@ToString
class IntersectsFilter extends SpatialFilter {
    IntersectsFilter(String field, Geometry geometry) {
        super(field, geometry);
    }

    @Override
    public Set<NitriteId> applyFilter(NitriteMap<NitriteId, Document> documentMap) {
        Geometry geometry = getGeometry();

        if (getIndexedQueryTemplate().hasIndex(getField())
                && !getIndexedQueryTemplate().isIndexing(getField())) {
            SpatialIndexer spatialIndexer = getIndexedQueryTemplate().getSpatialIndexer();
            return spatialIndexer.findIntersects(getField(), geometry);
        } else {
            throw new FilterException(errorMessage(getField() + " is not indexed",
                    FE_INTERSECTS_FILTER_FIELD_NOT_INDEXED));
        }
    }
}
