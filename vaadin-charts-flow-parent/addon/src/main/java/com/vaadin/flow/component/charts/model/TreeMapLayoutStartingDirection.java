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
