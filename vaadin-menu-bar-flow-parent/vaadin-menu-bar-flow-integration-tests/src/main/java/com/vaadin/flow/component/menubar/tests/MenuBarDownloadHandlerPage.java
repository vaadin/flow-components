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
package com.vaadin.flow.component.menubar.tests;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

@Route("vaadin-menu-bar/download-handler")
public class MenuBarDownloadHandlerPage extends Div {
    static final String DOWNLOAD_BODY = "menu-bar-download-body";

    public MenuBarDownloadHandlerPage() {
        MenuBar menuBar = new MenuBar();

        MenuItem rootItem = menuBar.addItem("Download");
        rootItem.setDownloadHandler(createDownloadHandler());

        MenuItem subMenuItem = menuBar.addItem("More").getSubMenu()
                .addItem("Download from sub menu");
        subMenuItem.setDownloadHandler(createDownloadHandler());

        add(menuBar);
    }

    private static DownloadHandler createDownloadHandler() {
        return DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(
                        DOWNLOAD_BODY.getBytes(StandardCharsets.UTF_8)),
                "menu-bar.txt", "text/plain", DOWNLOAD_BODY.length()));
    }
}
