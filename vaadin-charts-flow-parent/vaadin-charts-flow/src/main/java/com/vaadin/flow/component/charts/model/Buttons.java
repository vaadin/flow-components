/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Options for the export related buttons, print and export. In addition to the
 * default buttons listed here, custom buttons can be added. See
 * <a href="#navigation.buttonOptions">navigation.buttonOptions</a> for general
 * options.
 */
public class Buttons extends AbstractConfigurationObject {

    private ContextButton contextButton;

    public Buttons() {
    }

    /**
     * @see #setContextButton(ContextButton)
     */
    public ContextButton getContextButton() {
        if (contextButton == null) {
            contextButton = new ContextButton();
        }
        return contextButton;
    }

    /**
     * <p>
     * Options for the export button.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, export button styles can be applied with the
     * <code>.highcharts-contextbutton</code> class.
     * </p>
     */
    public void setContextButton(ContextButton contextButton) {
        this.contextButton = contextButton;
    }
}
