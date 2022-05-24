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
