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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class DatabaseSpecificSQLGeneratorCoverageTest {

    private DatabaseTypeResolver databaseTypeResolver;
    private DatabaseSpecificSQLGenerator generator;

    @BeforeEach
    void setUp() {
        databaseTypeResolver = mock(DatabaseTypeResolver.class);
        generator = new DatabaseSpecificSQLGenerator(databaseTypeResolver);

        // Setup ThreadLocal context for DateUtils
        FineractPlatformTenant tenant = mock(FineractPlatformTenant.class);
        when(tenant.getTimezoneId()).thenReturn("UTC");
        ThreadLocalContextUtil.setTenant(tenant);

        HashMap<BusinessDateType, LocalDate> dates = new HashMap<>();
        dates.put(BusinessDateType.BUSINESS_DATE, LocalDate.of(2023, 1, 1));
        ThreadLocalContextUtil.setBusinessDates(dates);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.clearTenant();
        // ThreadLocalContextUtil doesn't have clearBusinessDates, but we can set null or empty
        try {
            ThreadLocalContextUtil.setBusinessDates(new HashMap<>());
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void testGetDialect() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);
        assertEquals(DatabaseType.MYSQL, generator.getDialect());
    }

    @Test
    void testEscape() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("`test`", generator.escape("test"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("\"test\"", generator.escape("test"));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertEquals("test", generator.escape("test"));
    }

    @Test
    void testFormatValue() {
        assertEquals("'val'", generator.formatValue(JdbcJavaType.VARCHAR, "val"));
        assertEquals("123", generator.formatValue(JdbcJavaType.INTEGER, "123"));
    }

    @Test
    void testGroupConcat() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("GROUP_CONCAT(col)", generator.groupConcat("col"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("STRING_AGG(col::varchar, ',')", generator.groupConcat("col"));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL); // fallback
        assertThrows(IllegalStateException.class, () -> generator.groupConcat("col"));
    }

    @Test
    void testLimit() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("LIMIT 0,10", generator.limit(10));
        assertEquals("LIMIT 5,10", generator.limit(10, 5));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("LIMIT 10 OFFSET 0", generator.limit(10));
        assertEquals("LIMIT 10 OFFSET 5", generator.limit(10, 5));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.limit(10));
    }

    @Test
    void testCalcFoundRows() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("SQL_CALC_FOUND_ROWS", generator.calcFoundRows());

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        assertEquals("", generator.calcFoundRows());
    }

    @Test
    void testCountLastExecutedQueryResult() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("SELECT FOUND_ROWS()", generator.countLastExecutedQueryResult("SELECT * FROM test"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        assertEquals("SELECT COUNT(*) FROM (SELECT * FROM test) AS temp", generator.countLastExecutedQueryResult("SELECT * FROM test LIMIT 10 OFFSET 5"));
    }

    @Test
    void testCurrentBusinessDate() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("DATE('2023-01-01')", generator.currentBusinessDate());

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("DATE '2023-01-01'", generator.currentBusinessDate());

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.currentBusinessDate());
    }

    @Test
    void testCurrentTenantDate() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String expectedMySql = "DATE('" + LocalDate.now(java.time.ZoneOffset.UTC).toString() + "')";
        assertEquals(expectedMySql, generator.currentTenantDate());

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        String expectedPgReal = "DATE '" + LocalDate.now(java.time.ZoneOffset.UTC).toString() + "'";
        assertEquals(expectedPgReal, generator.currentTenantDate());

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.currentTenantDate());
    }

    @Test
    void testCurrentTenantDateTime() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String resMySql = generator.currentTenantDateTime();
        assertEquals(true, resMySql.startsWith("TIMESTAMP('"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        String resPg = generator.currentTenantDateTime();
        assertEquals(true, resPg.startsWith("TIMESTAMP '"));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.currentTenantDateTime());
    }

    @Test
    void testSubDate() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("DATE_SUB(col, INTERVAL 5 DAY)", generator.subDate("col", "5", "DAY"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("(col::TIMESTAMP - 5 * INTERVAL '1 DAY')", generator.subDate("col", "5", "DAY"));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.subDate("col", "5", "DAY"));
    }

    @Test
    void testDateDiff() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("DATEDIFF(d1, d2)", generator.dateDiff("d1", "d2"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("EXTRACT(DAY FROM (d1::TIMESTAMP - d2::TIMESTAMP))", generator.dateDiff("d1", "d2"));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.dateDiff("d1", "d2"));
    }

    @Test
    void testLastInsertId() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("LAST_INSERT_ID()", generator.lastInsertId());

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("LASTVAL()", generator.lastInsertId());

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.lastInsertId());
    }

    @Test
    void testCastChar() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("CAST(col AS CHAR)", generator.castChar("col"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("col::CHAR", generator.castChar("col"));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.castChar("col"));
    }

    @Test
    void testCastInteger() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("CAST(col AS SIGNED INTEGER)", generator.castInteger("col"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("col::INTEGER", generator.castInteger("col"));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.castInteger("col"));
    }

    @Test
    void testCurrentSchema() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("SCHEMA()", generator.currentSchema());

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("CURRENT_SCHEMA()", generator.currentSchema());

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.currentSchema());
    }

    @Test
    void testCastJson() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("col", generator.castJson("col"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("col ::json", generator.castJson("col"));

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.castJson("col"));
    }

    @Test
    void testAlias() {
        assertEquals("t.field", generator.alias("field", "t"));
        assertEquals("field", generator.alias("field", ""));
        assertEquals("field", generator.alias("field", null));
    }

    @Test
    void testBuildSelect() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("", generator.buildSelect(null, "t", false));
        assertEquals("", generator.buildSelect(Collections.emptyList(), "t", false));
        assertEquals("SELECT `f1`, `f2`", generator.buildSelect(Arrays.asList("f1", "f2"), null, false));
        assertEquals("t.`f1`, t.`f2`", generator.buildSelect(Arrays.asList("f1", "f2"), "t", true));
    }

    @Test
    void testBuildFrom() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("", generator.buildFrom(null, "t", false));
        assertEquals("FROM `table` t", generator.buildFrom("table", "t", false));
        assertEquals("`table`", generator.buildFrom("table", "", true));
    }

    @Test
    void testBuildJoin() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("JOIN `table` t ON  t.`fk` = ref.`refCol`", generator.buildJoin("table", "t", "fk", "ref", "refCol", null));
        assertEquals("LEFT JOIN `table` t ON  t.`fk` = ref.`refCol`", generator.buildJoin("table", "t", "fk", "ref", "refCol", "LEFT"));
    }

    @Test
    void testBuildOrderBy() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("", generator.buildOrderBy(null, "t", false));
        assertEquals("", generator.buildOrderBy(Collections.emptyList(), "t", false));
        
        List<Sort.Order> orders = Arrays.asList(Sort.Order.asc("f1"), Sort.Order.desc("f2"));
        assertEquals("ORDER BY t.`f1` ASC, t.`f2` DESC", generator.buildOrderBy(orders, "t", false));
        assertEquals("`f1` ASC, `f2` DESC", generator.buildOrderBy(orders, null, true));
    }

    @Test
    void testBuildInsert() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("", generator.buildInsert("table", null, null));
        
        Map<String, ResultsetColumnHeaderData> headers = new HashMap<>();
        ResultsetColumnHeaderData header = mock(ResultsetColumnHeaderData.class);
        when(header.getColumnType()).thenReturn(JdbcJavaType.VARCHAR);
        headers.put("f1", header);

        assertEquals("INSERT INTO `table`(`f1`, `f2`) VALUES (?, ?)", generator.buildInsert("table", Arrays.asList("f1", "f2"), headers));
    }

    @Test
    void testBuildUpdate() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("", generator.buildUpdate("table", null, null));

        Map<String, ResultsetColumnHeaderData> headers = new HashMap<>();
        ResultsetColumnHeaderData header = mock(ResultsetColumnHeaderData.class);
        when(header.getColumnType()).thenReturn(JdbcJavaType.VARCHAR);
        headers.put("f1", header);

        assertEquals("UPDATE `table` SET `f1` = ?, `f2` = ?", generator.buildUpdate("table", Arrays.asList("f1", "f2"), headers));
    }

    @Test
    void testDecoratePlaceHolderPostgresJson() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.POSTGRESQL);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);

        Map<String, ResultsetColumnHeaderData> headers = new HashMap<>();
        ResultsetColumnHeaderData header = mock(ResultsetColumnHeaderData.class);
        when(header.getColumnType()).thenReturn(JdbcJavaType.JSON);
        headers.put("f1", header);

        assertEquals("INSERT INTO \"table\"(\"f1\") VALUES (?::JSON)", generator.buildInsert("table", Arrays.asList("f1"), headers));
    }
}
