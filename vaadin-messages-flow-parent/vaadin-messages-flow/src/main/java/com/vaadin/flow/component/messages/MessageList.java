/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.internal.JsonUtils;

/**
 * Message List allows you to show a list of messages, for example, a chat log.
 * You can configure the text content, information about the sender and the time
 * of sending for each message. The component displays a list of messages that
 * can be configured with {@link #setItems(Collection)}.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-message-list")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("./messageListConnector.js")
@JsModule("@vaadin/message-list/src/vaadin-message-list.js")
@NpmPackage(value = "@vaadin/message-list", version = "24.8.0-alpha18")
public class MessageList extends Component
        implements HasStyle, HasSize, LocaleChangeObserver {

    private List<MessageListItem> items = new ArrayList<>();
    private boolean pendingUpdate = false;
    private boolean pendingTextUpdate = false;
    private Integer pendingAddItemsIndex;

    private final String CONNECTOR_OBJECT = "window.Vaadin.Flow.messageListConnector";

    /**
     * Creates a new message list component. To populate the content of the
     * list, use {@link #setItems(Collection)}.
     */
    public MessageList() {
    }

    /**
     * Creates a new message list component, with the provided items rendered as
     * messages.
     *
     * @param items
     *            the items to render as messages
     * @see #setItems(Collection)
     */
    public MessageList(Collection<MessageListItem> items) {
        setItems(items);
    }

    /**
     * Creates a new message list component, with the provided items rendered as
     * messages.
     *
     * @param items
     *            the items to render as messages
     * @see #setItems(MessageListItem...)
     */
    public MessageList(MessageListItem... items) {
        setItems(items);
    }

    /**
     * Sets the items that will be rendered as messages in this message list.
     *
     * @param items
     *            the items to set, not {@code null} and not containing any
     *            {@code null} items
     */
    public void setItems(Collection<MessageListItem> items) {
        Objects.requireNonNull(items,
                "Can't set null item collection to MessageList.");
        items.forEach(item -> Objects.requireNonNull(item,
                "Can't include null items in MessageList."));

        this.items.forEach(item -> item.setHost(null));

        this.items = new ArrayList<>(items);
        items.forEach(item -> item.setHost(this));
        scheduleItemsUpdate();
    }

    /**
     * Sets the items that will be rendered as messages in this message list.
     *
     * @param items
     *            the items to set, none of which can be {@code null}
     */
    public void setItems(MessageListItem... items) {
        setItems(Arrays.asList(items));
    }

    /**
     * Adds a single item to be rendered as a message at the end of this message
     * list.
     *
     * @param item
     *            the item to add, not {@code null}
     */
    public void addItem(MessageListItem item) {
        Objects.requireNonNull(item, "Can't add null item to MessageList.");

        item.setHost(this);
        items.add(item);
        scheduleAddItemsUpdate();
    }

    /**
     * Gets the items that are rendered as message components in this message
     * list.
     *
     * @return an unmodifiable view of the list of items
     */
    public List<MessageListItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Schedules an incremental update of the message list items' text content.
     */
    void scheduleItemsTextUpdate() {
        scheduleUpdate();
        // Avoid multiple updateClient() calls even though all but the first one
        // are NOPs (untestable flag)
        pendingTextUpdate = true;
    }

    void scheduleAddItemsUpdate() {
        scheduleUpdate();
        if (pendingAddItemsIndex == null) {
            pendingAddItemsIndex = items.size() - 1;
        }
    }

    /**
     * Schedules a full update of the message list items.
     */
    void scheduleItemsUpdate() {
        scheduleUpdate();
        pendingUpdate = true;
    }

    /**
     * Schedules a client sync of the message list items to be run before the
     * next client response.
     */
    private void scheduleUpdate() {
        if (pendingUpdate || pendingTextUpdate
                || pendingAddItemsIndex != null) {
            // Already scheduled
            return;
        }

        // Schedule update before the next client response
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, ctx -> updateClient(ui)));
    }

    /**
     * Updates the client with the current state of the message list items.
     * 
     * @param ui
     *            the UI the component is attached to
     */
    private void updateClient(UI ui) {
        if (pendingUpdate) {
            handleFullUpdate(ui);
        } else {
            // Incremental updates

            // Check if we need to add new items
            handleAddItemsUpdate(ui);

            // Check for text updates if not a full update
            handleTextUpdates();
        }

        // Reset flags for the next update cycle
        pendingTextUpdate = false;
        pendingUpdate = false;
        pendingAddItemsIndex = null;
    }

    /**
     * Handles a full update of the message list items.
     * 
     * @param ui
     *            the UI the component is attached to
     */
    private void handleFullUpdate(UI ui) {
        // Sync clientText for items
        items.forEach(item -> item.clientText = item.getText());

        var itemsJson = JsonUtils.listToJson(items);
        getElement().executeJs(CONNECTOR_OBJECT + ".setItems(this, $0, $1)",
                itemsJson, ui.getLocale().toLanguageTag());
    }

    /**
     * Handles incremental updates of the message list items' text content. This
     * may involve appending text to existing text or replacing it entirely.
     */
    private void handleTextUpdates() {
        items.forEach(item -> {
            // Check if text needs updating for this item
            var textChanged = !Objects.equals(item.getText(), item.clientText);

            if (textChanged) {
                if (item.getText() != null && item.clientText != null
                        && item.getText().startsWith(item.clientText)) {
                    // Append optimization
                    var diff = item.getText()
                            .substring(item.clientText.length());
                    getElement().executeJs(
                            CONNECTOR_OBJECT + ".appendItemText(this, $0, $1)",
                            diff, items.indexOf(item));
                } else {
                    // Full text update for this item
                    getElement().executeJs(
                            CONNECTOR_OBJECT + ".setItemText(this, $0, $1)",
                            item.getText(), items.indexOf(item));
                }
                // Sync clientText *after* sending the update
                item.clientText = item.getText();
            }
        });
    }

    private void handleAddItemsUpdate(UI ui) {
        if (pendingAddItemsIndex == null) {
            return;
        }

        var newItems = items.subList(pendingAddItemsIndex, items.size());
        // Sync clientText for new items
        newItems.forEach(item -> item.clientText = item.getText());

        var newItemsJson = JsonUtils.listToJson(newItems);
        // Call the connector function to add items
        getElement().executeJs(CONNECTOR_OBJECT + ".addItems(this, $0, $1)",
                newItemsJson, ui.getLocale().toLanguageTag());
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        scheduleItemsUpdate();
    }

    /**
     * Sets whether the messages should be parsed as markdown. By default, this
     * is set to {@code false}.
     *
     * @param markdown
     *            {@code true} if the message text is parsed as Markdown.
     */
    public void setMarkdown(boolean markdown) {
        getElement().setProperty("markdown", markdown);
    }

    /**
     * Returns whether the messages are parsed as markdown.
     *
     * @return {@code true} if the message text is parsed as Markdown.
     */
    public boolean isMarkdown() {
        return getElement().getProperty("markdown", false);
    }
}
