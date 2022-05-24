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

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.Style;

/**
 * The legend is a box containing a symbol and name for each series item or
 * point item in the chart.
 */
public class Legend extends AbstractConfigurationObject {

    private HorizontalAlign align;
    private Color backgroundColor;
    private Color borderColor;
    private Number borderRadius;
    private Number borderWidth;
    private Boolean enabled;
    private Boolean floating;
    private Number itemDistance;
    private Style itemHiddenStyle;
    private Style itemHoverStyle;
    private Number itemMarginBottom;
    private Number itemMarginTop;
    private Style itemStyle;
    private Number itemWidth;
    private String labelFormat;
    private String _fn_labelFormatter;
    private LayoutDirection layout;
    private Number lineHeight;
    private Number margin;
    private Number maxHeight;
    private LegendNavigation navigation;
    private Number padding;
    private Boolean reversed;
    private Boolean rtl;
    private Boolean shadow;
    private Boolean squareSymbol;
    private Number symbolHeight;
    private Number symbolPadding;
    private Number symbolRadius;
    private Number symbolWidth;
    private LegendTitle title;
    private Boolean useHTML;
    private VerticalAlign verticalAlign;
    private Number width;
    private Number x;
    private Number y;

    public Legend() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * <p>
     * The horizontal alignment of the legend box within the chart area. Valid
     * values are <code>left</code>, <code>center</code> and <code>right</code>.
     * </p>
     *
     * <p>
     * In the case that the legend is aligned in a corner position, the
     * <code>layout</code> option will determine whether to place it above/below
     * or on the side of the plot area.
     * </p>
     * <p>
     * Defaults to: center
     */
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    /**
     * @see #setBackgroundColor(Color)
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * The background color of the legend.
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * The color of the drawn border around the legend.
     * <p>
     * Defaults to: #999999
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderRadius(Number)
     */
    public Number getBorderRadius() {
        return borderRadius;
    }

    /**
     * The border corner radius of the legend.
     * <p>
     * Defaults to: 0
     */
    public void setBorderRadius(Number borderRadius) {
        this.borderRadius = borderRadius;
    }

    /**
     * @see #setBorderWidth(Number)
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * The width of the drawn border around the legend.
     * <p>
     * Defaults to: 0
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Legend(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the legend.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setFloating(Boolean)
     */
    public Boolean getFloating() {
        return floating;
    }

    /**
     * When the legend is floating, the plot area ignores it and is allowed to
     * be placed below it.
     * <p>
     * Defaults to: false
     */
    public void setFloating(Boolean floating) {
        this.floating = floating;
    }

    /**
     * @see #setItemDistance(Number)
     */
    public Number getItemDistance() {
        return itemDistance;
    }

    /**
     * In a legend with horizontal layout, the itemDistance defines the pixel
     * distance between each item.
     * <p>
     * Defaults to: 20
     */
    public void setItemDistance(Number itemDistance) {
        this.itemDistance = itemDistance;
    }

    /**
     * @see #setItemHiddenStyle(Style)
     */
    public Style getItemHiddenStyle() {
        if (itemHiddenStyle == null) {
            itemHiddenStyle = new Style();
        }
        return itemHiddenStyle;
    }

    /**
     * CSS styles for each legend item when the corresponding series or point is
     * hidden. Only a subset of CSS is supported, notably those options related
     * to text. Properties are inherited from <code>style</code> unless
     * overridden here.
     * <p>
     * Defaults to: { "color": "#cccccc" }
     */
    public void setItemHiddenStyle(Style itemHiddenStyle) {
        this.itemHiddenStyle = itemHiddenStyle;
    }

    /**
     * @see #setItemHoverStyle(Style)
     */
    public Style getItemHoverStyle() {
        if (itemHoverStyle == null) {
            itemHoverStyle = new Style();
        }
        return itemHoverStyle;
    }

    /**
     * CSS styles for each legend item in hover mode. Only a subset of CSS is
     * supported, notably those options related to text. Properties are
     * inherited from <code>style</code> unless overridden here.
     * <p>
     * Defaults to: { "color": "#000000" }
     */
    public void setItemHoverStyle(Style itemHoverStyle) {
        this.itemHoverStyle = itemHoverStyle;
    }

    /**
     * @see #setItemMarginBottom(Number)
     */
    public Number getItemMarginBottom() {
        return itemMarginBottom;
    }

    /**
     * The pixel bottom margin for each legend item.
     * <p>
     * Defaults to: 0
     */
    public void setItemMarginBottom(Number itemMarginBottom) {
        this.itemMarginBottom = itemMarginBottom;
    }

    /**
     * @see #setItemMarginTop(Number)
     */
    public Number getItemMarginTop() {
        return itemMarginTop;
    }

    /**
     * The pixel top margin for each legend item.
     * <p>
     * Defaults to: 0
     */
    public void setItemMarginTop(Number itemMarginTop) {
        this.itemMarginTop = itemMarginTop;
    }

