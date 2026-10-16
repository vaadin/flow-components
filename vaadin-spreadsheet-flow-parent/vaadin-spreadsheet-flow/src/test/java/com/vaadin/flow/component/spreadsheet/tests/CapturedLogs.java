/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * The warnings and errors logged while a test looks, so it can tell when the
 * component reports a problem and goes on.
 * <p>
 * The tests run without a logging backend; {@code org.slf4j.impl}'s binder in
 * the test sources makes this class the backend. Nothing is printed, and
 * nothing is kept while no capture is open.
 */
public final class CapturedLogs {

    /** A warning or an error, as logged. */
    public record Entry(String logger, String level, String message,
            Throwable throwable) {
        @Override
        public String toString() {
            return level.toUpperCase() + " " + logger + ": " + message
                    + (throwable == null ? "" : " (" + throwable + ")");
        }
    }

    private static volatile Capture current;

    private CapturedLogs() {
    }

    /**
     * Starts keeping the warnings and errors logged, until the capture is
     * closed.
     */
    public static Capture capture() {
        Capture capture = new Capture();
        current = capture;
        return capture;
    }

    /** The warnings and errors logged while it is open. */
    public static final class Capture implements AutoCloseable {
        private final List<Entry> entries = Collections
                .synchronizedList(new ArrayList<>());

        private Capture() {
        }

        public List<Entry> entries() {
            synchronized (entries) {
                return List.copyOf(entries);
            }
        }

        @Override
        public void close() {
            if (current == this) {
                current = null;
            }
        }
    }

    /** A logger that keeps what is logged at WARN and ERROR. */
    public static Logger logger(String name) {
        return (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(),
                new Class<?>[] { Logger.class }, (proxy, method, args) -> {
                    switch (method.getName()) {
                    case "getName":
                        return name;
                    case "isWarnEnabled", "isErrorEnabled":
                        return true;
                    case "warn", "error":
                        keep(name, method.getName(), args);
                        return null;
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    case "toString":
                        return "CapturedLogs.logger(" + name + ")";
                    default:
                        return method.getReturnType() == boolean.class ? false
                                : null;
                    }
                });
    }

    private static void keep(String logger, String level, Object[] args) {
        Capture capture = current;
        if (capture == null) {
            return;
        }
        // The variants with a marker take it first.
        int first = args[0] instanceof Marker ? 1 : 0;
        String format = (String) args[first];
        Object[] parameters = Arrays.copyOfRange(args, first + 1, args.length);
        if (parameters.length == 1 && parameters[0] instanceof Object[] all) {
            parameters = all;
        }
        FormattingTuple formatted = MessageFormatter.arrayFormat(format,
                parameters);
        capture.entries.add(new Entry(logger, level, formatted.getMessage(),
                formatted.getThrowable()));
    }
}
