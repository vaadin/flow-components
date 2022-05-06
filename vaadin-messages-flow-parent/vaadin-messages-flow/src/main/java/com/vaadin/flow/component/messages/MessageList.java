/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.internal.JsonUtils;

import elemental.json.JsonArray;

/**
 * Message List allows you to show a list of messages, for example, a chat log.
 * You can configure the text content, information about the sender and the time
 * of sending for each message. The component displays a list of messages that
 * can be configured with {@link #setItems(Collection)}.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-message-list")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("./messageListConnector.js")
@JsModule("@vaadin/message-list/src/vaadin-message-list.js")
@NpmPackage(value = "@vaadin/message-list", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-messages", version = "23.1.0-beta1")
public class MessageList extends Component
        implements HasStyle, HasSize, LocaleChangeObserver {

    private List<MessageListItem> items = Collections.emptyList();
    private boolean pendingUpdate = false;

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
     * Gets the items that are rendered as message components in this message
     * list.
     *
     * @return an unmodifiable view of the list of items
     */
    public List<MessageListItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    void scheduleItemsUpdate() {
        if (!pendingUpdate) {
            pendingUpdate = true;
            getElement().getNode().runWhenAttached(
                    ui -> ui.beforeClientResponse(this, ctx -> {
                        JsonArray itemsJson = JsonUtils.listToJson(items);
                        getElement().executeJs(
                                "window.Vaadin.Flow.messageListConnector"
                                        + ".setItems(this, $0, $1)",
                                itemsJson, ui.getLocale().toLanguageTag());
                        pendingUpdate = false;
                    }));
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        scheduleItemsUpdate();
    }
}
