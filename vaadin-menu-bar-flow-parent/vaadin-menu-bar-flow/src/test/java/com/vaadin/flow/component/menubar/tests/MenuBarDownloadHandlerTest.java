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

    private MenuBar menuBar;
    private MenuItem item;

    @BeforeEach
    void setup() {
        menuBar = new MenuBar();
        item = menuBar.addItem("Export");
        ui.add(menuBar);
        item.setDownloadHandler(handler);
        ui.fakeClientCommunication();
    }

    @Test
    void enabled_served() throws IOException {
        Assertions.assertTrue(requestDownload(item));
    }

    @Test
    void disabled_refused() throws IOException {
        item.setEnabled(false);
        Assertions.assertFalse(requestDownload(item));
    }

    @Test
    void hidden_refused() throws IOException {
        item.setVisible(false);
        Assertions.assertFalse(requestDownload(item));
    }

    @Test
    void menuBarDisabled_refused() throws IOException {
        menuBar.setEnabled(false);
        Assertions.assertFalse(requestDownload(item));
    }

    @Test
    void disabledByClick_served() throws IOException {
        item.setDisableOnClick(true);
        click(item);

        Assertions.assertFalse(item.isEnabled());
        Assertions.assertTrue(requestDownload(item));
    }

    @Test
    void disabledByClick_thenDisabledExplicitly_refused() throws IOException {
        item.setDisableOnClick(true);
        click(item);
        item.setEnabled(false);

        Assertions.assertFalse(requestDownload(item));
    }

    @Test
    void disabledByClick_thenMenuBarDisabled_refused() throws IOException {
        item.setDisableOnClick(true);
        click(item);
        menuBar.setEnabled(false);

        Assertions.assertFalse(requestDownload(item));
    }

    @Test
    void allowDisabled_disabled_served() throws IOException {
        item.setDownloadHandler(handler.allowDisabled());
        item.setEnabled(false);

        Assertions.assertTrue(requestDownload(item));
    }

    @Test
    void allowDisabled_hidden_refused() throws IOException {
        item.setDownloadHandler(handler.allowDisabled());
        item.setVisible(false);

        Assertions.assertFalse(requestDownload(item));
    }

    @Test
    void subMenuItem_served() throws IOException {
        MenuItem subItem = item.getSubMenu().addItem("Sub");
        MenuItem nestedItem = subItem.getSubMenu().addItem("Nested");
        nestedItem.setDownloadHandler(handler);
        ui.fakeClientCommunication();

        Assertions.assertTrue(requestDownload(nestedItem));
    }

    @Test
    void subMenuItem_disabledByClick_served() throws IOException {
        MenuItem subItem = item.getSubMenu().addItem("Sub");
        subItem.setDownloadHandler(handler);
        subItem.setDisableOnClick(true);
        ui.fakeClientCommunication();
        click(subItem);

        Assertions.assertTrue(requestDownload(subItem));
    }

    @Test
    void removed_notFound() throws IOException {
        String url = getDownloadUrl(item);
        menuBar.remove(item);
        ui.fakeClientCommunication();

        VaadinResponse response = sendRequest(url);

        Assertions.assertEquals(0, downloads.get());
        Mockito.verify(response).sendError(Mockito.eq(404),
                Mockito.anyString());
    }

    @Test
    void removed_registrationRemoved() {
        menuBar.remove(item);
        Assertions.assertEquals(0, downloadAttributes(item).count());
    }

    @Test
    void removed_handlerSet_notRegistered() {
        menuBar.remove(item);
        item.setDownloadHandler(event -> {
        });

        Assertions.assertEquals(0, downloadAttributes(item).count());
    }

    @Test
    void replacedAndCleared_onlyCurrentHandlerRegistered() {
        DownloadHandler replacement = event -> downloads.incrementAndGet();
        item.setDownloadHandler(replacement);

        Assertions.assertSame(replacement, item.getDownloadHandler());
        Assertions.assertEquals(1, downloadAttributes(item).count());

        item.setDownloadHandler(null);

        Assertions.assertNull(item.getDownloadHandler());
        Assertions.assertEquals(0, downloadAttributes(item).count());
    }

    private void click(MenuItem menuItem) {
        ComponentUtil.fireEvent(menuItem, new ClickEvent<>(menuItem, false, 0,
                0, 0, 0, 0, 0, false, false, false, false));
    }

    /**
     * Sends a request for the item's download URL through Flow's stream request
     * handler and returns whether the handler served the file.
     */
    private boolean requestDownload(MenuItem menuItem) throws IOException {
        int before = downloads.get();
        VaadinResponse response = sendRequest(getDownloadUrl(menuItem));
        boolean served = downloads.get() > before;

        // Cross-check with the response, so the test fails if a request is
        // refused for an unexpected reason, for example an unknown URL
        if (served) {
            Mockito.verify(response, Mockito.never())
                    .sendError(Mockito.anyInt(), Mockito.anyString());
        } else {
            Mockito.verify(response).sendError(403, "Resource not available");
        }
        return served;
    }

    private VaadinResponse sendRequest(String url) throws IOException {
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn("/" + url);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        new StreamRequestHandler().handleRequest(ui.getSession(), request,
                response);
        return response;
    }

    private String getDownloadUrl(MenuItem menuItem) {
        return downloadAttributes(menuItem)
                .map(menuItem.getElement()::getAttribute).findFirst()
                .orElseThrow();
    }

    // Each registered download handler is kept as a stream resource attribute
    // on the item element, so these tell which handlers the browser can still
    // download from.
    private Stream<String> downloadAttributes(MenuItem menuItem) {
        return menuItem.getElement().getAttributeNames()
                .filter(name -> name.startsWith("data-menu-item-download-"));
    }
}
