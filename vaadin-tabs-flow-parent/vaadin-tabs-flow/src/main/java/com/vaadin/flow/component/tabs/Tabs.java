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

package com.vaadin.flow.component.tabs;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.shared.Registration;

/**
 * Tabs are used to organize and group content into sections that the user can
 * navigate between. Use Tabs when you want to allow in-place navigation within
 * a certain part of the UI, instead of showing everything at once or forcing
 * the user to navigate between different views.
 * <p>
 * {@link Tab} components can be added to this component with the
 * {@link #add(Tab...)} method or the {@link #Tabs(Tab...)} constructor. The Tab
 * components added to it can be selected with the
 * {@link #setSelectedIndex(int)} or {@link #setSelectedTab(Tab)} methods. The
 * first added {@link Tab} component will be automatically selected, firing a
 * {@link SelectedChangeEvent}, unless autoselection is explicitly disabled with
 * {@link #Tabs(boolean, Tab...)}, or {@link #setAutoselect(boolean)}. Removing
 * the selected tab from the component changes the selection to the next
 * available tab.
 * <p>
 * <strong>Note:</strong> Adding or removing Tab components via the Element API,
 * eg. {@code tabs.getElement().insertChild(0, tab.getElement()); }, doesn't
 * update the selected index, so it may cause the selected tab to change
 * unexpectedly.
 *
 * @author Vaadin Ltd.
 */
