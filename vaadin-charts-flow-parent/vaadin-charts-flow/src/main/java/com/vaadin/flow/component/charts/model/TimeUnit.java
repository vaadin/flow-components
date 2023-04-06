/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

public enum TimeUnit implements ChartEnum {
    MILLISECOND("millisecond"), SECOND("second"), MINUTE("minute"), HOUR(
            "hour"), DAY("day"), WEEK("week"), MONTH("month"), YEAR("year");

    private String name;

    private TimeUnit(String name) {
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
