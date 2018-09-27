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

import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.mapper.JacksonFacade;
import org.dizitart.no2.common.mapper.MapperFacade;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class DocumentTest {
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
    public void testGetValue() {
        MapperFacade nitriteMapper = new JacksonFacade();
        assertNull(doc.get(""));
        assertEquals(doc.get("score"), 1034);
        assertEquals(doc.get("location.state"), "NY");
        assertEquals(doc.get("location.address"), nitriteMapper.parse("{" +
                "            line1: '40', " +
                "            line2: 'ABC Street', " +
                "            house: ['1', '2', '3'] " +
                "       },"));
        assertEquals(doc.get("location.address.line1"), "40");
        assertNull(doc.get("location.category"));

        assertEquals(doc.get("category"), doc.get("category"));
        assertEquals(doc.get("category.2"), "grocery");
        assertEquals(doc.get("location.address.house.2"), "3");

        assertNotEquals(doc.get("location.address.test"), nitriteMapper.parse("{" +
                "            line1: '40', " +
                "            line2: 'ABC Street'" +
                "       },"));
        assertNotEquals(doc.get("location.address.test"), "a");
        assertNull(doc.get("."));
        assertNull(doc.get("score.test"));
    }

    @Test
    public void testGetValueObjectArray() {
        assertEquals(doc.get("objArray.0.value"), 1);
    }

    @Test(expected = ValidationException.class)
    public void testGetValueInvalidIndex() {
        assertEquals(doc.get("category.3"), "grocery");
    }

    @Test(expected = InvalidOperationException.class)
    public void testPut() {
        doc.put(DOC_ID, "id");
    }

    @Test(expected = InvalidIdException.class)
    public void testGetId() {
        Map<String, Object> map = new HashMap<>();
        map.put(DOC_ID, "id");

        Document document = new Document(map);
        document.getId();
    }

    @Test(expected = ValidationException.class)
    public void testGet() {
        String key = "first.array.-1";
        Document document = new Document()
                .put("first", new Document().put("array", new int[] {0}));
        document.get(key);
    }

    @Test
    public void testRemove() {
        Iterator<KeyValuePair> iterator = doc.iterator();
        assertEquals(doc.size(), 4);
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        assertEquals(doc.size(), 3);
    }
}
