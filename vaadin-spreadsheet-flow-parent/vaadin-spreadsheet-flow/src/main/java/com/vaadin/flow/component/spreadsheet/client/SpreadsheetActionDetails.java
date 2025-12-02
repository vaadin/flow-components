/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SpreadsheetActionDetails implements Serializable {
    public String caption;
    public String key;
    /**
     * 0 = cell, 1 = row, 2 = column - kept as int for client-server
     * compatibility
     */
    public int type;
    /** Node id of the icon virtual child, 0 if no icon provided */
    public int iconNodeId;
}
