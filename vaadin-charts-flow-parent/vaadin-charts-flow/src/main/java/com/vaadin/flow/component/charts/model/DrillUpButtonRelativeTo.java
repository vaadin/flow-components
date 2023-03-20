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
 * What box to align the button to.
 */
public enum DrillUpButtonRelativeTo implements ChartEnum {

    PLOTBOX("plotBox"), SPACINGBOX("spacingBox");

    DrillUpButtonRelativeTo(String box) {
        this.box = box;
    }

    private String box;

    @Override
    public String toString() {
        return box;
    }
}
