package com.vaadin.flow.component.accordion.testbench;

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

import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * TestBench element for the vaadin-accordion element
 */
@Element("vaadin-accordion")
public class AccordionElement extends TestBenchElement {

    static final String OPENED_PROPERTY = "opened";

    /**
     * Closes the opened panel (if any) in the accordion.
     */
    public void close() {
        setProperty(OPENED_PROPERTY, (Boolean) null);
    }

    /**
     * Opens the panel at the specified index. The first panel is at index zero.
     *
     * @param index
     *            the index of the panel to be opened
     */
    public void open(int index) {
        setProperty(OPENED_PROPERTY, index);
    }

    /**
     * Gets the index of the opened panel or null if the accordion is closed.
     *
     * @return the index of the opened panel or null if closed.
     */
    public OptionalInt getOpenedIndex() {
        final String openedAttribute = getAttribute(OPENED_PROPERTY);
        return openedAttribute == null ? OptionalInt.empty()
                : OptionalInt.of(Integer.valueOf(openedAttribute));
    }

    /**
     * Gets the the opened panel or null if the accordion is closed.
     *
     * @return the opened panel or null if closed.
     */
    public Optional<AccordionPanelElement> getOpenedPanel() {
        final ElementQuery<AccordionPanelElement> openedPanels = $(
                AccordionPanelElement.class).attribute(OPENED_PROPERTY, "");

        return !openedPanels.exists() ? Optional.empty()
                : Optional.of(openedPanels.first());
    }
}
