/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

import java.io.Serializable;

import com.vaadin.flow.component.charts.Chart;

public interface HasAxis<T> extends Serializable {
    Chart getSource();

    int getAxisIndex();

    T getAxis();
}
