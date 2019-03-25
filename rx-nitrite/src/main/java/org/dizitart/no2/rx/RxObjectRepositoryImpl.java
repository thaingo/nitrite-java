package org.dizitart.no2.rx;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.common.event.ChangeType;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.Index;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class RxObjectRepositoryImpl<T> implements RxObjectRepository<T> {
    private final ObjectRepository<T> repository;
    private final NitriteContext nitriteContext;
    private final PublishSubject<Pair<ChangeType, T>> updates;

    RxObjectRepositoryImpl(ObjectRepository<T> repository, NitriteContext nitriteContext) {
        this.repository = repository;
        this.updates = PublishSubject.create();
        this.nitriteContext = nitriteContext;
        initializeUpdateObserver();
    }

    @Override
    @SafeVarargs
    public final FlowableWriteResult insert(T object, T... others) {
        return new FlowableWriteResult(() -> repository.insert(object, others));
    }

    @Override
    public FlowableWriteResult update(Filter filter, T update) {
        return new FlowableWriteResult(() -> repository.update(filter, update));
    }

    @Override
    public FlowableWriteResult update(Filter filter, T update, boolean upsert) {
        return new FlowableWriteResult(() -> repository.update(filter, update, upsert));
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update) {
        return new FlowableWriteResult(() -> repository.update(filter, update));
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update, boolean justOnce) {
        return new FlowableWriteResult(() -> repository.update(filter, update, justOnce));
    }

    @Override
    public FlowableWriteResult remove(Filter filter) {
        return new FlowableWriteResult(() -> repository.remove(filter));
    }

    @Override
    public FlowableWriteResult remove(Filter filter, RemoveOptions removeOptions) {
        return new FlowableWriteResult(() -> repository.remove(filter, removeOptions));
    }

    @Override
    public FlowableCursor<T> find() {
        return new FlowableCursor<>(repository::find);
    }

    @Override
    public FlowableCursor<T> find(Filter filter) {
        return new FlowableCursor<>(() -> repository.find(filter));
    }

    @Override
    public FlowableCursor<T> find(FindOptions findOptions) {
        return new FlowableCursor<>(() -> repository.find(findOptions));
    }

    @Override
    public FlowableCursor<T> find(Filter filter, FindOptions findOptions) {
        return new FlowableCursor<>(() -> repository.find(filter, findOptions));
    }

    @Override
    public Class<T> getType() {
        return repository.getType();
    }

    @Override
    public Completable createIndex(String field, IndexOptions indexOptions) {
        return Completable.fromAction(() -> repository.createIndex(field, indexOptions));
    }

    @Override
    public Completable rebuildIndex(String field, boolean async) {
        return Completable.fromAction(() -> repository.rebuildIndex(field, async));
    }

    @Override
    public Single<Collection<Index>> listIndices() {
        return Single.fromCallable(repository::listIndices);
    }

    @Override
    public Single<Boolean> hasIndex(String field) {
        return Single.fromCallable(() -> repository.hasIndex(field));
    }

    @Override
    public Single<Boolean> isIndexing(String field) {
        return Single.fromCallable(() -> repository.isIndexing(field));
    }

    @Override
    public Completable dropIndex(String field) {
        return Completable.fromAction(() -> repository.dropIndex(field));
    }

    @Override
    public Completable dropAllIndices() {
        return Completable.fromAction(repository::dropAllIndices);
    }

    @Override
    public FlowableWriteResult insert(T[] items) {
        return new FlowableWriteResult(() -> repository.insert(items));
    }

    @Override
    public FlowableWriteResult update(T element) {
        return new FlowableWriteResult(() -> repository.update(element));
    }

    @Override
    public FlowableWriteResult update(T element, boolean upsert) {
        return new FlowableWriteResult(() -> repository.update(element, upsert));
    }

    @Override
    public FlowableWriteResult remove(T element) {
        return new FlowableWriteResult(() -> repository.remove(element));
    }

    @Override
    public Single<T> getById(NitriteId nitriteId) {
        return Single.fromCallable(() -> repository.getById(nitriteId));
    }

    @Override
    public Completable drop() {
        return Completable.fromAction(repository::drop);
    }

    @Override
    public Single<Boolean> isDropped() {
        return Single.fromCallable(repository::isDropped);
    }

    @Override
    public Single<Boolean> isClosed() {
        return Single.fromCallable(repository::isClosed);
    }

    @Override
    public Completable close() {
        return Completable.fromAction(repository::close);
    }

    @Override
    public String getName() {
        return repository.getName();
    }

    @Override
    public Single<Long> size() {
        return Single.fromCallable(repository::size);
    }

    @Override
    public Flowable<T> observe(BackpressureStrategy backpressureStrategy) {
        return updates.toFlowable(backpressureStrategy)
                .map(Pair::getValue);
    }

    @Override
    public Flowable<T> observe(ChangeType changeType, BackpressureStrategy backpressureStrategy) {
        return updates.toFlowable(backpressureStrategy)
                .filter(pair -> pair.getKey() == changeType)
                .map(Pair::getValue);
    }

    private void initializeUpdateObserver() {
        repository.register(changeInfo -> {
            if (changeInfo != null && changeInfo.getChangedItems() != null) {
                changeInfo.getChangedItems().forEach(changedItem -> {
                    try {
                        Document document = changedItem.getDocument();
                        NitriteMapper nitriteMapper = nitriteContext.getNitriteMapper();
                        T object = nitriteMapper.asObject(document, getType());
                        updates.onNext(new Pair<>(changedItem.getChangeType(), object));
                    } catch (Exception e) {
                        log.error("Error while listening to changed item", e);
                    }
                });
            }
        });
    }
}
