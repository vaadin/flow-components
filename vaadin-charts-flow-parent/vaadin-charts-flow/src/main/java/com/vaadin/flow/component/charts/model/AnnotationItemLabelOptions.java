package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.Style;

public class AnnotationItemLabelOptions extends AbstractConfigurationObject {

    private AnnotationItemLabelAccessibility accessibility;
    private HorizontalAlign align;
    private Boolean allowOverlap;
    private Color backgroundColor;
    private Color borderColor;
    private Number borderRadius;
    private Number borderWidth;
    private String className;
    private Boolean crop;
    private Number distance;
    private String format;
    private String formatter;
    private Boolean includeInDataExport;
    private String overflow;
    private Number padding;
    private Shadow shadow;
    private Shape shape;
    private Style style;
    private String text;
    private Boolean useHTML;
    private VerticalAlign verticalAlign;
    private Number x;
    private Number y;

    public AnnotationItemLabelAccessibility getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(
            AnnotationItemLabelAccessibility accessibility) {
        this.accessibility = accessibility;
    }

    public HorizontalAlign getAlign() {
        return align;
    }

    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    public Boolean getAllowOverlap() {
        return allowOverlap;
    }

    public void setAllowOverlap(Boolean allowOverlap) {
        this.allowOverlap = allowOverlap;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Number getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(Number borderRadius) {
        this.borderRadius = borderRadius;
    }

    public Number getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Boolean getCrop() {
        return crop;
    }

    public void setCrop(Boolean crop) {
        this.crop = crop;
    }

    public Number getDistance() {
        return distance;
    }

    public void setDistance(Number distance) {
        this.distance = distance;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    public Boolean getIncludeInDataExport() {
        return includeInDataExport;
    }

    public void setIncludeInDataExport(Boolean includeInDataExport) {
        this.includeInDataExport = includeInDataExport;
    }

    public String getOverflow() {
        return overflow;
    }

    public void setOverflow(String overflow) {
        this.overflow = overflow;
    }

    public Number getPadding() {
        return padding;
    }

    public void setPadding(Number padding) {
        this.padding = padding;
    }

    public Shadow getShadow() {
        return shadow;
    }

    public void setShadow(Shadow shadow) {
        this.shadow = shadow;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getUseHTML() {
        return useHTML;
    }

    public void setUseHTML(Boolean useHTML) {
        this.useHTML = useHTML;
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    public Number getX() {
        return x;
    }

    public void setX(Number x) {
        this.x = x;
    }

    public Number getY() {
        return y;
    }

    public void setY(Number y) {
        this.y = y;
    }
}
