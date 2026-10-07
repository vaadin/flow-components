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
package com.vaadin.flow.component.contextmenu;

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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.tests.MockUIExtension;

class MenuItemDownloadHandlerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final AtomicInteger downloads = new AtomicInteger();
    private final DownloadHandler handler = event -> downloads
            .incrementAndGet();

    private Div target;
    private ContextMenu contextMenu;
    private MenuItem item;

    @BeforeEach
    void setup() {
        target = new Div();
        ui.add(target);
        contextMenu = new ContextMenu(target);
        item = contextMenu.addItem("Download");
        item.setDownloadHandler(handler);
        // Opening the menu adds it to the UI
        ui.add(contextMenu);
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
    void hidden_refused() throws IOException {
        item.setVisible(false);
        Assertions.assertFalse(requestDownload());
    }

    @Test
    void menuClosed_served() throws IOException {
        // Closing the menu removes it from the UI, which can happen before
        // the browser requests the file
        contextMenu.removeFromParent();
        ui.fakeClientCommunication();

        Assertions.assertTrue(requestDownload());
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
        Assertions.assertEquals(1, downloadAttributes(target).count());

        item.setDownloadHandler(null);

        Assertions.assertNull(item.getDownloadHandler());
        Assertions.assertEquals(0, downloadAttributes(target).count());
    }

    @Test
    void itemRemoved_unregistered() {
        contextMenu.remove(item);
        Assertions.assertEquals(0, downloadAttributes(target).count());
    }

    @Test
    void parentItemRemoved_subMenuItemUnregistered() {
        MenuItem parent = contextMenu.addItem("More");
        parent.getSubMenu().addItem("Download from sub menu")
                .setDownloadHandler(handler);
        Assertions.assertEquals(2, downloadAttributes(target).count());

        contextMenu.remove(parent);

        Assertions.assertEquals(1, downloadAttributes(target).count());
    }

    @Test
    void targetChanged_registeredForNewTarget() throws IOException {
        Div newTarget = new Div();
        ui.add(newTarget);
        contextMenu.setTarget(newTarget);

        Assertions.assertEquals(0, downloadAttributes(target).count());
        target = newTarget;
        Assertions.assertTrue(requestDownload());
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
        String url = downloadAttributes(target)
                .map(target.getElement()::getAttribute).findFirst()
                .orElseThrow();
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn("/" + url);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        int before = downloads.get();
        new StreamRequestHandler().handleRequest(ui.getSession(), request,
                response);
        boolean served = downloads.get() > before;

        // Cross-check with the response, so the test fails if a request is
        // refused for a reason other than the state of the item
        if (served) {
            Mockito.verify(response, Mockito.never())
                    .sendError(Mockito.anyInt(), Mockito.anyString());
        } else {
            Mockito.verify(response).sendError(403, "Resource not available");
        }
        return served;
    }

    // The download handlers of the menu items are kept as stream resource
    // attributes on the target element
    private static Stream<String> downloadAttributes(Div target) {
        return target.getElement().getAttributeNames()
                .filter(name -> name.startsWith("data-menu-item-download-"));
    }
}
