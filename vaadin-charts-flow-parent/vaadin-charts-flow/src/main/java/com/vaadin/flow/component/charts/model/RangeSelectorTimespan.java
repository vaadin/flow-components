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
