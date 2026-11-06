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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class DatabaseSpecificSQLGeneratorCoverageTest {

    private final DatabaseTypeResolver databaseTypeResolver = mock(DatabaseTypeResolver.class);
    private final DatabaseSpecificSQLGenerator generator = new DatabaseSpecificSQLGenerator(databaseTypeResolver);

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
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

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
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

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);
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

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
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
        assertEquals("SELECT COUNT(*) FROM (SELECT * FROM test) AS temp", generator.countLastExecutedQueryResult("SELECT * FROM test"));
    }

    @Test
    void testCurrentBusinessDate() {
        HashMap<BusinessDateType, LocalDate> dates = new HashMap<>();
        dates.put(BusinessDateType.BUSINESS_DATE, LocalDate.of(2023, 10, 15));
        ThreadLocalContextUtil.setBusinessDates(dates);

        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("DATE('2023-10-15')", generator.currentBusinessDate());

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("DATE '2023-10-15'", generator.currentBusinessDate());

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.currentBusinessDate());
    }

    @Test
    void testCurrentTenantDate() {
        FineractPlatformTenant tenant = mock(FineractPlatformTenant.class);
        when(tenant.getTimezoneId()).thenReturn("UTC");
        ThreadLocalContextUtil.setTenant(tenant);

        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String mysqlResult = generator.currentTenantDate();
        assertEquals(true, mysqlResult.startsWith("DATE('"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        String pgResult = generator.currentTenantDate();
        assertEquals(true, pgResult.startsWith("DATE '"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.currentTenantDate());
    }

    @Test
    void testCurrentTenantDateTime() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String mysqlResult = generator.currentTenantDateTime();
        assertEquals(true, mysqlResult.startsWith("TIMESTAMP('"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        String pgResult = generator.currentTenantDateTime();
        assertEquals(true, pgResult.startsWith("TIMESTAMP '"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.currentTenantDateTime());
    }

    @Test
    void testSubDate() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("DATE_SUB(col, INTERVAL 1 DAY)", generator.subDate("col", "1", "DAY"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("(col::TIMESTAMP - 1 * INTERVAL '1 DAY')", generator.subDate("col", "1", "DAY"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.subDate("col", "1", "DAY"));
    }

    @Test
    void testDateDiff() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("DATEDIFF(d1, d2)", generator.dateDiff("d1", "d2"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertEquals("EXTRACT(DAY FROM (d1::TIMESTAMP - d2::TIMESTAMP))", generator.dateDiff("d1", "d2"));

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
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

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
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

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
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

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
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

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
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

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> generator.castJson("col"));
    }

    @Test
    void testAlias() {
        assertEquals("col", generator.alias("col", ""));
        assertEquals("a.col", generator.alias("col", "a"));
    }

    @Test
    void testBuildSelect() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("SELECT `col1`, `col2`", generator.buildSelect(List.of("col1", "col2"), "", false));
        assertEquals("`col1`, `col2`", generator.buildSelect(List.of("col1", "col2"), "", true));
        assertEquals("", generator.buildSelect(List.of(), "", false));
    }

    @Test
    void testBuildFrom() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("FROM `tbl` t", generator.buildFrom("tbl", "t", false));
        assertEquals("`tbl` t", generator.buildFrom("tbl", "t", true));
        assertEquals("", generator.buildFrom(null, "t", false));
    }

    @Test
    void testBuildJoin() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("LEFT JOIN `tbl` t ON  t.`fk` = r.`ref`", generator.buildJoin("tbl", "t", "fk", "r", "ref", "LEFT"));
    }

    @Test
    void testBuildOrderBy() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("ORDER BY `col` ASC", generator.buildOrderBy(List.of(Sort.Order.asc("col")), "", false));
        assertEquals("`col` ASC", generator.buildOrderBy(List.of(Sort.Order.asc("col")), "", true));
        assertEquals("", generator.buildOrderBy(List.of(), "", false));
    }

    @Test
    void testBuildInsert() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("INSERT INTO `tbl`(`col`) VALUES (?)", generator.buildInsert("tbl", List.of("col"), Map.of()));
    }

    @Test
    void testBuildUpdate() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertEquals("UPDATE `tbl` SET `col` = ?", generator.buildUpdate("tbl", List.of("col"), Map.of()));
    }

    @Test
    void testDecoratePlaceHolderPostgresJson() {
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.POSTGRESQL);
        ResultsetColumnHeaderData header = mock(ResultsetColumnHeaderData.class);
        when(header.getColumnType()).thenReturn(JdbcJavaType.JSON);
        assertEquals("INSERT INTO tbl(col) VALUES (?::JSON)", generator.buildInsert("tbl", List.of("col"), Map.of("col", header)));
    }
}
