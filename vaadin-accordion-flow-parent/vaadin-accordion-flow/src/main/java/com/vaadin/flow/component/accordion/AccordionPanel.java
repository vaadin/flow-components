/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.6.1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/accordion", version = "23.6.1")
@NpmPackage(value = "@vaadin/vaadin-accordion", version = "23.6.1")
@JsModule("@vaadin/accordion/src/vaadin-accordion-panel.js")
public class AccordionPanel extends Details {

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
     *            the content components to set.
     *
     * @see #setSummaryText(String)
     * @see #addContent(Component...)
     */
    public AccordionPanel(String summary, Component... components) {
        this(summary);
        addContent(components);
    }

    /**
     * Initializes a new panel using the provided summary and content
     * components.
     *
     * @param summary
     *            the summary component to set.
     * @param components
     *            the content components to set.
     *
     * @see #setSummary(Component)
     * @see #addContent(Component...)
     */
    public AccordionPanel(Component summary, Component... components) {
        this(summary);
        addContent(components);
    }
}
