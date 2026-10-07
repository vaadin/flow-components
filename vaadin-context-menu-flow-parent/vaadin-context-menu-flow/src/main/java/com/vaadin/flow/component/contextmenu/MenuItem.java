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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.download.Download;
import com.vaadin.flow.function.SerializableRunnable;
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
     * The file is only served while the item is attached, visible and enabled,
     * so hiding or removing the item in a click listener makes the download
     * fail. The exception is an item with {@link #setDisableOnClick(boolean)
     * disable on click}: it disables itself before the browser requests the
     * file, so the file is also served while the item is disabled by the click.
     * Once the enabled state is set explicitly, for example with
     * {@code setEnabled(false)}, or a parent is disabled, the file is no longer
     * served while the item is disabled. To serve the file regardless of the
     * enabled state, pass {@link DownloadHandler#allowDisabled()
     * handler.allowDisabled()}.
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
        if (downloadRegistration != null) {
            downloadRegistration.remove();
            downloadRegistration = null;
        }
        this.downloadHandler = downloadHandler;
        if (downloadHandler != null) {
            downloadRegistration = Download.onClick(this,
                    getDisableOnClickController()
                            .allowDisabledByClick(downloadHandler));
        }
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
