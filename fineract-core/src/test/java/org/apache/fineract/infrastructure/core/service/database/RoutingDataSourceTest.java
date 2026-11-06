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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class RoutingDataSourceTest {

    @Test
    void testDetermineTargetDataSourceAndGetConnection() throws Exception {
        RoutingDataSource routingDataSource = new RoutingDataSource();

        RoutingDataSourceServiceFactory mockFactory = mock(RoutingDataSourceServiceFactory.class);
        RoutingDataSourceService mockService = mock(RoutingDataSourceService.class);
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);

        when(mockFactory.determineDataSourceService()).thenReturn(mockService);
        when(mockService.retrieveDataSource()).thenReturn(mockDataSource);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockDataSource.getConnection("user", "pass")).thenReturn(mockConnection);

        Field field = RoutingDataSource.class.getDeclaredField("dataSourceServiceFactory");
        field.setAccessible(true);
        field.set(routingDataSource, mockFactory);

        assertEquals(mockDataSource, routingDataSource.determineTargetDataSource());
        assertEquals(mockConnection, routingDataSource.getConnection());
        assertEquals(mockConnection, routingDataSource.getConnection("user", "pass"));
    }
}
