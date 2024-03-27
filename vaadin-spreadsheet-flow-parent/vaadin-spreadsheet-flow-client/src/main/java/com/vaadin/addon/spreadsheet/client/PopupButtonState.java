/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import com.vaadin.shared.AbstractComponentState;

import jsinterop.annotations.JsType;

@SuppressWarnings("serial")
@JsType(namespace = "Vaadin.Spreadsheet")
public class PopupButtonState extends AbstractComponentState {

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
