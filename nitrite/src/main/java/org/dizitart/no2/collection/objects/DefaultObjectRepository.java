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

package org.dizitart.no2.collection.objects;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.event.ChangeListener;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.Index;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.meta.Attributes;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import static org.dizitart.no2.collection.IndexOptions.indexOptions;
import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * A default implementation of {@link ObjectRepository}.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
class DefaultObjectRepository<T> implements ObjectRepository<T> {
    private NitriteCollection collection;
    private Class<T> type;
    private NitriteMapper nitriteMapper;
    private Field idField;
    private RepositoryDelegate repositoryDelegate;

    DefaultObjectRepository(Class<T> type, NitriteCollection collection,
                            NitriteContext nitriteContext) {
        this.type = type;
        this.collection = collection;
        initRepository(nitriteContext);
    }

    @Override
    public void createIndex(String field, IndexOptions indexOptions) {
        validateCollection();
        notNull(field, errorMessage("field cannot be null", VE_OBJ_CREATE_INDEX_NULL_FIELD));

        collection.createIndex(field, indexOptions);
    }

    @Override
    public void rebuildIndex(String field, boolean async) {
        validateCollection();
        notNull(field, errorMessage("field cannot be null", VE_OBJ_REBUILD_INDEX_NULL_FIELD));

        collection.rebuildIndex(field, async);
    }

    @Override
    public Collection<Index> listIndices() {
        validateCollection();

        return collection.listIndices();
    }

    @Override
    public boolean hasIndex(String field) {
        validateCollection();
        notNull(field, errorMessage("field cannot be null", VE_OBJ_HAS_INDEX_NULL_FIELD));

        return collection.hasIndex(field);
    }

    @Override
    public boolean isIndexing(String field) {
        validateCollection();
        notNull(field, errorMessage("field cannot be null", VE_OBJ_IS_INDEXING_NULL_FIELD));

        return collection.isIndexing(field);
    }

    @Override
    public void dropIndex(String field) {
        validateCollection();
        notNull(field, errorMessage("field cannot be null", VE_OBJ_DROP_INDEX_NULL_FIELD));

        collection.dropIndex(field);
    }

    @Override
    public void dropAllIndices() {
        validateCollection();

        collection.dropAllIndices();
    }

    @SafeVarargs
    @Override
    public final WriteResult insert(T object, T... others) {
        validateCollection();
        notNull(object, errorMessage("null object cannot be inserted", VE_OBJ_INSERT_NULL));
        if (others != null) {
            containsNull(others, errorMessage("null object cannot be inserted",
                    VE_OBJ_INSERT_OTHERS_CONTAINS_NULL));
        }

        return collection.insert(asDocument(object, false), asDocuments(others));
    }

    @Override
    public WriteResult insert(T[] objects) {
        validateCollection();
        notNull(objects, errorMessage("null object cannot be inserted", VE_OBJ_INSERT_ARRAY_NULL));
        containsNull(objects, errorMessage("null object cannot be inserted",
                VE_OBJ_INSERT_ARRAY_CONTAINS_NULL));

        return collection.insert(asDocuments(objects));
    }

    @Override
    public WriteResult update(T element) {
        validateCollection();
        notNull(element, errorMessage("null object cannot be used for update", VE_OBJ_UPDATE_NULL));

        return update(element, false);
    }

    @Override
    public WriteResult update(T element, boolean upsert) {
        validateCollection();
        notNull(element, errorMessage("null object cannot be used for update", VE_OBJ_UPDATE_ELEMENT_NULL));

        if (idField == null) {
            throw new NotIdentifiableException(OBJ_UPDATE_FAILED_AS_NO_ID_FOUND);
        }
        return update(repositoryDelegate.createUniqueFilter(element, idField), element, upsert);
    }

    @Override
    public WriteResult update(Filter filter, T update) {
        validateCollection();
        notNull(update, errorMessage("null object cannot be used for update", VE_OBJ_FILTER_UPDATE_NULL));

        return update(filter, update, false);
    }

    @Override
    public WriteResult update(Filter filter, T update, boolean upsert) {
        validateCollection();
        notNull(update, errorMessage("null object cannot be used for update", VE_OBJ_UPDATE_NULL_OBJECT));

        Document updateDocument = asDocument(update, true);
        removeNitriteId(updateDocument);
        return collection.update(setNitriteMapper(filter), updateDocument, updateOptions(upsert, true));
    }

    @Override
    public WriteResult update(Filter filter, Document update) {
        validateCollection();
        notNull(update, errorMessage("null document cannot be used for update", VE_OBJ_FILTER_UPDATE_NULL_DOCUMENT));

        return update(filter, update, false);
    }

    @Override
    public WriteResult update(Filter filter, Document update, boolean justOnce) {
        validateCollection();
        notNull(update, errorMessage("null document cannot be used for update", VE_OBJ_UPDATE_NULL_DOCUMENT));

        removeNitriteId(update);
        serializeFields(update);
        return collection.update(setNitriteMapper(filter), update, updateOptions(false, justOnce));
    }

    @Override
    public WriteResult remove(T element) {
        validateCollection();
        notNull(element, errorMessage("null object cannot be removed", VE_OBJ_REMOVE_NULL_OBJECT));

        if (idField == null) {
            throw new NotIdentifiableException(OBJ_REMOVE_FAILED_AS_NO_ID_FOUND);
        }
        return remove(repositoryDelegate.createUniqueFilter(element, idField));
    }

