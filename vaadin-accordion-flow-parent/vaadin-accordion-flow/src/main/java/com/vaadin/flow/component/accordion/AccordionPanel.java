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
package com.vaadin.flow.component.accordion;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.details.Details;

/**
 * An accordion panel which could be opened or closed.
 */
@Tag("vaadin-accordion-panel")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/accordion", version = "24.8.0-alpha18")
@JsModule("@vaadin/accordion/src/vaadin-accordion-panel.js")
public class AccordionPanel extends Details {

    /**
     * Server-side component for the {@code <vaadin-accordion-heading>} element.
     */
    @Tag("vaadin-accordion-heading")
    static class AccordionHeading extends Component {

        public AccordionHeading() {
        }
    }

    /**
     * Creates an empty panel.
     */
    public AccordionPanel() {
    }

    /**
     * Initializes a new panel using the provided summary.
     *
     * @param summary
     *            the summary component to set.
     * @see #setSummaryText(String)
     */
    public AccordionPanel(String summary) {
        this();
        setSummaryText(summary);
    }

    /**
     * Initializes a new panel using the provided summary.
     *
     * @param summary
     *            the summary component to set.
     * @see #setSummary(Component)
     */
    public AccordionPanel(Component summary) {
        this();
        setSummary(summary);
    }

    /**
     * Creates a panel with the provided summary text and content.
     *
     * @param summary
     *            the summary. Null is treated like an empty string.
     * @param content
     *            the content. If null no content is added.
     */
    public AccordionPanel(String summary, Component content) {
        super(summary, content);
    }

    /**
     * Creates a panel with the provided summary component and content.
     *
     * @param summary
     *            the summary. Null clears any existing summary.
     * @param content
     *            the content. If null no content is added.
     */
    public AccordionPanel(Component summary, Component content) {
        super(summary, content);
    }

    /**
     * Initializes a new panel using the provided summary and content
     * components.
     *
     * @param summary
     *            the summary text to set.
     * @param components
     *            the content components to add.
     *
     * @see #setSummaryText(String)
     * @see #add(Component...)
     */
    public AccordionPanel(String summary, Component... components) {
        this(summary);
        add(components);
    }

    /**
     * Initializes a new panel using the provided summary and content
     * components.
     *
     * @param summary
     *            the summary component to set.
     * @param components
     *            the content components to add.
     *
     * @see #setSummary(Component)
     * @see #add(Component...)
     */
    public AccordionPanel(Component summary, Component... components) {
        this(summary);
        add(components);
    }

    /**
     * Creates the summary container component.
     *
     * @return the summary container
     */
    protected Component createSummaryContainer() {
        return new AccordionHeading();
    }
}
