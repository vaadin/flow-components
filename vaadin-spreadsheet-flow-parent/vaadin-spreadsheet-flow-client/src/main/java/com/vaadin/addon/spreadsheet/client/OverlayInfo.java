/**
 * Copyright 2000-2024 Vaadin Ltd.
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
