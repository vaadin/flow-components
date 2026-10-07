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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.download.Download;
import com.vaadin.flow.component.shared.internal.DownloadUtil;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.server.HttpStatusCode;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.shared.Registration;

/**
 * Item component used inside {@link ContextMenu} and {@link SubMenu}. This
 * component can be created and added to a menu overlay with
 * {@link HasMenuItems#addItem(String, ComponentEventListener)} and similar
 * methods.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class MenuItem extends MenuItemBase<ContextMenu, MenuItem, SubMenu>
        implements ClickNotifier<MenuItem> {

    private final SerializableRunnable contentReset;
    private DownloadHandler downloadHandler;
    private Registration downloadRegistration;
    private Component downloadTarget;

    public MenuItem(ContextMenu contextMenu,
            SerializableRunnable contentReset) {
        super(contextMenu, contentReset);
        this.contentReset = contentReset;
    }

    @Override
    protected SubMenu createSubMenu() {
        return new SubMenu(this, contentReset);
    }

    /**
     * Gets the handler that produces the file downloaded when the item is
     * clicked.
     *
     * @return the download handler, or {@code null} if clicking the item does
     *         not start a download
     * @see #setDownloadHandler(DownloadHandler)
     * @since 25.4
     */
    public DownloadHandler getDownloadHandler() {
        return downloadHandler;
    }

    /**
     * Sets a handler that produces a file to download when the item is clicked.
     * The download starts in the browser as part of the click, and click
     * listeners still run as usual. Setting a new handler replaces the previous
     * one, and {@code null} stops the item from starting a download.
     * <p>
     * The handler runs only when the browser requests the file, once per click,
     * so you can create the content, file name and response headers at that
     * point. If the handler fails, the browser reports a failed download
     * instead of saving an empty file. Use the transfer callbacks of the
     * handler, for example
     * {@link com.vaadin.flow.server.streams.TransferProgressAwareHandler#whenComplete(com.vaadin.flow.function.SerializableConsumer)
     * whenComplete}, to react on the server when the transfer has finished or
     * failed.
     * <p>
     * The file is only served while the item is part of its menu, visible and
     * enabled, so hiding or removing the item in a click listener makes the
     * download fail. Closing the menu does not affect the download. The
     * exception is an item with {@link #setDisableOnClick(boolean) disable on
     * click}: it disables itself before the browser requests the file, so the
     * file is also served while the item is disabled by the click. Once the
     * enabled state is set explicitly, for example with
     * {@code setEnabled(false)}, or a parent is disabled, the file is no longer
     * served while the item is disabled. To serve the file regardless of the
     * enabled state, pass {@link DownloadHandler#allowDisabled()
     * handler.allowDisabled()}.
     * <p>
     * For an item in a {@link ContextMenu} that has a target, the
     * {@link DownloadEvent#getOwningComponent() owning component} of the
     * download is the target of the menu.
     *
     * <pre>{@code
     * MenuItem export = menu.addItem("Export");
     * export.setDownloadHandler(
     *         DownloadHandler.fromInputStream(event -> createReport()));
     * }</pre>
     *
     * @param downloadHandler
     *            the handler that produces the file, or {@code null} to not
     *            start a download on click
     * @since 25.4
     */
    public void setDownloadHandler(DownloadHandler downloadHandler) {
        removeDownload();
        this.downloadHandler = downloadHandler;
        if (getContextMenu() != null) {
            getContextMenu().updateDownloads();
        } else if (downloadHandler != null) {
            registerDownload(null);
        }
    }

    /**
     * Registers the download handler of this item for the given target, unless
     * it is already registered for it.
     * <p>
     * A context menu removes itself from the UI when it closes, which can
     * happen before the browser requests the file. So for a menu with a target,
     * the file is served on behalf of the target, which stays attached, instead
     * of on behalf of the item.
     *
     * @param target
     *            the target of the context menu, or {@code null} to serve the
     *            file on behalf of the item
     */
    void registerDownload(Component target) {
        if (downloadRegistration != null && downloadTarget == target) {
            return;
        }
        removeDownload();
        DownloadHandler handler = DownloadUtil.allowDisabledByClick(this,
                getDisableOnClickController(), downloadHandler);
        downloadRegistration = target == null ? Download.onClick(this, handler)
                : registerDownloadOnTarget(target, handler);
        downloadTarget = target;
    }

    void removeDownload() {
        if (downloadRegistration != null) {
            downloadRegistration.remove();
            downloadRegistration = null;
            downloadTarget = null;
        }
    }

    private Registration registerDownloadOnTarget(Component target,
            DownloadHandler handler) {
        Element targetElement = target.getElement();
        StreamResourceRegistry.ElementStreamResource resource = new StreamResourceRegistry.ElementStreamResource(
                createTargetDownloadHandler(handler), targetElement);
        // The resource is registered while the attribute is set and the target
        // is attached
        String attribute = "data-menu-item-download-" + resource.getId();
        targetElement.setAttribute(attribute, resource);
        Registration clickRegistration = Download.onClick(this,
                StreamResourceRegistry.getURI(resource).toASCIIString());
        return () -> {
            clickRegistration.remove();
            targetElement.removeAttribute(attribute);
        };
    }

    /**
     * Wraps the given handler so that it also checks the state of this item, as
     * only the state of the target is checked before serving the file.
     */
    private DownloadHandler createTargetDownloadHandler(
            DownloadHandler handler) {
        DisabledUpdateMode targetDisabledUpdateMode = downloadHandler
                .getDisabledUpdateMode();
        return new DownloadHandler() {
            @Override
            public void handleRequest(VaadinRequest request,
                    VaadinResponse response, VaadinSession session,
                    Element owner) throws IOException {
                boolean available;
                session.lock();
                try {
                    available = isDownloadAvailable(handler);
                } finally {
                    session.unlock();
                }
                if (!available) {
                    response.sendError(HttpStatusCode.FORBIDDEN.getCode(),
                            "Resource not available");
                    return;
                }
                handler.handleRequest(request, response, session, owner);
            }

            @Override
            public void handleDownloadRequest(DownloadEvent event)
                    throws IOException {
                handler.handleDownloadRequest(event);
            }

            @Override
            public String getUrlPostfix() {
                return handler.getUrlPostfix();
            }

            @Override
            public boolean isAllowInert() {
                return handler.isAllowInert();
            }

            @Override
            public DisabledUpdateMode getDisabledUpdateMode() {
                return targetDisabledUpdateMode;
            }
        };
    }

    private boolean isDownloadAvailable(DownloadHandler handler) {
        Element element = getElement();
        return element.getNode().isVisible() && (element.isEnabled() || handler
                .getDisabledUpdateMode() == DisabledUpdateMode.ALWAYS);
    }

    /**
     * Sets the menu item explicitly disabled or enabled. When disabled, menu
     * items are rendered as "dimmed".
     * <p>
     * By default, disabled items are not focusable and don't react to hover. As
     * a result, they are hidden from assistive technologies, and it's not
     * possible to show a tooltip to explain why they are disabled. This can be
     * addressed by enabling the feature flag
     * {@code accessibleDisabledMenuItems}, which makes disabled items focusable
     * and hoverable, while still preventing them from being activated. To
     * enable this feature flag, add the following line to
     * {@code src/main/resources/vaadin-featureflags.properties}:
     *
     * <pre>
     * com.vaadin.experimental.accessibleDisabledMenuItems = true
     * </pre>
     * 
     * @since 25.2
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

}
