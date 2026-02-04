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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class DatePickerSignalTest extends AbstractSignalsUnitTest {

    private DatePicker datePicker;
    private ValueSignal<LocalDate> signal;

    @Before
    public void setup() {
        datePicker = new DatePicker();
        signal = new ValueSignal<>(LocalDate.of(2023, 1, 1));
    }

    @Test
    public void bindMin_synchronizedWhenAttached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        Assert.assertEquals(signal.value(), datePicker.getMin());

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(signal.value(), datePicker.getMin());
    }

    @Test
    public void bindMin_internalFieldSynchronizedImmediately()
            throws Exception {
        datePicker.bindMin(signal);
        UI.getCurrent().add(datePicker);

        signal.value(LocalDate.of(2023, 2, 1));

        // Use reflection to check the private field 'min'
        java.lang.reflect.Field minField = DatePicker.class
                .getDeclaredField("min");
        minField.setAccessible(true);
        LocalDate internalMin = (LocalDate) minField.get(datePicker);

        Assert.assertEquals(LocalDate.of(2023, 2, 1), internalMin);
    }

    @Test
    public void bindMin_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        datePicker.removeFromParent();

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1), datePicker.getMin());

        UI.getCurrent().add(datePicker);
        Assert.assertEquals(LocalDate.of(2023, 2, 1), datePicker.getMin());
    }

    @Test
    public void bindMin_unbindWithNull() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        datePicker.bindMin(null);

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1), datePicker.getMin());

        datePicker.setMin(LocalDate.of(2023, 3, 1));
        Assert.assertEquals(LocalDate.of(2023, 3, 1), datePicker.getMin());
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_manualSetThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        datePicker.setMin(LocalDate.of(2023, 2, 1));
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_rebindingThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        datePicker.bindMin(new ValueSignal<>(LocalDate.of(2023, 2, 1)));
    }

    @Test
    public void bindMax_synchronizedWhenAttached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        Assert.assertEquals(signal.value(), datePicker.getMax());

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(signal.value(), datePicker.getMax());
    }

    @Test
    public void bindMax_internalFieldSynchronizedImmediately()
            throws Exception {
        datePicker.bindMax(signal);
        UI.getCurrent().add(datePicker);

        signal.value(LocalDate.of(2023, 2, 1));

        // Use reflection to check the private field 'max'
        java.lang.reflect.Field maxField = DatePicker.class
                .getDeclaredField("max");
        maxField.setAccessible(true);
        LocalDate internalMax = (LocalDate) maxField.get(datePicker);

        Assert.assertEquals(LocalDate.of(2023, 2, 1), internalMax);
    }

    @Test
    public void bindMax_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        datePicker.removeFromParent();

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1), datePicker.getMax());

        UI.getCurrent().add(datePicker);
        Assert.assertEquals(LocalDate.of(2023, 2, 1), datePicker.getMax());
    }

    @Test
    public void bindMax_unbindWithNull() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        datePicker.bindMax(null);

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1), datePicker.getMax());

        datePicker.setMax(LocalDate.of(2023, 3, 1));
        Assert.assertEquals(LocalDate.of(2023, 3, 1), datePicker.getMax());
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_manualSetThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        datePicker.setMax(LocalDate.of(2023, 2, 1));
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_rebindingThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        datePicker.bindMax(new ValueSignal<>(LocalDate.of(2023, 2, 1)));
    }

    @Test
    public void bindInitialPosition_synchronizedWhenAttached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        Assert.assertEquals(signal.value(), datePicker.getInitialPosition());

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(signal.value(), datePicker.getInitialPosition());
    }

    @Test
    public void bindInitialPosition_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        datePicker.removeFromParent();

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1),
                datePicker.getInitialPosition());

        UI.getCurrent().add(datePicker);
        Assert.assertEquals(LocalDate.of(2023, 2, 1),
                datePicker.getInitialPosition());
    }

    @Test
    public void bindInitialPosition_unbindWithNull() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        datePicker.bindInitialPosition(null);

        signal.value(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1),
                datePicker.getInitialPosition());

        datePicker.setInitialPosition(LocalDate.of(2023, 3, 1));
        Assert.assertEquals(LocalDate.of(2023, 3, 1),
                datePicker.getInitialPosition());
    }

    @Test(expected = BindingActiveException.class)
    public void bindInitialPosition_manualSetThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        datePicker.setInitialPosition(LocalDate.of(2023, 2, 1));
    }

    @Test(expected = BindingActiveException.class)
    public void bindInitialPosition_rebindingThrows() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        datePicker.bindInitialPosition(
                new ValueSignal<>(LocalDate.of(2023, 2, 1)));
    }
}
