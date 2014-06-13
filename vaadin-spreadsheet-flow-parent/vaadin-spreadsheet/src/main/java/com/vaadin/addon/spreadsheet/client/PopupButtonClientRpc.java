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
