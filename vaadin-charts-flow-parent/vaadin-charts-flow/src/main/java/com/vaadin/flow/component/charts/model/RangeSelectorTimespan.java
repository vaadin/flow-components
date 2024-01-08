/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Possible timespan values for range selector buttons
 */
public enum RangeSelectorTimespan implements ChartEnum {
    MILLISECOND("millisecond"), SECOND("second"), MINUTE("minute"), DAY(
            "day"), WEEK("week"), MONTH(
                    "month"), YEAR_TO_DATE("ytd"), YEAR("year"), ALL("all");

    private String name;

    private RangeSelectorTimespan(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
