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
package com.vaadin.flow.component.messages;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

import tools.jackson.databind.node.ArrayNode;

/**
 * The JavaScript that {@link MessageList} runs on its element in the browser,
 * for {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
interface MessageListJs extends JsInvokers {

    /**
     * Replaces all items of the message list.
     *
     * @param items
     *            the items
     * @param locale
     *            the language tag of the locale used to format the item times
     */
    @JsExpression("window.Vaadin.Flow.messageListConnector.setItems(this, $0, $1)")
    void setItems(ArrayNode items, String locale);

    /**
     * Adds items to the end of the message list.
     *
     * @param items
     *            the items to add
     * @param locale
     *            the language tag of the locale used to format the item times
     */
    @JsExpression("window.Vaadin.Flow.messageListConnector.addItems(this, $0, $1)")
    void addItems(ArrayNode items, String locale);

    /**
     * Replaces the text of an item.
     *
     * @param text
     *            the new text, or {@code null}
     * @param index
     *            the index of the item
     */
    @JsExpression("window.Vaadin.Flow.messageListConnector.setItemText(this, $0, $1)")
    void setItemText(String text, int index);

    /**
     * Appends text to the text of an item.
     *
     * @param text
     *            the text to append
     * @param index
     *            the index of the item
     */
    @JsExpression("window.Vaadin.Flow.messageListConnector.appendItemText(this, $0, $1)")
    void appendItemText(String text, int index);
}
