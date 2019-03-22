package org.dizitart.no2.rx;

import io.reactivex.*;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.Index;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
class RxNitriteCollectionImpl implements RxNitriteCollection {
    RxNitriteCollectionImpl(String name, Nitrite nitrite, Scheduler scheduler) {

    }

    @Override
    public FlowableWriteResult insert(Document document, Document... documents) {
        return null;
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update) {
        return null;
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
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
    public FlowableCursor<Document> find() {
        return null;
    }

    @Override
    public FlowableCursor<Document> find(Filter filter) {
        return null;
    }

    @Override
    public FlowableCursor<Document> find(FindOptions findOptions) {
        return null;
    }

    @Override
    public FlowableCursor<Document> find(Filter filter, FindOptions findOptions) {
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
    public FlowableWriteResult insert(Document[] items) {
        return null;
    }

    @Override
    public FlowableWriteResult update(Document element) {
        return null;
    }

    @Override
    public FlowableWriteResult update(Document element, boolean upsert) {
        return null;
    }

    @Override
    public FlowableWriteResult remove(Document element) {
        return null;
    }

    @Override
    public Single<Document> getById(NitriteId nitriteId) {
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
    public Flowable<Document> observe(NitriteId nitriteId, BackpressureStrategy backpressureStrategy) {
        return null;
    }

    @Override
    public Flowable<Document> observeAll(BackpressureStrategy backpressureStrategy) {
        return null;
    }
}
