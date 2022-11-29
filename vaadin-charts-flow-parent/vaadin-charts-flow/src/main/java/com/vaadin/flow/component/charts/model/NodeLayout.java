package com.vaadin.flow.component.charts.model;

import java.util.Locale;

/**
 * Copyright (C) 2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
