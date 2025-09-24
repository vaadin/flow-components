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
package com.vaadin.flow.component.checkbox.testbench;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-checkbox-group")
public class CheckboxGroupElement extends TestBenchElement
        implements HasHelper, HasValidation {

    /**
     * Gets the labels of checkboxes that are part of this group.
     *
     * @return a list of the labels
     */
    public List<String> getOptions() {
        return getCheckboxes().stream().map(CheckboxElement::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * Gets all checkboxes that are part of this group.
     *
     * @return a list of the checkboxes
     */
    public List<CheckboxElement> getCheckboxes() {
        return $(CheckboxElement.class).all();
    }

    /**
     * Attempts to select a checkbox by matching the label. If it is selected
     * does nothing.
     *
     * @throws NoSuchElementException
     *             if no matching label was found
     * @param label
     *            the label of the checkbox to select
     */
    public void selectByText(String label) {
        Optional<CheckboxElement> checkbox = getCheckboxByLabel(label);
        if (!checkbox.isPresent()) {
            throw new NoSuchElementException(
                    "No checkbox with label '" + label + "' found");
        }

        checkbox.get().setChecked(true);
    }

    /**
     * Attempts to deselect a checkbox that is currently selected, by matching
     * the label. Does nothing if the checkbox is not selected.
     *
     * @throws NoSuchElementException
     *             if no matching label was found
     * @param label
     *            the label of the checkbox to select
     */
    public void deselectByText(String label) {
        Optional<CheckboxElement> checkbox = getCheckboxByLabel(label);
        if (!checkbox.isPresent()) {
            throw new NoSuchElementException(
                    "No checkbox with label '" + label + "' found");
        }

        checkbox.get().setChecked(false);
    }

    /**
     * Gets the labels of the currently selected checkboxes.
     *
     * @return the labels of the currently selected checkboxes
     */
    public List<String> getSelectedTexts() {
        Stream<CheckboxElement> button = getSelectedCheckboxes();
        return button.map(CheckboxElement::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * Gets the selected checkboxes.
     *
     * @return a stream with the selected checkboxes elements
     */
    private Stream<CheckboxElement> getSelectedCheckboxes() {
        return getCheckboxByChecked(true);
    }

    /**
     * Gets the checkbox which is part of this group and has the given label.
     *
     * @param label
     *            the label to search for
     * @return an optional with the checkbox element or an empty optional if no
     *         match was found
     */
    private Optional<CheckboxElement> getCheckboxByLabel(String label) {
        if (label == null) {
            return Optional.empty();
        }
        return getCheckboxes().stream()
                .filter(checkbox -> label.equals(checkbox.getLabel()))
                .findFirst();
    }

    /**
     * Gets the stream of checkboxes which are part of this group and has the
     * checked state.
     *
     * @param value
     *            the value to search for
     * @return a stream with the checkbox elements
     */
    private Stream<CheckboxElement> getCheckboxByChecked(boolean value) {
        return getCheckboxes().stream()
                .filter(checkbox -> checkbox.isChecked() == value);
    }

    /**
     * Gets the slotted error message component for the element.
     *
     * @return the slotted component or {@code null} if there is no component
     */
    public TestBenchElement getErrorMessageComponent() {
        return $("div").withAttribute("slot", "error-message").first();
    }
}
