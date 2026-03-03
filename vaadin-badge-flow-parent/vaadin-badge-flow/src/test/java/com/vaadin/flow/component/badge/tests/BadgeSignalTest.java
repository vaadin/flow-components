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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeFeatureFlagProvider;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;
import com.vaadin.tests.EnableFeatureFlagRule;

public class BadgeSignalTest extends AbstractSignalsUnitTest {

    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            BadgeFeatureFlagProvider.BADGE_COMPONENT);

    private ValueSignal<String> textSignal;
    private ValueSignal<Integer> numberSignal;

    @Before
    public void setup() {
        textSignal = new ValueSignal<>("foo");
        numberSignal = new ValueSignal<>(0);
    }

    @Test
    public void textSignalConstructor() {
        Badge badge = new Badge(textSignal);
        ui.add(badge);
        Assert.assertNull(badge.getIcon());
        Assert.assertNull(badge.getNumber());
        Assert.assertEquals("foo", badge.getText());
    }

    @Test
    public void textSignalAndIconConstructor() {
        Span icon = new Span();
        Badge badge = new Badge(textSignal, icon);
        ui.add(badge);
        Assert.assertNull(badge.getNumber());
        Assert.assertEquals(icon, badge.getIcon());
        Assert.assertEquals("foo", badge.getText());
    }

    @Test
    public void bindTextSignal() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        ui.add(badge);
        Assert.assertEquals("foo", badge.getText());
        Assert.assertEquals("foo", badge.getElement().getText());

        textSignal.set(null);
        Assert.assertNull(badge.getText());
        Assert.assertEquals("", badge.getElement().getText());

        textSignal.set("bar");
        Assert.assertEquals("bar", badge.getText());
        Assert.assertEquals("bar", badge.getElement().getText());
    }

    @Test
    public void bindText_replacesContent() {
        Badge badge = new Badge();
        Span content = new Span();
        badge.setContent(content);

        badge.bindText(textSignal);
        ui.add(badge);

        Assert.assertNull(badge.getContent());
        Assert.assertFalse(content.getParent().isPresent());
    }

    @Test(expected = BindingActiveException.class)
    public void bindTextSignal_setText_throws() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        badge.setText("bar");
    }

    @Test(expected = BindingActiveException.class)
    public void bindTextSignal_setContent_throws() {
        Badge badge = new Badge();
        badge.bindText(textSignal);
        badge.setContent(new Span());
    }

    @Test
    public void bindNumberSignal() {
        Badge badge = new Badge();
        badge.bindNumber(numberSignal);
        ui.add(badge);
        Assert.assertEquals((Integer) 0, badge.getNumber());

        numberSignal.set(null);
        Assert.assertNull(badge.getNumber());

        numberSignal.set(50);
        Assert.assertEquals((Integer) 50, badge.getNumber());
    }

    @Test(expected = BindingActiveException.class)
    public void bindNumberSignal_setNumber_throws() {
        Badge badge = new Badge();
        badge.bindNumber(numberSignal);
        badge.setNumber(5);
    }
}
