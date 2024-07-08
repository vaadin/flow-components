/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.events.internal;

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

import com.vaadin.flow.component.charts.model.Series;

/**
 * Event when the data was removed.
 *
 * @since 2.0
 *
 */
public class DataRemovedEvent extends AbstractSeriesEvent {

    private static final long serialVersionUID = 20141117;

    private final int index;

    /**
     * Constructs the event with given series and index of the removed data.
     *
     * @param series
     *            Series.
     * @param index
     *            Index.
     */
    public DataRemovedEvent(Series series, int index) {
        super(series);
        this.index = index;
    }

    /**
     * Returns index of the removed data point.
     *
     * @return index of the removed data point
     */
    public int getIndex() {
        return index;
    }

}
