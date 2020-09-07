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


import java.time.Instant;
import java.util.Date;

/**
 * A DataSeriesItem implementation suitable for <a
 * https://en.wikipedia.org/wiki/Open-high-low-close_chart">OHLC charts</a>.
 * OHLC charts visualize well financial data.
 * 
 * @see PlotOptionsOhlc
 * @see PlotOptionsCandlestick
 * 
 */
public class OhlcItem extends DataSeriesItem {
    // high/low already defined in DataSeriesItem

    private Number open;
    private Number close;

    /**
     * Constructs an empty ohlc data item
     */
    public OhlcItem() {
    }

    /**
     * Constructs an ohlc data item for give open, high, low and close values
     *
     * @param x
     * @param open
     * @param high
     * @param low
     * @param close
     */
    public OhlcItem(Number x, Number open, Number high, Number low,
            Number close) {
        this();
        setX(x);
        setOpen(open);
        setLow(low);
        setHigh(high);
        setClose(close);
    }

    /**
     * Constructs an ohlc data item for give open, high, low and close values
     *
     * @param instant
     * @param open
     * @param high
     * @param low
     * @param close
     */
    public OhlcItem(Instant instant, Number open, Number high, Number low,
            Number close) {
        this();
        setX(instant);
        setOpen(open);
        setLow(low);
        setHigh(high);
        setClose(close);
    }

    /**
     * @deprecated as of 4.0. Use
     *             {@link #OhlcItem(Instant, Number, Number, Number, Number)}
     */
    @Deprecated
    public OhlcItem(Date date, Number open, Number high, Number low,
            Number close) {
        this();
        setX(date);
        setOpen(open);
        setLow(low);
        setHigh(high);
        setClose(close);
    }

    /**
     * @see #setOpen(Number)
     */
    public Number getOpen() {
        return open;
    }

    /**
     * Sets the open value of the OHLC item
     *
     * @param open
     */
    public void setOpen(Number open) {
        this.open = open;
    }

    /**
     * @see #setClose(Number)
     */
    public Number getClose() {
        return close;
    }

    /**
     * Sets the close value of the OHLC item
     *
     * @param close
     */
    public void setClose(Number close) {
        this.close = close;
    }
}
