/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "22.1.0")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
public class AccordionPanel extends Details {

    /**
     * Creates an empty panel.
     */
    public AccordionPanel() {
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
}
