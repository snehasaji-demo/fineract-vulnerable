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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;

class DatabaseTypeResolverTest {

    @Test
    void testAfterPropertiesSetMySQL() throws Exception {
        HikariConfig hikariConfig = mock(HikariConfig.class);
        when(hikariConfig.getDriverClassName()).thenReturn("com.mysql.cj.jdbc.Driver");

        DatabaseTypeResolver resolver = new DatabaseTypeResolver(hikariConfig);
        resolver.afterPropertiesSet();

        assertEquals(DatabaseType.MYSQL, resolver.databaseType());
        assertTrue(resolver.isMySQL());
        assertFalse(resolver.isPostgreSQL());
    }

    @Test
    void testAfterPropertiesSetPostgreSQL() throws Exception {
        HikariConfig hikariConfig = mock(HikariConfig.class);
        when(hikariConfig.getDriverClassName()).thenReturn("org.postgresql.Driver");

        DatabaseTypeResolver resolver = new DatabaseTypeResolver(hikariConfig);
        resolver.afterPropertiesSet();

        assertEquals(DatabaseType.POSTGRESQL, resolver.databaseType());
        assertTrue(resolver.isPostgreSQL());
        assertFalse(resolver.isMySQL());
    }

    @Test
    void testUnsupportedDriverThrows() {
        HikariConfig hikariConfig = mock(HikariConfig.class);
        when(hikariConfig.getDriverClassName()).thenReturn("invalid.Driver");

        DatabaseTypeResolver resolver = new DatabaseTypeResolver(hikariConfig);
        assertThrows(IllegalArgumentException.class, resolver::afterPropertiesSet);
    }
}
