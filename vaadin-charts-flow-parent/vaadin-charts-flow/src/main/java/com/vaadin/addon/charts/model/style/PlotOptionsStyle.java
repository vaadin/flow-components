package com.vaadin.addon.charts.model.style;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */
import com.vaadin.addon.charts.model.AbstractConfigurationObject;
import com.vaadin.addon.charts.model.PlotOptionsArea;
import com.vaadin.addon.charts.model.PlotOptionsArearange;
import com.vaadin.addon.charts.model.PlotOptionsAreaspline;
import com.vaadin.addon.charts.model.PlotOptionsAreasplinerange;
import com.vaadin.addon.charts.model.PlotOptionsBar;
import com.vaadin.addon.charts.model.PlotOptionsBoxplot;
import com.vaadin.addon.charts.model.PlotOptionsBubble;
import com.vaadin.addon.charts.model.PlotOptionsCandlestick;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.PlotOptionsColumnrange;
import com.vaadin.addon.charts.model.PlotOptionsErrorbar;
import com.vaadin.addon.charts.model.PlotOptionsFlags;
import com.vaadin.addon.charts.model.PlotOptionsFunnel;
import com.vaadin.addon.charts.model.PlotOptionsGauge;
import com.vaadin.addon.charts.model.PlotOptionsHeatmap;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.PlotOptionsOhlc;
import com.vaadin.addon.charts.model.PlotOptionsPie;
import com.vaadin.addon.charts.model.PlotOptionsPolygon;
import com.vaadin.addon.charts.model.PlotOptionsPyramid;
import com.vaadin.addon.charts.model.PlotOptionsScatter;
import com.vaadin.addon.charts.model.PlotOptionsSeries;
import com.vaadin.addon.charts.model.PlotOptionsSolidgauge;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.PlotOptionsTreemap;
import com.vaadin.addon.charts.model.PlotOptionsWaterfall;

/**
 * Styles for PlotOptions of different plot types
 */
@SuppressWarnings("serial")
public class PlotOptionsStyle extends AbstractConfigurationObject {
    private PlotOptionsArea area = new PlotOptionsArea();
    private PlotOptionsArearange arearange = new PlotOptionsArearange();
    private PlotOptionsAreaspline areaspline = new PlotOptionsAreaspline();
    private PlotOptionsAreasplinerange areasplinerange = new PlotOptionsAreasplinerange();
    private PlotOptionsBar bar = new PlotOptionsBar();
    private PlotOptionsBoxplot boxplot = new PlotOptionsBoxplot();
    private PlotOptionsBubble bubble = new PlotOptionsBubble();
    private PlotOptionsColumn column = new PlotOptionsColumn();
    private PlotOptionsColumnrange columnrange = new PlotOptionsColumnrange();
    private PlotOptionsErrorbar errorbar = new PlotOptionsErrorbar();
    private PlotOptionsFunnel funnel = new PlotOptionsFunnel();
    private PlotOptionsGauge gauge = new PlotOptionsGauge();
    private PlotOptionsHeatmap heatmap = new PlotOptionsHeatmap();
    private PlotOptionsLine line = new PlotOptionsLine();
    private PlotOptionsPie pie = new PlotOptionsPie();
    private PlotOptionsPolygon polygon = new PlotOptionsPolygon();
    private PlotOptionsPyramid pyramid = new PlotOptionsPyramid();
    private PlotOptionsScatter scatter = new PlotOptionsScatter();
    private PlotOptionsSeries series = new PlotOptionsSeries();
    private PlotOptionsSolidgauge solidgauge = new PlotOptionsSolidgauge();
    private PlotOptionsSpline spline = new PlotOptionsSpline();
    private PlotOptionsTreemap treemap = new PlotOptionsTreemap();
    private PlotOptionsWaterfall waterfall = new PlotOptionsWaterfall();

    private PlotOptionsCandlestick candlestick = new PlotOptionsCandlestick();
    private PlotOptionsFlags flags = new PlotOptionsFlags();
    private PlotOptionsOhlc ohlc = new PlotOptionsOhlc();

    /**
     * @see #setBar(PlotOptionsBar)
     */
    public PlotOptionsBar getBar() {
        return bar;
    }

    /**
     * Sets the style options for {@link ChartType#BAR} charts
     *
     * @param bar
     */
    public void setBar(PlotOptionsBar bar) {
        this.bar = bar;
    }

