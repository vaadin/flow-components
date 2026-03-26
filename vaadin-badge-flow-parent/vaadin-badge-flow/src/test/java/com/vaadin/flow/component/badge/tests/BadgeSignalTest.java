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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeFeatureFlagProvider;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;
import com.vaadin.tests.EnableFeatureFlagExtension;

class BadgeSignalTest extends AbstractSignalsTest {

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            BadgeFeatureFlagProvider.BADGE_COMPONENT);

    private ValueSignal<String> textSignal;
    private ValueSignal<Integer> numberSignal;

    @BeforeEach
    void setup() {
        textSignal = new ValueSignal<>("foo");
        numberSignal = new ValueSignal<>(0);
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
    void bindTextSignal() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        ui.add(badge);
        Assertions.assertEquals("foo", badge.getText());
        Assertions.assertEquals("foo", badge.getElement().getText());

        textSignal.set(null);
        Assertions.assertNull(badge.getText());
        Assertions.assertEquals("", badge.getElement().getText());

        textSignal.set("bar");
        Assertions.assertEquals("bar", badge.getText());
        Assertions.assertEquals("bar", badge.getElement().getText());
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
    void bindTextSignal_setText_throws() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> badge.setText("bar"));
    }

    @Test
    void bindTextSignal_setContent_throws() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> badge.setContent(new Span()));
    }

    @Test
    void bindNumberSignal() {
        Badge badge = new Badge();
        badge.bindNumber(numberSignal);
        ui.add(badge);
        Assertions.assertEquals((Integer) 0, badge.getNumber());

        numberSignal.set(null);
        Assertions.assertNull(badge.getNumber());

        numberSignal.set(50);
        Assertions.assertEquals((Integer) 50, badge.getNumber());
    }

    @Test
    void bindNumberSignal_setNumber_throws() {
        Badge badge = new Badge();
        badge.bindNumber(numberSignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> badge.setNumber(5));
    }
}
