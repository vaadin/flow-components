/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.details;

import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;

/**
 * Details is an expandable panel for showing and hiding content from the user
 * to make the UI less crowded. Details consists of a summary and a content
 * area.
 * <p>
 * The Summary is the part that is always visible, and typically describes the
 * contents, for example, with a title. Clicking on the summary toggles the
 * content area’s visibility. The summary supports rich content and can contain
 * any component. This can be utilized for example to indicate the status of the
 * corresponding content.
 * <p>
 * The content area is the collapsible part of Details. It can contain any
 * component. When the content area is collapsed, the content is invisible and
 * inaccessible by keyboard or screen reader.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-details")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.1.13")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/details", version = "24.1.13")
@JsModule("@vaadin/details/src/vaadin-details.js")
public class Details extends Component implements HasEnabled, HasSize, HasStyle,
        HasThemeVariant<DetailsVariant>, HasTooltip {

    private Component summary;
    private Component summaryContainer;
    private final Div contentContainer;

    /**
     * Server-side component for the {@code <vaadin-details-summary>} element.
     */
    @Tag("vaadin-details-summary")
    static class DetailsSummary extends Component {

        public DetailsSummary() {
        }
    }

    /**
     * Initializes a new Details component.
     */
    public Details() {
        contentContainer = new Div();
        getElement().appendChild(contentContainer.getElement());
        summaryContainer = createSummaryContainer();
        SlotUtils.addToSlot(this, "summary", summaryContainer);

        if (getElement().getPropertyRaw("opened") == null) {
            setOpened(false);
        }

        getElement().addPropertyChangeListener("opened", event -> fireEvent(
                new OpenedChangeEvent(this, event.isUserOriginated())));
    }

    /**
     * Initializes a new Details using the provided summary.
     *
     * @param summary
     *            the summary component to set.
     * @see #setSummaryText(String)
     */
    public Details(String summary) {
        this();
        setSummaryText(summary);
    }

    /**
     * Initializes a new Details using the provided summary.
     *
     * @param summary
     *            the summary component to set.
     * @see #setSummary(Component)
     */
    public Details(Component summary) {
        this();
        setSummary(summary);
    }

    /**
     * Initializes a new Details using the provided summary and content.
     *
     * @param summary
     *            the summary text to set.
     * @param content
     *            the content component to set.
     *
     * @see #setSummaryText(String)
     * @see #setContent(Component)
     */
    public Details(String summary, Component content) {
        this();
        setSummaryText(summary);
        setContent(content);
    }

    /**
     * Initializes a new Details using the provided summary and content.
     *
     * @param summary
     *            the summary component to set.
     * @param content
     *            the content component to set.
     *
     * @see #setSummary(Component)
     * @see #setContent(Component)
     */
    public Details(Component summary, Component content) {
        this();
        setSummary(summary);
        setContent(content);
    }

    /**
     * Initializes a new Details using the provided summary and content
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
    public Details(String summary, Component... components) {
        this(summary);
        addContent(components);
    }

    /**
     * Initializes a new Details using the provided summary and content
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
    public Details(Component summary, Component... components) {
        this(summary);
        addContent(components);
    }

    /**
     * Creates the summary container component.
     *
     * @return the summary container
     */
    protected Component createSummaryContainer() {
        return new DetailsSummary();
    }

    /**
     * Sets the component summary
     *
     * @see #getSummary()
     * @param summary
     *            the summary component to set, or <code>null</code> to remove
     *            any previously set summary
     */
    public void setSummary(Component summary) {
        summaryContainer.getElement().removeAllChildren();
        if (summary == null) {
            return;
        }

        this.summary = summary;
        summaryContainer.getElement().appendChild(summary.getElement());
    }

    /**
     * Returns summary component which was set via
     * {@link #setSummary(Component)} or {@link #setSummaryText(String)}
     *
     * @return the summary component, <code>null</code> if nothing was set
     */
    public Component getSummary() {
        return summary;
    }

    /**
     * Creates a text wrapper and sets a summary via
     * {@link #setSummary(Component)}
     */
    public void setSummaryText(String summary) {
        if (summary == null) {
            summary = "";
        }
        setSummary(new Span(summary));
    }

    /**
     * @return summary section content as string (empty string if nothing was
     *         set)
     */
    public String getSummaryText() {
        return summary == null ? "" : summary.getElement().getText();
    }

    /**
     * Sets the component content
     *
     * @see #getContent()
     * @param content
     *            the content of the component to set, or <code>null</code> to
     *            remove any previously set content
     */
    public void setContent(Component content) {
        contentContainer.getElement().removeAllChildren();
        if (content == null) {
            return;
        }

        contentContainer.add(content);
    }

    /**
     * Adds components to the content section
     *
     * @see #getContent()
     * @param components
     *            the components to add
     */
    public void addContent(Component... components) {
        contentContainer.add(components);
    }

    /**
     * Returns the content components which were added via
     * {@link #setContent(Component)} or via {@link #addContent(Component...)}
     *
     * @return the child components of the content section
     */
    public Stream<Component> getContent() {
        return contentContainer.getChildren();
    }

    /**
     * See {@link #setOpened(boolean)}
     *
     * @return whether details are expanded or collapsed
     */
    @Synchronize(property = "opened", value = "opened-changed")
    public boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    /**
     * <p>
     * True if the details are opened and the content is displayed
     * </p>
     *
     * @param opened
     *            the boolean value to set
     */
    public void setOpened(boolean opened) {
        getElement().setProperty("opened", opened);
    }

    public static class OpenedChangeEvent extends ComponentEvent<Details> {
        private final boolean opened;

        public OpenedChangeEvent(Details source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * Adds a listener to get notified when the opened state of the component
     * changes.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent> listener) {
        return addListener(OpenedChangeEvent.class, listener);
    }
}
