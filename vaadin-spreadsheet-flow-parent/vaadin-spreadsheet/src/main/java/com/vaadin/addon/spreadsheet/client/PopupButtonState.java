package com.vaadin.addon.spreadsheet.client;

import com.vaadin.shared.AbstractComponentState;

public class PopupButtonState extends AbstractComponentState {

    /** 1-based */
    public int col;
    /** 1-based */
    public int row;
    public boolean active;
    public boolean headerHidden;
    public String popupHeight = null;
    public String popupWidth = null;
}
