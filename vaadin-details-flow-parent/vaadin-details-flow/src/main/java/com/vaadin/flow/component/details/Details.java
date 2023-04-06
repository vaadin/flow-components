/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.details;
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

@Tag("vaadin-details")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "22.0.22")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/details", version = "22.0.22")
@NpmPackage(value = "@vaadin/vaadin-details", version = "22.0.22")
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
