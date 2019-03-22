package org.dizitart.no2.rx;

import io.reactivex.Flowable;
import org.dizitart.no2.collection.RecordIterable;
import org.reactivestreams.Subscriber;

/**
 * @author Anindya Chatterjee
 */
public final class FlowableCursor<T> extends Flowable<T> {

    private RecordIterable<T> cursor;

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {

    }
}
