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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p>
 * Data grouping is the concept of sampling the data values into larger blocks
 * in order to ease readability and increase performance of the JavaScript
 * charts. Highstock by default applies data grouping when the points become
 * closer than a certain pixel value, determined by the
 * <code>groupPixelWidth</code> option.
 * </p>
 *
 * <p>
 * If data grouping is applied, the grouping information of grouped points can
 * be read from the <a href="#Point.dataGroup">Point.dataGroup</a>.
 * </p>
 */
public class DataGrouping extends AbstractConfigurationObject {

    private DateTimeLabelFormats dateTimeLabelFormats;
    private Boolean enabled;
    private Boolean forced;
    private Number groupPixelWidth;
    private Boolean smoothed;
    private ArrayList<TimeUnitMultiples> units;

    public DataGrouping() {
    }

    /**
     * @see #setDateTimeLabelFormats(DateTimeLabelFormats)
     */
    public DateTimeLabelFormats getDateTimeLabelFormats() {
        if (dateTimeLabelFormats == null) {
            dateTimeLabelFormats = new DateTimeLabelFormats();
        }
        return dateTimeLabelFormats;
    }

    /**
     * <p>
     * Datetime formats for the header of the tooltip in a stock chart. The
     * format can vary within a chart depending on the currently selected time
     * range and the current data grouping.
     * </p>
     *
     * <p>
     * The default formats are:
     * </p>
     *
     * <pre>
     * {
     * 	   millisecond: ['%A, %b %e, %H:%M:%S.%L', '%A, %b %e, %H:%M:%S.%L', '-%H:%M:%S.%L'],
     * 	   second: ['%A, %b %e, %H:%M:%S', '%A, %b %e, %H:%M:%S', '-%H:%M:%S'],
     * 	   minute: ['%A, %b %e, %H:%M', '%A, %b %e, %H:%M', '-%H:%M'],
     * 	   hour: ['%A, %b %e, %H:%M', '%A, %b %e, %H:%M', '-%H:%M'],
     * 	   day: ['%A, %b %e, %Y', '%A, %b %e', '-%A, %b %e, %Y'],
     * 	   week: ['Week from %A, %b %e, %Y', '%A, %b %e', '-%A, %b %e, %Y'],
     * 	   month: ['%B %Y', '%B', '-%B %Y'],
     * 	   year: ['%Y', '%Y', '-%Y']
     * 	}
     * </pre>
     *
     * <p>
     * For each of these array definitions, the first item is the format used
     * when the active time span is one unit. For instance, if the current data
     * applies to one week, the first item of the week array is used. The second
     * and third items are used when the active time span is more than two
     * units. For instance, if the current data applies to two weeks, the second
     * and third item of the week array are used, and applied to the start and
     * end date of the time span.
     * </p>
     */
    public void setDateTimeLabelFormats(
            DateTimeLabelFormats dateTimeLabelFormats) {
        this.dateTimeLabelFormats = dateTimeLabelFormats;
    }

    public DataGrouping(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable or disable data grouping.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setForced(Boolean)
     */
    public Boolean getForced() {
        return forced;
    }

    /**
     * When data grouping is forced, it runs no matter how small the intervals
     * are. This can be handy for example when the sum should be calculated for
     * values appearing at random times within each hour.
     * <p>
     * Defaults to: false
     */
    public void setForced(Boolean forced) {
        this.forced = forced;
    }

    /**
     * @see #setGroupPixelWidth(Number)
     */
    public Number getGroupPixelWidth() {
        return groupPixelWidth;
    }

    /**
     * The approximate pixel width of each group. If for example a series with
     * 30 points is displayed over a 600 pixel wide plot area, no grouping is
     * performed. If however the series contains so many points that the spacing
     * is less than the groupPixelWidth, Highcharts will try to group it into
     * appropriate groups so that each is more or less two pixels wide. If
     * multiple series with different group pixel widths are drawn on the same x
     * axis, all series will take the greatest width. For example, line series
     * have 2px default group width, while column series have 10px. If combined,
     * both the line and the column will have 10px by default.
     * <p>
     * Defaults to: 2
     */
    public void setGroupPixelWidth(Number groupPixelWidth) {
        this.groupPixelWidth = groupPixelWidth;
    }

    /**
     * @see #setSmoothed(Boolean)
     */
    public Boolean getSmoothed() {
        return smoothed;
    }

    /**
     * Normally, a group is indexed by the start of that group, so for example
     * when 30 daily values are grouped into one month, that month's x value
     * will be the 1st of the month. This apparently shifts the data to the
     * left. When the smoothed option is true, this is compensated for. The data
     * is shifted to the middle of the group, and min and max values are
     * preserved. Internally, this is used in the Navigator series.
     * <p>
     * Defaults to: false
     */
    public void setSmoothed(Boolean smoothed) {
        this.smoothed = smoothed;
    }

    /**
     * @see #setUnits(TimeUnitMultiples...)
     */
    public TimeUnitMultiples[] getUnits() {
        if (units == null) {
            return new TimeUnitMultiples[] {};
        }
        TimeUnitMultiples[] arr = new TimeUnitMultiples[units.size()];
        units.toArray(arr);
        return arr;
    }

    /**
     * An array determining what time intervals the data is allowed to be
     * grouped to. Each array item is an array where the first value is the time
     * unit and the second value another array of allowed multiples. Defaults
     * to:
     *
     * <pre>
     * units: [[
     * 		'millisecond', // unit name
     * 		[1, 2, 5, 10, 20, 25, 50, 100, 200, 500] // allowed multiples
     * 	], [
     * 		'second',
     * 		[1, 2, 5, 10, 15, 30]
     * 	], [
     * 		'minute',
     * 		[1, 2, 5, 10, 15, 30]
     * 	], [
     * 		'hour',
     * 		[1, 2, 3, 4, 6, 8, 12]
     * 	], [
     * 		'day',
     * 		[1]
     * 	], [
     * 		'week',
     * 		[1]
     * 	], [
     * 		'month',
     * 		[1, 3, 6]
     * 	], [
     * 		'year',
     * 		null
     * 	]]
     * </pre>
     */
    public void setUnits(TimeUnitMultiples... units) {
        this.units = new ArrayList<TimeUnitMultiples>(Arrays.asList(units));
    }

    /**
     * Adds unit to the units array
     *
     * @param unit
     *            to add
     * @see #setUnits(TimeUnitMultiples...)
     */
    public void addUnit(TimeUnitMultiples unit) {
        if (this.units == null) {
            this.units = new ArrayList<TimeUnitMultiples>();
        }
        this.units.add(unit);
    }

    /**
     * Removes first occurrence of unit in units array
     *
     * @param unit
     *            to remove
     * @see #setUnits(TimeUnitMultiples...)
     */
    public void removeUnit(TimeUnitMultiples unit) {
        this.units.remove(unit);
    }
}
