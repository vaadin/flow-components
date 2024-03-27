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

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@SuppressWarnings("serial")
@JsType(namespace = "Vaadin.Spreadsheet")
public class MergedRegion implements Serializable {
    public int id;
    public int col1;
    public int col2;
    public int row1;
    public int row2;

    @JsIgnore
    public MergedRegion() {
    }

    @JsIgnore
    public MergedRegion(int c1, int r1, int c2, int r2) {
        col1 = c1;
        row1 = r1;
        col2 = c2;
        row2 = r2;
    }
}