/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.datepicker.testbench;

import java.time.LocalDate;

import com.vaadin.flow.component.common.testbench.HasLabel;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-date-picker&gt;</code>
 * element.
 */
@Element("vaadin-date-picker")
public class DatePickerElement extends TestBenchElement implements HasLabel {

    /**
     * Clears the value of the date picker.
     */
    @Override
    public void clear() {
        setDate(null);
    }

    /**
     * Selects the given date.
     *
     * @param date
     *            the date to set
     */
    public void setDate(LocalDate date) {
        if (date == null) {
            setValue("");
        } else {
            setValue(date.toString());
        }
    }

    /**
     * Gets the selected date
     *
     * @return the selected date or <code>null</code> if no date is selected
     */
    public LocalDate getDate() {
        String value = getValue();
        if (value.isEmpty()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    /**
     * Sets the selected date as a string.
     * <p>
     * The value is always in format <code>YYYY-MM-DD</code>.
     *
     * @param value
     *            the value to set
     */
    protected void setValue(String value) {
        setProperty("value", value);
    }

    /**
     * Gets the selected date as a string.
     * <p>
     * The value is always in format <code>YYYY-MM-DD</code>.
     *
     * @return the value of the date picker or an empty string if no date is
     *         selected
     */
    protected String getValue() {
        return getPropertyString("value");
    }
}
