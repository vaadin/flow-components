package com.vaadin.flow.component.spreadsheet.shared;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

@SuppressWarnings("serial")
public class PopupButtonState {

    /** 1-based */
    public int col;
    /** 1-based */
    public int row;
    public boolean active;
    public boolean headerHidden;
    public String popupHeight = null;
    public String popupWidth = null;
    public String sheet;
}
