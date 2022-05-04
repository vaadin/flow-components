package com.vaadin.flow.component.accordion;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

/**
 * Accordion is a vertically stacked set of expandable panels. It reduces
 * clutter and helps maintain the user’s focus by showing only the relevant
 * content at a time.
 * <p>
 * Accordion consists of stacked panels, each composed of two parts: a summary
 * and a content area. Only one panel can be expanded at a time.
 * <p>
 * The summary is the part that is always visible, and typically describes the
 * contents, for example, with a title. Clicking on the summary toggles the
 * content area’s visibility.
 * <p>
 * The content area is the collapsible part of a panel. It can contain any
 * component. When the content area is collapsed, the content is invisible and
 * inaccessible by keyboard or screen reader.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-accordion")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/accordion", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-accordion", version = "23.1.0-beta1")
@JsModule("@vaadin/accordion/src/vaadin-accordion.js")
public class Accordion extends Component implements HasSize, HasStyle {

    private static final String OPENED_PROPERTY = "opened";
    private static final String OPENED_CHANGED_DOM_EVENT = "opened-changed";

    /**
     * Adds a panel created from the given title and content.
     *
     * @param summary
     *            the title of the panel
     * @param content
     *            the content of th panel
     * @return the panel created and added
     */
    public AccordionPanel add(String summary, Component content) {
        final AccordionPanel panel = new AccordionPanel(summary, content);
        return add(panel);
    }

    /**
     * Adds a panel.
     *
     * @param panel
     *            the non-null panel to be added
     * @return the added panel
     */
    public AccordionPanel add(AccordionPanel panel) {
        Objects.requireNonNull(panel, "The panel to be added cannot be null");
        getElement().appendChild(panel.getElement());
        return panel;
    }

    /**
     * Removes a panel.
     *
     * @param panel
     *            the non-null panel to be removed
     */
    public void remove(AccordionPanel panel) {
        Objects.requireNonNull(panel, "The panel to be removed cannot be null");
        getElement().removeChild(panel.getElement());
    }

    /**
     * Removes a panel based on the content
     *
     * @param content
     *            the non-null content of the panel to be removed
     */
    public void remove(Component content) {
        Objects.requireNonNull(content,
                "The content of the panel to be removed cannot be null");

        if (content instanceof AccordionPanel) {
            remove((AccordionPanel) content);
            return;
        }

        if (content.getParent().isPresent()) {
            final Optional<Component> grandParent = content.getParent().get()
                    .getParent();
            if (grandParent.isPresent()
                    && grandParent.get() instanceof AccordionPanel) {
                remove((AccordionPanel) grandParent.get());
                return;
            }
        }

        throw new IllegalArgumentException(
                "The supplied content is not a descendant of this Accordion. "
                        + "It can be added with the accordion.add.");
    }

    /**
     * Closes the opened panel (if any) in this accordion.
     */
    public void close() {
        getElement().setProperty(OPENED_PROPERTY, null);
    }

    /**
     * Opens the panel at the specified index.
     *
     * @param index
     *            the (positive) index of the panel to be open. The first panel
     *            is at index zero.
     */
    public void open(int index) {
        if (index < 0) {
            throw new IllegalArgumentException(
                    "The index to open cannot be negative");
        }

        getElement().setProperty(OPENED_PROPERTY, index);
    }

    /**
     * Opens the specified panel.
     *
     * @param panel
     *            the non-null panel to be opened
     */
    public void open(AccordionPanel panel) {
        Objects.requireNonNull(panel, "The panel to be opened cannot be null");
        open(getElement().indexOfChild(panel.getElement()));
    }

    /**
     * Gets the index of the currently opened index.
     *
     * @return the index of the opened panel or null if the accordion is closed.
     */
    @Synchronize(property = OPENED_PROPERTY, value = OPENED_CHANGED_DOM_EVENT)
    public OptionalInt getOpenedIndex() {
        final String opened = getElement().getProperty(OPENED_PROPERTY);
        return opened == null ? OptionalInt.empty()
                : OptionalInt.of(Integer.valueOf(opened));
    }

    /**
     * Gets the opened panel.
     *
     * Caution should be exercised when using this method with an Accordion
     * which along with its panels were created in a template. Such template
     * children would by default not be children of the Accordion Flow
     * component, thus making it possible for this method to return the wrong
     * panel in such cases.
     *
     * @return the opened panel.
     */
    @Synchronize(property = OPENED_PROPERTY, value = OPENED_CHANGED_DOM_EVENT)
    public Optional<AccordionPanel> getOpenedPanel() {
        final OptionalInt optionalOpenedIndex = getOpenedIndex();

        if (!optionalOpenedIndex.isPresent()) {
            return Optional.empty();
        }

        int index = optionalOpenedIndex.getAsInt();
        return Accordion.getOpenedPanel(this, index);
    }

    private static Optional<AccordionPanel> getOpenedPanel(Accordion accordion,
            Integer index) {
        return index == null || index >= accordion.getChildren().count()
                ? Optional.empty()
                : accordion.getElement().getChild(index).getComponent()
                        .map(AccordionPanel.class::cast);
    }

    /**
     * Registers a listener to be notified whenever a panel is opened or closed.
     *
     * @param listener
     *            the listener to be notified
     * @return a handle to the registered listener which could also be used to
     *         unregister it.
     */
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent> listener) {

        return ComponentUtil.addListener(this, OpenedChangeEvent.class,
                listener);
    }

    /**
     * An event fired when an Accordion is opened or closed.
     */
    @DomEvent(OPENED_CHANGED_DOM_EVENT)
    public static class OpenedChangeEvent extends ComponentEvent<Accordion> {

        private final Integer index;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param index
         *            the index of the opened panel or null if the accordion is
         *            closed
         */
        public OpenedChangeEvent(Accordion source, boolean fromClient,
                @EventData("event.detail.value") Integer index) {
            super(source, fromClient);
            this.index = index;
        }

        /**
         * Gets the index of the opened panel or null if the accordion is
         * closed.
         *
         * @return the index of the opened panel or null if closed
         */
        public OptionalInt getOpenedIndex() {
            return index == null ? OptionalInt.empty() : OptionalInt.of(index);
        }

        /**
         * Gets the opened panel.
         *
         * Caution should be exercised when using this method with an Accordion
         * which along with its panels were created in a template. Such template
         * children would by default not be children of the Accordion Flow
         * component, thus making it possible for this method to return the
         * wrong panel in such cases.
         *
         * @return the opened panel.
         */
        public Optional<AccordionPanel> getOpenedPanel() {
            return Accordion.getOpenedPanel(getSource(), index);
        }
    }
}
