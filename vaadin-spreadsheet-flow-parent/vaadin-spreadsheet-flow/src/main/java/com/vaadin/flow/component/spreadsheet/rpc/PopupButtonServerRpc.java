/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.rpc;

import java.io.Serializable;

/**
 * @since 23.1
 * @deprecated Not used: the pop-up button hears of its opening and closing from
 *             the element. No replacement is planned.
 */
@Deprecated(since = "25.4")
public interface PopupButtonServerRpc extends Serializable {

    /**
     * Called when the button has been clicked, and the pop-up has been opened.
     */
    public void onPopupButtonClick();

    /**
     * Called after the pop-up has been closed.
     */
    public void onPopupClose();
}
