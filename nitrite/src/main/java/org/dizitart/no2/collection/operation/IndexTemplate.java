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
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.common.ExecutorServiceManager;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.index.Index;
import org.dizitart.no2.index.SpatialIndexer;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.store.IndexStore;
import org.locationtech.jts.geom.Geometry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.Constants.FIELD_SEPARATOR;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.validateDocumentIndexField;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
class IndexTemplate {
    private final IndexStore indexStore;
    private final Map<String, AtomicBoolean> indexBuildRegistry;
    private final ExecutorService rebuildExecutor;
    private final TextIndexer textIndexer;
    private final ComparableIndexer comparableIndexer;
    private final SpatialIndexer spatialIndexer;
    private final NitriteMapper nitriteMapper;

    private final Object indexLock = new Object();

    IndexTemplate(NitriteMapper nitriteMapper,
                  IndexStore indexStore,
                  ComparableIndexer comparableIndexer,
                  TextIndexer textIndexer,
                  SpatialIndexer spatialIndexer) {
        this.indexBuildRegistry = new ConcurrentHashMap<>();
        this.rebuildExecutor = ExecutorServiceManager.daemonExecutor();
        this.indexStore = indexStore;
        this.textIndexer = textIndexer;
        this.comparableIndexer = comparableIndexer;
        this.spatialIndexer = spatialIndexer;
        this.nitriteMapper = nitriteMapper;
    }

    void ensureIndex(String field, IndexType indexType, boolean isAsync) {
        Index index;

        synchronized (indexLock) {
            if (!indexStore.hasIndex(field)) {
                // if no index create index
                index = indexStore.createIndex(field, indexType);
            } else {
                // if index already there throw
                throw new IndexingException(errorMessage(
                        "index already exists on " + field, IE_INDEX_EXISTS));
            }
        }

        rebuildIndex(index, isAsync);
    }

    void updateIndexEntry(Document document, NitriteId nitriteId) {
        Set<String> fields = getFields(document);

        for (String field : fields) {
            Index index = indexStore.findIndex(field);
            if (index != null) {
                Object fieldValue = document.get(field);

                if (fieldValue == null) continue;
                validateDocumentIndexField(fieldValue, field);

                // if dirty index and currently indexing is not running, rebuild
                if (indexStore.isDirtyIndex(field)
                        && indexBuildRegistry.get(field) != null
                        && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(index, true);
                } else {
                    IndexType indexType = index.getIndexType();

                    switch (indexType) {
                        case Unique:
                            comparableIndexer.writeIndex(nitriteId, field, (Comparable) fieldValue, true);
                            break;
                        case NonUnique:
                            comparableIndexer.writeIndex(nitriteId, field, (Comparable) fieldValue, false);
                            break;
                        case Fulltext:
                            textIndexer.writeIndex(nitriteId, field, (String) fieldValue, false);
                            break;
                        case Spatial:
                            spatialIndexer.writeIndex(nitriteId, field, parseGeometry(fieldValue), false);
                            break;
                    }
                }
            }
        }
    }

