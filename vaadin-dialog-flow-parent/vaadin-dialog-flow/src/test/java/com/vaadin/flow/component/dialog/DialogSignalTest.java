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
package com.vaadin.flow.component.dialog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.nodefeature.SignalBindingFeature;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

/**
 * Unit tests for Dialog signals.
 */
public class DialogSignalTest extends AbstractSignalsUnitTest {

    private Dialog dialog;
    private ValueSignal<Boolean> openedSignal;

    @Before
    public void setup() {
        dialog = new Dialog();
        openedSignal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (dialog != null && dialog.isAttached()) {
            dialog.removeFromParent();
        }
    }

    private void detachUI(UI ui) {
        ui.getInternals().setSession(null);
    }

    @Test
    public void bindOpened_notSynchronizedWhenUIDetached() {

        dialog.bindOpened(openedSignal);
        Assert.assertFalse(dialog.isOpened());

        detachUI(UI.getCurrent());

        openedSignal.value(true);
        Assert.assertFalse(dialog.isOpened());
    }

    @Test
    public void bindOpened_signalBound_openedSynchronizedWhenUIAttached() {
        dialog.bindOpened(openedSignal);

        Assert.assertFalse(dialog.isOpened());

        openedSignal.value(true);
        Assert.assertTrue(dialog.isOpened());

        openedSignal.value(false);
        Assert.assertFalse(dialog.isOpened());
    }

    @Test
    public void bindOpened_nullUnbindsSignal() {
        dialog.bindOpened(openedSignal);
        Assert.assertFalse(dialog.isOpened());

        dialog.bindOpened(null);
        openedSignal.value(true);
        Assert.assertFalse(dialog.isOpened());

        // Should be able to set manually after unbinding
        dialog.setOpened(true);
        Assert.assertTrue(dialog.isOpened());

    }

    @Test
    public void bindOpened_setOpenedWhileBound_updatesSignal() {
        // DialogSignal allows setOpened while bound (two-way binding)
        dialog.bindOpened(openedSignal);

        dialog.setOpened(true);
        Assert.assertTrue(dialog.isOpened());
        Assert.assertTrue(openedSignal.peek());
    }

    @Test(expected = BindingActiveException.class)
    public void bindOpened_bindAgainWhileBound_throwsException() {
        dialog.bindOpened(openedSignal);

        ValueSignal<Boolean> anotherSignal = new ValueSignal<>(true);
        dialog.bindOpened(anotherSignal);
    }

    @Test
    public void bindOpened_initialValueIsApplied() {
        ValueSignal<Boolean> initiallyOpenSignal = new ValueSignal<>(true);
        dialog.bindOpened(initiallyOpenSignal);

        Assert.assertTrue(dialog.isOpened());
    }

    @Test
    public void bindOpened_nullSignalValue() {
        ValueSignal<Boolean> nullableSignal = new ValueSignal<>(null);
        dialog.bindOpened(nullableSignal);

        Assert.assertFalse(dialog.isOpened()); // null treated as false

        nullableSignal.value(true);
        Assert.assertTrue(dialog.isOpened());

        nullableSignal.value(null);
        Assert.assertFalse(dialog.isOpened()); // null treated as false
    }

    @Test
    public void bindOpened_nullClearsOldBinding() {
        dialog.bindOpened(openedSignal);

        SignalBindingFeature feature = dialog.getElement().getNode()
                .getFeature(SignalBindingFeature.class);
        Assert.assertNotNull(feature.getSignal(SignalBindingFeature.VALUE));

        dialog.bindOpened(null);
        Assert.assertNull(feature.getSignal(SignalBindingFeature.VALUE));
    }
}
