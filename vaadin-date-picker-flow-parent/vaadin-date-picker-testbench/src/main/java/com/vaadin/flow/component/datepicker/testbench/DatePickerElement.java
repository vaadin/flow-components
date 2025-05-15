/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.testbench.HasClearButton;
import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-date-picker&gt;</code>
 * element.
 */
@Element("vaadin-date-picker")
public class DatePickerElement extends TestBenchElement
        implements HasLabel, HasHelper, HasClearButton, HasValidation {

    @Element("vaadin-date-picker-overlay-content")
    public static class OverlayContentElement extends TestBenchElement {
        /**
         * Gets all visible month calendars that are currently rendered by the
         * infinite scroller in the overlay.
         *
         * @return
         */
        public List<MonthCalendarElement> getVisibleMonthCalendars() {
            return this.$("vaadin-month-calendar").all().stream()
                    .map(el -> el.wrap(MonthCalendarElement.class))
                    .collect(Collectors.toList());
        }

        /**
         * Gets the today button from the overlays toolbar
         *
         * @return
         */
        public ButtonElement getTodayButton() {
            return this.$(ButtonElement.class)
                    .withAttribute("slot", "today-button").first();
        }

        /**
         * Gets the cancel button from the overlays toolbar
         *
         * @return
         */
        public ButtonElement getCancelButton() {
            return this.$(ButtonElement.class)
                    .withAttribute("slot", "cancel-button").first();
        }
    }

    public static class MonthCalendarElement extends TestBenchElement {
        /**
         * Gets the header text of the month calendar, e.g. `January 1999`
         *
         * @return
         */
        public String getHeaderText() {
            return this.$(TestBenchElement.class)
                    .withAttribute("part", "month-header").first().getText();
        }

        /**
         * Gets the weekday headers that are rendered by the month calendar
         *
         * @return
         */
        public List<WeekdayElement> getWeekdays() {
            return this.$(WeekdayElement.class).withAttribute("part", "weekday")
                    .all();
        }
    }

    public static class WeekdayElement extends TestBenchElement {
    }

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

    /**
     * Simulates the user selecting a date via the input element. This
     * effectively clears the input element with a key shortcut, then types the
     * given date string and finally presses {@code Enter} to commit the new
     * date.
     *
     * @param value
     *            the date string to enter.
     */
    public void setInputValue(String value) {
        sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        sendKeys(value, Keys.ENTER);
    }

    /**
     * Gets the visible presentation value from the inner input element as a
     * string. This value depends on the used Locale.
     *
     * @return
     */
    public String getInputValue() {
        TestBenchElement input = $("input").first();
        return input.getPropertyString("value");
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

    /**
     * Opens the date picker overlay
     */
    public void open() {
        executeScript("arguments[0].open();", this);
    }

    /**
     * Closes the date picker overlay
     */
    public void close() {
        executeScript("arguments[0].close();", this);
    }

    /**
     * Gets the content of the first date picker overlay on the page Should only
     * be used with a single date picker at a time, there is no check that the
     * overlay belongs to this specific date picker
     *
     * @return
     */
    public OverlayContentElement getOverlayContent() {
        return this.$("vaadin-date-picker-overlay").onPage().waitForFirst()
                .$(OverlayContentElement.class).first();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        findElement(By.tagName("input")).sendKeys(keysToSend);
    }
}
