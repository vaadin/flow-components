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
package com.vaadin.flow.component.timepicker.testbench;

import java.util.Objects;

import org.openqa.selenium.Keys;

import com.vaadin.testbench.HasClearButton;
import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasSelectByText;
import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-time-picker&gt;</code>
 * element.
 */
@Element("vaadin-time-picker")
public class TimePickerElement extends TestBenchElement
        implements HasStringValueProperty, HasSelectByText, HasHelper,
        HasClearButton, HasValidation {
    /**
     * Gets the <code>&lt;input&gt;</code> element inside the
     * <code>&lt;vaadin-time-picker&gt;</code> element.
     *
     * @return the input element
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
     * Gets the item at the given index from the drop down.
     * <p>
     * <em>NOTE:</em> the time picker drop down should be opened with
     * {@link #openDropDown()} first.
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
     * Gets the last item inside the drop down.
     * <p>
     * <em>NOTE:</em> the time picker drop down should be opened with
     * {@link #openDropDown()} first.
     *
     * @return the last item element
     */
    public TestBenchElement getLastItem() {
        return $("vaadin-time-picker-item").all().stream()
                .max((a, b) -> a.getPropertyInteger("index")
                        - b.getPropertyInteger("index"))
                .get();
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
        return getItem(index).getText();
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
        return getLastItem().getText();
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
        callFunction("open");
    }

    /**
     * Closes the drop down for the time picker.
     */
    public void closeDropDown() {
        executeScript(
                "const cb = arguments[0]; window.requestAnimationFrame(function(){ cb.close(); });",
                this);
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
        executeScript("arguments[0]._scroller.scrollIntoView(arguments[1])",
                this, index);
    }

    /**
     * Selects the item with the given index by clicking on the item from the
     * overlay drop down.
     *
     * @param index
     *            the index of the item to select
     */
    public void selectItemByIndex(int index) {
        openDropDown();
        scrollToItem(index);

        TestBenchElement item = getItem(index);
        item.click();
    }

    /**
     * Simulates the user selecting a time via the input element. This
     * effectively clears the input element with a key shortcut, then types the
     * given time string and finally presses {@code Enter} to commit the new
     * time.
     *
     * @param timeInput
     *            the time string to enter, not {@code null}
     */
    @Override
    public void selectByText(String timeInput) {
        Objects.requireNonNull(timeInput, "null input not accepted");
        sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        sendKeys(timeInput, Keys.ENTER);
    }

    @Override
    public String getSelectedText() {
        return getTimePickerInputElement().getPropertyString("value");
    }

    /**
     * Gets whether dropdown will open automatically or not.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isAutoOpen() {
        return !getPropertyBoolean("autoOpenDisabled");
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        getTimePickerInputElement().sendKeys(keysToSend);
    }
}
