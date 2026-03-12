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
package com.vaadin.flow.component.shared.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;

class OverlayClassListProxyTest {
    private TestComponent component;
    private OverlayClassListProxy proxy;

    @BeforeEach
    void setup() {
        component = new TestComponent();
        proxy = new OverlayClassListProxy(component);
    }

    @Test
    void add() {
        proxy.add("foo");
        proxy.add("bar");
        proxy.add("baz");

        verifyClassNames("foo bar baz");

        // No duplicates
        proxy.add("foo");
        proxy.add("bar");
        proxy.add("baz");

        verifyClassNames("foo bar baz");
    }

    @Test
    void addAll() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        verifyClassNames("foo bar baz");

        // No duplicates
        proxy.addAll(List.of("foo", "bar", "baz"));

        verifyClassNames("foo bar baz");
    }

    @Test
    void remove() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.remove("bar");
        verifyClassNames("foo baz");

        proxy.remove("foo");
        verifyClassNames("baz");

        proxy.remove("baz");
        verifyClassNames(null);
    }

    @Test
    void removeAll() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.removeAll(List.of("foo", "baz"));
        verifyClassNames("bar");

        proxy.removeAll(List.of("bar"));
        verifyClassNames(null);
    }

    @Test
    void removeIf() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.removeIf("foo"::equals);
        verifyClassNames("bar baz");

        proxy.removeIf("baz"::equals);
        verifyClassNames("bar");

        proxy.removeIf("bar"::equals);
        verifyClassNames(null);
    }

    @Test
    void retainAll() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.retainAll(List.of("foo", "baz"));
        verifyClassNames("foo baz");

        proxy.retainAll(new ArrayList<String>());
        verifyClassNames(null);
    }

    @Test
    void clear() {
        proxy.addAll(List.of("foo", "bar", "baz"));

        proxy.clear();
        verifyClassNames(null);
    }

    @Test
    void contains() {
        proxy.add("foo");

        Assertions.assertTrue(proxy.contains("foo"));

        proxy.remove("foo");

        Assertions.assertFalse(proxy.contains("foo"));
    }

    @Test
    void containsAll() {
        proxy.addAll(List.of("foo", "bar"));

        Assertions.assertTrue(proxy.containsAll(List.of("foo", "bar")));

        proxy.remove("foo");

        Assertions.assertFalse(proxy.containsAll(List.of("foo", "bar")));
    }

    @Test
    void isEmpty() {
        Assertions.assertTrue(proxy.isEmpty());

        proxy.add("foo");

        Assertions.assertFalse(proxy.isEmpty());
    }

    private void verifyClassNames(String expected) {
        Assertions.assertEquals(expected,
                component.getElement().getProperty("overlayClass"));
        Assertions.assertEquals(expected, component.getClassName());
    }

    @Tag("test-component")
    private static class TestComponent extends Component implements HasStyle {
    }
}
