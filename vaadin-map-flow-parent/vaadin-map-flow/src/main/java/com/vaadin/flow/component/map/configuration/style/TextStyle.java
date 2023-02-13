package com.vaadin.flow.component.map.configuration.style;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;

import java.io.Serializable;

/**
 * Text style that defines how to render texts, such as labels, on the map.
 */
public class TextStyle extends AbstractConfigurationObject {
    private String font;
    private TextOffset offset;
    private double scale;
    private double rotation;
    private boolean rotateWithView;
    private TextAlign textAlign;
    private TextBaseline textBaseline;
    private Fill fill;
    private Stroke stroke;
    private Fill backgroundFill;
    private Stroke backgroundStroke;
    private int padding;

    public TextStyle() {
        font = "13px sans-serif";
        offset = new TextOffset(0, 10);
        scale = 1;
        rotation = 0;
        rotateWithView = false;
        textAlign = TextAlign.CENTER;
        fill = new Fill("#333");
        stroke = new Stroke("#fff", 3);
        padding = 0;
    }

    @Override
    public String getType() {
        return Constants.OL_STYLE_TEXT;
    }

    /**
     * The font style as CSS `font` value.
     *
     * @return the font style
     */
    public String getFont() {
        return font;
    }

    /**
     * Sets the font style as CSS `font` value. See <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/font">CanvasRenderingContext2D.font</a>.
     * Default is {@code 13px sans-serif}.
     *
     * @param font
     *            the new font style
     */
    public void setFont(String font) {
        this.font = font;
        markAsDirty();
    }

    /**
     * The offset of the text from its anchor point.
     *
     * @return the offset
     */
    public TextOffset getOffset() {
        return offset;
    }

    /**
     * Sets the offset of the text from whatever it is anchored to, in pixels.
     * For example, a marker label is anchored to the marker's position, and
     * then shifted by the specified offset. Default is {@code {x: 0, y: 10}}
     *
     * @param offset
     *            the new offset
     */
    public void setOffset(TextOffset offset) {
        this.offset = offset;
        markAsDirty();
    }

    /**
     * Sets the offset of the text from whatever it is anchored to, in pixels.
     * For example, a marker label is anchored to the marker's position, and
     * then shifted by the specified offset. Default is {@code {x: 0, y: 10}}
     *
     * @param x
     *            the horizontal offset in pixels
     * @param y
     *            the vertical offset in pixels
     */
    public void setOffset(int x, int y) {
        setOffset(new TextOffset(x, y));
    }

    /**
     * The scaling to apply to the text.
     *
     * @return the scale value
     */
    public double getScale() {
        return scale;
    }

    /**
     * Sets the scaling factor to apply to the text. Default value is {@code 1}.
     *
     * @param scale
     *            the new scaling factor.
     */
    public void setScale(double scale) {
        this.scale = scale;
        markAsDirty();
    }

    /**
     * The rotation to apply to the text, in radians.
     *
     * @return the rotation, in radians
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation to apply to the text, in radians. Default value is
     * {@code 0}.
     *
     * @param rotation
     *            the new rotation, in radians
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
        markAsDirty();
    }

    /**
     * Whether to rotate the text with the view.
     *
     * @return whether to rotate the text with the view
     */
    public boolean isRotateWithView() {
        return rotateWithView;
    }

    /**
     * Sets whether to rotate the text with the view. Default value is
     * {@code false}.
     *
     * @param rotateWithView
     *            whether to rotate the text with the view
     */
    public void setRotateWithView(boolean rotateWithView) {
        this.rotateWithView = rotateWithView;
        markAsDirty();
    }

    /**
     * The horizontal text alignment based from the text's anchor point.
     *
     * @return the horizontal alignment
     */
    public TextAlign getTextAlign() {
        return textAlign;
    }

    /**
     * Sets the horizontal alignment based from the text's anchor point,
     * including the offset set using {@link #setOffset(TextOffset)}. Default
     * value is {@link TextAlign#CENTER}.
     *
     * @param textAlign
     *            the new alignment
     */
    public void setTextAlign(TextAlign textAlign) {
        this.textAlign = textAlign;
        markAsDirty();
    }

    /**
     * The vertical baseline for aligning the text based on its anchor point.
     *
     * @return the vertical baseline
     */
    public TextBaseline getTextBaseline() {
        return textBaseline;
    }

    /**
     * Sets the vertical baseline for aligning the text based on its anchor
     * point, including the offset set using {@link #setOffset(TextOffset)}.
     * Default value is {@link TextBaseline#MIDDLE}.
     *
     * @param textBaseline
     *            the new baseline
     */
    public void setTextBaseline(TextBaseline textBaseline) {
        this.textBaseline = textBaseline;
        markAsDirty();
    }

