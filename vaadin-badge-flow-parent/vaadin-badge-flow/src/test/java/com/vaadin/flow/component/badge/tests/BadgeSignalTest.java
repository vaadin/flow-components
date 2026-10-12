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
package com.vaadin.flow.component.badge.tests;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class BadgeSignalTest extends AbstractSignalsTest {

    private ValueSignal<String> textSignal;
    private ValueSignal<Integer> numberSignal;

    @BeforeEach
    void setup() {
        textSignal = new ValueSignal<>("foo");
        numberSignal = new ValueSignal<>(0);
    }

    @TestFactory
    Stream<DynamicTest> bindText() {
        return generateBindingTests(Badge::new, Badge::bindText, Badge::getText,
                Badge::setText, "foo", "bar");
    }

    @TestFactory
    Stream<DynamicTest> bindNumber() {
        return generateBindingTests(Badge::new, Badge::bindNumber,
                Badge::getNumber, Badge::setNumber, 5, 10);
    }

    @Test
    void textSignalConstructor() {
        Badge badge = new Badge(textSignal);
        ui.add(badge);
        Assertions.assertNull(badge.getIcon());
        Assertions.assertNull(badge.getNumber());
        Assertions.assertEquals("foo", badge.getText());
    }

    @Test
    void textSignalAndIconConstructor() {
        Span icon = new Span();
        Badge badge = new Badge(textSignal, icon);
        ui.add(badge);
        Assertions.assertNull(badge.getNumber());
        Assertions.assertEquals(icon, badge.getIcon());
        Assertions.assertEquals("foo", badge.getText());
    }

    @Test
    void bindText_updatesElementText() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        ui.add(badge);
        Assertions.assertEquals("foo", badge.getElement().getText());

        textSignal.set("bar");
        Assertions.assertEquals("bar", badge.getElement().getText());
    }

    @Test
    void bindText_nullValue_clearsText() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        ui.add(badge);

        textSignal.set(null);
        Assertions.assertNull(badge.getText());
        Assertions.assertEquals("", badge.getElement().getText());
    }

    @Test
    void bindText_replacesContent() {
        Badge badge = new Badge();
        Span content = new Span();
        badge.setContent(content);

        badge.bindText(textSignal);
        ui.add(badge);

        Assertions.assertNull(badge.getContent());
        Assertions.assertFalse(content.getParent().isPresent());
    }

    @Test
    void bindTextSignal_setContent_throws() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> badge.setContent(new Span()));
    }

    @Test
    void bindNumber_nullValue_clearsNumber() {
        Badge badge = new Badge();
        badge.bindNumber(numberSignal);
        ui.add(badge);

        numberSignal.set(null);
        Assertions.assertNull(badge.getNumber());
    }
}
