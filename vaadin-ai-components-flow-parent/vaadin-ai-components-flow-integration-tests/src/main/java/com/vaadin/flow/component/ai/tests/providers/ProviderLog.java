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

import org.slf4j.Logger;

import com.vaadin.flow.component.ai.common.QueryLanguage;
import com.vaadin.flow.component.ai.common.QueryPage;
import com.vaadin.flow.component.ai.provider.ToolException;

/**
 * The log lines every provider writes, so the console tells the same story for
 * each of them: the query as the LLM wrote it, what was asked of the backend,
 * how many rows came back, and what the LLM was told when a query failed.
 * Providers pass their own logger, so the lines name the provider.
 */
final class ProviderLog {

    private ProviderLog() {
    }

    /**
     * Logs a query as received from the LLM.
     *
     * @param logger
     *            the provider's logger
     * @param language
     *            the provider's query language
     * @param query
     *            the query
     */
    static void query(Logger logger, QueryLanguage language, String query) {
        logger.info("{} query from the LLM: {}", language.getName(), query);
    }

    /**
     * Logs a page request of a lazily loading component.
     *
     * @param logger
     *            the provider's logger
     * @param page
     *            the requested page
     */
    static void page(Logger logger, QueryPage page) {
        logger.info("Page requested: offset={} limit={} sort={}", page.offset(),
                page.limit(),
                page.sortOrders().stream().map(
                        order -> order.getSorted() + " " + order.getDirection())
                        .toList());
    }

    /**
     * Logs what is sent to the backend after the provider's own translation or
     * push-down.
     *
     * @param logger
     *            the provider's logger
     * @param what
     *            what the backend is asked, for example a URL or a pipeline
     */
    static void backend(Logger logger, Object what) {
        logger.info("Asking the backend: {}", what);
    }

    /**
     * Logs the size of a result.
     *
     * @param logger
     *            the provider's logger
     * @param rows
     *            the number of rows returned
     */
    static void rows(Logger logger, int rows) {
        logger.info("Returned {} rows", rows);
    }

    /**
     * Logs a count result.
     *
     * @param logger
     *            the provider's logger
     * @param count
     *            the number of rows the query has
     */
    static void count(Logger logger, int count) {
        logger.info("Counted {} rows", count);
    }

    /**
     * Logs a schema request.
     *
     * @param logger
     *            the provider's logger
     * @param schema
     *            the schema text that is returned to the LLM
     */
    static void schema(Logger logger, String schema) {
        logger.info("Schema requested, {} characters", schema.length());
        logger.debug("Schema:\n{}", schema);
    }

    /**
     * Logs a failure whose message is passed on to the LLM.
     *
     * @param logger
     *            the provider's logger
     * @param e
     *            the exception being thrown
     */
    static void relayed(Logger logger, ToolException e) {
        logger.warn("Query failed, telling the LLM: {}", e.getMessage());
    }
}
