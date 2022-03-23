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

public class Labels extends AbstractConfigurationObject {

    private HorizontalAlign align;
    private Number[] autoRotation;
    private Number autoRotationLimit;
    private Number distance;
    private Boolean enabled;
    private String format;
    private String _fn_formatter;
    private Number padding;
    private Boolean reserveSpace;
    private Number staggerLines;
    private Number step;
    private Style style;
    private Boolean useHTML;
    private Number x;
    private Number y;
    private Number zIndex;
    private String rotation;

    public Labels() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * What part of the string the given position is anchored to. Can be one of
     * <code>"left"</code>, <code>"center"</code> or <code>"right"</code>. The
     * exact position also depends on the <code>labels.x</code> setting. Angular
     * gauges and solid gauges defaults to <code>center</code>.
     * <p>
     * Defaults to: right
     */
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    /**
     * @see #setAutoRotation(Number[])
     */
    public Number[] getAutoRotation() {
        return autoRotation;
    }

    /**
     * For horizontal axes, the allowed degrees of label rotation to prevent
     * overlapping labels. If there is enough space, labels are not rotated. As
     * the chart gets narrower, it will start rotating the labels -45 degrees,
     * then remove every second label and try again with rotations 0 and -45
     * etc. Set it to <code>false</code> to disable rotation, which will cause
     * the labels to word-wrap if possible.
     * <p>
     * Defaults to: [-45]
     */
    public void setAutoRotation(Number[] autoRotation) {
        this.autoRotation = autoRotation;
    }

    /**
     * @see #setAutoRotationLimit(Number)
     */
    public Number getAutoRotationLimit() {
        return autoRotationLimit;
    }

    /**
     * When each category width is more than this many pixels, we don't apply
     * auto rotation. Instead, we lay out the axis label with word wrap. A lower
     * limit makes sense when the label contains multiple short words that don't
     * extend the available horizontal space for each label.
     * <p>
     * Defaults to: 80
     */
    public void setAutoRotationLimit(Number autoRotationLimit) {
        this.autoRotationLimit = autoRotationLimit;
    }

    /**
     * @see #setDistance(Number)
     */
    public Number getDistance() {
        return distance;
    }

    /**
     * Angular gauges and solid gauges only. The label's pixel distance from the
     * perimeter of the plot area.
     * <p>
     * Defaults to: -25
     */
    public void setDistance(Number distance) {
        this.distance = distance;
    }

    public Labels(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the axis labels.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setFormat(String)
     */
    public String getFormat() {
        return format;
    }

    /**
     * A <a href=
     * "http://www.highcharts.com/docs/chart-concepts/labels-and-string-formatting"
     * >format string</a> for the axis label.
     * <p>
     * Defaults to: {value}
     */
    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatter() {
        return _fn_formatter;
    }

    public void setFormatter(String _fn_formatter) {
        this._fn_formatter = _fn_formatter;
    }

    /**
     * @see #setPadding(Number)
     */
    public Number getPadding() {
        return padding;
    }

    /**
     * The pixel padding for axis labels, to ensure white space between them.
     * <p>
     * Defaults to: 5
     */
    public void setPadding(Number padding) {
        this.padding = padding;
    }

    /**
     * @see #setReserveSpace(Boolean)
     */
    public Boolean getReserveSpace() {
        return reserveSpace;
    }

    /**
     * Whether to reserve space for the labels. This can be turned off when for
     * example the labels are rendered inside the plot area instead of outside.
     * <p>
     * Defaults to: true
     */
    public void setReserveSpace(Boolean reserveSpace) {
        this.reserveSpace = reserveSpace;
    }

    /**
     * @see #setStaggerLines(Number)
     */
    public Number getStaggerLines() {
        return staggerLines;
    }

    /**
     * Horizontal axes only. The number of lines to spread the labels over to
     * make room or tighter labels.
     */
    public void setStaggerLines(Number staggerLines) {
        this.staggerLines = staggerLines;
    }

    /**
     * @see #setStep(Number)
     */
    public Number getStep() {
        return step;
    }

    /**
     * <p>
     * To show only every <em>n</em>'th label on the axis, set the step to
     * <em>n</em>. Setting the step to 2 shows every other label.
     * </p>
     *
     * <p>
     * By default, the step is calculated automatically to avoid overlap. To
     * prevent this, set it to 1. This usually only happens on a category axis,
     * and is often a sign that you have chosen the wrong axis type. Read more
     * at <a href="http://www.highcharts.com/docs/chart-concepts/axes">Axis
     * docs</a> => What axis should I use?
     * </p>
     */
    public void setStep(Number step) {
        this.step = step;
    }

    /**
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        if (style == null) {
            style = new Style();
        }
        return style;
    }

    /**
     * <p>
     * CSS styles for the label. Use <code>whiteSpace: 'nowrap'</code> to
     * prevent wrapping of category labels. Use
     * <code>textOverflow: 'none'</code> to prevent ellipsis (dots).
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the labels are styled with the
     * <code>.highcharts-axis-labels</code> class.
     * </p>
     * <p>
     * Defaults to: { "color": "#666666", "cursor": "default", "fontSize":
     * "11px" }
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see #setUseHTML(Boolean)
     */
    public Boolean getUseHTML() {
        return useHTML;
    }

    /**
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
     * @see #setX(Number)
     */
    public Number getX() {
        return x;
    }

    /**
     * The x position offset of the label relative to the tick position on the
     * axis. Defaults to -15 for left axis, 15 for right axis.
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
     * The y position offset of the label relative to the tick position on the
     * axis.
     * <p>
     * Defaults to: 3
     */
    public void setY(Number y) {
        this.y = y;
    }

    /**
     * @see #setZIndex(Number)
     */
    public Number getZIndex() {
        return zIndex;
    }

    /**
     * The Z index for the axis labels.
     * <p>
     * Defaults to: 7
     */
    public void setZIndex(Number zIndex) {
        this.zIndex = zIndex;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public void setRotation(Number rotation) {
        this.rotation = rotation + "";
    }

    public void setRotationPerpendicular() {
        this.rotation = "auto";
    }
}
