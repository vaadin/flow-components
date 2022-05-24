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

import java.util.Date;
import java.time.Instant;
import com.vaadin.flow.component.charts.util.Util;

/**
 * An array defining breaks in the axis, the sections defined will be left out
 * and all the points shifted closer to each other.
 */
public class Breaks extends AbstractConfigurationObject {

    private Number breakSize;
    private Number from;
    private Number repeat;
    private Number to;

    public Breaks() {
    }

    /**
     * @see #setBreakSize(Number)
     */
    public Number getBreakSize() {
        return breakSize;
    }

    /**
     * A number indicating how much space should be left between the start and
     * the end of the break. The break size is given in axis units, so for
     * instance on a <code>datetime</code> axis, a break size of 3600000 would
     * indicate the equivalent of an hour.
     * <p>
     * Defaults to: 0
     */
    public void setBreakSize(Number breakSize) {
        this.breakSize = breakSize;
    }

    /**
     * @see #setFrom(Number)
     */
    public Number getFrom() {
        return from;
    }

    /**
     * The point where the break starts.
     */
    public void setFrom(Number from) {
        this.from = from;
    }

    /**
     * @see #setRepeat(Number)
     */
    public Number getRepeat() {
        return repeat;
    }

    /**
     * Defines an interval after which the break appears again. By default the
     * breaks do not repeat.
     * <p>
     * Defaults to: 0
     */
    public void setRepeat(Number repeat) {
        this.repeat = repeat;
    }

    /**
     * @see #setTo(Number)
     */
    public Number getTo() {
        return to;
    }

    /**
     * The point where the break ends.
     */
    public void setTo(Number to) {
        this.to = to;
    }

    /**
     * @deprecated as of 4.0. Use {@link #setPointStart(Instant)}
     */
    @Deprecated
    public void setFrom(Date date) {
        this.from = Util.toHighchartsTS(date);
    }

    /**
     * @see #setFrom(Number)
     */
    public void setFrom(Instant instant) {
        this.from = Util.toHighchartsTS(instant);
    }

    /**
     * @deprecated as of 4.0. Use {@link #setPointStart(Instant)}
     */
    @Deprecated
    public void setTo(Date date) {
        this.to = Util.toHighchartsTS(date);
    }

    /**
     * @see #setTo(Number)
     */
    public void setTo(Instant instant) {
        this.to = Util.toHighchartsTS(instant);
    }
}
