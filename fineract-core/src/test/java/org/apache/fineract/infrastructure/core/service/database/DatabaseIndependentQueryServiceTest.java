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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.rowset.SqlRowSet;

class DatabaseIndependentQueryServiceTest {

    @Test
    void testDelegation() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        DatabaseQueryService mockService = mock(DatabaseQueryService.class);
        when(mockService.isSupported()).thenReturn(true);

        SqlRowSet mockRowSet = mock(SqlRowSet.class);
        List<IndexDetail> mockIndexes = List.of();

        when(mockService.isTablePresent(dataSource, "test_table")).thenReturn(true);
        when(mockService.getTableColumns(dataSource, "test_table")).thenReturn(mockRowSet);
        when(mockService.getTableIndexes(dataSource, "test_table")).thenReturn(mockIndexes);

        DatabaseIndependentQueryService service = new DatabaseIndependentQueryService(List.of(mockService));

        assertTrue(service.isSupported());
        assertTrue(service.isTablePresent(dataSource, "test_table"));
        assertEquals(mockRowSet, service.getTableColumns(dataSource, "test_table"));
        assertEquals(mockIndexes, service.getTableIndexes(dataSource, "test_table"));
    }

    @Test
    void testNoSupportedServiceThrows() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:unsupported://localhost");

        DatabaseQueryService mockService = mock(DatabaseQueryService.class);
        when(mockService.isSupported()).thenReturn(false);

        DatabaseIndependentQueryService service = new DatabaseIndependentQueryService(List.of(mockService));

        assertThrows(IllegalStateException.class, () -> service.isTablePresent(dataSource, "test_table"));
    }
}
