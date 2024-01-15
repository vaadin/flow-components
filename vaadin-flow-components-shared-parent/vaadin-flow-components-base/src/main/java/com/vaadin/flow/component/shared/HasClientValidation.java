/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.shared;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.shared.Registration;

/**
 * Mixin interface for subscribing to the client-side `validated` event from a
 * component.
 */
public interface HasClientValidation extends Serializable {
    /**
     * Adds a listener for the {@code validated} event fired by the web
     * component whenever it is validated on the client-side.
     *
     * @param listener
     *            the listener, not null.
     * @return a {@link Registration} for removing the event listener.
     */
    default Registration addClientValidatedEventListener(
            ComponentEventListener<ClientValidatedEvent> listener) {
        return ComponentUtil.addListener((Component) this,
                ClientValidatedEvent.class, listener);
    }

    /**
     * An event fired by the web component whenever it is validated on the
     * client-side.
     */
    @DomEvent("validated")
    public static class ClientValidatedEvent extends ComponentEvent<Component> {

        private final boolean valid;

        /**
         * Creates a new event using the given source.
         *
         * @param source
         *            the source component.
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         *            side, <code>false</code> otherwise
         * @param valid
         *            whether the client-side validation succeeded.
         */
        public ClientValidatedEvent(Component source, boolean fromClient,
                @EventData("event.detail.valid") boolean valid) {
            super(source, fromClient);
            this.valid = valid;
        }

        /**
         * Creates a new event using the given source.
         *
         * This constructor should be used when creating the event on the
         * server-side.
         *
         * @param source
         *            the source component.
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         *            side, <code>false</code> otherwise
         */
        public ClientValidatedEvent(Component source, boolean fromClient) {
            super(source, fromClient);
            this.valid = true;
        }

        /**
         * Returns true if the client-side validation succeeded and false
         * otherwise.
         *
         * <p>
         * Note, this method will always return true if the event originated
         * from the server-side i.e. {@link #isFromClient()} returns false.
         *
         * @return whether the client-side validation succeeded.
         */
        public Boolean isValid() {
            return valid;
        }
    }
}
