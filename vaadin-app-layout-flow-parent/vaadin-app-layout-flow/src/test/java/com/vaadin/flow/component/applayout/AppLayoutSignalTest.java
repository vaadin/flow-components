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
package com.vaadin.flow.component.applayout;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class AppLayoutSignalTest extends AbstractSignalsUnitTest {

    private AppLayout appLayout;
    private ValueSignal<Boolean> signal;

    @Before
    public void setup() {
        appLayout = new AppLayout();
        signal = new ValueSignal<>(true);
    }

    @After
    public void tearDown() {
        if (appLayout != null && appLayout.isAttached()) {
            appLayout.removeFromParent();
        }
    }

    @Test
    public void bindDrawerOpened_signalBound_propertySync() {
        appLayout.bindDrawerOpened(signal, signal::set);
        UI.getCurrent().add(appLayout);

        Assert.assertTrue(appLayout.isDrawerOpened());

        signal.set(false);
        Assert.assertFalse(appLayout.isDrawerOpened());

        signal.set(true);
        Assert.assertTrue(appLayout.isDrawerOpened());
    }

    @Test
    public void bindDrawerOpened_notAttached_noEffect() {
        appLayout.bindDrawerOpened(signal, signal::set);

        boolean initial = appLayout.getElement().getProperty("drawerOpened",
                true);
        signal.set(false);
        Assert.assertEquals(initial,
                appLayout.getElement().getProperty("drawerOpened", true));
    }

    @Test
    public void bindDrawerOpened_detachAndReattach() {
        appLayout.bindDrawerOpened(signal, signal::set);
        UI.getCurrent().add(appLayout);

        signal.set(false);
        Assert.assertFalse(appLayout.isDrawerOpened());

        appLayout.removeFromParent();
        signal.set(true);
        Assert.assertFalse(appLayout.isDrawerOpened());

        UI.getCurrent().add(appLayout);
        Assert.assertTrue(appLayout.isDrawerOpened());
    }

    @Test(expected = BindingActiveException.class)
    public void bindDrawerOpened_setWhileBound_throws() {
        appLayout.bindDrawerOpened(signal, signal::set);
        UI.getCurrent().add(appLayout);

        appLayout.setDrawerOpened(false);
    }

    @Test(expected = BindingActiveException.class)
    public void bindDrawerOpened_doubleBind_throws() {
        appLayout.bindDrawerOpened(signal, signal::set);
        var other = new ValueSignal<>(false);
        appLayout.bindDrawerOpened(other, other::set);
    }

    @Test(expected = NullPointerException.class)
    public void bindDrawerOpened_nullSignal_throwsNPE() {
        appLayout.bindDrawerOpened(null, null);
    }

    @Test
    public void bindDrawerOpened_nullDefault_defaultsToTrue() {
        ValueSignal<Boolean> nullSignal = new ValueSignal<>(null);
        appLayout.bindDrawerOpened(nullSignal, nullSignal::set);
        UI.getCurrent().add(appLayout);

        Assert.assertTrue(appLayout.isDrawerOpened());
    }
}
