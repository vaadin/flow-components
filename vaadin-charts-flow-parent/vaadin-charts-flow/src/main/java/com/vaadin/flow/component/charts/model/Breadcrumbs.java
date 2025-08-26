/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.ButtonTheme;
import com.vaadin.flow.component.charts.model.style.Style;

/**
 * Options for the breadcrumbs, the navigation at the top leading the way up
 * through the drilldown levels.
 */
public class Breadcrumbs extends AbstractConfigurationObject {

    private Number buttonSpacing;
    private ButtonTheme buttonTheme;
    private Boolean floating;
    private String format;
    private String _fn_formatter;
    private BreadcrumbsPosition position;
    private DrillUpButtonRelativeTo relativeTo;
    private Boolean rtl;
    private Separator separator;
    private Boolean showFullPath;
    private Style style;
    private Boolean useHTML;
    private Number zIndex;

    /**
     * @return The space between individual breadcrumb buttons in pixels.
     */
    public Number getButtonSpacing() {
        return buttonSpacing;
    }

    /**
     * Sets the space between individual breadcrumb buttons in pixels.
     */
    public void setButtonSpacing(Number buttonSpacing) {
        this.buttonSpacing = buttonSpacing;
    }

    /**
     * @return Theme options for the breadcrumb buttons.
     */
    public ButtonTheme getButtonTheme() {
        if (buttonTheme == null) {
            buttonTheme = new ButtonTheme();
        }
        return buttonTheme;
    }

    /**
     * Sets theme options for the breadcrumb buttons.
     */
    public void setButtonTheme(ButtonTheme buttonTheme) {
        this.buttonTheme = buttonTheme;
    }

    /**
     * @return Whether the breadcrumbs should float above the chart.
     */
    public Boolean isFloating() {
        return floating;
    }

    /**
     * Sets whether the breadcrumbs should float above the chart.
     */
    public void setFloating(Boolean floating) {
        this.floating = floating;
    }

    /**
     * @return Format string for the breadcrumb text.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format string for the breadcrumb text.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return Custom JavaScript formatter function for breadcrumb text.
     */
    public String getFormatter() {
        return _fn_formatter;
    }

    /**
     * Sets a custom JavaScript formatter function for breadcrumb text.
     */
    public void setFormatter(String formatter) {
        this._fn_formatter = formatter;
    }

    /**
     * @return Positioning options for the breadcrumbs navigation.
     */
    public BreadcrumbsPosition getPosition() {
        if (position == null) {
            position = new BreadcrumbsPosition();
        }
        return position;
    }

    /**
     * Sets positioning options for the breadcrumbs navigation.
     */
    public void setPosition(BreadcrumbsPosition position) {
        this.position = position;
    }

    /**
     * @return What box to align the breadcrumbs to.
     */
    public DrillUpButtonRelativeTo getRelativeTo() {
        return relativeTo;
    }

    /**
     * Sets what box to align the breadcrumbs to.
     */
    public void setRelativeTo(DrillUpButtonRelativeTo relativeTo) {
        this.relativeTo = relativeTo;
    }

    /**
     * @return Whether to use right-to-left text direction.
     */
    public Boolean isRtl() {
        return rtl;
    }

    /**
     * Sets whether to use right-to-left text direction.
     */
    public void setRtl(Boolean rtl) {
        this.rtl = rtl;
    }

    /**
     * Returns the separator configuration for breadcrumbs, which defines the
     * visual separator between breadcrumb items.
     *
     * @return the separator configuration
     */
    public Separator getSeparator() {
        if (separator == null) {
            separator = new Separator();
        }
        return separator;
    }

    /**
     * Sets the separator configuration for breadcrumbs, which defines the
     * visual separator between breadcrumb items.
     *
     * @param separator
     *            the separator configuration
     */
    public void setSeparator(Separator separator) {
        this.separator = separator;
    }

    /**
     * Returns whether the full path should be shown in breadcrumbs, including
     * all drilldown levels.
     *
     * @return true if full path is shown, false otherwise
     */
    public Boolean isShowFullPath() {
        return showFullPath;
    }

    /**
     * Sets whether the full path should be shown in breadcrumbs, including all
     * drilldown levels.
     *
     * @param showFullPath
     *            true to show full path, false otherwise
     */
    public void setShowFullPath(Boolean showFullPath) {
        this.showFullPath = showFullPath;
    }

    /**
     * Returns the CSS styles for the breadcrumb text. This can be used to
     * customize font, color, spacing, and other visual aspects of the
     * breadcrumb labels.
     *
     * @return the style configuration for breadcrumb text
     */
    public Style getStyle() {
        if (style == null) {
            style = new Style();
        }
        return style;
    }

    /**
     * Sets the CSS styles for the breadcrumb text. This can be used to
     * customize font, color, spacing, and other visual aspects of the
     * breadcrumb labels.
     *
     * @param style
     *            the style configuration for breadcrumb text
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * Returns whether to use HTML to render the breadcrumb labels.
     *
     * @return true if HTML is used, false otherwise
     */
    public Boolean isUseHTML() {
        return useHTML;
    }

    /**
     * Sets whether to use HTML to render the breadcrumb labels.
     *
     * @param useHTML
     *            true to use HTML, false otherwise
     */
    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }

    /**
     * Returns the z-index for the breadcrumbs navigation, controlling its
     * stacking order.
     *
     * @return the z-index value
     */
    public Number getzIndex() {
        return zIndex;
    }

    /**
     * Sets the z-index for the breadcrumbs navigation, controlling its stacking
     * order.
     *
     * @param zIndex
     *            the z-index value
     */
    public void setzIndex(Number zIndex) {
        this.zIndex = zIndex;
    }
}
