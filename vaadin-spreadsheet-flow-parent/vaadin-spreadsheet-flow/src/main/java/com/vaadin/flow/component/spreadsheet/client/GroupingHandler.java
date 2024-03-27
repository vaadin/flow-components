/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.client;

public interface GroupingHandler {
    void setGroupingCollapsed(boolean cols, int colIndex, boolean collapsed);

    void levelHeaderClicked(boolean cols, int level);
}