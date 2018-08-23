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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.SpatialIndexer;
import org.dizitart.no2.spatial.EqualityType;
import org.dizitart.no2.store.IndexStore;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.*;

import static org.dizitart.no2.exceptions.ErrorCodes.IE_REBUILD_INDEX_NON_SPATIAL;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;

/**
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
class NitriteSpatialIndexer implements SpatialIndexer {
    private IndexStore indexStore;
    private NitriteMap<NitriteId, Document> underlyingMap;
    private final Object lock = new Object();

    NitriteSpatialIndexer(NitriteMap<NitriteId, Document> underlyingMap,
                          IndexStore indexStore) {
        this.underlyingMap = underlyingMap;
        this.indexStore = indexStore;
    }

    @Override
    public void writeIndex(NitriteId id, String field, Geometry element, boolean unique) {
        synchronized (lock) {
            NitriteRTreeMap indexMap = indexStore.getSpatialIndexMap(field);
            SpatialKey spatialKey = getKey(id.getIdValue(), element);
            indexMap.add(spatialKey, element);
        }
    }

    @Override
    public void updateIndex(NitriteId id, String field, Geometry newElement, Geometry oldElement, boolean unique) {
        synchronized (lock) {
            NitriteRTreeMap indexMap = indexStore.getSpatialIndexMap(field);
            SpatialKey oldKey = getKey(id.getIdValue(), oldElement);
            SpatialKey newKey = getKey(id.getIdValue(), newElement);
            indexMap.remove(oldKey);
            indexMap.add(newKey, newElement);
        }
    }

    @Override
    public void removeIndex(NitriteId id, String field, Geometry element) {
        synchronized (lock) {
            NitriteRTreeMap indexMap = indexStore.getSpatialIndexMap(field);
            SpatialKey spatialKey = getKey(id.getIdValue(), element);
            indexMap.remove(spatialKey);
        }
    }

    @Override
    public void dropIndex(String field) {
        synchronized (lock) {
            indexStore.dropIndex(field);
        }
    }

    @Override
    public void rebuildIndex(String field, boolean unique) {
        NitriteRTreeMap indexMap = indexStore.getSpatialIndexMap(field);
        indexMap.clear();

        for (Map.Entry<NitriteId, Document> entry : underlyingMap.entrySet()) {
            // create the document
            Document object = entry.getValue();

            // retrieved the value from document
            Object fieldValue = getFieldValue(object, field);

            if (fieldValue == null) continue;

            if (!(fieldValue instanceof Geometry)) {
                throw new IndexingException(errorMessage(field + " field is not a spatial field",
                        IE_REBUILD_INDEX_NON_SPATIAL));
            }

            Geometry geometry = (Geometry) fieldValue;
            SpatialKey spatialKey = getKey(entry.getKey().getIdValue(), geometry);
            indexMap.add(spatialKey, geometry);
        }
    }

    @Override
    public Set<NitriteId> findEqual(String field, Geometry geometry, EqualityType equalityType) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();

        NitriteRTreeMap indexMap = indexStore.getSpatialIndexMap(field);
        SpatialKey spatialKey = getKey(0L, geometry);

        for (Map.Entry<SpatialKey, Geometry> geometryEntry : indexMap.entrySet()) {
            SpatialKey key = geometryEntry.getKey();
            if (key.equalsIgnoringId(spatialKey)) {
                Geometry geom = geometryEntry.getValue();

                boolean equal = false;
                switch (equalityType) {
                    case Exact:
                        equal = geom.equalsExact(geometry);
                        break;
                    case Normalized:
                        equal = geom.norm().equalsNorm(geometry.norm());
                        break;
                    case Topological:
                        equal = geom.equalsTopo(geometry);
                        break;
                }

                if (equal) {
                    resultSet.add(NitriteId.createId(key.getId()));
                }
            }
        }

        return resultSet;
    }

    @Override
    public Set<NitriteId> findWithin(String field, Geometry geometry) {
        NitriteRTreeMap indexMap = indexStore.getSpatialIndexMap(field);
        SpatialKey spatialKey = getKey(0L, geometry);
        Iterator<SpatialKey> containedKeys = indexMap.findContainedKeys(spatialKey);
        return findNitriteIds(containedKeys);
    }

    @Override
    public Set<NitriteId> findIntersects(String field, Geometry geometry) {
        NitriteRTreeMap indexMap = indexStore.getSpatialIndexMap(field);
        SpatialKey spatialKey = getKey(0L, geometry);
        Iterator<SpatialKey> containedKeys = indexMap.findIntersectingKeys(spatialKey);
        return findNitriteIds(containedKeys);
    }

    private SpatialKey getKey(Long id, Geometry geometry) {
        if (id == null) {
            return null;
        }

        Envelope env = geometry.getEnvelopeInternal();
        return new SpatialKey(id, (float) env.getMinX(), (float) env.getMaxX(),
                (float) env.getMinY(), (float) env.getMaxY());
    }

    private Set<NitriteId> findNitriteIds(Iterator<SpatialKey> spatialKeyIterator) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        if (spatialKeyIterator != null) {
            while (spatialKeyIterator.hasNext()) {
                SpatialKey key = spatialKeyIterator.next();
                NitriteId nitriteId = NitriteId.createId(key.getId());
                resultSet.add(nitriteId);
            }
        }
        return resultSet;
    }
}
