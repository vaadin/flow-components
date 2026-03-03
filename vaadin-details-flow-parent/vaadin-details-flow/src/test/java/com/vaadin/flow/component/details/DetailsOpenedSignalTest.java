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
package com.vaadin.flow.component.details;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class DetailsOpenedSignalTest extends AbstractSignalsUnitTest {

    private Details details;
    private ValueSignal<Boolean> signal;

    @Before
    public void setup() {
        details = new Details();
        signal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (details != null && details.isAttached()) {
            details.removeFromParent();
        }
    }

    @Test
    public void bindOpened_signalBound_propertySync() {
        details.bindOpened(signal);
        UI.getCurrent().add(details);

        Assert.assertFalse(details.isOpened());

        signal.set(true);
        Assert.assertTrue(details.isOpened());

        signal.set(false);
        Assert.assertFalse(details.isOpened());
    }

    @Test
    public void bindOpened_notAttached_noEffect() {
        details.bindOpened(signal);

        boolean initial = details.isOpened();
        signal.set(true);
        Assert.assertEquals(initial, details.isOpened());
    }

    @Test
    public void bindOpened_detachAndReattach() {
        details.bindOpened(signal);
        UI.getCurrent().add(details);

        signal.set(true);
        Assert.assertTrue(details.isOpened());

        details.removeFromParent();
        signal.set(false);
        Assert.assertTrue(details.isOpened());

        UI.getCurrent().add(details);
        Assert.assertFalse(details.isOpened());
    }

    @Test(expected = BindingActiveException.class)
    public void bindOpened_setWhileBound_throws() {
        details.bindOpened(signal);
        UI.getCurrent().add(details);

        details.setOpened(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindOpened_doubleBind_throws() {
        details.bindOpened(signal);
        details.bindOpened(new ValueSignal<>(true));
    }

    @Test(expected = NullPointerException.class)
    public void bindOpened_nullSignal_throwsNPE() {
        details.bindOpened(null);
    }
}
