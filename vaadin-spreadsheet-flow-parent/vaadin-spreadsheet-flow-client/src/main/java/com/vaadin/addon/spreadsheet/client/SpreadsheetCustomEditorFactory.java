package com.vaadin.addon.spreadsheet.client;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import com.google.gwt.user.client.ui.Widget;

public interface SpreadsheetCustomEditorFactory {

    boolean hasCustomEditor(String selectedCellKey);

    Widget getCustomEditor(String selectedCellKey);

}
