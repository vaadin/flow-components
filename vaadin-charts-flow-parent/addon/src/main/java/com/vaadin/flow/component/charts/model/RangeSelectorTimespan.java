package com.vaadin.flow.component.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2012 - 2016 Vaadin Ltd
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
 * Possible timespan values for range selector buttons
 */
public enum RangeSelectorTimespan implements ChartEnum {
    MILLISECOND("millisecond"), SECOND("second"), MINUTE("minute"), DAY("day"), WEEK(
            "week"), MONTH("month"), YEAR_TO_DATE("ytd"), YEAR("year"), ALL(
            "all");

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
