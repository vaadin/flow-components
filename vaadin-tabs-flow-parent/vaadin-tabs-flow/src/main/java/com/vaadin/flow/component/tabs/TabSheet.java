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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

/**
 * TabSheet consists of a set of tabs and the content area. The content area
 * displays a component associated with the selected tab.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-tabsheet")
@NpmPackage(value = "@vaadin/tabsheet", version = "24.8.0-alpha18")
@JsModule("@vaadin/tabsheet/src/vaadin-tabsheet.js")
public class TabSheet extends Component implements HasPrefix, HasStyle, HasSize,
        HasSuffix, HasThemeVariant<TabSheetVariant> {

    private Tabs tabs = new Tabs();

    private Map<Tab, Element> tabToContent = new HashMap<>();

    /**
     * The default constructor.
     */
    public TabSheet() {
        super();

        SlotUtils.addToSlot(this, "tabs", tabs);

        addSelectedChangeListener(e -> {
            getElement().setProperty("selected", tabs.getSelectedIndex());
            updateContent();
        });
    }

    /**
     * Adds a tab created from the given text and content.
     *
     * @param tabText
     *            the text of the tab
     * @param content
     *            the content related to the tab
     * @return the created tab
     */
    public Tab add(String tabText, Component content) {
        return add(new Tab(tabText), content);
    }

    /**
     * Adds a tab created from the given tab content and content.
     *
     * @param tabContent
     *            the content of the tab
     * @param content
     *            the content related to the tab
     * @return the created tab
     */
    public Tab add(Component tabContent, Component content) {
        return add(new Tab(tabContent), content);
    }

    /**
     * Adds a tab with the given content.
     *
     * @param tab
     *            the tab
     * @param content
     *            the content related to the tab
     * @return the added tab
     */
    public Tab add(Tab tab, Component content) {
        return add(tab, content, -1);
    }

    /**
     * Adds a tab with the given content to the given position.
     *
     * @param tab
     *            the tab
     * @param content
     *            the content related to the tab
     * @param position
     *            the position where the tab should be added. If negative, the
     *            tab is added at the end.
     * @return the added tab
     */
    public Tab add(Tab tab, Component content, int position) {
        Objects.requireNonNull(tab, "The tab to be added cannot be null");
        Objects.requireNonNull(content,
                "The content to be added cannot be null");

        if (content instanceof Text) {
            throw new IllegalArgumentException(
                    "Text as content is not supported. Consider wrapping the Text inside a Div.");
        }

        if (position < 0) {
            tabs.add(tab);
        } else {
            tabs.addTabAtIndex(position, tab);
        }

        // Make sure possible old content related to the same tab gets removed
        if (tabToContent.containsKey(tab)) {
            tabToContent.get(tab).removeFromParent();
        }

        linkTabToContent(tab, content);

        tabToContent.put(tab, content.getElement());

        updateContent();

        return tab;
    }

    private void linkTabToContent(Tab tab, Component content) {
        runBeforeClientResponse(ui -> {
            // On the client, content is associated with a tab by id
            var tabId = tab.getId().orElse("tabsheet-tab-" + UUID.randomUUID());
            tab.setId(tabId);
            content.getElement().setAttribute("tab", tabId);
        });
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * Removes a tab.
     *
     * @param tab
     *            the non-null tab to be removed
     */
    public void remove(Tab tab) {
        Objects.requireNonNull(tab, "The tab to be removed cannot be null");
        var content = tabToContent.remove(tab);
        content.removeFromParent();
        tabs.remove(tab);
    }

    /**
     * Removes a tab based on the content
     *
     * @param content
     *            the non-null content related to the tab to be removed
     */
    public void remove(Component content) {
        Objects.requireNonNull(content,
                "The content of the tab to be removed cannot be null");

        if (content instanceof Text) {
            throw new IllegalArgumentException(
                    "Text as content is not supported.");
        }

        var tab = getTab(content);

        if (tab != null) {
            remove(tab);
        }
    }

    /**
     * Removes the tab at the given position.
     *
     * @param position
     *            the position of the tab to be removed
     */
    public void remove(int position) {
        remove(getTabAt(position));
    }

    /**
     * Gets the zero-based index of the currently selected tab.
     *
     * @return the zero-based index of the selected tab, or -1 if none of the
     *         tabs is selected
     */
    public int getSelectedIndex() {
        return tabs.getSelectedIndex();
    }

    /**
     * Selects a tab based on its zero-based index.
     *
     * @param selectedIndex
     *            the zero-based index of the selected tab, -1 to unselect all
     */
    public void setSelectedIndex(int selectedIndex) {
        tabs.setSelectedIndex(selectedIndex);
    }

    /**
     * Gets the currently selected tab.
     *
     * @return the selected tab, or {@code null} if none is selected
     */
    public Tab getSelectedTab() {
        return tabs.getSelectedTab();
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
        tabs.setSelectedTab(selectedTab);
    }

    /**
     * Gets the number of tabs.
     *
     * @return the number of tabs
     */
    public int getTabCount() {
        return tabs.getTabCount();
    }

    /**
     * Returns the tab at the given position.
     *
     * @param position
     *            the position of the tab, must be greater than or equals to 0
     *            and less than the number of tabs
     * @return The tab at the given index
     * @throws IllegalArgumentException
     *             if the index is less than 0 or greater than or equals to the
     *             number of tabs
     */
    public Tab getTabAt(int position) {
        return tabs.getTabAt(position);
    }

    /**
     * Returns the index of the given tab.
     *
     * @param tab
     *            the tab to look up, can not be <code>null</code>
     * @return the index of the tab or -1 if the tab is not added
     */
    public int getIndexOf(Tab tab) {
        return tabs.indexOf(tab);
    }

    /**
     * Returns the {@link Tab} associated with the given component.
     *
     * @param content
     *            the component to look up, can not be <code>null</code>
     * @return The tab instance associated with the given component, or
     *         <code>null</code> if the {@link TabSheet} does not contain the
     *         component.
     */
    public Tab getTab(Component content) {
        Objects.requireNonNull(content,
                "The component to look for the tab cannot be null");

        return tabToContent.entrySet().stream()
                .filter(entry -> entry.getValue().equals(content.getElement()))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    /**
     * Returns the {@link Component} instance associated with the given tab.
     *
     * @param tab
     *            the tab to look up, can not be <code>null</code>
     * @return The component instance associated with the given tab, or
     *         <code>null</code> if the {@link TabSheet} does not contain the
     *         tab.
     */
    public Component getComponent(Tab tab) {
        Objects.requireNonNull(tab,
                "The tab to look for the component cannot be null");

        var tabContent = tabToContent.get(tab);
        if (tabContent == null) {
            return null;
        }
        return tabContent.getComponent().orElse(null);
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

        return tabs.addSelectedChangeListener(event -> {
            listener.onComponentEvent(new SelectedChangeEvent(TabSheet.this,
                    event.getPreviousTab(), event.isFromClient(),
                    event.isInitialSelection()) {
                @Override
                public void unregisterListener() {
                    event.unregisterListener();
                }
            });
        });

    }

    /**
     * Marks the content related to the selected tab as enabled and adds it to
     * the component if it is not already added. All the other content panels
     * are disabled so they can't be interacted with.
     */
    private void updateContent() {
        for (Map.Entry<Tab, Element> entry : tabToContent.entrySet()) {
            var tab = entry.getKey();
            var content = entry.getValue();

            if (tab.equals(tabs.getSelectedTab())) {
                if (content.getParent() == null) {
                    getElement().appendChild(content);
                }
                content.setEnabled(true);
            } else {
                // Can't use setEnabled(false) because it would also mark the
                // elements as disabled in the DOM. Navigating between tabs
                // would then briefly show the content as disabled.
                content.getNode().setEnabled(false);
            }
        }
    }

    /**
     * An event to mark that the selected tab has changed.
     */
    public static class SelectedChangeEvent extends ComponentEvent<TabSheet> {
        private final Tab selectedTab;
        private final Tab previousTab;
        private final boolean initialSelection;

        /**
         * Creates a new selected change event.
         *
         * @param source
         *            The TabSheet that fired the event.
         * @param previousTab
         *            The previous selected tab.
         * @param fromClient
         *            <code>true</code> for client-side events,
         *            <code>false</code> otherwise.
         */
        public SelectedChangeEvent(TabSheet source, Tab previousTab,
                boolean fromClient, boolean initialSelection) {
            super(source, fromClient);
            this.selectedTab = source.getSelectedTab();
            this.initialSelection = initialSelection;
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
         * Checks if this event is initial TabSheet selection.
         *
         * @return <code>true</code> if the event is initial TabSheet selection,
         *         <code>false</code> otherwise
         */
        public boolean isInitialSelection() {
            return this.initialSelection;
        }
    }

}
