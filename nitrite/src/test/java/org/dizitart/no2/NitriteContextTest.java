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

package org.dizitart.no2;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.common.mapper.JacksonMapper;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class NitriteContextTest {

    @Test
    public void testNitriteMapper() {
        NitriteContext context = new NitriteContext();
        JacksonMapper mapper = (JacksonMapper) context.getNitriteMapper();
        assertNotNull(mapper);
        assertFalse(mapper.getObjectMapper().getRegisteredModuleIds().contains(TestModule.class.getName()));
    }

    @Test
    public void testNitriteMapperWithModule() {
        Nitrite db = Nitrite.builder()
                .registerJacksonModule(new TestModule())
                .openOrCreate();
        NitriteContext context = db.getContext();
        JacksonMapper mapper = (JacksonMapper) context.getNitriteMapper();
        assertNotNull(mapper);
        assertTrue(mapper.getObjectMapper().getRegisteredModuleIds().contains(TestModule.class.getName()));
    }

    @Test
    public void testRemoveFromRegistry() {
        Nitrite db = Nitrite.builder().openOrCreate();

        NitriteCollection collection = db.getCollection("test");
        ObjectRepository<TestModule> repository = db.getRepository(TestModule.class);

        assertNotNull(collection);
        assertNotNull(repository);
        assertTrue(db.hasCollection("test"));
        assertTrue(db.hasRepository(TestModule.class));

        NitriteContext context = db.getContext();
        context.removeFromRegistry("test");

        assertFalse(db.hasCollection("test"));
        assertTrue(db.hasRepository(TestModule.class));

        context.removeFromRegistry(repository.getName());
        assertFalse(db.hasCollection("test"));
        assertFalse(db.hasRepository(TestModule.class));
    }

    private class TestModule extends SimpleModule { }
}
