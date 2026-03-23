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
package com.vaadin.flow.data.renderer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

class LitRendererTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void doNotAllowFunctionNamesWithFunctions() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction(
                        "foo=0; alert(\"gotcha\"); const bar", item -> {
                        }));
    }

    @Test
    void doNotAllowFunctionNamesWithSpaces() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegal name", item -> {
                }));
    }

    @Test
    void doNotAllowFunctionNamesWithDots() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegal.name", item -> {
                }));
    }

    @Test
    void doNotAllowFunctionNamesWithParenthesis() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> LitRenderer.of("").withFunction("illegalname()", item -> {
                }));
    }

    @Test
    void allowAlphaNumericFunctionNames() {
        LitRenderer.of("<div></div>").withFunction("legalName1", item -> {
        });
    }

    @Test
    void supportGettingValueProviders() {
        LitRenderer<?> renderer = LitRenderer.of("<div></div>")
                .withProperty("foo", item -> 1).withProperty("bar", item -> 2);

        Assertions.assertTrue(
                renderer.getValueProviders().keySet().contains("foo"));
        Assertions.assertTrue(
                renderer.getValueProviders().keySet().contains("bar"));
        Assertions.assertTrue(renderer.getValueProviders().size() == 2);
    }

}
