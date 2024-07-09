/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.testbench;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-checkbox-group")
public class CheckboxGroupElement extends TestBenchElement
        implements HasHelper {

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

}
