package com.vaadin.flow.component.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;

@SuppressWarnings("serial")
public class SpreadsheetActionDetails implements Serializable {
    public String caption;
    public String key;
    /** 0 = cell, 1 = row, 2 = column TODO replace with enum type */
    public int type;
}
