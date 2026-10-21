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
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.server.streams.AbstractDownloadHandler;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;

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
    private DownloadHandler openInNewTabHandler;

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
     * one, and {@code null} stops the item from starting a download. Setting a
     * non-null download handler removes the handler set with
     * {@link #setOpenInNewTabHandler(DownloadHandler)}.
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
     * The file is only served while the item is in its menu, visible and
     * enabled, so hiding or removing the item in a click listener makes the
     * download fail. The exception is an item with
     * {@link #setDisableOnClick(boolean) disable on click}: it disables itself
     * before the browser requests the file, so the file is also served while
     * the item is disabled by the click. Once the enabled state is set
     * explicitly, for example with {@code setEnabled(false)}, or the menu is
     * disabled, the file is no longer served while the item is disabled. To
     * serve the file regardless of the enabled state, pass
     * {@link DownloadHandler#allowDisabled() handler.allowDisabled()}.
     * <p>
     * For an item of a {@link ContextMenu}, the file is served on behalf of the
     * target of the menu, so the download only works while the menu has a
     * target that is attached. Closing the menu does not affect the download.
     * The URL of the file stays valid while the target is attached, also when
     * the menu is closed, so hide or disable the item to stop serving the file.
     * The {@link DownloadEvent#getOwningComponent() owning component} of the
     * download is the target of the menu.
     * <p>
     * For an item of a {@code MenuBar}, the file is served on behalf of the
     * item itself, so the download only works while the menu bar is attached.
     * The {@link DownloadEvent#getOwningComponent() owning component} of the
     * download is the item.
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
        this.downloadHandler = downloadHandler;
        if (downloadHandler != null) {
            this.openInNewTabHandler = null;
        }
        updateDownloadManager();
    }

    /**
     * Gets the handler that produces the content opened in a new browser tab
     * when the item is clicked.
     *
     * @return the handler, or {@code null} if clicking the item does not open a
     *         new tab
     * @see #setOpenInNewTabHandler(DownloadHandler)
     * @since 25.4
     */
    public DownloadHandler getOpenInNewTabHandler() {
        return openInNewTabHandler;
    }

    /**
     * Sets a handler that produces content to show in a new browser tab when
     * the item is clicked, for example a PDF or an image. The tab opens in the
     * browser as part of the click, so popup blockers let it through, and click
     * listeners still run as usual. Setting a new handler replaces the previous
     * one, and {@code null} stops the item from opening a new tab. Setting a
     * non-null handler removes the handler set with
     * {@link #setDownloadHandler(DownloadHandler)}.
     * <p>
     * The handler runs only when the new tab requests the content, once per
     * click. The browser shows the content when it can, otherwise it downloads
     * it. The built-in handlers, such as
     * {@link DownloadHandler#forFile(java.io.File)}, are switched to serve the
     * content inline. This changes the given handler instance, so it also
     * serves inline wherever else it is used. A custom handler should call
     * {@link DownloadEvent#inline(String)} itself, since content served as an
     * attachment is downloaded instead of shown.
     * <p>
     * The content is served under the same conditions, and on behalf of the
     * same component, as described in
     * {@link #setDownloadHandler(DownloadHandler)}. To serve the content
     * regardless of the enabled state, call
     * {@link AbstractDownloadHandler#inline() inline()} on a built-in handler
     * before {@link DownloadHandler#allowDisabled() allowDisabled()}, for
     * example {@code DownloadHandler.forFile(file).inline().allowDisabled()}.
     *
     * <pre>{@code
     * MenuItem view = menu.addItem("View invoice");
     * view.setOpenInNewTabHandler(DownloadHandler.forFile(invoicePdf));
     * }</pre>
     *
     * @param openInNewTabHandler
     *            the handler that produces the content, or {@code null} to not
     *            open a new tab on click
     * @since 25.4
     */
    public void setOpenInNewTabHandler(DownloadHandler openInNewTabHandler) {
        if (openInNewTabHandler instanceof AbstractDownloadHandler<?> builtIn) {
            // The menu wraps the handler, which OpenInNewTab can not switch
            // to inline
            builtIn.inline();
        }
        this.openInNewTabHandler = openInNewTabHandler;
        if (openInNewTabHandler != null) {
            this.downloadHandler = null;
        }
        updateDownloadManager();
    }

    private void updateDownloadManager() {
        MenuDownloadManager downloadManager = getMenuDownloadManager();
        if (downloadManager != null) {
            downloadManager.update();
        }
    }

    /**
     * Gets the manager for the download registrations of the menu that this
     * item belongs to.
     * <p>
     * For internal use only. May be renamed or removed in a future release.
     *
     * @return the manager for the download registrations, or {@code null} if
     *         the item does not belong to a menu
     */
    protected MenuDownloadManager getMenuDownloadManager() {
        return getContextMenu() != null ? getContextMenu().getDownloadManager()
                : null;
    }
}
