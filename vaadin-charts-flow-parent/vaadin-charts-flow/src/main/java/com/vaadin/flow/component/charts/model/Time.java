/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * These settings affect how datetime axes are laid out, how tooltips are
 * formatted, how series pointIntervalUnit works and how the Highstock range
 * selector handles time.
 */
public class Time extends AbstractConfigurationObject {

    private String timezone;
    private Number timezoneOffset;
    private Boolean useUTC;

    public Time() {
    }

    /**
     * @see #setTimezone(String)
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * A named time zone. Supported time zone names rely on the browser
     * implementations, as described in the <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat/DateTimeFormat#timezone">mdn
     * docs</a>. If the given time zone is not recognized by the browser,
     * Highcharts provides a warning and falls back to returning a 0 offset,
     * corresponding to the UTC time zone.
     * <p>
     * The time zone affects axis scaling, tickmark placement and time display
     * in <code>Highcharts.dateFormat</code>.
     * <p>
     * Setting timezone to {@code null} falls back to the default browser
     * timezone setting. Setting it to {@code "UTC"} is equivalent to setting
     * {@link #setUseUTC(Boolean) useUTC} to true.
     *
     * @param timezone
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * @see #setTimezoneOffset(Number)
     * @deprecated This property is deprecated and should not be used in new
     *             code. Use {@link #setTimezone(String)} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
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
     *
     * @deprecated This property is deprecated and should not be used in new
     *             code. Use {@link #setTimezone(String)} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public void setTimezoneOffset(Number timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    /**
     * @see #setUseUTC(Boolean)
     * @deprecated This property is deprecated and should not be used in new
     *             code. Use {@link #setTimezone(String)} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
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
     * 
     * @deprecated This property is deprecated and should not be used in new
     *             code. Use {@link #setTimezone(String)} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public void setUseUTC(Boolean useUTC) {
        this.useUTC = useUTC;
    }
}
