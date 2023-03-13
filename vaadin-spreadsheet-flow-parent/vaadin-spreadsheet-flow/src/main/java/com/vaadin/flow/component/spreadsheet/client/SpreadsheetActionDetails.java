/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SpreadsheetActionDetails implements Serializable {
    public String caption;
    public String key;
    /** 0 = cell, 1 = row, 2 = column TODO replace with enum type */
    public int type;
}
