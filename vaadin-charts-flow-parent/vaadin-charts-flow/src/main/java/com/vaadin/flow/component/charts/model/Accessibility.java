/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * <p>
 * Options for configuring accessibility for the chart. Requires the
 * <a href="//code.highcharts.com/modules/accessibility.js">accessibility
 * module</a> to be loaded. For a description of the module and information on
 * its features, see <a href=
 * "http://www.highcharts.com/docs/chart-concepts/accessibility">Highcharts
 * Accessibility</a>.
 * </p>
 * 
 * @since 6.0.1
 */
public class Accessibility extends AbstractConfigurationObject {

    private Boolean describeSingleSeries;
    private Boolean enabled;
    private KeyboardNavigation keyboardNavigation;
    private String _fn_onTableAnchorClick;
    private ChartFunction onTableAnchorClick;
    private String pointDateFormat;
    private String _fn_pointDateFormatter;
    private ChartFunction pointDateFormatter;
    private String _fn_pointDescriptionFormatter;
    private ChartFunction pointDescriptionFormatter;
    private String _fn_screenReaderSectionFormatter;
    private ChartFunction screenReaderSectionFormatter;
    private String _fn_seriesDescriptionFormatter;
    private ChartFunction seriesDescriptionFormatter;

    public Accessibility() {
    }

    /**
     * @see #setDescribeSingleSeries(Boolean)
     */
    public Boolean getDescribeSingleSeries() {
        return describeSingleSeries;
    }

    /**
     * Whether or not to add series descriptions to charts with a single series.
     * <p>
     * Defaults to: false
     */
    public void setDescribeSingleSeries(Boolean describeSingleSeries) {
        this.describeSingleSeries = describeSingleSeries;
    }

