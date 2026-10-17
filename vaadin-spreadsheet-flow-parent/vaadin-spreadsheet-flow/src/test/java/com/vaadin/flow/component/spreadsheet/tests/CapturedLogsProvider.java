/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * Makes {@link CapturedLogs} the logging backend of the tests. SLF4J 2, the
 * version on the test classpath, finds it through {@code META-INF/services}.
 */
public final class CapturedLogsProvider implements SLF4JServiceProvider {

    private final ILoggerFactory factory = CapturedLogs::logger;

    private final IMarkerFactory markers = new BasicMarkerFactory();

    private final MDCAdapter mdc = new NOPMDCAdapter();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return factory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markers;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdc;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.99";
    }

    @Override
    public void initialize() {
        // Nothing to set up: CapturedLogs keeps what is logged.
    }
}
