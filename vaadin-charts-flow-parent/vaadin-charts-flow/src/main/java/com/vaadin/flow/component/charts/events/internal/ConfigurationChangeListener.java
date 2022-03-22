package com.vaadin.flow.component.charts.events.internal;

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

import java.io.Serializable;

/**
 * Listener interface for events triggered in Configuration. E.g. in DataSeries,
 * events like data add/remove/update.
 *
 * @since 2.0
 *
 */
public interface ConfigurationChangeListener extends Serializable {
    /**
     * A data point has been added
     *
     * @param event
     *            The event.
     */
    void dataAdded(DataAddedEvent event);

    /**
     * A data point has been removed
     *
     * @param event
     *            The event.
     */
    void dataRemoved(DataRemovedEvent event);

    /**
     * A data point has been updated
     *
     * @param event
     *            The event.
     */
    void dataUpdated(DataUpdatedEvent event);

    /**
     * A point has been sliced
     *
     * @param event
     *            The event
     */
    void itemSliced(ItemSlicedEvent event);

    /**
     * The series is enabled or disabled
     *
     * @param event
     *            The event.
     */
    void seriesStateChanged(SeriesStateEvent event);

    /**
     * A new series has been added
     *
     * @param event
     */
    void seriesAdded(SeriesAddedEvent event);

    /**
     * The series has been changed.
     *
     * @param event
     *            The event
     */
    void seriesChanged(SeriesChangedEvent event);

    /**
     * An axis has been rescaled.
     *
     * @param event
     *            The event.
     */
    void axisRescaled(AxisRescaledEvent event);

    /**
     * Reset zoom level by setting axis extremes to null
     *
     * @param redraw
     * @param animate
     */
    void resetZoom(boolean redraw, boolean animate);
}
