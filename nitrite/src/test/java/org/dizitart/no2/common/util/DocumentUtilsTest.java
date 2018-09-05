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

package org.dizitart.no2.common.util;

import org.dizitart.no2.Document;
import org.dizitart.no2.common.mapper.JacksonFacade;
import org.dizitart.no2.common.mapper.JacksonMapper;
import org.dizitart.no2.common.mapper.MapperFacade;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.filters.BaseFilter;
import org.dizitart.no2.filters.Filter;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_REVISION;
import static org.dizitart.no2.common.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.common.util.DocumentUtils.skeletonDocument;
import static org.dizitart.no2.common.util.DocumentUtils.isRecent;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DocumentUtilsTest {
    private Document doc;

    @Before
    public void setUp() {
    	MapperFacade mapperFacade = new JacksonFacade();
        doc = mapperFacade.parse("{" +
                "  score: 1034," +
                "  location: {  " +
                "       state: 'NY', " +
                "       city: 'New York', " +
                "       address: {" +
                "            line1: '40', " +
                "            line2: 'ABC Street', " +
                "            house: ['1', '2', '3'] " +
                "       }" +
                "  }," +
                "  category: ['food', 'produce', 'grocery'], " +
                "  objArray: [{ value: 1}, {value: 2}]" +
                "}");
    }

    @Test
    public void testIsRecent() throws InterruptedException {
        Document first = createDocument("key1", "value1");
        Thread.sleep(500);
        Document second = createDocument("key1", "value2");
        assertTrue(isRecent(second, first));
        second = first.clone();
        second.put(DOC_REVISION, 1);

        first.put("key2", "value3");
        first.put(DOC_REVISION, 2);
        assertTrue(isRecent(first, second));
    }

    @Test
    public void testCreateUniqueFilter() {
        doc.getId();
        Filter filter = createUniqueFilter(doc);
        assertNotNull(filter);
        assertTrue(filter instanceof BaseFilter);
    }

    @Test
    public void testDummyDocument() {
        NitriteMapper nitriteMapper = new JacksonMapper();
        Document document = skeletonDocument(nitriteMapper, DummyTest.class);
        assertTrue(document.containsKey("first"));
        assertTrue(document.containsKey("second"));
        assertNull(document.get("first"));
        assertNull(document.get("second"));
    }

    private class DummyTest {
        private String first;
        private Double second;
    }
}
