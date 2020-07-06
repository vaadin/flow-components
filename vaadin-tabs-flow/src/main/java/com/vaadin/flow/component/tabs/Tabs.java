/*
 * Copyright 2000-2017 Vaadin Ltd.
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
 * Server-side component for the {@code vaadin-tabs} element.
 * <p>
 * {@link Tab} components can be added to this component with the
 * {@link #add(Tab...)} method or the {@link #Tabs(Tab...)} constructor. The Tab
 * components added to it can be selected with the
 * {@link #setSelectedIndex(int)} or {@link #setSelectedTab(Tab)} methods.
 * Removing the selected tab from the component changes the selection to the
 * next available tab.
 * <p>
 * <strong>Note:</strong> Adding or removing Tab components via the Element API,
 * eg. {@code tabs.getElement().insertChild(0, tab.getElement()); }, doesn't
 * update the selected index, so it may cause the selected tab to change
 * unexpectedly.
 *
 * @author Vaadin Ltd.
 */
public class Tabs extends GeneratedVaadinTabs<Tabs>
        implements HasOrderedComponents<Tabs>, HasSize {

    private static final String SELECTED = "selected";

    private transient Tab selectedTab;

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
     *
     * @param tabs
     *            the tabs to enclose
     */
    public Tabs(Tab... tabs) {
        this();
        add(tabs);
    }

    /**
     * Adds the given tabs to the component.
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
        if (wasEmpty) {
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
     * tab. Removing the selected tab will select the next available tab.
     */
    @Override
    public void remove(Component... components) {
        int lowerIndices = (int) Stream.of(components).map(this::indexOf)
                .filter(index -> index >= 0 && index < getSelectedIndex())
                .count();

        HasOrderedComponents.super.remove(components);

        // Prevents changing the selected tab
        int newSelectedIndex = getSelectedIndex() - lowerIndices;

        // In case the last tab was removed
        if (newSelectedIndex > 0 && newSelectedIndex >= getComponentCount()) {
            newSelectedIndex = getComponentCount() - 1;
        }

        if (getComponentCount() == 0) {
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
        public SelectedChangeEvent(Tabs source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(
                this,
                context -> ui.getPage().executeJavaScript(
                        "$0.addEventListener('items-changed', "
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
                    "Illegal component inside Tabs: " + selectedComponent);
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
                orientation.name().toLowerCase());
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

    @ClientCallable
    private void updateSelectedTab(boolean changedFromClient) {
        if (getSelectedIndex() < -1) {
            setSelectedIndex(-1);
            return;
        }

        Tab currentlySelected = getSelectedTab();

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

            fireEvent(new SelectedChangeEvent(this, changedFromClient));
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
