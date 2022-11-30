package com.vaadin.flow.component.spreadsheet.rpc;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

public interface PopupButtonServerRpc {

    /**
     * Called when the button has been clicked, and the pop-up has been opened.
     */
    public void onPopupButtonClick();

    /**
     * Called after the pop-up has been closed.
     */
    public void onPopupClose();
}