    @Override
    public WriteResult remove(Filter filter) {
        validateCollection();

        return remove(setNitriteMapper(filter), new RemoveOptions());
    }

    @Override
    public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
        validateCollection();
        notNull(removeOptions, errorMessage("removeOptions cannot be null", VE_OBJ_REMOVE_OPTION_NULL));

        return collection.remove(setNitriteMapper(filter), removeOptions);
    }

    @Override
    public Cursor<T> find() {
        validateCollection();

        return new ObjectCursor<>(nitriteMapper, collection.find(), type);
    }

    @Override
    public Cursor<T> find(Filter filter) {
        validateCollection();

        return new ObjectCursor<>(nitriteMapper,
                collection.find(setNitriteMapper(filter)), type);
    }

    @Override
    public Cursor<T> find(FindOptions findOptions) {
        validateCollection();
        notNull(findOptions, errorMessage("findOptions cannot be null", VE_OBJ_FIND_OPTION_NULL));

        return new ObjectCursor<>(nitriteMapper,
                collection.find(findOptions), type);
    }

    @Override
    public Cursor<T> find(Filter filter, FindOptions findOptions) {
        validateCollection();
        notNull(findOptions, errorMessage("findOptions cannot be null", VE_OBJ_FILTER_FIND_OPTION_NULL));

        return new ObjectCursor<>(nitriteMapper,
                collection.find(setNitriteMapper(filter), findOptions), type);
    }

    @Override
    public T getById(NitriteId nitriteId) {
        validateCollection();
        notNull(nitriteId, errorMessage("nitriteId cannot be null", VE_OBJ_NITRITE_ID_GET_NULL));

        Document document = collection.getById(nitriteId);
        if (document != null) {
            Document item = document.clone();
            item.remove(DOC_ID);
            return nitriteMapper.asObject(item, type);
        }
        return null;
    }

    @Override
    public void drop() {
        validateCollection();

        collection.drop();
    }

    @Override
    public boolean isDropped() {
        validateCollection();

        return collection.isDropped();
    }

    @Override
    public String getName() {
        validateCollection();

        return collection.getName();
    }

    @Override
    public long size() {
        validateCollection();

        return collection.size();
    }

    @Override
    public boolean isClosed() {
        validateCollection();

        return collection.isClosed();
    }

    @Override
    public void close() {
        validateCollection();

        collection.close();
    }

    @Override
    public Class<T> getType() {
        validateCollection();

        return type;
    }

    @Override
    public NitriteCollection getDocumentCollection() {
        return collection;
    }

    @Override
    public Attributes getAttributes() {
        validateCollection();

        return collection.getAttributes();
    }

    @Override
    public void setAttributes(Attributes attributes) {
        validateCollection();
        notNull(attributes, errorMessage("attributes cannot be null", VE_OBJ_ATTRIBUTE_NULL));

        collection.setAttributes(attributes);
    }

    @Override
    public void register(ChangeListener listener) {
        validateCollection();
        notNull(listener, errorMessage("listener cannot be null", VE_OBJ_LISTENER_NULL));

        collection.register(listener);
    }

    @Override
    public void deregister(ChangeListener listener) {
        validateCollection();
        notNull(listener, errorMessage("listener cannot be null", VE_OBJ_LISTENER_DEREGISTER_NULL));

        collection.deregister(listener);
    }

    private void validateCollection() {
        if (collection == null) {
            throw new ValidationException(REPOSITORY_NOT_INITIALIZED);
        }
    }

    private Document asDocument(T object, boolean update) {
        return repositoryDelegate.toDocument(object, nitriteMapper, idField, update);
    }

    private Document[] asDocuments(T[] others) {
        if (others == null || others.length == 0) return null;
        Document[] documents = new Document[others.length];
        for (int i = 0; i < others.length; i++) {
            documents[i] = asDocument(others[i], false);
        }
        return documents;
    }

    private void initRepository(NitriteContext nitriteContext) {
        repositoryDelegate = new RepositoryDelegate();
        nitriteMapper = nitriteContext.getNitriteMapper();
        createIndexes();
    }

    private void createIndexes() {
        validateCollection();
        Set<org.dizitart.no2.index.annotations.Index> indexes = repositoryDelegate.extractIndices(nitriteMapper, type);
        for (org.dizitart.no2.index.annotations.Index idx : indexes) {
            if (!collection.hasIndex(idx.value())) {
                collection.createIndex(idx.value(), indexOptions(idx.type(), false));
            }
        }

        idField = repositoryDelegate.getIdField(nitriteMapper, type);
        if (idField != null) {
            if (!collection.hasIndex(idField.getName())) {
                collection.createIndex(idField.getName(), indexOptions(IndexType.Unique));
            }
        }
    }

    private Filter setNitriteMapper(Filter filter) {
        if (filter != null) {
            filter.setNitriteMapper(nitriteMapper);
            return filter;
        }
        return null;
    }

    private void removeNitriteId(Document document) {
        document.remove(DOC_ID);
        if (idField != null && idField.getType() == NitriteId.class) {
            document.remove(idField.getName());
        }
    }

    private void serializeFields(Document document) {
        if (document != null) {
            for (KeyValuePair keyValuePair : document) {
                String key = keyValuePair.getKey();
                Object value = keyValuePair.getValue();
                Object serializedValue;
                if (nitriteMapper.isValueType(value)) {
                    serializedValue = nitriteMapper.convertValue(value);
                } else {
                    serializedValue = nitriteMapper.asDocument(value);
                }
                document.put(key, serializedValue);
            }
        }
    }
}
