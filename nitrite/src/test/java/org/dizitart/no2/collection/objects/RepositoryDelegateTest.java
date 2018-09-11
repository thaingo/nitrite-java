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

package org.dizitart.no2.collection.objects;

import org.dizitart.no2.common.mapper.JacksonMapper;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.util.ObjectUtilsTest;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryDelegateTest {
    private RepositoryDelegate repositoryDelegate = new RepositoryDelegate();

    @Test
    public void testIndexes() {
        NitriteMapper nitriteMapper = new JacksonMapper();
        Set<Index> indexes = repositoryDelegate.extractIndices(nitriteMapper, TestObjectWithIndex.class);
        assertEquals(indexes.size(), 2);
    }

    @Test(expected = IndexingException.class)
    public void testInvalidIndexNonComparable() {
        NitriteMapper nitriteMapper = new JacksonMapper();
        Set<Index> indexes = repositoryDelegate.extractIndices(nitriteMapper, ObjectWithNonComparableIndex.class);
        assertEquals(indexes.size(), 2);
    }

    @Test(expected = IndexingException.class)
    public void testInvalidIndexComparableAndIterable() {
        NitriteMapper nitriteMapper = new JacksonMapper();
        repositoryDelegate.extractIndices(nitriteMapper, ObjectWithIterableIndex.class);
    }

    @Test(expected = ValidationException.class)
    public void testGetFieldsUpToNullStartClass() {
        assertEquals(repositoryDelegate.getFieldsUpto(null, null).size(), 0);
    }

    @Test(expected = ValidationException.class)
    public void testGetFieldNoSuchField() {
        repositoryDelegate.getField(getClass(), "test", false);
    }

    @Test
    public void testGetFieldsUpTo() {
        assertEquals(repositoryDelegate.getFieldsUpto(A.class, B.class).size(), 3);
        assertEquals(repositoryDelegate.getFieldsUpto(A.class, Object.class).size(), 5);
        assertEquals(repositoryDelegate.getFieldsUpto(A.class, null).size(), 5);

        assertEquals(repositoryDelegate.getFieldsUpto(ClassWithAnnotatedFields.class,
                Object.class).size(), 5);
        assertEquals(repositoryDelegate.getFieldsUpto(ClassWithAnnotatedFields.class,
                null).size(), 5);
        assertEquals(repositoryDelegate.getFieldsUpto(ClassWithAnnotatedFields.class,
                ClassWithNoAnnotatedFields.class).size(), 3);
    }

    @Test(expected = InvalidIdException.class)
    public void testCreateUniqueFilterInvalidId() throws NoSuchFieldException {
        B b = new B();
        Field field = b.getClass().getDeclaredField("d");
        repositoryDelegate.createUniqueFilter(b, field);
    }

    @Test(expected = NotIdentifiableException.class)
    public void testGetIdFieldMultipleId() {
        class Test {
            @Id
            private String id1;

            @Id
            private Long id2;
        }

        repositoryDelegate.getIdField(new JacksonMapper(), Test.class);
    }


    private static class ClassWithAnnotatedFields extends ClassWithNoAnnotatedFields {
        private String stringValue;

        @Deprecated
        private String anotherValue;

        @Deprecated
        Long longValue;

        @Deprecated
        public ClassWithAnnotatedFields() {
        }
    }

    private static class ClassWithNoAnnotatedFields {
        private String stringValue;
        private Integer integer;
    }

    private static class A extends B {
        private String a;
        private Long b;
        private Integer c;
    }

    private static class B {
        String a;
        private Short d;
    }

    @Index(value = "testClass")
    private static class ObjectWithNonComparableIndex {
        private ObjectUtilsTest testClass;
    }

    @Index(value = "testClass")
    private static class ObjectWithIterableIndex {
        private TestClass testClass;
    }

    private static class TestClass implements Comparable<TestClass>, Iterable<Long> {
        @Override
        public int compareTo(TestClass o) {
            return 0;
        }

        @Override
        public Iterator<Long> iterator() {
            return null;
        }
    }

    @Indices({
            @Index(value = "longValue"),
            @Index(value = "decimal")
    })
    private class TestObjectWithIndex {
        private long longValue;

        private TestObject testObject;

        private BigDecimal decimal;
    }

    @Index(value = "longValue")
    private class TestObject {
        private String stringValue;

        private Long longValue;
    }

}
