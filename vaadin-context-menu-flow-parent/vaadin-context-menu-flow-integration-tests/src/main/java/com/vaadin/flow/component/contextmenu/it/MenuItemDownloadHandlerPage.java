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
package com.vaadin.flow.component.contextmenu.it;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

@Route("vaadin-context-menu/download-handler")
public class MenuItemDownloadHandlerPage extends Div {
    static final String DOWNLOAD_BODY = "menu-item-download-body";

    public MenuItemDownloadHandlerPage() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem item = contextMenu.addItem("Download");
        item.setId("download-item");
        item.setDownloadHandler(createDownloadHandler());

        MenuItem disableOnClick = contextMenu
                .addItem("Download, disable on click");
        disableOnClick.setId("download-disable-on-click-item");
        disableOnClick.setDisableOnClick(true);
        // Keep the menu open so the item can be checked after the click
        disableOnClick.setKeepOpen(true);
        disableOnClick.setDownloadHandler(createDownloadHandler());

        MenuItem subMenuItem = contextMenu.addItem("More").getSubMenu()
                .addItem("Download from sub menu");
        subMenuItem.setId("download-sub-menu-item");
        subMenuItem.setDownloadHandler(createDownloadHandler());

        // Adding an item regenerates the menu, which detaches and reattaches
        // the existing items
        MenuItem addItem = contextMenu.addItem("Add item",
                event -> contextMenu.addItem("Added item"));
        addItem.setId("add-item");

        Div target = new Div("Target");
        target.setId("target-div");
        contextMenu.setTarget(target);
        add(target);
    }

    private static DownloadHandler createDownloadHandler() {
        return DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(
                        DOWNLOAD_BODY.getBytes(StandardCharsets.UTF_8)),
                "menu-item.txt", "text/plain", DOWNLOAD_BODY.length()));
    }
}
