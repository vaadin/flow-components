/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.io.Serializable;

import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

import tools.jackson.databind.node.ObjectNode;

/**
 * The JavaScript that charts run in the browser, for
 * {@link com.vaadin.flow.dom.Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface ChartJs extends Serializable {

    /**
     * Sets the global Highcharts options and redraws all charts.
     *
     * @param options
     *            the options to set
     */
    @JsExpression("customElements.get('vaadin-chart').__callHighchartsFunction('setOptions', true, $0)")
    void setHighchartsOptions(ObjectNode options);

    /**
     * Adds a series to the chart as the drilldown of a point.
     *
     * @param seriesIndex
     *            the index of the series that contains the point
     * @param pointIndex
     *            the index of the point in the series
     * @param series
     *            the series to add
     */
    @JsExpression("this.__callChartFunction('addSeriesAsDrilldown', this.configuration.series[$0].data[$1], $2)")
    void addSeriesAsDrilldown(int seriesIndex, int pointIndex,
            ObjectNode series);
}
