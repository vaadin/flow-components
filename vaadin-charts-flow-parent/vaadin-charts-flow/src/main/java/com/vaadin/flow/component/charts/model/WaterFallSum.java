package com.vaadin.flow.component.charts.model;

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
