package com.vaadin.addon.spreadsheet.client;

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

import jsinterop.annotations.JsType;

@SuppressWarnings("serial")
@JsType
public class OverlayInfo implements Serializable {

    public final static String IMAGE = "IMAGE";
    public final static String COMPONENT = "COMPONENT";

    public String type = IMAGE;
    public int col;
    public int row;
    public float width;
    public float height;
    public float dy;
    public float dx;
}
