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
    private final AtomicReference<Component> owningComponent = new AtomicReference<>();
    private final DownloadHandler handler = event -> {
        downloads.incrementAndGet();
        owningComponent.set(event.getOwningComponent());
    };

    private Div target;
    private ContextMenu menu;
    private MenuItem item;

    @BeforeEach
    void setup() {
        target = new Div();
        ui.add(target);
        menu = new ContextMenu(target);
        item = menu.addItem("Export");
        item.setDownloadHandler(handler);
    }

    @Test
    void registeredOnTarget() {
        Assertions.assertEquals(1, downloadAttributes(target).count());
        Assertions.assertEquals(0, downloadAttributes(item).count());
    }

    @Test
    void served_ownedByTarget() throws IOException {
        Assertions.assertTrue(requestDownload());
        Assertions.assertSame(target, owningComponent.get());
    }

    @Test
    void menuClosedAndRemoved_served() throws IOException {
        openAndClose();

        Assertions.assertFalse(menu.isAttached());
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
    void menuHidden_refused() throws IOException {
        menu.setVisible(false);
        Assertions.assertFalse(requestDownload());
    }

    @Test
    void menuHidden_afterMenuClosed_refused() throws IOException {
        openAndClose();
        menu.setVisible(false);

        Assertions.assertFalse(requestDownload());
    }

    @Test
    void menuInDisabledParent_refused() throws IOException {
        Div parent = new Div(menu);
        parent.setEnabled(false);
        ui.add(parent);

        Assertions.assertFalse(requestDownload());
    }

    @Test
    void targetDisabled_served() throws IOException {
        // Clicks on the items reach the server also when the target is
        // disabled, so only the state of the item counts
        target.setEnabled(false);
        Assertions.assertTrue(requestDownload());
    }

    @Test
    void disabledByClick_afterMenuClosed_served() throws IOException {
        item.setDisableOnClick(true);
        ui.add(menu);
        ui.fakeClientCommunication();
        click(item);
        ui.remove(menu);
        ui.fakeClientCommunication();

        Assertions.assertFalse(item.isEnabled());
        Assertions.assertTrue(requestDownload());
    }

    @Test
    void disabledByClick_thenDisabledExplicitly_refused() throws IOException {
        item.setDisableOnClick(true);
        openAndClose();
        click(item);
        item.setEnabled(false);

        Assertions.assertFalse(requestDownload());
    }

    @Test
    void allowDisabled_disabled_served() throws IOException {
        item.setDownloadHandler(handler.allowDisabled());
        item.setEnabled(false);

        Assertions.assertTrue(requestDownload());
    }

    @Test
    void subMenuItem_served() throws IOException {
        item.setDownloadHandler(null);
        MenuItem nestedItem = item.getSubMenu().addItem("Sub").getSubMenu()
                .addItem("Nested");
        nestedItem.setDownloadHandler(handler);

        Assertions.assertTrue(requestDownload());
    }

    @Test
    void targetDetached_notFound() throws IOException {
        String url = getDownloadUrl();
        ui.remove(target);

        assertNotFound(url);
    }

    @Test
    void noTarget_notRegistered() {
        ContextMenu menuWithoutTarget = new ContextMenu();
        MenuItem itemWithoutTarget = menuWithoutTarget.addItem("Export");
        itemWithoutTarget.setDownloadHandler(handler);

        Assertions.assertEquals(0,
                downloadAttributes(itemWithoutTarget).count());
        Assertions.assertEquals(0,
                downloadAttributes(menuWithoutTarget).count());
    }

    @Test
    void noTarget_targetSet_registeredOnTarget() {
        ContextMenu menuWithoutTarget = new ContextMenu();
        menuWithoutTarget.addItem("Export").setDownloadHandler(handler);
        Div otherTarget = new Div();

        menuWithoutTarget.setTarget(otherTarget);

        Assertions.assertEquals(1, downloadAttributes(otherTarget).count());
    }

    @Test
    void targetChanged_registrationMoved() throws IOException {
        Div otherTarget = new Div();
        ui.add(otherTarget);

        menu.setTarget(otherTarget);

        Assertions.assertEquals(0, downloadAttributes(target).count());
        Assertions.assertEquals(1, downloadAttributes(otherTarget).count());
        Assertions.assertTrue(requestDownload(otherTarget));
        Assertions.assertSame(otherTarget, owningComponent.get());
    }

    @Test
    void targetRemoved_registrationRemoved() {
        menu.setTarget(null);
        Assertions.assertEquals(0, downloadAttributes(target).count());
    }

    @Test
    void itemRemoved_registrationRemoved() throws IOException {
        String url = getDownloadUrl();
        menu.remove(item);

        Assertions.assertEquals(0, downloadAttributes(target).count());
        assertNotFound(url);
    }

    @Test
    void parentItemRemoved_registrationRemoved() {
        item.setDownloadHandler(null);
        MenuItem parent = menu.addItem("Parent");
        parent.getSubMenu().addItem("Sub").setDownloadHandler(handler);
        Assertions.assertEquals(1, downloadAttributes(target).count());

        menu.remove(parent);

        Assertions.assertEquals(0, downloadAttributes(target).count());
    }

    @Test
    void subMenuItemRemoved_registrationRemoved() {
        item.setDownloadHandler(null);
        MenuItem parent = menu.addItem("Parent");
        MenuItem subItem = parent.getSubMenu().addItem("Sub");
        subItem.setDownloadHandler(handler);

        parent.getSubMenu().remove(subItem);

        Assertions.assertEquals(0, downloadAttributes(target).count());
    }

    @Test
    void removeAll_registrationRemoved() {
        menu.removeAll();
        Assertions.assertEquals(0, downloadAttributes(target).count());
    }

    @Test
    void itemRemoved_handlerSet_notRegistered() {
        menu.remove(item);
        item.setDownloadHandler(event -> {
        });

        Assertions.assertEquals(0, downloadAttributes(target).count());
    }

    @Test
    void itemRemovedAndAddedAgain_registered() throws IOException {
        menu.remove(item);
        menu.addComponent(item);

        Assertions.assertTrue(requestDownload());
    }

    @Test
    void contentChanged_registrationKept() {
        String url = getDownloadUrl();
        menu.addItem("Other");
        item.getSubMenu().addItem("Sub");

        Assertions.assertEquals(url, getDownloadUrl());
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

    /**
     * Adds the menu to the UI and renders its items, like opening it does, and
     * then removes it, like closing it does.
     */
    private void openAndClose() {
        ui.add(menu);
        ui.fakeClientCommunication();
        ui.remove(menu);
        ui.fakeClientCommunication();
    }

    private void click(MenuItem menuItem) {
        ComponentUtil.fireEvent(menuItem, new ClickEvent<>(menuItem, false, 0,
                0, 0, 0, 0, 0, false, false, false, false));
    }

    private boolean requestDownload() throws IOException {
        return requestDownload(target);
    }

    /**
     * Sends a request for the download URL registered on the given target
     * through Flow's stream request handler and returns whether the handler
     * served the file.
     */
    private boolean requestDownload(Component owner) throws IOException {
        int before = downloads.get();
        VaadinResponse response = sendRequest(getDownloadUrl(owner));
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

    private void assertNotFound(String url) throws IOException {
        VaadinResponse response = sendRequest(url);

        Assertions.assertEquals(0, downloads.get());
        Mockito.verify(response).sendError(Mockito.eq(404),
                Mockito.anyString());
    }

    private VaadinResponse sendRequest(String url) throws IOException {
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn("/" + url);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        new StreamRequestHandler().handleRequest(ui.getSession(), request,
                response);
        return response;
    }

    private String getDownloadUrl() {
        return getDownloadUrl(target);
    }

    private String getDownloadUrl(Component owner) {
        return downloadAttributes(owner).map(owner.getElement()::getAttribute)
                .findFirst().orElseThrow();
    }

    // Each registered download handler is kept as a stream resource attribute
    // on the element that owns the registration, so these tell which handlers
    // the browser can still download from.
    private Stream<String> downloadAttributes(Component component) {
        return component.getElement().getAttributeNames()
                .filter(name -> name.startsWith("data-menu-item-download-"));
    }
}
