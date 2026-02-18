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
package com.vaadin.flow.component.combobox;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ComboBoxSignalTest extends AbstractSignalsUnitTest {

    private ComboBox<String> comboBox;
    private ValueSignal<Boolean> requiredSignal;

    @Before
    public void setup() {
        comboBox = new ComboBox<>();
        requiredSignal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (comboBox != null && comboBox.isAttached()) {
            comboBox.removeFromParent();
        }
    }

    @Test
    public void bindRequired_signalBound_requiredSynchronizedWhenAttached() {
        comboBox.bindRequired(requiredSignal);
        UI.getCurrent().add(comboBox);

        Assert.assertFalse(
                comboBox.getElement().getProperty("required", false));

        requiredSignal.set(true);
        Assert.assertTrue(comboBox.getElement().getProperty("required", false));

        requiredSignal.set(false);
        Assert.assertFalse(
                comboBox.getElement().getProperty("required", false));
    }

    @Test
    public void bindRequired_signalBound_noEffectWhenDetached() {
        comboBox.bindRequired(requiredSignal);
        // Not attached to UI

        boolean initial = comboBox.getElement().getProperty("required", false);
        requiredSignal.set(true);
        Assert.assertEquals(initial,
                comboBox.getElement().getProperty("required", false));
    }

    @Test
    public void bindRequired_signalBound_detachAndReattach() {
        comboBox.bindRequired(requiredSignal);
        UI.getCurrent().add(comboBox);
        Assert.assertFalse(
                comboBox.getElement().getProperty("required", false));

        // Detach
        comboBox.removeFromParent();
        requiredSignal.set(true);
        Assert.assertFalse(
                comboBox.getElement().getProperty("required", false));

        // Reattach
        UI.getCurrent().add(comboBox);
        Assert.assertTrue(comboBox.getElement().getProperty("required", false));

        requiredSignal.set(false);
        Assert.assertFalse(
                comboBox.getElement().getProperty("required", false));
    }

    @Test(expected = NullPointerException.class)
    public void bindRequired_nullSignal_throwsNPE() {
        comboBox.bindRequired(null);
    }

    @Test(expected = BindingActiveException.class)
    public void bindRequired_setRequiredWhileBound_throwsException() {
        comboBox.bindRequired(requiredSignal);
        UI.getCurrent().add(comboBox);

        comboBox.setRequired(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindRequired_bindAgainWhileBound_throwsException() {
        comboBox.bindRequired(requiredSignal);
        UI.getCurrent().add(comboBox);

        comboBox.bindRequired(new ValueSignal<>(true));
    }
}
