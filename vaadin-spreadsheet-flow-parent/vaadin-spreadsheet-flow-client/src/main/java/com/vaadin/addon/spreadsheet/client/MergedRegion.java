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

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@SuppressWarnings("serial")
@JsType
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