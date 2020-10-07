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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class DatePickerTest {

    private static final String OPENED_PROPERTY_NOT_UPDATED = "The server-side \"opened\"-property was not updated synchronously";

    private static LocalDate TEST_VALUE = LocalDate.now();

    private static class TestDatePicker
            extends GeneratedVaadinDatePicker<TestDatePicker, LocalDate> {

        TestDatePicker() {
            super(TEST_VALUE, null, String.class, value -> null, value -> null,
                    true);
        }
    }

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

    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-date-picker");
        element.setProperty("value", "2007-12-03");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(TestDatePicker.class))
                .thenAnswer(invocation -> new TestDatePicker());

        TestDatePicker field = Component.from(element, TestDatePicker.class);
        Assert.assertEquals("2007-12-03",
                field.getElement().getPropertyRaw("value"));
    }

    public void assertClearButtonPropertyValueEquals(DatePicker picker,
            Boolean value) {
        picker.setClearButtonVisible(value);
        assertEquals(value, picker.isClearButtonVisible());
        assertEquals(picker.isClearButtonVisible(),
                picker.getElement().getProperty("clearButtonVisible", value));
    }

}
