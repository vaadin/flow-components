package com.vaadin.addon.charts;

/*
 * #%L
 * Vaadin Charts Addon
 * %%
 * Copyright (C) 2012 - 2015 Vaadin Ltd
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

import com.vaadin.ui.event.ComponentEvent;
import com.vaadin.ui.event.DomEvent;
import com.vaadin.ui.event.EventData;

/**
 * Fired when a series is shown
 */
@DomEvent("series-show")
public class SeriesShowEvent extends ComponentEvent<Chart> implements HasSeries {

    private final int seriesIndex;

    public SeriesShowEvent(Chart source, boolean fromClient,
            @EventData("event.detail.originalEvent.target.index") int seriesIndex) {
        super(source, fromClient);
        this.seriesIndex = seriesIndex;
    }

    public int getSeriesItemIndex() {
        return seriesIndex;
    }
}
