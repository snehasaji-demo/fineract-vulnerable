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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.support.rowset.SqlRowSet;

class MySQLQueryServiceTest {

    private final DatabaseTypeResolver databaseTypeResolver = mock(DatabaseTypeResolver.class);
    private final MySQLQueryService service = new MySQLQueryService(databaseTypeResolver);

    private DataSource createMockDataSource(boolean hasRows, String firstColValue, boolean forIndexes) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(Mockito.anyString(), Mockito.any(int[].class))).thenReturn(preparedStatement);
        when(connection.prepareStatement(Mockito.anyString(), Mockito.any(String[].class))).thenReturn(preparedStatement);
        when(connection.prepareStatement(Mockito.anyString(), Mockito.anyInt())).thenReturn(preparedStatement);
        when(connection.prepareStatement(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(preparedStatement);
        when(connection.prepareStatement(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(preparedStatement);
        when(connection.createStatement(Mockito.anyInt(), Mockito.anyInt())).thenReturn(preparedStatement);
        when(connection.createStatement(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(preparedStatement.executeQuery(Mockito.anyString())).thenReturn(resultSet);

        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("col1");
        when(metaData.getColumnName(1)).thenReturn("col1");
        when(metaData.getColumnType(1)).thenReturn(java.sql.Types.VARCHAR);

        if (hasRows) {
            if (forIndexes) {
                when(resultSet.next()).thenReturn(true, true, false);
            } else {
                when(resultSet.next()).thenReturn(true, false);
            }
            when(resultSet.getString(1)).thenReturn(firstColValue);
            when(resultSet.getString("col1")).thenReturn(firstColValue);
        } else {
            when(resultSet.next()).thenReturn(false);
        }

        return dataSource;
    }

    @Test
    void testIsSupported() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        assertTrue(service.isSupported());

        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        assertFalse(service.isSupported());
    }

    @Test
    void testIsTablePresent() throws SQLException {
        DataSource dsWithTable = createMockDataSource(true, "my_table", false);
        assertTrue(service.isTablePresent(dsWithTable, "my_table"));

        DataSource dsWithoutTable = createMockDataSource(false, null, false);
        assertFalse(service.isTablePresent(dsWithoutTable, "my_table"));
    }

    @Test
    void testGetTableColumns() throws SQLException {
        DataSource dsWithTable = createMockDataSource(true, "col1", false);
        SqlRowSet rowSet = service.getTableColumns(dsWithTable, "my_table");
        assertNotNull(rowSet);

        DataSource dsWithoutTable = createMockDataSource(false, null, false);
        assertThrows(IllegalArgumentException.class, () -> service.getTableColumns(dsWithoutTable, "my_table"));
    }

    @Test
    void testGetTableIndexes() throws SQLException {
        DataSource dsWithTable = createMockDataSource(true, "idx_name", true);
        List<IndexDetail> indexes = service.getTableIndexes(dsWithTable, "my_table");
        assertFalse(indexes.isEmpty());
        assertEquals("idx_name", indexes.get(0).getIndexName());

        DataSource dsWithoutTable = createMockDataSource(false, null, false);
        assertThrows(IllegalArgumentException.class, () -> service.getTableIndexes(dsWithoutTable, "my_table"));
    }
}
