/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import com.vaadin.flow.component.spreadsheet.tests.CapturedLogs;

/**
 * Makes {@link CapturedLogs} the logging backend of the tests. SLF4J 1.7, the
 * version on the test classpath, looks for this class by its name.
 */
public final class StaticLoggerBinder implements LoggerFactoryBinder {

    /** Read by SLF4J, so it must not be a compile-time constant. */
    public static String REQUESTED_API_VERSION = "1.7.36";

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    private final ILoggerFactory factory = CapturedLogs::logger;

    private StaticLoggerBinder() {
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return factory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return CapturedLogs.class.getName();
    }
}
