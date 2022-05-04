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

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.shared.Registration;

/**
 * Message Input allows users to author and send messages. The component
 * displays a text-area to input a message and a button to send it. The user can
 * send the message with one of the following actions:
 * <ul>
 * <li>by pressing Enter (use Shift + Enter to add a new line)</li>
 * <li>by clicking the “send” button.</li>
 * </ul>
 * Use the {@link MessageList} component to show messages that users have sent.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-message-input")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/message-input/src/vaadin-message-input.js")
@NpmPackage(value = "@vaadin/message-input", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-messages", version = "23.1.0-beta1")
public class MessageInput extends Component
        implements HasSize, HasStyle, HasEnabled {

    private MessageInputI18n i18n;

    /**
     * The {@code submit} event which is fired by {@link MessageInput}
     * component.
     */
    @DomEvent("submit")
    public static class SubmitEvent extends ComponentEvent<MessageInput> {

        private final String value;

        /**
         * Creates the event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            true if the event comes from the client
         * @param value
         *            the value of the input
         */
        public SubmitEvent(MessageInput source, boolean fromClient,
                @EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
        }

        /**
         * Gets the submitted value.
         *
         * @return the submitted value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Creates a new message input component.
     */
    public MessageInput() {
    }

    /**
     * Creates a new message input component with the provided listener that
     * gets invoked when the user submits a new message.
     *
     * @param listener
     *            the submit event listener
     * @see #addSubmitListener(ComponentEventListener)
     */
    public MessageInput(ComponentEventListener<SubmitEvent> listener) {
        addSubmitListener(listener);
    }

    /**
     * Adds a listener that is called when the user submits the value of the
     * input field, which can be obtained with {@link SubmitEvent#getValue()}.
     * <p>
     * The event is fired when clicking the Send button or pressing the Enter
     * key.
     *
     * @param listener
     *            the listener
     * @return registration for removal of the listener
     */
    public Registration addSubmitListener(
            ComponentEventListener<SubmitEvent> listener) {
        return addListener(SubmitEvent.class, listener);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the object content returned by this method will not update
     * the component if not set back using
     * {@link MessageInput#setI18n(MessageInputI18n)}.
     *
     * @return the i18n object, or {@code null} if one has not been set with
     *         {@link #setI18n(MessageInputI18n)}
     */
    public MessageInputI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component. It enabled
     * you to customize and translate the language used in the message input.
     * <p>
     * Note: updating the object properties after setting the i18n will not
     * update the component. To make the changes effective, you need to set the
     * updated object again.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(MessageInputI18n i18n) {
        Objects.requireNonNull(i18n, "The i18n object should not be null");
        this.i18n = i18n;
        getElement().setPropertyJson("i18n", JsonUtils.beanToJson(i18n));
    }
}