    void removeIndexEntry(Document document, NitriteId nitriteId) {
        Set<String> fields = getFields(document);

        for (String field : fields) {
            Index index = indexStore.findIndex(field);
            if (index != null) {
                Object fieldValue = document.get(field);

                if (fieldValue == null) continue;
                validateDocumentIndexField(fieldValue, field);

                // if dirty index and currently indexing is not running, rebuild
                if (indexStore.isDirtyIndex(field)
                        && indexBuildRegistry.get(field) != null
                        && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(index, true);
                } else {
                    IndexType indexType = index.getIndexType();

                    switch (indexType) {
                        case Unique:
                        case NonUnique:
                            comparableIndexer.removeIndex(nitriteId, field, (Comparable) fieldValue);
                            break;
                        case Fulltext:
                            textIndexer.removeIndex(nitriteId, field, (String) fieldValue);
                            break;
                        case Spatial:
                            spatialIndexer.removeIndex(nitriteId, field, parseGeometry(fieldValue));
                            break;
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    void refreshIndexEntry(Document oldDocument, Document newDocument, NitriteId nitriteId) {
        Set<String> fields = getFields(newDocument);

        for (String field : fields) {
            Index index = indexStore.findIndex(field);
            if (index != null) {
                Object newValue = newDocument.get(field);
                Object oldValue = oldDocument.get(field);

                if (newValue == null) continue;
                if (newValue instanceof Comparable && oldValue instanceof Comparable) {
                    if (((Comparable) newValue).compareTo(oldValue) == 0) continue;
                }

                validateDocumentIndexField(newValue, field);

                // if dirty index and currently indexing is not running, rebuild
                if (indexStore.isDirtyIndex(field) &&
                        indexBuildRegistry.get(field) != null
                        && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(index, true);
                } else {
                    IndexType indexType = index.getIndexType();

                    switch (indexType) {
                        case Unique:
                            comparableIndexer.updateIndex(nitriteId, field, (Comparable) newValue, (Comparable) oldValue, true);
                            break;
                        case NonUnique:
                            comparableIndexer.updateIndex(nitriteId, field, (Comparable) newValue, (Comparable) oldValue, false);
                            break;
                        case Fulltext:
                            textIndexer.updateIndex(nitriteId, field, (String) newValue, null,false);
                            break;
                        case Spatial:
                            spatialIndexer.updateIndex(nitriteId, field, parseGeometry(newValue), parseGeometry(oldValue), false);
                            break;
                    }
                }
            }
        }
    }

    Collection<Index> listIndexes() {
        return indexStore.listIndexes();
    }

    boolean hasIndex(String field) {
        return indexStore.hasIndex(field);
    }

    boolean isIndexing(String field) {
        // has index will only return true, if there is an index on
        // the value and indexing is not running on it
        return indexStore.hasIndex(field)
                && indexBuildRegistry.get(field) != null
                && indexBuildRegistry.get(field).get();
    }

    Index findIndex(String field) {
        return indexStore.findIndex(field);
    }

    void dropIndex(String field) {
        if (indexBuildRegistry.get(field) != null
                && indexBuildRegistry.get(field).get()) {
            throw new IndexingException(errorMessage(
                    "cannot drop index as indexing is running on " + field,
                    IE_CAN_NOT_DROP_RUNNING_INDEX));
        }

        Index index = findIndex(field);
        if (index != null) {
            switch (index.getIndexType()) {
                case Unique:
                case NonUnique:
                    comparableIndexer.dropIndex(field);
                    break;
                case Fulltext:
                    textIndexer.dropIndex(field);
                    break;
                case Spatial:
                    spatialIndexer.dropIndex(field);
                    break;
            }
        } else {
            throw new IndexingException(errorMessage(
                    field + " is not indexed", IE_DROP_NON_EXISTING_INDEX));
        }

        indexBuildRegistry.remove(field);
    }

    void dropAllIndices() {
        for (Map.Entry<String, AtomicBoolean> entry :indexBuildRegistry.entrySet()) {
            if (entry.getValue() != null && entry.getValue().get()) {
                throw new IndexingException(errorMessage(
                        "cannot drop index as indexing is running on " + entry.getKey(),
                        IE_CAN_NOT_DROP_ALL_RUNNING_INDEX));
            }
        }

        for (Index index : listIndexes()) {
            dropIndex(index.getField());
        }
        indexStore.dropAll();
        indexBuildRegistry.clear();
    }

    // call to this method is already synchronized, only one thread per field
    // can access it only if rebuild is already not running for that field
    void rebuildIndex(final Index index, boolean isAsync) {
        final String field = index.getField();
        if (getBuildFlag(field).compareAndSet(false, true)) {
            if (isAsync) {
                rebuildExecutor.submit(() -> buildIndexInternal(field, index));
            } else {
                buildIndexInternal(field, index);
            }
            return;
        }
        throw new IndexingException(errorMessage(
                "indexing is already running on " + index.getField(),
                IE_REBUILD_INDEX_RUNNING));
    }

    private void buildIndexInternal(final String field, final Index index) {
        try {
            // first put dirty marker
            indexStore.mark(field, true);

            switch (index.getIndexType()) {
                case Unique:
                    comparableIndexer.rebuildIndex(field, true);
                    break;
                case NonUnique:
                    comparableIndexer.rebuildIndex(field, false);
                    break;
                case Fulltext:
                    textIndexer.rebuildIndex(field, false);
                    break;
                case Spatial:
                    spatialIndexer.rebuildIndex(field, false);
                    break;
            }
        } finally {
            // remove dirty marker to denote indexing completed successfully
            // if dirty marker is found in any index, it needs to be rebuild
            indexStore.mark(field, false);
            getBuildFlag(field).set(false);
        }
    }

    private synchronized AtomicBoolean getBuildFlag(String field) {
        AtomicBoolean flag = indexBuildRegistry.get(field);
        if (flag != null) return flag;

        flag = new AtomicBoolean(false);
        indexBuildRegistry.put(field, flag);
        return flag;
    }

    private Geometry parseGeometry(Object fieldValue) {
        Geometry geometry = null;
        if (fieldValue instanceof String) {
            geometry = nitriteMapper.convertValue(fieldValue, Geometry.class);
        } else if (fieldValue instanceof Geometry) {
            geometry = (Geometry) fieldValue;
        }
        return geometry;
    }

    private Set<String> getFields(Document document) {
        return getFieldsInternal(document, "");
    }

    private Set<String> getFieldsInternal(Document document, String prefix) {
        Set<String> fields = new TreeSet<>();
        if (document == null) return fields;

        for (KeyValuePair entry : document) {
            Object value = entry.getValue();
            if (value instanceof Document) {
                if (isNullOrEmpty(prefix)) {
                    fields.addAll(getFieldsInternal((Document) value, entry.getKey()));
                } else {
                    fields.addAll(getFieldsInternal((Document) value, prefix + FIELD_SEPARATOR + entry.getKey()));
                }
            } else if (!(value instanceof Iterable)) {
                if (StringUtils.isNullOrEmpty(prefix)) {
                    fields.add(entry.getKey());
                } else {
                    fields.add(prefix + FIELD_SEPARATOR + entry.getKey());
                }
            }
        }
        return fields;
    }

}
