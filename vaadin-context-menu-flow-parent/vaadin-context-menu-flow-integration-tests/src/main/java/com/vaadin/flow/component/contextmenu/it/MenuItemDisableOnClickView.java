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
package com.vaadin.flow.component.contextmenu.it;

import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-context-menu/disable-on-click")
public class MenuItemDisableOnClickView extends Div {

    static String getEnabledStateChangeMessage(String itemName,
            boolean newEnabledState, int numberOfClicks) {
        return "Item " + itemName
                + " was clicked and enabled state was changed to "
                + newEnabledState + " receiving " + numberOfClicks + " clicks";
    }

    private final ContextMenu contextMenu;

    public MenuItemDisableOnClickView() {
        contextMenu = new ContextMenu();
        addItemWithDisableOnClick();
        addItemWithDisableOnClickThatEnablesInSameRoundTrip();
        addItemWithDisableAndHideOnClick();
        addItemWithDisableOnClickAndPointerEventsAuto();
        contextMenu.getItems().forEach(item -> item.setKeepOpen(true));

        var toggleKeepOpen = new NativeButton("Toggle keep open",
                click -> contextMenu.getItems()
                        .forEach(item -> item.setKeepOpen(!item.isKeepOpen())));
        toggleKeepOpen.setId("toggle-keep-open");
        add(toggleKeepOpen);

        var target = new Div("Target");
        target.setId("target-div");
        contextMenu.setTarget(target);
        add(target);
    }

    private void addItemWithDisableOnClick() {
        var disableOnClickMenuItem = contextMenu.addItem("Disabled on click",
                event -> {
                    // Triggering an action that can be started only once
                });
        disableOnClickMenuItem.setDisableOnClick(true);

        var disabledMessage = new Div();
        disabledMessage.setId("disabled-message");
        add(disabledMessage);

        var runCount = new AtomicInteger(0);
        var enable = contextMenu.addItem("Enable disabled menu item", click -> {
            disabledMessage.setText("Re-enabled item from server.");
            disableOnClickMenuItem.setEnabled(true);
            runCount.set(0);
        });

        var toggleDisableOnClick = contextMenu.addItem("Disable on click true",
                event -> {
                    disableOnClickMenuItem.setDisableOnClick(
                            !disableOnClickMenuItem.isDisableOnClick());
                    event.getSource().setText("Disable on click "
                            + disableOnClickMenuItem.isDisableOnClick());
                });
        toggleDisableOnClick.setId("toggle-menu-item");

        disableOnClickMenuItem.addClickListener(
                evt -> disabledMessage.setText(getEnabledStateChangeMessage(
                        evt.getSource().getText(), evt.getSource().isEnabled(),
                        runCount.incrementAndGet())));

        disableOnClickMenuItem.setId("disable-on-click-menu-item");
        enable.setId("enable-menu-item");
    }

    private void addItemWithDisableOnClickThatEnablesInSameRoundTrip() {
        var item = contextMenu.addItem(
                "Disabled on click and re-enabled in same round-trip",
                event -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        event.getSource().setEnabled(true);
                    }
                });
        item.setDisableOnClick(true);
        item.setId("disable-on-click-re-enable-menu-item");

        var removeReEnableInSameRoundTripItem = contextMenu.addItem(
                "Remove re-enable in same round-trip item",
                event -> contextMenu.remove(item));
        removeReEnableInSameRoundTripItem
                .setId("remove-re-enable-in-same-round-trip-menu-item");

        var addReEnableInSameRoundTripItem = contextMenu.addItem(
                "Add re-enable in same round-trip item",
                event -> contextMenu.add(item));
        addReEnableInSameRoundTripItem
                .setId("add-re-enable-in-same-round-trip-menu-item");
    }

    private void addItemWithDisableAndHideOnClick() {
        var item = contextMenu.addItem("Disabled on click and hide",
                event -> event.getSource().setVisible(false));
        item.setDisableOnClick(true);
        item.setId("disable-on-click-hidden-menu-item");

        var enableMenuItem = contextMenu.addItem("Enable hidden item and show",
                event -> {
                    item.setEnabled(true);
                    item.setVisible(true);
                });
        enableMenuItem.setId("enable-hidden-menu-item");
    }

    private void addItemWithDisableOnClickAndPointerEventsAuto() {
        var item = contextMenu.addItem("Disabled and pointer events auto");
        item.setEnabled(false);
        item.getStyle().set("pointer-events", "auto");
        item.setDisableOnClick(true);
        item.setDisableOnClick(false);
        item.setId("disable-on-click-pointer-events-auto");
    }
}
