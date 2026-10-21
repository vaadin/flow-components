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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

@Route("vaadin-menu-bar/open-in-new-tab-handler")
public class MenuBarOpenInNewTabHandlerPage extends Div {
    static final String CONTENT = "menu-bar-new-tab-body";

    public MenuBarOpenInNewTabHandlerPage() {
        MenuBar menuBar = new MenuBar();
        menuBar.addItem("View").setOpenInNewTabHandler(
                DownloadHandler.fromInputStream(event -> new DownloadResponse(
                        new ByteArrayInputStream(
                                CONTENT.getBytes(StandardCharsets.UTF_8)),
                        "menu-bar.txt", "text/plain", CONTENT.length())));
        add(menuBar);
    }
}
