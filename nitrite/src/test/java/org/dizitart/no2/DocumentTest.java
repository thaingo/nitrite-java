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

import org.dizitart.no2.common.mapper.JacksonFacade;
import org.dizitart.no2.common.mapper.MapperFacade;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Before;
import org.junit.Test;

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
        assertNull(doc.getFieldValue(""));
        assertEquals(doc.getFieldValue("score"), 1034);
        assertEquals(doc.getFieldValue("location.state"), "NY");
        assertEquals(doc.getFieldValue("location.address"), nitriteMapper.parse("{" +
                "            line1: '40', " +
                "            line2: 'ABC Street', " +
                "            house: ['1', '2', '3'] " +
                "       },"));
        assertEquals(doc.getFieldValue("location.address.line1"), "40");
        assertNull(doc.getFieldValue("location.category"));

        assertEquals(doc.getFieldValue("category"), doc.get("category"));
        assertEquals(doc.getFieldValue("category.2"), "grocery");
        assertEquals(doc.getFieldValue("location.address.house.2"), "3");

        assertNotEquals(doc.getFieldValue("location.address.test"), nitriteMapper.parse("{" +
                "            line1: '40', " +
                "            line2: 'ABC Street'" +
                "       },"));
        assertNotEquals(doc.getFieldValue("location.address.test"), "a");
    }

    @Test(expected = ValidationException.class)
    public void testGetValueFailure() {
        assertEquals(doc.getFieldValue("score.test"), 1034);
    }

    @Test(expected = ValidationException.class)
    public void testGetValueInvalidIndex() {
        assertEquals(doc.getFieldValue("category.3"), "grocery");
    }

    @Test(expected = ValidationException.class)
    public void testGetValueObjectArray() {
        assertEquals(doc.getFieldValue("objArray.0.value"), 1);
    }

    @Test(expected = ValidationException.class)
    public void testGetValueInvalidKey() {
        assertEquals(doc.getFieldValue("."), 1);
    }
}
