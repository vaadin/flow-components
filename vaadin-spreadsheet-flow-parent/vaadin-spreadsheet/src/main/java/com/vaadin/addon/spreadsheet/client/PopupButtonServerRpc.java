package com.vaadin.addon.spreadsheet.client;

import com.vaadin.shared.communication.ServerRpc;

public interface PopupButtonServerRpc extends ServerRpc {

    /**
     * Called when the button has been clicked, and the pop-up has been opened.
     */
    public void onPopupButtonClick();

    /**
     * Called after the pop-up has been closed.
     */
    public void onPopupClose();
}
