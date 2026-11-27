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
package com.vaadin.flow.component.button.tests;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.download.DownloadTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

@Route("vaadin-button/download-handler")
public class DownloadHandlerButtonPage extends Div {
    static final String DOWNLOAD_BODY = "button-download-body";

    public DownloadHandlerButtonPage() {
        // One handler for all buttons, so the tests also check that showing
        // it in a new tab doesn't change how the other buttons serve it
        DownloadHandler handler = createDownloadHandler();

        Button button = new Button("Download");
        button.setId("download-button");
        button.setDownloadHandler(handler);

        Button disableOnClick = new Button("Download, disable on click");
        disableOnClick.setId("download-disable-on-click-button");
        disableOnClick.setDisableOnClick(true);
        disableOnClick.setDownloadHandler(handler);

        Button newTab = new Button("View", handler, DownloadTarget.NEW_TAB);
        newTab.setId("new-tab-button");

        Button newTabDisableOnClick = new Button("View, disable on click");
        newTabDisableOnClick.setId("new-tab-disable-on-click-button");
        newTabDisableOnClick.setDisableOnClick(true);
        newTabDisableOnClick.setDownloadHandler(handler);
        newTabDisableOnClick.setDownloadTarget(DownloadTarget.NEW_TAB);

        // A custom handler that leaves inline or attachment to the button,
        // behind the plain wrapper that allowDisabled() returns
        DownloadHandler custom = event -> {
            event.fileName("custom.txt");
            event.getOutputStream()
                    .write(DOWNLOAD_BODY.getBytes(StandardCharsets.UTF_8));
        };
        Button newTabCustomAllowDisabled = new Button("View, custom handler",
                custom.allowDisabled(), DownloadTarget.NEW_TAB);
        newTabCustomAllowDisabled.setId("new-tab-custom-allow-disabled-button");

        add(button, disableOnClick, newTab, newTabDisableOnClick,
                newTabCustomAllowDisabled);
    }

    private static DownloadHandler createDownloadHandler() {
        return DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(
                        DOWNLOAD_BODY.getBytes(StandardCharsets.UTF_8)),
                "button.txt", "text/plain", DOWNLOAD_BODY.length()));
    }
}
