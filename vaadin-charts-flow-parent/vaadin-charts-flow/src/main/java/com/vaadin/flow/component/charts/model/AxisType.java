package com.vaadin.flow.component.charts.model;

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

import java.util.Date;

/**
 * Enum representing different axis types. Can be one of LINEAR, LOGARITHMIC,
 * CATEGORY or DATETIME. In a DATETIME axis, the numbers are given in
 * milliseconds (or as {@link Date}s), and tick marks are placed on appropriate
 * values like full hours or days. The default for new axes is LINEAR. CATEGORY
 * is a convenience mode for where the point names of the first series are used
 * for categories - avoiding the need to call
 * {@link Axis#setCategories(String...)}.
 */
public enum AxisType implements ChartEnum {
    LINEAR("linear"), LOGARITHMIC("logarithmic"),
    /**
     * In axis mode, the numbers are given in milliseconds (or as {@link Date}
     * s), and tick marks are placed on appropriate values like full hours or
     * days and formatted appropriately.
     */
    DATETIME("datetime"),
    /**
     * a convenience mode for where the point names of the first series are used
     * for categories - avoiding the need to call
     * {@link Axis#setCategories(String...)}. Note that mode does not affect "x"
     * value, so all series must have same points defined in same order.
     */
    CATEGORY("category");

    private String type;

    private AxisType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }

}
