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
package com.vaadin.flow.component.checkbox.testbench;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-checkbox-group")
public class CheckboxGroupElement extends TestBenchElement
        implements HasHelper {

    /**
     * Gets a list of all available options.
     *
     * @return a list of the options (visible text)
     */
    public List<String> getOptions() {
        return getCheckboxes().stream().map(CheckboxElement::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * Gets all checkboxes which are part of this group.
     *
     * @return a list of all checkboxes in this group
     */
    public List<CheckboxElement> getCheckboxes() {
        return $(CheckboxElement.class).all();
    }

    /**
     * Selects the first checkbox matching the given text. If it is selected
     * does nothing.
     *
     * @param text
     *            the text of the option to select
     */
    public void selectByText(String text) {
        Optional<CheckboxElement> checkbox = getCheckboxByText(text);
        if (!checkbox.isPresent()) {
            throw new NoSuchElementException(
                    "No item with text '" + text + "' found");
        }

        checkbox.get().setChecked(true);
    }

    /**
     * Unselects the first checkbox matching the given text. If it is unslected
     * does nothing.
     *
     * @param text
     *            the text of the option to select
     */
    public void unselectByText(String text) {
        Optional<CheckboxElement> checkbox = getCheckboxByText(text);
        if (!checkbox.isPresent()) {
            throw new NoSuchElementException(
                    "No item with text '" + text + "' found");
        }

        checkbox.get().setChecked(false);
    }

    public List<String> getSelectedTexts() {
        Stream<CheckboxElement> button = getSelectedCheckboxes();
        return button.map(CheckboxElement::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * Gets the selected checkboxes.
     *
     * @return an stream with the selected checkboxs elements.
     */
    private Stream<CheckboxElement> getSelectedCheckboxes() {
        return getCheckboxByChecked(true);
    }

    /**
     * Gets the checkbox which is part of this group and has the given visible
     * text.
     *
     * @param text
     *            the text to search for
     * @return an optional with the checkbox element or an empty optional if no
     *         match was found
     */
    private Optional<CheckboxElement> getCheckboxByText(String text) {
        if (text == null) {
            return Optional.empty();
        }
        return getCheckboxes().stream()
                .filter(checkbox -> text.equals(checkbox.getLabel()))
                .findFirst();
    }

    /**
     * Gets the stream of checkboxes which is part of this group and has the
     * given value.
     *
     * @param value
     *            the value to search for
     * @return a stream with the checkbox elements
     */
    private Stream<CheckboxElement> getCheckboxByChecked(boolean value) {
        return getCheckboxes().stream()
                .filter(checkbox -> checkbox.isChecked());
    }

    /**
     * Gets the slotted error message component for the element.
     *
     * @return the slotted component or {@code null} if there is no component
     */
    public TestBenchElement getErrorMessageComponent() {
        return $("div").attributeContains("slot", "error-message").first();
    }
}
