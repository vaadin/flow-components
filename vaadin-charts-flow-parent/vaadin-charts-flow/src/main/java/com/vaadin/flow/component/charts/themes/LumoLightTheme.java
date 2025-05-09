/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.themes;

import com.vaadin.flow.component.charts.model.AbstractDataLabels;
import com.vaadin.flow.component.charts.model.style.AxisStyle;
import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.FontWeight;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.charts.model.style.Style;
import com.vaadin.flow.component.charts.model.style.Theme;
import com.vaadin.flow.component.charts.model.style.TickIntervalStyle;

/**
 * The default theme for Vaadin Charts. Suitable for use with the Lumo theme for
 * Vaadin.
 */
@SuppressWarnings("serial")
public class LumoLightTheme extends Theme {

    protected static final int BORDER_RADIUS = 4;
    protected final static SolidColor COLOR1 = new SolidColor(90, 194, 247);
    protected final static SolidColor COLOR2 = new SolidColor(22, 118, 243);
    protected final static SolidColor COLOR3 = new SolidColor(255, 125, 148);
    protected final static SolidColor COLOR4 = new SolidColor(197, 22, 78);
    protected final static SolidColor COLOR5 = new SolidColor(21, 193, 93);
    protected final static SolidColor COLOR6 = new SolidColor(14, 129, 81);
    protected final static SolidColor COLOR7 = new SolidColor(193, 142, 210);
    protected final static SolidColor COLOR8 = new SolidColor(146, 51, 179);
    protected final static SolidColor COLOR9 = new SolidColor(253, 162, 83);
    protected final static SolidColor COLOR10 = new SolidColor(226, 73, 50);
    protected final static SolidColor COLOR11 = new SolidColor(127, 176, 83);
    protected final static SolidColor COLOR12 = new SolidColor(187, 168, 91);
    protected final static SolidColor COLOR13 = new SolidColor(36, 121, 129);
    protected final static SolidColor COLOR14 = new SolidColor(150, 57, 112);
    protected final static SolidColor COLOR15 = new SolidColor(75, 86, 168);
    protected final static SolidColor COLOR16 = new SolidColor(154, 89, 61);

    protected final static SolidColor TITLE_COLOR = new SolidColor(25, 36, 52);
    protected final static SolidColor TOOLTIP_TEXT_COLOR = new SolidColor(24,
            39, 57, 0.94);
    protected final static SolidColor TOOLTIP_BACKGROUND_COLOR = new SolidColor(
            255, 255, 255);
    protected final static SolidColor SUBTITLE_COLOR = new SolidColor(27, 43,
            65, 0.72);
    protected final static SolidColor LINE_COLOR = new SolidColor(28, 55, 90,
            0.16);
    protected final static SolidColor TEXT_COLOR = new SolidColor(27, 43, 65,
            0.72);
    protected final static SolidColor GRID_COLOR = new SolidColor(28, 55, 90,
            0.16);
    protected final static SolidColor LABEL_COLOR = new SolidColor(27, 43, 65,
            0.72);

    protected final static SolidColor BGCOLOR = new SolidColor(255, 255, 255);
    protected final static SolidColor BGCOLOR_LIGHT_GRAY = new SolidColor(25,
            59, 103, 0.05);
    protected final static SolidColor TRANSPARENT_COLOR = new SolidColor(255,
            255, 255, 0.0);

    protected final static String DEFAULT_FONT_FAMILIES = "var(--lumo-font-family)";
    protected static final Color LEGEND_TEXT_COLOR = new SolidColor(27, 43, 65,
            0.72);

