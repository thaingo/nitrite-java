package org.dizitart.no2.rx;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.operators.flowable.FlowableFromIterable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.WriteResult;
import org.reactivestreams.Subscriber;

import java.util.Iterator;

/**
 * @author Anindya Chatterjee
 */
public final class FlowableWriteResult extends Flowable<NitriteId> {
    private final WriteResult wrapped;
    private final Scheduler scheduler;

    FlowableWriteResult(WriteResult wrapped, Scheduler scheduler) {
        this.wrapped = wrapped;
        this.scheduler = scheduler;
    }

    Single<Integer> getAffectedCount() {
        return Single.fromCallable(wrapped::getAffectedCount)
                .subscribeOn(scheduler);
    }

    @Override
    protected void subscribeActual(Subscriber<? super NitriteId> s) {
        Iterator<NitriteId> it;
        try {
            it = wrapped.iterator();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptySubscription.error(e, s);
            return;
        }

        FlowableFromIterable.subscribe(s, it);
    }
}
