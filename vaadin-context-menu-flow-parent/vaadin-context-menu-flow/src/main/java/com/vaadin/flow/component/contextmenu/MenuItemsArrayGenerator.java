/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu;

import java.io.Serializable;
import com.vaadin.flow.function.SerializableConsumer;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;

/**
 * A class which is utilized internally by components such as context menu and
 * menu bar. It transform the components added to the menus and their sub menus
 * to the hierarchical items array of the web component.
 *
 * @param <I>
 *            the menu item type
 */
public class MenuItemsArrayGenerator<I extends MenuItemBase<?, I, ?>>
        implements Serializable {

    private Component menu;

    private boolean updateScheduled = false;
    private final Element container;

    public MenuItemsArrayGenerator(Component menu) {
        this.menu = menu;
        container = new Element("div");
        getElement().appendVirtualChild(container);

        menu.getUI().ifPresent(this::addContextMenuDependencies);
        menu.addAttachListener(e -> addContextMenuDependencies(e.getUI()));
    }

    /**
     * Rebuilds the client-side items array with the current components in the
     * menu and its sub menus.
     */
    public void generate() {
        if (updateScheduled) {
            return;
        }
        updateScheduled = true;
        runBeforeClientResponse(ui -> {
            container.removeAllChildren();
            getItems().forEach(this::resetContainers);

            int containerNodeId = createNewContainer(menu.getChildren());
            String appId = ui.getInternals().getAppId();

            ui.getPage().executeJavaScript(
                    "window.Vaadin.Flow.contextMenuConnector.generateItems($0, $1, $2)",
                    getElement(), appId, containerNodeId);

            updateScheduled = false;
        });
    }

    private void resetContainers(MenuItemBase<?, I, ?> menuItem) {
        if (!menuItem.isParentItem()) {
            menuItem.getElement().removeProperty("_containerNodeId");
            return;
        }
        SubMenuBase<?, I, ?> subMenu = menuItem.getSubMenu();

        int containerNodeId = createNewContainer(subMenu.getChildren());
        menuItem.getElement().setProperty("_containerNodeId", containerNodeId);

        subMenu.getItems().stream().forEach(this::resetContainers);
    }

    private int createNewContainer(Stream<Component> components) {
        Element subContainer = new Element("div");
        container.appendChild(subContainer);

        components
                .forEach(child -> subContainer.appendChild(child.getElement()));
        return subContainer.getNode().getId();
    }

    private Stream<MenuItemBase> getItems() {
        return menu.getChildren().filter(MenuItemBase.class::isInstance)
                .map(MenuItemBase.class::cast);
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(menu, context -> command.accept(ui)));
    }

    private Element getElement() {
        return menu.getElement();
    }

    private void addContextMenuDependencies(UI ui) {
        ui.getInternals().addComponentDependencies(ContextMenu.class);
    }
}
