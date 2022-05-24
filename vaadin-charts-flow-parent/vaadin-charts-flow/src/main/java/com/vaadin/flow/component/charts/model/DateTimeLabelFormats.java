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
 * For a DATETIME axis, the scale will automatically adjust to the appropriate
 * unit. This member gives the default string representations used for each
 * unit. For an overview of the replacement codes, see
 * {@link java.text.DateFormat}. Defaults to:
 *
 * <code>
 * {
 *   second: '%H:%M:%S',
 *   minute: '%H:%M',
 *   hour: '%H:%M',
 *   day: '%e. %b',
 *   week: '%e. %b',
 *   month: '%b \'%y',
 *   year: '%Y'
 * }
 * </code>
 */
@SuppressWarnings("serial")
public class DateTimeLabelFormats extends AbstractConfigurationObject {

    private String second;
    private String minute;
    private String hour;
    private String day;
    private String week;
    private String month;
    private String year;

    public DateTimeLabelFormats() {
    }

    /**
     * Constructs a DateTimeLabelFormats with the given format strings for month
     * and year
     *
     * @param month
     * @param year
     */
    public DateTimeLabelFormats(String month, String year) {
        this.month = month;
        this.year = year;
    }

    /**
     * @see #setSecond(String)
     * @return The format string for second resolution.
     */
    public String getSecond() {
        return second;
    }

    /**
     * Sets the format String for second resolution.
     *
     * @param second
     */
    public void setSecond(String second) {
        this.second = second;
    }

    /**
     * @see #setMinute(String)
     * @return The format string for minute resolution.
     */
    public String getMinute() {
        return minute;
    }

    /**
     * Sets the format String for minute resolution.
     *
     * @param minute
     */
    public void setMinute(String minute) {
        this.minute = minute;
    }

    /**
     * @see #setHour(String)
     * @return The format string for hour resolution.
     */
    public String getHour() {
        return hour;
    }

    /**
     * Sets the format String for hour resolution
     *
     * @param hour
     */
    public void setHour(String hour) {
        this.hour = hour;
    }

    /**
     * @see #setDay(String)
     * @return The format string for day resolution
     */
    public String getDay() {
        return day;
    }

    /**
     * Sets the format String for day resolution.
     *
     * @param day
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * @see #setWeek(String)
     * @return The format string for week resolution.
     */
    public String getWeek() {
        return week;
    }

    /**
     * Sets the format String for week resolution.
     *
     * @param week
     */
    public void setWeek(String week) {
        this.week = week;
    }

    /**
     * @see #setMonth(String)
     * @return The format string for month resolution.
     */
    public String getMonth() {
        return month;
    }

    /**
     * Sets the format String for month resolution.
     *
     * @param month
     */
    public void setMonth(String month) {
        this.month = month;
    }

    /**
     * @see #setYear(String)
     * @return The format string for year resolution
     */
    public String getYear() {
        return year;
    }

    /**
     * Sets the format String for year resolution.
     *
     * @param year
     */
    public void setYear(String year) {
        this.year = year;
    }

}
