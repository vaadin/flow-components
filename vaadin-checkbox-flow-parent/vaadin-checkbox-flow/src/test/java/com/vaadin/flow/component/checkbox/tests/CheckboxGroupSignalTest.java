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
package com.vaadin.flow.component.checkbox.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class CheckboxGroupSignalTest extends AbstractSignalsUnitTest {

    private CheckboxGroup<String> checkboxGroup;
    private ValueSignal<Boolean> requiredSignal;

    @Before
    public void setup() {
        checkboxGroup = new CheckboxGroup<>();
        requiredSignal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (checkboxGroup != null && checkboxGroup.isAttached()) {
            checkboxGroup.removeFromParent();
        }
    }

    @Test
    public void bindRequired_signalBound_requiredSynchronizedWhenAttached() {
        checkboxGroup.bindRequired(requiredSignal);
        UI.getCurrent().add(checkboxGroup);

        Assert.assertFalse(
                checkboxGroup.getElement().getProperty("required", false));

        requiredSignal.set(true);
        Assert.assertTrue(
                checkboxGroup.getElement().getProperty("required", false));

        requiredSignal.set(false);
        Assert.assertFalse(
                checkboxGroup.getElement().getProperty("required", false));
    }

    @Test
    public void bindRequired_signalBound_noEffectWhenDetached() {
        checkboxGroup.bindRequired(requiredSignal);
        // Not attached to UI

        boolean initial = checkboxGroup.getElement().getProperty("required",
                false);
        requiredSignal.set(true);
        Assert.assertEquals(initial,
                checkboxGroup.getElement().getProperty("required", false));
    }

    @Test
    public void bindRequired_signalBound_detachAndReattach() {
        checkboxGroup.bindRequired(requiredSignal);
        UI.getCurrent().add(checkboxGroup);
        Assert.assertFalse(
                checkboxGroup.getElement().getProperty("required", false));

        // Detach
        checkboxGroup.removeFromParent();
        requiredSignal.set(true);
        Assert.assertFalse(
                checkboxGroup.getElement().getProperty("required", false));

        // Reattach
        UI.getCurrent().add(checkboxGroup);
        Assert.assertTrue(
                checkboxGroup.getElement().getProperty("required", false));

        requiredSignal.set(false);
        Assert.assertFalse(
                checkboxGroup.getElement().getProperty("required", false));
    }

    @Test(expected = NullPointerException.class)
    public void bindRequired_nullSignal_throwsNPE() {
        checkboxGroup.bindRequired(null);
    }

    @Test(expected = BindingActiveException.class)
    public void bindRequired_setRequiredWhileBound_throwsException() {
        checkboxGroup.bindRequired(requiredSignal);
        UI.getCurrent().add(checkboxGroup);

        checkboxGroup.setRequired(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindRequired_bindAgainWhileBound_throwsException() {
        checkboxGroup.bindRequired(requiredSignal);
        UI.getCurrent().add(checkboxGroup);

        checkboxGroup.bindRequired(new ValueSignal<>(true));
    }
}
