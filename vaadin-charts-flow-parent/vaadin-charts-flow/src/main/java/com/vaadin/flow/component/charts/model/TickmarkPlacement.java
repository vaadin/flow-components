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
 * For categorized axes only. If ON the tick mark is placed in the center of the
 * category, if BETWEEN the tick mark is placed between categories. Defaults to
 * BETWEEN.
 */
public enum TickmarkPlacement implements ChartEnum {
    ON("on"), BETWEEN("between");

    private final String tickmarkPlacement;

    private TickmarkPlacement(String tickmarkPlacement) {
        this.tickmarkPlacement = tickmarkPlacement;
    }

    @Override
    public String toString() {
        return tickmarkPlacement;
    }
}
