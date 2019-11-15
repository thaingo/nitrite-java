package org.dizitart.no2.rx;

import io.reactivex.*;
import io.reactivex.subjects.PublishSubject;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.event.ChangeType;
import org.dizitart.no2.common.event.ChangedItem;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.Index;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
class RxNitriteCollectionImpl implements RxNitriteCollection {
    private final NitriteCollection nitriteCollection;
    private final PublishSubject<ChangedItem<Document>> updates;

    RxNitriteCollectionImpl(NitriteCollection collection) {
        this.nitriteCollection = collection;
        this.updates = PublishSubject.create();
        initializeUpdateObserver();
    }

    @Override
    public FlowableWriteResult insert(Document document, Document... documents) {
        return new FlowableWriteResult(() -> nitriteCollection.insert(document, documents));
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update) {
        return new FlowableWriteResult(() -> nitriteCollection.update(filter, update));
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        return new FlowableWriteResult(() -> nitriteCollection.update(filter, update, updateOptions));
    }

    @Override
    public FlowableWriteResult remove(Filter filter) {
        return new FlowableWriteResult(() -> nitriteCollection.remove(filter));
    }

    @Override
    public FlowableWriteResult remove(Filter filter, RemoveOptions removeOptions) {
        return new FlowableWriteResult(() -> nitriteCollection.remove(filter, removeOptions));
    }

    @Override
    public FlowableDocumentCursor find() {
        return new FlowableDocumentCursor(nitriteCollection::find);
    }

    @Override
    public FlowableDocumentCursor find(Filter filter) {
        return new FlowableDocumentCursor(() -> nitriteCollection.find(filter));
    }

    @Override
    public FlowableDocumentCursor find(FindOptions findOptions) {
        return new FlowableDocumentCursor(() -> nitriteCollection.find(findOptions));
    }

    @Override
    public FlowableDocumentCursor find(Filter filter, FindOptions findOptions) {
        return new FlowableDocumentCursor(() -> nitriteCollection.find(filter, findOptions));
    }

    @Override
    public Completable createIndex(String field, IndexOptions indexOptions) {
        return Completable.fromAction(() -> nitriteCollection.createIndex(field, indexOptions));
    }

    @Override
    public Completable rebuildIndex(String field, boolean async) {
        return Completable.fromAction(() -> nitriteCollection.rebuildIndex(field, async));
    }

    @Override
    public Single<Collection<Index>> listIndices() {
        return Single.fromCallable(nitriteCollection::listIndices);
    }

    @Override
    public Single<Boolean> hasIndex(String field) {
        return Single.fromCallable(() -> nitriteCollection.hasIndex(field));
    }

    @Override
    public Single<Boolean> isIndexing(String field) {
        return Single.fromCallable(() -> nitriteCollection.isIndexing(field));
    }

    @Override
    public Completable dropIndex(String field) {
        return Completable.fromAction(() -> nitriteCollection.dropIndex(field));
    }

    @Override
    public Completable dropAllIndices() {
        return Completable.fromAction(nitriteCollection::dropAllIndices);
    }

    @Override
    public FlowableWriteResult insert(Document[] items) {
        return new FlowableWriteResult(() -> nitriteCollection.insert(items));
    }

    @Override
    public FlowableWriteResult update(Document element) {
        return new FlowableWriteResult(() -> nitriteCollection.update(element));
    }

    @Override
    public FlowableWriteResult update(Document element, boolean upsert) {
        return new FlowableWriteResult(() -> nitriteCollection.update(element, upsert));
    }

    @Override
    public FlowableWriteResult remove(Document element) {
        return new FlowableWriteResult(() -> nitriteCollection.remove(element));
    }

    @Override
    public Single<Document> getById(NitriteId nitriteId) {
        return Single.fromCallable(() -> nitriteCollection.getById(nitriteId));
    }

    @Override
    public Completable drop() {
        return Completable.fromAction(nitriteCollection::drop);
    }

    @Override
    public Single<Boolean> isDropped() {
        return Single.fromCallable(nitriteCollection::isDropped);
    }

    @Override
    public Single<Boolean> isClosed() {
        return Single.fromCallable(nitriteCollection::isClosed);
    }

    @Override
    public Completable close() {
        return Completable.fromAction(nitriteCollection::close);
    }

    @Override
    public String getName() {
        return nitriteCollection.getName();
    }

    @Override
    public Single<Long> size() {
        return Single.fromCallable(nitriteCollection::size);
    }

    @Override
    public Observable<ChangedItem<Document>> observe() {
        return updates;
    }

    @Override
    public Observable<ChangedItem<Document>> observe(ChangeType changeType) {
        return updates.filter(changedItem -> changedItem.getChangeType() == changeType);
    }

    private void initializeUpdateObserver() {
        nitriteCollection.register(changedItem -> {
            if (changedItem != null) {
                updates.onNext(changedItem);
            }
        });
    }
}
