/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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

import elemental.json.JsonArray;

/**
 * Server-side component for the {@code vaadin-message-list} element. The
 * component displays a list of messages that can be configured with
 * {@link #setItems(Collection)}.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-message-list")
@JsModule("./messageListConnector.js")
@JsModule("@vaadin/vaadin-messages/src/vaadin-message-list.js")
@NpmPackage(value = "@vaadin/vaadin-messages", version = "1.0.2")
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
