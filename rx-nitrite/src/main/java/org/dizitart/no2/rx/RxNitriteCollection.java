package org.dizitart.no2.rx;

import org.dizitart.no2.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.filters.Filter;

/**
 * @author Anindya Chatterjee
 */
public interface RxNitriteCollection extends RxPersistentCollection<Document> {
    FlowableWriteResult insert(Document document, Document... documents);

    FlowableWriteResult update(Filter filter, Document update);

    FlowableWriteResult update(Filter filter, Document update, UpdateOptions updateOptions);

    FlowableWriteResult remove(Filter filter);

    FlowableWriteResult remove(Filter filter, RemoveOptions removeOptions);

    FlowableCursor<Document> find();

    FlowableCursor<Document> find(Filter filter);

    FlowableCursor<Document> find(FindOptions findOptions);

    FlowableCursor<Document> find(Filter filter, FindOptions findOptions);

}
