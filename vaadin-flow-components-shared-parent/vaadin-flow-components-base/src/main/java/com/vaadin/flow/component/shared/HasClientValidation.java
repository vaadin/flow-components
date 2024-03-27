/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
         * Returns true if the client-side validation succeeded and false
         * otherwise.
         *
         * @return whether the client-side validation succeeded.
         */
        public Boolean isValid() {
            return valid;
        }
    }
}
