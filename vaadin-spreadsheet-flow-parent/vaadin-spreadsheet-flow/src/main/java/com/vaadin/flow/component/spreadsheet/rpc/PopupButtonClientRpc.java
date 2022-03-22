package com.vaadin.flow.component.spreadsheet.rpc;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

public interface PopupButtonClientRpc {

    /**
     * Opens the popup if the button is rendered.
     */
    public void openPopup();

    /**
     * Closes the popup if it is open.
     */
    public void closePopup();
}
