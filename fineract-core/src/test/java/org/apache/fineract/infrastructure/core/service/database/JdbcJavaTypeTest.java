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

import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.junit.jupiter.api.Test;

class JdbcJavaTypeTest {

    @Test
    void testGetJavaType() {
        assertEquals(JavaType.BOOLEAN, JdbcJavaType.BIT.getJavaType());
        assertEquals(JavaType.STRING, JdbcJavaType.VARCHAR.getJavaType());
    }

    @Test
    void testGetByTypeName() {
        assertEquals(JdbcJavaType.BOOLEAN, JdbcJavaType.getByTypeName(DatabaseType.MYSQL, "BIT", true));
        assertEquals(JdbcJavaType.BOOLEAN, JdbcJavaType.getByTypeName(DatabaseType.MYSQL, "TINYINT", true));
        assertEquals(JdbcJavaType.VARCHAR, JdbcJavaType.getByTypeName(DatabaseType.POSTGRESQL, "CHARACTER VARYING", true));
        assertNull(JdbcJavaType.getByTypeName(DatabaseType.MYSQL, "NON_EXISTENT", false));
        assertThrows(PlatformServiceUnavailableException.class, () -> JdbcJavaType.getByTypeName(DatabaseType.MYSQL, "NON_EXISTENT", true));
    }

    @Test
    void testCanBooleanType() {
        assertTrue(JdbcJavaType.BOOLEAN.canBooleanType(DatabaseType.MYSQL));
        assertTrue(JdbcJavaType.TINYINT.canBooleanType(DatabaseType.MYSQL));
        assertFalse(JdbcJavaType.TINYINT.canBooleanType(DatabaseType.POSTGRESQL));
    }

    @Test
    void testHasPrecisionAndScale() {
        assertTrue(JdbcJavaType.VARCHAR.hasPrecision(DatabaseType.MYSQL));
        assertFalse(JdbcJavaType.DATE.hasPrecision(DatabaseType.MYSQL));
        assertTrue(JdbcJavaType.DECIMAL.hasScale(DatabaseType.MYSQL));
    }

    @Test
    void testFormatSql() {
        assertEquals("VARCHAR(50)", JdbcJavaType.VARCHAR.formatSql(DatabaseType.MYSQL, 50));
        assertEquals("INTEGER", JdbcJavaType.INTEGER.formatSql(DatabaseType.MYSQL));
    }

    @Test
    void testToJdbcValue() {
        assertEquals(1, JdbcJavaType.BIT.toJdbcValue(DatabaseType.MYSQL, true, true));
        assertEquals(0, JdbcJavaType.BIT.toJdbcValue(DatabaseType.MYSQL, false, true));
        assertEquals(1, JdbcJavaType.BOOLEAN.toJdbcValue(DatabaseType.MYSQL, true, true));
        assertEquals((short) 1, JdbcJavaType.TINYINT.toJdbcValue(DatabaseType.MYSQL, (short) 1, true));
        assertEquals("test", JdbcJavaType.VARCHAR.toJdbcValue(DatabaseType.MYSQL, "test", true));

        assertThrows(PlatformServiceUnavailableException.class, () -> JdbcJavaType.INTEGER.toJdbcValue(DatabaseType.MYSQL, "not-an-int", true));
    }

    @Test
    void testToJdbcValueImplDirect() {
        assertEquals(1, JdbcJavaType.BIT.toJdbcValueImpl(DatabaseType.MYSQL, true));
        assertNull(JdbcJavaType.BIT.toJdbcValueImpl(DatabaseType.MYSQL, null));
        assertEquals(1, JdbcJavaType.BOOLEAN.toJdbcValueImpl(DatabaseType.MYSQL, true));
        assertEquals(1, JdbcJavaType.TINYINT.toJdbcValueImpl(DatabaseType.MYSQL, true));
        assertEquals("test", JdbcJavaType.VARCHAR.toJdbcValueImpl(DatabaseType.MYSQL, "test"));
    }

    @Test
    void testTypeChecks() {
        assertTrue(JdbcJavaType.BOOLEAN.isBooleanType());
        assertTrue(JdbcJavaType.VARCHAR.isStringType());
        assertTrue(JdbcJavaType.VARCHAR.isVarcharType());
        assertTrue(JdbcJavaType.TEXT.isTextType());
        assertTrue(JdbcJavaType.JSON.isJsonType());
        assertTrue(JdbcJavaType.SERIAL.isSerialType());
        assertTrue(JdbcJavaType.INTEGER.isIntegerType());
        assertTrue(JdbcJavaType.INTEGER.isAnyIntegerType());
        assertTrue(JdbcJavaType.FLOAT.isFloatType());
        assertTrue(JdbcJavaType.FLOAT.isAnyFloatType());
        assertTrue(JdbcJavaType.DECIMAL.isDecimalType());
        assertTrue(JdbcJavaType.DECIMAL.isNumericType());
        assertTrue(JdbcJavaType.DATE.isDateType());
        assertTrue(JdbcJavaType.TIME.isTimeType());
        assertTrue(JdbcJavaType.TIMESTAMP.isDateTimeType());
        assertTrue(JdbcJavaType.TIMESTAMP.isAnyDateType());
        assertTrue(JdbcJavaType.BLOB.isBlobType());
        assertTrue(JdbcJavaType.BINARY.isBinaryType());
    }
}
