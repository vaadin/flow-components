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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class AppLayoutSignalTest extends AbstractSignalsTest {

    private AppLayout appLayout;
    private ValueSignal<Boolean> signal;

    @BeforeEach
    void setup() {
        appLayout = new AppLayout();
        signal = new ValueSignal<>(true);
    }

    @AfterEach
    void tearDown() {
        if (appLayout != null && appLayout.isAttached()) {
            appLayout.removeFromParent();
        }
    }

    @Test
    void bindDrawerOpened_signalBound_propertySync() {
        appLayout.bindDrawerOpened(signal, signal::set);
        UI.getCurrent().add(appLayout);

        Assertions.assertTrue(appLayout.isDrawerOpened());

        signal.set(false);
        Assertions.assertFalse(appLayout.isDrawerOpened());

        signal.set(true);
        Assertions.assertTrue(appLayout.isDrawerOpened());
    }

    @Test
    void bindDrawerOpened_notAttached_noEffect() {
        appLayout.bindDrawerOpened(signal, signal::set);

        boolean initial = appLayout.getElement().getProperty("drawerOpened",
                true);
        signal.set(false);
        Assertions.assertEquals(initial,
                appLayout.getElement().getProperty("drawerOpened", true));
    }

    @Test
    void bindDrawerOpened_detachAndReattach() {
        appLayout.bindDrawerOpened(signal, signal::set);
        UI.getCurrent().add(appLayout);

        signal.set(false);
        Assertions.assertFalse(appLayout.isDrawerOpened());

        appLayout.removeFromParent();
        signal.set(true);
        Assertions.assertFalse(appLayout.isDrawerOpened());

        UI.getCurrent().add(appLayout);
        Assertions.assertTrue(appLayout.isDrawerOpened());
    }

    void bindDrawerOpened_setWhileBound_syncsToSignal() {
        appLayout.bindDrawerOpened(signal, signal::set);
        UI.getCurrent().add(appLayout);

        appLayout.setDrawerOpened(false);

        Assertions.assertFalse(signal.peek());
    }

    @Test
    void bindDrawerOpened_doubleBind_throws() {
        appLayout.bindDrawerOpened(signal, signal::set);
        var other = new ValueSignal<>(false);

        Assertions.assertThrows(BindingActiveException.class,
                () -> appLayout.bindDrawerOpened(other, other::set));
    }

    @Test
    void bindDrawerOpened_nullSignal_throwsNPE() {
        Assertions.assertThrows(NullPointerException.class,
                () -> appLayout.bindDrawerOpened(null, null));
    }
}
