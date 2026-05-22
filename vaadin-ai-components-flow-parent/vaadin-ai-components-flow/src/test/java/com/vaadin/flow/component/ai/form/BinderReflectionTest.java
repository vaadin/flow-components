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
package com.vaadin.flow.component.ai.form;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.data.binder.Binder;

class BinderReflectionTest {

    private final TestLogger logger = TestLoggerFactory
            .getTestLogger(BinderReflection.class);

    @BeforeEach
    void clearLogger() {
        logger.clear();
    }

    @Test
    void collectPropertyNames_nullBinder_returnsEmptyMapWithoutLogging() {
        // Callers (including FormAIController#seedDescriptionsFromBinder)
        // pass the field unconditionally — a null binder is the no-binder
        // controller path and must not produce log noise on every prompt.
        var result = BinderReflection.collectPropertyNames(null);

        Assertions.assertTrue(result.isEmpty(),
                "Null binder must yield an empty map, got: " + result);
        var warnings = logger.getLoggingEvents().stream()
                .filter(event -> event.getLevel() == Level.WARN).toList();
        Assertions.assertTrue(warnings.isEmpty(),
                "Null binder must not log a warning; got: " + warnings);
    }

    @Test
    void setBoundPropertiesAccessible() {
        Assertions.assertDoesNotThrow(() -> {
            var field = Binder.class.getDeclaredField("boundProperties");
            field.setAccessible(true);
        }, "Could not access Binder.boundProperties.");
    }
}
