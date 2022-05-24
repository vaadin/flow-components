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
 * These settings affect how datetime axes are laid out, how tooltips are
 * formatted, how series pointIntervalUnit works and how the Highstock range
 * selector handles time.
 */
public class Time extends AbstractConfigurationObject {

    private Number timezoneOffset;
    private Boolean useUTC;

    public Time() {
    }

    /**
     * @see #setTimezoneOffset(Number)
     */
    public Number getTimezoneOffset() {
        return timezoneOffset;
    }

    /**
     * The timezone offset in minutes. Positive values are west, negative values
     * are east of UTC, as in the ECMAScript <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getTimezoneOffset"
     * >getTimezoneOffset</a> method. Use this to display UTC based data in a
     * predefined time zone.
     * <p>
     * Defaults to: 0
     */
    public void setTimezoneOffset(Number timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    /**
     * @see #setUseUTC(Boolean)
     */
    public Boolean getUseUTC() {
        return useUTC;
    }

    /**
     * Whether to use UTC time for axis scaling, tickmark placement and time
     * display in <code>Highcharts.dateFormat</code>. Advantages of using UTC is
     * that the time displays equally regardless of the user agent's time zone
     * settings. Local time can be used when the data is loaded in real time or
     * when correct Daylight Saving Time transitions are required.
     * <p>
     * Defaults to: true
     */
    public void setUseUTC(Boolean useUTC) {
        this.useUTC = useUTC;
    }
}
