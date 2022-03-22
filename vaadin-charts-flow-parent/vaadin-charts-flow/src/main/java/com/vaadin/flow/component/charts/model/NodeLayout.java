package com.vaadin.flow.component.charts.model;

import java.util.Locale;

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
 * @see Node#setLayout(NodeLayout)
 */
public enum NodeLayout implements ChartEnum {
    NORMAL, HANGING;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
