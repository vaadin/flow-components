/*
 * Copyright 2022 Vaadin Ltd.
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
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;

/**
 * TabSheet consists of a set of tabs and the content area constrolled by them.
 * The content area displays a component associated with the selected tab.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-tabsheet")
// @JsModule("@vaadin/tabsheet/src/vaadin-tabsheet.js")
// @NpmPackage(value = "@vaadin/vaadin-tabsheet", version = "23.3.0-alpha1")
// Temporary module used until the package is published to npm
@JsModule("./tabsheet/vaadin-tabsheet.js")
public class TabSheet extends Component implements HasStyle, HasSize {

    private Tabs tabs = new Tabs();

    private Map<Tab, Component> tabToContent = new HashMap<>();

    /**
     * The default constructor.
     */
    public TabSheet() {
        super();

        tabs.getElement().setAttribute("slot", "tabs");
        getElement().appendChild(tabs.getElement());

        addSelectedChangeListener(e -> updateContent());
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
    public Tab add(Tab tab, Component content) {
        Objects.requireNonNull(tab, "The tab to be added cannot be null");
        Objects.requireNonNull(content,
                "The content to be added cannot be null");
        tabs.add(tab);

        // On the client, content is associated with a tab by id
        var id = "tabsheet-tab-" + UUID.randomUUID().toString();
        tab.setId(id);
        content.getElement().setAttribute("tab", id);

        tabToContent.put(tab, content);

        updateContent();

        return tab;
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
     * Adds a tab created from the given text and content.
     *
     * @param tabText
     *            the text of the tab
     * @param content
     *            the content related to the tab
     * @return the created tab
     */
    public Tab add(String tabText, Component content) {
        return add(new Span(tabText), content);
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
        content.getElement().removeFromParent();
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
        var tab = tabToContent.entrySet().stream()
                .filter(entry -> entry.getValue().equals(content))
                .map(Map.Entry::getKey).findFirst().orElse(null);

        if (tab != null) {
            remove(tab);
        }
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
                    event.isInitialSelection()));
        });

    }

    /**
     * Marks the content related to the selected tab as enabled and adds it to
     * the component if it is not already added. All the other content panels
     * are disabled so they can't be interacted with.
     */
    private void updateContent() {
        for (Map.Entry<Tab, Component> entry : tabToContent.entrySet()) {
            var tab = entry.getKey();
            var content = entry.getValue();

            if (tab.equals(tabs.getSelectedTab())) {
                if (content.getElement().getParent() == null) {
                    getElement().appendChild(content.getElement());
                }
                content.getElement().setEnabled(true);
            } else {
                // Can't use setEnabled(false) because it would also mark the
                // elements as disabled in the DOM. Navigating between tabs
                // would then briefly show the content as disabled.
                content.getElement().getNode().setEnabled(false);
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
         *            The tabs that fired the event.
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
         * Checks if this event is initial tabs selection.
         *
         * @return <code>true</code> if the event is initial tabs selection,
         *         <code>false</code> otherwise
         */
        public boolean isInitialSelection() {
            return this.initialSelection;
        }
    }

}
