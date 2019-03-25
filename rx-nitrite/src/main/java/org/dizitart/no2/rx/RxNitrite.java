package org.dizitart.no2.rx;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;

import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public class RxNitrite {

    private final Nitrite nitrite;
    private final NitriteContext nitriteContext;

    RxNitrite(Nitrite nitrite) {
        this.nitrite = nitrite;
        this.nitriteContext = nitrite.getContext();
    }

    public static RxNitriteBuilder builder() {
        return new RxNitriteBuilder(Nitrite.builder());
    }

    public RxNitriteCollection getCollection(String name) {
        NitriteCollection collection = nitrite.getCollection(name);
        return new RxNitriteCollectionImpl(collection);
    }

    public <T> RxObjectRepository<T> getRepository(Class<T> type) {
        ObjectRepository<T> repository = nitrite.getRepository(type);
        return new RxObjectRepositoryImpl<>(repository, nitriteContext);
    }

    public <T> RxObjectRepository<T> getRepository(String key, Class<T> type) {
        ObjectRepository<T> repository = nitrite.getRepository(key, type);
        return new RxObjectRepositoryImpl<>(repository, nitriteContext);
    }

    public Single<Set<String>> listRepositories() {
        return Single.fromCallable(nitrite::listRepositories);
    }

    public Single<Map<String, String>> listKeyedRepository() {
        return Single.fromCallable(nitrite::listKeyedRepository);
    }

    public Single<Boolean> hasCollection(String name) {
        return Single.fromCallable(() -> nitrite.hasCollection(name));
    }

    public <T> Single<Boolean> hasRepository(Class<T> type) {
        return Single.fromCallable(() -> nitrite.hasRepository(type));
    }

    public <T> Single<Boolean> hasRepository(String key, Class<T> type) {
        return Single.fromCallable(() -> nitrite.hasRepository(key, type));
    }

    public Single<Boolean> hasUnsavedChanges() {
        return Single.fromCallable(nitrite::hasUnsavedChanges);
    }

    public Completable compact() {
        return Completable.fromAction(nitrite::compact);
    }

    public Completable commit() {
        return Completable.fromAction(nitrite::commit);
    }

    public Single<Boolean> isClosed() {
        return Single.fromCallable(nitrite::isClosed);
    }

    private Single<Boolean> validateUser(String userId, String password) {
        return Single.fromCallable(() -> nitrite.validateUser(userId, password));
    }

    public Completable close() {
        return Completable.fromAction(() -> {
            if (!nitrite.isClosed()) {
                nitrite.close();
            }
        });
    }
}
