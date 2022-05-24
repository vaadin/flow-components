package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts Addon
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
 * Handler interface for chart's drilldown callbacks.
 * <p/>
 * DrilldownCallback is used for async drilldown.
 * <p/>
 * {@link DrilldownCallback#handleDrilldown(DrilldownDetails)} is called when a
 * point with drilldown enabled is clicked and needs to return the Series to be
 * used as drilldown for the point.
 * <p/>
 * To enable async drilldown for a series item use
 * {@link DataSeries#addItemWithDrilldown(com.vaadin.flow.component.charts.model.DataSeriesItem)}
 */
public interface DrilldownCallback extends Serializable {

    /**
     * Method called when a point with drilldown enabled is clicked and should
     * return the Series to be used as drilldown for the point.
     *
     * @param event
     * @return a {@link Series} instance to be used as drilldown for the point
     *         or <code>null</code> if nothing should be done
     */
    Series handleDrilldown(DrilldownDetails event);

    class DrilldownDetails implements Serializable {
        private final Series series;
        private final DataSeriesItem item;
        private final int itemIndex;

        /**
         * Construct a DrilldownDetails
         *
         * @param series
         *            {@link Series} in which the event was originated
         * @param item
         *            {@link DataSeriesItem}
         * @param itemIndex
         *            index of the item.
         */
        public DrilldownDetails(Series series, DataSeriesItem item,
                int itemIndex) {
            this.series = series;
            this.item = item;
            this.itemIndex = itemIndex;
        }

        /**
         * Returns the {@link #getItem()} series.
         *
         * @return
         */
        public Series getSeries() {
            return series;
        }

        /**
         * Returns the item that was clicked
         *
         * @return
         */
        public DataSeriesItem getItem() {
            return item;
        }

        /**
         * Returns the index of {@link #getItem()} in {@link #getSeries()}.
         *
         * @return
         */
        public int getItemIndex() {
            return itemIndex;
        }

    }
}
