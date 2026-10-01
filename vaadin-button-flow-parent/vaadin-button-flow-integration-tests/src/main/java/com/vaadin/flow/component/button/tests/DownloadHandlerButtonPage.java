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
import com.vaadin.flow.component.shared.DisableOnClickMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

@Route("vaadin-button/download-handler")
public class DownloadHandlerButtonPage extends Div {

    static final String BODY = "button-download-body";

    public DownloadHandlerButtonPage() {
        Button download = Button.forDownload("Download", createHandler());
        download.setId("download");

        Button disableOnClick = new Button("Download and disable");
        disableOnClick.setId("download-disable-on-click");
        disableOnClick.setDisableOnClick(DisableOnClickMode.UNTIL_ENABLED);
        disableOnClick.setDownloadHandler(createHandler().allowDisabled());

        add(download, disableOnClick);
    }

    private static DownloadHandler createHandler() {
        return DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(BODY.getBytes(StandardCharsets.UTF_8)),
                "button.txt", "text/plain", BODY.length()));
    }
}
