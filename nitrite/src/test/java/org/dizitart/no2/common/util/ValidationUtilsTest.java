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

import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class ValidationUtilsTest {

    @Test
    public void testNotEmpty() {
        boolean exception = false;
        try {
            notEmpty("", errorMessage("empty string", 0));
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "empty string");
        } finally {
            assertTrue(exception);
        }
    }

    @Test
    public void testNotNull() {
        boolean exception = false;
        String a = null;
        try {
            notNull(a, errorMessage("null string", 0));
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "null string");
        } finally {
            assertTrue(exception);
        }
    }

    @Test
    public void testCharSequenceNotEmpty() {
        CharSequence cs = "";
        boolean invalid = false;
        try {
            notEmpty(cs, errorMessage("test", 0));
        } catch (ValidationException iae) {
            invalid = true;
            assertEquals(iae.getErrorMessage().getMessage(), "test");
        } finally {
            assertTrue(invalid);
        }
    }
}