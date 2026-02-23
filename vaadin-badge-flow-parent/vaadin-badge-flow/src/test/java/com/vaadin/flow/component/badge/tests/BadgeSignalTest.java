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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeFeatureFlagProvider;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;
import com.vaadin.tests.EnableFeatureFlagRule;

public class BadgeSignalTest extends AbstractSignalsUnitTest {

    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            BadgeFeatureFlagProvider.BADGE_COMPONENT);

    private Badge badge;
    private ValueSignal<String> textSignal;
    private Signal<String> computedSignal;
    private ValueSignal<Integer> numberSignal;

    @Before
    public void setup() {
        textSignal = new ValueSignal<>("foo");
        computedSignal = Signal.computed(() -> textSignal.get() + " bar");
        numberSignal = new ValueSignal<>(0);
    }

    @After
    public void tearDown() {
        if (badge != null && badge.isAttached()) {
            badge.removeFromParent();
        }
    }

    @Test
    public void textSignalCtor() {
        badge = new Badge(textSignal);
        UI.getCurrent().add(badge);
        assertTextSignalBindingActive();
    }

    @Test
    public void textSignalAndIconCtor() {
        Span icon = new Span();
        badge = new Badge(textSignal, icon);
        UI.getCurrent().add(badge);
        assertTextSignalBindingActive();
        Assert.assertEquals(icon, badge.getIcon());
    }

    @Test
    public void textSignal_notAttached() {
        badge = new Badge(textSignal);
        assertTextSignalBindingInactive();
    }

    @Test
    public void textSignal_detachedAndAttached() {
        badge = new Badge(textSignal);
        UI.getCurrent().add(badge);
        badge.removeFromParent();
        assertTextSignalBindingInactive();

        UI.getCurrent().add(badge);
        assertTextSignalBindingActive();
    }

    @Test
    public void textComputedSignalCtor() {
        badge = new Badge(computedSignal);
        UI.getCurrent().add(badge);
        Assert.assertEquals("foo bar", badge.getText());
        textSignal.set("bar");
        Assert.assertEquals("bar bar", badge.getText());
    }

    @Test(expected = BindingActiveException.class)
    public void textSignalAndSetText_error() {
        badge = new Badge(textSignal);
        UI.getCurrent().add(badge);
        badge.setText("bar");
    }

    @Test(expected = BindingActiveException.class)
    public void textSignal_rebind_error() {
        badge = new Badge(textSignal);
        UI.getCurrent().add(badge);
        badge.bindText(textSignal);
    }

    @Test
    public void numberSignal_binding() {
        badge = new Badge();
        badge.bindNumber(numberSignal);
        UI.getCurrent().add(badge);
        numberSignal.set(5);
        Assert.assertEquals(Integer.valueOf(5), badge.getNumber());
        numberSignal.set(10);
        Assert.assertEquals(Integer.valueOf(10), badge.getNumber());
    }

    @Test(expected = BindingActiveException.class)
    public void numberSignalAndSetNumber_error() {
        badge = new Badge();
        badge.bindNumber(numberSignal);
        UI.getCurrent().add(badge);
        badge.setNumber(5);
    }

    @Test
    public void numberSignal_detachedAndAttached() {
        badge = new Badge();
        badge.bindNumber(numberSignal);
        UI.getCurrent().add(badge);
        badge.removeFromParent();
        assertNumberSignalBindingInactive();

        UI.getCurrent().add(badge);
        numberSignal.set(42);
        Assert.assertEquals(Integer.valueOf(42), badge.getNumber());
    }

    private void assertTextSignalBindingActive() {
        textSignal.set("foo");
        Assert.assertEquals("foo", badge.getText());
        textSignal.set("bar");
        Assert.assertEquals("bar", badge.getText());
    }

    private void assertTextSignalBindingInactive() {
        var currentText = badge.getText();
        textSignal.set(currentText + " with change");
        Assert.assertEquals(currentText, badge.getText());
    }

    private void assertNumberSignalBindingInactive() {
        var currentNumber = badge.getNumber();
        numberSignal.set(currentNumber != null ? currentNumber + 1 : 999);
        Assert.assertEquals(currentNumber, badge.getNumber());
    }
}
