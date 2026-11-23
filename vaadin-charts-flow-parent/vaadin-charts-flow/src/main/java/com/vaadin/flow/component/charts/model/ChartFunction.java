/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.js.JsSnippet;

/**
 * A JavaScript function of a chart option, such as a formatter, declared in a
 * {@link com.vaadin.flow.js.JsDefinition JsDefinition} interface so that it is
 * part of the frontend bundle. The browser runs the function from the bundle
 * and does not compile any JavaScript from a string, so a chart that uses it
 * works under a Content Security Policy without <code>unsafe-eval</code>.
 * <p>
 * Declare a method that returns this type, and get its implementation from
 * {@link com.vaadin.flow.js.JsSnippets#of(Class)}. The arguments of the method
 * are available in the expression as <code>$0</code>, <code>$1</code>, and so
 * on. Highcharts calls the function with the option's context as
 * <code>this</code>. When the option passes arguments to the function, declare
 * their names on the method with
 * {@link com.vaadin.flow.js.JsFunctionParameterNames JsFunctionParameterNames}.
 *
 * <pre>
 * &#64;JsDefinition
 * public interface SalesFormatters extends JsSnippets {
 *
 *     &#64;JsExpression("return this.y / $0 + $1;")
 *     ChartFunction scaled(int divisor, String suffix);
 * }
 *
 * SalesFormatters formatters = JsSnippets.of(SalesFormatters.class);
 * dataLabels.setFormatterFunction(formatters.scaled(1000, "k"));
 * </pre>
 *
 * @since 25.4
 */
public interface ChartFunction extends JsSnippet {
}
