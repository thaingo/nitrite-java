package org.dizitart.no2.rx;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.common.event.ChangeType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Anindya Chatterjee
 */
public class RxObjectRepositoryTest extends RxBaseTest {
    private RxObjectRepository<Employee> repository;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        repository = db.getRepository(Employee.class);
    }

    @Test
    public void testWrite() {
        repository.observe(ChangeType.INSERT, BackpressureStrategy.MISSING)
                .subscribe(new BaseSubscriber<Employee>() {
                    @Override
                    public void onNext(Employee employee) {
                        System.out.println(employee);
                    }
                });

        Flowable<NitriteId> flowable = repository.insert(testData.toArray(new Employee[0]));

        assertFalse(repository.find().size().blockingGet() > 0);

        TestSubscriber<NitriteId> test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();

        assertEquals(2, repository.find().size().blockingGet().intValue());
    }
}
