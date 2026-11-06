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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.download.Download;
import com.vaadin.flow.component.newtab.OpenInNewTab;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.server.HttpStatusCode;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.shared.Registration;

/**
 * Manages the download registrations of the items of a menu, including the
 * items of its sub menus. This covers both the download handler and the open in
 * new tab handler of an item. The menu calls {@link #update()} whenever its
 * items, or the handlers of an item, change, or whenever the component that
 * owns the registrations changes, for example the target of a context menu.
 * <p>
 * The download registration of an item is kept on a component that stays
 * attached while the item can be clicked: the target for a context menu, and
 * the item itself for a menu bar. Removed items are no longer reachable from
 * the menu, so this remembers the items that have a registration to be able to
 * remove it.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public class MenuDownloadManager implements Serializable {

    private final Component menu;
    private final SerializableSupplier<List<MenuItem>> rootItems;
    private final SerializableFunction<MenuItem, Component> ownerProvider;
    private Map<MenuItem, ItemRegistration> registrations = new HashMap<>();

    /**
     * Creates a manager for the download registrations of a menu.
     *
     * @param menu
     *            the menu that the items belong to, not {@code null}
     * @param rootItems
     *            supplies the root level items of the menu, not {@code null}
     * @param ownerProvider
     *            returns the component that owns the download registration of
     *            the given item, or {@code null} if such a component does not
     *            exist yet
     */
    public MenuDownloadManager(Component menu,
            SerializableSupplier<List<MenuItem>> rootItems,
            SerializableFunction<MenuItem, Component> ownerProvider) {
        this.menu = Objects.requireNonNull(menu);
        this.rootItems = Objects.requireNonNull(rootItems);
        this.ownerProvider = Objects.requireNonNull(ownerProvider);
    }

    /**
     * Registers the download handlers of the items in the menu on the component
     * that currently owns their registration, and removes the registrations of
     * items that are no longer in the menu or no longer have a download
     * handler.
     */
    public void update() {
        List<MenuItem> items = new ArrayList<>();
        collectDownloadItems(rootItems.get(), items);

        Map<MenuItem, ItemRegistration> previous = registrations;
        registrations = new HashMap<>();
        for (MenuItem item : items) {
            Component owner = ownerProvider.apply(item);
            boolean openInNewTab = item.getOpenInNewTabHandler() != null;
            DownloadHandler handler = openInNewTab
                    ? item.getOpenInNewTabHandler()
                    : item.getDownloadHandler();
            ItemRegistration registration = previous.remove(item);
            if (registration != null && (registration.owner() != owner
                    || registration.handler() != handler
                    || registration.openInNewTab() != openInNewTab)) {
                registration.registration().remove();
                registration = null;
            }
            if (registration == null && owner != null) {
                registration = register(item, handler, openInNewTab, owner);
            }
            if (registration != null) {
                registrations.put(item, registration);
            }
        }
        // The items that are no longer in the menu or no longer have a
        // handler
        previous.values()
                .forEach(registration -> registration.registration().remove());
    }

    private static void collectDownloadItems(List<MenuItem> items,
            List<MenuItem> downloadItems) {
        for (MenuItem item : items) {
            if (item.getDownloadHandler() != null
                    || item.getOpenInNewTabHandler() != null) {
                downloadItems.add(item);
            }
            if (item.isParentItem()) {
                collectDownloadItems(item.getSubMenu().getItems(),
                        downloadItems);
            }
        }
    }

    private ItemRegistration register(MenuItem item, DownloadHandler handler,
            boolean openInNewTab, Component owner) {
        Element ownerElement = owner.getElement();
        StreamResourceRegistry.ElementStreamResource resource = new StreamResourceRegistry.ElementStreamResource(
                new ItemDownloadHandler(item, handler), ownerElement);
        String attribute = "data-menu-item-download-" + resource.getId();
        ownerElement.setAttribute(attribute, resource);
        String url = StreamResourceRegistry.getURI(resource).toASCIIString();
        Registration clickRegistration = openInNewTab
                ? OpenInNewTab.onClick(item, url)
                : Download.onClick(item, url);
        return new ItemRegistration(owner, handler, openInNewTab, () -> {
            clickRegistration.remove();
            ownerElement.removeAttribute(attribute);
        });
    }

    private boolean isDownloadAllowed(MenuItem item, DownloadHandler handler) {
        Element element = item.getElement();
        Element menuElement = menu.getElement();
        if (!element.getNode().isVisible()
                || !menuElement.getNode().isVisible()) {
            return false;
        }
        if ((element.isEnabled() && menuElement.isEnabled()) || handler
                .getDisabledUpdateMode() == DisabledUpdateMode.ALWAYS) {
            return true;
        }
        // Disable on click disables the item before the browser requests the
        // file, so that should not prevent the download
        return item.isDisabledByClick() && menuElement.isEnabled();
    }

    private record ItemRegistration(Component owner, DownloadHandler handler,
            boolean openInNewTab,
            Registration registration) implements Serializable {
    }

    /**
     * Serves the file only while the item allows it. By default,
     * DownloadHandler checks the state of the component that owns the download
     * registrations, however for menu items we want to check the state of the
     * item itself.
     */
    private class ItemDownloadHandler implements DownloadHandler {
        private final MenuItem item;
        private final DownloadHandler delegate;

        private ItemDownloadHandler(MenuItem item, DownloadHandler delegate) {
            this.item = item;
            this.delegate = delegate;
        }

        @Override
        public void handleRequest(VaadinRequest request,
                VaadinResponse response, VaadinSession session, Element owner)
                throws IOException {
            boolean allowed;
            session.lock();
            try {
                allowed = isDownloadAllowed(item, delegate);
            } finally {
                session.unlock();
            }
            if (!allowed) {
                response.sendError(HttpStatusCode.FORBIDDEN.getCode(),
                        "Resource not available");
                return;
            }
            delegate.handleRequest(request, response, session, owner);
        }

        @Override
        public void handleDownloadRequest(DownloadEvent event)
                throws IOException {
            delegate.handleDownloadRequest(event);
        }

        @Override
        public String getUrlPostfix() {
            return delegate.getUrlPostfix();
        }

        @Override
        public boolean isAllowInert() {
            return delegate.isAllowInert();
        }

        @Override
        public DisabledUpdateMode getDisabledUpdateMode() {
            return DisabledUpdateMode.ALWAYS;
        }
    }
}
