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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

@Route("vaadin-button/download-handler")
public class DownloadHandlerButtonPage extends Div {
    static final String DOWNLOAD_BODY = "button-download-body";

    public DownloadHandlerButtonPage() {
        Button button = new Button("Download");
        button.setId("download-button");
        button.setDownloadHandler(createDownloadHandler());

        Button disableOnClick = new Button("Download, disable on click");
        disableOnClick.setId("download-disable-on-click-button");
        disableOnClick.setDisableOnClick(true);
        disableOnClick.setDownloadHandler(createDownloadHandler());

        Button explicitlyDisabled = new Button(
                "Download, disable on click, disabled by listener");
        explicitlyDisabled.setId("download-explicitly-disabled-button");
        explicitlyDisabled.setDisableOnClick(true);
        explicitlyDisabled.setDownloadHandler(createDownloadHandler());
        explicitlyDisabled
                .addClickListener(event -> event.getSource().setEnabled(false));

        Button parentDisabled = new Button(
                "Download, disable on click, parent disabled by listener");
        parentDisabled.setId("download-parent-disabled-button");
        parentDisabled.setDisableOnClick(true);
        parentDisabled.setDownloadHandler(createDownloadHandler());
        Div parent = new Div(parentDisabled);
        parentDisabled.addClickListener(event -> parent.setEnabled(false));

        add(button, disableOnClick, explicitlyDisabled, parent);
    }

    private static DownloadHandler createDownloadHandler() {
        return DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(
                        DOWNLOAD_BODY.getBytes(StandardCharsets.UTF_8)),
                "button.txt", "text/plain", DOWNLOAD_BODY.length()));
    }
}
