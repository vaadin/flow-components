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
import com.vaadin.flow.component.charts.model.style.ButtonTheme;

/**
 * Options for the export button.
 */
public class ContextButton extends AbstractConfigurationObject {

    private HorizontalAlign align;
    private Boolean enabled;
    private Number height;
    private ContextButtonMenuItem[] menuItems;
    private String symbol;
    private Color symbolFill;
    private Number symbolSize;
    private Color symbolStroke;
    private Number symbolStrokeWidth;
    private Number symbolX;
    private Number symbolY;
    private String text;
    private ButtonTheme theme;
    private VerticalAlign verticalAlign;
    private Number width;
    private Number x;
    private Number y;

    public ContextButton() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * Alignment for the buttons.
     * <p>
     * Defaults to: right
     */
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    public ContextButton(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Whether to enable buttons.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setHeight(Number)
     */
    public Number getHeight() {
        return height;
    }

    /**
     * Pixel height of the buttons.
     * <p>
     * Defaults to: 20
     */
    public void setHeight(Number height) {
        this.height = height;
    }

    /**
     * @see #setMenuItems(ContextButtonMenuItem[])
     */
    public ContextButtonMenuItem[] getMenuItems() {
        return menuItems;
    }

    /**
     * <p>
     * A collection of config options for the menu items. Each options object
     * consists of a <code>text</code> option which is a string to show in the
     * menu item, as well as an <code>onclick</code> parameter which is a
     * callback function to run on click.
     * </p>
     * <p>
     * By default, there is the "Print" menu item plus one menu item for each of
     * the available export types. Menu items can be customized by defining a
     * new array of items and assigning <code>null</code> to unwanted positions
     * (see override example below).
     * </p>
     */
    public void setMenuItems(ContextButtonMenuItem[] menuItems) {
        this.menuItems = menuItems;
    }

    /**
     * @see #setSymbol(String)
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * The symbol for the button. Points to a definition function in the
     * <code>Highcharts.Renderer.symbols</code> collection. The default
     * <code>exportIcon</code> function is part of the exporting module.
     * <p>
     * Defaults to: menu
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @see #setSymbolFill(Color)
     */
    public Color getSymbolFill() {
        return symbolFill;
    }

    /**
     * See <a class="internal" href=
     * "#navigation.buttonOptions">navigation.buttonOptions</a> =&gt;
     * symbolFill.
     * <p>
     * Defaults to: #666666
     */
    public void setSymbolFill(Color symbolFill) {
        this.symbolFill = symbolFill;
    }

    /**
     * @see #setSymbolSize(Number)
     */
    public Number getSymbolSize() {
        return symbolSize;
    }

    /**
     * The pixel size of the symbol on the button.
     * <p>
     * Defaults to: 14
     */
    public void setSymbolSize(Number symbolSize) {
        this.symbolSize = symbolSize;
    }

    /**
     * @see #setSymbolStroke(Color)
     */
    public Color getSymbolStroke() {
        return symbolStroke;
    }

    /**
     * The color of the symbol's stroke or line.
     * <p>
     * Defaults to: #666666
     */
    public void setSymbolStroke(Color symbolStroke) {
        this.symbolStroke = symbolStroke;
    }

    /**
     * @see #setSymbolStrokeWidth(Number)
     */
    public Number getSymbolStrokeWidth() {
        return symbolStrokeWidth;
    }

    /**
     * The pixel stroke width of the symbol on the button.
     * <p>
     * Defaults to: 1
     */
    public void setSymbolStrokeWidth(Number symbolStrokeWidth) {
        this.symbolStrokeWidth = symbolStrokeWidth;
    }

    /**
     * @see #setSymbolX(Number)
     */
    public Number getSymbolX() {
        return symbolX;
    }

    /**
     * The x position of the center of the symbol inside the button.
     * <p>
     * Defaults to: 12.5
     */
    public void setSymbolX(Number symbolX) {
        this.symbolX = symbolX;
    }

    /**
     * @see #setSymbolY(Number)
     */
    public Number getSymbolY() {
        return symbolY;
    }

    /**
     * The y position of the center of the symbol inside the button.
     * <p>
     * Defaults to: 10.5
     */
    public void setSymbolY(Number symbolY) {
        this.symbolY = symbolY;
    }

    public ContextButton(String text) {
        this.text = text;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * A text string to add to the individual button.
     * <p>
     * Defaults to: null
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @see #setTheme(ButtonTheme)
     */
    public ButtonTheme getTheme() {
        if (theme == null) {
            theme = new ButtonTheme();
        }
        return theme;
    }

    /**
     * A configuration object for the button theme. The object accepts SVG
     * properties like <code>stroke-width</code>, <code>stroke</code> and
     * <code>fill</code>. Tri-state button styles are supported by the
     * <code>states.hover</code> and <code>states.select</code> objects.
     */
    public void setTheme(ButtonTheme theme) {
        this.theme = theme;
    }

    /**
     * @see #setVerticalAlign(VerticalAlign)
     */
    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    /**
     * The vertical alignment of the buttons. Can be one of "top", "middle" or
     * "bottom".
     * <p>
     * Defaults to: top
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
     * The pixel width of the button.
     * <p>
     * Defaults to: 24
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
     * The horizontal position of the button relative to the <code>align</code>
     * option.
     * <p>
     * Defaults to: -10
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
     * The vertical offset of the button's position relative to its
     * <code>verticalAlign</code>. .
     * <p>
     * Defaults to: 0
     */
    public void setY(Number y) {
        this.y = y;
    }
}
