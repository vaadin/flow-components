package com.vaadin.flow.component.charts.model.style;

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

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;

/**
 * Style options for CSS styling
 */
@SuppressWarnings("serial")
public class Style extends AbstractConfigurationObject {
    private Color color;
    private FontWeight fontWeight;
    private String fontFamily;
    private String fontSize;
    private String left;
    private String top;
    private StylePosition position;
    private String lineHeight;
    private String textShadow;

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the <code>color</code> CSS attribute.
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setFontWeight(FontWeight)
     */
    public FontWeight getFontWeight() {
        return fontWeight;
    }

    /**
     * Sets the <code>font-weight</code> CSS attribute.
     *
     * @param fontWeight
     */
    public void setFontWeight(FontWeight fontWeight) {
        this.fontWeight = fontWeight;
    }

    /**
     * @see #setFontFamily(String)
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Sets the <code>font-family</code> CSS attribute.
     *
     * @param fontFamily
     */
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    /**
     * @see #setFontSize(String)
     */
    public String getFontSize() {
        return fontSize;
    }

    /**
     * Sets the <code>font-size</code> CSS attribute.
     *
     * @param fontSize
     */
    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * @see #setLeft(String)
     */
    public String getLeft() {
        return left;
    }

    /**
     * Sets the <code>left</code> CSS attribute
     *
     * @param left
     */
    public void setLeft(String left) {
        this.left = left;
    }

    /**
     * @see #setTop(String)
     */
    public String getTop() {
        return top;
    }

    /**
     * Sets the <code>top</code> CSS attribute
     *
     * @param top
     */
    public void setTop(String top) {
        this.top = top;
    }

    /**
     * Sets the <code>position</code> CSS attribute
     *
     * @param position
     */
    public void setPosition(StylePosition position) {
        this.position = position;
    }

    /**
     * @see #setPosition(StylePosition)
     */
    public StylePosition getPosition() {
        return position;
    }

    /**
     * Sets the <code>line-height</code> CSS attribute
     */
    public void setLineHeight(String lineHeight) {
        this.lineHeight = lineHeight;
    }

    /**
     * @see #setLineHeight(String)
     */
    public String getLineHeight() {
        return lineHeight;
    }

    /**
     * @see #setTextShadow(String)
     */
    public String getTextShadow() {
        return textShadow;
    }

    /**
     * Sets the <code>textShadow</code> CSS attribute
     *
     * @param textShadow
     */
    public void setTextShadow(String textShadow) {
        this.textShadow = textShadow;
    }
}
