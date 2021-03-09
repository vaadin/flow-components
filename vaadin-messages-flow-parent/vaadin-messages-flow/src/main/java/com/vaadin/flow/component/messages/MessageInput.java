/*
 * Copyright 2000-2021 Vaadin Ltd.
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
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the {@code vaadin-message-input} element. The
 * component displays a text-area to input a message and a button to send it.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-message-input")
@JsModule("@vaadin/vaadin-messages/src/vaadin-message-input.js")
@NpmPackage(value = "@vaadin/vaadin-messages", version = "v2.0.0-alpha1")
public class MessageInput extends Component
        implements HasSize, HasStyle, HasEnabled {

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
}
