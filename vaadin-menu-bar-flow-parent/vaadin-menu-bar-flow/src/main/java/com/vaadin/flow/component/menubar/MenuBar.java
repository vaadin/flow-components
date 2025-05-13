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
package com.vaadin.flow.component.menubar;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.MenuItemsArrayGenerator;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.internal.JsonSerializer;

import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * Menu Bar is a horizontal button bar with hierarchical drop-down menus. Menu
 * items can either trigger an action, open a menu, or work as a toggle.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-menu-bar")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("./menubarConnector.js")
@JsModule("@vaadin/menu-bar/src/vaadin-menu-bar.js")
@JsModule("@vaadin/tooltip/src/vaadin-tooltip.js")
@NpmPackage(value = "@vaadin/menu-bar", version = "24.8.0-alpha18")
@NpmPackage(value = "@vaadin/tooltip", version = "24.8.0-alpha18")
public class MenuBar extends Component
        implements HasEnabled, HasMenuItems, HasOverlayClassName, HasSize,
        HasStyle, HasThemeVariant<MenuBarVariant> {

    private MenuManager<MenuBar, MenuItem, SubMenu> menuManager;
    private MenuItemsArrayGenerator<MenuItem> menuItemsArrayGenerator;

    private MenuBarI18n i18n;

    private boolean updateScheduled = false;

    /**
     * Creates an empty menu bar component.
     * <p>
     * Use {@link #addItem(String)} to add content to the menu bar.
     */
    public MenuBar() {
        menuItemsArrayGenerator = new MenuItemsArrayGenerator<>(this);
        // Not a lambda because of UI serialization purposes
        SerializableRunnable resetContent = new SerializableRunnable() {
            @Override
            public void run() {
                resetContent();
            }
        };
        menuManager = new MenuManager<>(this, resetContent,
                (menu, contentReset) -> new MenuBarRootItem(this, contentReset),
                MenuItem.class, null);
        addAttachListener(event -> {
            String appId = event.getUI().getInternals().getAppId();
            initConnector(appId);
            resetContent();
        });
    }

    /**
     * Creates a new {@link MenuItem} component with the provided text content
     * and adds it to the root level of this menu bar.
     * <p>
     * The added {@link MenuItem} component is placed inside a button in the
     * menu bar. If this button overflows the menu bar horizontally, the
     * {@link MenuItem} is moved out of the button, into a context menu openable
     * via an overflow button at the end of the button row.
     * <p>
     * To add content to the sub menu opened by clicking the root level item,
     * use {@link MenuItem#getSubMenu()}.
     *
     * @param text
     *            the text content for the new item
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(String text) {
        return menuManager.addItem(text);
    }

    /**
     * Creates a new {@link MenuItem} component and adds it to the root level of
     * this menu bar. The provided component is added into the created
     * {@link MenuItem}.
     * <p>
     * The added {@link MenuItem} component is placed inside a button in the
     * menu bar. If this button overflows the menu bar horizontally, the
     * {@link MenuItem} is moved out of the button, into a context menu openable
     * via an overflow button at the end of the button row.
     * <p>
     * To add content to the sub menu opened by clicking the root level item,
     * use {@link MenuItem#getSubMenu()}.
     *
     * @param component
     *            the component to add inside new item
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(Component component) {
        return menuManager.addItem(component);
    }

    /**
     * Creates a new {@link MenuItem} component with the provided text content
     * and click listener and adds it to the root level of this menu bar.
     * <p>
     * The added {@link MenuItem} component is placed inside a button in the
     * menu bar. If this button overflows the menu bar horizontally, the
     * {@link MenuItem} is moved out of the button, into a context menu openable
     * via an overflow button at the end of the button row.
     * <p>
     * To add content to the sub menu opened by clicking the root level item,
     * use {@link MenuItem#getSubMenu()}.
     *
     * @param text
     *            the text content for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     */
    @Override
    public MenuItem addItem(String text,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return menuManager.addItem(text, clickListener);
    }

    /**
     * Creates a new {@link MenuItem} component with the provided click listener
     * and adds it to the root level of this menu bar. The provided component is
     * added into the created {@link MenuItem}.
     * <p>
     * The added {@link MenuItem} component is placed inside a button in the
     * menu bar. If this button overflows the menu bar horizontally, the
     * {@link MenuItem} is moved out of the button, into a context menu openable
     * via an overflow button at the end of the button row.
     * <p>
     * To add content to the sub menu opened by clicking the root level item,
     * use {@link MenuItem#getSubMenu()}.
     *
     * @param component
     *            the component to add inside the added menu item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     */
    @Override
    public MenuItem addItem(Component component,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return menuManager.addItem(component, clickListener);
    }

    /**
     * Creates a new {@link MenuItem} component with the provided text content
     * and the tooltip text and adds it to the root level of this menu bar.
     * <p>
     * The added {@link MenuItem} component is placed inside a button in the
     * menu bar. If this button overflows the menu bar horizontally, the
     * {@link MenuItem} is moved out of the button, into a context menu openable
     * via an overflow button at the end of the button row.
     * <p>
     * To add content to the sub menu opened by clicking the root level item,
     * use {@link MenuItem#getSubMenu()}.
     *
     * @param text
     *            the text content for the new item
     * @param tooltipText
     *            the tooltip text for the new item
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(String text, String tooltipText) {
        var item = addItem(text);
        setTooltipText(item, tooltipText);
        return item;
    }

    /**
     * Creates a new {@link MenuItem} component with the provided tooltip text
     * and adds it to the root level of this menu bar. The provided component is
     * added into the created {@link MenuItem}.
     * <p>
     * The added {@link MenuItem} component is placed inside a button in the
     * menu bar. If this button overflows the menu bar horizontally, the
     * {@link MenuItem} is moved out of the button, into a context menu openable
     * via an overflow button at the end of the button row.
     * <p>
     * To add content to the sub menu opened by clicking the root level item,
     * use {@link MenuItem#getSubMenu()}.
     *
     * @param component
     *            the component to add inside new item
     * @param tooltipText
     *            the tooltip text for the new item
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(Component component, String tooltipText) {
        var item = addItem(component);
        setTooltipText(item, tooltipText);
        return item;
    }

    /**
     * Creates a new {@link MenuItem} component with the provided text content
     * and the tooltip text and click listener and adds it to the root level of
     * this menu bar.
     * <p>
     * The added {@link MenuItem} component is placed inside a button in the
     * menu bar. If this button overflows the menu bar horizontally, the
     * {@link MenuItem} is moved out of the button, into a context menu openable
     * via an overflow button at the end of the button row.
     * <p>
     * To add content to the sub menu opened by clicking the root level item,
     * use {@link MenuItem#getSubMenu()}.
     *
     * @param text
     *            the text content for the new item
     * @param tooltipText
     *            the tooltip text for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(String text, String tooltipText,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        var item = addItem(text, clickListener);
        setTooltipText(item, tooltipText);
        return item;
    }

    /**
     * Creates a new {@link MenuItem} component with the provided click listener
     * and the tooltip text and adds it to the root level of this menu bar. The
     * provided component is added into the created {@link MenuItem}.
     * <p>
     * The added {@link MenuItem} component is placed inside a button in the
     * menu bar. If this button overflows the menu bar horizontally, the
     * {@link MenuItem} is moved out of the button, into a context menu openable
     * via an overflow button at the end of the button row.
     * <p>
     * To add content to the sub menu opened by clicking the root level item,
     * use {@link MenuItem#getSubMenu()}.
     *
     * @param component
     *            the component to add inside the added menu item
     * @param tooltipText
     *            the tooltip text for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(Component component, String tooltipText,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        var item = addItem(component, clickListener);
        setTooltipText(item, tooltipText);
        return item;
    }

    /**
     * Gets the {@link MenuItem} components added to the root level of the menu
     * bar.
     * <p>
     * To manage the contents inside the sub menus, use the
     * {@link MenuItem#getSubMenu()}.
     *
     * @return the root level {@link MenuItem} components added to this menu bar
     */
    public List<MenuItem> getItems() {
        return menuManager.getItems();
    }

    /**
     * Removes the given item components from this menu bar.
     *
     * @param items
     *            the item components to remove, not {@code null}
     * @throws IllegalArgumentException
     *             if any of the item components to remove is not a child of
     *             this menu bar
     *
     */
    public void remove(MenuItem... items) {
        menuManager.remove(items);
    }

    /**
     * Removes all item components from this menu bar.
     */
    public void removeAll() {
        menuManager.removeAll();
    }

    /**
     * Gets the child components of this menu bar.
     * <p>
     * The returned components are the same as the ones returned by
     * {@link #getItems()}.
     *
     * @return the child components of this menu bar
     */
    @Override
    public Stream<Component> getChildren() {
        return menuManager.getChildren();
    }

    /**
     * Sets the event which opens the sub menus of the root level buttons.
     *
     * @param openOnHover
     *            {@code true} to make the sub menus open on hover (mouseover),
     *            {@code false} to make them openable by clicking
     */
    public void setOpenOnHover(boolean openOnHover) {
        getElement().setProperty("openOnHover", openOnHover);
    }

    /**
     * Gets whether the sub menus open by clicking or hovering on the root level
     * buttons.
     *
     * @return {@code true} if the sub menus open by hovering on the root level
     *         buttons, {@code false} if they open by clicking
     */
    public boolean isOpenOnHover() {
        return getElement().getProperty("openOnHover", false);
    }

    /**
     * Sets reverse collapse order for the menu bar.
     *
     * @param reverseCollapseOrder
     *            If {@code true}, the buttons will be collapsed into the
     *            overflow menu starting from the "start" end of the bar instead
     *            of the "end".
     */
    public void setReverseCollapseOrder(boolean reverseCollapseOrder) {
        getElement().setProperty("reverseCollapse", reverseCollapseOrder);
    }

    /**
     * Gets whether the menu bar uses reverse collapse order.
     *
     * @return {@code true} if the buttons will be collapsed into the overflow
     *         menu starting from the "start" end of the bar instead of the
     *         "end".
     *
     */
    public boolean isReverseCollapseOrder() {
        return getElement().getProperty("reverseCollapse", false);
    }

    /**
     * Sets tab navigation for the menu bar.
     *
     * @param tabNavigation
     *            If {@code true}, the top-level menu items is traversable by
     *            tab instead of arrow keys (i.e. disabling roving tabindex)
     */
    public void setTabNavigation(boolean tabNavigation) {
        getElement().setProperty("tabNavigation", tabNavigation);
    }

    /**
     * Gets whether the menu bar uses tab navigation.
     *
     * @return {@code true} if the top-level menu items is traversable by tab
     *         instead of arrow keys (i.e. disabling roving tabindex)
     *
     */
    public boolean isTabNavigation() {
        return getElement().getProperty("tabNavigation", false);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using {@link #setI18n(MenuBarI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public MenuBarI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(MenuBarI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");

        runBeforeClientResponse(ui -> {
            if (i18n == this.i18n) {
                setI18nWithJS();
            }
        });
    }

    private void setI18nWithJS() {
        JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(this.i18n);

        // Remove properties with null values to prevent errors in web
        // component
        removeNullValuesFromJsonObject(i18nJson);

        // Assign new I18N object to WC, by merging the existing
        // WC I18N, and the values from the new MenuBarI18n instance,
        // into an empty object
        getElement().executeJs("this.i18n = Object.assign({}, this.i18n, $0);",
                i18nJson);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Element state is not persisted across attach/detach
        if (this.i18n != null) {
            setI18nWithJS();
        }
    }

    private void removeNullValuesFromJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keys()) {
            if (jsonObject.get(key).getType() == JsonType.NULL) {
                jsonObject.remove(key);
            }
        }
    }

    void resetContent() {
        menuItemsArrayGenerator.generate();
        updateButtons();
    }

    void updateButtons() {
        if (updateScheduled) {
            return;
        }
        runBeforeClientResponse(ui -> {
            // When calling `generateItems` without providing a node id, it will
            // use the previously generated items tree, only updating the
            // disabled and hidden properties of the root items = the menu bar
            // buttons.
            getElement().executeJs("this.$connector.generateItems()");
            updateScheduled = false;
        });
        updateScheduled = true;
    }

    private void initConnector(String appId) {
        getElement().executeJs(
                "window.Vaadin.Flow.menubarConnector.initLazy(this, $0)",
                appId);
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * The internationalization properties for {@link MenuBar}
     */
    public static class MenuBarI18n implements Serializable {
        private String moreOptions;

        /**
         * Gets the text that is used on the overflow button to make it
         * accessible.
         *
         * @return the overflow button aria-label
         */
        public String getMoreOptions() {
            return moreOptions;
        }

        /**
         * Sets the text that is used on the overflow button to make it
         * accessible.
         *
         * @param moreOptions
         *            the overflow button aria-label
         * @return this instance for method chaining
         */
        public MenuBarI18n setMoreOptions(String moreOptions) {
            this.moreOptions = moreOptions;
            return this;
        }
    }

    /**
     * Sets the tooltip text for the given {@link MenuItem}.
     *
     * @param item
     *            the menu item to set the tooltip for
     * @param tooltipText
     *            the tooltip text to set for the item
     */
    public void setTooltipText(MenuItem item, String tooltipText) {
        if (!getElement().getChildren().anyMatch(
                child -> "tooltip".equals(child.getAttribute("slot")))) {
            // No <vaadin-tooltip> yet added, add one
            Element tooltipElement = new Element("vaadin-tooltip");

            tooltipElement.addAttachListener(e -> {
                // Assigns a generator that reads the tooltip property of the
                // item component
                tooltipElement.executeJs(
                        "this.generator = ({item}) => { return (item && item.component) ? item.component.tooltip : ''; }");
            });
            SlotUtils.addToSlot(this, "tooltip", tooltipElement);
        }

        item.getElement().setProperty("tooltip", tooltipText);
    }

    /**
     * Closes the current submenu.
     */
    public void close() {
        getElement().callJsFunction("close");
    }
}
