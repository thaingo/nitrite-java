package org.dizitart.no2.rx;

import org.dizitart.no2.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.filters.Filter;

/**
 * @author Anindya Chatterjee
 */
public interface RxObjectRepository<T> extends RxPersistentCollection<T> {

    FlowableWriteResult insert(T object, T... others);

    FlowableWriteResult update(Filter filter, T update);

    FlowableWriteResult update(Filter filter, T update, boolean upsert);

    FlowableWriteResult update(Filter filter, Document update);

    FlowableWriteResult update(Filter filter, Document update, boolean justOnce);

    FlowableWriteResult remove(Filter filter);

    FlowableWriteResult remove(Filter filter, RemoveOptions removeOptions);

    FlowableCursor<T> find();

    FlowableCursor<T> find(Filter filter);

    FlowableCursor<T> find(FindOptions findOptions);

    FlowableCursor<T> find(Filter filter, FindOptions findOptions);

    Class<T> getType();

    RxNitriteCollection getDocumentCollection();
}
