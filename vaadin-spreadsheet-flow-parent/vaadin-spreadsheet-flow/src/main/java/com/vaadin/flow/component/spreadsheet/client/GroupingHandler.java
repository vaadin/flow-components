/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.client;

import java.io.Serializable;

/**
 * The grouping messages the client sends to the server.
 * {@link SpreadsheetHandler} extends it with all the others.
 *
 * @since 23.1
 */
public interface GroupingHandler extends Serializable {
    void setGroupingCollapsed(boolean cols, int colIndex, boolean collapsed);

    void levelHeaderClicked(boolean cols, int level);
}
