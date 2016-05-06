package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.addon.charts.model.ChartModel;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Frame;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.Options3d;
import com.vaadin.addon.charts.model.PlotOptionsSeries;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.FontWeight;
import com.vaadin.addon.charts.model.style.GradientColor;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.AbstractSeriesData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.AxisProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.BackgroundProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.BorderStyle;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.ColorProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.GradientProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.TextProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.TitleProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.View3dData;

@SuppressWarnings("serial")
public class ChartDataToVaadinConfigWriter {

    private static final int DEFAULT_LEGEND_Y_OFFSET = 10;

    private Logger logger = Logger
            .getLogger(ChartDataToVaadinConfigWriter.class.getSimpleName());
    {
        logger.setLevel(Level.OFF);
    }

    public Configuration createConfigurationFromChartData(ChartData definition) {
        logger.info("createConfData()");

        if (definition.plotData.size() > 0)
            logger.info("*** NEXT CHART *** title: " + definition.title
                    + " , series type: "
                    + definition.plotData.get(0).getClass().getSimpleName());

        Configuration conf = new Configuration();

        if (definition.view3dData != null) {
            logger.info("view3dData: " + definition.view3dData.rotation3dAngleA
                    + "/" + definition.view3dData.rotation3dAngleB);

            conf.getChart().setOptions3d(getOptions3d(definition.view3dData));
        } else {
            logger.info("definition.view3dData is null");
        }

        convertPlotData(definition, conf);

        setDefaults(conf);

        // special handling for turning pie charts into donuts
        PieToDonutConverter.convertIfNeeded(definition, conf);

        conf.setTitle(createTitle(definition.title, definition.titleStyle));

        updateLegendPosition(conf.getLegend(), definition);

        updateLegendTextProperties(conf.getLegend(),
                definition.legendProperties.textProperties);

        updateBorder(conf.getChart(), definition.borderStyle);

        updateBackgroundColor(conf, definition.background);

        updateXAxisTitle(conf.getxAxis(), definition.xAxisProperties);
        updateYAxesTitles(conf, definition.yAxesProperties);

        if (conf.getSeries().isEmpty())
            conf.setSubTitle("*** Unsupported chart type ***");

        return conf;
    }

    private void updateYAxesTitles(Configuration conf,
            List<AxisProperties> yAxisProperties) {
        if (yAxisProperties == null || yAxisProperties.size() == 0)
            return;

        YAxis defaultyAxis = conf.getyAxis();
        updateYAxisTitle(defaultyAxis, yAxisProperties.get(0));

        for (AxisProperties axProp : yAxisProperties.subList(1,
                yAxisProperties.size())) {
            YAxis axis = new YAxis();
            axis.setOpposite(true);
            conf.addyAxis(axis);
            updateYAxisTitle(axis, axProp);
        }
    }

    private void updateBackgroundColor(Configuration conf,
            BackgroundProperties background) {

        if (background == null)
            return;

        if (background.color != null) {
            conf.getChart().setBackgroundColor(
                    createSolidColorFromColorProperties(background.color,
                            SolidColor.WHITE));
        } else if (background.gradient != null) {
            conf.getChart().setBackgroundColor(
                    createGradientFromGradientProps(background.gradient));
        }
    }

    private Color createGradientFromGradientProps(GradientProperties prop) {
        double[] grPts = calculateGradientPoints(prop.angle);

        GradientColor linear = GradientColor.createLinear(grPts[0], grPts[1],
                grPts[2], grPts[3]);

        for (Entry<Double, ColorProperties> grdStop : prop.colorStops
                .entrySet()) {
            Double position = grdStop.getKey();
            ColorProperties colorProp = grdStop.getValue();

            linear.addColorStop(
                    position,
                    createSolidColorFromColorProperties(colorProp,
                            SolidColor.WHITE));
        }

        return linear;
    }

    private double[] calculateGradientPoints(double angle) {
        // into radians and rotate 180 deg
        double angleInRad = angle * 2 * Math.PI - Math.PI;

        double x1 = Math.cos(angleInRad), y1 = Math.sin(angleInRad);

        // cos and sin map to -1..1, we need to remap to 0..1
        x1 = (x1 + 1) / 2;
        y1 = (y1 + 1) / 2;

        return new double[] { x1, y1, 1 - x1, 1 - y1 };
    }

    private SolidColor createSolidColorFromColorProperties(
            ColorProperties colorProp, SolidColor defaultColor) {
        if (colorProp == null)
            return defaultColor;

        return new SolidColor(colorProp.red, colorProp.green, colorProp.blue,
                colorProp.opacity);
    }

    private void updateXAxisTitle(XAxis axis, AxisProperties axisProperties) {
        if (axisProperties == null)
            return;

        axis.setTitle(wrapStringIntoItalicsTagIfNeeded(axisProperties.title,
                axisProperties.textProperties));

        axis.getTitle().setStyle(
                createStyleFromTextFroperties(axisProperties.textProperties));
    }

    private void updateYAxisTitle(YAxis axis, AxisProperties axisProperties) {
        if (axisProperties == null) {
            axis.setTitle((String) null);
            return;
        }

        axis.setTitle(wrapStringIntoItalicsTagIfNeeded(axisProperties.title,
                axisProperties.textProperties));

        axis.getTitle().setStyle(
                createStyleFromTextFroperties(axisProperties.textProperties));
    }

