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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class CheckboxSignalTest extends AbstractSignalsUnitTest {

    private Checkbox checkbox;
    private ValueSignal<Boolean> signal;

    @Before
    public void setup() {
        checkbox = new Checkbox();
        signal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (checkbox != null && checkbox.isAttached()) {
            checkbox.removeFromParent();
        }
    }

    @Test
    public void bindIndeterminate_signalBound_propertySync() {
        checkbox.bindIndeterminate(signal, signal::set);
        UI.getCurrent().add(checkbox);

        Assert.assertFalse(checkbox.isIndeterminate());

        signal.set(true);
        Assert.assertTrue(checkbox.isIndeterminate());

        signal.set(false);
        Assert.assertFalse(checkbox.isIndeterminate());
    }

    @Test
    public void bindIndeterminate_notAttached_noEffect() {
        checkbox.bindIndeterminate(signal, signal::set);

        boolean initial = checkbox.isIndeterminate();
        signal.set(true);
        Assert.assertEquals(initial, checkbox.isIndeterminate());
    }

    @Test
    public void bindIndeterminate_detachAndReattach() {
        checkbox.bindIndeterminate(signal, signal::set);
        UI.getCurrent().add(checkbox);

        signal.set(true);
        Assert.assertTrue(checkbox.isIndeterminate());

        checkbox.removeFromParent();
        signal.set(false);
        Assert.assertTrue(checkbox.isIndeterminate());

        UI.getCurrent().add(checkbox);
        Assert.assertFalse(checkbox.isIndeterminate());
    }

    @Test(expected = BindingActiveException.class)
    public void bindIndeterminate_setWhileBound_throws() {
        checkbox.bindIndeterminate(signal, signal::set);
        UI.getCurrent().add(checkbox);

        checkbox.setIndeterminate(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindIndeterminate_doubleBind_throws() {
        checkbox.bindIndeterminate(signal, signal::set);
        var other = new ValueSignal<>(true);
        checkbox.bindIndeterminate(other, other::set);
    }

    @Test(expected = NullPointerException.class)
    public void bindIndeterminate_nullSignal_throwsNPE() {
        checkbox.bindIndeterminate(null, null);
    }
}
