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
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
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
        Assert.assertEquals(signal.peek(), datePicker.getMin());

        signal.set(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(signal.peek(), datePicker.getMin());
    }

    @Test
    public void bindMin_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMin(signal);
        datePicker.removeFromParent();

        signal.set(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1), datePicker.getMin());

        UI.getCurrent().add(datePicker);
        Assert.assertEquals(LocalDate.of(2023, 2, 1), datePicker.getMin());
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
        Assert.assertEquals(signal.peek(), datePicker.getMax());

        signal.set(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(signal.peek(), datePicker.getMax());
    }

    @Test
    public void bindMax_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindMax(signal);
        datePicker.removeFromParent();

        signal.set(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1), datePicker.getMax());

        UI.getCurrent().add(datePicker);
        Assert.assertEquals(LocalDate.of(2023, 2, 1), datePicker.getMax());
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
        Assert.assertEquals(signal.peek(), datePicker.getInitialPosition());

        signal.set(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(signal.peek(), datePicker.getInitialPosition());
    }

    @Test
    public void bindInitialPosition_noEffectWhenDetached() {
        UI.getCurrent().add(datePicker);
        datePicker.bindInitialPosition(signal);
        datePicker.removeFromParent();

        signal.set(LocalDate.of(2023, 2, 1));
        Assert.assertEquals(LocalDate.of(2023, 1, 1),
                datePicker.getInitialPosition());

        UI.getCurrent().add(datePicker);
        Assert.assertEquals(LocalDate.of(2023, 2, 1),
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
