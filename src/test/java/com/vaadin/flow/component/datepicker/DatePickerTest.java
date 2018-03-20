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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class DatePickerTest {

    private static final String OPENED_PROPERTY_NOT_UPDATED = "The server-side \"opened\"-property was not updated synchronously";

    @Test
    public void defaultCtor_valueIsSetImplicitely() {
        AtomicInteger valueSetCount = new AtomicInteger();
        DatePicker picker = new DatePicker() {
            @Override
            protected void setValueAsString(String valueAsString) {
                valueSetCount.incrementAndGet();
                super.setValueAsString(valueAsString);
            }
        };

        assertEquals(1, valueSetCount.get());
        assertNull(picker.getValue());
        assertEquals("", picker.getValueAsStringString());
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

}
