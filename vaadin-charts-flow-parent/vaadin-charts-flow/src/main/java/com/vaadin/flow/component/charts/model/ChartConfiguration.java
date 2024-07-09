/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.io.Serializable;

public interface ChartConfiguration extends Serializable {

    void fireAxesRescaled(Axis axis, Number minimum, Number maximum,
            boolean redraw, boolean animate);
}
