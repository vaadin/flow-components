/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import java.io.Serializable;

import jsinterop.annotations.JsType;

@SuppressWarnings("serial")
@JsType(namespace = "Vaadin.Spreadsheet")
public class SpreadsheetActionDetails implements Serializable {
    public String caption;
    public String key;
    /** 0 = cell, 1 = row, 2 = column */
    public int type;
}
