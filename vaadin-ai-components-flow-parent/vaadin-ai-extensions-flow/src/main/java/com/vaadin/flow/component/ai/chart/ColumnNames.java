/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;

/**
 * Column name constants used by {@link DefaultDataConverter} for pattern
 * matching. These names are the expected SQL column aliases that the LLM should
 * produce in its queries.
 * <p>
 * All names are lowercase and prefixed with {@value #PREFIX} to avoid
 * collisions with real database column names.
 * </p>
 *
 * @author Vaadin Ltd
 * @since 25.2
 */
public final class ColumnNames implements Serializable {

    private ColumnNames() {
    }

    /**
     * Prefix applied to all column name constants to distinguish converter
     * control columns from real data columns.
     */
    public static final String PREFIX = "_";

    // --- Common ---

    public static final String X = PREFIX + "x";
    public static final String Y = PREFIX + "y";
    public static final String Z = PREFIX + "z";
    public static final String X2 = PREFIX + "x2";
    public static final String NAME = PREFIX + "name";
    public static final String ID = PREFIX + "id";
    public static final String PARENT = PREFIX + "parent";
    public static final String VALUE = PREFIX + "value";
    public static final String COLOR = PREFIX + "color";

    // --- Multi-series grouping ---

    public static final String SERIES = PREFIX + "series";

    // --- OHLC / Candlestick ---

    public static final String OPEN = PREFIX + "open";
    public static final String HIGH = PREFIX + "high";
    public static final String LOW = PREFIX + "low";
    public static final String CLOSE = PREFIX + "close";

    // --- BoxPlot ---

    public static final String Q1 = PREFIX + "q1";
    public static final String MEDIAN = PREFIX + "median";
    public static final String Q3 = PREFIX + "q3";

    // --- Sankey ---

    public static final String FROM = PREFIX + "from";
    public static final String TO = PREFIX + "to";
    public static final String WEIGHT = PREFIX + "weight";

    // --- Timeline ---

    public static final String LABEL = PREFIX + "label";
    public static final String DESCRIPTION = PREFIX + "description";

    // --- Flags ---

    public static final String TITLE = PREFIX + "title";
    public static final String TEXT = PREFIX + "text";

    // --- Gantt ---

    public static final String START = PREFIX + "start";
    public static final String END = PREFIX + "end";
    public static final String DEPENDENCY = PREFIX + "dependency";
    public static final String COMPLETED = PREFIX + "completed";

    // --- Bullet ---

    public static final String TARGET = PREFIX + "target";

    // --- Waterfall ---

    public static final String WATERFALL_TYPE = PREFIX + "waterfall_type";

    // --- Treemap ---

    public static final String COLOR_VALUE = PREFIX + "colorvalue";

    // --- Organization ---

    public static final String IMAGE = PREFIX + "image";
}
