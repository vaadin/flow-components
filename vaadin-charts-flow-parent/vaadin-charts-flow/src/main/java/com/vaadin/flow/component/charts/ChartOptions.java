/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.model.style.Theme;
import com.vaadin.flow.component.charts.util.ChartSerialization;

import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

/**
 * The ChartOptions configures a page local global options like localized texts
 * for charts.
 * <p>
 * Use {@link ChartOptions#get()} or {@link ChartOptions#get(UI)} to get an
 * instance for the current or specified {@link UI}.
 */
public class ChartOptions extends AbstractConfigurationObject {

    @JsonUnwrapped
    private Theme theme;
    private Lang lang;
    private transient JreJsonFactory jsonFactory;

    protected ChartOptions() {
    }

    private JreJsonFactory getJsonFactory() {
        if (jsonFactory == null) {
            jsonFactory = new JreJsonFactory();
        }
        return jsonFactory;
    }

    private void updateOptions() {
        UI ui = UI.getCurrent();

        if (ui == null) {
            return;
        }

        JsonObject configurationNode = getJsonFactory()
                .parse(ChartSerialization.toJSON(this));
        ui.getElement().executeJs(
                "customElements.get('vaadin-chart').__callHighchartsFunction('setOptions',$0,$1)",
                true, configurationNode);
    }

    /**
     * Changes the language of all charts.
     *
     * @param lang
     */
    public void setLang(Lang lang) {
        this.lang = lang;
        updateOptions();
    }

    /**
     * Returns the {@link Lang} in use or {@code null} if no lang configuration
     * has been set.
     *
     * @return the {@link Lang} in use or {@code null}.
     */
    public Lang getLang() {
        return lang;
    }

    /**
     * Returns the {@link Theme} in use or {@code null} if no theme has been
     * set.
     *
     * @return the {@link Theme} in use or {@code null}.
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Sets the theme to use.
     * <p/>
     * Note that if the view is already drawn, all existing {@link Chart}s will
     * be redrawn.
     *
     * @param theme
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
        updateOptions();
    }

    /**
     * Returns a ChartOptions instance for the given UI. If a ChartOptions
     * extension has not yet been added, a new one is created and added.
     *
     * @param ui
     *            the UI for which the ChartOptions should be returned, not
     *            <code>null</code>
     * @return the ChartOptions instance connected to the given UI
     */
    public static ChartOptions get(UI ui) {
        Objects.requireNonNull(ui, "Given UI may not be null");

        ChartOptions options = ComponentUtil.getData(ui, ChartOptions.class);

        // Create new options if not found
        if (options == null) {
            options = new ChartOptions();
            ComponentUtil.setData(ui, ChartOptions.class, options);
        }

        return options;

    }

    /**
     * Returns a ChartOptions instance for the current UI. If a ChartOptions
     * extension has not yet been added, a new one is created and added.
     *
     * @return a ChartOptions instance connected to the currently active UI
     */
    public static ChartOptions get() {
        UI ui = UI.getCurrent();

        if (ui == null) {
            throw new IllegalStateException(
                    "This method must be used from UI thread");
        }
        return get(ui);
    }

}
