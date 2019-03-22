package org.dizitart.no2.rx;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import org.dizitart.no2.Nitrite;

import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class RxNitrite {

    private Nitrite nitrite;
    private Scheduler scheduler;

    RxNitrite(Nitrite nitrite, Scheduler scheduler) {
        this.nitrite = nitrite;
        this.scheduler = scheduler;
    }

    public static RxNitriteBuilder builder() {
        return new RxNitriteBuilder(Nitrite.builder());
    }

    public RxPersistentCollection getCollection(String name) {
        return new RxNitriteCollectionImpl(name, nitrite, scheduler);
    }

    public <T> RxObjectRepository<T> getRepository(Class<T> type) {
        return null;
    }

    public <T> RxObjectRepository<T> getRepository(String key, Class<T> type) {
        return null;
    }

    public Single<Set<String>> listRepositories() {
        return Single.fromCallable(() -> nitrite.listRepositories())
                .subscribeOn(scheduler);
    }

    public Single<Map<String, String>> listKeyedRepository() {
        return Single.fromCallable(() -> nitrite.listKeyedRepository())
                .subscribeOn(scheduler);
    }

    public Single<Boolean> hasCollection(String name) {
        return Single.fromCallable(() -> nitrite.hasCollection(name))
                .subscribeOn(scheduler);
    }

    public <T> Single<Boolean> hasRepository(Class<T> type) {
        return Single.fromCallable(() -> nitrite.hasRepository(type))
                .subscribeOn(scheduler);
    }

    public <T> Single<Boolean> hasRepository(String key, Class<T> type) {
        return Single.fromCallable(() -> nitrite.hasRepository(key, type))
                .subscribeOn(scheduler);
    }

    public Single<Boolean> hasUnsavedChanges() {
        return Single.fromCallable(() -> nitrite.hasUnsavedChanges())
                .subscribeOn(scheduler);
    }

    public Completable compact() {
        return Completable.fromAction(() -> nitrite.compact())
                .subscribeOn(scheduler);
    }

    public Completable commit() {
        return Completable.fromAction(() -> nitrite.commit())
                .subscribeOn(scheduler);
    }

    public Single<Boolean> isClosed() {
        return Single.fromCallable(() -> nitrite.isClosed())
                .subscribeOn(scheduler);
    }

    private Single<Boolean> validateUser(String userId, String password) {
        return Single.fromCallable(() -> nitrite.validateUser(userId, password))
                .subscribeOn(scheduler);
    }

    public Completable close() {
        return Completable.fromAction(() -> {
            if (!nitrite.isClosed()) {
                nitrite.close();
            }
        }).subscribeOn(scheduler);
    }
}
