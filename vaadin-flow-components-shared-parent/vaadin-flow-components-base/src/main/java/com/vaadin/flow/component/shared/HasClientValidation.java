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
package com.vaadin.flow.component.shared;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.shared.Registration;

/**
 * Mixin interface for subscribing to the client-side `validated` event from a
 * component.
 *
 * @deprecated Since 24.6, this interface is no longer supported. Consider
 *             {@link HasValidation} or {@link HasValidator} as an alternative.
 */
@Deprecated
public interface HasClientValidation extends Serializable {
    /**
     * Adds a listener for the {@code validated} event fired by the web
     * component whenever it is validated on the client-side.
     *
     * @param listener
     *            the listener, not null.
     * @return a {@link Registration} for removing the event listener.
     * @deprecated Since 24.6, this event is no longer supported. Consider
     *             subscribing to {@link ValidationStatusChangeEvent} to get
     *             notified when the user enters input that cannot be parsed.
     */
    @Deprecated
    default Registration addClientValidatedEventListener(
            ComponentEventListener<ClientValidatedEvent> listener) {
        // TODO: Temporary workaround to make the web component fire
        // the validated event in manual validation mode. This will be
        // removed in Vaadin 25 along with the validated event.
        ((HasElement) this).getElement().executeJs(
                """
                            this._requestValidation = function () {
                                Object.getPrototypeOf(this)._requestValidation.call(this);
                                if (this.manualValidation) {
                                    const valid = this.checkValidity();
                                    this.dispatchEvent(new CustomEvent('validated', { detail: { valid } }));
                                }
                            }
                        """);

        return ComponentUtil.addListener((Component) this,
                ClientValidatedEvent.class, listener);
    }

    /**
     * An event fired by the web component whenever it is validated on the
     * client-side.
     *
     * @deprecated Since 24.6, this event is no longer supported. Consider
     *             subscribing to {@link ValidationStatusChangeEvent} to get
     *             notified when the user enters input that cannot be parsed.
     */
    @DomEvent("validated")
    @Deprecated
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
