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
 * Global options that don't apply to each chart. These options, like the
 * <code>lang</code> options, must be set using the
 * <code>Highcharts.setOptions</code> method.
 *
 * <pre>
 * Highcharts.setOptions({
 * 	global: {
 * 		useUTC: false
 * 	}
 * });
 * </pre>
 */
public class Global extends AbstractConfigurationObject {

    private String VMLRadialGradientURL;
    private String timezone;
    private Number timezoneOffset;
    private Boolean useUTC;

    public Global() {
    }

    /**
     * @see #setVMLRadialGradientURL(String)
     */
    public String getVMLRadialGradientURL() {
        return VMLRadialGradientURL;
    }

    /**
     * Path to the pattern image required by VML browsers in order to draw
     * radial gradients.
     * <p>
     * Defaults to:
     * http://code.highcharts.com/{version}/gfx/vml-radial-gradient.png
     */
    public void setVMLRadialGradientURL(String VMLRadialGradientURL) {
        this.VMLRadialGradientURL = VMLRadialGradientURL;
    }

    /**
     * @see #setTimezone(String)
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Requires <a href="http://momentjs.com/">moment.js</a>. If the timezone
     * option is specified, it creates a default
     * <a href="#global.getTimezoneOffset">getTimezoneOffset</a> function that
     * looks up the specified timezone in moment.js. If moment.js is not
     * included, this throws a Highcharts error in the console, but does not
     * crash the chart.
     * <p>
     * Defaults to: undefined
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
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
