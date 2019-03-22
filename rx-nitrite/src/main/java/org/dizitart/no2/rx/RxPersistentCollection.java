package org.dizitart.no2.rx;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.IndexOptions;
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

    Completable dropAllIndices(String field);

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

    Flowable<T> observe(NitriteId nitriteId, BackpressureStrategy backpressureStrategy);

    Flowable<T> observeAll(BackpressureStrategy backpressureStrategy);
}
