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

import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.meta.Attributes;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFactoryTest {

    @Test(expected = ValidationException.class)
    public void testGetCollectionMapStoreNull() {
        CollectionFactory factory = new CollectionFactory();
        assertNotNull(factory);

        Nitrite db = Nitrite.builder().openOrCreate();
        CollectionFactory.open(null, db.getContext());
    }

    @Test(expected = ValidationException.class)
    public void testGetCollectionContextNull() {
        CollectionFactory.open(new NitriteMap<NitriteId, Document>() {
            @Override
            public boolean containsKey(NitriteId id) {
                return false;
            }

            @Override
            public Document get(NitriteId id) {
                return null;
            }

            @Override
            public NitriteStore getStore() {
                return null;
            }

            @Override
            public void clear() {

            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Collection<Document> values() {
                return null;
            }

            @Override
            public Document remove(NitriteId id) {
                return null;
            }

            @Override
            public Set<NitriteId> keySet() {
                return null;
            }

            @Override
            public void put(NitriteId id, Document keyValuePairs) {

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public long sizeAsLong() {
                return 0;
            }

            @Override
            public Document putIfAbsent(NitriteId id, Document keyValuePairs) {
                return null;
            }

            @Override
            public Set<Map.Entry<NitriteId, Document>> entrySet() {
                return null;
            }

            @Override
            public NitriteId higherKey(NitriteId id) {
                return null;
            }

            @Override
            public NitriteId ceilingKey(NitriteId id) {
                return null;
            }

            @Override
            public NitriteId lowerKey(NitriteId id) {
                return null;
            }

            @Override
            public NitriteId floorKey(NitriteId id) {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public List<NitriteId> keyList() {
                return null;
            }

            @Override
            public void drop() {

            }

            @Override
            public Attributes getAttributes() {
                return null;
            }

            @Override
            public void setAttributes(Attributes attributes) {

            }
        }, null);
    }
}
