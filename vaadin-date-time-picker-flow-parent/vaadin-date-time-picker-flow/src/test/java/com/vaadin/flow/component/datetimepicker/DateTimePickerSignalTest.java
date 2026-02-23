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
package com.vaadin.flow.component.datetimepicker;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class DateTimePickerSignalTest extends AbstractSignalsUnitTest {

    private DateTimePicker dateTimePicker;
    private ValueSignal<LocalDateTime> signal;

    @Before
    public void setup() {
        dateTimePicker = new DateTimePicker();
        signal = new ValueSignal<>(LocalDateTime.of(2023, 10, 1, 10, 0));
    }

    @Test
    public void bindMin_elementAttached_updatesWithSignal() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMin(signal);

        Assert.assertEquals(signal.peek(), dateTimePicker.getMin());
        Assert.assertEquals(signal.peek().toString(),
                dateTimePicker.getElement().getProperty("min"));

        LocalDateTime newValue = LocalDateTime.of(2023, 10, 2, 11, 0);
        signal.set(newValue);
        Assert.assertEquals(newValue, dateTimePicker.getMin());
        Assert.assertEquals(newValue.toString(),
                dateTimePicker.getElement().getProperty("min"));
    }

    @Test
    public void bindMin_elementNotAttached_bindingInactive_untilAttach() {
        dateTimePicker.bindMin(signal);

        Assert.assertNull(dateTimePicker.getMin());

        UI.getCurrent().add(dateTimePicker);
        Assert.assertEquals(signal.peek(), dateTimePicker.getMin());
    }

    @Test(expected = BindingActiveException.class)
    public void setMin_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMin(signal);
        dateTimePicker.setMin(LocalDateTime.now());
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMin(signal);
        dateTimePicker.bindMin(new ValueSignal<>(LocalDateTime.now()));
    }

    @Test
    public void bindMax_elementAttached_updatesWithSignal() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMax(signal);

        Assert.assertEquals(signal.peek(), dateTimePicker.getMax());
        Assert.assertEquals(signal.peek().toString(),
                dateTimePicker.getElement().getProperty("max"));

        LocalDateTime newValue = LocalDateTime.of(2023, 10, 2, 11, 0);
        signal.set(newValue);
        Assert.assertEquals(newValue, dateTimePicker.getMax());
        Assert.assertEquals(newValue.toString(),
                dateTimePicker.getElement().getProperty("max"));
    }

    @Test
    public void bindMax_elementNotAttached_bindingInactive_untilAttach() {
        dateTimePicker.bindMax(signal);

        Assert.assertNull(dateTimePicker.getMax());

        UI.getCurrent().add(dateTimePicker);
        Assert.assertEquals(signal.peek(), dateTimePicker.getMax());
    }

    @Test(expected = BindingActiveException.class)
    public void setMax_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMax(signal);
        dateTimePicker.setMax(LocalDateTime.now());
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_whileBound_throwsException() {
        UI.getCurrent().add(dateTimePicker);
        dateTimePicker.bindMax(signal);
        dateTimePicker.bindMax(new ValueSignal<>(LocalDateTime.now()));
    }
}
