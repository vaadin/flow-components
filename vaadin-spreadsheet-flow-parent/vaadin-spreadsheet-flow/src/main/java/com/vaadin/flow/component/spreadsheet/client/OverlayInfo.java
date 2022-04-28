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
public class OverlayInfo implements Serializable {
    public enum Type {
        IMAGE, COMPONENT
    };

    public OverlayInfo() {
    }

    public OverlayInfo(Type t) {
        type = t;
    }

    public Type type = Type.IMAGE;

    public int col;
    public int row;
    public float width;
    public float height;
    public float dy;
    public float dx;
}
