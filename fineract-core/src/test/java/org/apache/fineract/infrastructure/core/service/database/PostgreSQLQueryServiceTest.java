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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class PostgreSQLQueryServiceTest {

    private final DatabaseTypeResolver databaseTypeResolver = mock(DatabaseTypeResolver.class);
    private final PostgreSQLQueryService queryService = new PostgreSQLQueryService(databaseTypeResolver);

    @Test
    public void testIsSupportedTrue() {
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        Assertions.assertTrue(queryService.isSupported());
    }

    @Test
    public void testIsSupportedFalse() {
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        Assertions.assertFalse(queryService.isSupported());
    }

    @Test
    public void testIsTablePresentValid() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getObject(1, Integer.class)).thenReturn(1);
        when(resultSet.getObject(1)).thenReturn(1);
        when(resultSet.getInt(1)).thenReturn(1);

        boolean present = queryService.isTablePresent(dataSource, "valid_table_name");
        Assertions.assertTrue(present);
    }

    @Test
    public void testIsTablePresentInvalid() {
        DataSource dataSource = mock(DataSource.class);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            queryService.isTablePresent(dataSource, "invalid; table");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            queryService.isTablePresent(dataSource, "table'name");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            queryService.isTablePresent(dataSource, "123table");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            queryService.isTablePresent(dataSource, null);
        });
    }

    @Test
    public void testGetTableColumnsValid() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(metaData.getColumnCount()).thenReturn(5);
        when(metaData.getColumnLabel(1)).thenReturn("column_name");
        when(metaData.getColumnLabel(2)).thenReturn("is_nullable");
        when(metaData.getColumnLabel(3)).thenReturn("data_type");
        when(metaData.getColumnLabel(4)).thenReturn("max_length");
        when(metaData.getColumnLabel(5)).thenReturn("column_key");

        when(resultSet.findColumn("column_name")).thenReturn(1);
        when(resultSet.findColumn("is_nullable")).thenReturn(2);
        when(resultSet.findColumn("data_type")).thenReturn(3);
        when(resultSet.findColumn("max_length")).thenReturn(4);
        when(resultSet.findColumn("column_key")).thenReturn(5);

        for (int i = 1; i <= 5; i++) {
            when(metaData.getColumnType(i)).thenReturn(java.sql.Types.VARCHAR);
            when(metaData.getColumnTypeName(i)).thenReturn("VARCHAR");
        }

        when(resultSet.getString(1)).thenReturn("id");
        when(resultSet.getString("column_name")).thenReturn("id");
        when(resultSet.getObject(1)).thenReturn("id");
        when(resultSet.getObject("column_name")).thenReturn("id");

        SqlRowSet rowSet = queryService.getTableColumns(dataSource, "valid_table");
        Assertions.assertNotNull(rowSet);
    }

    @Test
    public void testGetTableColumnsInvalid() {
        DataSource dataSource = mock(DataSource.class);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            queryService.getTableColumns(dataSource, "invalid; table");
        });
    }

    @Test
    public void testGetTableColumnsNotFound() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(5);
        when(resultSet.next()).thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            queryService.getTableColumns(dataSource, "valid_table");
        });
    }

    @Test
    public void testGetTableIndexesValid() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);

        final int[] cursor = { -1 }; // -1 = before first, 0 = first row, 1 = after last
        when(resultSet.next()).thenAnswer(invocation -> {
            if (cursor[0] == -1) {
                cursor[0] = 0;
                return true;
            } else {
                cursor[0] = 1;
                return false;
            }
        });
        doAnswer(invocation -> {
            cursor[0] = -1;
            return null;
        }).when(resultSet).beforeFirst();

        when(resultSet.getString(1)).thenReturn("idx_test");
        when(resultSet.getString("indexname")).thenReturn("idx_test");
        when(resultSet.getObject(1)).thenReturn("idx_test");
        when(resultSet.getObject("indexname")).thenReturn("idx_test");
        when(resultSet.findColumn("indexname")).thenReturn(1);

        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("indexname");
        when(metaData.getColumnType(1)).thenReturn(java.sql.Types.VARCHAR);
        when(metaData.getColumnTypeName(1)).thenReturn("VARCHAR");

        List<IndexDetail> indexes = queryService.getTableIndexes(dataSource, "valid_table");
        Assertions.assertNotNull(indexes);
        Assertions.assertEquals(1, indexes.size());
        Assertions.assertEquals("idx_test", indexes.get(0).getIndexName());
    }

    @Test
    public void testGetTableIndexesInvalid() {
        DataSource dataSource = mock(DataSource.class);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            queryService.getTableIndexes(dataSource, "invalid; table");
        });
    }

    @Test
    public void testGetTableIndexesNotFound() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(resultSet.next()).thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            queryService.getTableIndexes(dataSource, "valid_table");
        });
    }
}
