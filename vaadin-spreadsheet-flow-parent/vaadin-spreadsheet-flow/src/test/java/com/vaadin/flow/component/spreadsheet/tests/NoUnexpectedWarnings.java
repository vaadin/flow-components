/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.List;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Fails a test in which the component logs a warning or an error, unless the
 * test is marked with {@link ExpectWarnings}: a problem the component reports
 * and goes on from is a problem the test did not see. Every test of the module
 * has it, through {@code junit-platform.properties}.
 */
public class NoUnexpectedWarnings
        implements BeforeEachCallback, AfterEachCallback {

    /** The loggers of the component, by the start of their names. */
    static final String COMPONENT = "com.vaadin.flow.component.spreadsheet";

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(NoUnexpectedWarnings.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getStore(NAMESPACE).put(CapturedLogs.Capture.class,
                CapturedLogs.capture());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        CapturedLogs.Capture capture = context.getStore(NAMESPACE)
                .remove(CapturedLogs.Capture.class, CapturedLogs.Capture.class);
        capture.close();
        boolean expected = context.getTestMethod()
                .map(method -> method.isAnnotationPresent(ExpectWarnings.class))
                .orElse(false)
                || context.getTestClass().map(
                        type -> type.isAnnotationPresent(ExpectWarnings.class))
                        .orElse(false);
        List<CapturedLogs.Entry> unexpected = unexpected(capture.entries(),
                expected);
        if (!unexpected.isEmpty()) {
            throw new AssertionError(
                    "The component logged what the test does not expect: "
                            + unexpected);
        }
    }

    /** The entries of the component's loggers a test did not expect. */
    static List<CapturedLogs.Entry> unexpected(List<CapturedLogs.Entry> logged,
            boolean expected) {
        return expected ? List.of()
                : logged.stream()
                        .filter(entry -> entry.logger().startsWith(COMPONENT))
                        .toList();
    }
}
