/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
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
