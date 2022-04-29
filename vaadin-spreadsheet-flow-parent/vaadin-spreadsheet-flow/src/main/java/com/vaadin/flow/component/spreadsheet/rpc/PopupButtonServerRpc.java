package com.vaadin.flow.component.spreadsheet.rpc;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
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
