package com.vaadin.flow.component.charts.model.style;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
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
