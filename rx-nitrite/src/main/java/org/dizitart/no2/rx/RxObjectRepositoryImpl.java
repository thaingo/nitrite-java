package org.dizitart.no2.rx;

import io.reactivex.*;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.Index;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
class RxObjectRepositoryImpl<T> implements RxObjectRepository<T> {

    RxObjectRepositoryImpl(String name, Nitrite nitrite, Scheduler scheduler) {

    }

    @Override
    @SafeVarargs
    public final FlowableWriteResult insert(T object, T... others) {
        return null;
    }

    @Override
    public FlowableWriteResult update(Filter filter, T update) {
        return null;
    }

    @Override
    public FlowableWriteResult update(Filter filter, T update, boolean upsert) {
        return null;
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update) {
        return null;
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update, boolean justOnce) {
        return null;
    }

    @Override
    public FlowableWriteResult remove(Filter filter) {
        return null;
    }

    @Override
    public FlowableWriteResult remove(Filter filter, RemoveOptions removeOptions) {
        return null;
    }

    @Override
    public FlowableCursor<T> find() {
        return null;
    }

    @Override
    public FlowableCursor<T> find(Filter filter) {
        return null;
    }

    @Override
    public FlowableCursor<T> find(FindOptions findOptions) {
        return null;
    }

    @Override
    public FlowableCursor<T> find(Filter filter, FindOptions findOptions) {
        return null;
    }

    @Override
    public Class<T> getType() {
        return null;
    }

    @Override
    public RxNitriteCollection getDocumentCollection() {
        return null;
    }

    @Override
    public Completable createIndex(String field, IndexOptions indexOptions) {
        return null;
    }

    @Override
    public Completable rebuildIndex(String field, boolean async) {
        return null;
    }

    @Override
    public Single<Collection<Index>> listIndices() {
        return null;
    }

    @Override
    public Single<Boolean> hasIndex(String field) {
        return null;
    }

    @Override
    public Single<Boolean> isIndexing(String field) {
        return null;
    }

    @Override
    public Completable dropIndex(String field) {
        return null;
    }

    @Override
    public Completable dropAllIndices(String field) {
        return null;
    }

    @Override
    public FlowableWriteResult insert(T[] items) {
        return null;
    }

    @Override
    public FlowableWriteResult update(T element) {
        return null;
    }

    @Override
    public FlowableWriteResult update(T element, boolean upsert) {
        return null;
    }

    @Override
    public FlowableWriteResult remove(T element) {
        return null;
    }

    @Override
    public Single<T> getById(NitriteId nitriteId) {
        return null;
    }

    @Override
    public Completable drop() {
        return null;
    }

    @Override
    public Single<Boolean> isDropped() {
        return null;
    }

    @Override
    public Single<Boolean> isClosed() {
        return null;
    }

    @Override
    public Completable close() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Single<Long> size() {
        return null;
    }

    @Override
    public Flowable<T> observe(NitriteId nitriteId, BackpressureStrategy backpressureStrategy) {
        return null;
    }

    @Override
    public Flowable<T> observeAll(BackpressureStrategy backpressureStrategy) {
        return null;
    }
}
