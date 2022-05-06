package com.vaadin.flow.component.details;

/*
 * #%L
 * Details for Vaadin Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/details", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-details", version = "23.1.0-beta1")
@JsModule("@vaadin/details/src/vaadin-details.js")
public class Details extends Component
        implements HasEnabled, HasTheme, HasStyle, HasSize {

    private Component summary;
    private final Div contentContainer;

    /**
     * Initializes a new Details component.
     */
    public Details() {
        contentContainer = new Div();
        getElement().appendChild(contentContainer.getElement());
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
     * Sets the component summary
     *
     * @see #getSummary()
     * @param summary
     *            the summary component to set, or <code>null</code> to remove
     *            any previously set summary
     */
    public void setSummary(Component summary) {
        if (this.summary != null) {
            this.summary.getElement().removeAttribute("slot");
            this.summary.getElement().removeFromParent();
        }

        this.summary = summary;
        if (summary == null) {
            return;
        }

        summary.getElement().setAttribute("slot", "summary");
        getElement().appendChild(summary.getElement());
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
     * @return summary section content as string
     */
    public String getSummaryText() {
        return summary.getElement().getText();
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

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(DetailsVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants).map(DetailsVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(DetailsVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(DetailsVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    @DomEvent("opened-changed")
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
     * Adds a listener for {@code opened-changed} events fired by the
     * webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent> listener) {
        return ComponentUtil.addListener(this, OpenedChangeEvent.class,
                listener);
    }
}
