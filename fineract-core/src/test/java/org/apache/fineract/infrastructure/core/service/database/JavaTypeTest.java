/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.service.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JavaTypeTest {

    @BeforeAll
    static void setupMoneyHelper() throws Exception {
        ConfigurationDomainService mockConfig = mock(ConfigurationDomainService.class);
        when(mockConfig.getRoundingMode()).thenReturn(4); // HALF_EVEN

        Field field = MoneyHelper.class.getDeclaredField("staticConfigurationDomainService");
        field.setAccessible(true);
        field.set(null, mockConfig);
    }

    @Test
    void testForType() {
        assertEquals(JavaType.OBJECT, JavaType.forType(null));
        assertEquals(JavaType.STRING, JavaType.forType(String.class));
        assertEquals(JavaType.ARRAY, JavaType.forType(String[].class));
        
        JavaType listType = JavaType.forType(List.class);
        assertTrue(listType == JavaType.OBJECT || listType == JavaType.COLLECTION);

        assertEquals(JavaType.INPUT_READER, JavaType.forType(Reader.class));
        assertEquals(JavaType.INPUT_STREAM, JavaType.forType(InputStream.class));
        assertEquals(JavaType.OBJECT, JavaType.forType(Serializable.class));
        
        JavaType runnableType = JavaType.forType(Runnable.class);
        assertTrue(runnableType == JavaType.OBJECT || runnableType == JavaType.PC_UNTYPED);
    }

    @Test
    void testMatchType() {
        assertTrue(JavaType.STRING.matchType(String.class));
        assertFalse(JavaType.STRING.matchType(Integer.class));
        assertTrue(JavaType.OBJECT.matchType(null));
        assertFalse(JavaType.ARRAY.matchType(String.class));
        assertFalse(JavaType.ARRAY.matchType(String[].class));
        assertTrue(JavaType.INPUT_READER.matchType(Reader.class));
        assertTrue(JavaType.INPUT_STREAM.matchType(InputStream.class));

        // strict vs non-strict
        assertTrue(JavaType.INT.matchType(Integer.class, false));
        assertFalse(JavaType.INT.matchType(Integer.class, true));
    }

    @Test
    void testIsDefault() {
        assertTrue(JavaType.BOOLEAN.isDefault(false));
        assertFalse(JavaType.BOOLEAN.isDefault(true));
        assertTrue(JavaType.INT.isDefault(0));
        assertFalse(JavaType.INT.isDefault(1));
        assertTrue(JavaType.LONG.isDefault(0L));
        assertTrue(JavaType.DOUBLE.isDefault(0.0d));
        assertFalse(JavaType.STRING.isDefault(""));
    }

    @Test
    void testParse() {
        assertNull(JavaType.STRING.parse(null, null, null));
        assertEquals(true, JavaType.BOOLEAN.parse("true", null, null));
        assertEquals((byte) 5, JavaType.BYTE.parse("5", null, null));
        assertEquals((short) 10, JavaType.SHORT.parse("10", null, null));
        assertEquals(100, JavaType.INT.parse("100", null, null));
        assertEquals(1000L, JavaType.LONG.parse("1000", null, null));
        assertEquals('A', JavaType.CHAR.parse("A", null, null));
        assertNull(JavaType.CHAR.parse("", null, null));
        assertEquals(1.5f, JavaType.FLOAT.parse("1.5", null, null));
        assertEquals(2.5d, JavaType.DOUBLE.parse("2.5", null, null));
        assertEquals(new BigDecimal("123.45"), JavaType.BIGDECIMAL.parse("123.45", null, null));
        assertEquals(new BigInteger("999"), JavaType.BIGINTEGER.parse("999", null, null));
        assertEquals("hello", JavaType.STRING.parse("hello", null, null));
        assertEquals(Timestamp.valueOf("2023-10-15 10:00:00"), JavaType.DATETIME.parse("2023-10-15 10:00:00", null, null));
        assertEquals(Time.valueOf("10:00:00"), JavaType.TIME.parse("10:00:00", null, null));

        assertThrows(PlatformServiceUnavailableException.class, () -> JavaType.OBJECT.parse("obj", null, null));
    }

    @Test
    void testGetPrimitiveAndObjectType() {
        assertEquals(JavaType.BOOLEAN, JavaType.BOOLEAN_OBJ.getPrimitiveType());
        assertEquals(JavaType.BOOLEAN_OBJ, JavaType.BOOLEAN.getObjectType());
        assertNull(JavaType.STRING.getPrimitiveType());
    }
}
