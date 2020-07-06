package com.vaadin.flow.component.charts.model.style;

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

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;

/**
 * Style options for CSS styling
 */
@SuppressWarnings("serial")
public class LabelStyle extends AbstractConfigurationObject {
    private Color color;
    private FontWeight fontWeight;
    private String fontFamily;
    private String fontSize;
    private String left;
    private String top;
    private StylePosition position;
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
     * @see #setTextShadow(String)
     */
    public String getTextShadow(){
        return textShadow;
    }

    /**
     * Sets the <code>textShadow</code> CSS attribute
     *
     * @param textShadow
     */
    public void setTextShadow(String textShadow) {
        this.textShadow=textShadow;
    }
}