    /**
     * The fill color to use for rendering the text.
     *
     * @return the text's fill color
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Fill getFill() {
        return fill;
    }

    /**
     * Sets the fill color to use for rendering the text. This is effectively
     * the text's color, which can be complemented by an outline by setting
     * {@link #setStroke(Stroke)}. The fill's color value must be a valid CSS
     * color. The default fill color is {@code #333}.
     *
     * @param fill
     *            the new fill color
     */
    public void setFill(Fill fill) {
        removeChild(this.fill);
        this.fill = fill;
        addNullableChild(fill);
    }

    /**
     * Sets the fill color to use for rendering the text. This is effectively
     * the text's color, which can be complemented by an outline by setting
     * {@link #setStroke(Stroke)}. The fill's color value must be a valid CSS
     * color. The default fill color is {@code #333}.
     *
     * @param fillColor
     *            the new fill color
     */
    public void setFill(String fillColor) {
        setFill(new Fill(fillColor));
    }

    /**
     * The stroke to use for rendering the text.
     *
     * @return the text's stroke
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the stroke to use for rendering the text. This effectively renders
     * an outline around the text, see {@link #setFill(Fill)} for setting the
     * text's main color. The stroke's color must be a valid CSS color, and its
     * width is specified in pixels. The default stroke's color is {@code #fff},
     * and the default width is {@code 3} pixels.
     *
     * @param stroke
     *            the new stroke
     */
    public void setStroke(Stroke stroke) {
        removeChild(this.stroke);
        this.stroke = stroke;
        addNullableChild(stroke);
    }

    /**
     * Sets the stroke to use for rendering the text. This effectively renders
     * an outline around the text, see {@link #setFill(Fill)} for setting the
     * text's main color. The stroke's color must be a valid CSS color, and its
     * width is specified in pixels. The default stroke's color is {@code #fff},
     * and the default width is {@code 3} pixels.
     *
     * @param strokeColor
     *            the new stroke color
     * @param strokeWidth
     *            the new stroke width
     */
    public void setStroke(String strokeColor, int strokeWidth) {
        setStroke(new Stroke(strokeColor, strokeWidth));
    }

    /**
     * The fill color to use for the background of the text.
     *
     * @return the text's background fill
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Fill getBackgroundFill() {
        return backgroundFill;
    }

    /**
     * Sets the fill color to use for the background of the text. The fill's
     * color must be a valid CSS color. By default, the text does not use a
     * background color.
     *
     * @param backgroundFill
     *            the new background fill color
     */
    public void setBackgroundFill(Fill backgroundFill) {
        removeChild(this.backgroundFill);
        this.backgroundFill = backgroundFill;
        addNullableChild(backgroundFill);
    }

    /**
     * Sets the fill color to use for the background of the text. The fill's
     * color must be a valid CSS color. By default, the text does not use a
     * background color.
     *
     * @param fillColor
     *            the new background fill color
     */
    public void setBackgroundFill(String fillColor) {
        setBackgroundFill(new Fill(fillColor));
    }

    /**
     * The stroke with which to render the texts background border.
     *
     * @return the text's background stroke
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Stroke getBackgroundStroke() {
        return backgroundStroke;
    }

    /**
     * Sets the stroke with which to render the texts background border. The
     * stroke's color must be a valid CSS color, and its width is specified in
     * pixels. By default, the text does not use a background stroke.
     *
     * @param backgroundStroke
     *            the new background stroke
     */
    public void setBackgroundStroke(Stroke backgroundStroke) {
        removeChild(this.backgroundStroke);
        this.backgroundStroke = backgroundStroke;
        addNullableChild(backgroundStroke);
    }

    /**
     * Sets the stroke with which to render the texts background border. The
     * stroke's color must be a valid CSS color, and its width is specified in
     * pixels. By default, the text does not use a background stroke.
     *
     * @param strokeColor
     *            the new background stroke color
     * @param strokeWidth
     *            the new background stroke width
     */
    public void setBackgroundStroke(String strokeColor, int strokeWidth) {
        setBackgroundStroke(new Stroke(strokeColor, strokeWidth));
    }

    /**
     * The padding for the text's background.
     *
     * @return the background padding
     */
    public int getPadding() {
        return padding;
    }

    /**
     * Sets the padding for the text's background. The padding is applied to all
     * sides of the background. Default value is {@code 0}.
     *
     * @param padding
     *            the new padding
     */
    public void setPadding(int padding) {
        this.padding = padding;
        markAsDirty();
    }

    public enum TextAlign {
        CENTER, LEFT, RIGHT, START, END
    }

    public enum TextBaseline {
        MIDDLE, TOP, BOTTOM, ALPHABETIC, HANGING, IDEOGRAPHIC
    }

    public static class TextOffset implements Serializable {
        private final int x;
        private final int y;

        public TextOffset(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
