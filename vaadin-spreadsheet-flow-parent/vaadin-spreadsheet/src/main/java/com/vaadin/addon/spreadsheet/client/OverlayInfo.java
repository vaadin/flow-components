package com.vaadin.addon.spreadsheet.client;

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

import java.io.Serializable;

@SuppressWarnings("serial")
public class OverlayInfo implements Serializable {
    public enum Type {
        IMAGE,
        COMPONENT
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

    @Override
    public String toString() {
        return "OverlayInfo: col=" + col + ", row=" + row + ", width=" + width
                + ", height=" + height + ", dx=" + dx + ", dy=" + dy;
    }
}