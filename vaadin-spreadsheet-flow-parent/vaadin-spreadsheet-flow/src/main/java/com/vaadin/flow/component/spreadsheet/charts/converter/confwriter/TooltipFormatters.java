/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.confwriter;

import com.vaadin.flow.component.charts.model.ChartFunction;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;

import tools.jackson.databind.node.ArrayNode;

/**
 * The tooltip formatter of spreadsheet charts, declared so that the browser
 * does not compile it from a string.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface TooltipFormatters extends JsSnippets {

    /**
     * Returns a tooltip formatter that shows the series name, the point name
     * and the point value.
     *
     * @param series
     *            one object per chart series, in the order of the series IDs.
     *            The object has the key of the point value
     *            (<code>valueKey</code>), the series name to show when the
     *            series has none (<code>defaultName</code>) and the number of
     *            decimals to show, or a negative number to round to 9 digits
     *            (<code>decimals</code>)
     * @return the tooltip formatter
     */
    @JsExpression("""
            const series = $0[this.series.options.id];
            if (!series) {
              return '';
            }
            const value = this[series.valueKey];
            const signlessNumber = Math.abs(value);
            let formattedNumber;
            if (series.decimals < 0) {
              // Round numbers to contain only 9 digits if they contain decimals.
              const decimals = signlessNumber > 1.0 ? 9 - Math.floor(Math.log(signlessNumber) / Math.LN10) : 9;
              formattedNumber = Math.round(value) == value ? value : Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
            } else {
              // numberFormat can handle numbers 20 digits long.
              const decimals = Math.ceil(Math.log(signlessNumber) / Math.LN10) + series.decimals <= 20 ? series.decimals : 20;
              formattedNumber = this.point.series.chart.numberFormatter(value, decimals);
            }
            const name = series.defaultName != null ? series.defaultName : this.series.name;
            const seriesName = typeof name == 'number' ? name : JSON.stringify(name);
            const pointName = 'name' in this.point ? JSON.stringify(this.point.name) : this.x + 1;
            return 'Series ' + seriesName + ' Point ' + pointName + ' <br>' + formattedNumber;""")
    ChartFunction seriesPointValue(ArrayNode series);
}
