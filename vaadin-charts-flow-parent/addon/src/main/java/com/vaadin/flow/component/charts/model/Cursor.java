package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */


/**
 * You can set the cursor to POINTER("pointer") if you have click events
 * attached to the series, to signal to the user that the points and lines can
 * be clicked. Defaults to NONE.
 */
public enum Cursor implements ChartEnum {
    POINTER("pointer"), NONE("");

    private String cursor;

    private Cursor(String cursor) {
        this.cursor = cursor;
    }

    public String toString() {
        return cursor;
    }

}
