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

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.internal.JacksonUtils;
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
 * @since 14.7
 */
@Tag("vaadin-message-input")
@JsModule("@vaadin/message-input/src/vaadin-message-input.js")
@NpmPackage(value = "@vaadin/message-input", version = "25.4.0-alpha5")
public class MessageInput extends Component
        implements Focusable<MessageInput>, HasSize, HasStyle, HasEnabled,
        HasTooltip, HasThemeVariant<MessageInputVariant> {

    private MessageInputI18n i18n;

    /**
     * Whether submitting was disabled when the latest submit event arrived from
     * the client. Stored once per event so that all submit listeners handle the
     * event in the same way, even if a listener changes
     * {@link #setSubmitDisabled(boolean)}.
     */
    private boolean ignoreClientSubmit;

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
        // Registered before any submit listener, so it runs first for each
        // submit event from the client
        getElement().addEventListener("submit",
                event -> ignoreClientSubmit = isSubmitDisabled());
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
        this();
        addSubmitListener(listener);
    }

    /**
     * Adds a listener that is called when the user submits the value of the
     * input field, which can be obtained with {@link SubmitEvent#getValue()}.
     * <p>
     * The event is fired when clicking the Send button or pressing the Enter
     * key. The event is not fired while submitting is disabled with
     * {@link #setSubmitDisabled(boolean)}. If a listener disables submitting,
     * the remaining listeners still receive the current event.
     *
     * @param listener
     *            the listener
     * @return registration for removal of the listener
     */
    public Registration addSubmitListener(
            ComponentEventListener<SubmitEvent> listener) {
        return addListener(SubmitEvent.class, event -> {
            if (event.isFromClient() && ignoreClientSubmit) {
                return;
            }
            listener.onComponentEvent(event);
        });
    }

    /**
     * Gets whether submitting a message is disabled.
     *
     * @return {@code true} if submitting is disabled, {@code false} otherwise
     * @see #setSubmitDisabled(boolean)
     */
    public boolean isSubmitDisabled() {
        return getElement().getProperty("submitDisabled", false);
    }

    /**
     * Sets whether submitting a message is disabled. When disabled, the Send
     * button is disabled and pressing the Enter key does not submit the
     * message. Unlike {@link #setEnabled(boolean)}, you can still type in the
     * input field, and the typed text is kept. Use this, for example, to
     * prevent sending a message while a file is still uploading.
     * <p>
     * The default value is {@code false}.
     *
     * @param submitDisabled
     *            {@code true} to disable submitting, {@code false} to enable it
     */
    public void setSubmitDisabled(boolean submitDisabled) {
        getElement().setProperty("submitDisabled", submitDisabled);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(MessageInputI18n)}.
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public MessageInputI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component. It enabled you
     * to customize and translate the language used in the message input.
     * <p>
     * Note: updating the object properties after setting the i18n will not
     * update the component. To make the changes effective, you need to set the
     * updated object again.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(MessageInputI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n object should not be null");
        getElement().setPropertyJson("i18n", JacksonUtils.beanToJson(i18n));
    }
}
