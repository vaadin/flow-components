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
package com.vaadin.flow.component.timepicker.testbench;

import java.util.Objects;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasSelectByText;
import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

/**
 * A TestBench element representing a <code>&lt;vaadin-time-picker&gt;</code>
 * element.
 */
@Element("vaadin-time-picker")
public class TimePickerElement extends TestBenchElement
        implements HasStringValueProperty, HasSelectByText, HasHelper {

    /**
     * A TestBench element representing
     * <code>&lt;vaadin-time-picker-combo-box&gt;</code> element inside the
     * <code>&lt;vaadin-time-picker&gt;</code> element.
     */
    @Element("vaadin-time-picker-combo-box")
    public static class TimePickerComboBoxElement extends TestBenchElement {

    }

    /**
     * A TestBench element representing
     * <code>&lt;vaadin-time-picker-overlay&gt;</code> element that contains the
     * items for the <code>&lt;vaadin-time-picker&gt;</code> element when the
     * drop down has been opened with {@link #openDropDown()}.
     */
    @Element("vaadin-time-picker-overlay")
    public static class TimePickerOverlayElement extends TestBenchElement {

        /**
         * Gets the item at the given index from the opened drop down for the
         * <code>&lt;vaadin-time-picker&gt;</code> element.
         *
         * @param index
         *            the index of the item
         * @return the item element
         */
        public TestBenchElement getItem(int index) {
            return $("vaadin-time-picker-item").all().stream()
                    .filter(item -> index == item.getPropertyInteger("index"))
                    .findFirst().get();
        }

        /**
         * Gets the last item from the opened drop down for the
         * <code>&lt;vaadin-time-picker&gt;</code> element.
         *
         * @return the last item element
         */
        public TestBenchElement getLastItem() {
            return $("vaadin-time-picker-item").all().stream()
                    .max((a, b) -> a.getPropertyInteger("index")
                            - b.getPropertyInteger("index"))
                    .get();
        }
    }

    /**
     * Gets the <code>&lt;vaadin-time-picker-combo-box&gt;</code> element inside
     * the <code>&lt;vaadin-time-picker&gt;</code> element.
     *
     * @return the combo box light element
     */
    public TimePickerComboBoxElement getTimePickerComboBox() {
        return $(TimePickerComboBoxElement.class).first();
    }

    /**
     * Gets the <code>&lt;input&gt;</code> element inside the
     * <code>&lt;vaadin-time-picker&gt;</code> element.
     *
     * @return the combo box light element
     */
    public TestBenchElement getTimePickerInputElement() {
        return $("input").first();
    }

    @Override
    public String getText() {
        // The default implementation seems to use innerText, which adds a lot
        // of whitespace in Edge
        return getPropertyString("textContent");
    }

    /**
     * Gets the text content for the item inside the drop down with the given
     * index.
     * <p>
     * <em>NOTE:</em> the time picker drop down should be opened with
     * {@link #openDropDown()} first.
     *
     * @param index
     *            the index of the item
     * @return the text content for the item
     */
    public String getItemText(int index) {
        return $(TimePickerOverlayElement.class).onPage().first().getItem(index)
                .getText();
    }

    /**
     * Gets the text content for the last item inside the drop down.
     * <p>
     * <em>NOTE:</em> the time picker drop down should be opened with
     * {@link #openDropDown()} first.
     *
     * @return the text content for the last item
     */
    public String getLastItemText() {
        return $(TimePickerOverlayElement.class).onPage().first().getLastItem()
                .getText();
    }

    /**
     * Gets the value property for the text field of the time picker.
     * <p>
     * <em>NOTE:</em> this is not the same as the value property for the time
     * picker, returned by {@link #getValue()}.
     *
     * @return the value of the text field inside the time picker
     */
    public String getTimePickerInputValue() {
        return getTimePickerInputElement().getPropertyString("value");
    }

    /**
     * Opens the drop down for the time picker.
     */
    public void openDropDown() {
        executeScript("arguments[0].open()", getTimePickerComboBox());
        waitUntilDropDownOpen();
    }

    public void waitUntilDropDownOpen() {
        $(TimePickerOverlayElement.class).onPage().waitForFirst();
    }

    /**
     * Closes the drop down for the time picker.
     */
    public void closeDropDown() {
        executeScript(
                "const cb = arguments[0]; window.requestAnimationFrame(function(){ cb.close(); });",
                getTimePickerComboBox());
        waitUntil(input -> input
                .findElements(By.tagName("vaadin-time-picker-overlay"))
                .isEmpty());
    }

    /**
     * Scrolls to the item with the given index in the time picker drop down.
     * <p>
     * <em>NOTE:</em> the drop down must be opened before scrolling, e.g. use
     * {@link #openDropDown()}.
     *
     * @param index
     *            the index of the item to scroll to
     */
    public void scrollToItem(int index) {
        executeScript(
                "arguments[0].$.dropdown._scroller.scrollIntoView(arguments[1])",
                getTimePickerComboBox(), index);
    }

    /**
     * Selects the item with the given index by clicking on the item from the
     * combo box drop down.
     *
     * @param index
     *            the index of the item to select
     */
    public void selectItemByIndex(int index) {
        openDropDown();
        scrollToItem(index);

        TestBenchElement item = $(TimePickerOverlayElement.class).onPage()
                .first().getItem(index);
        item.click();
    }

    /**
     * Enter the given time input to the text field.
     *
     * @param timeInput
     *            the time input to enter, not {@code null}
     */
    @Override
    public void selectByText(String timeInput) {
        Objects.requireNonNull(timeInput, "null input not accepted");

        TestBenchElement timePickerInputElement = getTimePickerInputElement();
        executeScript("arguments[0].value = ''", timePickerInputElement);
        timePickerInputElement.sendKeys(timeInput + Keys.RETURN);
    }

    @Override
    public String getSelectedText() {
        return getTimePickerInputElement().getPropertyString("value");
    }

    /**
     * Gets whether dropdown will open automatically or not.
     *
     * @return @{code true} if enabled, {@code false} otherwise
     */
    public boolean isAutoOpen() {
        return !getPropertyBoolean("autoOpenDisabled");
    }

}
