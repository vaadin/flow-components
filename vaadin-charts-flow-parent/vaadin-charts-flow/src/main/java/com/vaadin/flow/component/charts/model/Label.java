package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
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

/**
 * Text labels for the plot bands
 */
public class Label extends AbstractConfigurationObject {

    private HorizontalAlign align;
    private Number rotation;
    private String text;
    private TextAlign textAlign;
    private Boolean useHTML;
    private VerticalAlign verticalAlign;
    private Number x;
    private Number y;

    public Label() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * Horizontal alignment of the label. Can be one of "left", "center" or
     * "right".
     * <p>
     * Defaults to: left
     */
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    /**
     * @see #setRotation(Number)
     */
    public Number getRotation() {
        return rotation;
    }

    /**
     * Rotation of the text label in degrees. Defaults to 0 for horizontal plot
     * lines and 90 for vertical lines.
     */
    public void setRotation(Number rotation) {
        this.rotation = rotation;
    }

    public Label(String text) {
        this.text = text;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * The text itself. A subset of HTML is supported.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @see #setTextAlign(TextAlign)
     */
    public TextAlign getTextAlign() {
        return textAlign;
    }

    /**
     * The text alignment for the label. While <code>align</code> determines
     * where the texts anchor point is placed within the plot band,
     * <code>textAlign</code> determines how the text is aligned against its
     * anchor point. Possible values are "left", "center" and "right". Defaults
     * to the same as the <code>align</code> option.
     */
    public void setTextAlign(TextAlign textAlign) {
        this.textAlign = textAlign;
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
     * >use HTML</a> to render the labels.
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
     * Vertical alignment of the label relative to the plot band. Can be one of
     * "top", "middle" or "bottom".
     * <p>
     * Defaults to: top
     */
    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * @see #setX(Number)
     */
    public Number getX() {
        return x;
    }

    /**
     * Horizontal position relative the alignment. Default varies by
     * orientation.
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
     * Vertical position of the text baseline relative to the alignment. Default
     * varies by orientation.
     */
    public void setY(Number y) {
        this.y = y;
    }
}
