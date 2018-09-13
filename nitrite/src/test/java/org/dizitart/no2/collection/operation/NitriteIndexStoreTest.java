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
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.store.NitriteMVStore;
import org.dizitart.no2.store.NitriteMap;
import org.h2.mvstore.MVStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class NitriteIndexStoreTest {
    private NitriteIndexStore indexStore;

    @Before
    public void setup() {
        MVStore mvStore = new MVStore.Builder().open();
        NitriteMVStore store = new NitriteMVStore(mvStore);
        NitriteMap<NitriteId, Document> map = store.openMap("test");
        indexStore = new NitriteIndexStore(map);
    }

    @Test
    public void testGetIndexMapNull() {
        assertNull(indexStore.getIndexMap("fake"));
    }

    @Test
    public void testGetSpatialIndexMapNull() {
        assertNull(indexStore.getSpatialIndexMap("fake"));
    }

    @Test
    public void testDropAll() {
        indexStore.createIndex("field1", IndexType.Spatial);
        indexStore.createIndex("field2", IndexType.Fulltext);
        indexStore.dropAll();
        assertTrue(indexStore.listIndexes().isEmpty());
    }
}
