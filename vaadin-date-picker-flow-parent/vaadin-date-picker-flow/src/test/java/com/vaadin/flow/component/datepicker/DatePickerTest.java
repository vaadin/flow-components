/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NotThreadSafe
public class DatePickerTest {

    private static final String OPENED_PROPERTY_NOT_UPDATED = "The server-side \"opened\"-property was not updated synchronously";

    private UI ui;

    @Before
    public void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void datePicker_basicCases() {
        DatePicker picker = new DatePicker();

        assertEquals(null, picker.getValue());
        assertFalse(picker.getElement().hasProperty("value"));

        picker.setValue(LocalDate.of(2018, 4, 25));
        assertEquals("2018-04-25", picker.getElement().getProperty("value"));

        picker.getElement().setProperty("value", "2017-03-24");
        assertEquals(LocalDate.of(2017, 3, 24), picker.getValue());

        // Cannot do removeProperty because
        // https://github.com/vaadin/flow/issues/3994
        picker.getElement().setProperty("value", null);
        assertEquals(null, picker.getValue());
    }

    @Test
    public void defaultCtor_does_not_update_values() {
        DatePicker picker = new DatePicker();
        assertNull(picker.getValue());
        assertEquals(null, picker.getElement().getProperty("value"));
    }

    @Test
    public void setInitialValue() {
        DatePicker picker = new DatePicker(LocalDate.of(2018, 4, 25));
        assertEquals(LocalDate.of(2018, 4, 25), picker.getValue());
        assertEquals("2018-04-25", picker.getElement().getProperty("value"));
    }

    @Test
    public void updatingToNullValue_displaysEmptyString() {
        DatePicker picker = new DatePicker();

        picker.setValue(LocalDate.now());
        picker.setValue(null);

        assertNull(picker.getValue());
        assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void setOpened_openedPropertyIsUpdated() {
        DatePicker picker = new DatePicker();
        assertFalse("Initially DatePicker should be closed", picker.isOpened());
        picker.setOpened(true);
        assertTrue(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
        picker.setOpened(false);
        assertFalse(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
    }

    @Test
    public void openAndClose_openedPropertyIsUpdated() {
        DatePicker picker = new DatePicker();
        assertFalse("Initially DatePicker should be closed", picker.isOpened());
        picker.open();
        assertTrue(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
        picker.close();
        assertFalse(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        DatePicker picker = new DatePicker();

        assertFalse("Clear button should not be visible by default",
                picker.isClearButtonVisible());
        assertClearButtonPropertyValueEquals(picker, true);
        assertClearButtonPropertyValueEquals(picker, false);
    }

    public void assertClearButtonPropertyValueEquals(DatePicker picker,
            Boolean value) {
        picker.setClearButtonVisible(value);
        assertEquals(value, picker.isClearButtonVisible());
        assertEquals(picker.isClearButtonVisible(),
                picker.getElement().getProperty("clearButtonVisible", value));
    }

}
