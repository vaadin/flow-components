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

package com.vaadin.flow.component.charts.export;

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.model.style.Theme;

/**
 * <p>
 * Optional settings for exporting charts in the server.
 * </p>
 *
 * <p>
 * Options include:
 * <ul>
 * <li>width: Width of the exported image.</li>
 * <li>height: Height of the exported image.</li>
 * <li>theme: Theme used to style the chart. For example:
 * {@link com.vaadin.flow.component.charts.themes.LumoDarkTheme}</li>
 * <li>lang: Lang specifications for internationalization purposes.</li>
 * <li>timeline: Determines if the generated chart is in timeline mode.</li>
 * <li>executeFunctions: execute JavaScript functions (for example: formatter
 * functions)</li>
 * </ul>
 * </p>
 *
 * @see SVGGenerator
 *
 * @since 21.0
 */
public class ExportOptions extends AbstractConfigurationObject {

    private Theme theme;
    private Lang lang;
    private Number width;
    private Number height;
    private Boolean timeline;
    private Boolean executeFunctions;

    public ExportOptions() {
        super();
    }

    // <editor-fold desc="Getters and setters">

    /**
     * Get the {@link Theme} used to style the chart.
     *
     * @return the {@link Theme} used to style the chart.
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Set the {@link Theme} used to style the chart.
     *
     * @param theme
     *            the theme to use when styling the chart.
     * @see com.vaadin.flow.component.charts.themes.LumoLightTheme
     * @see com.vaadin.flow.component.charts.themes.LumoDarkTheme
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    /**
     * Get the {@link Lang} object used to add i18n features to the chart.
     *
     * @return the lang object used to provide i18n to the chart.
     */
    public Lang getLang() {
        return lang;
    }

    /**
     * Set the {@link Lang} object used to add i18n features to the chart.
     *
     * @param lang
     *            the {@link Lang} object used to provide the i18n features of
     *            the chart.
     */
    public void setLang(Lang lang) {
        this.lang = lang;
    }

    /**
     * Get the width the resulting chart will have.
     *
     * @return the width the resulting chart will have.
     */
    public Number getWidth() {
        return width;
    }

    /**
     * Set the width the resulting chart will have.
     *
     * @param width
     *            the width the resulting chart will have.
     */
    public void setWidth(Number width) {
        this.width = width;
    }

    /**
     * Get the height the resulting chart will have.
     *
     * @return the height the resulting chart will have.
     */
    public Number getHeight() {
        return height;
    }

    /**
     * Get the height the resulting chart will have.
     *
     * @param height
     *            the height the resulting chart will have.
     */
    public void setHeight(Number height) {
        this.height = height;
    }

    /**
     * Check flag to see if the generated chart will be in timeline mode.
     *
     * @return <code>true</code> if generated chart will be in timeline mode,
     *         <code>false</code> otherwise.
     */
    public Boolean getTimeline() {
        return timeline;
    }

    /**
     * Set flag to mark the generated chart in timeline mode.
     *
     * @param timeline
     *            if the generated chart should be in timeline mode.
     */
    public void setTimeline(Boolean timeline) {
        this.timeline = timeline;
    }

    /**
     * Check flag to see if generators will execute JavaScript functions when
     * using a {@link com.vaadin.flow.component.charts.model.Configuration} with
     * JS functions.
     *
     * @return <code>true</code> if the generator will execute JavaScript
     *         functions, <code>false</code> otherwise.
     */
    public boolean getExecuteFunctions() {
        return executeFunctions;
    }

    /**
     * Set flag to execute JS functions when exporting charts.
     *
     * @param executeFunctions
     *            if the generator should execute JS functions.
     */
    public void setExecuteFunctions(Boolean executeFunctions) {
        this.executeFunctions = executeFunctions;
    }
    // </editor-fold>
}