public class Tabs extends GeneratedVaadinTabs<Tabs>
        implements HasOrderedComponents, HasSize {

    private static final String SELECTED = "selected";

    private transient Tab selectedTab;

    private boolean autoselect = true;

    /**
     * The valid orientations of {@link Tabs} instances.
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    /**
     * Constructs an empty new object with {@link Orientation#HORIZONTAL
     * HORIZONTAL} orientation.
     */
    public Tabs() {
        setSelectedIndex(-1);
        getElement().addPropertyChangeListener(SELECTED,
                event -> updateSelectedTab(event.isUserOriginated()));
    }

    /**
     * Constructs a new object enclosing the given tabs, with
     * {@link Orientation#HORIZONTAL HORIZONTAL} orientation.
     * <p>
     * The first added {@link Tab} component will be automatically selected,
     * firing a {@link SelectedChangeEvent}, unless autoselection is explicitly
     * disabled with {@link #Tabs(boolean, Tab...)}, or
     * {@link #setAutoselect(boolean)}.
     *
     * @param tabs
     *            the tabs to enclose
     */
    public Tabs(Tab... tabs) {
        this();
        add(tabs);
    }

    /**
     * Constructs a new object enclosing the given autoselect option and tabs,
     * with {@link Orientation#HORIZONTAL HORIZONTAL} orientation.
     *
     * @param autoselect
     *            {@code true} to automatically select the first added tab,
     *            {@code false} to leave tabs unselected
     * @param tabs
     *            the tabs to enclose
     */
    public Tabs(boolean autoselect, Tab... tabs) {
        this();
        this.autoselect = autoselect;
        add(tabs);
    }

    /**
     * Adds the given tabs to the component.
     * <p>
     * The first added {@link Tab} component will be automatically selected,
     * firing a {@link SelectedChangeEvent}, unless autoselection is explicitly
     * disabled with {@link #Tabs(boolean, Tab...)}, or
     * {@link #setAutoselect(boolean)}.
     *
     * @param tabs
     *            the tabs to enclose
     */
    public void add(Tab... tabs) {
        add((Component[]) tabs);
    }

    @Override
    public void add(Component... components) {
        boolean wasEmpty = getComponentCount() == 0;
        HasOrderedComponents.super.add(components);
        if (components.length == 0) {
            return;
        }
        if (wasEmpty && autoselect) {
            assert getSelectedIndex() == -1;
            setSelectedIndex(0);
        } else {
            updateSelectedTab(false);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Removing components before the selected tab will decrease the
     * {@link #getSelectedIndex() selected index} to avoid changing the selected
     * tab. Removing the selected tab will select the next available tab if
     * autoselect is true, otherwise no tab will be selected.
     */
    @Override
    public void remove(Component... components) {
        int lowerIndices = (int) Stream.of(components).map(this::indexOf)
                .filter(index -> index >= 0 && index < getSelectedIndex())
                .count();

        boolean isSelectedTab = Stream.of(components)
                .anyMatch(component -> component.equals(getSelectedTab()));

        HasOrderedComponents.super.remove(components);

        // Prevents changing the selected tab
        int newSelectedIndex = getSelectedIndex() - lowerIndices;

        // In case the last tab was removed
        if (newSelectedIndex > 0 && newSelectedIndex >= getComponentCount()) {
            newSelectedIndex = getComponentCount() - 1;
        }

        if (getComponentCount() == 0 || (isSelectedTab && !isAutoselect())) {
            newSelectedIndex = -1;
        }

        if (newSelectedIndex != getSelectedIndex()) {
            setSelectedIndex(newSelectedIndex);
        } else {
            updateSelectedTab(false);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This will reset the {@link #getSelectedIndex() selected index} to zero.
     */
    @Override
    public void removeAll() {
        HasOrderedComponents.super.removeAll();
        if (getSelectedIndex() > -1) {
            setSelectedIndex(-1);
        } else {
            updateSelectedTab(false);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adding a component before the currently selected tab will increment the
     * {@link #getSelectedIndex() selected index} to avoid changing the selected
     * tab.
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        HasOrderedComponents.super.addComponentAtIndex(index, component);

        if (autoselect && getChildren().count() == 1) {
            setSelectedIndex(0);
        } else if (index <= getSelectedIndex()) {
            // Prevents changing the selected tab
            setSelectedIndex(getSelectedIndex() + 1);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Replacing the currently selected tab will make the new tab selected.
     */
    @Override
    public void replace(Component oldComponent, Component newComponent) {
        HasOrderedComponents.super.replace(oldComponent, newComponent);
        updateSelectedTab(false);
    }

    /**
     * An event to mark that the selected tab has changed.
     */
    public static class SelectedChangeEvent extends ComponentEvent<Tabs> {
        private final Tab selectedTab;
        private final Tab previousTab;
        private final boolean initialSelection;

        /**
         * Creates a new selected change event.
         *
         * @param source
         *            The tabs that fired the event.
         * @param fromClient
         *            <code>true</code> for client-side events,
         *            <code>false</code> otherwise.
         *
         * @deprecated use
         *             {@link #SelectedChangeEvent(Tabs source, Tab previousTab, boolean fromClient)}
         *             instead.
         */
        @Deprecated
        public SelectedChangeEvent(Tabs source, boolean fromClient) {
            this(source, null, fromClient);
        }

        /**
         * Creates a new selected change event.
         *
         * @param source
         *            The tabs that fired the event.
         * @param previousTab
         *            The previous selected tab.
         * @param fromClient
         *            <code>true</code> for client-side events,
         *            <code>false</code> otherwise.
         */
        public SelectedChangeEvent(Tabs source, Tab previousTab,
                boolean fromClient) {
            super(source, fromClient);
            this.selectedTab = source.getSelectedTab();
            this.initialSelection = source.isAutoselect() && previousTab == null
                    && !fromClient;
            this.previousTab = previousTab;
        }

        /**
         * Get selected tab for this event. Can be {@code null} when autoselect
         * is set to false.
         *
         * @return the selected tab for this event
         */
        public Tab getSelectedTab() {
            return this.selectedTab;
        }

        /**
         * Get previous selected tab for this event. Can be {@code null} when
         * autoselect is set to false.
         *
         * @return the selected tab for this event
         */
        public Tab getPreviousTab() {
            return this.previousTab;
        }

        /**
         * Checks if this event is initial tabs selection.
         *
         * @return <code>true</code> if the event is initial tabs selection,
         *         <code>false</code> otherwise
         */
        public boolean isInitialSelection() {
            return this.initialSelection;
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(
                this,
                context -> ui.getPage()
                        .executeJs("$0.addEventListener('items-changed', "
                                + "function(){ this.$server.updateSelectedTab(true); });",
                                getElement())));
    }

    /**
     * Adds a listener for {@link SelectedChangeEvent}.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addSelectedChangeListener(
            ComponentEventListener<SelectedChangeEvent> listener) {
        return addListener(SelectedChangeEvent.class, listener);
    }

    /**
     * Gets the zero-based index of the currently selected tab.
     *
     * @return the zero-based index of the selected tab, or -1 if none of the
     *         tabs is selected
     */
    @Synchronize(property = SELECTED, value = "selected-changed")
    public int getSelectedIndex() {
        return getElement().getProperty(SELECTED, -1);
    }

    /**
     * Selects a tab based on its zero-based index.
     *
     * @param selectedIndex
     *            the zero-based index of the selected tab, -1 to unselect all
     */
    public void setSelectedIndex(int selectedIndex) {
        getElement().setProperty(SELECTED, selectedIndex);
    }

    /**
     * Gets the currently selected tab.
     *
     * @return the selected tab, or {@code null} if none is selected
     */
    public Tab getSelectedTab() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        }

        Component selectedComponent = getComponentAt(selectedIndex);
        if (!(selectedComponent instanceof Tab)) {
            throw new IllegalStateException(
                    "Illegal component inside Tabs: " + selectedComponent + "."
                            + "Component should be an instance of Tab.");
        }
        return (Tab) selectedComponent;
    }

    /**
     * Selects the given tab.
     *
     * @param selectedTab
     *            the tab to select, {@code null} to unselect all
     * @throws IllegalArgumentException
     *             if {@code selectedTab} is not a child of this component
     */
    public void setSelectedTab(Tab selectedTab) {
        if (selectedTab == null) {
            setSelectedIndex(-1);
            return;
        }

        int selectedIndex = indexOf(selectedTab);
        if (selectedIndex < 0) {
            throw new IllegalArgumentException(
                    "Tab to select must be a child: " + selectedTab);
        }
        setSelectedIndex(selectedIndex);
    }

    /**
     * Gets the orientation of this tab sheet.
     *
     * @return the orientation
     */
    public Orientation getOrientation() {
        String orientation = getElement().getProperty("orientation");
        if (orientation != null) {
            return Orientation.valueOf(orientation.toUpperCase(Locale.ROOT));
        }
        return Orientation.HORIZONTAL;
    }

    /**
     * Sets the orientation of this tab sheet.
     *
     * @param orientation
     *            the orientation
     */
    public void setOrientation(Orientation orientation) {
        getElement().setProperty("orientation",
                orientation.name().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Sets the flex grow property of all enclosed tabs. The flex grow property
     * specifies what amount of the available space inside the layout the
     * component should take up, proportionally to the other components.
     * <p>
     * For example, if all components have a flex grow property value set to 1,
     * the remaining space in the layout will be distributed equally to all
     * components inside the layout. If you set a flex grow property of one
     * component to 2, that component will take twice the available space as the
     * other components, and so on.
     * <p>
     * Setting to flex grow property value 0 disables the expansion of the
     * component. Negative values are not allowed.
     *
     * @param flexGrow
     *            the proportion of the available space the enclosed tabs should
     *            take up
     * @throws IllegalArgumentException
     *             if {@code flexGrow} is negative
     */
    public void setFlexGrowForEnclosedTabs(double flexGrow) {
        if (flexGrow < 0) {
            throw new IllegalArgumentException(
                    "Flex grow property must not be negative");
        }
        getChildren().forEach(tab -> ((Tab) tab).setFlexGrow(flexGrow));
    }

    /**
     * Specify that the tabs should be automatically selected. When autoselect
     * is false, no tab will be selected when the component load and it will not
     * select any others tab when removing currently selected tab. The default
     * value is true.
     *
     * @param autoselect
     *            {@code true} to autoselect tab, {@code false} to not.
     */
    public void setAutoselect(boolean autoselect) {
        this.autoselect = autoselect;
    }

    /**
     * Gets whether the tabs should be automatically selected. The default value
     * is true.
     *
     * @return <code>true</code> if autoselect is active, <code>false</code>
     *         otherwise
     * @see #setAutoselect(boolean)
     */
    public boolean isAutoselect() {
        return this.autoselect;
    }

    @ClientCallable
    private void updateSelectedTab(boolean changedFromClient) {
        if (getSelectedIndex() < -1) {
            setSelectedIndex(-1);
            return;
        }

        Tab currentlySelected = getSelectedTab();
        Tab previousTab = selectedTab;

        if (Objects.equals(currentlySelected, selectedTab)) {
            return;
        }

        if (currentlySelected == null || currentlySelected.isEnabled()) {
            selectedTab = currentlySelected;
            getChildren().filter(Tab.class::isInstance).map(Tab.class::cast)
                    .forEach(tab -> tab.setSelected(false));

            if (selectedTab != null) {
                selectedTab.setSelected(true);
            }

            fireEvent(new SelectedChangeEvent(this, previousTab,
                    changedFromClient));
        } else {
            updateEnabled(currentlySelected);
            setSelectedTab(selectedTab);
        }
    }

    private void updateEnabled(Tab tab) {
        boolean enabled = tab.isEnabled();
        Serializable rawValue = tab.getElement().getPropertyRaw("disabled");
        if (rawValue instanceof Boolean) {
            // convert the boolean value to a String to force update the
            // property value. Otherwise since the provided value is the same as
            // the current one the update don't do anything.
            tab.getElement().setProperty("disabled",
                    enabled ? null : Boolean.TRUE.toString());
        } else {
            tab.setEnabled(enabled);
        }
    }

}