    public Accessibility(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable accessibility features for the chart.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setKeyboardNavigation(KeyboardNavigation)
     */
    public KeyboardNavigation getKeyboardNavigation() {
        if (keyboardNavigation == null) {
            keyboardNavigation = new KeyboardNavigation();
        }
        return keyboardNavigation;
    }

    /**
     * Options for keyboard navigation.
     */
    public void setKeyboardNavigation(KeyboardNavigation keyboardNavigation) {
        this.keyboardNavigation = keyboardNavigation;
    }

    public String getOnTableAnchorClick() {
        return _fn_onTableAnchorClick;
    }

    /**
     * Sets the JavaScript function of the <code>onTableAnchorClick</code>
     * option, as the source code of the function.
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException}.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setOnTableAnchorClickFunction(ChartFunction)}, which works
     * without compiling JavaScript in the browser.
     *
     * @param _fn_onTableAnchorClick
     *            the source code of the JavaScript function
     */
    public void setOnTableAnchorClick(String _fn_onTableAnchorClick) {
        this._fn_onTableAnchorClick = _fn_onTableAnchorClick;
        this.onTableAnchorClick = null;
    }

    /**
     * @see #setOnTableAnchorClickFunction(ChartFunction)
     */
    public ChartFunction getOnTableAnchorClickFunction() {
        return onTableAnchorClick;
    }

    /**
     * Sets the function of the <code>onTableAnchorClick</code> option, declared
     * in a {@link com.vaadin.flow.js.JsDefinition JsDefinition} interface. The
     * browser runs the function from the frontend bundle, so it works under a
     * Content Security Policy without <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setOnTableAnchorClick(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAiController</code>.
     *
     * @param onTableAnchorClick
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setOnTableAnchorClickFunction(
            ChartFunction onTableAnchorClick) {
        this.onTableAnchorClick = onTableAnchorClick;
        this._fn_onTableAnchorClick = null;
    }

    /**
     * @see #setPointDateFormat(String)
     */
    public String getPointDateFormat() {
        return pointDateFormat;
    }

    /**
     * <p>
     * Date format to use for points on datetime axes when describing them to
     * screen reader users.
     * </p>
     * <p>
     * Defaults to the same format as in tooltip.
     * </p>
     * <p>
     * For an overview of the replacement codes, see
     * <a href="#Highcharts.dateFormat">dateFormat</a>.
     * </p>
     */
    public void setPointDateFormat(String pointDateFormat) {
        this.pointDateFormat = pointDateFormat;
    }

    public String getPointDateFormatter() {
        return _fn_pointDateFormatter;
    }

    /**
     * Sets the JavaScript function of the <code>pointDateFormatter</code>
     * option, as the source code of the function.
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException}.
     * Where a format string is enough, use {@link #setPointDateFormat(String)},
     * which needs no JavaScript.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setPointDateFormatterFunction(ChartFunction)}, which works
     * without compiling JavaScript in the browser.
     *
     * @param _fn_pointDateFormatter
     *            the source code of the JavaScript function
     */
    public void setPointDateFormatter(String _fn_pointDateFormatter) {
        this._fn_pointDateFormatter = _fn_pointDateFormatter;
        this.pointDateFormatter = null;
    }

    /**
     * @see #setPointDateFormatterFunction(ChartFunction)
     */
    public ChartFunction getPointDateFormatterFunction() {
        return pointDateFormatter;
    }

    /**
     * Sets the function of the <code>pointDateFormatter</code> option, declared
     * in a {@link com.vaadin.flow.js.JsDefinition JsDefinition} interface. The
     * browser runs the function from the frontend bundle, so it works under a
     * Content Security Policy without <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setPointDateFormatter(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAiController</code>.
     *
     * @param pointDateFormatter
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setPointDateFormatterFunction(
            ChartFunction pointDateFormatter) {
        this.pointDateFormatter = pointDateFormatter;
        this._fn_pointDateFormatter = null;
    }

    public String getPointDescriptionFormatter() {
        return _fn_pointDescriptionFormatter;
    }

    /**
     * Sets the JavaScript function of the
     * <code>pointDescriptionFormatter</code> option, as the source code of the
     * function.
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException}.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setPointDescriptionFormatterFunction(ChartFunction)}, which works
     * without compiling JavaScript in the browser.
     *
     * @param _fn_pointDescriptionFormatter
     *            the source code of the JavaScript function
     */
    public void setPointDescriptionFormatter(
            String _fn_pointDescriptionFormatter) {
        this._fn_pointDescriptionFormatter = _fn_pointDescriptionFormatter;
        this.pointDescriptionFormatter = null;
    }

    /**
     * @see #setPointDescriptionFormatterFunction(ChartFunction)
     */
    public ChartFunction getPointDescriptionFormatterFunction() {
        return pointDescriptionFormatter;
    }

    /**
     * Sets the function of the <code>pointDescriptionFormatter</code> option,
     * declared in a {@link com.vaadin.flow.js.JsDefinition JsDefinition}
     * interface. The browser runs the function from the frontend bundle, so it
     * works under a Content Security Policy without <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setPointDescriptionFormatter(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAiController</code>.
     *
     * @param pointDescriptionFormatter
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setPointDescriptionFormatterFunction(
            ChartFunction pointDescriptionFormatter) {
        this.pointDescriptionFormatter = pointDescriptionFormatter;
        this._fn_pointDescriptionFormatter = null;
    }

    public String getScreenReaderSectionFormatter() {
        return _fn_screenReaderSectionFormatter;
    }

    /**
     * Sets the JavaScript function of the
     * <code>screenReaderSectionFormatter</code> option, as the source code of
     * the function.
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException}.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setScreenReaderSectionFormatterFunction(ChartFunction)}, which
     * works without compiling JavaScript in the browser.
     *
     * @param _fn_screenReaderSectionFormatter
     *            the source code of the JavaScript function
     */
    public void setScreenReaderSectionFormatter(
            String _fn_screenReaderSectionFormatter) {
        this._fn_screenReaderSectionFormatter = _fn_screenReaderSectionFormatter;
        this.screenReaderSectionFormatter = null;
    }

    /**
     * @see #setScreenReaderSectionFormatterFunction(ChartFunction)
     */
    public ChartFunction getScreenReaderSectionFormatterFunction() {
        return screenReaderSectionFormatter;
    }

    /**
     * Sets the function of the <code>screenReaderSectionFormatter</code>
     * option, declared in a {@link com.vaadin.flow.js.JsDefinition
     * JsDefinition} interface. The browser runs the function from the frontend
     * bundle, so it works under a Content Security Policy without
     * <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setScreenReaderSectionFormatter(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAiController</code>.
     *
     * @param screenReaderSectionFormatter
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setScreenReaderSectionFormatterFunction(
            ChartFunction screenReaderSectionFormatter) {
        this.screenReaderSectionFormatter = screenReaderSectionFormatter;
        this._fn_screenReaderSectionFormatter = null;
    }

    public String getSeriesDescriptionFormatter() {
        return _fn_seriesDescriptionFormatter;
    }

    /**
     * Sets the JavaScript function of the
     * <code>seriesDescriptionFormatter</code> option, as the source code of the
     * function.
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException}.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setSeriesDescriptionFormatterFunction(ChartFunction)}, which
     * works without compiling JavaScript in the browser.
     *
     * @param _fn_seriesDescriptionFormatter
     *            the source code of the JavaScript function
     */
    public void setSeriesDescriptionFormatter(
            String _fn_seriesDescriptionFormatter) {
        this._fn_seriesDescriptionFormatter = _fn_seriesDescriptionFormatter;
        this.seriesDescriptionFormatter = null;
    }

    /**
     * @see #setSeriesDescriptionFormatterFunction(ChartFunction)
     */
    public ChartFunction getSeriesDescriptionFormatterFunction() {
        return seriesDescriptionFormatter;
    }

    /**
     * Sets the function of the <code>seriesDescriptionFormatter</code> option,
     * declared in a {@link com.vaadin.flow.js.JsDefinition JsDefinition}
     * interface. The browser runs the function from the frontend bundle, so it
     * works under a Content Security Policy without <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setSeriesDescriptionFormatter(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAiController</code>.
     *
     * @param seriesDescriptionFormatter
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setSeriesDescriptionFormatterFunction(
            ChartFunction seriesDescriptionFormatter) {
        this.seriesDescriptionFormatter = seriesDescriptionFormatter;
        this._fn_seriesDescriptionFormatter = null;
    }
}
