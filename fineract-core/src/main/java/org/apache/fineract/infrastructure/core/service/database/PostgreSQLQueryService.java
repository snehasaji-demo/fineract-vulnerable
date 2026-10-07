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

import static java.lang.String.format;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern; // Added import
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLQueryService implements DatabaseQueryService {

    private final DatabaseTypeResolver databaseTypeResolver;
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$"); // Added field

    @Autowired
    public PostgreSQLQueryService(DatabaseTypeResolver databaseTypeResolver) {
        this.databaseTypeResolver = databaseTypeResolver;
    }

    // Added validation method
    private String validateTableName(String tableName) {
        if (tableName == null || !TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        return tableName;
    }

    @Override
    public boolean isSupported() {
        return databaseTypeResolver.isPostgreSQL();
    }

    @Override
    public boolean isTablePresent(DataSource dataSource, String tableName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String validatedTableName = validateTableName(tableName); // Added validation call
        Integer result = jdbcTemplate.queryForObject(format("SELECT COUNT(table_name) " + "FROM information_schema.tables "
                + "WHERE table_schema = 'public' " + "AND table_name = '%s';", validatedTableName), Integer.class); // Used validatedTableName
        return Objects.equals(result, 1);
    }

    @Override
    public SqlRowSet getTableColumns(DataSource dataSource, String tableName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String validatedTableName = validateTableName(tableName); // Added validation call
        String sql = "SELECT column_name, is_nullable, data_type,"
                + " coalesce(character_maximum_length, numeric_precision, datetime_precision) AS max_length, ordinal_position = 1 AS column_key"
                + " FROM information_schema.columns WHERE table_catalog = current_catalog AND table_schema = current_schema AND table_name = '"
                + validatedTableName + "' ORDER BY ordinal_position"; // Used validatedTableName
        final SqlRowSet columnDefinitions = jdbcTemplate.queryForRowSet(sql); // NOSONAR
        if (columnDefinitions.next()) {
            return columnDefinitions;
        } else {
            throw new IllegalArgumentException("Table " + validatedTableName + " is not found"); // Used validatedTableName
        }
    }

    @Override
    public List<IndexDetail> getTableIndexes(DataSource dataSource, String tableName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String validatedTableName = validateTableName(tableName); // Added validation call
        String sql = "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND tablename = '" + validatedTableName + "'"; // Used validatedTableName
        final SqlRowSet indexDefinitions = jdbcTemplate.queryForRowSet(sql); // NOSONAR
        if (indexDefinitions.next()) {
            return DatabaseIndexMapper.getIndexDetails(indexDefinitions);
        } else {
            throw new IllegalArgumentException("Table " + validatedTableName + " is not found"); // Used validatedTableName
        }
    }
}