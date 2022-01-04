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

import com.vaadin.testbench.elementsbase.Element;
import com.vaadin.flow.component.details.testbench.DetailsElement;

import static com.vaadin.flow.component.accordion.testbench.AccordionElement.OPENED_PROPERTY;

/**
 * TestBench element for the vaadin-accordion-panel element
 */
@Element("vaadin-accordion-panel")
public class AccordionPanelElement extends DetailsElement {

    /**
     * Opens the panel.
     */
    public void open() {
        setProperty(OPENED_PROPERTY, true);
    }

    /**
     * Closes the panel.
     */
    public void close() {
        setProperty(OPENED_PROPERTY, (Boolean) null);
    }
}