    public LumoLightTheme() {
        setColors(COLOR1, COLOR2, COLOR3, COLOR4, COLOR5, COLOR6, COLOR7,
                COLOR8, COLOR9, COLOR10, COLOR11, COLOR12, COLOR13, COLOR14,
                COLOR15, COLOR16);
        Style style = new Style();
        style.setFontFamily(DEFAULT_FONT_FAMILIES);
        style.setFontSize("14px");
        getChart().setStyle(style);

        getTitle().setColor(TITLE_COLOR);
        getTitle().setFontSize("26px");
        getTitle().setFontWeight(FontWeight.NORMAL);

        getSubtitle().setColor(SUBTITLE_COLOR);
        getSubtitle().setFontWeight(FontWeight.NORMAL);
        getSubtitle().setFontSize("14px");

        getChart().setClassName("vaadin-chart");
        getChart().setPlotBackgroundColor(TRANSPARENT_COLOR);
        getChart().setPlotBackgroundImage("");
        getChart().setPlotBorderColor(TRANSPARENT_COLOR);
        getChart().setBackgroundColor(BGCOLOR);
        getChart().setPlotBackgroundColor(TRANSPARENT_COLOR);
        getChart().setPlotBorderWidth(0);
        getChart().setBorderRadius(0);
        getChart().setPlotShadow(false);

        setAxisDefaults(getxAxis());

        setAxisDefaults(getyAxis());

        getTooltip().setBackgroundColor(TOOLTIP_BACKGROUND_COLOR);
        getTooltip().setBorderWidth(0);
        getTooltip().setBorderRadius(BORDER_RADIUS);
        getTooltip().getStyle().setColor(TOOLTIP_TEXT_COLOR);

        getLegend().setItemStyle(new Style());
        getLegend().getItemStyle().setColor(LEGEND_TEXT_COLOR);
        getLegend().getItemStyle().setFontWeight(FontWeight.NORMAL);
        getLegend().getItemStyle().setFontSize("14px");
        Style itemHoverStyle = new Style();
        itemHoverStyle.setColor(LEGEND_TEXT_COLOR);
        getLegend().setItemHoverStyle(itemHoverStyle);
        getLegend().setItemHiddenStyle(new Style());
        getLegend().getItemHiddenStyle()
                .setColor(new SolidColor(128, 128, 128));
        getLegend().setBorderRadius(BORDER_RADIUS);
        getLegend().setBorderColor(LINE_COLOR);
        getLegend().setBackgroundColor(new SolidColor(255, 255, 255, 0.9));

        getLabels().setColor(TEXT_COLOR);

        getCredits().setStyle(new Style());
        getCredits().getStyle().setFontSize("14px");
        getCredits().getStyle().setColor(new SolidColor(128, 128, 128));
        getCredits().setText("");
        getCredits().setHref("");

        getyAxis().setMinorTickInterval(TickIntervalStyle.NONE);
        getyAxis().setGridLineColor(LINE_COLOR);
        getyAxis().setGridLineWidth(1);

        getxAxis().setGridLineColor(LINE_COLOR);
        getxAxis().setGridLineWidth(0);

        // Shadows on by default, off in range stuff
        getPlotOptions().getArearange().setShadow(false);
        getPlotOptions().getAreasplinerange().setShadow(false);
        getPlotOptions().getLine().setShadow(false);
        getPlotOptions().getSpline().setShadow(false);
        getPlotOptions().getBar().setShadow(false);
        getPlotOptions().getColumn().setShadow(false);
        getPlotOptions().getArea().setShadow(false);
        getPlotOptions().getPie().setShadow(false);
        getPlotOptions().getWaterfall().setShadow(false);

        getPlotOptions().getPie().setBorderWidth(0);
        getPlotOptions().getColumn().setBorderWidth(0);
        getPlotOptions().getColumn().setPointPadding(0);
        getPlotOptions().getBar().setBorderWidth(0);
        getPlotOptions().getBar().setPointPadding(0);
        getPlotOptions().getPyramid().setBorderWidth(0);
        getPlotOptions().getWaterfall().setBorderWidth(0);

        setDataLabelsDefaults(getPlotOptions().getArearange().getDataLabels());
        setDataLabelsDefaults(
                getPlotOptions().getAreasplinerange().getDataLabels());
        setDataLabelsDefaults(getPlotOptions().getBar().getDataLabels());
        setDataLabelsDefaults(getPlotOptions().getColumn().getDataLabels());
        setDataLabelsDefaults(getPlotOptions().getLine().getDataLabels());
        setDataLabelsDefaults(getPlotOptions().getPie().getDataLabels());
        setDataLabelsDefaults(getPlotOptions().getPyramid().getDataLabels());
        setDataLabelsDefaults(getPlotOptions().getSpline().getDataLabels());
        setDataLabelsDefaults(getPlotOptions().getWaterfall().getDataLabels());
    }

    protected void setDataLabelsDefaults(AbstractDataLabels labels) {
        labels.setColor(TEXT_COLOR);
        labels.setStyle(new Style());
        labels.getStyle().setFontFamily(DEFAULT_FONT_FAMILIES);
        labels.getStyle().setFontSize("12px");
    }

    protected void setAxisDefaults(AxisStyle style) {
        style.setGridLineColor(GRID_COLOR);
        style.setLineColor(GRID_COLOR);
        style.setLineWidth(0);
        style.setTickWidth(0);
        style.setTickColor(new SolidColor(192, 208, 224));

        style.setAlternateGridColor(new SolidColor(255, 255, 255, 0.0));

        style.getTitle().setColor(TEXT_COLOR);
        style.getTitle().setFontWeight(FontWeight.NORMAL);

        style.getSubtitle().setColor(SUBTITLE_COLOR);
        style.getSubtitle().setFontSize("14px");
        style.getSubtitle().setFontWeight(FontWeight.NORMAL);

        style.getLabels().setFontWeight(FontWeight.NORMAL);
        style.getLabels().setColor(LABEL_COLOR);
        style.getLabels().setFontSize("14px");
    }

}
