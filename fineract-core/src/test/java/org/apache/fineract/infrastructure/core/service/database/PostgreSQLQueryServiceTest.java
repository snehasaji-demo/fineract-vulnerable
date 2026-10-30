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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class PostgreSQLQueryServiceTest {

    private DatabaseTypeResolver databaseTypeResolver;
    private PostgreSQLQueryService service;
    private DataSource dataSource;
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ResultSetMetaData resultSetMetaData;

    @BeforeEach
    public void setUp() throws Exception {
        databaseTypeResolver = mock(DatabaseTypeResolver.class);
        service = new PostgreSQLQueryService(databaseTypeResolver);
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        resultSetMetaData = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
    }

    private void mockResultSetForRowSet(int columnCount, String[] columnNames, Object[][] rows) throws Exception {
        when(resultSetMetaData.getColumnCount()).thenReturn(columnCount);
        for (int i = 0; i < columnCount; i++) {
            int colIndex = i + 1;
            when(resultSetMetaData.getColumnType(colIndex)).thenReturn(java.sql.Types.VARCHAR);
            when(resultSetMetaData.getColumnName(colIndex)).thenReturn(columnNames[i]);
            when(resultSetMetaData.getColumnLabel(colIndex)).thenReturn(columnNames[i]);
            when(resultSetMetaData.getPrecision(colIndex)).thenReturn(0);
            when(resultSetMetaData.getScale(colIndex)).thenReturn(0);
        }

        final int[] currentRow = {-1};
        Mockito.doAnswer(invocation -> {
            currentRow[0]++;
            return currentRow[0] < rows.length;
        }).when(resultSet).next();

        when(resultSet.getObject(Mockito.anyInt())).thenAnswer(invocation -> {
            int colIndex = invocation.getArgument(0, Integer.class) - 1;
            return rows[currentRow[0]][colIndex];
        });
        when(resultSet.getString(Mockito.anyInt())).thenAnswer(invocation -> {
            int colIndex = invocation.getArgument(0, Integer.class) - 1;
            Object val = rows[currentRow[0]][colIndex];
            return val != null ? val.toString() : null;
        });
    }

    @Test
    public void testIsSupported() {
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        assertTrue(service.isSupported());

        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        assertFalse(service.isSupported());
    }

    @Test
    public void testIsTablePresentSuccess() throws Exception {
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getObject(1, Integer.class)).thenReturn(1);
        when(resultSet.wasNull()).thenReturn(false);

        boolean present = service.isTablePresent(dataSource, "valid_table_name");
        assertTrue(present);
    }

    @Test
    public void testIsTablePresentNotFound() throws Exception {
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSet.getInt(1)).thenReturn(0);
        when(resultSet.getObject(1, Integer.class)).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(false);

        boolean present = service.isTablePresent(dataSource, "valid_table_name");
        assertFalse(present);
    }

    @Test
    public void testIsTablePresentInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.isTablePresent(dataSource, "invalid; table");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            service.isTablePresent(dataSource, "table name");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            service.isTablePresent(dataSource, "");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            service.isTablePresent(dataSource, null);
        });
    }

    @Test
    public void testGetTableColumnsSuccess() throws Exception {
        String[] columns = {"column_name", "is_nullable", "data_type", "max_length", "column_key"};
        Object[][] rows = {
            {"id", "NO", "integer", 0, "PRI"}
        };
        mockResultSetForRowSet(5, columns, rows);

        SqlRowSet rowSet = service.getTableColumns(dataSource, "valid_table");
        assertEquals("id", rowSet.getString("column_name"));
    }

    @Test
    public void testGetTableColumnsNotFound() throws Exception {
        mockResultSetForRowSet(5, new String[]{"column_name", "is_nullable", "data_type", "max_length", "column_key"}, new Object[][]{});

        assertThrows(IllegalArgumentException.class, () -> {
            service.getTableColumns(dataSource, "valid_table");
        });
    }

    @Test
    public void testGetTableColumnsInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getTableColumns(dataSource, "invalid; table");
        });
    }

    @Test
    public void testGetTableIndexesSuccess() throws Exception {
        mockResultSetForRowSet(1, new String[]{"indexname"}, new Object[][]{{"idx_test"}});

        List<IndexDetail> indexes = service.getTableIndexes(dataSource, "valid_table");
        assertEquals(1, indexes.size());
        assertEquals("idx_test", indexes.get(0).getIndexName());
    }

    @Test
    public void testGetTableIndexesNotFound() throws Exception {
        mockResultSetForRowSet(1, new String[]{"indexname"}, new Object[][]{});

        assertThrows(IllegalArgumentException.class, () -> {
            service.getTableIndexes(dataSource, "valid_table");
        });
    }

    @Test
    public void testGetTableIndexesInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getTableIndexes(dataSource, "invalid; table");
        });
    }
}
