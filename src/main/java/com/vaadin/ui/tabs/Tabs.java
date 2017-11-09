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

package com.vaadin.ui.tabs;

import java.util.Locale;

import com.vaadin.shared.Registration;
import com.vaadin.ui.Component;
import com.vaadin.ui.common.HasOrderedComponents;
import com.vaadin.ui.common.HasSize;
import com.vaadin.ui.event.ComponentEvent;
import com.vaadin.ui.event.ComponentEventListener;
import com.vaadin.ui.event.DomEvent;
import com.vaadin.ui.event.Synchronize;

/**
 * Server-side component for the {@code vaadin-tabs} element.
 *
 * @author Vaadin Ltd.
 */
public class Tabs extends GeneratedVaadinTabs<Tabs>
        implements HasOrderedComponents<Tabs>, HasSize {

    /**
     * The valid orientations of {@link Tabs} instances.
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    /**
     * Constructs a new object in its default state.
     */
    public Tabs() {
    }

    /**
     * Constructs a new object enclosing the given tabs.
     *
     * @param tabs
     *            the tabs to enclose
     */
    public Tabs(Tab... tabs) {
        super(tabs);
    }

    /**
     * Adds the given tabs to the component.
     *
     * @param tabs
     *            the tabs to enclose
     */
    public void add(Tab... tabs) {
        super.add(tabs);
    }

    /**
     * An event to mark that the selected tab has changed.
     */
    @DomEvent("selected-changed")
    public static class SelectedChangeEvent extends ComponentEvent<Tabs> {
        public SelectedChangeEvent(Tabs source, boolean fromClient) {
            super(source, fromClient);
        }
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
     * @return the zero-based index of the selected tab
     */
    @Synchronize(property = "selected", value = "selected-changed")
    public int getSelectedIndex() {
        return getElement().getProperty("selected", 0);
    }

    /**
     * Selects a tab based on its zero-based index.
     *
     * @param selectedIndex
     *            the zero-based index of the selected tab
     */
    public void setSelectedIndex(int selectedIndex) {
        getElement().setProperty("selected", selectedIndex);
    }

    /**
     * Gets the currently selected tab.
     *
     * @return the selected tab
     */
    public Tab getSelectedTab() {
        int selectedIndex = getSelectedIndex();
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
     *            the tab to select
     * @throws IllegalArgumentException
     *             if {@code selectedTab} is not a child of this component
     */
    public void setSelectedTab(Tab selectedTab) {
        int selectedIndex = indexOf(selectedTab);
        if (selectedIndex < 0) {
            throw new IllegalArgumentException(
                    "Tab to select must be a child: " + selectedTab);
        }
        getElement().setProperty("selected", selectedIndex);
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
}