    /**
     * @see #setArea(PlotOptionsArea)
     */
    public PlotOptionsArea getArea() {
        return area;
    }

    /**
     * Sets the style options for {@link ChartType#AREA} charts
     *
     * @param area
     */
    public void setArea(PlotOptionsArea area) {
        this.area = area;
    }

    /**
     * @see #setPie(PlotOptionsPie)
     */
    public PlotOptionsPie getPie() {
        return pie;
    }

    /**
     * Sets the style options for {@link ChartType#PIE} charts
     *
     * @param pie
     */
    public void setPie(PlotOptionsPie pie) {
        this.pie = pie;
    }

    /**
     * @see #setLine(PlotOptionsLine)
     */
    public PlotOptionsLine getLine() {
        return line;
    }

    /**
     * Sets the style options for {@link ChartType#LINE} charts
     *
     * @param line
     */
    public void setLine(PlotOptionsLine line) {
        this.line = line;
    }

    /**
     * @see #setColumn(PlotOptionsColumn)
     */
    public PlotOptionsColumn getColumn() {
        return column;
    }

    /**
     * Sets the style options for {@link ChartType#COLUMN} charts
     *
     * @param column
     */
    public void setColumn(PlotOptionsColumn column) {
        this.column = column;
    }

    /**
     * @see #setSpline(PlotOptionsSpline)
     */
    public PlotOptionsSpline getSpline() {
        return spline;
    }

    /**
     * Sets the style options for {@link ChartType#SPLINE} charts
     *
     * @param spline
     */
    public void setSpline(PlotOptionsSpline spline) {
        this.spline = spline;
    }

    /**
     * @see #setSeries(PlotOptionsSeries)
     */
    public PlotOptionsSeries getSeries() {
        return series;
    }

    /**
     * Sets the style options for all type of charts
     *
     * @param series
     */
    public void setSeries(PlotOptionsSeries series) {
        this.series = series;
    }

    /**
     * @see #setArearange(PlotOptionsArearange)
     */
    public PlotOptionsArearange getArearange() {
        return arearange;
    }

    /**
     * Sets the style options for {@link ChartType#AREARANGE} charts
     *
     * @param arearange
     */
    public void setArearange(PlotOptionsArearange arearange) {
        this.arearange = arearange;
    }

    /**
     * @see #setAreasplinerange(PlotOptionsAreasplinerange)
     */
    public PlotOptionsAreasplinerange getAreasplinerange() {
        return areasplinerange;
    }

    /**
     * Sets the style options for {@link ChartType#AREASPLINERANGE} charts
     *
     * @param areasplinerange
     */
    public void setAreasplinerange(PlotOptionsAreasplinerange areasplinerange) {
        this.areasplinerange = areasplinerange;
    }

    /**
     * @see #setAreaspline(PlotOptionsAreaspline)
     */
    public PlotOptionsAreaspline getAreaspline() {
        return areaspline;
    }

    /**
     * Sets the style options for {@link ChartType#AREASPLINE} charts
     *
     * @param areaspline
     */
    public void setAreaspline(PlotOptionsAreaspline areaspline) {
        this.areaspline = areaspline;
    }

    /**
     * @see #setPyramid(PlotOptionsPyramid)
     */
    public PlotOptionsPyramid getPyramid() {
        return pyramid;
    }

    /**
     * Sets the style options for {@link ChartType#PYRAMID} charts
     *
     * @param pyramid
     */
    public void setPyramid(PlotOptionsPyramid pyramid) {
        this.pyramid = pyramid;
    }

    /**
     * @see #setWaterfall(PlotOptionsWaterfall)
     */
    public PlotOptionsWaterfall getWaterfall() {
        return waterfall;
    }

    /**
     * Sets the style options for {@link ChartType#WATERFALL} charts
     *
     * @param waterfall
     */
    public void setWaterfall(PlotOptionsWaterfall waterfall) {
        this.waterfall = waterfall;
    }

    public PlotOptionsTreemap getTreeMap() {
        return treemap;
    }

    /**
     * Sets the style options for {@link ChartType#TREEMAP} charts
     *
     * @param treemap
     */
    public void setTreemap(PlotOptionsTreemap treemap) {
        this.treemap = treemap;
    }

    /**
     * @see #setPolygon(PlotOptionsPolygon)
     */
    public PlotOptionsPolygon getPolygon() {
        return polygon;
    }

