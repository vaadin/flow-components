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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.tests.MockUIExtension;

class MenuBarDownloadHandlerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final AtomicInteger downloads = new AtomicInteger();
    private final DownloadHandler handler = event -> downloads
            .incrementAndGet();

    private MenuItem item;

    @BeforeEach
    void setup() {
        MenuBar menuBar = new MenuBar();
        ui.add(menuBar);
        item = menuBar.addItem("Download");
        item.setDownloadHandler(handler);
        // Menu items are attached to the UI before the response
        ui.fakeClientCommunication();
    }

    @Test
    void enabled_served() throws IOException {
        Assertions.assertTrue(requestDownload());
    }

    @Test
    void disabled_refused() throws IOException {
        item.setEnabled(false);
        Assertions.assertFalse(requestDownload());
    }

    @Test
    void disabledByClick_served() throws IOException {
        item.setDisableOnClick(true);
        click();

        Assertions.assertFalse(item.isEnabled());
        Assertions.assertTrue(requestDownload());
    }

    @Test
    void disabledByClick_thenDisabledExplicitly_refused() throws IOException {
        item.setDisableOnClick(true);
        click();
        item.setEnabled(false);

        Assertions.assertFalse(requestDownload());
    }

    @Test
    void replacedAndCleared_onlyCurrentHandlerRegistered() {
        DownloadHandler replacement = event -> downloads.incrementAndGet();
        item.setDownloadHandler(replacement);

        Assertions.assertSame(replacement, item.getDownloadHandler());
        ui.fakeClientCommunication();
        Assertions.assertEquals(1, downloadAttributes().count());

        item.setDownloadHandler(null);

        Assertions.assertNull(item.getDownloadHandler());
        ui.fakeClientCommunication();
        Assertions.assertEquals(0, downloadAttributes().count());
    }

    private void click() {
        ComponentUtil.fireEvent(item, new ClickEvent<>(item, false, 0, 0, 0, 0,
                0, 0, false, false, false, false));
    }

    /**
     * Sends a request for the item's download URL through Flow's stream request
     * handler and returns whether the handler served the file.
     */
    private boolean requestDownload() throws IOException {
        String url = downloadAttributes().map(item.getElement()::getAttribute)
                .findFirst().orElseThrow();
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn("/" + url);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        int before = downloads.get();
        new StreamRequestHandler().handleRequest(ui.getSession(), request,
                response);
        boolean served = downloads.get() > before;

        // Cross-check with the response, so the test fails if a request is
        // refused for a reason other than the enabled state
        if (served) {
            Mockito.verify(response, Mockito.never())
                    .sendError(Mockito.anyInt(), Mockito.anyString());
        } else {
            Mockito.verify(response).sendError(403, "Resource not available");
        }
        return served;
    }

    // Each registered download handler is kept as a stream resource attribute
    // on the item element
    private Stream<String> downloadAttributes() {
        return item.getElement().getAttributeNames()
                .filter(name -> name.startsWith("data-flow-download-"));
    }
}
