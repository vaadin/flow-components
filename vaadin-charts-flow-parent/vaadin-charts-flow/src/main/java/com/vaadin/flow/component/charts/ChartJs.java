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

import com.vaadin.flow.component.charts.model.ChartFunction;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

import tools.jackson.databind.node.ArrayNode;
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
     * Declares <code>putFunctions(target, paths, functions)</code>, which sets
     * each function at its path in the target. A path is an array of property
     * names and array indexes.
     */
    String PUT_FUNCTIONS = """
            const putFunctions = (target, paths, functions) => paths.forEach((path, i) => {
              let parent = target;
              for (let j = 0; j < path.length - 1; j++) {
                parent = parent[path[j]];
              }
              parent[path[path.length - 1]] = functions[i];
            });
            """;

    /**
     * Sets the global Highcharts options and redraws all charts.
     *
     * @param options
     *            the options to set
     * @param functionPaths
     *            the path of each function in the options
     * @param functions
     *            the functions to set at those paths
     */
    @JsExpression(PUT_FUNCTIONS
            + """
                    putFunctions($0, $1, $2);
                    customElements.get('vaadin-chart').__callHighchartsFunction('setOptions', true, $0);
                    """)
    void setHighchartsOptions(ObjectNode options, ArrayNode functionPaths,
            ChartFunction... functions);

    /**
     * Adds a series to the chart as the drilldown of a point.
     *
     * @param seriesIndex
     *            the index of the series that contains the point
     * @param pointIndex
     *            the index of the point in the series
     * @param series
     *            the series to add
     * @param functionPaths
     *            the path of each function in the series
     * @param functions
     *            the functions to set at those paths
     */
    @JsExpression(PUT_FUNCTIONS
            + """
                    putFunctions($2, $3, $4);
                    this.__callChartFunction('addSeriesAsDrilldown', this.configuration.series[$0].data[$1], $2);
                    """)
    void addSeriesAsDrilldown(int seriesIndex, int pointIndex,
            ObjectNode series, ArrayNode functionPaths,
            ChartFunction... functions);

    /**
     * Updates the configuration of the chart.
     *
     * @param configuration
     *            the configuration to apply
     * @param resetConfiguration
     *            whether to redraw the chart from scratch
     * @param functionPaths
     *            the path of each function in the configuration
     * @param functions
     *            the functions to set at those paths
     */
    @JsExpression(PUT_FUNCTIONS + """
            putFunctions($0, $2, $3);
            this.updateConfiguration($0, $1);
            """)
    void updateConfiguration(ObjectNode configuration,
            boolean resetConfiguration, ArrayNode functionPaths,
            ChartFunction... functions);

    /**
     * Calls a function of the chart element with the given arguments.
     *
     * @param functionName
     *            the name of the function of the chart element
     * @param arguments
     *            the arguments of the function
     * @param functionPaths
     *            the path of each function in the arguments, starting with the
     *            index of the argument
     * @param functions
     *            the functions to set at those paths
     */
    @JsExpression(PUT_FUNCTIONS + """
            putFunctions($1, $2, $3);
            this[$0](...$1);
            """)
    void callElementFunction(String functionName, ArrayNode arguments,
            ArrayNode functionPaths, ChartFunction... functions);
}
