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
 * Font weight used by Style class
 */
public enum FontWeight implements ChartEnum {

    /**
     * Normal text
     */
    NORMAL("normal"),

    /**
     * Bold text
     */
    BOLD("bold");

    private String type;

    private FontWeight(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

}