    /**
     * Sets the style options for {@link ChartType#POLYGON} charts
     *
     * @param polygon
     */
    public void setPolygon(PlotOptionsPolygon polygon) {
        this.polygon = polygon;
    }

    /**
     * @see #setBoxplot(PlotOptionsBoxplot)
     */
    public PlotOptionsBoxplot getBoxplot() {
        return boxplot;
    }

    /**
     * Sets the style options for {@link ChartType#BOXPLOT} charts
     *
     * @param boxplot
     */
    public void setBoxplot(PlotOptionsBoxplot boxplot) {
        this.boxplot = boxplot;
    }

    /**
     * @see #setBubble(PlotOptionsBubble)
     */
    public PlotOptionsBubble getBubble() {
        return bubble;
    }

    /**
     * Sets the style options for {@link ChartType#BUBBLE} charts
     *
     * @param bubble
     */
    public void setBubble(PlotOptionsBubble bubble) {
        this.bubble = bubble;
    }

    public PlotOptionsColumnrange getColumnrange() {
        return columnrange;
    }

    /**
     * Sets the style options for {@link ChartType#COLUMNRANGE} charts
     *
     * @param columnrange
     */
    public void setColumnrange(PlotOptionsColumnrange columnrange) {
        this.columnrange = columnrange;
    }

    public PlotOptionsErrorbar getErrorbar() {
        return errorbar;
    }

    /**
     * Sets the style options for {@link ChartType#ERRORBAR} charts
     *
     * @param errorbar
     */
    public void setErrorbar(PlotOptionsErrorbar errorbar) {
        this.errorbar = errorbar;
    }

    /**
     * @see #setFunnel(PlotOptionsFunnel)
     */
    public PlotOptionsFunnel getFunnel() {
        return funnel;
    }

    /**
     * Sets the style options for {@link ChartType#FUNNEL} charts
     *
     * @param funnel
     */
    public void setFunnel(PlotOptionsFunnel funnel) {
        this.funnel = funnel;
    }

    /**
     * @see #setGauge(PlotOptionsGauge)
     */
    public PlotOptionsGauge getGauge() {
        return gauge;
    }

    /**
     * Sets the style options for {@link ChartType#GAUGE} charts
     *
     * @param gauge
     */
    public void setGauge(PlotOptionsGauge gauge) {
        this.gauge = gauge;
    }

    public PlotOptionsHeatmap getHeatmap() {
        return heatmap;
    }

    /**
     * Sets the style options for {@link ChartType#HEATMAP} charts
     *
     * @param heatmap
     */
    public void setHeatmap(PlotOptionsHeatmap heatmap) {
        this.heatmap = heatmap;
    }

    public PlotOptionsScatter getScatter() {
        return scatter;
    }

    /**
     * Sets the style options for {@link ChartType#SCATTER} charts
     *
     * @param scatter
     */
    public void setScatter(PlotOptionsScatter scatter) {
        this.scatter = scatter;
    }

    public PlotOptionsSolidgauge getSolidgauge() {
        return solidgauge;
    }

    /**
     * Sets the style options for {@link ChartType#SOLIDGAUGE} charts
     *
     * @param solidgauge
     */
    public void setSolidgauge(PlotOptionsSolidgauge solidgauge) {
        this.solidgauge = solidgauge;
    }

    /**
     * @see #setTreemap(PlotOptionsTreemap)
     */
    public PlotOptionsTreemap getTreemap() {
        return treemap;
    }

    public PlotOptionsCandlestick getCandlestick() {
        return candlestick;
    }

    /**
     * Sets the style options for {@link ChartType#CANDLESTICK} charts
     *
     * @param candlestick
     */
    public void setCandlestick(PlotOptionsCandlestick candlestick) {
        this.candlestick = candlestick;
    }

    /**
     * @see #setFlags(PlotOptionsFlags)
     */
    public PlotOptionsFlags getFlags() {
        return flags;
    }

    /**
     * Sets the style options for {@link ChartType#FLAGS} charts
     *
     * @param flags
     */
    public void setFlags(PlotOptionsFlags flags) {
        this.flags = flags;
    }

    /**
     * @see #setOhlc(PlotOptionsOhlc)
     */
    public PlotOptionsOhlc getOhlc() {
        return ohlc;
    }

    /**
     * Sets the style options for {@link ChartType#OHLC} charts
     *
     * @param ohlc
     */
    public void setOhlc(PlotOptionsOhlc ohlc) {
        this.ohlc = ohlc;
    }
}
