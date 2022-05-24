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
