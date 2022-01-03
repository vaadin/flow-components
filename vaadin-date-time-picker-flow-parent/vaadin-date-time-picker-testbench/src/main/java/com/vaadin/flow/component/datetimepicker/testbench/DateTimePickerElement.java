/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.datetimepicker.testbench;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-date-time-picker&gt;</code> element.
 */
@Element("vaadin-date-time-picker")
public class DateTimePickerElement extends TestBenchElement
        implements HasLabel, HasHelper {

    private static final String VALUE_PROPERTY = "value";

    /**
     * Clears the value of the date time picker.
     */
    @Override
    public void clear() {
        setDateTime(null);
    }

    /**
     * Selects the given date time.
     *
     * @param dateTime
     *            the date time to set
     */
    public void setDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            setValue("");
        } else {
            setValue(dateTime.toString());
        }
    }

    /**
     * Gets the selected date time
     *
     * @return the selected date time or <code>null</code> if no date time is
     *         selected
     */
    public LocalDateTime getDateTime() {
        String value = getValue();
        if (value.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    /**
     * Selects the given date.
     *
     * @param date
     *            the date to set
     */
    public void setDate(LocalDate date) {
        if (date == null) {
            setDateValue("");
        } else {
            setDateValue(date.toString());
        }
    }

    /**
     * Gets the selected date
     *
     * @return the selected date or <code>null</code> if no date is selected
     */
    public LocalDate getDate() {
        String value = getDateValue();
        if (value.isEmpty()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    /**
     * Selects the given time.
     *
     * @param time
     *            the time to set
     */
    public void setTime(LocalTime time) {
        if (time == null) {
            setTimeValue("");
        } else {
            // Time needs to be truncated to millisecond precision, otherwise
            // the web component will not update the value
            setTimeValue(time.truncatedTo(ChronoUnit.MILLIS).toString());
        }
    }

    /**
     * Gets the selected time
     *
     * @return the selected time or <code>null</code> if no time is selected
     */
    public LocalTime getTime() {
        String value = getTimeValue();
        if (value.isEmpty()) {
            return null;
        }
        return LocalTime.parse(value);
    }

    /**
     * Sets the selected date time as a string.
     * <p>
     * The value is based on ISO 8601 (without a time zone designator) e.g.
     * <code>YYYY-MM-DDThh:mm:ss</code>.
     *
     * @param value
     *            the value to set
     */
    private void setValue(String value) {
        setProperty(VALUE_PROPERTY, value);
    }

    /**
     * Gets the selected date time as a string.
     * <p>
     * The value is based on ISO 8601 (without a time zone designator) e.g.
     * <code>YYYY-MM-DDThh:mm:ss</code>.
     *
     * @return the value of the date time picker or an empty string if no date
     *         time is selected
     */
    private String getValue() {
        return getPropertyString(VALUE_PROPERTY);
    }

    /**
     * Sets the selected date of the inner DatePicker as a string.
     * <p>
     * The value is based on ISO 8601 e.g. <code>YYYY-MM-DD</code>.
     *
     * @param value
     *            the value to set
     */
    private void setDateValue(String value) {
        getDatePicker().setProperty(VALUE_PROPERTY, value);
        triggerChange(getDatePicker());
    }

    /**
     * Gets the selected date of the inner DatePicker as a string.
     * <p>
     * The value is based on ISO 8601 e.g. <code>YYYY-MM-DD</code>.
     *
     * @return the value of the inner date picker or an empty string if no date
     *         is selected
     */
    private String getDateValue() {
        return getDatePicker().getPropertyString(VALUE_PROPERTY);
    }

    /**
     * Sets the selected time of the inner TimePicker as a string.
     * <p>
     * The value is based on ISO 8601 e.g. <code>hh:mm:ss</code>.
     *
     * @param value
     *            the value to set
     */
    private void setTimeValue(String value) {
        getTimePicker().setProperty(VALUE_PROPERTY, value);
        triggerChange(getTimePicker());
    }

    /**
     * Gets the selected time of the inner TimePicker as a string.
     * <p>
     * The value is based on ISO 8601 e.g. <code>hh:mm:ss</code>.
     *
     * @return the value of the inner time picker or an empty string if no time
     *         is selected
     */
    private String getTimeValue() {
        return getTimePicker().getPropertyString(VALUE_PROPERTY);
    }

    /**
     * This is needed when simulating user input by explicitly setting the value
     * property of inner inputs.
     */
    private void triggerChange(TestBenchElement pickerElement) {
        executeScript(
                "arguments[0].dispatchEvent(new CustomEvent('change', { bubbles: true }));",
                pickerElement);
    }

    /**
     * Gets the visible presentation value from the inner DatePicker as a
     * string. This value depends on the used Locale.
     *
     * @return the presentation value of the inner date picker
     */
    public String getDatePresentation() {
        return getDatePicker().getPropertyString("_inputValue");
    }

    /**
     * Gets the visible presentation value from the inner TimePicker as a
     * string. This value depends on the used Locale.
     *
     * @return the presentation value of the inner time picker
     */
    public String getTimePresentation() {
        return getTimePicker().getPropertyString("inputElement",
                VALUE_PROPERTY);
    }

    /**
     * When auto open is enabled, the dropdown will open when the field is
     * clicked.
     *
     * @return {@code true} if auto open is enabled. {@code false} otherwise.
     *         Default is {@code true}
     */
    public boolean isAutoOpen() {
        return !getPropertyBoolean("autoOpenDisabled");
    }

    private TestBenchElement getDatePicker() {
        return $("vaadin-date-time-picker-date-picker")
                .attribute("slot", "date-picker").first();
    }

    private TestBenchElement getTimePicker() {
        return $("vaadin-date-time-picker-time-picker")
                .attribute("slot", "time-picker").first();
    }

    /**
     * {@inheritDoc}
     */
    // TODO: Remove once https://github.com/vaadin/testbench/issues/1299 is
    // fixed
    @Override
    public TestBenchElement getHelperComponent() {
        final ElementQuery<TestBenchElement> query = $(TestBenchElement.class)
                .attribute("slot", "helper");
        if (query.exists()) {
            TestBenchElement last = query.last();
            // To avoid getting the "slot" element, for components with slotted
            // slots
            if (!"slot".equals(last.getTagName())
                    && this.equals(last.getPropertyElement("parentElement"))) {
                return last;
            }
        }
        return null;
    }
}
