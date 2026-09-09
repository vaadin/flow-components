/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.client;

/**
 * Grouping-feature subset of the client→server signals from the GWT widget.
 * Split out from {@link SpreadsheetHandler} so the widget-side grouping code
 * can depend on the smaller interface without pulling in the full handler
 * surface.
 *
 * @since 23.1
 */
public interface GroupingHandler {
    void setGroupingCollapsed(boolean cols, int colIndex, boolean collapsed);

    void levelHeaderClicked(boolean cols, int level);
}
