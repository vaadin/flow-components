/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
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
 * DataSeriesItem that can be used as sum or intermediate sum in waterfall
 * charts. Note that sums don't support all standard point features and their
 * value don't need be be set (automatically calculated).
 */
public class WaterFallSum extends DataSeriesItem {

    @SuppressWarnings("unused")
    private Boolean isSum = Boolean.TRUE;
    private Boolean isIntermediateSum;

    public WaterFallSum(String name) {
        setName(name);
    }

    /**
     * @param intermediate
     *            true if the sum is should be intermediate
     */
    public void setIntermediate(boolean intermediate) {
        if (intermediate) {
            isIntermediateSum = Boolean.TRUE;
            isSum = null;
        } else {
            isIntermediateSum = null;
            isSum = Boolean.TRUE;
        }
    }

    public boolean isIntermediate() {
        return isIntermediateSum != null;
    }
}