    /**
     * @see #setItemStyle(Style)
     */
    public Style getItemStyle() {
        if (itemStyle == null) {
            itemStyle = new Style();
        }
        return itemStyle;
    }

    /**
     * CSS styles for each legend item. Only a subset of CSS is supported,
     * notably those options related to text. The default
     * <code>textOverflow</code> property makes long texts truncate. Set it to
     * <code>null</code> to wrap text instead. A <code>width</code> property can
     * be added to control the text width.
     * <p>
     * Defaults to: { "color": "#333333", "cursor": "pointer", "fontSize":
     * "12px", "fontWeight": "bold", "textOverflow": "ellipsis" }
     */
    public void setItemStyle(Style itemStyle) {
        this.itemStyle = itemStyle;
    }

    /**
     * @see #setItemWidth(Number)
     */
    public Number getItemWidth() {
        return itemWidth;
    }

    /**
     * The width for each legend item. This is useful in a horizontal layout
     * with many items when you want the items to align vertically. .
     */
    public void setItemWidth(Number itemWidth) {
        this.itemWidth = itemWidth;
    }

    /**
     * @see #setLabelFormat(String)
     */
    public String getLabelFormat() {
        return labelFormat;
    }

    /**
     * A <a href=
     * "http://www.highcharts.com/docs/chart-concepts/labels-and-string-formatting"
     * >format string</a> for each legend label. Available variables relates to
     * properties on the series, or the point in case of pies.
     * <p>
     * Defaults to: {name}
     */
    public void setLabelFormat(String labelFormat) {
        this.labelFormat = labelFormat;
    }

    public String getLabelFormatter() {
        return _fn_labelFormatter;
    }

    public void setLabelFormatter(String _fn_labelFormatter) {
        this._fn_labelFormatter = _fn_labelFormatter;
    }

    /**
     * @see #setLayout(LayoutDirection)
     */
    public LayoutDirection getLayout() {
        return layout;
    }

    /**
     * The layout of the legend items. Can be one of "horizontal" or "vertical".
     * <p>
     * Defaults to: horizontal
     */
    public void setLayout(LayoutDirection layout) {
        this.layout = layout;
    }

    /**
     * @see #setLineHeight(Number)
     */
    public Number getLineHeight() {
        return lineHeight;
    }

    /**
     * Line height for the legend items. Deprecated as of 2.1. Instead, the line
     * height for each item can be set using itemStyle.lineHeight, and the
     * padding between items using itemMarginTop and itemMarginBottom.
     * <p>
     * Defaults to: 16
     */
    public void setLineHeight(Number lineHeight) {
        this.lineHeight = lineHeight;
    }

    /**
     * @see #setMargin(Number)
     */
    public Number getMargin() {
        return margin;
    }

    /**
     * If the plot area sized is calculated automatically and the legend is not
     * floating, the legend margin is the space between the legend and the axis
     * labels or plot area.
     * <p>
     * Defaults to: 12
     */
    public void setMargin(Number margin) {
        this.margin = margin;
    }

    /**
     * @see #setMaxHeight(Number)
     */
    public Number getMaxHeight() {
        return maxHeight;
    }

    /**
     * Maximum pixel height for the legend. When the maximum height is extended,
     * navigation will show.
     */
    public void setMaxHeight(Number maxHeight) {
        this.maxHeight = maxHeight;
    }

    /**
     * @see #setNavigation(LegendNavigation)
     */
    public LegendNavigation getNavigation() {
        if (navigation == null) {
            navigation = new LegendNavigation();
        }
        return navigation;
    }

    /**
     * Options for the paging or navigation appearing when the legend is
     * overflown. Navigation works well on screen, but not in static exported
     * images. One way of working around that is to <a href=
     * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/legend/navigation-enabled-false/"
     * >increase the chart height in export</a>.
     */
    public void setNavigation(LegendNavigation navigation) {
        this.navigation = navigation;
    }

    /**
     * @see #setPadding(Number)
     */
    public Number getPadding() {
        return padding;
    }

    /**
     * The inner padding of the legend box.
     * <p>
     * Defaults to: 8
     */
    public void setPadding(Number padding) {
        this.padding = padding;
    }

    /**
     * @see #setReversed(Boolean)
     */
    public Boolean getReversed() {
        return reversed;
    }

    /**
     * Whether to reverse the order of the legend items compared to the order of
     * the series or points as defined in the configuration object.
     * <p>
     * Defaults to: false
     */
    public void setReversed(Boolean reversed) {
        this.reversed = reversed;
    }

    /**
     * @see #setRtl(Boolean)
     */
    public Boolean getRtl() {
        return rtl;
    }

    /**
     * Whether to show the symbol on the right side of the text rather than the
     * left side. This is common in Arabic and Hebraic.
     * <p>
     * Defaults to: false
     */
    public void setRtl(Boolean rtl) {
        this.rtl = rtl;
    }

