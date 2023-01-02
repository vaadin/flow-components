/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.vaadin.shared.communication.ClientRpc;

public interface PopupButtonClientRpc extends ClientRpc {

    /**
     * Opens the popup if the button is rendered.
     */
    public void openPopup();

    /**
     * Closes the popup if it is open.
     */
    public void closePopup();
}
