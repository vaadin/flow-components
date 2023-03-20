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
 * The direction where the layout algorithm will start drawing. Applies to
 * {@link ChartType#TREEMAP} charts.
 */
public enum TreeMapLayoutStartingDirection implements ChartEnum {
    VERTICAL("vertical"), HORIZONTAL("horizontal");

    private final String type;

    TreeMapLayoutStartingDirection(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
