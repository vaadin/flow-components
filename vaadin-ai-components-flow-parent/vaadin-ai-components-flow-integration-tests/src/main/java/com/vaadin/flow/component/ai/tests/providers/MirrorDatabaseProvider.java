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
package com.vaadin.flow.component.ai.tests.providers;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Supplier;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;

/**
 * Base of the providers that copy a non-SQL source into a {@link SqlMirror} on
 * first use and let the LLM query the copy with SQL. The copy is a snapshot:
 * {@link #refresh()} discards it, so the next query copies again.
 */
public abstract class MirrorDatabaseProvider implements DatabaseProvider {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private transient volatile JdbcDatabaseProvider mirror;

    @Override
    public QueryLanguage getQueryLanguage() {
        return SqlDialectQueryLanguage.H2;
    }

    @Override
    public String getSchema() {
        return mirror().getSchema();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        return mirror().executeQuery(sql);
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql, QueryPage page) {
        return mirror().executeQuery(sql, page);
    }

    @Override
    public OptionalInt count(String sql) {
        return mirror().count(sql);
    }

    /**
     * Discards the copied data, so the next query copies it again.
     */
    public void refresh() {
        logger.info("Discarding the mirrored data");
        mirror = null;
    }

    /**
     * Copies the source into a new mirror.
     *
     * @return the filled mirror
     */
    protected abstract SqlMirror copy();

    /**
     * Returns the notes appended to the schema description: where the data
     * comes from and how names were converted.
     *
     * @return the notes
     */
    protected abstract String notes();

    private JdbcDatabaseProvider mirror() {
        var current = mirror;
        if (current == null) {
            synchronized (this) {
                current = mirror;
                if (current == null) {
                    logger.info(
                            "Copying the source into an in-memory SQL database");
                    var sqlMirror = copy();
                    logger.info("Copied tables: {}", sqlMirror.tables());
                    var dataSource = sqlMirror.dataSource();
                    Supplier<DataSource> supplier = () -> dataSource;
                    current = new JdbcDatabaseProvider(supplier,
                            SqlDialectQueryLanguage.H2, null, notes());
                    mirror = current;
                }
            }
        }
        return current;
    }
}
