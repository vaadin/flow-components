/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.SignalPropertySupport;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;

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
@NpmPackage(value = "@vaadin/details", version = "25.1.0-alpha7")
@JsModule("@vaadin/details/src/vaadin-details.js")
public class Details extends Component implements HasComponents, HasSize,
        HasThemeVariant<DetailsVariant>, HasTooltip {

    private Component summary;
    private final Component summaryContainer;
    private final Div contentContainer;

    /** Signal support for the summary text property. */
    private final SignalPropertySupport<String> summaryTextSupport = SignalPropertySupport
            .create(this, this::updateSummaryText);

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
            doSetOpened(false);
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
        updateSummaryText(summary);
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
        updateSummary(summary);
    }

    /**
     * Initializes a new Details component with a summary text provided by a
     * signal.
     *
     * @param summaryTextSignal
     *            the signal that provides the summary text
     */
    public Details(Signal<String> summaryTextSignal) {
        this();
        summaryTextSupport.bind(summaryTextSignal);
    }

    /**
     * Initializes a new Details using the provided summary and content.
     *
     * @param summary
     *            the summary text to set.
     * @param content
     *            the content component to add.
     *
     * @see #setSummaryText(String)
     * @see #add(Component...)
     */
    public Details(String summary, Component content) {
        this();
        updateSummaryText(summary);
        contentContainer.add(content);
    }

    /**
     * Initializes a new Details using the provided summary and content.
     *
     * @param summary
     *            the summary component to set.
     * @param content
     *            the content component to add.
     *
     * @see #setSummary(Component)
     * @see #add(Component...)
     */
    public Details(Component summary, Component content) {
        this();
        updateSummary(summary);
        contentContainer.add(content);
    }

    /**
     * Initializes a new Details component with a summary text provided by a
     * signal and content.
     *
     * @param summaryTextSignal
     *            the signal that provides the summary text.
     * @param content
     *            the content component to add.
     */
    public Details(Signal<String> summaryTextSignal, Component content) {
        this();
        summaryTextSupport.bind(summaryTextSignal);
        contentContainer.add(content);
    }

    /**
     * Initializes a new Details using the provided summary and content
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
    public Details(String summary, Component... components) {
        this(summary);
        contentContainer.add(components);
    }

    /**
     * Initializes a new Details using the provided summary and content
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
    public Details(Component summary, Component... components) {
        this(summary);
        contentContainer.add(components);
    }

    /**
     * Initializes a new Details component with a summary text provided by a
     * signal and optional content components.
     *
     * @param summaryTextSignal
     *            the signal that provides the summary text.
     * @param components
     *            the content components to add.
     */
    public Details(Signal<String> summaryTextSignal, Component... components) {
        this();
        summaryTextSupport.bind(summaryTextSignal);
        contentContainer.add(components);
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
        updateSummary(summary);
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
     * Sets the summary text of the details component.
     *
     * @param summary
     *            the summary text to set, or {@code null} for empty text
     * @throws BindingActiveException
     *             if the summary text is currently bound to a signal
     * @see #bindSummaryText(Signal)
     */
    public void setSummaryText(String summary) {
        summaryTextSupport.set(summary);
    }

    /**
     * @return summary section content as string (empty string if nothing was
     *         set)
     */
    public String getSummaryText() {
        return summary == null ? "" : summary.getElement().getText();
    }

    /**
     * Updates the summary component. For internal use during initialization and
     * signal updates.
     */
    private void updateSummary(Component summary) {
        summaryContainer.getElement().removeAllChildren();
        if (summary == null) {
            return;
        }

        this.summary = summary;
        summaryContainer.getElement().appendChild(summary.getElement());
    }

    /**
     * Updates the summary text when bound to a signal.
     */
    private void updateSummaryText(String newText) {
        if (summary == null || !(summary instanceof Span)) {
            updateSummary(new Span(newText));
        } else {
            summary.getElement().setText(newText);
        }
    }

    /**
     * Binds a {@link Signal}'s value to the summary text content of this
     * component and keeps the summary text synchronized with the signal value
     * while the element is in attached state. When the element is in detached
     * state, signal value changes have no effect. <code>null</code> signal
     * unbinds the existing binding.
     * <p>
     * While a Signal is bound, any attempt to set the summary text manually
     * throws {@link BindingActiveException}. Same happens when trying to bind a
     * new Signal while one is already bound.
     *
     * @param signal
     *            the signal to bind or <code>null</code> to unbind any existing
     *            binding
     * @throws BindingActiveException
     *             thrown when there is already an existing binding
     * @see #setSummaryText(String)
     */
    public void bindSummaryText(Signal<String> signal) {
        summaryTextSupport.bind(signal);
    }

    /**
     * Gets the summary text signal support instance for testing purposes.
     *
     * @return the summary text signal support
     */
    SignalPropertySupport<String> getSummaryTextSupport() {
        return summaryTextSupport;
    }

    /**
     * Adds components to the content section
     *
     * @see #getContent()
     * @param components
     *            the components to add
     */
    @Override
    public void add(Collection<Component> components) {
        contentContainer.add(components);
    }

    /**
     * Adds the given text to the content section
     *
     * @see #getContent()
     * @param text
     *            the text to add, not null
     */
    @Override
    public void add(String text) {
        contentContainer.add(text);
    }

    /**
     * Removes specified components from the content section
     *
     * @param components
     *            the components to remove
     */
    @Override
    public void remove(Collection<Component> components) {
        contentContainer.remove(components);
    }

    /**
     * Removes all components from the content section
     */
    @Override
    public void removeAll() {
        contentContainer.removeAll();
    }

    /**
     * Adds the given component to the content section at the specific index.
     * <p>
     * In case the specified component has already been added to another parent,
     * it will be removed from there and added to the content section of this
     * one.
     *
     * @param index
     *            the index, where the component will be added. The index must
     *            be non-negative and may not exceed the children count
     * @param component
     *            the component to add, value should not be null
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        contentContainer.addComponentAtIndex(index, component);
    }

    @Override
    public <T, S extends Signal<T>> void bindChildren(Signal<List<S>> list,
            SerializableFunction<S, Component> childFactory) {
        Objects.requireNonNull(list, "ListSignal cannot be null");
        Objects.requireNonNull(childFactory,
                "Child element factory cannot be null");
        contentContainer.bindChildren(list, childFactory);
    }

    /**
     * Returns the content components which were added via
     * {@link #add(Component...)}
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
        doSetOpened(opened);
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

    private void doSetOpened(boolean opened) {
        getElement().setProperty("opened", opened);
    }
}
