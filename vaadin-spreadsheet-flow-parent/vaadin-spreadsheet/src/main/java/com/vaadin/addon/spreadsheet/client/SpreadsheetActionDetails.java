package com.vaadin.addon.spreadsheet.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SpreadsheetActionDetails implements Serializable {
    public String caption;
    public String key;
    /** 0 = cell, 1 = row, 2 = column TODO replace with enum type */
    public int type;
}
