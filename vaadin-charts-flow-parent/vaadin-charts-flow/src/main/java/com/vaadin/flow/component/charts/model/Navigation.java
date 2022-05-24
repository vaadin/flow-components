package com.vaadin.flow.component.charts.model;

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

import com.vaadin.flow.component.charts.model.style.Style;

/**
 * A collection of options for buttons and menus appearing in the exporting
 * module.
 */
public class Navigation extends AbstractConfigurationObject {

    private ButtonOptions buttonOptions;
    private Style menuItemHoverStyle;
    private Style menuItemStyle;
    private Style menuStyle;

    public Navigation() {
    }

    /**
     * @see #setButtonOptions(ButtonOptions)
     */
    public ButtonOptions getButtonOptions() {
        if (buttonOptions == null) {
            buttonOptions = new ButtonOptions();
        }
        return buttonOptions;
    }

    /**
     * <p>
     * A collection of options for buttons appearing in the exporting module.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the buttons are styled with the
     * <code>.highcharts-contextbutton</code> and
     * <code>.highcharts-button-symbol</code> class.
     * </p>
     */
    public void setButtonOptions(ButtonOptions buttonOptions) {
        this.buttonOptions = buttonOptions;
    }

    /**
     * @see #setMenuItemHoverStyle(Style)
     */
    public Style getMenuItemHoverStyle() {
        if (menuItemHoverStyle == null) {
            menuItemHoverStyle = new Style();
        }
        return menuItemHoverStyle;
    }

    /**
     * CSS styles for the hover state of the individual items within the popup
     * menu appearing by default when the export icon is clicked. The menu items
     * are rendered in HTML.
     * <p>
     * Defaults to: { "background": "#335cad", "color": "#ffffff" }
     */
    public void setMenuItemHoverStyle(Style menuItemHoverStyle) {
        this.menuItemHoverStyle = menuItemHoverStyle;
    }

    /**
     * @see #setMenuItemStyle(Style)
     */
    public Style getMenuItemStyle() {
        if (menuItemStyle == null) {
            menuItemStyle = new Style();
        }
        return menuItemStyle;
    }

    /**
     * CSS styles for the individual items within the popup menu appearing by
     * default when the export icon is clicked. The menu items are rendered in
     * HTML.
     * <p>
     * Defaults to: { "padding": "0.5em 1em", "color": "#333333", "background":
     * "none" }
     */
    public void setMenuItemStyle(Style menuItemStyle) {
        this.menuItemStyle = menuItemStyle;
    }

    /**
     * @see #setMenuStyle(Style)
     */
    public Style getMenuStyle() {
        if (menuStyle == null) {
            menuStyle = new Style();
        }
        return menuStyle;
    }

    /**
     * CSS styles for the popup menu appearing by default when the export icon
     * is clicked. This menu is rendered in HTML.
     * <p>
     * Defaults to: { "border": "1px solid #999999", "background": "#ffffff",
     * "padding": "5px 0" }
     */
    public void setMenuStyle(Style menuStyle) {
        this.menuStyle = menuStyle;
    }
}
