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
