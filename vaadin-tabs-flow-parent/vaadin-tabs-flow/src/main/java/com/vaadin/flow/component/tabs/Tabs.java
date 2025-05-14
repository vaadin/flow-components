/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
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
import com.vaadin.flow.dom.Element;
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
@Tag("vaadin-tabs")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/tabs/src/vaadin-tabs.js")
@NpmPackage(value = "@vaadin/tabs", version = "24.8.0-alpha18")
public class Tabs extends Component
        implements HasEnabled, HasSize, HasStyle, HasThemeVariant<TabsVariant> {

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
        getElement().addPropertyChangeListener(SELECTED, event -> {
            int oldIndex = selectedTab != null ? indexOf(selectedTab) : -1;
            int newIndex = getSelectedIndex();
            if (newIndex >= getTabCount()) {
                LoggerFactory.getLogger(getClass()).warn(String.format(
                        "The selected index is out of range: %d. Reverting to the previous index: %d.",
                        newIndex, oldIndex));
                setSelectedIndex(oldIndex);
                return;
            }

            updateSelectedTab(event.isUserOriginated());
        });
    }

    /**
     * Constructs a new object enclosing the given tabs, with
     * {@link Orientation#HORIZONTAL HORIZONTAL} orientation.
     * <p>
     * The first added {@link Tab} component will be automatically selected. Any
     * selection change listener added afterwards will not be notified about the
     * auto-selected tab.
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
     * <p>
     * Unless auto-select is disabled, the first added {@link Tab} component
     * will be automatically selected. Any selection change listener added
     * afterwards will not be notified about the auto-selected tab.
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
     * unless auto-selection is explicitly disabled with
     * {@link #Tabs(boolean, Tab...)}, or {@link #setAutoselect(boolean)}. If a
     * selection change listener has been added before adding the tabs, it will
     * be notified with the auto-selected tab.
     *
     * @param components
     *            the tabs to enclose
     * @deprecated since 24.0, use {@link #add(Tab...)} instead.
     */
    @Deprecated
    public void add(Component... components) {
        Objects.requireNonNull(components, "Tabs should not be null");
        boolean allComponentsAreTabs = Arrays.stream(components).map(
                tab -> Objects.requireNonNull(tab, "Tab to add cannot be null"))
                .allMatch(component -> component instanceof Tab);
        if (!allComponentsAreTabs) {
            throw new IllegalArgumentException(
                    "Adding a component other than a Tab is not supported.");
        }
        Tab[] tabs = Arrays.copyOf(components, components.length, Tab[].class);
        add(tabs);
    }

    /**
     * Adds the given tabs to the component.
     * <p>
     * The first added {@link Tab} component will be automatically selected,
     * unless auto-selection is explicitly disabled with
     * {@link #Tabs(boolean, Tab...)}, or {@link #setAutoselect(boolean)}. If a
     * selection change listener has been added before adding the tabs, it will
     * be notified with the auto-selected tab.
     *
     * @param tabs
     *            the tabs to enclose
     */
    public void add(Tab... tabs) {
        Objects.requireNonNull(tabs, "Tabs should not be null");
        boolean wasEmpty = getTabCount() == 0;
        Arrays.stream(tabs).map(
                tab -> Objects.requireNonNull(tab, "Tab to add cannot be null"))
                .map(Tab::getElement).forEach(getElement()::appendChild);
        if (tabs.length == 0) {
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
     * Removes the given child tabs from this component.
     *
     * @param components
     *            the tabs to remove
     * @throws IllegalArgumentException
     *             if there is a tab whose non {@code null} parent is not this
     *             component
     *             <p>
     *             Removing tabs before the selected tab will decrease the
     *             {@link #getSelectedIndex() selected index} to avoid changing
     *             the selected tab. Removing the selected tab will select the
     *             next available tab if autoselect is true, otherwise no tab
     *             will be selected.
     * @deprecated since 24.0, use {@link #remove(Tab...)} instead.
     */
    @Deprecated
    public void remove(Component... components) {
        Objects.requireNonNull(components, "Tabs should not be null");
        boolean allComponentsAreTabs = Arrays.stream(components)
                .map(tab -> Objects.requireNonNull(tab,
                        "Tab to remove cannot be null"))
                .allMatch(component -> component instanceof Tab);
        if (!allComponentsAreTabs) {
            throw new IllegalArgumentException(
                    "Adding a component other than a Tab is not supported.");
        }
        remove((Tab[]) components);
    }

    /**
     * Removes the given child tabs from this component.
     *
     * @param tabs
     *            the tabs to remove
     * @throws IllegalArgumentException
     *             if there is a tab whose non {@code null} parent is not this
     *             component
     *             <p>
     *             Removing tabs before the selected tab will decrease the
     *             {@link #getSelectedIndex() selected index} to avoid changing
     *             the selected tab. Removing the selected tab will select the
     *             next available tab if autoselect is true, otherwise no tab
     *             will be selected.
     */
    public void remove(Tab... tabs) {
        int selectedIndex = getSelectedIndex();
        int lowerIndices = (int) Stream.of(tabs).map(this::indexOf)
                .filter(index -> index >= 0 && index < selectedIndex).count();

        Tab selectedTab = getSelectedTab();
        boolean isSelectedTab = selectedTab == null
                || Stream.of(tabs).anyMatch(selectedTab::equals);

        doRemoveTabs(tabs);

        // Prevents changing the selected tab
        int newSelectedIndex = getSelectedIndex() - lowerIndices;

        // In case the last tab was removed
        if (newSelectedIndex > 0 && newSelectedIndex >= getTabCount()) {
            newSelectedIndex = getTabCount() - 1;
        }

        if (getTabCount() == 0 || (isSelectedTab && !isAutoselect())) {
            newSelectedIndex = -1;
        }

        if (newSelectedIndex != getSelectedIndex()) {
            setSelectedIndex(newSelectedIndex);
        } else {
            updateSelectedTab(false);
        }
    }

    private void doRemoveTabs(Tab... tabs) {
        List<Tab> toRemove = new ArrayList<>(tabs.length);
        for (Tab tab : tabs) {
            Objects.requireNonNull(tab, "Tab to remove cannot be null");
            Element parent = tab.getElement().getParent();
            if (parent == null) {
                LoggerFactory.getLogger(getClass())
                        .debug("Remove of a tab with no parent does nothing.");
                continue;
            }
            if (getElement().equals(parent)) {
                toRemove.add(tab);
            } else {
                throw new IllegalArgumentException("The given tab (" + tab
                        + ") is not a child of this tab");
            }
        }
        toRemove.stream().map(Tab::getElement)
                .forEach(getElement()::removeChild);
    }

    /**
     * Removes all tabs from this component. It also removes the children that
     * were added only at the client-side.
     * <p>
     * This will reset the {@link #getSelectedIndex() selected index} to zero.
     */
    public void removeAll() {
        getElement().removeAllChildren();
        if (getSelectedIndex() > -1) {
            setSelectedIndex(-1);
        } else {
            updateSelectedTab(false);
        }
    }

    /**
     * Adds the given tab as child of this tab at the specific index.
     * <p>
     * In case the specified tab has already been added to another parent, it
     * will be removed from there and added to this one.
     *
     * @param index
     *            the index, where the tab will be added. The index must be
     *            non-negative and may not exceed the children count
     * @param component
     *            the tab to add, value should not be null
     *            <p>
     *            Adding a tab before the currently selected tab will increment
     *            the {@link #getSelectedIndex() selected index} to avoid
     *            changing the selected tab.
     * @deprecated since 24.0, use {@link #addTabAtIndex(int, Tab)} instead.
     */
    @Deprecated
    public void addComponentAtIndex(int index, Component component) {
        Objects.requireNonNull(component, "Tab should not be null");
        if (!(component instanceof Tab)) {
            throw new IllegalArgumentException(
                    "Adding a component other than a Tab is not supported.");
        }
        addTabAtIndex(index, (Tab) component);
    }

    /**
     * Adds the given tab as child of this tab at the specific index.
     * <p>
     * In case the specified tab has already been added to another parent, it
     * will be removed from there and added to this one.
     *
     * @param index
     *            the index, where the tab will be added. The index must be
     *            non-negative and may not exceed the children count
     * @param tab
     *            the tab to add, value should not be null
     *            <p>
     *            Adding a tab before the currently selected tab will increment
     *            the {@link #getSelectedIndex() selected index} to avoid
     *            changing the selected tab.
     */
    public void addTabAtIndex(int index, Tab tab) {
        Objects.requireNonNull(tab, "Tab should not be null");
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a tab with a negative index");
        }
        // The case when the index is bigger than the children count is handled
        // inside the method below
        getElement().insertChild(index, tab.getElement());

        if (autoselect && getChildren().count() == 1) {
            setSelectedIndex(0);
        } else if (index <= getSelectedIndex()) {
            // Prevents changing the selected tab
            setSelectedIndex(getSelectedIndex() + 1);
        }
    }

    /**
     * Replaces the tab in the container with another one without changing
     * position. This method replaces tab with another one is such way that the
     * new tab overtakes the position of the old tab. If the old tab is not in
     * the container, the new tab is added to the container. If the both tabs
     * are already in the container, their positions are swapped. Tab attach and
     * detach events should be taken care as with add and remove.
     *
     * @param oldComponent
     *            the old tab that will be replaced. Can be <code>null</code>,
     *            which will make the newTab to be added to the layout without
     *            replacing any other
     *
     * @param newComponent
     *            the new tab to be replaced. Can be <code>null</code>, which
     *            will make the oldTab to be removed from the layout without
     *            adding any other
     *            <p>
     *            Replacing the currently selected tab will make the new tab
     *            selected.
     * @deprecated since 24.0, use {@link #replace(Tab, Tab)} instead.
     */
    @Deprecated
    public void replace(Component oldComponent, Component newComponent) {
        if (oldComponent != null && !(oldComponent instanceof Tab)) {
            throw new IllegalArgumentException(
                    "Removing a component other than a Tab is not supported.");
        }
        if (newComponent != null && !(newComponent instanceof Tab)) {
            throw new IllegalArgumentException(
                    "Adding a component other than a Tab is not supported.");
        }
        replace((Tab) oldComponent, (Tab) newComponent);
    }

    /**
     * Replaces the tab in the container with another one without changing
     * position. This method replaces tab with another one is such way that the
     * new tab overtakes the position of the old tab. If the old tab is not in
     * the container, the new tab is added to the container. If the both tabs
     * are already in the container, their positions are swapped. Tab attach and
     * detach events should be taken care as with add and remove.
     *
     * @param oldTab
     *            the old tab that will be replaced. Can be <code>null</code>,
     *            which will make the newTab to be added to the layout without
     *            replacing any other
     *
     * @param newTab
     *            the new tab to be replaced. Can be <code>null</code>, which
     *            will make the oldTab to be removed from the layout without
     *            adding any other
     *            <p>
     *            Replacing the currently selected tab will make the new tab
     *            selected.
     */
    public void replace(Tab oldTab, Tab newTab) {
        if (oldTab == null && newTab == null) {
            // NO-OP
        } else if (oldTab == null) {
            add(newTab);
        } else if (newTab == null) {
            remove(oldTab);
        } else {
            doReplace(oldTab, newTab);
        }
        updateSelectedTab(false);
    }

    private void doReplace(Tab oldTab, Tab newTab) {
        Element element = getElement();
        int oldIndex = element.indexOfChild(oldTab.getElement());
        int newIndex = element.indexOfChild(newTab.getElement());
        if (oldIndex >= 0 && newIndex >= 0) {
            element.insertChild(oldIndex, newTab.getElement());
            element.insertChild(newIndex, oldTab.getElement());
        } else if (oldIndex >= 0) {
            element.setChild(oldIndex, newTab.getElement());
        } else {
            add(newTab);
        }
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
        return getTabAt(selectedIndex);
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

        if (currentlySelected == null
                || currentlySelected.getElement().getNode().isEnabledSelf()) {
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
        boolean enabled = tab.getElement().getNode().isEnabledSelf();
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

    /**
     * Returns the index of the given tab.
     *
     * @param component
     *            the tab to look up, can not be <code>null</code>
     * @return the index of the tab or -1 if the tab is not a child
     * @deprecated since 24.0, use {@link #indexOf(Tab)} instead.
     */
    @Deprecated
    public int indexOf(Component component) {
        Objects.requireNonNull(component, "Tab should not be null");
        if (!(component instanceof Tab)) {
            throw new IllegalArgumentException(
                    "Adding a component other than a Tab is not supported.");
        }
        return indexOf((Tab) component);
    }

    /**
     * Returns the index of the given tab.
     *
     * @param tab
     *            the tab to look up, can not be <code>null</code>
     * @return the index of the tab or -1 if the tab is not a child
     */
    public int indexOf(Tab tab) {
        if (tab == null) {
            throw new IllegalArgumentException(
                    "The 'tab' parameter cannot be null");
        }
        Iterator<Component> it = getChildren().sequential().iterator();
        int index = 0;
        while (it.hasNext()) {
            Component next = it.next();
            if (tab.equals(next)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Gets the number of children tabs.
     *
     * @return the number of tabs
     * @deprecated since 24.5, use {@link #getTabCount} instead.
     */
    @Deprecated
    public int getComponentCount() {
        return getTabCount();
    }

    /**
     * Gets the number of tabs.
     *
     * @return the number of tabs
     */
    public int getTabCount() {
        return (int) getChildren().count();
    }

    /**
     * Returns the tab at the given position.
     *
     * @param index
     *            the position of the tab, must be greater than or equals to 0
     *            and less than the number of children tabs
     * @return The tab at the given index
     * @throws IllegalArgumentException
     *             if the index is less than 0 or greater than or equals to the
     *             number of children tabs
     * @deprecated since 24.0, use {@link #getTabAt(int)} instead.
     */
    @Deprecated
    public Component getComponentAt(int index) {
        return getTabAt(index);
    }

    /**
     * Returns the tab at the given position.
     *
     * @param index
     *            the position of the tab, must be greater than or equals to 0
     *            and less than the number of children tabs
     * @return The tab at the given index
     * @throws IllegalArgumentException
     *             if the index is less than 0 or greater than or equals to the
     *             number of children tabs
     */
    public Tab getTabAt(int index) {
        if (index < 0) {
            throw new IllegalArgumentException(
                    "The 'index' argument should be greater than or equal to 0. It was: "
                            + index);
        }
        return (Tab) getChildren().sequential().skip(index).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "The 'index' argument should not be greater than or equals to the number of children tabs. It was: "
                                + index));
    }

    /**
     * Adds the given tab as the first child of this component.
     * <p>
     * In case the specified tab has already been added to another parent, it
     * will be removed from there and added to this one.
     *
     * @param component
     *            the tab to add, value should not be null
     * @deprecated since 24.0, use {@link #addTabAsFirst(Tab)} instead.
     */
    @Deprecated
    public void addComponentAsFirst(Component component) {
        Objects.requireNonNull(component, "Tab should not be null");
        if (!(component instanceof Tab)) {
            throw new IllegalArgumentException(
                    "Adding a component other than a Tab is not supported.");
        }
        addTabAsFirst((Tab) component);
    }

    /**
     * Adds the given tab as the first child of this component.
     * <p>
     * In case the specified tab has already been added to another parent, it
     * will be removed from there and added to this one.
     *
     * @param tab
     *            the tab to add, value should not be null
     */
    public void addTabAsFirst(Tab tab) {
        addTabAtIndex(0, tab);
    }
}
