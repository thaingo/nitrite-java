package org.dizitart.no2.rx;

import io.reactivex.*;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.common.event.ChangeType;
import org.dizitart.no2.common.event.ChangedItem;
import org.dizitart.no2.index.Index;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
public interface RxPersistentCollection<T> {
    Completable createIndex(String field, IndexOptions indexOptions);

    Completable rebuildIndex(String field, boolean async);

    Single<Collection<Index>> listIndices();

    Single<Boolean> hasIndex(String field);

    Single<Boolean> isIndexing(String field);

    Completable dropIndex(String field);

    Completable dropAllIndices();

    FlowableWriteResult insert(T[] items);

    FlowableWriteResult update(T element);

    FlowableWriteResult update(T element, boolean upsert);

    FlowableWriteResult remove(T element);

    Single<T> getById(NitriteId nitriteId);

    Completable drop();

    Single<Boolean> isDropped();

    Single<Boolean> isClosed();

    Completable close();

    String getName();

    Single<Long> size();

    Observable<ChangedItem<T>> observe();

    Observable<ChangedItem<T>> observe(ChangeType changeType);
}
