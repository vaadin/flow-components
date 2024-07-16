/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.events;

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

import java.io.Serializable;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Series;

/**
 * Indicates that an event has an associated series
 */
public interface HasSeries extends Serializable {

    Chart getSource();

    /**
     * Returns the index of the series
     *
     * @return
     */
    int getSeriesItemIndex();

    /**
     * Returns the series
     *
     * @return
     */
    default Series getSeries() {
        return getSource().getConfiguration().getSeries()
                .get(getSeriesItemIndex());
    }
}
