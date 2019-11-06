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
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.event.ChangeInfo;
import org.dizitart.no2.common.event.ChangeListener;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.*;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.store.IndexStore;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A service class for Nitrite database operations.
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
public class CollectionOperation {
    private NitriteContext nitriteContext;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private IndexTemplate indexTemplate;
    private ReadWriteOperation readWriteOperation;
    private QueryTemplate queryTemplate;
    private IndexStore indexStore;
    private EventBus<ChangeInfo, ChangeListener> eventBus;
    private Lock readLock;
    private Lock writeLock;

    /**
     * Instantiates a new CollectionOperation.
     *
     * @param nitriteMap     the map store
     * @param nitriteContext the nitrite context
     */
    public CollectionOperation(NitriteMap<NitriteId, Document> nitriteMap,
                        NitriteContext nitriteContext,
                        EventBus<ChangeInfo, ChangeListener> eventBus) {
        this.nitriteMap = nitriteMap;
        this.nitriteContext = nitriteContext;
        this.eventBus = eventBus;
        initialize();
    }

    // region Index Operations

    /**
     * Creates an index.
     *
     * @param field     the value
     * @param indexType the index type
     * @param async     asynchronous operation if set to `true`
     */
    public void createIndex(String field, IndexType indexType, boolean async) {
        try {
            writeLock.lock();
            indexTemplate.ensureIndex(field, indexType, async);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Rebuilds an index.
     *
     * @param index   the index
     * @param isAsync asynchronous operation if set to `true`
     */
    public void rebuildIndex(Index index, boolean isAsync) {
        try {
            writeLock.lock();
            indexTemplate.rebuildIndex(index, isAsync);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Finds the index information of a value.
     *
     * @param field the value
     * @return the index information.
     */
    public Index findIndex(String field) {
        try {
            readLock.lock();
            return indexTemplate.findIndex(field);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Drops the index of a value.
     *
     * @param field the value
     */
    public void dropIndex(String field) {
        try {
            writeLock.lock();
            indexTemplate.dropIndex(field);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Specifies if an indexing operation is currently running.
     *
     * @param field the field
     * @return `true` if operation is still running; `false` otherwise.
     */
    public boolean isIndexing(String field) {
        try {
            readLock.lock();
            return indexTemplate.isIndexing(field);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Specifies if a value is indexed.
     *
     * @param field the field
     * @return `true` if indexed; `false` otherwise.
     */
    public boolean hasIndex(String field) {
        try {
            readLock.lock();
            return indexTemplate.hasIndex(field);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Drops all indices.
     */
    public void dropAllIndices() {
        try {
            writeLock.lock();
            indexTemplate.dropAllIndices();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets indices information of all indexed fields.
     *
     * @return the collection of index information.
     */
    public Collection<Index> listIndexes() {
        try {
            readLock.lock();
            return indexTemplate.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    // endregion

    // region Read Write Operations

    /**
     * Inserts documents in the database.
     *
     * @param document  the document to insert
     * @param documents other documents to insert
     * @return the write result
     */
    public WriteResultImpl insert(Document document, Document... documents) {
        try {
            int length = documents == null ? 0 : documents.length;

            writeLock.lock();
            if (length > 0) {
                Document[] array = new Document[length + 1];
                array[0] = document;
                System.arraycopy(documents, 0, array, 1, length);
                return readWriteOperation.insert(array);
            } else {
                return readWriteOperation.insert(document);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Inserts documents in the database.
     *
     * @param documents the documents to insert
     * @return the write result
     */
    public WriteResult insert(Document[] documents) {
        try {
            writeLock.lock();
            return readWriteOperation.insert(documents);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates a document in the database.
     *
     * @param filter        the filter
     * @param update        the update
     * @param updateOptions the update options
     * @return the write result
     */
    public WriteResultImpl update(Filter filter, Document update, UpdateOptions updateOptions) {
        try {
            writeLock.lock();
            return readWriteOperation.update(filter, update, updateOptions);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes documents from the database.
     *
     * @param filter        the filter
     * @param removeOptions the remove options
     * @return the write result
     */
    public WriteResultImpl remove(Filter filter, RemoveOptions removeOptions) {
        try {
            writeLock.lock();
            return readWriteOperation.remove(filter, removeOptions);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Drops a nitrite collection from the store.
     */
    public void dropCollection() {
        try {
            writeLock.lock();
            indexTemplate.dropAllIndices();
            nitriteContext.removeFromRegistry(nitriteMap.getName());
            nitriteMap.drop();
        } finally {
            writeLock.unlock();
        }
    }

    // endregion

    // region Query Operations

    /**
     * Queries the database.
     *
     * @param filter the filter
     * @return the result set
     */
    public DocumentCursor find(Filter filter) {
        try {
            readLock.lock();
            return queryTemplate.find(filter);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns ids of all records stored in the database.
     *
     * @return the result set
     */
    public DocumentCursor find() {
        try {
            readLock.lock();
            return queryTemplate.find();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Queries the database.
     *
     * @param findOptions the find options
     * @return the result set
     */
    public DocumentCursor find(FindOptions findOptions) {
        try {
            readLock.lock();
            return queryTemplate.find(findOptions);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Queries the database.
     *
     * @param filter      the filter
     * @param findOptions the find options
     * @return the result set
     */
    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        try {
            readLock.lock();
            return queryTemplate.find(filter, findOptions);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets a document by its id.
     *
     * @param nitriteId the nitrite id
     * @return the document associated with the id; `null` otherwise.
     */
    public Document getById(NitriteId nitriteId) {
        try {
            readLock.lock();
            return queryTemplate.getById(nitriteId);
        } finally {
            readLock.unlock();
        }
    }

    // endregion

    private TextIndexer getTextIndexer() {
        TextIndexer textIndexer = nitriteContext.getTextIndexer();
        TextTokenizer textTokenizer = getTextTokenizer();

        if (textIndexer == null) {
            textIndexer = new NitriteTextIndexer(nitriteMap, textTokenizer, indexStore);
        }
        return textIndexer;
    }

    private TextTokenizer getTextTokenizer() {
        TextTokenizer textTokenizer = nitriteContext.getTextTokenizer();
        if (textTokenizer == null) {
            textTokenizer = new EnglishTextTokenizer();
        }
        return textTokenizer;
    }

    private void initialize() {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();

        this.indexStore = new NitriteIndexStore(nitriteMap);
        TextIndexer textIndexer = getTextIndexer();
        ComparableIndexer comparableIndexer = new NitriteComparableIndexer(nitriteMap, indexStore);
        SpatialIndexer spatialIndexer = new NitriteSpatialIndexer(nitriteMap, indexStore);
        NitriteMapper nitriteMapper = nitriteContext.getNitriteMapper();
        this.indexTemplate = new IndexTemplate(nitriteMapper, indexStore, comparableIndexer, textIndexer, spatialIndexer);

        IndexedQueryTemplate indexedQueryTemplate
            = new NitriteIndexedQueryTemplate(indexTemplate, comparableIndexer, textIndexer, spatialIndexer);
        this.queryTemplate = new QueryTemplate(indexedQueryTemplate, nitriteMap);
        this.readWriteOperation = new ReadWriteOperation(indexTemplate, queryTemplate, nitriteMap, eventBus);
    }
}
