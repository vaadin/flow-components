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
 * Class used to define allowed multiples a time unit is allowed to be grouped
 * to.
 */
public class TimeUnitMultiples extends AbstractConfigurationObject {

    private TimeUnit timeUnit;
    private int[] allowedMultiples;

    public TimeUnitMultiples(TimeUnit name, int... allowedMultiples) {
        super();
        this.timeUnit = name;
        this.allowedMultiples = allowedMultiples;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit name) {
        this.timeUnit = name;
    }

    public int[] getAllowedMultiples() {
        return allowedMultiples;
    }

    public void setAllowedMultiples(int... allowedMultiples) {
        this.allowedMultiples = allowedMultiples;
    }

}
