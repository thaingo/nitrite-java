/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.junit.Assert.*;

public class CollectionInsertTest extends BaseCollectionTest {

    @Test
    public void testInsert() {
        WriteResult result = collection.insert(doc1, doc2, doc3);
        assertEquals(result.getAffectedCount(), 3);

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        for (Document document : cursor) {
            assertNotNull(document.get("firstName"));
            assertNotNull(document.get("lastName"));
            assertNotNull(document.get("birthDay"));
            assertNotNull(document.get("data"));
            assertNotNull(document.get("body"));
            assertNotNull(document.get(DOC_ID));
        }
    }

    @Test
    public void testInsertHeteroDocs() {
        Document document = createDocument("test", "Nitrite Test");

        WriteResult result = collection.insert(doc1, doc2, doc3, document);
        assertEquals(result.getAffectedCount(), 4);
    }

    @Test
    public void testInsertObservable() {
        Document document = createDocument("test", "Nitrite Test");
        WriteResult result = collection.insert(doc1, doc2, doc3, document);
        AtomicInteger count  = new AtomicInteger(0);
        Observer<NitriteId> observer = result.toObservable().subscribeWith(new Observer<NitriteId>() {
            @Override
            public void onSubscribe(Disposable d) {
                assertEquals(count.get(), 0);
            }

            @Override
            public void onNext(NitriteId id) {
                assertNotNull(id);
                count.incrementAndGet();
            }

            @Override
            public void onError(Throwable e) {
                fail("should not happen - " + e.getMessage());
            }

            @Override
            public void onComplete() {
                assertEquals(count.get(), 4);
            }
        });
        assertNotNull(observer);

        assertEquals(result.getAffectedCount(), count.get());
    }
}
