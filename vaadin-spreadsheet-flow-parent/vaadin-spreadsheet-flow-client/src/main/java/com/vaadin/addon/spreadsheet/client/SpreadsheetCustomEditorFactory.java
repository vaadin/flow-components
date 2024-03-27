/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.google.gwt.user.client.ui.Widget;

public interface SpreadsheetCustomEditorFactory {

    boolean hasCustomEditor(String selectedCellKey);

    Widget getCustomEditor(String selectedCellKey);

}
