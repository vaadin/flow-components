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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.tests.MockUIExtension;

// The access rules and the registration lifecycle are covered in
// vaadin-context-menu-flow, these check that MenuBar keeps the registrations
// up to date
class MenuBarDownloadHandlerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final AtomicInteger downloads = new AtomicInteger();
    private final AtomicReference<Component> owningComponent = new AtomicReference<>();
    private final DownloadHandler handler = event -> {
        downloads.incrementAndGet();
        owningComponent.set(event.getOwningComponent());
    };

    private MenuBar menuBar;
    private MenuItem item;

    @BeforeEach
    void setup() {
        menuBar = new MenuBar();
        ui.add(menuBar);
        item = menuBar.addItem("Download");
        item.setDownloadHandler(handler);
    }

    @Test
    void served_ownedByMenuBar() throws IOException {
        Assertions.assertEquals(1, downloadAttributes().count());
        Assertions.assertTrue(requestDownload());
        Assertions.assertSame(menuBar, owningComponent.get());
    }

    @Test
    void disabled_refused() throws IOException {
        item.setEnabled(false);
        Assertions.assertFalse(requestDownload());
    }

    @Test
    void menuBarDisabled_refused() throws IOException {
        menuBar.setEnabled(false);
        Assertions.assertFalse(requestDownload());
    }

    @Test
    void disabledByClick_served() throws IOException {
        item.setDisableOnClick(true);
        ComponentUtil.fireEvent(item, new ClickEvent<>(item, false, 0, 0, 0, 0,
                0, 0, false, false, false, false));

        Assertions.assertFalse(item.isEnabled());
        Assertions.assertTrue(requestDownload());
    }

    @Test
    void subMenuItem_served() throws IOException {
        item.setDownloadHandler(null);
        menuBar.addItem("More").getSubMenu().addItem("Sub").getSubMenu()
                .addItem("Nested").setDownloadHandler(handler);

        Assertions.assertTrue(requestDownload());
    }

    @Test
    void itemRemoved_registrationRemoved() {
        menuBar.remove(item);
        Assertions.assertEquals(0, downloadAttributes().count());
    }

    @Test
    void subMenuItemRemoved_registrationRemoved() {
        item.setDownloadHandler(null);
        MenuItem parent = menuBar.addItem("More");
        MenuItem subItem = parent.getSubMenu().addItem("Sub");
        subItem.setDownloadHandler(handler);

        parent.getSubMenu().remove(subItem);

        Assertions.assertEquals(0, downloadAttributes().count());
    }

    @Test
    void replacedAndCleared_onlyCurrentHandlerRegistered() {
        DownloadHandler replacement = event -> downloads.incrementAndGet();
        item.setDownloadHandler(replacement);

        Assertions.assertSame(replacement, item.getDownloadHandler());
        Assertions.assertEquals(1, downloadAttributes().count());

        item.setDownloadHandler(null);

        Assertions.assertNull(item.getDownloadHandler());
        Assertions.assertEquals(0, downloadAttributes().count());
    }

    /**
     * Sends a request for the download URL registered on the menu bar through
     * Flow's stream request handler and returns whether the handler served the
     * file.
     */
    private boolean requestDownload() throws IOException {
        String url = downloadAttributes()
                .map(menuBar.getElement()::getAttribute).findFirst()
                .orElseThrow();
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn("/" + url);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        int before = downloads.get();
        new StreamRequestHandler().handleRequest(ui.getSession(), request,
                response);
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

    // Each registered download handler is kept as a stream resource attribute
    // on the menu bar element
    private Stream<String> downloadAttributes() {
        return menuBar.getElement().getAttributeNames()
                .filter(name -> name.startsWith("data-menu-item-download-"));
    }
}