    /**
     * @see #setShadow(Boolean)
     */
    public Boolean getShadow() {
        return shadow;
    }

    /**
     * Whether to apply a drop shadow to the legend. A
     * <code>backgroundColor</code> also needs to be applied for this to take
     * effect. Since 2.3 the shadow can be an object configuration containing
     * <code>color</code>, <code>offsetX</code>, <code>offsetY</code>,
     * <code>opacity</code> and <code>width</code>.
     * <p>
     * Defaults to: false
     */
    public void setShadow(Boolean shadow) {
        this.shadow = shadow;
    }

    /**
     * @see #setSquareSymbol(Boolean)
     */
    public Boolean getSquareSymbol() {
        return squareSymbol;
    }

    /**
     * When this is true, the legend symbol width will be the same as the symbol
     * height, which in turn defaults to the font size of the legend items.
     * <p>
     * Defaults to: true
     */
    public void setSquareSymbol(Boolean squareSymbol) {
        this.squareSymbol = squareSymbol;
    }

    /**
     * @see #setSymbolHeight(Number)
     */
    public Number getSymbolHeight() {
        return symbolHeight;
    }

    /**
     * The pixel height of the symbol for series types that use a rectangle in
     * the legend. Defaults to the font size of legend items.
     */
    public void setSymbolHeight(Number symbolHeight) {
        this.symbolHeight = symbolHeight;
    }

    /**
     * @see #setSymbolPadding(Number)
     */
    public Number getSymbolPadding() {
        return symbolPadding;
    }

    /**
     * The pixel padding between the legend item symbol and the legend item
     * text.
     * <p>
     * Defaults to: 5
     */
    public void setSymbolPadding(Number symbolPadding) {
        this.symbolPadding = symbolPadding;
    }

    /**
     * @see #setSymbolRadius(Number)
     */
    public Number getSymbolRadius() {
        return symbolRadius;
    }

    /**
     * The border radius of the symbol for series types that use a rectangle in
     * the legend. Defaults to half the <code>symbolHeight</code>.
     */
    public void setSymbolRadius(Number symbolRadius) {
        this.symbolRadius = symbolRadius;
    }

    /**
     * @see #setSymbolWidth(Number)
     */
    public Number getSymbolWidth() {
        return symbolWidth;
    }

    /**
     * The pixel width of the legend item symbol. When the
     * <code>squareSymbol</code> option is set, this defaults to the
     * <code>symbolHeight</code>, otherwise 16.
     */
    public void setSymbolWidth(Number symbolWidth) {
        this.symbolWidth = symbolWidth;
    }

    /**
     * @see #setTitle(LegendTitle)
     */
    public LegendTitle getTitle() {
        if (title == null) {
            title = new LegendTitle();
        }
        return title;
    }

    /**
     * A title to be added on top of the legend.
     */
    public void setTitle(LegendTitle title) {
        this.title = title;
    }

    /**
     * @see #setUseHTML(Boolean)
     */
    public Boolean getUseHTML() {
        return useHTML;
    }

    /**
     * <p>
     * Whether to <a href=
     * "http://www.highcharts.com/docs/chart-concepts/labels-and-string-formatting#html"
     * >use HTML</a> to render the legend item texts. Prior to 4.1.7, when using
     * HTML, <a href="#legend.navigation">legend.navigation</a> was disabled.
     * </p>
     * <p>
     * Defaults to: false
     */
    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }

    /**
     * @see #setVerticalAlign(VerticalAlign)
     */
    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    /**
     * <p>
     * The vertical alignment of the legend box. Can be one of <code>top</code>,
     * <code>middle</code> or <code>bottom</code>. Vertical position can be
     * further determined by the <code>y</code> option.
     * </p>
     *
     * <p>
     * In the case that the legend is aligned in a corner position, the
     * <code>layout</code> option will determine whether to place it above/below
     * or on the side of the plot area.
     * </p>
     * <p>
     * Defaults to: bottom
     */
    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * @see #setWidth(Number)
     */
    public Number getWidth() {
        return width;
    }

    /**
     * The width of the legend box.
     */
    public void setWidth(Number width) {
        this.width = width;
    }

    /**
     * @see #setX(Number)
     */
    public Number getX() {
        return x;
    }

    /**
     * The x offset of the legend relative to its horizontal alignment
     * <code>align</code> within chart.spacingLeft and chart.spacingRight.
     * Negative x moves it to the left, positive x moves it to the right.
     * <p>
     * Defaults to: 0
     */
    public void setX(Number x) {
        this.x = x;
    }

    /**
     * @see #setY(Number)
     */
    public Number getY() {
        return y;
    }

    /**
     * The vertical offset of the legend relative to it's vertical alignment
     * <code>verticalAlign</code> within chart.spacingTop and
     * chart.spacingBottom. Negative y moves it up, positive y moves it down.
     * <p>
     * Defaults to: 0
     */
    public void setY(Number y) {
        this.y = y;
    }
}
