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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JavaTypeTest {

    @BeforeAll
    static void initMoneyHelper() throws Exception {
        java.lang.reflect.Field field = MoneyHelper.class.getDeclaredField("staticConfigurationDomainService");
        field.setAccessible(true);
        ConfigurationDomainService mockConfig = mock(ConfigurationDomainService.class);
        when(mockConfig.getRoundingMode()).thenReturn(java.math.RoundingMode.HALF_UP.ordinal());
        field.set(null, mockConfig);
    }

    @Test
    void testForType() {
        assertEquals(JavaType.OBJECT, JavaType.forType(null));
        assertEquals(JavaType.BOOLEAN, JavaType.forType(boolean.class));
        assertEquals(JavaType.BOOLEAN_OBJ, JavaType.forType(Boolean.class));
        assertEquals(JavaType.ARRAY, JavaType.forType(int[].class));
        assertEquals(JavaType.COLLECTION, JavaType.forType(Collection.class));
        assertEquals(JavaType.COLLECTION, JavaType.forType(ArrayList.class));
        assertEquals(JavaType.MAP, JavaType.forType(Map.class));
        assertEquals(JavaType.MAP, JavaType.forType(HashMap.class));
        assertEquals(JavaType.INPUT_READER, JavaType.forType(Reader.class));
        assertEquals(JavaType.INPUT_STREAM, JavaType.forType(InputStream.class));
        assertEquals(JavaType.OBJECT, JavaType.forType(Object.class));
    }

    @Test
    void testMatchType() {
        assertTrue(JavaType.OBJECT.matchType(null));
        assertFalse(JavaType.ARRAY.matchType(null));
        
        assertTrue(JavaType.BOOLEAN.matchType(boolean.class));
        assertFalse(JavaType.BOOLEAN.matchType(int.class));
        
        // ARRAY has null type, so matchType returns false
        assertFalse(JavaType.ARRAY.matchType(int[].class));
        assertFalse(JavaType.ARRAY.matchType(int.class));
        
        assertTrue(JavaType.COLLECTION.matchType(ArrayList.class));
        assertTrue(JavaType.INPUT_READER.matchType(Reader.class));
        assertTrue(JavaType.INPUT_STREAM.matchType(InputStream.class));
        
        // strict vs non-strict matchType
        assertTrue(JavaType.BOOLEAN.matchType(Boolean.class, false));
        assertTrue(JavaType.BOOLEAN_OBJ.matchType(boolean.class, false));
        
        assertTrue(JavaType.INT.matchType(Integer.class, false));
        assertTrue(JavaType.INT_OBJ.matchType(int.class, false));
        
        // Number assignable
        assertTrue(JavaType.INT_OBJ.matchType(Byte.class, false));
        assertFalse(JavaType.BYTE_OBJ.matchType(Integer.class, false));
        
        assertTrue(JavaType.DOUBLE_OBJ.matchType(Float.class, false));
        assertFalse(JavaType.FLOAT_OBJ.matchType(Double.class, false));
    }

    @Test
    void testIsDefault() {
        assertTrue(JavaType.BOOLEAN.isDefault(false));
        assertTrue(JavaType.BOOLEAN_OBJ.isDefault(false));
        assertFalse(JavaType.BOOLEAN.isDefault(true));
        
        assertTrue(JavaType.BYTE.isDefault((byte) 0));
        assertTrue(JavaType.BYTE_OBJ.isDefault((byte) 0));
        assertFalse(JavaType.BYTE.isDefault((byte) 1));
        
        assertTrue(JavaType.SHORT.isDefault((short) 0));
        assertTrue(JavaType.SHORT_OBJ.isDefault((short) 0));
        assertFalse(JavaType.SHORT.isDefault((short) 1));
        
        assertTrue(JavaType.INT.isDefault(0));
        assertTrue(JavaType.INT_OBJ.isDefault(0));
        assertFalse(JavaType.INT.isDefault(1));
        
        assertTrue(JavaType.LONG.isDefault(0L));
        assertTrue(JavaType.LONG_OBJ.isDefault(0L));
        assertFalse(JavaType.LONG.isDefault(1L));
        
        assertTrue(JavaType.CHAR.isDefault('\u0000'));
        assertTrue(JavaType.CHAR_OBJ.isDefault('\u0000'));
        assertFalse(JavaType.CHAR.isDefault('a'));
        
        assertTrue(JavaType.FLOAT.isDefault(0.0F));
        assertTrue(JavaType.FLOAT_OBJ.isDefault(0.0F));
        assertFalse(JavaType.FLOAT.isDefault(1.0F));
        
        assertTrue(JavaType.DOUBLE.isDefault(0.0d));
        assertTrue(JavaType.DOUBLE_OBJ.isDefault(0.0d));
        assertFalse(JavaType.DOUBLE.isDefault(1.0d));
        
        assertFalse(JavaType.STRING.isDefault(""));
    }

    @Test
    void testParse() {
        assertNull(JavaType.BOOLEAN.parse(null, null, Locale.US));
        
        assertEquals(true, JavaType.BOOLEAN.parse("true", null, Locale.US));
        assertEquals((byte) 12, JavaType.BYTE.parse("12", null, Locale.US));
        assertEquals((short) 123, JavaType.SHORT.parse("123", null, Locale.US));
        assertEquals(1234, JavaType.INT.parse("1234", null, Locale.US));
        assertEquals(12345L, JavaType.LONG.parse("12345", null, Locale.US));
        assertEquals('a', JavaType.CHAR.parse("abc", null, Locale.US));
        assertNull(JavaType.CHAR.parse("", null, Locale.US));
        assertEquals(1.23F, JavaType.FLOAT.parse("1.23", null, Locale.US));
        assertEquals(1.234d, JavaType.DOUBLE.parse("1.234", null, Locale.US));
        assertEquals(new BigDecimal("123.45"), JavaType.BIGDECIMAL.parse("123.45", null, Locale.US));
        assertEquals(new BigInteger("123456789"), JavaType.BIGINTEGER.parse("123456789", null, Locale.US));
        assertEquals("hello", JavaType.STRING.parse("hello", null, Locale.US));
        
        Timestamp ts = Timestamp.valueOf("2023-01-01 12:00:00");
        assertEquals(ts, JavaType.DATETIME.parse("2023-01-01 12:00:00", null, Locale.US));
        
        Time time = Time.valueOf("12:00:00");
        assertEquals(time, JavaType.TIME.parse("12:00:00", null, Locale.US));
        
        Date date = new Date("2023/01/01");
        assertEquals(date, JavaType.DATE.parse("2023/01/01", null, Locale.US));
        
        assertThrows(PlatformServiceUnavailableException.class, () -> {
            JavaType.OBJECT.parse("test", null, Locale.US);
        });
    }
}
