/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.client;

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
