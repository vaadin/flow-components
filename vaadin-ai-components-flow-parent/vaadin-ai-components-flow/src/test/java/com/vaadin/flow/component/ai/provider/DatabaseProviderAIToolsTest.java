/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.provider;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.internal.JacksonUtils;

class DatabaseProviderAIToolsTest {

    private static final String SCHEMA = "Tables: users(id INT, name VARCHAR)";

    private DatabaseProvider provider;

    @BeforeEach
    void setUp() {
        provider = new DatabaseProvider() {
            @Override
            public String getSchema() {
                return SCHEMA;
            }

            @Override
            public List<Map<String, Object>> executeQuery(String sql) {
                return List.of();
            }
        };
    }

    @Test
    void getDatabaseSchema_nullProvider_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> DatabaseProviderAITools.getDatabaseSchema(null));
    }

    @Test
    void getDatabaseSchema_returnsToolWithCorrectName() {
        var tool = DatabaseProviderAITools.getDatabaseSchema(provider);
        Assertions.assertEquals("get_database_schema", tool.getName());
    }

    @Test
    void getDatabaseSchema_returnsToolWithDescription() {
        var tool = DatabaseProviderAITools.getDatabaseSchema(provider);
        Assertions.assertNotNull(tool.getDescription());
        Assertions.assertFalse(tool.getDescription().isEmpty());
    }

    @Test
    void getDatabaseSchema_returnsToolWithNullParametersSchema() {
        var tool = DatabaseProviderAITools.getDatabaseSchema(provider);
        Assertions.assertNull(tool.getParametersSchema());
    }

    @Test
    void getDatabaseSchema_executeDelegatesToProvider() {
        var tool = DatabaseProviderAITools.getDatabaseSchema(provider);
        Assertions.assertEquals(SCHEMA,
                tool.execute(JacksonUtils.createObjectNode()));
    }

    @Test
    void createAll_returnsListContainingDatabaseSchemaTool() {
        var tools = DatabaseProviderAITools.createAll(provider);
        Assertions.assertEquals(1, tools.size());
        Assertions.assertEquals("get_database_schema", tools.get(0).getName());
    }
}
