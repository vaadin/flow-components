/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.data.binder.Binder;

/**
 * Tests for {@link BinderReflection}'s public surface. The reflection
 * fall-through paths (NPE → catch → log + empty result) work for a {@code null}
 * binder but emit a WARN every call, which would flood logs whenever the
 * controller is constructed without a binder. Each method must short-circuit
 * silently on {@code null}.
 */
class BinderReflectionTest {

    @BeforeEach
    void clearLogger() {
        TestLoggerFactory.getTestLogger(BinderReflection.class).clearAll();
    }

    @Test
    void collectPropertyNamesOnNullBinderReturnsEmptyWithoutWarning() {
        // FormAIController's single-arg constructor passes a null binder
        // through to collectPropertyNames on every get_form_state call; an
        // exception-handled fallback would log a warning on every request.
        var result = BinderReflection.collectPropertyNames(null);

        Assertions.assertTrue(result.isEmpty(),
                "Null binder must produce an empty map, got: " + result);
        var warnings = TestLoggerFactory.getTestLogger(BinderReflection.class)
                .getLoggingEvents().stream()
                .filter(e -> e.getLevel() == Level.WARN).toList();
        Assertions.assertTrue(warnings.isEmpty(),
                "Null binder is a normal no-binder construction, not a "
                        + "reflection failure, and must not WARN, got: "
                        + warnings);
    }

    @Test
    void beanValidationErrorsOnNullBinderReturnsEmptyWithoutWarning() {
        // FormAIController's single-arg constructor passes a null binder
        // through to beanValidationErrors on every fill_form call; an
        // exception-handled fallback would log a warning on every request.
        var result = BinderReflection.beanValidationErrors(null);

        Assertions.assertTrue(result.isEmpty(),
                "Null binder must produce an empty list, got: " + result);
        var warnings = TestLoggerFactory.getTestLogger(BinderReflection.class)
                .getLoggingEvents().stream()
                .filter(e -> e.getLevel() == Level.WARN).toList();
        Assertions.assertTrue(warnings.isEmpty(),
                "Null binder is a normal no-binder construction, not a "
                        + "reflection failure, and must not WARN, got: "
                        + warnings);
    }

    @Test
    void findBindingOnNullBinderReturnsNullWithoutWarning() {
        var result = BinderReflection.findBinding(null, new TestField());

        Assertions.assertNull(result);
        var warnings = TestLoggerFactory.getTestLogger(BinderReflection.class)
                .getLoggingEvents().stream()
                .filter(e -> e.getLevel() == Level.WARN).toList();
        Assertions.assertTrue(warnings.isEmpty(),
                "Null binder must not WARN on every validation call, got: "
                        + warnings);
    }

    @Test
    void setBoundPropertiesAccessible() {
        Assertions.assertDoesNotThrow(() -> {
            var field = Binder.class.getDeclaredField("boundProperties");
            field.setAccessible(true);
        }, "Could not access Binder.boundProperties.");
    }
}