    private void updateLegendTextProperties(Legend legend, TextProperties textPr) {
        Style style = createStyleFromTextFroperties(textPr);
        legend.setItemStyle(style);
    }

    private void updateBorder(ChartModel chart, BorderStyle borderStyle) {
        chart.setBorderRadius(borderStyle.radius);
        chart.setBorderWidth(borderStyle.width);
        chart.setBorderColor(createSolidColorFromColorProperties(
                borderStyle.color, null));
    }

    private Options3d getOptions3d(View3dData view3dData) {
        logger.info("getOptions3d()");

        Options3d options3d = new Options3d();
        options3d.setEnabled(true);
        options3d.setAlpha(view3dData.rotation3dAngleA);
        options3d.setBeta(view3dData.rotation3dAngleB);
        options3d.setDepth(100);
        options3d.setViewDistance(400);
        Frame frame = new Frame();
        options3d.setFrame(frame);
        return options3d;
    }

    private void updateLegendPosition(Legend legend, ChartData definition) {
        logger.info("updateLegend()");

        switch (definition.legendProperties.position) {
        case NONE:
            legend.setEnabled(false);
            break;
        case RIGHT:
            legend.setVerticalAlign(VerticalAlign.MIDDLE);
            legend.setAlign(HorizontalAlign.RIGHT);
            legend.setLayout(LayoutDirection.VERTICAL);
            break;
        case BOTTOM:
            legend.setVerticalAlign(VerticalAlign.BOTTOM);
            legend.setAlign(HorizontalAlign.CENTER);
            legend.setLayout(LayoutDirection.HORIZONTAL);
            break;
        case LEFT:
            legend.setVerticalAlign(VerticalAlign.MIDDLE);
            legend.setAlign(HorizontalAlign.LEFT);
            legend.setLayout(LayoutDirection.VERTICAL);
            break;
        case TOP:
            legend.setVerticalAlign(VerticalAlign.TOP);
            legend.setAlign(HorizontalAlign.CENTER);
            legend.setLayout(LayoutDirection.HORIZONTAL);
            // if legend is aligned to top it overlaps the title if there is
            // one. this does not happen in a jsfiddle test, so maybe one of the
            // plugins that Vaadin Charts is loading are causing it, requires
            // further investigation. At this point we try to estimate the
            // titles' vertical size and move the legend down
            legend.setY(estimateTitleVerticalSize(definition));
            break;
        case TOP_RIGHT:
            legend.setVerticalAlign(VerticalAlign.TOP);
            legend.setAlign(HorizontalAlign.RIGHT);
            legend.setLayout(LayoutDirection.VERTICAL);
            break;
        }
    }

    private int estimateTitleVerticalSize(ChartData definition) {
        if (definition.title == null || definition.title.isEmpty())
            return 0;
        else if (definition.titleStyle != null
                && definition.titleStyle.textProperties != null
                && definition.titleStyle.textProperties.size > 0) {
            return (int) definition.titleStyle.textProperties.size;
        } else
            return DEFAULT_LEGEND_Y_OFFSET;
    }

    protected void convertPlotData(ChartData definition, Configuration conf) {
        logger.info("convertPlotData()");

        for (AbstractSeriesData series : definition.plotData) {
            AbstractSeriesDataWriter seriesDataWriter = series
                    .getSeriesDataWriter();

            seriesDataWriter.configureChart(conf);

            conf.addSeries(seriesDataWriter
                    .convertSeries(definition.blanksAsZeros));

            if (series.categories.size() > 0) {
                conf.getxAxis().setCategories(
                        series.categories.toArray(new String[] {}));
            }
        }
    }

    protected Title createTitle(String titleString, TitleProperties titleProps) {
        logger.info("createTitle()");

        if (titleString == null)
            return new Title("");

        Style style = createStyleFromTextFroperties(titleProps.textProperties);

        Title title = new Title(wrapStringIntoItalicsTagIfNeeded(titleString,
                titleProps.textProperties));

        title.setFloating(titleProps.isFloating);

        title.setStyle(style);

        return title;
    }

    private String wrapStringIntoItalicsTagIfNeeded(String string,
            TextProperties textPr) {
        if (textPr != null && textPr.italics)
            return "<i>" + string + "</i>";
        else
            return string;
    }

    private Style createStyleFromTextFroperties(TextProperties textProps) {
        Style style = new Style();

        if (textProps == null)
            return style;

        style.setColor(createSolidColorFromColorProperties(textProps.color,
                SolidColor.GREY));

        if (textProps.size > 0)
            style.setFontSize(textProps.size + "pt");

        if (textProps.fontFamily != null)
            style.setFontFamily(textProps.fontFamily);

        if (textProps.bold)
            style.setFontWeight(FontWeight.BOLD);

        // chart model style doesn't support italics

        return style;
    }

    /**
     * This is supposed to set the defaults closed to what Excel uses.
     */
    protected void setDefaults(Configuration conf) {
        logger.info("setDefaults()");

        // default in Excel
        conf.getyAxis().setTitle((String) null);

        conf.addPlotOptions(new PlotOptionsSeries() {
            {
                setAnimation(false);
                setAllowPointSelect(true);
            }
        });
    }
}
