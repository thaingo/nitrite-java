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

package org.dizitart.no2.collection;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.operation.CollectionOperation;
import org.dizitart.no2.common.event.ChangeInfo;
import org.dizitart.no2.common.event.ChangeListener;
import org.dizitart.no2.common.event.ChangeType;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.Index;
import org.dizitart.no2.meta.Attributes;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;

/**
 * The default implementation of {@link NitriteCollection}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 * */
class DefaultNitriteCollection implements NitriteCollection {
    private NitriteMap<NitriteId, Document> nitriteMap;
    private NitriteStore nitriteStore;
    private CollectionOperation collectionOperation;
    private volatile boolean isDropped;
    private EventBus<ChangeInfo, ChangeListener> eventBus;
    private String collectionName;

    DefaultNitriteCollection(NitriteMap<NitriteId, Document> nitriteMap, NitriteContext nitriteContext) {
        this.nitriteMap = nitriteMap;
        this.nitriteStore = nitriteMap.getStore();
        this.eventBus = new ChangeEventBus();
        this.collectionOperation = new CollectionOperation(nitriteMap, nitriteContext, eventBus);
        this.isDropped = false;
        this.collectionName = nitriteMap.getName();
    }

    @Override
    public void createIndex(String field, IndexOptions indexOptions) {
        checkOpened();
        // by default async is false while creating index
        if (indexOptions == null) {
            collectionOperation.createIndex(field, IndexType.Unique, false);
        } else {
            collectionOperation.createIndex(field, indexOptions.getIndexType(),
                    indexOptions.isAsync());
        }
    }

    @Override
    public void rebuildIndex(String field, boolean async) {
        checkOpened();
        Index index = collectionOperation.findIndex(field);
        if (index != null) {
            validateRebuildIndex(index);
            collectionOperation.rebuildIndex(index, async);
        } else {
            throw new IndexingException(errorMessage(field + " is not indexed",
                    IE_REBUILD_INDEX_FIELD_NOT_INDEXED));
        }
    }

    @Override
    public Collection<Index> listIndices() {
        checkOpened();
        return collectionOperation.listIndexes();
    }

    @Override
    public boolean hasIndex(String field) {
        checkOpened();
        return collectionOperation.hasIndex(field);
    }

    @Override
    public boolean isIndexing(String field) {
        checkOpened();
        return collectionOperation.isIndexing(field);
    }

    @Override
    public void dropIndex(String field) {
        checkOpened();
        collectionOperation.dropIndex(field);
    }

    @Override
    public void dropAllIndices() {
        checkOpened();
        collectionOperation.dropAllIndices();
    }

    @Override
    public WriteResult insert(Document document, Document... documents) {
        checkOpened();
        return collectionOperation.insert(document, documents);
    }

    @Override
    public WriteResult insert(Document[] documents) {
        checkOpened();
        return collectionOperation.insert(documents);
    }

    @Override
    public Cursor find(Filter filter) {
        checkOpened();
        return collectionOperation.find(filter);
    }

    @Override
    public Cursor find(FindOptions findOptions) {
        checkOpened();
        return collectionOperation.find(findOptions);
    }

    @Override
    public Cursor find(Filter filter, FindOptions findOptions) {
        checkOpened();
        return collectionOperation.find(filter, findOptions);
    }

    @Override
    public Cursor find() {
        checkOpened();
        return collectionOperation.find();
    }

    @Override
    public Document getById(NitriteId nitriteId) {
        checkOpened();
        return collectionOperation.getById(nitriteId);
    }

    @Override
    public void drop() {
        checkOpened();
        collectionOperation.dropCollection();
        isDropped = true;
        closeCollection();
        eventBus.post(new ChangeInfo(ChangeType.DROP));
        closeEventBus();
    }

    @Override
    public boolean isDropped() {
        return isDropped;
    }

    @Override
    public boolean isClosed() {
        if (nitriteStore == null || nitriteStore.isClosed() || isDropped) {
            closeCollection();
            closeEventBus();
            return true;
        }
        else return false;
    }

    @Override
    public void close() {
        closeCollection();
        eventBus.post(new ChangeInfo(ChangeType.CLOSE));
        closeEventBus();
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    public long size() {
        checkOpened();
        return nitriteMap.sizeAsLong();
    }

    @Override
    public WriteResult update(Document document) {
        checkOpened();
        if (document.containsKey(DOC_ID)) {
            return update(createUniqueFilter(document), document);
        } else {
            throw new NotIdentifiableException(UPDATE_FAILED_AS_NO_ID_FOUND);
        }
    }

    @Override
    public WriteResult update(Document document, boolean upsert) {
        checkOpened();
        return update(createUniqueFilter(document), document, UpdateOptions.updateOptions(upsert));
    }

    @Override
    public WriteResult update(Filter filter, Document update) {
        checkOpened();
        return update(filter, update, new UpdateOptions());
    }

    @Override
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        checkOpened();
        return collectionOperation.update(filter, update, updateOptions);
    }

    @Override
    public WriteResult remove(Document document) {
        checkOpened();
        notNull(document, errorMessage("document can not be null", VE_NC_REMOVE_NULL_DOCUMENT));
        if (document.containsKey(DOC_ID)) {
            return remove(createUniqueFilter(document));
        } else {
            throw new NotIdentifiableException(REMOVE_FAILED_AS_NO_ID_FOUND);
        }
    }

    @Override
    public WriteResult remove(Filter filter) {
        checkOpened();
        return remove(filter, new RemoveOptions());
    }

    @Override
    public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
        checkOpened();
        return collectionOperation.remove(filter, removeOptions);
    }

    @Override
    public void register(ChangeListener listener) {
        checkOpened();
        eventBus.register(listener);
    }

    @Override
    public void deregister(ChangeListener listener) {
        if (eventBus != null) {
            eventBus.deregister(listener);
        }
    }

    @Override
    public Attributes getAttributes() {
        return nitriteMap != null ? nitriteMap.getAttributes() : null;
    }

    @Override
    public void setAttributes(Attributes attributes) {
        nitriteMap.setAttributes(attributes);
    }

    private void checkOpened() {
        if (isDropped) {
            throw new NitriteIOException(COLLECTION_IS_DROPPED);
        }

        if (nitriteStore == null || nitriteStore.isClosed()) {
            throw new NitriteIOException(STORE_IS_CLOSED);
        }
    }

    private void validateRebuildIndex(Index index) {
        notNull(index, errorMessage("index can not be null", VE_NC_REBUILD_INDEX_NULL_INDEX));

        if (isIndexing(index.getField())) {
            throw new IndexingException(errorMessage("indexing on value " + index.getField() +
                    " is currently running", IE_VALIDATE_REBUILD_INDEX_RUNNING));
        }
    }

    private void closeCollection() {
        nitriteStore = null;
        nitriteMap = null;
        collectionOperation = null;
    }

    private void closeEventBus() {
        if (eventBus != null) {
            eventBus.close();
        }
        eventBus = null;
    }
}
