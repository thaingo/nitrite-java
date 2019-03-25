package org.dizitart.no2.rx;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.collection.RecordIterable;

import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public abstract class FlowableRecordIterable<T> extends FlowableIterable<T> {

    private final Callable<? extends RecordIterable<T>> supplier;

    static <R> FlowableRecordIterable<R> create(Callable<? extends RecordIterable<R>> supplier) {
        return new FlowableRecordIterable<R>(supplier) {
        };
    }

    FlowableRecordIterable(Callable<? extends RecordIterable<T>> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    public Single<Boolean> hasMore() {
        return Single.fromCallable(() -> {
            RecordIterable<T> recordIterable = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");
            return recordIterable.hasMore();
        });
    }

    public Single<Integer> size() {
        return Single.fromCallable(() -> {
            RecordIterable<T> recordIterable = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");
            return recordIterable.size();
        });
    }

    public Single<Integer> totalCount() {
        return Single.fromCallable(() -> {
            RecordIterable<T> recordIterable = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");
            return recordIterable.totalCount();
        });
    }

    public Maybe<T> firstOrNull() {
        return this.firstElement();
    }
}
