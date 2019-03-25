package org.dizitart.no2.rx;

import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.collection.objects.Cursor;

import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public final class FlowableCursor<T> extends FlowableRecordIterable<T> {

    private final Callable<Cursor<T>> supplier;

    FlowableCursor(Callable<Cursor<T>> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    public <P> FlowableRecordIterable<P> project(Class<P> projectionType) {
        Callable<RecordIterable<P>> projectionSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");
            return cursor.project(projectionType);
        };

        return FlowableRecordIterable.create(projectionSupplier);
    }

    public <Foreign, Joined> FlowableRecordIterable<Joined> join(FlowableCursor<Foreign> foreignCursor, Lookup lookup,
                                                                 Class<Joined> type) {
        Callable<RecordIterable<Joined>> joinSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");

            Cursor<Foreign> foreign = ObjectHelper.requireNonNull(foreignCursor.supplier.call(),
                    "The supplier supplied is null");

            return cursor.join(foreign, lookup, type);
        };
        return FlowableRecordIterable.create(joinSupplier);
    }
}
