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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.RecordIterable;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.junit.Test;

import java.util.Iterator;

import static org.dizitart.no2.Document.createDocument;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class JoinedDocumentIterableTest {

    @Test
    public void testFindResult() {
        FindResult findResult = new FindResult();
        JoinedDocumentIterable cursor = new JoinedDocumentIterable(findResult, null, null);
        assertEquals(cursor.size(), 0);

        Nitrite db = Nitrite.builder().openOrCreate();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        RecordIterable<Document> result = collection.find().join(collection.find(), new Lookup());
        assertTrue(result instanceof JoinedDocumentIterable);
    }

    @Test(expected = InvalidOperationException.class)
    public void testIteratorRemove() {
        Nitrite db = Nitrite.builder().openOrCreate();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        RecordIterable<Document> cursor = collection.find().join(collection.find(), new Lookup());
        assertNotNull(cursor.toString());
        Iterator<Document> iterator = cursor.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
