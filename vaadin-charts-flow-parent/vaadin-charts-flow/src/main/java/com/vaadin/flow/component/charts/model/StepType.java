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
 * Defines different step line types. Configurable in {@link PlotOptionsLine}.
 */
public enum StepType implements ChartEnum {
    RIGHT("right"), CENTER("center"), LEFT("left"), NONE("");

    private String highchartName;

    private StepType(String n) {
        this.highchartName = n;
    }

    public String toString() {
        return highchartName;
    }

}
