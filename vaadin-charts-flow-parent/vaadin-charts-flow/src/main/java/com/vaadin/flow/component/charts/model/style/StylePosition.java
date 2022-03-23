package com.vaadin.flow.component.charts.model.style;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.ChartEnum;

/**
 * CSS position attribute, ABSOLUTE or RELATIVE
 */
public enum StylePosition implements ChartEnum {
    ABSOLUTE("absolute"), RELATIVE("relative");

    private String position;

    private StylePosition(String position) {
        this.position = position;
    }

    public String toString() {
        return position;
    }

}
