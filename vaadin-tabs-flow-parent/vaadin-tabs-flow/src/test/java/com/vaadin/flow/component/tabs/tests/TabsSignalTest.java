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
package com.vaadin.flow.component.tabs.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class TabsSignalTest extends AbstractSignalsUnitTest {

    private Tabs tabs;
    private ValueSignal<Integer> signal;

    @Before
    public void setup() {
        tabs = new Tabs(false, new Tab("Tab 1"), new Tab("Tab 2"),
                new Tab("Tab 3"));
        signal = new ValueSignal<>(0);
    }

    @After
    public void tearDown() {
        if (tabs != null && tabs.isAttached()) {
            tabs.removeFromParent();
        }
    }

    @Test
    public void bindSelectedIndex_signalBound_propertySync() {
        tabs.bindSelectedIndex(signal, signal::set);
        UI.getCurrent().add(tabs);

        Assert.assertEquals(0, tabs.getSelectedIndex());

        signal.set(1);
        Assert.assertEquals(1, tabs.getSelectedIndex());

        signal.set(2);
        Assert.assertEquals(2, tabs.getSelectedIndex());
    }

    @Test
    public void bindSelectedIndex_notAttached_noEffect() {
        tabs.bindSelectedIndex(signal, signal::set);

        int initial = tabs.getSelectedIndex();
        signal.set(2);
        Assert.assertEquals(initial, tabs.getSelectedIndex());
    }

    @Test
    public void bindSelectedIndex_detachAndReattach() {
        tabs.bindSelectedIndex(signal, signal::set);
        UI.getCurrent().add(tabs);

        signal.set(1);
        Assert.assertEquals(1, tabs.getSelectedIndex());

        tabs.removeFromParent();
        signal.set(2);
        Assert.assertEquals(1, tabs.getSelectedIndex());

        UI.getCurrent().add(tabs);
        Assert.assertEquals(2, tabs.getSelectedIndex());
    }

    @Test(expected = BindingActiveException.class)
    public void bindSelectedIndex_setWhileBound_throws() {
        tabs.bindSelectedIndex(signal, signal::set);
        UI.getCurrent().add(tabs);

        tabs.setSelectedIndex(1);
    }

    @Test(expected = BindingActiveException.class)
    public void bindSelectedIndex_doubleBind_throws() {
        tabs.bindSelectedIndex(signal, signal::set);
        var other = new ValueSignal<>(1);
        tabs.bindSelectedIndex(other, other::set);
    }

    @Test(expected = NullPointerException.class)
    public void bindSelectedIndex_nullSignal_throwsNPE() {
        tabs.bindSelectedIndex(null, null);
    }
}
